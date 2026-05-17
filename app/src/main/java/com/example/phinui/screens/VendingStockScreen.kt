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

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton

@Composable
fun VendingStockScreen(
    locationId: String,
    navController: NavHostController
) {
    var machineName by remember(locationId) { mutableStateOf("Vending Stock") }
    var snackImageUrl by remember(locationId) { mutableStateOf<String?>(null) }
    var drinkImageUrl by remember(locationId) { mutableStateOf<String?>(null) }
    var selectedStock by remember { mutableStateOf("snacks") }
    var isLoading by remember(locationId) { mutableStateOf(true) }
    var errorMessage by remember(locationId) { mutableStateOf<String?>(null) }

    val currentImageUrl = if (selectedStock == "snacks") snackImageUrl else drinkImageUrl

    LaunchedEffect(locationId) {
        isLoading = true
        snackImageUrl = null
        drinkImageUrl = null
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

            val snackPath = doc.getString("imagePath")
            val drinkPath = doc.getString("drinkImagePath")

            if (!snackPath.isNullOrBlank()) {
                snackImageUrl = storage.reference.child(snackPath).downloadUrl.await().toString()
            }

            if (!drinkPath.isNullOrBlank()) {
                drinkImageUrl = storage.reference.child(drinkPath).downloadUrl.await().toString()
            }

            if (snackImageUrl == null && drinkImageUrl == null) {
                errorMessage = "No stock images configured for this machine."
            }
        } catch (e: Exception) {
            Log.e("VendingStockScreen", "Failed to load vending images", e)
            errorMessage = e.message ?: "Failed to load images."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        currentImageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "$selectedStock vending stock",
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
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = machineName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedStock == "snacks") {
                        Button(
                            onClick = { selectedStock = "snacks" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Snacks")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { selectedStock = "snacks" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Snacks")
                        }
                    }

                    if (selectedStock == "drinks") {
                        Button(
                            onClick = { selectedStock = "drinks" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Drinks")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { selectedStock = "drinks" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Drinks")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading images...")
                    }

                    currentImageUrl != null -> {
                        Text(
                            text = "Showing current ${selectedStock} stock.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }

                    else -> {
                        Text(
                            text = if (selectedStock == "drinks") {
                                "No drink stock image available."
                            } else {
                                errorMessage ?: "No snack stock image available."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }
    }
}