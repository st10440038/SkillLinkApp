package com.example.skilllinkapp.screens

import kotlinx.serialization.json.Json
import androidx.compose.ui.platform.LocalContext
import com.example.skilllinkapp.SkillLinkApplication
import com.example.skilllinkapp.data.local.CachedRoadmapEntity
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun HomeScreen(
    onGenerateRoadmap: () -> Unit,
    onContinueLearning: () -> Unit,
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

    var isLoading by remember {
        mutableStateOf(true)
    }

    // =========================================================
    // LOAD DASHBOARD DATA
    // =========================================================

    val user = supabase.auth.currentUserOrNull()

    val context = LocalContext.current

    val roadmapDao = remember(context) {
        (context.applicationContext as SkillLinkApplication)
            .database
            .skillLinkDao()
    }


    LaunchedEffect(
        user?.id,
        roadmapId,
        roadmapJson
    ) {
        if (user == null) {
            isLoading = false
            loadError = "You must be logged in."
            return@LaunchedEffect
        }

        isLoading = true
        loadError = ""

        try {
            // Use roadmap data supplied by the app if available.
            if (roadmapId.isNotBlank() && roadmapJson.isNotBlank()) {
                currentRoadmapId = roadmapId

                val suppliedRoadmap = parseRoadmapJson(roadmapJson)

                roadmapTitle = suppliedRoadmap
                    ?.get("title")
                    ?.jsonPrimitive
                    ?.content
                    ?: "Your learning roadmap"

                totalLessons = countLessons(suppliedRoadmap)

                val progress = ProgressRepository.getProgress(
                    roadmapId = roadmapId,
                    userId = user.id
                )

                completedLessons = progress.count { it.is_completed }
                isLoading = false
                return@LaunchedEffect
            }

            // Try Supabase first.
            val remoteRoadmaps = supabase
                .from("roadmaps")
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<JsonObject>()

            // Cache each roadmap locally for offline use.
            val cachedRoadmaps = remoteRoadmaps.mapNotNull { item ->
                try {
                    val id = item["roadmap_id"]
                        ?.jsonPrimitive
                        ?.content
                        ?: return@mapNotNull null

                    val data = item["roadmap_data"]
                        ?: return@mapNotNull null

                    val parsed = parseRoadmapElement(data)
                    val title = parsed
                        ?.get("title")
                        ?.jsonPrimitive
                        ?.content
                        ?: item["target_skill"]
                            ?.jsonPrimitive
                            ?.content
                        ?: "Your learning roadmap"

                    CachedRoadmapEntity(
                        roadmapId = id,
                        userId = user.id,
                        title = title,
                        roadmapJson = item.toString()
                    )
                } catch (_: Exception) {
                    null
                }
            }

            if (cachedRoadmaps.isNotEmpty()) {
                roadmapDao.saveRoadmaps(cachedRoadmaps)
            }

            // Choose the most recent roadmap.
            val latestRoadmap = remoteRoadmaps.maxByOrNull {
                it["created_at"]?.jsonPrimitive?.content ?: ""
            }

            if (latestRoadmap == null) {
                currentRoadmapId = ""
                roadmapTitle = "Start your learning journey"
                completedLessons = 0
                totalLessons = 0
            } else {
                currentRoadmapId = latestRoadmap["roadmap_id"]
                    ?.jsonPrimitive
                    ?.content
                    ?: ""

                roadmapTitle = latestRoadmap["target_skill"]
                    ?.jsonPrimitive
                    ?.content
                    ?.takeIf { it.isNotBlank() }
                    ?: "Your learning roadmap"

                val roadmapObject = parseRoadmapElement(
                    latestRoadmap["roadmap_data"]
                )

                totalLessons = countLessons(roadmapObject)

                completedLessons = if (currentRoadmapId.isNotBlank()) {
                    ProgressRepository.getProgress(
                        roadmapId = currentRoadmapId,
                        userId = user.id
                    ).count { it.is_completed }
                } else {
                    0
                }
            }
        } catch (networkError: Exception) {
            // Supabase failed: fall back to the cached roadmaps.
            try {
                val cached = roadmapDao
                    .observeRoadmaps(user.id)
                    .first()

                val latestCached = cached.maxByOrNull {
                    it.cachedAt
                }

                if (latestCached == null) {
                    currentRoadmapId = ""
                    roadmapTitle = "Start your learning journey"
                    completedLessons = 0
                    totalLessons = 0

                    loadError =
                        "No roadmaps are saved for offline use yet. Connect to the internet and open My Learning first."
                } else {
                    currentRoadmapId = latestCached.roadmapId

                    val cachedRow = Json
                        .parseToJsonElement(latestCached.roadmapJson)
                        .jsonObject

                    val roadmapObject = parseRoadmapElement(
                        cachedRow["roadmap_data"]
                    )

                    roadmapTitle = latestCached.title
                    totalLessons = countLessons(roadmapObject)

                    completedLessons = ProgressRepository.getProgress(
                        roadmapId = currentRoadmapId,
                        userId = user.id
                    ).count { it.is_completed }
                }
            } catch (cacheError: Exception) {
                loadError =
                    "Could not load your dashboard or saved offline roadmap."
            }
        } finally {
            isLoading = false
        }
    }


    // =========================================================
    // PROGRESS
    // =========================================================

    val progressPercentage =
        if (totalLessons > 0) {

            (
                    completedLessons.toFloat() /
                            totalLessons.toFloat()
                    ).coerceIn(0f, 1f)

        } else {
            0f
        }

    val percentage =
        (progressPercentage * 100).toInt()

    val hasRoadmap =
        currentRoadmapId.isNotBlank()

    val roadmapCompleted =
        hasRoadmap &&
                totalLessons > 0 &&
                completedLessons >= totalLessons

    // =========================================================
    // SCREEN
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillLinkBackground)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 20.dp,
                vertical = 24.dp
            )
    ) {

        // =====================================================
        // HEADER
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Ready to learn something new?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF777277)
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SkillLinkLightPurple),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "S",
                    style = MaterialTheme.typography.titleMedium,
                    color = SkillLinkPurple,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =====================================================
        // WELCOME CARD
        // =====================================================

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = SkillLinkPurple
        ) {

            Column(
                modifier = Modifier.padding(22.dp)
            ) {

                Text(
                    text = "Teach. Learn. Connect.",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Share what you know, discover new skills, and build your personalised learning journey.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(
                        alpha = 0.9f
                    )
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =====================================================
        // RECOMMENDED SKILLS
        // =====================================================

        Text(
            text = "Recommended skills",
            style = MaterialTheme.typography.titleLarge,
            color = SkillLinkNavy,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            SkillCard(
                skillName = "Python",
                description = "Programming",
                modifier = Modifier.weight(1f),
                onClick = onGenerateRoadmap
            )

            SkillCard(
                skillName = "Kotlin",
                description = "Android",
                modifier = Modifier.weight(1f),
                onClick = onGenerateRoadmap
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =====================================================
        // LEARNING JOURNEY HEADER
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Your learning journey",
                style = MaterialTheme.typography.titleLarge,
                color = SkillLinkNavy,
                fontWeight = FontWeight.SemiBold
            )

            if (hasRoadmap) {

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelLarge,
                    color = SkillLinkPurple,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // =====================================================
        // ROADMAP CARD
        // =====================================================

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = SkillLinkCardBackground
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                if (isLoading) {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = SkillLinkPurple,
                            strokeWidth = 3.dp
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Text(
                            text = "Loading your learning journey...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF777277)
                        )
                    }

                } else {

                    // -------------------------------------------------
                    // ROADMAP EXISTS
                    // -------------------------------------------------

                    if (hasRoadmap) {

                        Text(
                            text = roadmapTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = SkillLinkNavy,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                if (roadmapCompleted) {
                                    "You completed this roadmap. Keep building your skills or start a new one."
                                } else {
                                    "Continue working through your personalised learning roadmap."
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF777277)
                        )

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        LinearProgressIndicator(
                            progress = {
                                progressPercentage
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(
                                    RoundedCornerShape(50.dp)
                                ),
                            color = SkillLinkPurple,
                            trackColor = SkillLinkLightPurple
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "$completedLessons of $totalLessons lessons completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF777277)
                        )

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        Button(
                            onClick = onContinueLearning,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkillLinkPurple
                            )
                        ) {

                            Text(
                                text =
                                    if (roadmapCompleted) {
                                        "View roadmap"
                                    } else {
                                        "Continue learning"
                                    },
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        TextButton(
                            onClick = onGenerateRoadmap,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text = "Create another roadmap",
                                color = SkillLinkPurple
                            )
                        }

                    } else {

                        // -------------------------------------------------
                        // NO ROADMAP
                        // -------------------------------------------------

                        Text(
                            text = "Start your learning journey",
                            style = MaterialTheme.typography.titleMedium,
                            color = SkillLinkNavy,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = "Tell SkillLink what you want to learn and create a personalised roadmap.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF777277)
                        )

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        Button(
                            onClick = onGenerateRoadmap,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkillLinkPurple
                            )
                        ) {

                            Text(
                                text = "Generate roadmap",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // =====================================================
        // ERROR
        // =====================================================

        if (loadError.isNotBlank()) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF1F2)
            ) {

                Text(
                    text = loadError,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )
    }
}


// =============================================================
// SKILL CARD
// =============================================================

@Composable
private fun SkillCard(
    skillName: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = SkillLinkLightPurple
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = skillName
                        .first()
                        .toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = SkillLinkPurple,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = skillName,
                style = MaterialTheme.typography.titleMedium,
                color = SkillLinkNavy,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777277)
            )
        }
    }
}


// =============================================================
// ROADMAP JSON PARSING
// =============================================================

private fun parseRoadmapElement(
    element: JsonElement?
): JsonObject? {

    if (element == null) {
        return null
    }

    return try {

        when (element) {

            is JsonObject -> {

                val generated =
                    element["generated_roadmap"]

                if (generated != null) {

                    when (generated) {

                        is JsonObject ->
                            generated

                        is JsonPrimitive -> {

                            if (generated.isString) {
                                generated
                                    .content
                                    .let {
                                        kotlinx.serialization.json
                                            .Json
                                            .parseToJsonElement(it)
                                            .jsonObject
                                    }
                            } else {
                                element
                            }
                        }

                        else -> element
                    }

                } else {
                    element
                }
            }

            is JsonPrimitive -> {

                if (!element.isString) {
                    null
                } else {

                    val parsed =
                        kotlinx.serialization.json
                            .Json
                            .parseToJsonElement(
                                element.content
                            )

                    parseRoadmapElement(parsed)
                }
            }

            else -> null
        }

    } catch (e: Exception) {

        null
    }
}


private fun parseRoadmapJson(
    json: String
): JsonObject? {

    return try {

        val element =
            kotlinx.serialization.json
                .Json
                .parseToJsonElement(json)

        parseRoadmapElement(element)

    } catch (e: Exception) {

        null
    }
}


// =============================================================
// COUNT LESSONS
// =============================================================

private fun countLessons(
    roadmap: JsonObject?
): Int {

    if (roadmap == null) {
        return 0
    }

    val weeks =
        roadmap["weeks"]?.jsonArray
            ?: return 0

    return weeks.sumOf { weekElement ->

        weekElement
            .jsonObject["lessons"]
            ?.jsonArray
            ?.size
            ?: 0
    }
}