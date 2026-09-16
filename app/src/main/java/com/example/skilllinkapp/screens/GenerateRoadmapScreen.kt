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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.data.SkillRepository
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.network.GeminiService
import com.example.skilllinkapp.ui.theme.SkillLinkBackground
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Composable
fun GenerateRoadmapScreen(
    targetSkill: String,
    onBack: () -> Unit,
    onGenerate: (
        targetSkill: String,
        experienceLevel: String,
        knownSkills: String,
        learningGoal: String,
        weeklyTime: String,
        learningPace: String
    ) -> Unit,
    onRoadmapGenerated: (String, String) -> Unit
) {

    var knownSkills by remember {
        mutableStateOf("")
    }

    var learningGoal by remember {
        mutableStateOf("")
    }

    var experienceLevel by remember {
        mutableStateOf("Beginner")
    }

    var weeklyTime by remember {
        mutableStateOf("3–5 hours")
    }

    var learningPace by remember {
        mutableStateOf("Balanced")
    }

    var generatedRoadmap by remember {
        mutableStateOf("")
    }

    var isGenerating by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillLinkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            text = "Create Your Roadmap",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Tell us what you want to achieve and what you already know.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Skills You Already Know",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Add skills you already have. This helps the AI avoid teaching you things you already know.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp)
        )

        TextField(
            value = knownSkills,
            onValueChange = {
                knownSkills = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            placeholder = {
                Text("e.g. Power BI, Excel, SQL")
            },
            singleLine = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "What Do You Want to Achieve?",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "You don't need to know the name of the skill you need to learn.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp)
        )

        TextField(
            value = learningGoal,
            onValueChange = {
                learningGoal = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            placeholder = {
                Text("e.g. I want to become a data scientist")
            },
            singleLine = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Experience Level",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            listOf(
                "Beginner",
                "Intermediate",
                "Advanced"
            ).forEach { level ->

                FilterChip(
                    selected = experienceLevel == level,
                    onClick = {
                        experienceLevel = level
                    },
                    label = {
                        Text(level)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Weekly Learning Time",
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            listOf(
                "1–2 hours",
                "3–5 hours",
                "6–10 hours",
                "10+ hours"
            ).forEach { time ->

                OutlinedButton(
                    onClick = {
                        weeklyTime = time
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        if (weeklyTime == time) {
                            "✓ $time"
                        } else {
                            time
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Learning Pace",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            listOf(
                "Relaxed",
                "Balanced",
                "Fast"
            ).forEach { pace ->

                FilterChip(
                    selected = learningPace == pace,
                    onClick = {
                        learningPace = pace
                    },
                    label = {
                        Text(pace)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {

                if (learningGoal.isBlank()) {

                    errorMessage =
                        "Please enter your learning goal."

                    return@Button
                }

                isGenerating = true
                errorMessage = ""
                generatedRoadmap = ""

                scope.launch {

                    try {

                        val user =
                            supabase.auth.currentUserOrNull()

                        if (user == null) {
                            throw Exception(
                                "You must be logged in to save your skills."
                            )
                        }

                        val skillsToSave =
                            knownSkills
                                .split(",")
                                .map {
                                    it.trim()
                                }
                                .filter {
                                    it.isNotEmpty()
                                }
                                .distinctBy {
                                    it.lowercase()
                                }

                        for (skill in skillsToSave) {

                            SkillRepository.saveKnownSkill(
                                userId = user.id,
                                skillName = skill
                            )
                        }

                        val response =
                            GeminiService.generateRoadmap(
                                targetSkill =
                                    "Determine the appropriate skill from the learning goal",
                                experienceLevel =
                                    experienceLevel,
                                knownSkills =
                                    knownSkills,
                                learningGoal =
                                    learningGoal,
                                weeklyTime =
                                    weeklyTime,
                                learningPace =
                                    learningPace
                            )

                        val knownSkillsJson =
                            buildJsonArray {

                                knownSkills
                                    .split(",")
                                    .map {
                                        it.trim()
                                    }
                                    .filter {
                                        it.isNotEmpty()
                                    }
                                    .distinctBy {
                                        it.lowercase()
                                    }
                                    .forEach { skill ->

                                        add(
                                            JsonPrimitive(skill)
                                        )
                                    }
                            }

                        val roadmapJson =
                            try {

                                Json.parseToJsonElement(
                                    response
                                )

                            } catch (e: Exception) {

                                JsonPrimitive(response)
                            }

                        val roadmapData =
                            buildJsonObject {

                                put(
                                    "generated_roadmap",
                                    roadmapJson
                                )
                            }

                        /*
                         * Save the roadmap to Supabase
                         * and return the newly created row.
                         */
                        val createdRoadmap =
                            supabase
                                .from("roadmaps")
                                .insert(
                                    buildJsonObject {

                                        put(
                                            "user_id",
                                            user.id
                                        )

                                        put(
                                            "target_skill",
                                            targetSkill
                                        )

                                        put(
                                            "roadmap_data",
                                            roadmapData
                                        )

                                        put(
                                            "learning_goal",
                                            learningGoal
                                        )

                                        put(
                                            "experience_level",
                                            experienceLevel
                                        )

                                        put(
                                            "weekly_time",
                                            weeklyTime
                                        )

                                        put(
                                            "learning_pace",
                                            learningPace
                                        )

                                        put(
                                            "known_skills",
                                            knownSkillsJson
                                        )
                                    }
                                ) {
                                    select()
                                }
                                .decodeSingle<JsonObject>()

                        /*
                         * Get the UUID generated by Supabase.
                         */
                        val roadmapId =
                            createdRoadmap["roadmap_id"]
                                ?.jsonPrimitive
                                ?.content
                                ?: throw Exception(
                                    "Could not get the roadmap ID."
                                )

                        generatedRoadmap = response

                        /*
                         * Send the roadmap JSON and
                         * the real roadmap UUID back.
                         */
                        onRoadmapGenerated(
                            response,
                            roadmapId
                        )

                    } catch (e: Exception) {

                        errorMessage =
                            e.message
                                ?: "Something went wrong while generating the roadmap."

                    } finally {

                        isGenerating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(),
            enabled = !isGenerating
        ) {

            Text(
                if (isGenerating) {
                    "Generating..."
                } else {
                    "Generate Roadmap"
                }
            )
        }

        if (errorMessage.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (generatedRoadmap.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Generated Roadmap",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = generatedRoadmap,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Back")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )
    }
}