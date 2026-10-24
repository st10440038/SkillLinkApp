
package com.example.skilllinkapp.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
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
import com.example.skilllinkapp.LanguageSupport
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

/*
 * TRANSLATIONS FOR THIS SCREEN
 *
 * The selected language comes from LanguageSupport.
 * The English option values used by the application logic remain unchanged.
 */

private fun roadmapText(english: String): String {
    val zulu = mapOf(
        "You're offline" to "Awukho ku-inthanethi",
        "Connect to Wi-Fi or mobile data to create a new roadmap." to
                "Xhuma ku-Wi-Fi noma kudatha yeselula ukuze wakhe uhlelo olusha lokufunda.",
        "New roadmap" to "Uhlelo olusha lokufunda",
        "Personalise your learning journey" to "Yenza uhambo lwakho lokufunda luhambisane nezidingo zakho",
        "Your learning journey" to "Uhambo lwakho lokufunda",
        "Answer a few questions and we'll create a roadmap tailored to your goals, experience and available time." to
                "Phendula imibuzo embalwa ukuze sakhele uhlelo oluhambisana nezinhloso zakho, ulwazi lwakho nesikhathi onaso.",
        "Your current skills" to "Amakhono akho amanje",
        "Tell us what you already know." to "Sitshele lokho osukwazi kakade.",
        "Skills you already know" to "Amakhono osuwazi",
        "Separate multiple skills with commas." to "Hlukanisa amakhono amaningi ngokusebenzisa okhefana.",
        "e.g. Python, SQL, Excel" to "isib. Python, SQL, Excel",
        "Your goal" to "Inhloso yakho",
        "Tell us where you want to go." to "Sitshele ukuthi ufuna ukufinyelela kuphi.",
        "What do you want to achieve?" to "Yini ofuna ukuyifeza?",
        "Describe your goal naturally. You don't need to know the exact skill name." to
                "Chaza inhloso yakho ngendlela ejwayelekile. Akudingeki wazi igama eliqondile lekhono.",
        "e.g. I want to become a data scientist" to
                "isib. Ngifuna ukuba uchwepheshe wesayensi yedatha",
        "Your experience" to "Ulwazi lwakho",
        "Choose the level that describes you best." to "Khetha izinga elikuchaza kangcono.",
        "Beginner" to "Osaqalayo",
        "Intermediate" to "Osezingeni eliphakathi",
        "Advanced" to "Osezingeni eliphezulu",
        "Your schedule" to "Isikhathi sakho",
        "Choose how much time you can realistically give." to
                "Khetha isikhathi ongakwazi ukusinikela ngempela.",
        "1–2 hours" to "Amahora angu-1–2",
        "3–5 hours" to "Amahora angu-3–5",
        "6–10 hours" to "Amahora angu-6–10",
        "10+ hours" to "Amahora angu-10 noma ngaphezulu",
        "Learning pace" to "Ijubane lokufunda",
        "How quickly would you like to progress?" to "Ungathanda ukuthuthuka ngokushesha kangakanani?",
        "Relaxed" to "Kancane",
        "Balanced" to "Okulinganiselayo",
        "Fast" to "Ngokushesha",
        "Please enter your learning goal." to "Sicela ufake inhloso yakho yokufunda.",
        "No internet connection. Please connect to Wi-Fi or mobile data and try again." to
                "Akukho uxhumano lwe-inthanethi. Xhuma ku-Wi-Fi noma kudatha yeselula bese uzama futhi.",
        "You must be logged in to save your skills." to
                "Kufanele ungene ku-akhawunti yakho ukuze ulondoloze amakhono akho.",
        "Could not get the roadmap ID." to "Ayikwazanga ukuthola i-ID yohlelo lokufunda.",
        "We couldn't generate your roadmap right now. Please try again." to
                "Asikwazanga ukudala uhlelo lwakho lokufunda okwamanje. Sicela uzame futhi.",
        "Creating your roadmap..." to "Kwakhiwa uhlelo lwakho lokufunda...",
        "Generate my roadmap" to "Dala uhlelo lwami lokufunda",
        "Cancel" to "Khansela"
    )

    val afrikaans = mapOf(
        "You're offline" to "Jy is vanlyn",
        "Connect to Wi-Fi or mobile data to create a new roadmap." to
                "Koppel aan Wi-Fi of mobiele data om 'n nuwe leerplan te skep.",
        "New roadmap" to "Nuwe leerplan",
        "Personalise your learning journey" to "Pas jou leerreis by jou behoeftes aan",
        "Your learning journey" to "Jou leerreis",
        "Answer a few questions and we'll create a roadmap tailored to your goals, experience and available time." to
                "Beantwoord 'n paar vrae en ons skep 'n leerplan volgens jou doelwitte, ervaring en beskikbare tyd.",
        "Your current skills" to "Jou huidige vaardighede",
        "Tell us what you already know." to "Vertel ons wat jy reeds weet.",
        "Skills you already know" to "Vaardighede wat jy reeds ken",
        "Separate multiple skills with commas." to "Skei verskeie vaardighede met kommas.",
        "e.g. Python, SQL, Excel" to "bv. Python, SQL, Excel",
        "Your goal" to "Jou doelwit",
        "Tell us where you want to go." to "Vertel ons wat jy wil bereik.",
        "What do you want to achieve?" to "Wat wil jy bereik?",
        "Describe your goal naturally. You don't need to know the exact skill name." to
                "Beskryf jou doelwit op jou eie manier. Jy hoef nie die presiese vaardigheidsnaam te ken nie.",
        "e.g. I want to become a data scientist" to
                "bv. Ek wil 'n datawetenskaplike word",
        "Your experience" to "Jou ervaring",
        "Choose the level that describes you best." to "Kies die vlak wat jou die beste beskryf.",
        "Beginner" to "Beginner",
        "Intermediate" to "Intermediêr",
        "Advanced" to "Gevorderd",
        "Your schedule" to "Jou skedule",
        "Choose how much time you can realistically give." to
                "Kies hoeveel tyd jy realisties kan afstaan.",
        "1–2 hours" to "1–2 uur",
        "3–5 hours" to "3–5 uur",
        "6–10 hours" to "6–10 uur",
        "10+ hours" to "10+ uur",
        "Learning pace" to "Leertempo",
        "How quickly would you like to progress?" to "Hoe vinnig wil jy vorder?",
        "Relaxed" to "Ontspanne",
        "Balanced" to "Gebalanseerd",
        "Fast" to "Vinnig",
        "Please enter your learning goal." to "Voer asseblief jou leerdoelwit in.",
        "No internet connection. Please connect to Wi-Fi or mobile data and try again." to
                "Geen internetverbinding nie. Koppel aan Wi-Fi of mobiele data en probeer weer.",
        "You must be logged in to save your skills." to
                "Jy moet aangemeld wees om jou vaardighede te stoor.",
        "Could not get the roadmap ID." to "Kon nie die leerplan-ID kry nie.",
        "We couldn't generate your roadmap right now. Please try again." to
                "Ons kon nie nou jou leerplan skep nie. Probeer asseblief weer.",
        "Creating your roadmap..." to "Jou leerplan word geskep...",
        "Generate my roadmap" to "Skep my leerplan",
        "Cancel" to "Kanselleer"
    )

    return when (LanguageSupport.currentLanguage.value) {
        LanguageSupport.ZULU -> zulu[english] ?: english
        LanguageSupport.AFRIKAANS -> afrikaans[english] ?: english
        else -> english
    }
}

