package com.example.skilllinkapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.data.ProgressRecord
import com.example.skilllinkapp.data.ProgressRepository
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.ui.theme.SkillLinkNavy
import com.example.skilllinkapp.ui.theme.SkillLinkPurple
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val PageBackground = Color(0xFFF7F5FC)
private val White = Color.White
private val TextSecondary = Color(0xFF77737D)
private val BorderColor = Color(0xFFE7E2EF)
private val SoftPurple = Color(0xFFF0EAFF)
private val SoftGreen = Color(0xFFEAF8F0)
private val Green = Color(0xFF25855A)

@Serializable
data class RoadmapWeek(
    val title: String,
    val description: String,
    val lessons: List<String>
)

@Serializable
data class GeneratedRoadmap(
    val title: String,
    val summary: String,
    val weeks: List<RoadmapWeek>
)

@Composable
fun RoadmapScreen(
    roadmapJson: String = "",
    roadmapId: String = "",
    onBack: () -> Unit
) {
    val roadmap = remember(roadmapJson) {
        try {
            Json {
                ignoreUnknownKeys = true
            }.decodeFromString<GeneratedRoadmap>(roadmapJson)
        } catch (e: Exception) {
            null
        }
    }

    var progressRecords by remember {
        mutableStateOf<List<ProgressRecord>>(emptyList())
    }

    var isLoadingProgress by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(roadmapId) {
        if (roadmapId.isBlank()) {
            isLoadingProgress = false
            return@LaunchedEffect
        }

        try {
            val user = supabase.auth.currentUserOrNull()

            if (user != null) {
                progressRecords = ProgressRepository.getProgress(
                    roadmapId = roadmapId,
                    userId = user.id
                )
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Could not load progress."
        } finally {
            isLoadingProgress = false
        }
    }

    val totalLessons = roadmap?.weeks?.sumOf { it.lessons.size } ?: 0

    val completedLessons = roadmap?.weeks?.flatMapIndexed { weekIndex, week ->
        week.lessons.mapIndexedNotNull { lessonIndex, _ ->
            val stepOrder = (weekIndex * 3) + lessonIndex + 1

            progressRecords.firstOrNull {
                it.step_order == stepOrder && it.is_completed
            }
        }
    }?.size ?: 0

    val progressPercentage =
        if (totalLessons > 0) {
            (completedLessons.toFloat() / totalLessons.toFloat())
        } else {
            0f
        }

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
                    Text(
                        text = "‹",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SkillLinkNavy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Learning roadmap",
                    style = MaterialTheme.typography.titleLarge,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your personalised learning journey",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        if (roadmap == null) {

            // -----------------------------------------------------
            // EMPTY / INVALID STATE
            // -----------------------------------------------------

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(SoftPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SkillLinkPurple,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "No roadmap available",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your roadmap will appear here after it has been generated.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

        } else {

            // -----------------------------------------------------
            // HERO
            // -----------------------------------------------------

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

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Your roadmap",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f)
                        )

                        Text(
                            text = roadmap.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = roadmap.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -----------------------------------------------------
            // PROGRESS CARD
            // -----------------------------------------------------

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Your progress",
                                style = MaterialTheme.typography.titleMedium,
                                color = SkillLinkNavy,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (completedLessons == 0) {
                                    "Ready to get started?"
                                } else if (completedLessons == totalLessons) {
                                    "Roadmap completed!"
                                } else {
                                    "$completedLessons of $totalLessons lessons completed"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    if (completedLessons == totalLessons &&
                                        totalLessons > 0
                                    ) {
                                        SoftGreen
                                    } else {
                                        SoftPurple
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(progressPercentage * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (
                                    completedLessons == totalLessons &&
                                    totalLessons > 0
                                ) {
                                    Green
                                } else {
                                    SkillLinkPurple
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SoftPurple)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressPercentage)
                                .height(9.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SkillLinkPurple)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // -----------------------------------------------------
            // PLAN HEADER
            // -----------------------------------------------------

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Your learning plan",
                        style = MaterialTheme.typography.titleLarge,
                        color = SkillLinkNavy,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${roadmap.weeks.size} weeks • $totalLessons lessons",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (isLoadingProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SkillLinkPurple,
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // -----------------------------------------------------
            // WEEKS
            // -----------------------------------------------------

            roadmap.weeks.forEachIndexed { weekIndex, week ->

                val weekStartOrder = weekIndex * 3 + 1

                val completedWeekLessons =
                    week.lessons.mapIndexed { lessonIndex, _ ->
                        val stepOrder =
                            weekStartOrder + lessonIndex

                        progressRecords.any {
                            it.step_order == stepOrder &&
                                    it.is_completed
                        }
                    }.count { it }

                val weekCompleted =
                    week.lessons.isNotEmpty() &&
                            completedWeekLessons == week.lessons.size

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 7.dp
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = White,
                    shadowElevation = 1.dp
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        // WEEK HEADER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (weekCompleted) {
                                            SoftGreen
                                        } else {
                                            SoftPurple
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${weekIndex + 1}",
                                    color = if (weekCompleted) {
                                        Green
                                    } else {
                                        SkillLinkPurple
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Week ${weekIndex + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (weekCompleted) {
                                        Green
                                    } else {
                                        SkillLinkPurple
                                    },
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = week.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SkillLinkNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (weekCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(SoftGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = Green,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = week.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // LESSONS
                        week.lessons.forEachIndexed { lessonIndex, lesson ->

                            val stepOrder =
                                (weekIndex * 3) + lessonIndex + 1

                            val completed =
                                progressRecords.any {
                                    it.step_order == stepOrder &&
                                            it.is_completed
                                }

                            LessonRow(
                                lessonNumber = lessonIndex + 1,
                                lesson = lesson,
                                completed = completed,
                                onCheckedChange = { checked ->

                                    val user =
                                        supabase.auth.currentUserOrNull()

                                    if (user == null) {
                                        errorMessage =
                                            "You must be logged in."
                                        return@LessonRow
                                    }

                                    scope.launch {

                                        try {

                                            errorMessage = ""

                                            if (roadmapId.isNotBlank()) {

                                                ProgressRepository.saveProgress(
                                                    roadmapId = roadmapId,
                                                    userId = user.id,
                                                    stepTitle = lesson,
                                                    stepDescription = week.description,
                                                    stepOrder = stepOrder,
                                                    isCompleted = checked
                                                )

                                                progressRecords =
                                                    ProgressRepository.getProgress(
                                                        roadmapId = roadmapId,
                                                        userId = user.id
                                                    )

                                            } else {

                                                val updatedRecord =
                                                    ProgressRecord(
                                                        progress_id = null,
                                                        roadmap_id = "",
                                                        user_id = user.id,
                                                        step_title = lesson,
                                                        step_description = week.description,
                                                        step_order = stepOrder,
                                                        is_completed = checked,
                                                        completed_at = null
                                                    )

                                                progressRecords =
                                                    progressRecords
                                                        .filterNot {
                                                            it.step_order == stepOrder
                                                        }
                                                        .let { current ->
                                                            if (checked) {
                                                                current + updatedRecord
                                                            } else {
                                                                current
                                                            }
                                                        }
                                            }

                                        } catch (e: Exception) {

                                            errorMessage =
                                                e.message
                                                    ?: "Could not save progress."
                                        }
                                    }
                                }
                            )

                            if (lessonIndex < week.lessons.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------
            // ERROR
            // -----------------------------------------------------

            if (errorMessage.isNotEmpty()) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 12.dp
                        ),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFF0F0)
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---------------------------------------------------------
        // BACK BUTTON
        // ---------------------------------------------------------

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Back to My Learning",
                color = SkillLinkNavy,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LessonRow(
    lessonNumber: Int,
    lesson: String,
    completed: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = {
            onCheckedChange(!completed)
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (completed) {
                    Color(0xFFD7EBDD)
                } else {
                    BorderColor
                },
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (completed) {
            SoftGreen
        } else {
            Color.White
        }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (completed) {
                            Green
                        } else {
                            SoftPurple
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (completed) {
                        "✓"
                    } else {
                        lessonNumber.toString()
                    },
                    color = if (completed) {
                        Color.White
                    } else {
                        SkillLinkPurple
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = lesson,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = if (completed) {
                    Green
                } else {
                    SkillLinkNavy
                },
                fontWeight = if (completed) {
                    FontWeight.Medium
                } else {
                    FontWeight.Normal
                }
            )
        }
    }
}