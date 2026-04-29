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

class SettingsViewModel(context: Context) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val dataStore = context.dataStore

    private val _bottomBarType = MutableStateFlow<BottomBarType?>(null)
    val bottomBarType: StateFlow<BottomBarType?> = _bottomBarType

    init {
        observeUserChanges()
    }

    private fun key(uid: String) = stringPreferencesKey("bottom_bar_$uid")

    // main observer
    private fun observeUserChanges() {
        auth.addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid

            if (uid == null) {
                _bottomBarType.value = null
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
}