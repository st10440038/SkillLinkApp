package com.example.skilllinkapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.data.SkillRepository
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.network.GeminiService
import com.example.skilllinkapp.ui.theme.SkillLinkCardBackground
import com.example.skilllinkapp.ui.theme.SkillLinkLightPurple
import com.example.skilllinkapp.ui.theme.SkillLinkNavy
import com.example.skilllinkapp.ui.theme.SkillLinkPurple
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


private val PageBackground = Color(0xFFF7F5FC)
private val White = Color.White
private val TextSecondary = Color(0xFF77737D)
private val BorderColor = Color(0xFFE7E2EF)
private val SoftPurple = Color(0xFFF0EAFF)
private val PurpleDark = Color(0xFF6941C6)


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
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
    ) {

        // ---------------------------------------------------------
        // TOP BAR
        // ---------------------------------------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(13.dp),
                color = White,
                shadowElevation = 1.dp
            ) {

                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {

                    androidx.compose.material3.Text(
                        text = "‹",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SkillLinkNavy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "New roadmap",
                    style = MaterialTheme.typography.titleLarge,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Personalise your learning journey",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }


        // ---------------------------------------------------------
        // HERO
        // ---------------------------------------------------------

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SkillLinkPurple)
                .padding(22.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(alpha = 0.18f)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "S",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.width(14.dp)
                )

                Column {

                    Text(
                        text = "Your learning journey",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "Answer a few questions and we'll create a roadmap tailored to your goals, experience and available time.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ---------------------------------------------------------
        // SECTION 01
        // ---------------------------------------------------------

        RoadmapSectionHeader(
            number = "01",
            title = "Your current skills",
            description = "Tell us what you already know."
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        FormCard {

            Text(
                text = "Skills you already know",
                style = MaterialTheme.typography.titleMedium,
                color = SkillLinkNavy,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Separate multiple skills with commas.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            OutlinedTextField(
                value = knownSkills,
                onValueChange = {
                    knownSkills = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "e.g. Python, SQL, Excel"
                    )
                },
                minLines = 3,
                shape = RoundedCornerShape(14.dp)
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ---------------------------------------------------------
        // SECTION 02
        // ---------------------------------------------------------

        RoadmapSectionHeader(
            number = "02",
            title = "Your goal",
            description = "Tell us where you want to go."
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        FormCard {

            Text(
                text = "What do you want to achieve?",
                style = MaterialTheme.typography.titleMedium,
                color = SkillLinkNavy,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Describe your goal naturally. You don't need to know the exact skill name.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            OutlinedTextField(
                value = learningGoal,
                onValueChange = {
                    learningGoal = it
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "e.g. I want to become a data scientist"
                    )
                },
                minLines = 4,
                shape = RoundedCornerShape(14.dp)
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ---------------------------------------------------------
        // SECTION 03
        // ---------------------------------------------------------

        RoadmapSectionHeader(
            number = "03",
            title = "Your experience",
            description = "Choose the level that describes you best."
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        FormCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                listOf(
                    "Beginner",
                    "Intermediate",
                    "Advanced"
                ).forEach { level ->

                    SelectionChip(
                        text = level,
                        selected = experienceLevel == level,
                        onClick = {
                            experienceLevel = level
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ---------------------------------------------------------
        // SECTION 04
        // ---------------------------------------------------------

        RoadmapSectionHeader(
            number = "04",
            title = "Your schedule",
            description = "Choose how much time you can realistically give."
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        FormCard {
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                listOf(
                    "1–2 hours",
                    "3–5 hours",
                    "6–10 hours",
                    "10+ hours"
                ).forEach { time ->

                    ScheduleOption(
                        text = time,
                        selected = weeklyTime == time,
                        onClick = {
                            weeklyTime = time
                        }
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ---------------------------------------------------------
        // LEARNING PACE
        // ---------------------------------------------------------

        FormCard {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SoftPurple),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "P",
                        color = SkillLinkPurple,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Text(
                    text = "Learning pace",
                    style = MaterialTheme.typography.titleMedium,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "How quickly would you like to progress?",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                listOf(
                    "Relaxed",
                    "Balanced",
                    "Fast"
                ).forEach { pace ->

                    SelectionChip(
                        text = pace,
                        selected = learningPace == pace,
                        onClick = {
                            learningPace = pace
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ---------------------------------------------------------
        // ERROR
        // ---------------------------------------------------------

        if (errorMessage.isNotEmpty()) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFF0F0)
            ) {

                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }


        // ---------------------------------------------------------
        // GENERATE BUTTON
        // ---------------------------------------------------------

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


                        val roadmapId =
                            createdRoadmap["roadmap_id"]
                                ?.jsonPrimitive
                                ?.content
                                ?: throw Exception(
                                    "Could not get the roadmap ID."
                                )


                        generatedRoadmap = response


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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SkillLinkPurple,
                contentColor = Color.White
            ),
            enabled = !isGenerating
        ) {

            if (isGenerating) {

                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Text(
                    text = "Creating your roadmap...",
                    fontWeight = FontWeight.SemiBold
                )

            } else {

                Text(
                    text = "✦",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.width(9.dp)
                )

                Text(
                    text = "Generate my roadmap",
                    fontWeight = FontWeight.Bold
                )
            }
        }


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // ---------------------------------------------------------
        // CANCEL
        // ---------------------------------------------------------

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isGenerating
        ) {

            Text(
                text = "Cancel",
                color = SkillLinkNavy
            )
        }


        Spacer(
            modifier = Modifier.height(32.dp)
        )
    }
}


/*
 * SECTION HEADER
 */

@Composable
private fun RoadmapSectionHeader(
    number: String,
    title: String,
    description: String
) {

    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SkillLinkPurple),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SkillLinkNavy,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}


/*
 * FORM CARD
 */

@Composable
private fun FormCard(
    content: @Composable () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = White,
        shadowElevation = 1.dp
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            content()
        }
    }
}


/*
 * EXPERIENCE / PACE OPTION
 */

@Composable
private fun SelectionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(13.dp),
        color = if (selected) {
            SoftPurple
        } else {
            White
        },
        border = if (!selected) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                BorderColor
            )
        } else {
            null
        }
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) {
                    PurpleDark
                } else {
                    SkillLinkNavy
                },
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                }
            )
        }
    }
}


/*
 * WEEKLY TIME OPTION
 */

@Composable
private fun ScheduleOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            SoftPurple
        } else {
            White
        },
        border = if (!selected) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                BorderColor
            )
        } else {
            null
        }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) {
                    PurpleDark
                } else {
                    SkillLinkNavy
                },
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )

            if (selected) {

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SkillLinkPurple),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "✓",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}