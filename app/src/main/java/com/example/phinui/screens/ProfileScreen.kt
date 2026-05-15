package com.example.phinui.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val user = auth.currentUser

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "No email") }
    var major by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var pendingPhotoUrl by remember { mutableStateOf<String?>(null) }
    var photoMarkedForRemoval by remember { mutableStateOf(false) }

    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val displayPhotoUrl = if (isEditing) pendingPhotoUrl else photoUrl

    var showConfirm by remember { mutableStateOf(false) }
    var showPhotoMenu by remember { mutableStateOf(false) }

    var majorMenuExpanded by remember { mutableStateOf(false) }
    val majorOptions = listOf(
        "No Major",
        "Anthropology",
        "Applied Physics",
        "Art",
        "Biology",
        "Business",
        "Chemistry",
        "Chicana/o Studies",
        "Communication",
        "Computer Science",
        "Dance Studies",
        "Early Childhood Studies",
        "Economics",
        "English",
        "Environmental Science & Resource Management",
        "Global Studies",
        "Health Science",
        "History",
        "Information Technology",
        "Liberal Studies",
        "Mathematics",
        "Mechatronics Engineering",
        "Music",
        "Nursing",
        "Political Science",
        "Psychology",
        "Sociology",
        "Spanish",
        "Theatre and Performance Studies"
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        val uid = user?.uid ?: return@rememberLauncherForActivityResult
        isUploadingPhoto = true
        errorMessage = null

        val photoRef = storage.reference.child("profile_photos/$uid")

        photoRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Photo upload failed.")
                }
                photoRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val newPhotoUrl = downloadUri.toString()

                pendingPhotoUrl = newPhotoUrl
                photoMarkedForRemoval = false
                isUploadingPhoto = false
            }
            .addOnFailureListener { e ->
                isUploadingPhoto = false
                errorMessage = e.message ?: "Failed to upload photo."
            }
    }

    fun removeProfilePhoto() {
        pendingPhotoUrl = null
        photoMarkedForRemoval = true
    }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            isLoading = true
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    name = document.getString("name") ?: ""
                    major = document.getString("major") ?: ""
                    bio = document.getString("bio") ?: ""
                    photoUrl = document.getString("photoUrl")
                    email = user.email ?: "No email"
                    isLoading = false
                }
                .addOnFailureListener {
                    errorMessage = "Failed to load profile."
                    isLoading = false
                }
        } ?: run {
            isLoading = false
            errorMessage = "No logged-in user found."
        }
    }

    val initial = name.firstOrNull()?.uppercase() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = "Remove Photo?",
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove your profile photo?",
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirm = false }
                    ) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirm = false
                            removeProfilePhoto()
                        }
                    ) {
                        Text(
                            text = "Remove",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }

        Text(
            text = "My Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (!displayPhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = displayPhotoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        if (isEditing) {
                            showPhotoMenu = true
                        } else {
                            isEditing = true
                            pendingPhotoUrl = photoUrl
                            photoMarkedForRemoval = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Outlined.CameraAlt else Icons.Outlined.Edit,
                    contentDescription = if (isEditing) "Change profile photo" else "Edit profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                DropdownMenu(
                    expanded = showPhotoMenu,
                    onDismissRequest = { showPhotoMenu = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 6.dp
                ) {
                    DropdownMenuItem(
                        text = { Text("Upload Photo") },
                        onClick = {
                            showPhotoMenu = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    if (!displayPhotoUrl.isNullOrBlank()) {
                        DropdownMenuItem(
                            text = { Text("Remove Photo", color = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showPhotoMenu = false
                                showConfirm = true
                            }
                        )
                    }
                }
            }
        }

        if (isUploadingPhoto) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Uploading photo...",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (name.isNotBlank()) name else "No Name",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onTertiary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFEAEAEA)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Text("Loading profile...", color = Color.Gray)
        } else {
            AnimatedContent(
                targetState = isEditing,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 6 })
                        .togetherWith(fadeOut() + slideOutVertically { -it / 6 })
                },
                label = "profile_edit_transition"
            ) { editing ->
                if (editing) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = majorMenuExpanded,
                            onExpandedChange = { majorMenuExpanded = !majorMenuExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (major.isBlank()) "No Major" else major,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Major") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = majorMenuExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                    focusedTextColor = MaterialTheme.colorScheme.onTertiary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onTertiary,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = majorMenuExpanded,
                                onDismissRequest = { majorMenuExpanded = false },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                majorOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            major = if (option == "No Major") "" else option
                                            majorMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 6
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileInfoRow(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            label = "Full Name",
                            value = if (name.isNotBlank()) name else "Not added"
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        ProfileInfoRow(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            label = "Email Address",
                            value = email
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        ProfileInfoRow(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            label = "Major",
                            value = if (major.isNotBlank()) major else "Not added"
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        ProfileInfoRow(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.ShortText,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            label = "Bio",
                            value = if (bio.isNotBlank()) bio else "Not added"
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFEAEAEA)
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = {
                        user?.uid?.let { uid ->
                            db.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    name = document.getString("name") ?: ""
                                    major = document.getString("major") ?: ""
                                    bio = document.getString("bio") ?: ""
                                    photoUrl = document.getString("photoUrl")
                                    pendingPhotoUrl = photoUrl
                                    photoMarkedForRemoval = false
                                    isEditing = false
                                    errorMessage = null
                                }
                                .addOnFailureListener {
                                    isEditing = false
                                }
                        } ?: run {
                            isEditing = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        val uid = user?.uid ?: return@Button
                        isSaving = true
                        errorMessage = null

                        val updatedProfile = mapOf(
                            "name" to name.trim(),
                            "major" to major.trim(),
                            "bio" to bio.trim(),
                            "photoUrl" to pendingPhotoUrl
                        )

                        db.collection("users")
                            .document(uid)
                            .set(updatedProfile, SetOptions.merge())
                            .addOnSuccessListener {
                                photoUrl = pendingPhotoUrl
                                photoMarkedForRemoval = false
                                isSaving = false
                                isEditing = false
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                errorMessage = e.message ?: "Failed to save profile."
                            }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    enabled = !isSaving && !isUploadingPhoto
                ) {
                    Text(if (isSaving) "Saving..." else "Save")
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

