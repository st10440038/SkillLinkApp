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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import androidx.compose.foundation.clickable

@Composable
fun MyLearningScreen(
    onOpenRoadmap: (String, String) -> Unit,
    onGenerateRoadmap: () -> Unit
) {

    var roadmaps by remember {
        mutableStateOf<List<JsonObject>>(emptyList())
    }

    var roadmapProgress by remember {
        mutableStateOf<Map<String, Pair<Int, Int>>>(emptyMap())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {

        val user = supabase.auth.currentUserOrNull()

        if (user == null) {
            isLoading = false
            errorMessage = "You must be logged in."
            return@LaunchedEffect
        }

        try {

            val userRoadmaps =
                supabase
                    .from("roadmaps")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<JsonObject>()

            roadmaps = userRoadmaps

            val progressMap =
                mutableMapOf<String, Pair<Int, Int>>()

            for (roadmap in userRoadmaps) {

                val roadmapId =
                    roadmap["roadmap_id"]
                        ?.jsonPrimitive
                        ?.content
                        ?: continue

                val roadmapData =
                    roadmap["roadmap_data"]
                        ?: continue

                val roadmapObject =
                    parseRoadmapData(roadmapData)

                val totalLessons =
                    countLessons(roadmapObject)

                val progress =
                    ProgressRepository.getProgress(
                        roadmapId = roadmapId,
                        userId = user.id
                    )

                val completedLessons =
                    progress.count {
                        it.is_completed
                    }

                progressMap[roadmapId] =
                    Pair(
                        completedLessons,
                        totalLessons
                    )
            }

            roadmapProgress = progressMap

            isLoading = false

        } catch (e: Exception) {

            errorMessage =
                e.message
                    ?: "Could not load your learning roadmaps."

            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillLinkBackground)
            .padding(
                start = 22.dp,
                top = 26.dp,
                end = 22.dp,
                bottom = 20.dp
            )
    ) {

        // ---------------------------------------------------------
        // HEADER
        // ---------------------------------------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            SkillLinkLogo()

            Text(
                text = "My Learning",
                style = MaterialTheme.typography.titleLarge,
                color = SkillLinkNavy,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // ---------------------------------------------------------
        // FILTER CHIPS
        // ---------------------------------------------------------

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SkillLinkPurple
            ) {

                Text(
                    text = "In Progress",
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = androidx.compose.ui.graphics.Color.White
            ) {

                Text(
                    text = "Completed",
                    color = androidx.compose.ui.graphics.Color(0xFF838095),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    )
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // ---------------------------------------------------------
        // SECTION TITLE
        // ---------------------------------------------------------

        Text(
            text = "In progress",
            style = MaterialTheme.typography.titleMedium,
            color = SkillLinkNavy
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // ---------------------------------------------------------
        // CONTENT
        // ---------------------------------------------------------

        if (isLoading) {

            Text(
                text = "Loading your roadmaps...",
                color = SkillLinkNavy
            )

        } else if (errorMessage.isNotBlank()) {

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )

        } else if (roadmaps.isEmpty()) {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                color = androidx.compose.ui.graphics.Color.White
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "No roadmaps yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = SkillLinkNavy
                    )

                    Text(
                        text = "Generate a personalised roadmap to start learning.",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color(0xFF85849A),
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Button(
                        onClick = onGenerateRoadmap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    ) {
                        Text("Generate Roadmap")
                    }
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {

                items(
                    items = roadmaps
                ) { roadmap ->

                    val roadmapId =
                        roadmap["roadmap_id"]
                            ?.jsonPrimitive
                            ?.content
                            ?: ""

                    val targetSkill =
                        roadmap["target_skill"]
                            ?.jsonPrimitive
                            ?.content
                            ?: "Learning Roadmap"

                    val roadmapData =
                        roadmap["roadmap_data"]

                    val roadmapObject =
                        roadmapData?.let {
                            parseRoadmapData(it)
                        }

                    val title =
                        roadmapObject
                            ?.get("title")
                            ?.jsonPrimitive
                            ?.content
                            ?: "$targetSkill Roadmap"

                    val progress =
                        roadmapProgress[roadmapId]

                    val completed =
                        progress?.first ?: 0

                    val total =
                        progress?.second ?: 0

                    val percentage =
                        if (total > 0) {
                            completed.toFloat() /
                                    total.toFloat()
                        } else {
                            0f
                        }

                    val percentageText =
                        "${(percentage * 100).toInt()}%"

                    val lastAccessed =
                        roadmap["last_accessed"]
                            ?.jsonPrimitive
                            ?.content
                            ?: ""

                    RoadmapLearningRow(
                        title = title,
                        completed = completed,
                        total = total,
                        percentage = percentage,
                        percentageText = percentageText,
                        lastAccessed = lastAccessed,
                        onClick = {

                            val currentUser =
                                supabase.auth.currentUserOrNull()

                            if (currentUser != null) {

                                scope.launch {

                                    try {

                                        ProgressRepository.updateLastAccessed(
                                            roadmapId = roadmapId,
                                            userId = currentUser.id
                                        )

                                    } catch (_: Exception) {
                                        // Do not prevent opening the roadmap.
                                    }
                                }
                            }

                            onOpenRoadmap(
                                roadmapId,
                                roadmapData?.toString() ?: ""
                            )
                        }
                    )
                }
            }
        }
    }
}


// =============================================================
// ROADMAP ROW
// =============================================================

@Composable
private fun RoadmapLearningRow(
    title: String,
    completed: Int,
    total: Int,
    percentage: Float,
    percentageText: String,
    lastAccessed: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(15.dp),
        color = androidx.compose.ui.graphics.Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.ui.graphics.Color(0xFFECEBF3)
        )
    )  {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 12.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // -------------------------------------------------
                    // ICON
                    // -------------------------------------------------

                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(11.dp),
                        color = SkillLinkLightPurple
                    ) {

                        androidx.compose.foundation.layout.Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "⌘",
                                color = SkillLinkPurple,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.size(12.dp)
                    )

                    // -------------------------------------------------
                    // ROADMAP INFORMATION
                    // -------------------------------------------------

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SkillLinkNavy,
                            maxLines = 1
                        )

                        Spacer(
                            modifier = Modifier.height(7.dp)
                        )

                        // Progress bar
                        LinearProgressIndicator(
                            progress = {
                                percentage
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = SkillLinkPurple,
                            trackColor = androidx.compose.ui.graphics.Color(0xFFECEAF2)
                        )

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text = buildProgressText(
                                percentageText,
                                completed,
                                total,
                                lastAccessed
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color(0xFF9A98A8),
                            maxLines = 1
                        )
                    }

                    Spacer(
                        modifier = Modifier.size(8.dp)
                    )

                    // -------------------------------------------------
                    // CHEVRON
                    // -------------------------------------------------

                    Text(
                        text = "›",
                        style = MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color(0xFFB3B0BF)
                    )
                }
            }
        }



