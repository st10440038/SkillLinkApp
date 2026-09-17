package com.example.skilllinkapp.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.data.ProgressRepository
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.ui.theme.SkillLinkBackground
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
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun MyLearningScreen(
    refreshKey: Int = 0,
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

    var showCompleted by remember {
        mutableStateOf(false)
    }

    var roadmapToDelete by remember {
        mutableStateOf<JsonObject?>(null)
    }

    val scope = rememberCoroutineScope()

    /*
     * Load roadmaps whenever refreshKey changes.
     */
    LaunchedEffect(refreshKey) {

        val user = supabase.auth.currentUserOrNull()

        if (user == null) {
            isLoading = false
            errorMessage = "You must be logged in."
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = ""

        try {

            /*
             * Get all roadmaps belonging to the
             * currently logged-in user.
             */
            val userRoadmaps =
                supabase
                    .from("roadmaps")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<JsonObject>()

            /*
             * Keep roadmaps with valid roadmap data.
             *
             * parseRoadmapData() supports:
             *
             * 1. Normal roadmap JSON
             *
             * 2. {
             *      "generated_roadmap": {...}
             *    }
             *
             * 3. Double-encoded JSON
             */
            roadmaps =
                userRoadmaps
                    .filter { roadmap ->

                        val roadmapData =
                            roadmap["roadmap_data"]
                                ?: return@filter false

                        try {

                            val roadmapObject =
                                parseRoadmapData(
                                    roadmapData
                                )

                            val title =
                                roadmapObject["title"]
                                    ?.jsonPrimitive
                                    ?.content
                                    ?.trim()
                                    ?: ""

                            isProperRoadmapTitle(title)

                        } catch (_: Exception) {

                            false
                        }
                    }

                    /*
                     * IMPORTANT:
                     *
                     * Sort ONLY by created_at.
                     *
                     * This means:
                     *
                     * newest roadmap
                     *        ↓
                     * older roadmap
                     *        ↓
                     * oldest roadmap
                     *
                     * last_accessed does NOT affect the order.
                     */
                    .sortedByDescending { roadmap ->

                        roadmap["created_at"]
                            ?.jsonPrimitive
                            ?.content
                            ?: ""
                    }

            /*
             * Load progress for each roadmap.
             */
            val progressMap =
                mutableMapOf<String, Pair<Int, Int>>()

            for (roadmap in roadmaps) {

                val roadmapId =
                    roadmap["roadmap_id"]
                        ?.jsonPrimitive
                        ?.content
                        ?: continue

                val roadmapData =
                    roadmap["roadmap_data"]
                        ?: continue

                val roadmapObject =
                    parseRoadmapData(
                        roadmapData
                    )

                val totalLessons =
                    countLessons(
                        roadmapObject
                    )

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

    /*
     * Filter roadmaps into:
     *
     * In Progress
     * OR
     * Completed
     */
    val displayedRoadmaps =
        roadmaps.filter { roadmap ->

            val roadmapId =
                roadmap["roadmap_id"]
                    ?.jsonPrimitive
                    ?.content
                    ?: ""

            val progress =
                roadmapProgress[roadmapId]

            val completed =
                progress?.first ?: 0

            val total =
                progress?.second ?: 0

            val completedRoadmap =
                total > 0 &&
                        completed >= total

            if (showCompleted) {
                completedRoadmap
            } else {
                !completedRoadmap
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                SkillLinkBackground
            )
            .padding(
                start = 22.dp,
                top = 26.dp,
                end = 22.dp,
                bottom = 20.dp
            )
    ) {

        /*
         * Header
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            SkillLinkLogo()

            Text(
                text = "My Learning",
                style =
                    MaterialTheme.typography.titleLarge,
                color = SkillLinkNavy,
                modifier =
                    Modifier.padding(start = 12.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        /*
         * Filter buttons
         */
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Surface(
                modifier =
                    Modifier.clickable {
                        showCompleted = false
                    },
                shape =
                    RoundedCornerShape(20.dp),
                color =
                    if (!showCompleted) {
                        SkillLinkPurple
                    } else {
                        Color.White
                    }
            ) {

                Text(
                    text = "In Progress",
                    color =
                        if (!showCompleted) {
                            Color.White
                        } else {
                            Color(0xFF838095)
                        },
                    style =
                        MaterialTheme.typography.bodySmall,
                    modifier =
                        Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                )
            }

            Surface(
                modifier =
                    Modifier.clickable {
                        showCompleted = true
                    },
                shape =
                    RoundedCornerShape(20.dp),
                color =
                    if (showCompleted) {
                        SkillLinkPurple
                    } else {
                        Color.White
                    }
            ) {

                Text(
                    text = "Completed",
                    color =
                        if (showCompleted) {
                            Color.White
                        } else {
                            Color(0xFF838095)
                        },
                    style =
                        MaterialTheme.typography.bodySmall,
                    modifier =
                        Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text =
                if (showCompleted) {
                    "Completed"
                } else {
                    "In progress"
                },
            style =
                MaterialTheme.typography.titleMedium,
            color = SkillLinkNavy
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        /*
         * Loading state
         */
        if (isLoading) {

            Text(
                text = "Loading your roadmaps...",
                color = SkillLinkNavy
            )

            /*
             * Error state
             */
        } else if (errorMessage.isNotBlank()) {

            Text(
                text = errorMessage,
                color =
                    MaterialTheme.colorScheme.error,
                style =
                    MaterialTheme.typography.bodySmall
            )

            /*
             * Empty state
             */
        } else if (displayedRoadmaps.isEmpty()) {

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(15.dp),
                color = Color.White,
                border =
                    BorderStroke(
                        1.dp,
                        Color(0xFFECEBF3)
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text =
                            if (showCompleted) {
                                "No completed roadmaps"
                            } else {
                                "No roadmaps yet"
                            },
                        style =
                            MaterialTheme.typography.titleMedium,
                        color = SkillLinkNavy
                    )

                    if (!showCompleted) {

                        Text(
                            text =
                                "Generate a personalised roadmap to start learning.",
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                Color(0xFF85849A),
                            modifier =
                                Modifier.padding(top = 6.dp)
                        )

                        Button(
                            onClick =
                                onGenerateRoadmap,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                        ) {

                            Text(
                                text = "Generate Roadmap"
                            )
                        }
                    }
                }
            }

            /*
             * Roadmap list
             */
        } else {

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                verticalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {

                items(
                    items = displayedRoadmaps
                ) { roadmap ->

                    val roadmapId =
                        roadmap["roadmap_id"]
                            ?.jsonPrimitive
                            ?.content
                            ?: return@items

                    val roadmapData =
                        roadmap["roadmap_data"]
                            ?: return@items

                    val roadmapObject =
                        try {

                            parseRoadmapData(
                                roadmapData
                            )

                        } catch (_: Exception) {

                            null
                        }
                            ?: return@items

                    val title =
                        roadmapObject["title"]
                            ?.jsonPrimitive
                            ?.content
                            ?.trim()
                            ?: return@items

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
                        percentageText =
                            percentageText,
                        lastAccessed =
                            lastAccessed,

                        /*
                         * Open roadmap
                         */
                        onClick = {

                            val currentUser =
                                supabase.auth
                                    .currentUserOrNull()

                            if (currentUser != null) {

                                scope.launch {

                                    try {

                                        ProgressRepository
                                            .updateLastAccessed(
                                                roadmapId =
                                                    roadmapId,
                                                userId =
                                                    currentUser.id
                                            )

                                    } catch (_: Exception) {
                                        // Do not prevent opening
                                    }
                                }
                            }

                            /*
                             * Open roadmap using
                             * the normalised JSON.
                             */
                            onOpenRoadmap(
                                roadmapId,
                                roadmapObject.toString()
                            )
                        },

                        /*
                         * Delete roadmap
                         */
                        onDelete = {
                            roadmapToDelete =
                                roadmap
                        }
                    )
                }
            }
        }
    }

    /*
     * Delete confirmation dialog
     */
    if (roadmapToDelete != null) {

        val selectedRoadmap =
            roadmapToDelete

        val selectedTitle =
            try {

                selectedRoadmap
                    ?.get("roadmap_data")
                    ?.let {
                        parseRoadmapData(it)
                    }
                    ?.get("title")
                    ?.jsonPrimitive
                    ?.content
                    ?: "this roadmap"

            } catch (_: Exception) {

                "this roadmap"
            }

        AlertDialog(
            onDismissRequest = {
                roadmapToDelete = null
            },

            title = {
                Text(
                    text = "Delete roadmap?"
                )
            },

            text = {
                Text(
                    text =
                        "Are you sure you want to delete " +
                                "\"$selectedTitle\"? " +
                                "This will also remove its learning progress."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val roadmap =
                            roadmapToDelete

                        val currentUser =
                            supabase.auth
                                .currentUserOrNull()

                        val roadmapId =
                            roadmap
                                ?.get("roadmap_id")
                                ?.jsonPrimitive
                                ?.content

                        if (
                            currentUser != null &&
                            roadmapId != null
                        ) {

                            scope.launch {

                                try {

                                    /*
                                     * Delete roadmap from Supabase.
                                     */
                                    supabase
                                        .from("roadmaps")
                                        .delete {

                                            filter {

                                                eq(
                                                    "roadmap_id",
                                                    roadmapId
                                                )

                                                eq(
                                                    "user_id",
                                                    currentUser.id
                                                )
                                            }
                                        }

                                    /*
                                     * Remove it from the
                                     * local list immediately.
                                     */
                                    roadmaps =
                                        roadmaps.filterNot {

                                            it["roadmap_id"]
                                                ?.jsonPrimitive
                                                ?.content ==
                                                    roadmapId
                                        }

                                    /*
                                     * Remove its progress
                                     * from the local map.
                                     */
                                    roadmapProgress =
                                        roadmapProgress
                                            .toMutableMap()
                                            .apply {
                                                remove(
                                                    roadmapId
                                                )
                                            }

                                    roadmapToDelete =
                                        null

                                } catch (e: Exception) {

                                    errorMessage =
                                        e.message
                                            ?: "Could not delete roadmap."

                                    roadmapToDelete =
                                        null
                                }
                            }
                        }
                    }
                ) {

                    Text(
                        text = "Delete",
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        roadmapToDelete = null
                    }
                ) {

                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }
}


/*
 * Individual roadmap row.
 */
@Composable
private fun RoadmapLearningRow(
    title: String,
    completed: Int,
    total: Int,
    percentage: Float,
    percentageText: String,
    lastAccessed: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),
        shape =
            RoundedCornerShape(15.dp),
        color = Color.White,
        border =
            BorderStroke(
                1.dp,
                Color(0xFFECEBF3)
            )
    ) {

        Row(
            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            /*
             * Roadmap icon.
             */
            Surface(
                modifier =
                    Modifier.size(36.dp),
                shape =
                    RoundedCornerShape(11.dp),
                color =
                    SkillLinkLightPurple
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "⌘",
                        color =
                            SkillLinkPurple,
                        style =
                            MaterialTheme.typography
                                .titleMedium
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.size(12.dp)
            )

            /*
             * Roadmap information.
             */
            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color = SkillLinkNavy,
                    maxLines = 1
                )

                Spacer(
                    modifier =
                        Modifier.height(7.dp)
                )

                /*
                 * Progress bar.
                 */
                LinearProgressIndicator(
                    progress = {
                        percentage
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                    color =
                        SkillLinkPurple,
                    trackColor =
                        Color(0xFFECEAF2)
                )

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                /*
                 * Percentage,
                 * completed topics,
                 * last accessed.
                 */
                Text(
                    text =
                        buildProgressText(
                            percentageText =
                                percentageText,
                            completed =
                                completed,
                            total =
                                total,
                            lastAccessed =
                                lastAccessed
                        ),
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color =
                        Color(0xFF9A98A8),
                    maxLines = 1
                )
            }

            Spacer(
                modifier =
                    Modifier.size(4.dp)
            )

            /*
             * Delete button.
             */
            TextButton(
                onClick = onDelete,
                modifier =
                    Modifier.size(40.dp)
            ) {

                Text(
                    text = "⋮",
                    style =
                        MaterialTheme.typography
                            .headlineSmall,
                    color =
                        Color(0xFF9A98A8)
                )
            }
        }
    }
}


/*
 * SkillLink logo.
 */
@Composable
private fun SkillLinkLogo() {

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(2.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                modifier =
                    Modifier.size(
                        width = 5.dp,
                        height = 15.dp
                    ),
                shape =
                    RoundedCornerShape(4.dp),
                color =
                    SkillLinkPurple
            ) {}

            Surface(
                modifier =
                    Modifier.size(
                        width = 5.dp,
                        height = 19.dp
                    ),
                shape =
                    RoundedCornerShape(4.dp),
                color =
                    Color(0xFFA18AEF)
            ) {}

            Surface(
                modifier =
                    Modifier.size(
                        width = 5.dp,
                        height = 15.dp
                    ),
                shape =
                    RoundedCornerShape(4.dp),
                color =
                    Color(0xFFD0C4FF)
            ) {}
        }

        Text(
            text = "SkillLink",
            style =
                MaterialTheme.typography.titleMedium,
            color = SkillLinkNavy,
            modifier =
                Modifier.padding(start = 7.dp)
        )
    }
}


