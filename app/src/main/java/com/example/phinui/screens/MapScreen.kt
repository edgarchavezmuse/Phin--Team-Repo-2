package com.example.phinui.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLngBounds

import com.google.maps.android.compose.MapProperties
@Composable
fun MapScreen() {
    val campusCenter = LatLng(34.16205, -119.043572)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(campusCenter, 16f)
    }
    val campusBounds = LatLngBounds(
        LatLng(

            34.15817, -119.04855
        ), // southwest
        LatLng(34.17528, -119.03274)  // northeast


    )


    val markerState = remember {
        MarkerState(position = campusCenter)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            latLngBoundsForCameraTarget = campusBounds,
            minZoomPreference = 15f,
            maxZoomPreference = 20f
        )
    ){ Marker(
            state = markerState,
            title = "Singapore"
        )
    }
}














