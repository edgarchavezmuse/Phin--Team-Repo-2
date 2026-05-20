

package com.example.phinui.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PinSuggestionsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var building by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Suggest a Pin",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = building,
            onValueChange = { building = it },
            label = { Text("Building") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Amenity type, ex: restroom, microwave, vending") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Small location description, ex: near room 1231") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = {
                if (uid == null) {
                    Toast.makeText(context, "You must be logged in.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (building.isBlank() || category.isBlank() || description.isBlank()) {
                    Toast.makeText(context, "Please fill out the required fields.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isSubmitting = true

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val suggestionId = "${uid}_$today"

                val suggestion = hashMapOf(
                    "building" to building.trim(),
                    "category" to category.trim().lowercase(),
                    "description" to description.trim(),
                    "createdBy" to uid,
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "dailyKey" to today
                )

                db.collection("pin.Suggestions")
                    .document(suggestionId)
                    .set(suggestion)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Pin suggestion submitted.",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "You can only submit one pin suggestion per day.",
                            Toast.LENGTH_LONG
                        ).show()
                        isSubmitting = false
                    }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit Suggestion")
        }
    }
}