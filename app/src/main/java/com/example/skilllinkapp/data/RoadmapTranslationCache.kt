
package com.example.skilllinkapp.data

import android.content.Context
import org.json.JSONObject

object RoadmapTranslationCache {

    private const val PREFS_NAME = "skilllink_roadmap_translations"

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private fun normalizeLanguage(language: String): String {
        return when (language.trim().lowercase()) {
            "isizulu", "zulu", "zu" -> "zu"
            "afrikaans", "af" -> "af"
            "english", "en" -> "en"
            else -> language.trim().lowercase()
        }
    }

    private fun cacheKey(roadmapId: String, language: String): String {
        return "v3_${roadmapId}_${normalizeLanguage(language)}"
    }

    private fun oldCacheKey(roadmapId: String, language: String): String {
        return "v2_${roadmapId}_${language}"
    }

    fun get(
        context: Context,
        roadmapId: String,
        language: String
    ): String? {
        if (roadmapId.isBlank()) return null

        val prefs = preferences(context)

        // Check the new cache format first.
        val newKey = cacheKey(roadmapId, language)
        val newValue = prefs.getString(newKey, null)

        if (newValue != null) return newValue

        // Fall back to the previous cache format.
        val oldValue = prefs.getString(
            oldCacheKey(roadmapId, language),
            null
        ) ?: return null

        // Migrate the old entry to the new format.
        prefs.edit()
            .putString(newKey, oldValue)
            .commit()

        return oldValue
    }

    fun save(
        context: Context,
        roadmapId: String,
        language: String,
        translatedJson: String
    ): Boolean {
        if (roadmapId.isBlank() || translatedJson.isBlank()) {
            return false
        }

        return try {
            JSONObject(translatedJson)

            preferences(context).edit()
                .putString(
                    cacheKey(roadmapId, language),
                    translatedJson
                )
                .commit()

        } catch (e: Exception) {
            false
        }
    }

    fun delete(
        context: Context,
        roadmapId: String,
        language: String
    ) {
        preferences(context).edit()
            .remove(cacheKey(roadmapId, language))
            .remove(oldCacheKey(roadmapId, language))
            .apply()
    }
}
