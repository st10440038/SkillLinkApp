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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.skilllinkapp.data.ProgressRecord
import com.example.skilllinkapp.data.ProgressRepository
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.ui.theme.SkillLinkBackground
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

            errorMessage =
                e.message ?: "Could not load progress."

        } finally {

            isLoadingProgress = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillLinkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            text = "Your Learning Roadmap",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (roadmap == null) {

            Text(
                text = "Your roadmap will appear here after it has been generated.",
                style = MaterialTheme.typography.bodyLarge
            )

        } else {

            Text(
                text = roadmap.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = roadmap.summary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${roadmap.weeks.size} Week Learning Plan",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingProgress) {

                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            roadmap.weeks.forEachIndexed { weekIndex, week ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors()
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = week.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Week ${weekIndex + 1}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = week.description,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        week.lessons.forEachIndexed { lessonIndex, lesson ->

                            val stepOrder =
                                (weekIndex * 3) + lessonIndex + 1

                            val completed = progressRecords.any {
                                it.step_order == stepOrder &&
                                        it.is_completed
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Checkbox(
                                    checked = completed,
                                    onCheckedChange = { checked ->

                                        val user =
                                            supabase.auth.currentUserOrNull()

                                        if (user == null) {

                                            errorMessage =
                                                "You must be logged in."

                                            return@Checkbox
                                        }

                                        scope.launch {

                                            try {

                                                errorMessage = ""

                                                /*
                                                 * If the roadmap ID is available,
                                                 * save the progress to Supabase.
                                                 */
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

                                                    /*
                                                     * Temporary local behaviour
                                                     * until the roadmap UUID is
                                                     * connected.
                                                     */
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

                                Text(
                                    text = lesson,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {

                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}