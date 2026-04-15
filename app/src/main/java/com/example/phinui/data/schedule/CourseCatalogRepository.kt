package com.example.phinui.data.schedule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder

class CourseCatalogRepository {

    private val client = OkHttpClient()

    suspend fun fetchCourseCatalog(): List<CourseCatalogItem> = withContext(Dispatchers.IO) {
        val allCourses = mutableListOf<CourseCatalogItem>()

        for (page in 1..50) {
            val pageCourses = fetchCoursesFromPage(page)

            if (pageCourses.isEmpty()) {
                break
            }

            allCourses.addAll(pageCourses)
        }

        allCourses
            .distinctBy { "${it.code}|${it.name}" }
            .sortedBy { it.code }
    }

    private fun fetchCoursesFromPage(page: Int): List<CourseCatalogItem> {
        val url = buildCatalogUrl(page)

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to load catalog page $page: HTTP ${response.code}")
            }

            val html = response.body?.string().orEmpty()
            val document = Jsoup.parse(html)

            val regex = Regex("""^([A-Z]{2,5}\s*\d+[A-Z]?)\s*-\s*(.+)$""")
            val courses = mutableListOf<CourseCatalogItem>()

            document.select("a").forEach { element ->
                val text = element.text().trim()
                val match = regex.find(text)

                if (match != null) {
                    courses.add(
                        CourseCatalogItem(
                            code = match.groupValues[1].trim(),
                            name = match.groupValues[2].trim()
                        )
                    )
                }
            }

            return courses
                .distinctBy { "${it.code}|${it.name}" }
        }
    }

    private fun buildCatalogUrl(page: Int): String {
        return "https://catalog.csuci.edu/content.php?" +
                "filter%5B27%5D=-1&" +
                "filter%5B29%5D=&" +
                "filter%5Bcourse_type%5D=-1&" +
                "filter%5Bkeyword%5D=&" +
                "filter%5B32%5D=1&" +
                "filter%5Bcpage%5D=$page&" +
                "cur_cat_oid=64&" +
                "expand=&" +
                "navoid=8135&" +
                "search_database=Filter"
    }
}