package com.example.phinui.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@Composable
fun VendingStockScreen(
    locationId: String,
    navController: NavHostController
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(locationId) {
        isLoading = true
        imageUrl = null
        errorMessage = null

        try {
            Log.d("VendingStockScreen", "locationId = $locationId")

            if (locationId.isBlank()) {
                errorMessage = "Missing vending machine ID."
                return@LaunchedEffect
            }

            val path = "vending_stock/$locationId.jpg"
            Log.d("VendingStockScreen", "Loading storage path: $path")

            val ref = FirebaseStorage.getInstance()
                .reference
                .child(path)

            val url = ref.downloadUrl.await().toString()
            Log.d("VendingStockScreen", "Download URL loaded: $url")

            imageUrl = url
        } catch (e: Exception) {
            Log.e("VendingStockScreen", "Failed to load vending image", e)
            errorMessage = e.message ?: "Unknown error loading image."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Vending Stock",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Machine ID: $locationId",
            style = MaterialTheme.typography.bodyMedium
        )

        when {
            isLoading -> {
                CircularProgressIndicator()
                Text("Loading image...")
            }

            imageUrl != null -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Vending machine stock",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            errorMessage != null -> {
                Text("Failed to load image.")
                Text(errorMessage!!)
            }

            else -> {
                Text("No stock image available.")
            }
        }

        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}