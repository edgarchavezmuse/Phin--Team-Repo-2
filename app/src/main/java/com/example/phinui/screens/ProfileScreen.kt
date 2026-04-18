package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.phinui.ui.navigation.Routes
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "No email") }
    var major by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

        Surface(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = Color(0xFFFFEFEF)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFD32F2F)
                )
            }
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
            if (isEditing) {
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
            } else {
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
                        .set(updatedProfile, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            isSaving = false
                            isEditing = false
                        }
                        .addOnFailureListener {
                            isSaving = false
                            errorMessage = "Failed to save profile."
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                ),
                enabled = !isSaving
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = "Save profile"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSaving) "Saving..." else "Save Changes")
            }
        } else {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit profile"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = "Log out"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out",
                style = MaterialTheme.typography.titleMedium
            )
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
                .background(Color(0xFFF7F7F7)),
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