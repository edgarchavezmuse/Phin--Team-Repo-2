@file:Suppress("MissingPermission")
package com.example.phinui.ui.screens

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.widget.PlaceAutocomplete
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await
import com.example.phinui.BuildConfig
import androidx.compose.foundation.clickable

@Composable
fun MapScreen() {
    val context = LocalContext.current

    if (!Places.isInitialized()) {
        Places.initialize(context.applicationContext, BuildConfig.MAPS_API_KEY)
    }

    val placesClient = remember { Places.createClient(context) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val campusCenter = LatLng(34.16205, -119.043572)

    val campusBounds = remember {
        LatLngBounds(
            LatLng(34.15817, -119.04855),
            LatLng(34.17528, -119.03274)
        )
    }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var selectedPlaceLatLng by remember { mutableStateOf<LatLng?>(null) }
    var selectedPlaceName by remember { mutableStateOf<String?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(campusCenter, 16f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    val autocompleteLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                PlaceAutocompleteActivity.RESULT_OK -> {
                    val data = result.data ?: return@rememberLauncherForActivityResult
                    val prediction = PlaceAutocomplete.getPredictionFromIntent(data)
                    val placeId = prediction?.placeId

                    val request = FetchPlaceRequest.builder(
                        placeId,
                        listOf(
                            Place.Field.DISPLAY_NAME,
                            Place.Field.LOCATION
                        )
                    ).build()

                    placesClient.fetchPlace(request)
                        .addOnSuccessListener { response ->
                            val place = response.place
                            val latLng = place.location ?: return@addOnSuccessListener

                            searchText = place.displayName ?: ""
                            selectedPlaceName = place.displayName
                            selectedPlaceLatLng = latLng
                        }
                }

                PlaceAutocompleteActivity.RESULT_ERROR -> {
                    // Optional: show a Snackbar or Toast
                }

                Activity.RESULT_CANCELED,
                PlaceAutocompleteActivity.RESULT_CANCELED -> {
                    // User closed search
                }
            }
        }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(selectedPlaceLatLng) {
        selectedPlaceLatLng?.let { latLng ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                latLngBoundsForCameraTarget = campusBounds,
                minZoomPreference = 15f,
                maxZoomPreference = 20f
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true
            )
        ) {
            selectedPlaceLatLng?.let { latLng ->
                Marker(
                    state = MarkerState(position = latLng),
                    title = selectedPlaceName ?: "Selected place"
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .clickable {
                    val intent = PlaceAutocomplete.IntentBuilder()
                        .setInitialQuery(searchText)
                        .build(context)

                    autocompleteLauncher.launch(intent)
                }
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for a place") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = Color.White,
                    disabledBorderColor = Color.Transparent,
                    disabledTextColor = Color.Black,
                    disabledPlaceholderColor = Color.Gray
                )
            )
        }

        // Make the whole text field tappable by wrapping or replacing with clickable container if needed.

    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)

                if (campusBounds.contains(userLatLng)) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(userLatLng, 17f)
                    )
                } else {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(campusCenter, 16f)
                    )
                }
            }
        }
    }

    LaunchedEffect(searchText) {
        // no-op, just here to keep Compose state visible while you build
    }
}