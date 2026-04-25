package com.example.phinui.data.calendar

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class FirebaseCalendarRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("No authenticated Firebase user.")
    }

    private fun eventsCollection() =
        firestore.collection("users")
            .document(requireUserId())
            .collection("events")

    suspend fun saveEvent(event: CalendarEvent) {
        val docRef = eventsCollection().document()
        val safeEvent = event.copy(id = docRef.id)

        docRef.set(
            mapOf(
                "id" to safeEvent.id,
                "title" to safeEvent.title,
                "start" to safeEvent.start,
                "end" to safeEvent.end,
                "location" to safeEvent.location,
                "reminderMinutes" to safeEvent.reminderMinutes,
                "description" to safeEvent.description,
                "isAllDay" to safeEvent.isAllDay,
                "colorHex" to safeEvent.colorHex,
                "source" to safeEvent.source.name
            )
        ).await()
    }

    suspend fun deleteEvent(eventId: String) {
        eventsCollection()
            .document(eventId)
            .delete()
            .await()
    }

    suspend fun loadAllEvents(): List<CalendarEvent> {
        val snapshot = eventsCollection()
            .orderBy("start", Query.Direction.ASCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                CalendarEvent(
                    id = doc.getString("id") ?: doc.id,
                    title = doc.getString("title") ?: return@mapNotNull null,
                    start = doc.getString("start") ?: return@mapNotNull null,
                    end = doc.getString("end") ?: return@mapNotNull null,
                    location = doc.getString("location"),
                    reminderMinutes = (doc.get("reminderMinutes") as? List<*>)?.mapNotNull {
                        when (it) {
                            is Long -> it.toInt()
                            is Int -> it
                            else -> null
                        }
                    } ?: emptyList(),
                    source = doc.getString("source")
                        ?.let {
                            runCatching { CalendarSource.valueOf(it) }.getOrNull()
                        } ?: CalendarSource.LOCAL,
                    description = doc.getString("description"),
                    isAllDay = doc.getBoolean("isAllDay") ?: false,
                    colorHex = doc.getString("colorHex") ?: "#FF1F1F"
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun loadWeekEvents(weekStart: LocalDate): List<CalendarEvent> {
        val weekEnd = weekStart.plusDays(6)
        return loadAllEvents().filter { event ->
            val date = extractEventDate(event.start) ?: return@filter false
            !date.isBefore(weekStart) && !date.isAfter(weekEnd)
        }
    }

    private fun extractEventDate(start: String): LocalDate? {
        return try {
            if (!start.contains("T")) {
                LocalDate.parse(start)
            } else {
                LocalDateTime.parse(start, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        } catch (_: Exception) {
            try {
                LocalDate.parse(start.substring(0, 10))
            } catch (_: Exception) {
                null
            }
        }
    }
}