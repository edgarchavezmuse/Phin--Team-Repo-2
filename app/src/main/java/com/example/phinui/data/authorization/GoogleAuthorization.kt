package com.example.phinui.data.authorization

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope

// Handles Google "authorization" (permission) for accessing Google user data.
object GoogleAuthManager {

    private const val TAG = "GoogleAuthManager"
    private const val CALENDAR_EVENTS = "https://www.googleapis.com/auth/calendar.events"
    private const val EMAIL = "email"
    private const val PROFILE = "profile"
    private fun authorizationClient(activity: Activity): AuthorizationClient =
        Identity.getAuthorizationClient(activity)
    private fun calendarAuthorizationRequest(): AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(
                    Scope(CALENDAR_EVENTS),
                    Scope(EMAIL),
                    Scope(PROFILE)
                )
            )
            .build()

    /*
     * Starts the authorization for Google Calendar Events scope
     *
     * If user consent is needed, launches the Google consent UI through launcher
     * If already authorized, returns the access token through onAccessToken
     */
    fun startAuthorization(
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onAccessToken: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val client = authorizationClient(activity)

        client.authorize(calendarAuthorizationRequest())
            .addOnSuccessListener { result ->
                val token = result.accessToken
                // User already approved
                if (!token.isNullOrBlank()) {
                    onAccessToken(token)
                    return@addOnSuccessListener
                }

                // No tokens and no resolution (resolution - user can solve problem)
                if (!result.hasResolution()) {
                    onError(IllegalStateException("Not authorized and no resolution available."))
                    return@addOnSuccessListener
                }

                // Google sent user approval (waiting on user action to continue authorization)
                val pendingIntent = result.pendingIntent
                    ?: run {
                        onError(IllegalStateException("Authorization resolution missing PendingIntent"))
                        return@addOnSuccessListener
                    }

                launcher.launch(
                    IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                )
            }
            .addOnFailureListener(onError)
    }

     // Returns access token if successful authorization; otherwise null.
    fun handleAuthorizationResult(
        activity: Activity,
        activityResult: ActivityResult
    ): String? {

        // Did the user click “Submit”? (RESULT_OK)
        if (activityResult.resultCode != Activity.RESULT_OK) return null

        // Did the server actually send back a confirmation page? (data != null)
        val data = activityResult.data
        if (data == null) return null

         // Attempt to get access token from intent; return null if authorization fails
        return try {
            val result = authorizationClient(activity).getAuthorizationResultFromIntent(data)
            result.accessToken
        } catch (e: Exception) {
            Log.e(TAG, "Failed authorization", e)
            null
        }
    }
}