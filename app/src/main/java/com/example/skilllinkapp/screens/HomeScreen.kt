package com.example.skilllinkapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.data.ProgressRepository
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.ui.theme.SkillLinkBackground
import com.example.skilllinkapp.ui.theme.SkillLinkCardBackground
import com.example.skilllinkapp.ui.theme.SkillLinkLightPurple
import com.example.skilllinkapp.ui.theme.SkillLinkNavy
import com.example.skilllinkapp.ui.theme.SkillLinkPurple
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun HomeScreen(
    onCompleteProfile: () -> Unit,
    onGenerateRoadmap: () -> Unit,
    onLogout: () -> Unit,
    roadmapId: String = "",
    roadmapJson: String = ""
) {

    var currentRoadmapId by remember {
        mutableStateOf(roadmapId)
    }

    var roadmapTitle by remember {
        mutableStateOf("Start your learning journey")
    }

    var completedLessons by remember {
        mutableStateOf(0)
    }

    var totalLessons by remember {
        mutableStateOf(0)
    }

    var loadError by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        val user = supabase.auth.currentUserOrNull()

        if (user == null) {
            return@LaunchedEffect
        }

        try {

            /*
             * Get the user's roadmaps from Supabase.
             * Each row is treated as a JSON object so that
             * we don't have to create a Kotlin data class
             * for every column in the table.
             */
            val roadmaps =
                supabase
                    .from("roadmaps")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<JsonObject>()

            if (roadmaps.isEmpty()) {

                roadmapTitle = "Start your learning journey"
                currentRoadmapId = ""
                completedLessons = 0
                totalLessons = 0

                return@LaunchedEffect
            }

            /*
             * Use the most recently returned roadmap.
             */
            val latestRoadmap =
                roadmaps.last()

            /*
             * Get the roadmap ID.
             */
            val roadmapIdElement =
                latestRoadmap["roadmap_id"]

            currentRoadmapId =
                roadmapIdElement
                    ?.jsonPrimitive
                    ?.content
                    ?: ""

            /*
             * Get the target skill.
             */
            val targetSkillElement =
                latestRoadmap["target_skill"]

            roadmapTitle =
                targetSkillElement
                    ?.jsonPrimitive
                    ?.content
                    ?: "Your learning roadmap"

            /*
             * Get roadmap_data.
             */
            val roadmapDataElement: JsonElement? =
                latestRoadmap["roadmap_data"]

            if (roadmapDataElement == null) {
                throw Exception("roadmap_data was not found.")
            }

            /*
             * Convert roadmap_data into a JsonObject.
             *
             * Supabase may return JSONB as either:
             * 1. a JsonObject
             * 2. a JSON string containing an object
             */
            val roadmapObject: JsonObject =
                when (roadmapDataElement) {

                    is JsonObject -> {
                        roadmapDataElement
                    }

                    is JsonPrimitive -> {

                        if (!roadmapDataElement.isString) {
                            throw Exception(
                                "roadmap_data is not a JSON string."
                            )
                        }

                        Json.parseToJsonElement(
                            roadmapDataElement.content
                        ).jsonObject
                    }

                    else -> {
                        throw Exception(
                            "Unexpected roadmap_data format."
                        )
                    }
                }

            /*
             * Get the weeks from the generated roadmap.
             */
            val weeks =
                roadmapObject["weeks"]
                    ?.jsonArray

            /*
             * Count all lessons.
             *
             * Each week contains:
             * "lessons": ["Lesson 1", "Lesson 2", "Lesson 3"]
             */
            totalLessons =
                weeks?.sumOf { weekElement ->

                    weekElement
                        .jsonObject["lessons"]
                        ?.jsonArray
                        ?.size
                        ?: 0

                } ?: 0

            /*
             * Load progress from Supabase.
             */
            val progress =
                ProgressRepository.getProgress(
                    roadmapId = currentRoadmapId,
                    userId = user.id
                )

            /*
             * Count only completed lessons.
             */
            completedLessons =
                progress.count {
                    it.is_completed
                }

        } catch (e: Exception) {

            loadError =
                e.message
                    ?: "Could not load roadmap."

            completedLessons = 0
            totalLessons = 0
        }
    }

    /*
     * Calculate progress percentage.
     *
     * Example:
     * 1 completed lesson / 18 total lessons
     * = 5%
     */
    val progressPercentage =
        if (totalLessons > 0) {
            completedLessons.toFloat() /
                    totalLessons.toFloat()
        } else {
            0f
        }

    val percentageText =
        "${(progressPercentage * 100).toInt()}% complete"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillLinkBackground)
            .padding(20.dp)
    ) {

        /*
         * Temporary error message.
         *
         * We keep this visible while testing so that
         * Supabase errors don't get hidden.
         */
        if (loadError.isNotBlank()) {

            Text(
                text = "Roadmap loading error: $loadError",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    bottom = 8.dp
                )
            )
        }

        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineMedium,
            color = SkillLinkNavy
        )

        Text(
            text = "Teach. Learn. Connect.",
            style = MaterialTheme.typography.bodyLarge,
            color = SkillLinkNavy,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Recommended skills",
            style = MaterialTheme.typography.titleLarge,
            color = SkillLinkNavy
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            SkillCard("Python")
            SkillCard("Kotlin")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Your current roadmap",
            style = MaterialTheme.typography.titleLarge,
            color = SkillLinkNavy
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = SkillLinkCardBackground
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = roadmapTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = SkillLinkNavy
                )

                Text(
                    text =
                        if (currentRoadmapId.isNotBlank()) {
                            "Continue working through your personalised roadmap."
                        } else {
                            "Create a personalised roadmap based on your target skill."
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )

                LinearProgressIndicator(
                    progress = {
                        progressPercentage
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

                Text(
                    text = percentageText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Button(
                    onClick = onGenerateRoadmap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Generate Roadmap")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log out")
        }
    }
}

@Composable
private fun SkillCard(
    skillName: String
) {

    Surface(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(14.dp),
        color = SkillLinkLightPurple
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = skillName,
                style = MaterialTheme.typography.titleMedium,
                color = SkillLinkPurple
            )

            Text(
                text = "Recommended",
                style = MaterialTheme.typography.bodySmall,
                color = SkillLinkNavy,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}