private fun hasValidatedInternet(context: Context): Boolean {
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

private fun isNetworkError(error: Throwable): Boolean {
    var current: Throwable? = error

    while (current != null) {
        if (
            current is java.net.UnknownHostException ||
            current is java.net.ConnectException ||
            current is java.net.SocketTimeoutException ||
            current is java.net.NoRouteToHostException
        ) {
            return true
        }

        val message = current.message.orEmpty().lowercase()

        if (
            message.contains("failed to connect") ||
            message.contains("unable to resolve host") ||
            message.contains("network is unreachable") ||
            message.contains("no address associated") ||
            message.contains("connection timed out") ||
            message.contains("connection refused")
        ) {
            return true
        }

        current = current.cause
    }

    return false
}

@Composable
private fun OfflineBanner() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFF0F2),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFF3C3CD)
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFAD5DC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♡",
                    color = Color(0xFFB42345),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = roadmapText("You're offline"),
                    color = Color(0xFF98233F),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = roadmapText(
                        "Connect to Wi-Fi or mobile data to create a new roadmap."
                    ),
                    color = Color(0xFF8D4053),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "♡",
                color = Color(0xFFC33C5A)
            )
        }
    }
}

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

    // Keep these canonical English values for existing app logic and database data.
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

    val context = LocalContext.current

    var hasInternet by remember(context) {
        mutableStateOf(hasValidatedInternet(context))
    }

    DisposableEffect(context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                hasInternet = hasValidatedInternet(context)
            }

            override fun onLost(network: Network) {
                hasInternet = hasValidatedInternet(context)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                hasInternet = hasValidatedInternet(context)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)
        hasInternet = hasValidatedInternet(context)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    text = roadmapText("New roadmap"),
                    style = MaterialTheme.typography.titleLarge,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = roadmapText("Personalise your learning journey"),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        if (!hasInternet) {
            OfflineBanner()
        }

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
                        .background(Color.White.copy(alpha = 0.18f)),
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

                Column {
                    Text(
                        text = roadmapText("Your learning journey"),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = roadmapText(
                    "Answer a few questions and we'll create a roadmap tailored to your goals, experience and available time."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 01: CURRENT SKILLS
        RoadmapSectionHeader(
            number = "01",
            title = roadmapText("Your current skills"),
            description = roadmapText("Tell us what you already know.")
        )

        Spacer(modifier = Modifier.height(12.dp))

        FormCard {
            Text(
                text = roadmapText("Skills you already know"),
                style = MaterialTheme.typography.titleMedium,
                color = SkillLinkNavy,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = roadmapText("Separate multiple skills with commas."),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = knownSkills,
                onValueChange = {
                    knownSkills = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(roadmapText("e.g. Python, SQL, Excel"))
                },
                minLines = 3,
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 02: LEARNING GOAL
        RoadmapSectionHeader(
            number = "02",
            title = roadmapText("Your goal"),
            description = roadmapText("Tell us where you want to go.")
        )

        Spacer(modifier = Modifier.height(12.dp))

        FormCard {
            Text(
                text = roadmapText("What do you want to achieve?"),
                style = MaterialTheme.typography.titleMedium,
                color = SkillLinkNavy,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = roadmapText(
                    "Describe your goal naturally. You don't need to know the exact skill name."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = learningGoal,
                onValueChange = {
                    learningGoal = it
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        roadmapText(
                            "e.g. I want to become a data scientist"
                        )
                    )
                },
                minLines = 4,
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 03: EXPERIENCE
        RoadmapSectionHeader(
            number = "03",
            title = roadmapText("Your experience"),
            description = roadmapText(
                "Choose the level that describes you best."
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                        text = roadmapText(level),
                        selected = experienceLevel == level,
                        onClick = {
                            experienceLevel = level
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 04: WEEKLY SCHEDULE
        RoadmapSectionHeader(
            number = "04",
            title = roadmapText("Your schedule"),
            description = roadmapText(
                "Choose how much time you can realistically give."
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        FormCard {
            Spacer(modifier = Modifier.height(16.dp))

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
                        text = roadmapText(time),
                        selected = weeklyTime == time,
                        onClick = {
                            weeklyTime = time
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LEARNING PACE
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

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = roadmapText("Learning pace"),
                    style = MaterialTheme.typography.titleMedium,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = roadmapText(
                    "How quickly would you like to progress?"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                        text = roadmapText(pace),
                        selected = learningPace == pace,
                        onClick = {
                            learningPace = pace
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ERROR
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

            Spacer(modifier = Modifier.height(16.dp))
        }

        // GENERATE BUTTON
        Button(
            onClick = {
                if (learningGoal.isBlank()) {
                    errorMessage = roadmapText(
                        "Please enter your learning goal."
                    )
                    return@Button
                }

                if (!hasInternet) {
                    errorMessage = roadmapText(
                        "No internet connection. Please connect to Wi-Fi or mobile data and try again."
                    )
                    return@Button
                }

                isGenerating = true
                errorMessage = ""
                generatedRoadmap = ""

                scope.launch {
                    try {
                        val user = supabase.auth.currentUserOrNull()

                        if (user == null) {
                            throw Exception(
                                roadmapText(
                                    "You must be logged in to save your skills."
                                )
                            )
                        }

                        val skillsToSave = knownSkills
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .distinctBy { it.lowercase() }

                        for (skill in skillsToSave) {
                            SkillRepository.saveKnownSkill(
                                userId = user.id,
                                skillName = skill
                            )
                        }

                        val response = GeminiService.generateRoadmap(
                            targetSkill =
                                "Determine the appropriate skill from the learning goal",
                            experienceLevel = experienceLevel,
                            knownSkills = knownSkills,
                            learningGoal = learningGoal,
                            weeklyTime = weeklyTime,
                            learningPace = learningPace
                        )

                        val knownSkillsJson = buildJsonArray {
                            knownSkills
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .distinctBy { it.lowercase() }
                                .forEach { skill ->
                                    add(JsonPrimitive(skill))
                                }
                        }

                        val roadmapJson = try {
                            Json.parseToJsonElement(response)
                        } catch (e: Exception) {
                            JsonPrimitive(response)
                        }

                        val roadmapData = buildJsonObject {
                            put(
                                "generated_roadmap",
                                roadmapJson
                            )
                        }

                        val createdRoadmap = supabase
                            .from("roadmaps")
                            .insert(
                                buildJsonObject {
                                    put("user_id", user.id)
                                    put("target_skill", targetSkill)
                                    put("roadmap_data", roadmapData)
                                    put("learning_goal", learningGoal)
                                    put("experience_level", experienceLevel)
                                    put("weekly_time", weeklyTime)
                                    put("learning_pace", learningPace)
                                    put("known_skills", knownSkillsJson)
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
                                    roadmapText(
                                        "Could not get the roadmap ID."
                                    )
                                )

                        generatedRoadmap = response

                        onRoadmapGenerated(
                            response,
                            roadmapId
                        )
                    } catch (e: Exception) {
                        errorMessage = if (
                            !hasValidatedInternet(context) ||
                            isNetworkError(e)
                        ) {
                            roadmapText(
                                "No internet connection. Please connect to Wi-Fi or mobile data and try again."
                            )
                        } else {
                            roadmapText(
                                "We couldn't generate your roadmap right now. Please try again."
                            )
                        }
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

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = roadmapText("Creating your roadmap..."),
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "✦",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(9.dp))

                Text(
                    text = roadmapText("Generate my roadmap"),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CANCEL
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
                text = roadmapText("Cancel"),
                color = SkillLinkNavy
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
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

        Spacer(modifier = Modifier.width(12.dp))

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