/*
 * Convert roadmap_data into the normal roadmap object.
 *
 * Supports:
 *
 * {
 *     "title": "...",
 *     "summary": "...",
 *     "weeks": [...]
 * }
 *
 * AND:
 *
 * {
 *     "generated_roadmap": {
 *         "title": "...",
 *         "summary": "...",
 *         "weeks": [...]
 *     }
 * }
 *
 * AND JSON stored as a string.
 */
private fun parseRoadmapData(
    element: JsonElement
): JsonObject {

    var current =
        element

    /*
     * Unwrap JSON strings.
     */
    repeat(4) {

        if (
            current is JsonPrimitive &&
            current.isString
        ) {

            current =
                Json.parseToJsonElement(
                    current.content
                )
        }
    }

    /*
     * The result must be an object.
     */
    val objectData =
        current as? JsonObject
            ?: throw Exception(
                "roadmap_data could not be converted into a JSON object."
            )

    /*
     * Handle:
     *
     * "generated_roadmap": {...}
     */
    val generatedRoadmap =
        objectData["generated_roadmap"]

    if (generatedRoadmap != null) {

        var inner =
            generatedRoadmap

        /*
         * generated_roadmap might itself
         * be a JSON string.
         */
        repeat(4) {

            if (
                inner is JsonPrimitive &&
                inner.isString
            ) {

                inner =
                    Json.parseToJsonElement(
                        inner.content
                    )
            }
        }

        return inner as? JsonObject
            ?: throw Exception(
                "generated_roadmap could not be converted into a JSON object."
            )
    }

    /*
     * Already in normal format.
     */
    return objectData
}


