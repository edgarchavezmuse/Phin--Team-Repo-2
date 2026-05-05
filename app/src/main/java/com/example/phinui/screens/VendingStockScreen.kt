package com.example.phinui.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@Composable
fun VendingStockScreen(
    locationId: String,
    navController: NavHostController
) {
    var machineName by remember(locationId) { mutableStateOf("Vending Stock") }
    var imageUrl by remember(locationId) { mutableStateOf<String?>(null) }
    var isLoading by remember(locationId) { mutableStateOf(true) }
    var errorMessage by remember(locationId) { mutableStateOf<String?>(null) }

    LaunchedEffect(locationId) {
        isLoading = true
        imageUrl = null
        errorMessage = null

        try {
            val firestore = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance(
                "gs://our-awesome-app-2-269fe.firebasestorage.app"
            )

            val doc = firestore
                .collection("campus.locations")
                .document(locationId)
                .get()
                .await()

            machineName = doc.getString("name") ?: "Vending Stock"

            val imagePath = doc.getString("imagePath")

            if (imagePath.isNullOrBlank()) {
                errorMessage = "No image configured for this machine."
            } else {
                Log.d("VendingStockScreen", "Loading imagePath = $imagePath")
                val ref = storage.reference.child(imagePath)
                imageUrl = ref.downloadUrl.await().toString()
            }
        } catch (e: Exception) {
            Log.e("VendingStockScreen", "Failed to load vending image", e)
            errorMessage = e.message ?: "Failed to load image."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F1))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        imageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Vending machine stock",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 460.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = machineName,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading image...")
                    }

                    imageUrl != null -> {
                        Text(
                            text = "Current vending machine stock image.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    else -> {
                        Text(
                            text = errorMessage ?: "No stock image available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}