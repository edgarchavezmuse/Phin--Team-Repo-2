
package com.example.phinui.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class CampusLocation(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val building: String = "",
    val description: String = "",
    val isActive: Boolean = true
)


suspend fun fetchCampusLocations(): List<CampusLocation> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val snapshot = db.collection("campus.locations")
            .whereEqualTo("isActive", true)
            .get()
            .await()

        Log.d("FIREBASE_DEBUG", "docs fetched = ${snapshot.size()}")

        snapshot.documents.forEach { doc ->
            Log.d("FIREBASE_DEBUG", "doc id = ${doc.id}, data = ${doc.data}")
        }

        snapshot.documents.mapNotNull { doc ->
            val location = doc.toObject(CampusLocation::class.java)?.copy(id = doc.id)
            Log.d("FIREBASE_DEBUG", "mapped location = $location")
            location
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Failed to fetch campus locations", e)
        emptyList()
    }
}
fun filterCampusLocations(
    locations: List<CampusLocation>,
    selectedCategory: String?
): List<CampusLocation> {


    if (selectedCategory == null) {
        return emptyList()
    }

    // show all
    if (selectedCategory == "all") {
        return locations
    }

    // filter specific category
    return locations.filter {
        it.category.equals(selectedCategory, ignoreCase = true)
    }
}
