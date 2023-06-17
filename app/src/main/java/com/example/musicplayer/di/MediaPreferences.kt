package com.example.musicplayer.di

import android.content.Context
import com.example.musicplayer.R
import com.example.musicplayer.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPreferences @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(
        "${context.getString(R.string.app_name)}-Preferences", Context.MODE_PRIVATE
    )

    fun save(key: String, value: Any) {
        prefs.edit().apply {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is Boolean -> putBoolean(key, value)
            }
        }.apply()
    }

    fun getString(key: String): String = prefs.getString(key, Constants.EMPTY).toString()

    fun getBoolean(key: String): Boolean = prefs.getBoolean(key, false)

    fun getInt(key: String): Int = prefs.getInt(key, 0)

    fun getLong(key: String) = prefs.getLong(key, 0L)

    fun getFloat(key: String) = prefs.getFloat(key, 0f)
}