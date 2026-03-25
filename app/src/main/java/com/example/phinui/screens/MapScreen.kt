@file:Suppress("MissingPermission")
package com.example.phinui.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await











@Composable
fun MapScreen() {
 val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val campusCenter = LatLng(34.16205, -119.043572)
    val southWestLat = 34.15817
    val southWestLng = -119.04855
    val northEastLat = 34.17528
    val northEastLng = -119.03274

    val campusBounds = remember {
        LatLngBounds(
            LatLng(southWestLat, southWestLng),   // southwest
            LatLng(northEastLat, northEastLng)    // northeast
              
              

        )
    }

    var hasLocationPermission by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(campusCenter, 16f)
    }

    val markerState = remember {
        MarkerState(position = campusCenter)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

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
    )
        /*
        Marker(
            state = markerState,
            title = "Campus Center"
        )
        
         */


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
}




































