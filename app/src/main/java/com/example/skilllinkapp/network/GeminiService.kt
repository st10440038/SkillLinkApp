
package com.example.skilllinkapp.network

import com.example.skilllinkapp.LanguageSupport
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object GeminiService {

    // TODO: Move the API key to a secure backend before release.
    // Replace this placeholder with a valid key for local testing.
    // Never commit a real API key to Git.
    private const val API_KEY = "AQ.Ab8RN6Ipmtj41k-f_Ie9qazUvvOZDHrg77HUJYKrb49ZV7Nc8Q"

    private const val MODEL = "gemini-3.5-flash-lite"

    private const val URL =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = HttpClient(Android)

    private val jsonParser = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Generates a new roadmap in the currently selected app language.
     */
    suspend fun generateRoadmap(
        targetSkill: String,
        experienceLevel: String,
        knownSkills: String,
        learningGoal: String,
        weeklyTime: String,
        learningPace: String
    ): String {

        val selectedLanguage = when (LanguageSupport.currentLanguage.value) {
            LanguageSupport.ZULU -> "isiZulu"
            LanguageSupport.AFRIKAANS -> "Afrikaans"
            else -> "English"
        }

        val prompt = """
            You are an expert learning designer.

            Create a practical, personalized learning roadmap.

            LANGUAGE REQUIREMENTS:
            Write ALL learner-facing text in $selectedLanguage.
            This includes the roadmap title, summary, week titles,
            week descriptions, and every lesson.
            Do not mix languages unless a technical term, programming
            keyword, product name, or proper noun should remain unchanged.
            Keep explanations understandable for the learner.

            Keep JSON property names in English exactly as specified below.

            Learner details:
            Target skill: $targetSkill
            Experience level: $experienceLevel
            Known skills: $knownSkills
            Learning goal: $learningGoal
            Weekly learning time: $weeklyTime
            Learning pace: $learningPace

            Determine an appropriate number of weeks based on the learner's
            experience, existing skills, goal, weekly learning time, and pace.

            Make the roadmap as short as reasonably possible while covering
            the skills needed to achieve the goal.

            Each week must contain:
            - A week title
            - A short description
            - Exactly 3 lessons

            Return ONLY valid JSON. Do not include Markdown fences or
            introductory text.

            Use this exact structure:
            {
              "title": "string",
              "summary": "string",
              "weeks": [
                {
                  "title": "string",
                  "description": "string",
                  "lessons": [
                    "string",
                    "string",
                    "string"
                  ]
                }
              ]
            }
        """.trimIndent()

        return requestGeneratedText(prompt).also { generatedText ->
            validateRoadmapJson(generatedText)
        }
    }

    /**
     * Translates an existing roadmap into English, isiZulu, or Afrikaans.
     * Keeps the JSON property names, week order, and lesson order unchanged.
     */
    suspend fun translateRoadmap(
        roadmapJson: String,
        targetLanguage: String
    ): String {

        require(
            targetLanguage in listOf("English", "isiZulu", "Afrikaans")
        ) {
            "Unsupported language: $targetLanguage"
        }

        // Reject invalid input before sending it to Gemini.
        validateRoadmapJson(roadmapJson)

        val prompt = """
            Translate the following learning roadmap into $targetLanguage.

            STRICT RULES:
            1. Translate the title and summary.
            2. Translate every week title and description.
            3. Translate every lesson.
            4. Keep JSON property names in English:
               title, summary, weeks, description, lessons.
            5. Preserve the exact number and order of weeks and lessons.
            6. Do not add, remove, summarize, or reorder content.
            7. Preserve the original meaning and level of detail.
            8. Keep technical terms, programming keywords, product names,
               and proper nouns unchanged when translating them would
               make the content inaccurate.
            9. Return ONLY valid JSON without Markdown fences or commentary.
            10. Make sure the result can be parsed as JSON.

            Roadmap JSON:
            $roadmapJson
        """.trimIndent()

        val translatedText = requestGeneratedText(prompt)

        // Never return or cache malformed or structurally changed JSON.
        validateRoadmapJson(translatedText)

        return translatedText
    }

    /**
     * Sends a prompt to Gemini and extracts the generated text.
     * Both roadmap generation and translation use this same request logic.
     */
    private suspend fun requestGeneratedText(prompt: String): String {

        check(API_KEY != "API_KEY_HERE" && API_KEY.isNotBlank()) {
            "Gemini API key is not configured. Set a valid API key for testing."
        }

        val requestBody = buildJsonObject {
            put(
                "contents",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put(
                                "parts",
                                buildJsonArray {
                                    add(
                                        buildJsonObject {
                                            put("text", prompt)
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }

        val response = client.post(URL) {
            header("x-goog-api-key", API_KEY)
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }

        val responseText = response.bodyAsText()

        if (response.status.value !in 200..299) {
            throw IllegalStateException(
                "Gemini request failed (${response.status.value}): $responseText"
            )
        }

        val responseJson = Json.parseToJsonElement(responseText)

        val generatedText = responseJson
            .jsonObject["candidates"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("content")
            ?.jsonObject
            ?.get("parts")
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("text")
            ?.jsonPrimitive
            ?.content

        return generatedText
            ?: throw IllegalStateException(
                "Gemini returned no generated text."
            )
    }

    /**
     * Checks that roadmap JSON has the fields required by SkillLink.
     * Also verifies that every week has exactly three lessons.
     */
    private fun validateRoadmapJson(roadmapJson: String) {

        val root = try {
            Json.parseToJsonElement(roadmapJson).jsonObject
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                "The roadmap response is not valid JSON.",
                exception
            )
        }

        requireNonBlankString(root, "title")
        requireNonBlankString(root, "summary")

        val weeks = root["weeks"] as? JsonArray
            ?: throw IllegalArgumentException(
                "The roadmap must contain a weeks array."
            )

        require(weeks.isNotEmpty()) {
            "The roadmap must contain at least one week."
        }

        weeks.forEachIndexed { index, weekElement ->
            val week: JsonObject = try {
                weekElement.jsonObject
            } catch (exception: Exception) {
                throw IllegalArgumentException(
                    "Week ${index + 1} is not a valid JSON object.",
                    exception
                )
            }

            requireNonBlankString(week, "title")
            requireNonBlankString(week, "description")

            val lessons = week["lessons"] as? JsonArray
                ?: throw IllegalArgumentException(
                    "Week ${index + 1} must contain a lessons array."
                )

            require(lessons.size == 3) {
                "Week ${index + 1} must contain exactly three lessons."
            }

            lessons.forEachIndexed { lessonIndex, lesson ->
                val text = try {
                    lesson.jsonPrimitive.content
                } catch (exception: Exception) {
                    throw IllegalArgumentException(
                        "Lesson ${lessonIndex + 1} in week ${index + 1} " +
                                "must be a string.",
                        exception
                    )
                }

                require(text.isNotBlank()) {
                    "Lesson ${lessonIndex + 1} in week ${index + 1} is empty."
                }
            }
        }
    }

    private fun requireNonBlankString(
        obj: JsonObject,
        key: String
    ) {
        val value = try {
            obj[key]?.jsonPrimitive?.content
        } catch (exception: Exception) {
            null
        }

        require(!value.isNullOrBlank()) {
            "The roadmap field '$key' is missing or empty."
        }
    }
}

// private const val API_KEY = "AQ.Ab8RN6Ipmtj41k-f_Ie9qazUvvOZDHrg77HUJYKrb49ZV7Nc8Q"
