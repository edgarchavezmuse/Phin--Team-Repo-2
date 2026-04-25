package com.example.phinui.data.authorization

import android.content.Context

class GoogleCalendarSessionStorage(context: Context) {

    private val sessionPreference = context.getSharedPreferences(
        "google_calendar_session",
        Context.MODE_PRIVATE
    )

    private fun connectedKey(firebaseUid: String): String = "connected_$firebaseUid"
    private fun emailKey(firebaseUid: String): String = "user_email_$firebaseUid"

    fun saveSession(firebaseUid: String, userEmail: String?) {
        sessionPreference.edit()
            .putBoolean(connectedKey(firebaseUid), true)
            .putString(emailKey(firebaseUid), userEmail)
            .apply()
    }

    fun isConnected(firebaseUid: String): Boolean {
        return sessionPreference.getBoolean(connectedKey(firebaseUid), false)
    }

    fun getUserEmail(firebaseUid: String): String? {
        return sessionPreference.getString(emailKey(firebaseUid), null)
    }

    fun clearSession(firebaseUid: String) {
        sessionPreference.edit()
            .remove(connectedKey(firebaseUid))
            .remove(emailKey(firebaseUid))
            .apply()
    }
}