package com.example.phinui.data.schedule

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class CourseCatalogRepository {

    private val gson = Gson()
    private val storage = FirebaseStorage.getInstance()

    suspend fun fetchCourseCatalog(context: Context): List<CourseCatalogItem> = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("courses", ".json", context.cacheDir)

        storage.reference
            .child("catalog/courses.json")
            .getFile(tempFile)
            .await()

        val json = tempFile.readText()
        tempFile.delete()

        val type = object : TypeToken<List<CourseCatalogItem>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }
}