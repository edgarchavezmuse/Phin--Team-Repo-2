package com.example.phinui.data.schedule

data class CourseCatalogItem(
    val code: String,
    val name: String
) {
    override fun toString(): String = "$code - $name"
}