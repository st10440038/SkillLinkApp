package com.example.skilllinkapp.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put

object GeminiService {
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

        val prompt = """
            You are an expert learning designer.

            Create a practical personalized learning roadmap.

            Target skill: $targetSkill
            Experience level: $experienceLevel
            Known skills: $knownSkills
            Learning goal: $learningGoal
            Weekly learning time: $weeklyTime
            Learning pace: $learningPace

            Create a personalized learning roadmap.

            Determine the appropriate number of weeks based on:
            - the learner's experience level
            - the skills they already know
            - their learning goal
            - their weekly learning time
            - their learning pace
            
            Do not always use the same number of weeks.
            The roadmap should be as short as reasonably possible while still covering everything needed to achieve the learning goal.
            
            Each week must contain:
            - a week title
            - a short description
            - exactly 3 lessons

            Return ONLY valid JSON.

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

        val response = client.post(URL) {

            header(
                "x-goog-api-key",
                API_KEY
            )

            contentType(ContentType.Application.Json)

            setBody(requestBody.toString())
        }

        val responseText = response.bodyAsText()

        val json = Json.parseToJsonElement(responseText)

        val text = json
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

        return text ?: responseText
    }
}