// =============================================================
// LOGO
// =============================================================

@Composable
private fun SkillLinkLogo() {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier
                    .size(
                        width = 5.dp,
                        height = 15.dp
                    ),
                shape = RoundedCornerShape(4.dp),
                color = SkillLinkPurple
            ) {}

            Surface(
                modifier = Modifier
                    .size(
                        width = 5.dp,
                        height = 19.dp
                    ),
                shape = RoundedCornerShape(4.dp),
                color = androidx.compose.ui.graphics.Color(0xFFA18AEF)
            ) {}

            Surface(
                modifier = Modifier
                    .size(
                        width = 5.dp,
                        height = 15.dp
                    ),
                shape = RoundedCornerShape(4.dp),
                color = androidx.compose.ui.graphics.Color(0xFFD0C4FF)
            ) {}
        }

        Text(
            text = "SkillLink",
            style = MaterialTheme.typography.titleMedium,
            color = SkillLinkNavy,
            modifier = Modifier.padding(start = 7.dp)
        )
    }
}


// =============================================================
// JSON HELPERS
// =============================================================

private fun parseRoadmapData(
    element: JsonElement
): JsonObject {

    return when (element) {

        is JsonObject -> {
            element
        }

        is JsonPrimitive -> {

            if (!element.isString) {
                throw Exception(
                    "roadmap_data is not a JSON object."
                )
            }

            parseJsonObjectSafely(
                element.content
            )
        }

        else -> {

            throw Exception(
                "Unexpected roadmap_data format."
            )
        }
    }
}


private fun parseJsonObjectSafely(
    value: String
): JsonObject {

    var parsed: JsonElement =
        Json.parseToJsonElement(value)

    // Handles the case where Supabase returns
    // JSON stored inside another JSON string.
    repeat(2) {

        if (parsed is JsonPrimitive &&
            parsed.isString
        ) {

            parsed =
                Json.parseToJsonElement(
                    parsed.content
                )
        }
    }

    return parsed as? JsonObject
        ?: throw Exception(
            "roadmap_data could not be converted to a JSON object."
        )
}


private fun countLessons(
    roadmap: JsonObject
): Int {

    val weeks =
        roadmap["weeks"]
                as? JsonArray
            ?: return 0

    return weeks.sumOf { weekElement ->

        val week =
            weekElement as? JsonObject
                ?: return@sumOf 0

        val lessons =
            week["lessons"]
                    as? JsonArray
                ?: return@sumOf 0

        lessons.size
    }
}


// =============================================================
// PROGRESS TEXT
// =============================================================

private fun buildProgressText(
    percentage: String,
    completed: Int,
    total: Int,
    lastAccessed: String
): String {

    val topicsText =
        "$completed of $total topics"

    val accessedText =
        if (lastAccessed.isBlank()) {
            "Not accessed yet"
        } else {
            formatLastAccessed(lastAccessed)
        }

    return "$percentage · $topicsText · $accessedText"
}


// =============================================================
// LAST ACCESSED
// =============================================================

private fun formatLastAccessed(
    timestamp: String
): String {

    return try {

        val instant =
            java.time.Instant.parse(timestamp)

        val formatter =
            java.time.format.DateTimeFormatter
                .ofPattern("dd MMM yyyy, HH:mm")
                .withZone(
                    java.time.ZoneId.systemDefault()
                )

        "Last accessed ${formatter.format(instant)}"

    } catch (_: Exception) {

        "Last accessed $timestamp"
    }
}