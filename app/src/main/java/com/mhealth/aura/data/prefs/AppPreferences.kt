package com.mhealth.aura.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_prefs")

class AppPreferences(private val context: Context) {
    companion object {
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_AUTHENTICATED_EMAIL = stringPreferencesKey("authenticated_email")
    }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_ONBOARDING_DONE] ?: false }

    val selectedLanguage: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_LANGUAGE] ?: "kn" }

    val authenticatedEmail: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_AUTHENTICATED_EMAIL] ?: "" }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs -> prefs[KEY_LANGUAGE] = lang }
    }

    suspend fun setAuthenticatedEmail(email: String) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTHENTICATED_EMAIL] = email }
    }

    suspend fun clearAuthenticatedEmail() {
        context.dataStore.edit { prefs -> prefs.remove(KEY_AUTHENTICATED_EMAIL) }
    }
}
