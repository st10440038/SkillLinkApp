package com.example.skilllinkapp.data

import android.content.Context
import org.json.JSONObject

object NotificationTranslationCache {

    private const val PREFS_NAME = "skilllink_notification_translations"

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private fun cacheKey(notificationId: String, language: String) =
        "v2_${notificationId}_${language}"

    fun get(
        context: Context,
        notificationId: String,
        language: String
    ): Pair<String, String>? {

        if (notificationId.isBlank()) return null

        return try {
            val cached = preferences(context).getString(
                cacheKey(notificationId, language),
                null
            ) ?: return null

            val json = JSONObject(cached)

            val title = json.getString("title")
            val message = json.getString("message")

            if (title.isBlank() || message.isBlank()) {
                null
            } else {
                title to message
            }
        } catch (_: Exception) {
            null
        }
    }

    fun save(
        context: Context,
        notificationId: String,
        language: String,
        title: String,
        message: String
    ): Boolean {

        if (
            notificationId.isBlank() ||
            title.isBlank() ||
            message.isBlank()
        ) {
            return false
        }

        return try {
            val json = JSONObject().apply {
                put("title", title)
                put("message", message)
            }

            preferences(context).edit()
                .putString(
                    cacheKey(notificationId, language),
                    json.toString()
                )
                .commit()
        } catch (_: Exception) {
            false
        }
    }
}