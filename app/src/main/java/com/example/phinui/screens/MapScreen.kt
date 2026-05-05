@file:Suppress("MissingPermission")

package com.example.phinui.ui.screens

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.phinui.BuildConfig
import com.example.phinui.data.CampusLocation
import com.example.phinui.data.fetchCampusLocations
import com.example.phinui.data.filterCampusLocations
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.widget.PlaceAutocomplete
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.maps.model.MapStyleOptions


import com.example.phinui.ui.navigation.Routes
import androidx.navigation.NavHostController

@Composable
fun MapScreen(sharedPin: CampusLocation? = null,navController: NavHostController,darkMode: Boolean) {
    val context = LocalContext.current

    if (!Places.isInitialized()) {
        Places.initializeWithNewPlacesApiEnabled(
            context.applicationContext,
            BuildConfig.MAPS_API_KEY
        )
    }

    var campusLocations by remember { mutableStateOf<List<CampusLocation>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }
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

    val darkMapStyle = """
        [
          { "elementType": "geometry", "stylers": [{ "color": "#121212" }] },
          { "elementType": "labels.text.fill", "stylers": [{ "color": "#B0B0B0" }] },
          { "elementType": "labels.text.stroke", "stylers": [{ "color": "#121212" }] },
          { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#2C2C2C" }] },
          { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#000000" }] },
          { "featureType": "poi", "elementType": "geometry", "stylers": [{ "color": "#1E1E1E" }] }
        ]
    """.trimIndent()

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
                            selectedLocation = null
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

    LaunchedEffect(Unit) {
        campusLocations = fetchCampusLocations()
    }

    LaunchedEffect(sharedPin, campusLocations) {
        sharedPin?.let { incomingPin ->
            val matchingPin = campusLocations.firstOrNull { it.id == incomingPin.id } ?: incomingPin

            selectedCategory = "all"
            selectedLocation = matchingPin
            selectedPlaceLatLng = null
            selectedPlaceName = null
            searchText = matchingPin.name

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(matchingPin.latitude, matchingPin.longitude),
                    17f
                )
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

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                latLngBoundsForCameraTarget = campusBounds,
                minZoomPreference = 15f,
                maxZoomPreference = 20f,
                mapStyleOptions =  if (darkMode) {
                    MapStyleOptions(darkMapStyle)
                } else {
                    null
                }
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true
            ),
            onMapClick = {
                selectedLocation = null
            }
        ) {
            filteredCampusLocations.forEach { location ->
                key(location.id) {
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(location.latitude, location.longitude)
                        ),
                        icon = BitmapDescriptorFactory.defaultMarker(
                            markerHueForCategory(location.category)
                        ),
                        onClick = {
                            selectedLocation = location
                            selectedPlaceLatLng = null
                            selectedPlaceName = null
                            true
                        }
                    )
                }
            }

            selectedPlaceLatLng?.let { latLng ->
                key(latLng.latitude, latLng.longitude, selectedPlaceName) {
                    Marker(
                        state = rememberMarkerState(position = latLng),
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
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledBorderColor = Color.Transparent,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            CategoryFilterBar(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                    selectedLocation = null
                    selectedPlaceLatLng = null
                    selectedPlaceName = null
                    searchText = ""
                }
            )
        }

        selectedLocation?.let { location ->
            PinInfoCard(
                location = location,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                onDismiss = {
                    selectedLocation = null
                },
                onShare = { sharedLocation ->
                    // Next phase: open friend/chat picker
                },
                onVendingStockClick = { vendingLocation ->
                    navController.navigate("${Routes.VENDING_STOCK}/${vendingLocation.id}")
                }
            )
        }
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
                label = {
                    Text(
                        label,
                        color = if (selectedCategory == value)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun PinInfoCard(
    location: CampusLocation,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onShare: (CampusLocation) -> Unit,
    onVendingStockClick: (CampusLocation) -> Unit
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onDismiss() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Close",
                            color = MaterialTheme.colorScheme.surface,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (location.category.isNotBlank()) {
                    Text(
                        text = location.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (location.description.isNotBlank()) {
                    Text(
                        text = location.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Text(
                    text = "Lat: ${location.latitude}, Lng: ${location.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    if (location.category.equals("vending", ignoreCase = true)) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { onVendingStockClick(location) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Stock",
                                color = MaterialTheme.colorScheme.surface,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable {
                                openGoogleMapsDirections(
                                    location.latitude,
                                    location.longitude,
                                    location.name,
                                    context
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Directions",
                            color = MaterialTheme.colorScheme.surface,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
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

fun openGoogleMapsDirections(
    lat: Double,
    lng: Double,
    label: String,
    context: Context
) {
    val uri = Uri.parse("google.navigation:q=$lat,$lng&mode=w")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val fallbackUri =
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
    }
}