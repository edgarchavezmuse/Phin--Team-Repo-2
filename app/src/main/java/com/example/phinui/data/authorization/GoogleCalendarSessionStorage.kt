package com.example.phinui.data.authorization

import android.content.Context

class GoogleCalendarSessionStorage(context: Context) {

    private val sessionPreference = context.getSharedPreferences(
        "google_calendar_session",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_CONNECTED = "connected"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveSession(userEmail: String?) {
        sessionPreference.edit()
            .putBoolean(KEY_CONNECTED, true)
            .putString(KEY_USER_EMAIL, userEmail)
            .apply()
    }

    fun isConnected(): Boolean {
        return sessionPreference.getBoolean(KEY_CONNECTED, false)
    }

    fun getUserEmail(): String? {
        return sessionPreference.getString(KEY_USER_EMAIL, null)
    }

    fun clearSession() {
        sessionPreference.edit()
            .remove(KEY_CONNECTED)
            .remove(KEY_USER_EMAIL)
            .apply()
    }
}