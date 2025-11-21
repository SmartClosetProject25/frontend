package com.example.smartcloset_frontend.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_EMAIL = "saved_email"
        private const val KEY_PASSWORD = "saved_password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"
        private const val EXPIRY_MONTHS = 2L  // 2か月
    }

    fun saveLoginInfo(email: String, password: String) {
        val currentTime = System.currentTimeMillis()
        prefs.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LAST_LOGIN_TIME, currentTime)
            apply()
        }
    }

    fun getSavedEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getSavedPassword(): String? {
        return prefs.getString(KEY_PASSWORD, null)
    }

    fun getLastLoginTime(): Long {
        return prefs.getLong(KEY_LAST_LOGIN_TIME, 0)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun isLoginExpired(): Boolean {
        val lastLoginTime = getLastLoginTime()
        if (lastLoginTime == 0L) return true
        
        val currentTime = System.currentTimeMillis()
        val expiryTime = lastLoginTime + (EXPIRY_MONTHS * 30L * 24L * 60L * 60L * 1000L)  // 2か月をミリ秒に変換
        
        return currentTime > expiryTime
    }

    fun shouldAutoLogin(): Boolean {
        return isLoggedIn() && !isLoginExpired()
    }

    fun clearLoginInfo() {
        prefs.edit().apply {
            remove(KEY_EMAIL)
            remove(KEY_PASSWORD)
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_LAST_LOGIN_TIME)
            apply()
        }
    }
}

