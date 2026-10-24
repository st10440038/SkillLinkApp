
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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put

object GeminiService {

    // TODO: Move the API key out of the Android app before release.
    // Rotate the previously exposed key and do not commit secrets to Git.
    private const val API_KEY = "AQ.Ab8RN6Ipmtj41k-f_Ie9qazUvvOZDHrg77HUJYKrb49ZV7Nc8Q"

    private const val MODEL = "gemini-3.5-flash-lite"

    private const val URL =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = HttpClient(Android)

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

            IMPORTANT LANGUAGE RULE:
            Write ALL learner-facing text in $selectedLanguage.
            This includes the roadmap title, summary, week titles,
            week descriptions, and every lesson.
            Do not mix languages unless a technical term, programming
            keyword, product name, or proper noun should remain unchanged.
            Keep technical explanations understandable for the learner.
            The JSON property names must remain in English exactly as
            specified below so the app can parse the response.

            Learner details:
            Target skill: $targetSkill
            Experience level: $experienceLevel
            Known skills: $knownSkills
            Learning goal: $learningGoal
            Weekly learning time: $weeklyTime
            Learning pace: $learningPace

            Determine an appropriate number of weeks based on:
            - The learner's experience level
            - The skills they already know
            - Their learning goal
            - Their weekly learning time
            - Their learning pace

            Do not always use the same number of weeks.
            Make the roadmap as short as reasonably possible while
            still covering the skills needed to achieve the goal.

            Each week must contain:
            - A week title
            - A short description
            - Exactly 3 lessons

            Return ONLY valid JSON. Do not include Markdown fences,
            introductory text, or explanations outside the JSON.

            Use exactly this structure:
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

        val json = Json.parseToJsonElement(responseText)

        val generatedText = json.jsonObject["candidates"]
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
                "Gemini returned no generated roadmap text."
            )
    }
}

// private const val API_KEY = "AQ.Ab8RN6Ipmtj41k-f_Ie9qazUvvOZDHrg77HUJYKrb49ZV7Nc8Q"
