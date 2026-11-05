
package com.example.skilllinkapp.network

import com.example.skilllinkapp.LanguageSupport
import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.functions.*
import io.ktor.client.call.body
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class NotificationTranslationInput(
    val id: String,
    val title: String,
    val message: String
)

object GeminiService {

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

        validateRoadmapJson(roadmapJson)

        if (targetLanguage == "English") {
            return roadmapJson
        }

        val prompt = """
            You are a professional multilingual educational translator.

            YOUR TASK:
            Translate the entire learning roadmap into $targetLanguage.

            LANGUAGE REQUIREMENT:
            The output MUST be written in $targetLanguage.

            Translate ALL learner-facing content, including:
            - Roadmap title
            - Roadmap summary
            - Every week title
            - Every week description
            - Every lesson title and description, if present

            IMPORTANT:
            1. Do not leave ordinary English sentences untranslated.
            2. Do not return the original English roadmap.
            3. Do not summarize or shorten the content.
            4. Preserve the original meaning and detail.
            5. Keep technical terms, programming keywords and product
               names in English where appropriate.
            6. Keep JSON property names unchanged.
            7. Preserve the exact number and order of weeks.
            8. Preserve exactly three lessons per week.
            9. Do not add or remove any content.
            10. Return valid JSON only. No Markdown or explanations.

            REQUIRED JSON STRUCTURE:
            {
              "title": "Translated roadmap title",
              "summary": "Translated roadmap summary",
              "weeks": [
                {
                  "title": "Translated week title",
                  "description": "Translated description",
                  "lessons": [
                    "Translated lesson 1",
                    "Translated lesson 2",
                    "Translated lesson 3"
                  ]
                }
              ]
            }

            ORIGINAL ROADMAP:
            $roadmapJson
        """.trimIndent()

        val translatedText = requestGeneratedText(prompt)

        validateRoadmapJson(translatedText)

        return translatedText
    }

    /**
     * Translates all supplied notifications in ONE Gemini request.
     */
    suspend fun translateNotifications(
        notifications: List<NotificationTranslationInput>,
        targetLanguage: String
    ): Map<String, Pair<String, String>> {

        require(
            targetLanguage in listOf("English", "isiZulu", "Afrikaans")
        ) {
            "Unsupported language: $targetLanguage"
        }

        if (notifications.isEmpty()) {
            return emptyMap()
        }

        if (targetLanguage == "English") {
            return notifications.associate {
                it.id to (it.title to it.message)
            }
        }

        val originalNotificationsJson = buildJsonArray {
            notifications.forEach { notification ->
                add(
                    buildJsonObject {
                        put("id", notification.id)
                        put("title", notification.title)
                        put("message", notification.message)
                    }
                )
            }
        }

        val prompt = """
            You are an expert professional translator fluent in English,
            South African Afrikaans, and isiZulu.

            YOUR TASK:
            Translate EVERY notification in the input JSON array into
            $targetLanguage.

            STRICT LANGUAGE REQUIREMENTS:
            1. The target language is exactly $targetLanguage.
            2. If the target is Afrikaans, write in natural, standard
               South African Afrikaans. DO NOT use isiZulu.
            3. If the target is isiZulu, write in natural, standard isiZulu.
               DO NOT use Afrikaans.
            4. Translate every notification title and message.
            5. Do not leave ordinary English sentences untranslated.
            6. Preserve technical terms and proper nouns where appropriate.
            7. Preserve the original meaning and tone.
            8. Do not summarize, omit, or add information.
            9. Preserve every notification ID exactly.
            10. Return exactly the same number of notifications.
            11. Keep the JSON property names in English.
            12. Return ONLY valid JSON. No Markdown or explanations.

            INPUT NOTIFICATIONS:
            $originalNotificationsJson

            REQUIRED OUTPUT:
            [
              {
                "id": "original-notification-id",
                "title": "Translated title",
                "message": "Translated message"
              }
            ]

            IMPORTANT:
            Before returning the result, verify that every notification
            is translated into $targetLanguage, not another language.
        """.trimIndent()

        val translatedText = requestGeneratedText(prompt)

        val translatedArray = try {
            Json.parseToJsonElement(translatedText).jsonArray
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                "Gemini returned invalid notification translation JSON.",
                exception
            )
        }

        require(translatedArray.size == notifications.size) {
            "Gemini returned ${translatedArray.size} translations, " +
                    "but ${notifications.size} were expected."
        }

        val translatedNotifications =
            mutableMapOf<String, Pair<String, String>>()

        for (element in translatedArray) {

            val item = try {
                element.jsonObject
            } catch (exception: Exception) {
                throw IllegalArgumentException(
                    "Gemini returned an invalid notification object.",
                    exception
                )
            }

            val id = item["id"]?.jsonPrimitive?.content
            val translatedTitle = item["title"]?.jsonPrimitive?.content
            val translatedMessage = item["message"]?.jsonPrimitive?.content

            require(
                !id.isNullOrBlank() &&
                        !translatedTitle.isNullOrBlank() &&
                        !translatedMessage.isNullOrBlank()
            ) {
                "Gemini returned an incomplete notification translation."
            }

            require(notifications.any { it.id == id }) {
                "Gemini returned an unexpected notification ID: $id"
            }

            require(!translatedNotifications.containsKey(id)) {
                "Gemini returned a duplicate notification ID: $id"
            }

            translatedNotifications[id] = translatedTitle to translatedMessage
        }

        require(translatedNotifications.size == notifications.size) {
            "Gemini did not translate every notification."
        }

        return translatedNotifications
    }

    /**
     * Sends a prompt to Gemini and extracts the generated text.
     * Both roadmap generation and translation use this same request logic.
     */
    private suspend fun requestGeneratedText(prompt: String): String {

        val response = supabase.functions.invoke(
            function = "generate-roadmap",
            body = buildJsonObject {
                put("prompt", prompt)
            }
        )

        val responseJson = response.body<JsonObject>()

        val generatedText = responseJson["text"]
            ?.jsonPrimitive
            ?.content

        return generatedText
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                "Supabase returned no generated text."
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