/*
 * Count lessons across all weeks.
 */
private fun countLessons(
    roadmap: JsonObject
): Int {

    val weeks =
        roadmap["weeks"] as? JsonArray
            ?: return 0

    return weeks.sumOf { weekElement ->

        val week =
            weekElement as? JsonObject
                ?: return@sumOf 0

        val lessons =
            week["lessons"] as? JsonArray
                ?: return@sumOf 0

        lessons.size
    }
}


/*
 * Remove generic roadmap titles.
 */
private fun isProperRoadmapTitle(
    title: String
): Boolean {

    val cleaned =
        title
            .trim()
            .lowercase()

    if (cleaned.isBlank()) {
        return false
    }

    val invalidTitles =
        setOf(
            "roadmap",
            "learning roadmap",
            "your learning roadmap",
            "my roadmap",
            "personalised roadmap",
            "personalized roadmap"
        )

    return cleaned !in invalidTitles
}


/*
 * Build the progress information.
 */
private fun buildProgressText(
    percentageText: String,
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
            formatLastAccessed(
                lastAccessed
            )
        }

    return "$percentageText · $topicsText · $accessedText"
}


/*
 * Format the Supabase timestamp.
 */
private fun formatLastAccessed(
    timestamp: String
): String {

    return try {

        val instant =
            java.time.Instant.parse(
                timestamp
            )

        val zone =
            java.time.ZoneId.systemDefault()

        val localDate =
            instant
                .atZone(zone)
                .toLocalDate()

        val today =
            java.time.LocalDate.now()

        val yesterday =
            today.minusDays(1)

        when (localDate) {

            today ->
                "Last accessed today"

            yesterday ->
                "Last accessed yesterday"

            else -> {

                val formatter =
                    java.time.format.DateTimeFormatter
                        .ofPattern(
                            "dd MMM yyyy, HH:mm"
                        )

                "Last accessed ${
                    formatter.format(
                        instant.atZone(zone)
                    )
                }"
            }
        }

    } catch (_: Exception) {

        "Last accessed $timestamp"
    }
}