
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

    private fun cacheKey(roadmapId: String, language: String): String {
        return "${roadmapId}_${language}"
    }

    fun get(
        context: Context,
        roadmapId: String,
        language: String
    ): String? {
        if (roadmapId.isBlank()) return null

        return preferences(context).getString(
            cacheKey(roadmapId, language),
            null
        )
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

        // Validate before caching.
        return try {
            JSONObject(translatedJson)

            preferences(context).edit()
                .putString(
                    cacheKey(roadmapId, language),
                    translatedJson
                )
                .commit()
        } catch (_: Exception) {
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
            .apply()
    }
}
