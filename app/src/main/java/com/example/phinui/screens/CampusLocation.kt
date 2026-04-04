package com.example.phinui.screens

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