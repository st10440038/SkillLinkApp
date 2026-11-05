package com.example.skilllinkapp.screens

import androidx.compose.ui.platform.LocalContext

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import android.util.Log
import kotlinx.coroutines.launch
import com.example.skilllinkapp.data.RoadmapTranslationCache
import com.example.skilllinkapp.network.GeminiService
import kotlinx.serialization.json.decodeFromJsonElement
import com.example.skilllinkapp.LanguageSupport
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.ui.platform.LocalContext
import com.example.skilllinkapp.SkillLinkApplication
import com.example.skilllinkapp.data.local.CachedRoadmapEntity
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.jsonObject
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.data.ProgressRepository
import com.example.skilllinkapp.data.supabase
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


private val PageBackground = Color(0xFFF7F5FC)
private val White = Color.White
private val TextSecondary = Color(0xFF77737D)
private val BorderColor = Color(0xFFE7E2EF)
private val SoftPurple = Color(0xFFF0EAFF)
private val SoftGreen = Color(0xFFEAF8F0)
private val Green = Color(0xFF25855A)
private val PurpleDark = Color(0xFF6941C6)



private fun myLearningText(english: String): String {
    return when (LanguageSupport.currentLanguage.value) {
        LanguageSupport.AFRIKAANS -> when (english) {
            "My Learning" -> "My leerwerk"
            "Keep building your skills" -> "Hou aan om jou vaardighede te ontwikkel"
            "You must be logged in." -> "Jy moet aangemeld wees."
            "Untitled roadmap" -> "Leerroete sonder titel"
            "Your learning journey" -> "Jou leerreis"
            "Create your first roadmap and start learning." ->
                "Skep jou eerste leerroete en begin leer."
            "roadmap" -> "leerroete"
            "roadmaps" -> "leerroetes"
            "in your library" -> "in jou biblioteek"
            "completed" -> "voltooi"
            "In Progress" -> "Aan die gang"
            "Completed" -> "Voltooi"
            "Loading your roadmaps..." -> "Jou leerroetes word gelaai..."
            "Something went wrong" -> "Iets het verkeerd geloop"
            "Try again" -> "Probeer weer"
            "No completed roadmaps" -> "Geen voltooide leerroetes nie"
            "Complete a roadmap and it will appear here." ->
                "Voltooi 'n leerroete en dit sal hier verskyn."
            "View In Progress" -> "Bekyk leerroetes aan die gang"
            "No roadmaps yet" -> "Nog geen leerroetes nie"
            "Create a personalised roadmap and start your learning journey." ->
                "Skep 'n persoonlike leerroete en begin jou leerreis."
            "Generate Roadmap" -> "Genereer leerroete"
            "Completed roadmaps" -> "Voltooide leerroetes"
            "Your finished learning journeys." -> "Jou voltooide leerreise."
            "Continue learning" -> "Hou aan leer"
            "Pick up where you left off." -> "Gaan voort waar jy opgehou het."
            "Delete roadmap?" -> "Skrap leerroete?"
            "Delete" -> "Skrap"
            "Cancel" -> "Kanselleer"
            "Progress" -> "Vordering"
            "Roadmap completed" -> "Leerroete voltooi"
            "Continue learning  →" -> "Hou aan leer  →"
            "Today" -> "Vandag"
            "Yesterday" -> "Gister"
            "of" -> "van"
            "lessons" -> "lesse"
            "Unable to delete this roadmap. Please log in again." ->
                "Kan nie hierdie leerroete skrap nie. Meld asseblief weer aan."
            "Could not delete roadmap." ->
                "Kon nie die leerroete skrap nie."
            "this roadmap" -> "hierdie leerroete"
            else -> english
        }

        LanguageSupport.ZULU -> when (english) {
            "My Learning" -> "Ukufunda Kwami"
            "Keep building your skills" -> "Qhubeka uthuthukisa amakhono akho"
            "You must be logged in." -> "Kufanele ungene ku-akhawunti yakho."
            "Untitled roadmap" -> "Uhlelo lokufunda olungenasihloko"
            "Your learning journey" -> "Uhambo lwakho lokufunda"
            "Create your first roadmap and start learning." ->
                "Dala uhlelo lwakho lokuqala lokufunda bese uqala ukufunda."
            "roadmap" -> "uhlelo lokufunda"
            "roadmaps" -> "izinhlelo zokufunda"
            "in your library" -> "kulabhulali yakho"
            "completed" -> "kuqediwe"
            "In Progress" -> "Kuyaqhubeka"
            "Completed" -> "Kuqediwe"
            "Loading your roadmaps..." -> "Kulayishwa izinhlelo zakho zokufunda..."
            "Something went wrong" -> "Kukhona okungahambanga kahle"
            "Try again" -> "Zama futhi"
            "No completed roadmaps" -> "Azikho izinhlelo zokufunda eziqediwe"
            "Complete a roadmap and it will appear here." ->
                "Qedela uhlelo lokufunda futhi luzovela lapha."
            "View In Progress" -> "Buka okuqhubekayo"
            "No roadmaps yet" -> "Azikho izinhlelo zokufunda okwamanje"
            "Create a personalised roadmap and start your learning journey." ->
                "Dala uhlelo lokufunda oluqondene nawe bese uqala ukufunda."
            "Generate Roadmap" -> "Dala uhlelo lokufunda"
            "Completed roadmaps" -> "Izinhlelo zokufunda eziqediwe"
            "Your finished learning journeys." -> "Uhambo lwakho lokufunda oluqediwe."
            "Continue learning" -> "Qhubeka nokufunda"
            "Pick up where you left off." -> "Qhubeka lapho ogcine khona."
            "Delete roadmap?" -> "Susa uhlelo lokufunda?"
            "Delete" -> "Susa"
            "Cancel" -> "Khansela"
            "Progress" -> "Inqubekelaphambili"
            "Roadmap completed" -> "Uhlelo lokufunda luqediwe"
            "Continue learning  →" -> "Qhubeka nokufunda  →"
            "Today" -> "Namuhla"
            "Yesterday" -> "Izolo"
            "of" -> "ku"
            "lessons" -> "izifundo"
            "Unable to delete this roadmap. Please log in again." ->
                "Ayikwazi ukususa lolu hlelo. Sicela ungene futhi."
            "Could not delete roadmap." ->
                "Ayikwazanga ukususa uhlelo lokufunda."
            "this roadmap" -> "lolu hlelo lokufunda"
            else -> english
        }

        else -> english
    }
}


