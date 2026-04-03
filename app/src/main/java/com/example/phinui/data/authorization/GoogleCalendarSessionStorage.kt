package com.example.phinui.data.authorization

import android.content.Context

class GoogleCalendarSessionStorage(context: Context) {

    private val sessionPreference = context.getSharedPreferences(
        "google_calendar_session",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveSession(
        accessToken: String,
        userEmail: String?
    ) {
        sessionPreference.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_EMAIL, userEmail)
            .apply()
    }

    fun getAccessToken(): String? {
        return sessionPreference.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getUserEmail(): String? {
        return sessionPreference.getString(KEY_USER_EMAIL, null)
    }

    fun clearSession() {
        sessionPreference.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_EMAIL)
            .apply()
    }
}