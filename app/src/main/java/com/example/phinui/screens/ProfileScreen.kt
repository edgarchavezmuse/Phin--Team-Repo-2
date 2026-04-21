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
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.phinui.ui.navigation.Routes
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.example.phinui.ui.theme.PrimaryRed
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

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

    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

                db.collection("users")
                    .document(uid)
                    .set(mapOf("photoUrl" to newPhotoUrl), SetOptions.merge())
                    .addOnSuccessListener {
                        photoUrl = newPhotoUrl
                        isUploadingPhoto = false
                    }
                    .addOnFailureListener { e ->
                        isUploadingPhoto = false
                        errorMessage = e.message ?: "Failed to save photo URL."
                    }
            }
            .addOnFailureListener { e ->
                isUploadingPhoto = false
                errorMessage = e.message ?: "Failed to upload photo."
            }
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
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = Color(0xFFFFEFEF)
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
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
                            color = Color(0xFFD32F2F)
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
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            isEditing = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Outlined.CameraAlt else Icons.Outlined.Edit,
                    contentDescription = if (isEditing) "Change profile photo" else "Edit profile",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(16.dp)
                )
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
            color = Color.Black
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

                        OutlinedTextField(
                            value = major,
                            onValueChange = { major = it },
                            label = { Text("Major") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

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
                                    tint = Color(0xFFD32F2F)
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
                                    tint = Color(0xFFD32F2F)
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
                                    tint = Color(0xFFD32F2F)
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
                                    tint = Color(0xFFD32F2F)
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
                color = Color.Red,
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
                        color = Color(0xFFD32F2F),
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
                            "bio" to bio.trim()
                        )

                        db.collection("users")
                            .document(uid)
                            .set(updatedProfile, SetOptions.merge())
                            .addOnSuccessListener {
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
                        containerColor = PrimaryRed,
                        contentColor = Color.White
                    ),
                    enabled = !isSaving && !isUploadingPhoto
                ) {
                    Text(if (isSaving) "Saving..." else "Save")
                }
            }
        } else {
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = "Log out"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
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
                .background(Color(0xFFFFEBEE)),
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
                color = Color.Black
            )
        }
    }
}