@Composable
fun MyLearningScreen(
    refreshKey: Int = 0,
    onOpenRoadmap: (String, String) -> Unit,
    onGenerateRoadmap: () -> Unit
) {

    var roadmaps by remember {
        mutableStateOf<List<JsonObject>>(emptyList())
    }

    var translationRoadmaps by remember {
        mutableStateOf(emptyList<JsonObject>())
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


    val context = LocalContext.current

    val selectedLanguage by LanguageSupport.currentLanguage

    var translatedRoadmaps by remember {
        mutableStateOf<Map<String, JsonObject>>(emptyMap())
    }

    var translatingRoadmaps by remember {
        mutableStateOf(false)
    }

    // Tracks which language the currently displayed translations belong to.
    var translationsForLanguage by remember {
        mutableStateOf(LanguageSupport.ENGLISH)
    }

    // Stores translation errors and allows the user to retry.
    var translationError by remember {
        mutableStateOf<String?>(null)
    }

    var translationRetry by remember {
        mutableStateOf(0)
    }

    val roadmapDao = remember(context) {
        (context.applicationContext as SkillLinkApplication)
            .database
            .skillLinkDao()
    }

    var reloadAttempt by remember {
        mutableStateOf(0)
    }


    // =========================================================
    // LOAD ROADMAPS
    // =========================================================


    LaunchedEffect(refreshKey, reloadAttempt) {
        val user = supabase.auth.currentUserOrNull()

        isLoading = true
        errorMessage = ""

        // No account identity means we cannot safely select
        // which user's cached roadmaps to display.
        if (user == null) {
            roadmaps = emptyList()
            translationRoadmaps = emptyList()
            roadmapProgress = emptyMap()
            isLoading = false
            errorMessage = "You must be logged in."
            return@LaunchedEffect
        }

        // 1. Load cached roadmaps immediately from Room.
        try {
            val cached = roadmapDao
                .observeRoadmaps(user.id)
                .first()

            roadmaps = cached.mapNotNull { entity ->
                try {
                    Json.parseToJsonElement(entity.roadmapJson).jsonObject
                } catch (_: Exception) {
                    null
                }
            }

            // Show the cached list without waiting for Supabase.
            isLoading = false

            val cachedProgressMap = mutableMapOf<String, Pair<Int, Int>>()

            for (roadmap in roadmaps) {
                val id = roadmap["roadmap_id"]?.jsonPrimitive?.content ?: continue

                val progress = ProgressRepository.getCachedProgress(
                    roadmapId = id,
                    userId = user.id
                )

                val totalLessons = countLessons(roadmap)

                cachedProgressMap[id] = Pair(
                    progress.count { it.is_completed },
                    totalLessons
                )
            }

            roadmapProgress = cachedProgressMap
        } catch (cacheError: Exception) {
            roadmaps = emptyList()
        }

        // Offline fast path: calculate progress locally and skip
        // every Supabase request when there is no validated connection.
        if (!hasInternetConnection(context)) {
            val localProgressMap =
                mutableMapOf<String, Pair<Int, Int>>()

            for (roadmap in roadmaps) {
                val id = roadmap["roadmap_id"]
                    ?.jsonPrimitive
                    ?.content
                    ?: continue

                val data = roadmap["roadmap_data"]
                    ?: continue

                val totalLessons = countLessons(
                    parseRoadmapData(data)
                )

                val progress = ProgressRepository.getCachedProgress(
                    roadmapId = id,
                    userId = user.id
                )

                localProgressMap[id] = Pair(
                    progress.count { it.is_completed },
                    totalLessons
                )
            }

            roadmapProgress = localProgressMap
            isLoading = false
            errorMessage = ""

            // Allow translations to use the locally cached roadmaps.
            translationRoadmaps = roadmaps

            return@LaunchedEffect
        }


        // 2. Refresh the cached list from Supabase.
        try {
            val remoteRoadmaps = supabase
                .from("roadmaps")
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<JsonObject>()

            // Keep only valid roadmap records.
            val validRoadmaps = remoteRoadmaps.filter { roadmap ->
                try {
                    val data = roadmap["roadmap_data"]
                        ?: return@filter false

                    val roadmapObject = parseRoadmapData(data)
                    val title = roadmapObject["title"]
                        ?.jsonPrimitive
                        ?.content
                        ?.trim()
                        ?: ""

                    isProperRoadmapTitle(title)
                } catch (_: Exception) {
                    false
                }
            }

            val cachedRoadmaps = validRoadmaps.mapNotNull { roadmap ->
                try {
                    val id = roadmap["roadmap_id"]
                        ?.jsonPrimitive
                        ?.content
                        ?: return@mapNotNull null

                    val data = roadmap["roadmap_data"]
                        ?: return@mapNotNull null

                    val title = parseRoadmapData(data)["title"]
                        ?.jsonPrimitive
                        ?.content
                        ?.trim()
                        ?: "Untitled roadmap"

                    CachedRoadmapEntity(
                        roadmapId = id,
                        userId = user.id,
                        title = title,
                        roadmapJson = roadmap.toString()
                    )
                } catch (_: Exception) {
                    null
                }
            }

            /*
             * Replace the local cache only after a successful server response.
             *
             * An empty server response is treated as a valid empty list.
             * If every returned record is malformed, preserve the old cache
             * to avoid deleting potentially useful offline data.
             */
            if (remoteRoadmaps.isEmpty() || validRoadmaps.isNotEmpty()) {
                roadmapDao.deleteRoadmapsForUser(user.id)

                if (cachedRoadmaps.isNotEmpty()) {
                    roadmapDao.saveRoadmaps(cachedRoadmaps)
                }
            }

            // Display the latest valid server data.
            roadmaps = validRoadmaps.sortedByDescending { roadmap ->
                roadmap["created_at"]
                    ?.jsonPrimitive
                    ?.content
                    ?: ""
            }

        } catch (networkError: Exception) {
            // If the network request fails, retain the Room data already loaded.
            if (roadmaps.isEmpty()) {
                errorMessage =
                    "No roadmaps are saved on this device yet. " +
                            "Connect to the internet and open your roadmaps " +
                            "once to save them for offline use."
            }
        }


        // 3. Refresh progress silently in the background.
        isLoading = false

        // Start translations only after Room/Supabase loading is complete.
        translationRoadmaps = roadmaps

        roadmaps.forEach { roadmap ->
            launch {
                val id = roadmap["roadmap_id"]
                    ?.jsonPrimitive
                    ?.content ?: return@launch

                val data = roadmap["roadmap_data"]
                    ?: return@launch

                try {
                    val totalLessons = countLessons(parseRoadmapData(data))

                    val progress = ProgressRepository.getProgress(
                        roadmapId = id,
                        userId = user.id,
                        syncPending = false
                    )

                    roadmapProgress = roadmapProgress + (
                            id to Pair(
                                progress.count { it.is_completed },
                                totalLessons
                            )
                            )
                } catch (e: Exception) {
                    Log.w("MyLearningScreen", "Progress refresh failed.", e)
                }
            }
        }

        launch {
            ProgressRepository.syncPendingProgress(user.id)
        }
    }


    // =========================================================
    // TRANSLATE ROADMAP CARDS
    // =========================================================

    LaunchedEffect(
        translationRoadmaps,
        selectedLanguage,
        translationRetry
    ) {
        translatedRoadmaps = emptyMap()
        translationError = null

        if (translationRoadmaps.isEmpty()) {
            translatingRoadmaps = false
            translationsForLanguage = selectedLanguage
            return@LaunchedEffect
        }

        // English does not require translation.
        if (selectedLanguage == LanguageSupport.ENGLISH) {
            translatedRoadmaps = emptyMap()
            translatingRoadmaps = false
            translationsForLanguage = LanguageSupport.ENGLISH
            return@LaunchedEffect
        }

        translatingRoadmaps = true

        val languageName = when (selectedLanguage) {
            LanguageSupport.ZULU -> "isiZulu"
            LanguageSupport.AFRIKAANS -> "Afrikaans"
            else -> "English"
        }

        val translatedResults = mutableMapOf<String, JsonObject>()

        try {
            for (roadmapRecord in translationRoadmaps) {

                val id = roadmapRecord["roadmap_id"]
                    ?.jsonPrimitive
                    ?.content
                    ?: throw IllegalStateException(
                        "A saved roadmap is missing its ID."
                    )

                val data = roadmapRecord["roadmap_data"]
                    ?: throw IllegalStateException(
                        "A saved roadmap is missing its data."
                    )

                val originalRoadmap = parseRoadmapData(data)

                // Check the local translation cache first.
                val cachedTranslation = RoadmapTranslationCache.get(
                    context = context,
                    roadmapId = id,
                    language = selectedLanguage
                )

                Log.d(
                    "RoadmapTranslation",
                    "Language: $languageName | Roadmap: $id | Cache found: ${cachedTranslation != null}"
                )

                val translatedJson = if (cachedTranslation != null) {

                    cachedTranslation

                } else {

                    Log.d(
                        "RoadmapTranslation",
                        "Translating roadmap $id into $languageName"
                    )

                    val result = GeminiService.translateRoadmap(
                        roadmapJson = originalRoadmap.toString(),
                        targetLanguage = languageName
                    )

                    // Save the successful translation for future use,
                    // including offline access.
                    RoadmapTranslationCache.save(
                        context = context,
                        roadmapId = id,
                        language = selectedLanguage,
                        translatedJson = result
                    )

                    result
                }

                // Validate the translated roadmap.
                val translatedObject = Json.parseToJsonElement(
                    translatedJson
                ).jsonObject

                val translatedTitle = translatedObject["title"]
                    ?.jsonPrimitive
                    ?.content
                    ?.trim()

                require(!translatedTitle.isNullOrBlank()) {
                    "The translated roadmap has no title."
                }

                translatedResults[id] = translatedObject
            }

            // Display the translations only after the translation
            // process has completed successfully.
            translatedRoadmaps = translatedResults
            translationsForLanguage = selectedLanguage

        } catch (e: kotlinx.coroutines.CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(
                "RoadmapTranslation",
                "Could not translate roadmaps into $languageName.",
                e
            )

            translatedRoadmaps = emptyMap()

            translationError = e.message
                ?: "Unable to translate your roadmaps. Please try again."

            translationsForLanguage = selectedLanguage

        } finally {

            translatingRoadmaps = false
        }
    }


    // =========================================================
    // FILTERED ROADMAPS
    // =========================================================

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


    val inProgressCount =
        roadmaps.count { roadmap ->

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

            !(total > 0 && completed >= total)
        }


    val completedCount =
        roadmaps.count { roadmap ->

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

            total > 0 &&
                    completed >= total
        }


    // =========================================================
    // UI
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // =====================================================
        // HEADER
        // =====================================================

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 24.dp
                )
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SkillLinkPurple),
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
                    modifier = Modifier.size(12.dp)
                )

                Column {

                    Text(
                        text = myLearningText("My Learning"),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = myLearningText("Keep building your skills"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // =================================================
            // SUMMARY CARD
            // =================================================

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = SkillLinkPurple
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = myLearningText("Your learning journey"),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text = if (roadmaps.isEmpty()) {
                                myLearningText("Create your first roadmap and start learning.")
                            } else {
                                myLearningText(
                                    if (roadmaps.size == 1) {
                                        "1 roadmap in your library"
                                    } else {
                                        "${roadmaps.size} roadmaps in your library"
                                    }
                                )
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }

                    Spacer(
                        modifier = Modifier.size(14.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "$completedCount",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = myLearningText("completed"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // =====================================================
        // FILTERS
        // =====================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            LearningFilter(
                text = myLearningText("In Progress"),
                count = inProgressCount,
                selected = !showCompleted,
                onClick = {
                    showCompleted = false
                }
            )

            LearningFilter(
                text = myLearningText("Completed"),
                count = completedCount,
                selected = showCompleted,
                onClick = {
                    showCompleted = true
                }
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // =====================================================
        // CONTENT
        // =====================================================

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {

            when {

                    // -------------------------------------------------
                    // LOADING ROADMAPS OR TRANSLATIONS
                    // -------------------------------------------------

                    isLoading ||
                            translatingRoadmaps ||
                            translationsForLanguage != selectedLanguage -> {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 50.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            CircularProgressIndicator(
                                color = SkillLinkPurple,
                                strokeWidth = 3.dp
                            )

                            Spacer(
                                modifier = Modifier.height(14.dp)
                            )

                            val loadingMessage = if (isLoading) {
                                myLearningText("Loading your roadmaps...")
                            } else {
                                when (selectedLanguage) {
                                    LanguageSupport.ZULU ->
                                        "Preparing your roadmaps in isiZulu..."

                                    LanguageSupport.AFRIKAANS ->
                                        "Preparing your roadmaps in Afrikaans..."

                                    else ->
                                        "Preparing your roadmaps..."
                                }
                            }

                            Text(
                                text = loadingMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            if (!isLoading) {
                                Text(
                                    text = "Please wait while your translations are prepared.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // -------------------------------------------------
                    // TRANSLATION ERROR
                    // -------------------------------------------------

                    translationError != null -> {

                        EmptyLearningState(
                            title = "Translation unavailable",
                            description = translationError
                                ?: "Unable to translate your roadmaps.",

                            buttonText = "Retry translation",

                            onButtonClick = {
                                translationError = null
                                translatingRoadmaps = true
                                translationRetry++
                            }
                        )
                    }


                // -------------------------------------------------
                // ERROR
                // -------------------------------------------------

                errorMessage.isNotBlank() -> {

                    EmptyLearningState(
                        title = myLearningText("Something went wrong"),
                        description = errorMessage,
                        buttonText = myLearningText("Try again"),
                        onButtonClick = {
                            errorMessage = ""
                            isLoading = true
                            reloadAttempt++
                        }
                    )
                }


                // -------------------------------------------------
                // EMPTY
                // -------------------------------------------------

                displayedRoadmaps.isEmpty() -> {

                    EmptyLearningState(
                        title =
                            if (showCompleted) {
                                myLearningText("No completed roadmaps")
                            } else {
                                myLearningText("No roadmaps yet")
                            },

                        description =
                            if (showCompleted) {
                                myLearningText("Complete a roadmap and it will appear here.")
                            } else {
                                myLearningText("Create a personalised roadmap and start your learning journey.")
                            },

                        buttonText =
                            if (showCompleted) {
                                myLearningText("View In Progress")
                            } else {
                                myLearningText("Generate Roadmap")
                            },

                        onButtonClick = {

                            if (showCompleted) {

                                showCompleted = false

                            } else {

                                onGenerateRoadmap()
                            }
                        }
                    )
                }


                // -------------------------------------------------
                // ROADMAP LIST
                // -------------------------------------------------

                else -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 28.dp
                        ),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        item {

                            Text(
                                text =
                                    if (showCompleted) {
                                        myLearningText("Completed roadmaps")
                                    } else {
                                        myLearningText("Continue learning")
                                    },
                                style =
                                    MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    if (showCompleted) {
                                        myLearningText("Your finished learning journeys.")
                                    } else {
                                        myLearningText("Pick up where you left off.")
                                    },
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )
                        }


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

                            val originalRoadmapObject =
                                try {
                                    parseRoadmapData(roadmapData)
                                } catch (_: Exception) {
                                    null
                                } ?: return@items

                            val roadmapObject = if (
                                selectedLanguage == LanguageSupport.ENGLISH
                            ) {
                                originalRoadmapObject
                            } else {
                                translatedRoadmaps[roadmapId] ?: return@items
                            }

                            val title =
                                roadmapObject["title"]
                                    ?.jsonPrimitive
                                    ?.content
                                    ?.trim()
                                    ?: return@items

                            val summary =
                                roadmapObject["summary"]
                                    ?.jsonPrimitive
                                    ?.content
                                    ?.trim()
                                    ?: ""

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

                            val lastAccessed =
                                roadmap["last_accessed"]
                                    ?.jsonPrimitive
                                    ?.content
                                    ?: ""


                            RoadmapCard(
                                title = title,
                                summary = summary,
                                completed = completed,
                                total = total,
                                percentage = percentage,
                                lastAccessed = lastAccessed,
                                completedRoadmap =
                                    total > 0 &&
                                            completed >= total,

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
                                                // Opening the roadmap
                                                // should still work.
                                            }
                                        }
                                    }

                                    onOpenRoadmap(
                                        roadmapId,
                                        originalRoadmapObject.toString()
                                    )
                                },

                                onDelete = {
                                    roadmapToDelete = roadmap
                                }
                            )
                        }
                    }
                }
            }
        }
    }


    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

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

                myLearningText("this roadmap")
            }


        AlertDialog(

            onDismissRequest = {
                roadmapToDelete = null
            },

            title = {
                Text(
                    text = myLearningText("Delete roadmap?"),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text =
                        myLearningText("Are you sure you want to delete " +
                                "\"$selectedTitle\"? " +
                                "This will also remove its learning progress.")
                )
            },

            // =====================================================
            // CONFIRM DELETE
            // =====================================================

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
                            currentUser == null ||
                            roadmapId == null
                        ) {

                            errorMessage =
                                myLearningText("Unable to delete this roadmap. Please log in again.")

                            roadmapToDelete = null

                            return@TextButton
                        }


                        scope.launch {

                            try {

                                /*
                                 * IMPORTANT:
                                 *
                                 * Delete the progress records belonging
                                 * to this roadmap first.
                                 */

                                supabase
                                    .from("progress")
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
                                 * Then delete the actual roadmap
                                 * from the roadmaps table.
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
                                 * Only update the local UI after
                                 * Supabase deletion succeeds.
                                 */

                                roadmaps =
                                    roadmaps.filterNot { item ->

                                        item["roadmap_id"]
                                            ?.jsonPrimitive
                                            ?.content ==
                                                roadmapId
                                    }


                                roadmapProgress =
                                    roadmapProgress
                                        .toMutableMap()
                                        .apply {

                                            remove(
                                                roadmapId
                                            )
                                        }


                                roadmapToDelete = null

                                errorMessage = ""

                            } catch (e: Exception) {

                                /*
                                 * If Supabase rejects the deletion,
                                 * the roadmap remains visible locally.
                                 */

                                errorMessage =
                                    e.message
                                        ?: myLearningText("Could not delete roadmap.")

                                roadmapToDelete = null
                            }
                        }
                    }
                ) {

                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
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
                        text = myLearningText("Cancel")
                    )
                }
            }
        )
    }
}


