package com.example.smartcloset_frontend.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Context 拡張プロパティ（どこか1ファイルだけに書く）
private val Context.userPrefsDataStore by preferencesDataStore(name = "user_prefs")

object UserPrefsKeys {
    val USER_ID = intPreferencesKey("user_id")
}

class UserSessionRepository(private val context: Context) {

    // DataStore から userId を Flow で取得
    val userIdFlow: Flow<Int?> = context.userPrefsDataStore.data
        .map { prefs ->
            prefs[UserPrefsKeys.USER_ID]   // なければ null
        }

    // userId を保存（null のときは削除）
    suspend fun setUserId(id: Int?) {
        context.userPrefsDataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(UserPrefsKeys.USER_ID)
            } else {
                prefs[UserPrefsKeys.USER_ID] = id
            }
        }
    }

    suspend fun clear() {
        setUserId(null)
    }
}
