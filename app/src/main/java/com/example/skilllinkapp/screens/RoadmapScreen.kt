package com.example.skilllinkapp.screens


import com.example.skilllinkapp.network.GeminiService
import androidx.compose.ui.platform.LocalContext
import com.example.skilllinkapp.data.RoadmapTranslationCache
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
import com.example.skilllinkapp.LanguageSupport
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


    val context = LocalContext.current
    val selectedLanguage by LanguageSupport.currentLanguage

    var displayedRoadmapJson by remember(roadmapId) {
        mutableStateOf(roadmapJson)
    }

    var isTranslating by remember {
        mutableStateOf(false)
    }

    var translationError by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(roadmapId, roadmapJson, selectedLanguage) {
        displayedRoadmapJson = roadmapJson
        translationError = null

        if (roadmapJson.isBlank()) {
            return@LaunchedEffect
        }

        val languageName = when (selectedLanguage) {
            LanguageSupport.ZULU -> "isiZulu"
            LanguageSupport.AFRIKAANS -> "Afrikaans"
            else -> "English"
        }

        // Reuse a translation saved on this device if available.
        val cachedTranslation = if (roadmapId.isNotBlank()) {
            RoadmapTranslationCache.get(
                context = context,
                roadmapId = roadmapId,
                language = selectedLanguage
            )
        } else {
            null
        }

        if (cachedTranslation != null) {
            displayedRoadmapJson = cachedTranslation
            return@LaunchedEffect
        }

        // New roadmaps are generated in the selected language.
        // For existing roadmaps, request translation if needed.
        isTranslating = true

        try {
            val translatedJson = GeminiService.translateRoadmap(
                roadmapJson = roadmapJson,
                targetLanguage = languageName
            )

            // Verify that the returned JSON can be displayed.
            Json {
                ignoreUnknownKeys = true
            }.decodeFromString<GeneratedRoadmap>(translatedJson)

            displayedRoadmapJson = translatedJson

            if (roadmapId.isNotBlank()) {
                RoadmapTranslationCache.save(
                    context = context,
                    roadmapId = roadmapId,
                    language = selectedLanguage,
                    translatedJson = translatedJson
                )
            }
        } catch (e: Exception) {
            translationError = e.message
                ?: "Unable to translate this roadmap."

            displayedRoadmapJson = roadmapJson
        } finally {
            isTranslating = false
        }
    }

    val roadmap = remember(displayedRoadmapJson) {
        try {
            Json {
                ignoreUnknownKeys = true
            }.decodeFromString<GeneratedRoadmap>(displayedRoadmapJson)
        } catch (e: Exception) {
            null
        }
    }

    val originalRoadmap = remember(roadmapJson) {
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
            errorMessage = e.message
                ?: roadmapScreenText("Could not load progress.")
        } finally {
            isLoadingProgress = false
        }
    }

    val totalLessons = roadmap?.weeks?.sumOf {
        it.lessons.size
    } ?: 0

    val completedLessons = roadmap?.weeks?.flatMapIndexed {
            weekIndex, week ->
        week.lessons.mapIndexedNotNull { lessonIndex, _ ->
            val stepOrder = (weekIndex * 3) + lessonIndex + 1

            progressRecords.firstOrNull {
                it.step_order == stepOrder && it.is_completed
            }
        }
    }?.size ?: 0

    val progressPercentage =
        if (totalLessons > 0) {
            completedLessons.toFloat() / totalLessons.toFloat()
        } else {
            0f
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
    ) {

        if (isTranslating) {
            CircularProgressIndicator()
        }

        translationError?.let { error ->
            Text(
                text = "Translation failed: $error",
                color = MaterialTheme.colorScheme.error
            )
        }

        // TOP BAR

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
                    text = roadmapScreenText("Learning roadmap"),
                    style = MaterialTheme.typography.titleLarge,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = roadmapScreenText(
                        "Your personalised learning journey"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (isTranslating) {
                CircularProgressIndicator()
            }

            translationError?.let { error ->
                Text(
                    text = "Translation failed: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (roadmap == null) {
            // EMPTY / INVALID STATE

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
                    text = roadmapScreenText("No roadmap available"),
                    style = MaterialTheme.typography.headlineSmall,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = roadmapScreenText(
                        "Your roadmap will appear here after it has been generated."
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            // HERO

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
                            text = roadmapScreenText("Your roadmap"),
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

            // PROGRESS CARD

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
                                text = roadmapScreenText("Your progress"),
                                style = MaterialTheme.typography.titleMedium,
                                color = SkillLinkNavy,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val progressMessage = when {
                                completedLessons == 0 ->
                                    roadmapScreenText(
                                        "Ready to get started?"
                                    )

                                totalLessons > 0 &&
                                        completedLessons == totalLessons ->
                                    roadmapScreenText(
                                        "Roadmap completed!"
                                    )

                                else -> roadmapScreenText(
                                    "lessons_progress"
                                ).replace(
                                    "{completed}",
                                    completedLessons.toString()
                                ).replace(
                                    "{total}",
                                    totalLessons.toString()
                                )
                            }

                            Text(
                                text = progressMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    if (
                                        completedLessons == totalLessons &&
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

            // PLAN HEADER

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
                        text = roadmapScreenText("Your learning plan"),
                        style = MaterialTheme.typography.titleLarge,
                        color = SkillLinkNavy,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = roadmapScreenText("plan_count")
                            .replace(
                                "{weeks}",
                                roadmap.weeks.size.toString()
                            )
                            .replace(
                                "{lessons}",
                                totalLessons.toString()
                            ),
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

            // WEEKS

            roadmap.weeks.forEachIndexed { weekIndex, week ->
                val weekStartOrder = weekIndex * 3 + 1

                val completedWeekLessons =
                    week.lessons.mapIndexed { lessonIndex, _ ->
                        val stepOrder = weekStartOrder + lessonIndex

                        progressRecords.any {
                            it.step_order == stepOrder && it.is_completed
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
                                    text = roadmapScreenText("week_label")
                                        .replace(
                                            "{week}",
                                            (weekIndex + 1).toString()
                                        ),
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
                            val stepOrder = (weekIndex * 3) + lessonIndex + 1

                            val originalWeek = originalRoadmap
                                ?.weeks
                                ?.getOrNull(weekIndex)

                            val originalLesson = originalWeek
                                ?.lessons
                                ?.getOrNull(lessonIndex)
                                ?: lesson

                            val originalDescription = originalWeek?.description
                                ?: week.description

                            val completed = progressRecords.any {
                                it.step_order == stepOrder && it.is_completed
                            }

                            LessonRow(
                                lessonNumber = lessonIndex + 1,

                                // Display the translated lesson.
                                lesson = lesson,

                                completed = completed,
                                onCheckedChange = { checked ->
                                    val user = supabase.auth.currentUserOrNull()

                                    if (user == null) {
                                        errorMessage = roadmapScreenText(
                                            "You must be logged in."
                                        )
                                        return@LessonRow
                                    }

                                    errorMessage = ""

                                    // Save the original text in the progress record.
                                    val updatedRecord = ProgressRecord(
                                        progress_id = null,
                                        roadmap_id = roadmapId,
                                        user_id = user.id,
                                        step_title = originalLesson,
                                        step_description = originalDescription,
                                        step_order = stepOrder,
                                        is_completed = checked,
                                        completed_at = if (checked) {
                                            java.time.Instant.now().toString()
                                        } else {
                                            null
                                        }
                                    )

                                    progressRecords = progressRecords
                                        .filterNot {
                                            it.step_order == stepOrder
                                        }
                                        .plus(updatedRecord)

                                    scope.launch {
                                        try {
                                            if (roadmapId.isNotBlank()) {
                                                ProgressRepository.saveProgress(
                                                    roadmapId = roadmapId,
                                                    userId = user.id,
                                                    stepTitle = originalLesson,
                                                    stepDescription = originalDescription,
                                                    stepOrder = stepOrder,
                                                    isCompleted = checked
                                                )

                                                progressRecords =
                                                    ProgressRepository.getProgress(
                                                        roadmapId = roadmapId,
                                                        userId = user.id
                                                    )
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                                ?: roadmapScreenText(
                                                    "Could not save progress."
                                                )

                                            if (roadmapId.isNotBlank()) {
                                                try {
                                                    progressRecords =
                                                        ProgressRepository.getProgress(
                                                            roadmapId = roadmapId,
                                                            userId = user.id
                                                        )
                                                } catch (_: Exception) {
                                                    // Preserve the current state.
                                                }
                                            }
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

            // ERROR

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

        // BACK BUTTON

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = roadmapScreenText("Back to My Learning"),
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

private fun roadmapScreenText(english: String): String {
    return when (LanguageSupport.currentLanguage.value) {
        LanguageSupport.AFRIKAANS -> when (english) {
            "Learning roadmap" -> "Leerplan"
            "Your personalised learning journey" ->
                "Jou persoonlike leerreis"
            "No roadmap available" ->
                "Geen leerplan beskikbaar nie"
            "Your roadmap will appear here after it has been generated." ->
                "Jou leerplan sal hier verskyn nadat dit geskep is."
            "Your roadmap" -> "Jou leerplan"
            "Your progress" -> "Jou vordering"
            "Ready to get started?" -> "Gereed om te begin?"
            "Roadmap completed!" -> "Leerplan voltooi!"
            "Your learning plan" -> "Jou leerplan"
            "Back to My Learning" -> "Terug na My Leerwerk"
            "You must be logged in." -> "Jy moet aangemeld wees."
            "Could not load progress." -> "Kon nie vordering laai nie."
            "Could not save progress." -> "Kon nie vordering stoor nie."
            "lessons_progress" ->
                "{completed} van {total} lesse voltooi"
            "plan_count" -> "{weeks} weke • {lessons} lesse"
            "week_label" -> "Week {week}"
            else -> english
        }

        LanguageSupport.ZULU -> when (english) {
            "Learning roadmap" -> "Uhlelo lokufunda"
            "Your personalised learning journey" ->
                "Uhambo lwakho lokufunda oluqondene nawe"
            "No roadmap available" ->
                "Alukho uhlelo lokufunda olutholakalayo"
            "Your roadmap will appear here after it has been generated." ->
                "Uhlelo lwakho lokufunda luzovela lapha uma seludaliwe."
            "Your roadmap" -> "Uhlelo lwakho lokufunda"
            "Your progress" -> "Inqubekelaphambili yakho"
            "Ready to get started?" -> "Usukulungele ukuqala?"
            "Roadmap completed!" -> "Uhlelo lokufunda luqediwe!"
            "Your learning plan" -> "Uhlelo lwakho lokufunda"
            "Back to My Learning" -> "Buyela Ekufundeni Kwami"
            "You must be logged in." ->
                "Kufanele ungene ku-akhawunti yakho."
            "Could not load progress." ->
                "Ayikwazanga ukulayisha inqubekelaphambili."
            "Could not save progress." ->
                "Ayikwazanga ukugcina inqubekelaphambili."
            "lessons_progress" ->
                "{completed} kwezingu-{total} izifundo seziqediwe"
            "plan_count" -> "{weeks} amasonto • {lessons} izifundo"
            "week_label" -> "Isonto {week}"
            else -> english
        }

        else -> when (english) {
            "lessons_progress" ->
                "{completed} of {total} lessons completed"
            "plan_count" -> "{weeks} weeks • {lessons} lessons"
            "week_label" -> "Week {week}"
            else -> english
        }
    }
}