// =============================================================
// LEARNING FILTER
// =============================================================

@Composable
private fun LearningFilter(
    text: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        modifier = Modifier.height(42.dp),
        shape = RoundedCornerShape(13.dp),
        color =
            if (selected) {
                SkillLinkPurple
            } else {
                MaterialTheme.colorScheme.surface
            },
        border =
            if (!selected) {
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            } else {
                null
            }
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color =
                    if (selected) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                fontWeight =
                    if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
            )

            Spacer(
                modifier = Modifier.width(7.dp)
            )

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            Color.White.copy(alpha = 0.18f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        if (selected) {
                            Color.White
                        } else {
                            PurpleDark
                        },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// =============================================================
// ROADMAP CARD
// =============================================================

@Composable
private fun RoadmapCard(
    title: String,
    summary: String,
    completed: Int,
    total: Int,
    percentage: Float,
    lastAccessed: String,
    completedRoadmap: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.Top
            ) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (completedRoadmap) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text =
                            if (completedRoadmap) {
                                "✓"
                            } else {
                                "S"
                            },
                        style =
                            MaterialTheme.typography.titleMedium,
                        color =
                            if (completedRoadmap) {
                                Green
                            } else {
                                SkillLinkPurple
                            },
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = title,
                        style =
                            MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    if (summary.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text = summary,
                            style =
                                MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.size(42.dp)
                ) {

                    Text(
                        text = "⋮",
                        style =
                            MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text =
                        if (completedRoadmap) {
                            myLearningText("Completed")
                        } else {
                            myLearningText("Progress")
                        },
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        if (completedRoadmap) {
                            Green
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        if (completedRoadmap) {
                            Green
                        } else {
                            SkillLinkPurple
                        },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            val animatedProgress by animateFloatAsState(
                targetValue = if (completedRoadmap) 1f else percentage.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 900),
                label = "roadmapProgress"
            )
            val progressTrack = if (completedRoadmap) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
            ) {
                val radius = size.height / 2f
                drawRoundRect(
                    color = progressTrack,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius)
                )
                val progressWidth = size.width * animatedProgress
                if (progressWidth > 0f) {
                    drawRoundRect(
                        brush = if (completedRoadmap) {
                            Brush.horizontalGradient(
                                colors = listOf(Green, Green),
                                startX = 0f,
                                endX = size.width
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFE34B4B),
                                    Color(0xFFF18A36),
                                    Color(0xFFF0C94A),
                                    Color(0xFF8BCB58)
                                ),
                                startX = 0f,
                                endX = size.width
                            )
                        },
                        size = Size(progressWidth, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(9.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "$completed of $total lessons",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                if (lastAccessed.isNotBlank()) {

                    Text(
                        text =
                            formatLastAccessed(
                                lastAccessed
                            ),
                        style =
                            MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color =
                    if (completedRoadmap) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 11.dp
                    ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            if (completedRoadmap) {
                                myLearningText("✓  Roadmap completed")
                            } else {
                                myLearningText("Continue learning  →")
                            },
                        style =
                            MaterialTheme.typography.labelLarge,
                        color =
                            if (completedRoadmap) {
                                Green
                            } else {
                                PurpleDark
                            },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


// =============================================================
// EMPTY STATE
// =============================================================

@Composable
private fun EmptyLearningState(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 30.dp,
                vertical = 35.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "S",
                style =
                    MaterialTheme.typography.headlineMedium,
                color = SkillLinkPurple,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text = title,
            style =
                MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Text(
            text = description,
            style =
                MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign =
                androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onButtonClick,
            shape = RoundedCornerShape(14.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = SkillLinkPurple
                ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {

            Text(
                text = buttonText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// =============================================================
// PARSE ROADMAP DATA
// =============================================================

private fun parseRoadmapData(
    element: JsonElement
): JsonObject {

    var current = element

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

    val objectData =
        current as? JsonObject
            ?: throw Exception(
                myLearningText("roadmap_data could not be converted into a JSON object.")
            )


    val generatedRoadmap =
        objectData["generated_roadmap"]


    if (generatedRoadmap != null) {

        var inner =
            generatedRoadmap

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
                myLearningText("generated_roadmap could not be converted into a JSON object.")
            )
    }

    return objectData
}


// =============================================================
// COUNT LESSONS
// =============================================================

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


// =============================================================
// VALID ROADMAP TITLE
// =============================================================

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
            myLearningText("roadmap"),
            myLearningText("learning roadmap"),
            myLearningText("your learning roadmap"),
            myLearningText("my roadmap"),
            myLearningText("personalised roadmap"),
            myLearningText("personalized roadmap")
        )

    return cleaned !in invalidTitles
}


// =============================================================
// FORMAT LAST ACCESSED
// =============================================================

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
                myLearningText("Today")

            yesterday ->
                myLearningText("Yesterday")

            else -> {

                val formatter =
                    java.time.format.DateTimeFormatter
                        .ofPattern("dd MMM")

                formatter.format(
                    instant.atZone(zone)
                )
            }
        }

    } catch (_: Exception) {

        ""
    }
}

private fun hasInternetConnection(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

    val network = connectivityManager.activeNetwork
        ?: return false

    val capabilities =
        connectivityManager.getNetworkCapabilities(network)
            ?: return false

    return capabilities.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET
    ) && capabilities.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_VALIDATED
    )
}