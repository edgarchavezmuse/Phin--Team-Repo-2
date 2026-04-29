package com.example.phinui.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phinui.data.dataStore
import com.example.phinui.screens.BottomBarType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.compose.runtime.collectAsState

class SettingsViewModel(context: Context) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val dataStore = context.dataStore

    private val _bottomBarType = MutableStateFlow<BottomBarType?>(null)
    val bottomBarType: StateFlow<BottomBarType?> = _bottomBarType

    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled

    init {
        observeUserChanges()
    }

    private fun key(uid: String) = stringPreferencesKey("bottom_bar_$uid")

    // dark mode key
    private fun darkModeKey(uid: String) =
        booleanPreferencesKey("dark_mode_$uid")

    // main observer
    private fun observeUserChanges() {
        auth.addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid

            if (uid == null) {
                _bottomBarType.value = null
                _darkModeEnabled.value = false
                return@addAuthStateListener
            }

            observeDataStore(uid)
            observeFirestore(uid)
        }
    }

    // Local (DataStore)
    private fun observeDataStore(uid: String) {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val value = prefs[key(uid)]
                    ?.let { BottomBarType.valueOf(it) }

                if (value != null) {
                    _bottomBarType.value = value
                }

                // dark mode local
                _darkModeEnabled.value = prefs[darkModeKey(uid)] ?: false
            }
        }
    }

    // Remote (Firestore)
    private fun observeFirestore(uid: String) {

        firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, _ ->
                val remote = snapshot?.getString("bottomBarType")
                    ?.let { BottomBarType.valueOf(it) }

                if (remote != null && remote != _bottomBarType.value) {
                    _bottomBarType.value = remote

                    viewModelScope.launch {
                        dataStore.edit { prefs ->
                            prefs[key(uid)] = remote.name
                        }
                    }
                }

                // dark mode remote
                val remoteDark = snapshot?.getBoolean("darkModeEnabled")

                if (remoteDark != null && remoteDark != _darkModeEnabled.value) {
                    _darkModeEnabled.value = remoteDark

                    viewModelScope.launch {
                        dataStore.edit { prefs ->
                            prefs[darkModeKey(uid)] = remoteDark
                        }
                    }
                }
            }
    }

    fun setBottomBar(type: BottomBarType) {
        val uid = auth.currentUser?.uid ?: return

        _bottomBarType.value = type

        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[key(uid)] = type.name
            }
        }

        firestore.collection("users")
            .document(uid)
            .set(
                mapOf("bottomBarType" to type.name),
                SetOptions.merge()
            )
    }

    // set dark mode
    fun setDarkMode(enabled: Boolean) {
        val uid = auth.currentUser?.uid ?: return

        _darkModeEnabled.value = enabled

        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[darkModeKey(uid)] = enabled
            }
        }

        firestore.collection("users")
            .document(uid)
            .set(
                mapOf("darkModeEnabled" to enabled),
                SetOptions.merge()
            )
    }
}