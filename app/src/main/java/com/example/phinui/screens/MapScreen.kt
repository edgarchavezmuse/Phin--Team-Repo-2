@file:Suppress("MissingPermission")
package com.example.phinui.ui.screens
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await
import com.example.phinui.BuildConfig
import androidx.compose.foundation.clickable
import com.google.android.libraries.places.api.model.RectangularBounds
import com.example.phinui.data.fetchCampusLocations
import com.example.phinui.data.filterCampusLocations
import com.example.phinui.data.CampusLocation
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import com.google.maps.android.compose.rememberMarkerState
import androidx.compose.runtime.key
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Composable
fun MapScreen() {
    val context = LocalContext.current

    if (!Places.isInitialized()) {
        Places.initializeWithNewPlacesApiEnabled(
            context.applicationContext,
            BuildConfig.MAPS_API_KEY
        )
    }

    var campusLocations by remember { mutableStateOf<List<CampusLocation>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

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
    val campusRestriction = remember {
        RectangularBounds.newInstance(campusBounds)
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

    val filteredCampusLocations = filterCampusLocations(
        locations = campusLocations,
        selectedCategory = selectedCategory
    )

    val autocompleteLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                PlaceAutocompleteActivity.RESULT_OK -> {
                    val data = result.data ?: return@rememberLauncherForActivityResult
                    val prediction = PlaceAutocomplete.getPredictionFromIntent(data)
                    val placeId = prediction?.placeId ?: return@rememberLauncherForActivityResult

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
                    val data = result.data
                    if (data != null) {
                        val status = PlaceAutocomplete.getResultStatusFromIntent(data)
                        android.util.Log.e(
                            "PlacesAutocomplete",
                            "Error code: ${status?.statusCode}, message: ${status?.statusMessage}"
                        )
                    } else {
                        android.util.Log.e("PlacesAutocomplete", "No intent data returned")
                    }
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
    LaunchedEffect(campusLocations) {
        android.util.Log.d("FIREBASE_DEBUG", "campusLocations size = ${campusLocations.size}")
        campusLocations.forEach { location ->
            android.util.Log.d(
                "FIREBASE_DEBUG",
                "Loaded: name=${location.name}, category=${location.category}, lat=${location.latitude}, lng=${location.longitude}, active=${location.isActive}"
            )
        }
    }

    LaunchedEffect(filteredCampusLocations) {
        android.util.Log.d("FIREBASE_DEBUG", "filteredCampusLocations size = ${filteredCampusLocations.size}")
    }

    LaunchedEffect(Unit) {
        campusLocations = fetchCampusLocations()
    }

    LaunchedEffect(campusLocations) {
        android.util.Log.d("MAP_DEBUG", "Loaded campus locations: ${campusLocations.size}")
    }

    LaunchedEffect(filteredCampusLocations) {
        android.util.Log.d("MAP_DEBUG", "Filtered campus locations: ${filteredCampusLocations.size}")
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
            filteredCampusLocations.forEach { location ->
                key(location.id) {
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(location.latitude, location.longitude)
                        ),
                        title = location.name,
                        snippet = location.description,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            markerHueForCategory(location.category)
                        )
                    )
                }
            }

            selectedPlaceLatLng?.let { latLng ->
                key(latLng.latitude, latLng.longitude, selectedPlaceName) {
                    Marker(
                        state = rememberMarkerState(position = latLng),
                        title = selectedPlaceName ?: "Selected place",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_BLUE
                        )
                    )
                }
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
                        .setLocationRestriction(campusRestriction)
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
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            CategoryFilterBar(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category


                    selectedPlaceLatLng = null
                    selectedPlaceName = null
                    searchText = ""
                }
            )
        }

    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)

                if (campusBounds.contains(userLatLng)) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(userLatLng, 16f)
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

@Composable
fun CategoryFilterBar(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categories = listOf(
        null to "None",
        "all" to "All",
        "restroom" to "Restrooms",
        "microwave" to "Microwaves",
        "vending" to "Vending",
        "printer" to "Printers"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (value, label) ->
            FilterChip(
                selected = selectedCategory == value,
                onClick = { onCategorySelected(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}
private fun markerHueForCategory(category: String?): Float {
    return when (category?.lowercase()) {
        "restroom" -> BitmapDescriptorFactory.HUE_AZURE
        "microwave" -> BitmapDescriptorFactory.HUE_ORANGE
        "vending" -> BitmapDescriptorFactory.HUE_GREEN
        "printer" -> BitmapDescriptorFactory.HUE_VIOLET
        else -> BitmapDescriptorFactory.HUE_RED
    }
}