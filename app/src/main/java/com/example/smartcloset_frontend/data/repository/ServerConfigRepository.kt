package com.example.smartcloset_frontend.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.serverConfigDataStore by preferencesDataStore(name = "server_config")

class ServerConfigRepository(private val context: Context) {
    private val KEY_OVERRIDE_URL = stringPreferencesKey("override_base_url")

    val overrideUrlFlow: Flow<String?> = context.serverConfigDataStore.data
        .map { prefs -> prefs[KEY_OVERRIDE_URL] }

    suspend fun setOverrideUrl(url: String?) {
        context.serverConfigDataStore.edit { prefs ->
            if (url.isNullOrBlank()) prefs.remove(KEY_OVERRIDE_URL)
            else prefs[KEY_OVERRIDE_URL] = url.trim()
        }
    }
}
