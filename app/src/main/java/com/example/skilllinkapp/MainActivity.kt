package com.example.skilllinkapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import kotlinx.coroutines.delay
import com.example.skilllinkapp.notifications.NotificationEventBus
import com.example.skilllinkapp.notifications.DeviceTokenRepository
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import com.example.skilllinkapp.LanguageSupport
import com.example.skilllinkapp.localizedText
import android.content.Intent
import android.os.Bundle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.notifications.NotificationsScreen
import com.example.skilllinkapp.notifications.NotificationRepository
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.screens.EditProfileScreen
import com.example.skilllinkapp.screens.GenerateRoadmapScreen
import com.example.skilllinkapp.screens.HomeScreen
import com.example.skilllinkapp.screens.LoginScreen
import com.example.skilllinkapp.screens.MyLearningScreen
import com.example.skilllinkapp.screens.ProfileScreen
import com.example.skilllinkapp.screens.RegisterScreen
import com.example.skilllinkapp.screens.RoadmapScreen
import com.example.skilllinkapp.screens.SettingsScreen

import com.example.skilllinkapp.ui.theme.SkillLinkAppTheme

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks

import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supabase.handleDeeplinks(intent)

        LanguageSupport.load(this)

        // Retrieve the Firebase Cloud Messaging registration token
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Log.e(
                        "SkillLinkFCM",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@addOnCompleteListener
                }

                val token = task.result

                Log.d("SkillLinkFCM", "FCM Token: $token")
            }

        setContent {
            SkillLinkAppTheme {
                SkillLinkApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        supabase.handleDeeplinks(intent)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillLinkApp() {

    val scope = rememberCoroutineScope()

    var notificationTitle by remember {
        mutableStateOf("")
    }

    var notificationMessage by remember {
        mutableStateOf("")
    }

    var showNotificationBanner by remember {
        mutableStateOf(false)
    }

    var notificationVersion by remember {
        mutableStateOf(0)
    }


    // =========================================================
// LOGIN STATE
// =========================================================

    var isLoggedIn by remember {
        mutableStateOf(false)
    }

    var showRegister by remember {
        mutableStateOf(false)
    }

    var isAuthLoading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {

        NotificationEventBus.events.collect { event ->

            notificationTitle = event.title
            notificationMessage = event.message

            notificationVersion++

            showNotificationBanner = true
        }
    }

    LaunchedEffect(notificationVersion) {

        if (notificationVersion > 0) {

            delay(4500)

            showNotificationBanner = false
        }
    }

// =========================================================
// SUPABASE AUTH SESSION
// =========================================================

    LaunchedEffect(Unit) {
        val accessToken = supabase.auth.currentAccessTokenOrNull()
        Log.d("SkillLinkAuth", "Access token: $accessToken")

        supabase.auth.sessionStatus.collect { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    isLoggedIn = true
                    showRegister = false
                    isAuthLoading = false
                }

                is SessionStatus.NotAuthenticated -> {
                    isLoggedIn = false
                    isAuthLoading = false
                }

                else -> {
                    // Keep the loading screen visible.
                }
            }
        }
    }

    // =========================================================
    // MAIN NAVIGATION
    // =========================================================

    var selectedPage by remember {
        mutableStateOf("home")
    }

    var unreadNotificationCount by remember {
        mutableStateOf(0)
    }

    var previousPage by remember {
        mutableStateOf("home")
    }


    LaunchedEffect(isLoggedIn) {

        if (isLoggedIn) {

            try {

                val user = supabase.auth.currentUserOrNull()

                if (user != null) {

                    // Load unread notification count.
                    val notifications =
                        NotificationRepository.getNotifications(user.id)

                    unreadNotificationCount =
                        notifications.count { !it.isRead }

                    // Register the device token after authentication.
                    FirebaseMessaging.getInstance()
                        .token
                        .addOnCompleteListener { task ->

                            if (!task.isSuccessful) {

                                Log.e(
                                    "SkillLinkFCM",
                                    "Unable to retrieve FCM token",
                                    task.exception
                                )

                                return@addOnCompleteListener
                            }

                            val token = task.result

                            scope.launch {

                                try {

                                    DeviceTokenRepository.registerDevice(
                                        userId = user.id,
                                        token = token
                                    )

                                    Log.d(
                                        "SkillLinkFCM",
                                        "Device registration successful."
                                    )

                                } catch (e: Exception) {

                                    Log.e(
                                        "SkillLinkFCM",
                                        "Device registration failed",
                                        e
                                    )
                                }
                            }
                        }
                }

            } catch (e: Exception) {

                Log.e(
                    "SkillLink",
                    "Failed to initialize notifications",
                    e
                )

            }

        } else {

            unreadNotificationCount = 0
        }
    }

    /*LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            try {
                val user = supabase.auth.currentUserOrNull()

                if (user != null) {
                    val notifications =
                        NotificationRepository.getNotifications(user.id)

                    unreadNotificationCount =
                        notifications.count { !it.isRead }
                }
            } catch (e: Exception) {
                Log.e(
                    "SkillLink",
                    "Failed to load unread notification count",
                    e
                )
            }
        } else {
            unreadNotificationCount = 0
        }
    }*/

    // =========================================================
    // ROADMAP
    // =========================================================

    var showGenerateRoadmap by remember {
        mutableStateOf(false)
    }

    var generatedRoadmap by remember {
        mutableStateOf("")
    }

    var generatedRoadmapId by remember {
        mutableStateOf("")
    }


    // =========================================================
    // MY LEARNING
    // =========================================================

    var myLearningRefreshKey by remember {
        mutableStateOf(0)
    }


    // =========================================================
    // ANDROID SYSTEM BACK BUTTON
    // =========================================================

    BackHandler(
        enabled = isLoggedIn
    ) {
        if (selectedPage == "notifications") {
            selectedPage = previousPage
            return@BackHandler
        }

        // ---------------------------------------------------------
        // EDIT PROFILE FROM PROFILE
        // ---------------------------------------------------------

        if (selectedPage == "edit-profile-profile") {

            selectedPage = "profile"

            return@BackHandler
        }


        // ---------------------------------------------------------
        // EDIT PROFILE FROM SETTINGS
        // ---------------------------------------------------------

        if (selectedPage == "edit-profile-settings") {

            selectedPage = "settings"

            return@BackHandler
        }


        // ---------------------------------------------------------
        // ROADMAP DETAIL
        // ---------------------------------------------------------

        if (selectedPage == "roadmap-detail") {

            generatedRoadmap = ""
            generatedRoadmapId = ""

            selectedPage = "learning"

            myLearningRefreshKey++

            return@BackHandler
        }


        // ---------------------------------------------------------
        // ROADMAP GENERATION
        // ---------------------------------------------------------

        if (
            selectedPage == "roadmap" &&
            showGenerateRoadmap
        ) {

            showGenerateRoadmap = false
            selectedPage = "home"

            return@BackHandler
        }


        // ---------------------------------------------------------
        // OTHER MAIN PAGES
        // ---------------------------------------------------------

        when (selectedPage) {

            "learning" -> {
                selectedPage = "home"
            }

            "profile" -> {
                selectedPage = "home"
            }

            "settings" -> {
                selectedPage = "home"
            }

            "home" -> {
                // Already on Home.
            }
        }
    }


    // =========================================================
    // AUTHENTICATION LOADING
    // =========================================================

    if (isAuthLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }

        return
    }

    // =========================================================
    // NOT LOGGED IN
    // =========================================================

    if (!isLoggedIn) {

        if (showRegister) {

            RegisterScreen(
                onLoginClick = {
                    showRegister = false
                }
            )

        } else {

            LoginScreen(

                onRegisterClick = {
                    showRegister = true
                },

                onLoginSuccess = {

                    isLoggedIn = true

                    selectedPage = "home"

                    showGenerateRoadmap = false

                    generatedRoadmap = ""
                    generatedRoadmapId = ""
                }
            )
        }

        return
    }


    // =========================================================
    // EDIT PROFILE
    // =========================================================

    if (
        selectedPage == "edit-profile-profile" ||
        selectedPage == "edit-profile-settings"
    ) {

        val returnPage =
            if (selectedPage == "edit-profile-settings") {
                "settings"
            } else {
                "profile"
            }


        EditProfileScreen(

            onBack = {

                selectedPage = returnPage
            },

            onSave = {

                selectedPage = returnPage
            }
        )

        return
    }


    // =========================================================
    // ROADMAP DETAIL
    // =========================================================

    if (
        selectedPage == "roadmap-detail" &&
        generatedRoadmap.isNotBlank()
    ) {

        RoadmapScreen(

            roadmapJson = generatedRoadmap,

            roadmapId = generatedRoadmapId,

            onBack = {

                generatedRoadmap = ""
                generatedRoadmapId = ""

                selectedPage = "learning"

                myLearningRefreshKey++
            }
        )

        return
    }


    // =========================================================
    // MAIN APP
    // =========================================================

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Scaffold(

            topBar = {
                if (selectedPage != "notifications") {
                    TopAppBar(
                        title = {
                            Text(
                                text = "SkillLink"
                            )
                        },
                        actions = {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationCount > 0) {
                                        Badge {
                                            Text(
                                                if (unreadNotificationCount > 99) {
                                                    "99+"
                                                } else {
                                                    unreadNotificationCount.toString()
                                                }
                                            )
                                        }
                                    }
                                }
                            ) {
                                IconButton(
                                    onClick = {
                                        previousPage = selectedPage
                                        selectedPage = "notifications"
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications"
                                    )
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                NavigationBar {


                    // =================================================
                    // HOME
                    // =================================================

                    NavigationBarItem(

                        selected = selectedPage == "home",

                        onClick = {

                            selectedPage = "home"
                            showGenerateRoadmap = false
                        },

                        icon = {

                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },

                        label = {
                            Text(localizedText("Home"))
                        }
                    )


                    // =================================================
                    // ROADMAP
                    // =================================================

                    NavigationBarItem(

                        selected = selectedPage == "roadmap",

                        onClick = {

                            selectedPage = "roadmap"
                            showGenerateRoadmap = true
                        },

                        icon = {

                            Text("⌁")
                        },

                        label = {
                            Text(localizedText("Roadmap"))
                        }
                    )


                    // =================================================
                    // MY LEARNING
                    // =================================================

                    NavigationBarItem(

                        selected = selectedPage == "learning",

                        onClick = {

                            selectedPage = "learning"
                            showGenerateRoadmap = false

                            myLearningRefreshKey++
                        },

                        icon = {

                            Text("✓")
                        },

                        label = {
                            Text(localizedText("My Learning"))
                        }
                    )


                    // =================================================
                    // PROFILE
                    // =================================================

                    NavigationBarItem(

                        selected = selectedPage == "profile",

                        onClick = {

                            selectedPage = "profile"
                            showGenerateRoadmap = false
                        },

                        icon = {

                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile"
                            )
                        },

                        label = {
                            Text(localizedText("Profile"))
                        }
                    )


                    // =================================================
                    // SETTINGS
                    // =================================================

                    NavigationBarItem(

                        selected = selectedPage == "settings",

                        onClick = {

                            selectedPage = "settings"
                            showGenerateRoadmap = false
                        },

                        icon = {

                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        },

                        label = {
                            Text(localizedText("Settings"))
                        }
                    )
                }
            }

        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {


                // =====================================================
                // HOME
                // =====================================================

                if (selectedPage == "home") {

                    HomeScreen(

                        onGenerateRoadmap = {

                            selectedPage = "roadmap"
                            showGenerateRoadmap = true
                        },

                        // NEW: handles the "Continue learning" button
                        onContinueLearning = {

                            if (
                                generatedRoadmapId.isNotBlank() &&
                                generatedRoadmap.isNotBlank()
                            ) {

                                selectedPage = "roadmap-detail"

                            } else {

                                selectedPage = "learning"
                                showGenerateRoadmap = false

                                myLearningRefreshKey++
                            }
                        },

                        onLogout = {

                            scope.launch {

                                supabase.auth.signOut()

                                isLoggedIn = false
                                showRegister = true

                                selectedPage = "home"

                                showGenerateRoadmap = false

                                generatedRoadmap = ""
                                generatedRoadmapId = ""
                            }
                        },

                        roadmapId = generatedRoadmapId,

                        roadmapJson = generatedRoadmap
                    )
                }


                // =====================================================
                // ROADMAP GENERATION
                // =====================================================

                else if (
                    selectedPage == "roadmap" &&
                    showGenerateRoadmap
                ) {

                    GenerateRoadmapScreen(

                        targetSkill = "",

                        onBack = {

                            showGenerateRoadmap = false
                            selectedPage = "home"
                        },

                        onGenerate = { _, _, _, _, _, _ ->
                        },

                        onRoadmapGenerated = { roadmapJson,
                                               roadmapId ->

                            generatedRoadmap = roadmapJson

                            generatedRoadmapId = roadmapId

                            myLearningRefreshKey++

                            showGenerateRoadmap = false

                            selectedPage = "roadmap-detail"
                        }
                    )
                }


                // =====================================================
                // MY LEARNING
                // =====================================================

                else if (
                    selectedPage == "learning"
                ) {

                    MyLearningScreen(

                        refreshKey = myLearningRefreshKey,

                        onOpenRoadmap = { roadmapId,
                                          roadmapJson ->

                            generatedRoadmapId = roadmapId

                            generatedRoadmap = roadmapJson

                            selectedPage = "roadmap-detail"
                        },

                        onGenerateRoadmap = {

                            selectedPage = "roadmap"

                            showGenerateRoadmap = true
                        }
                    )
                }


                // =====================================================
                // PROFILE
                // =====================================================

                else if (
                    selectedPage == "profile"
                ) {

                    ProfileScreen(

                        onEditProfile = {

                            selectedPage = "edit-profile-profile"
                        }
                    )
                }


                // =====================================================
                // SETTINGS
                // =====================================================

                else if (
                    selectedPage == "settings"
                ) {

                    SettingsScreen(

                        onBack = {

                            selectedPage = "home"
                        },

                        onLogout = {

                            scope.launch {

                                supabase.auth.signOut()

                                isLoggedIn = false
                                showRegister = false

                                selectedPage = "home"

                                showGenerateRoadmap = false

                                generatedRoadmap = ""
                                generatedRoadmapId = ""
                            }
                        },

                        onEditProfile = {

                            selectedPage = "edit-profile-settings"
                        }
                    )
                } else if (selectedPage == "notifications") {

                    NotificationsScreen(
                        onBack = {
                            selectedPage = previousPage
                        },
                        onUnreadCountChanged = { count ->
                            unreadNotificationCount = count
                        }
                    )
                }
            } // Closes the inner content Box
        }     // Closes the Scaffold


        // =====================================================
        // SKILLLINK TOP NOTIFICATION BANNER
        // =====================================================

        AnimatedVisibility(

            visible = showNotificationBanner,

            enter = slideInVertically(
                initialOffsetY = { -it }
            ) + fadeIn(),

            exit = slideOutVertically(
                targetOffsetY = { -it }
            ) + fadeOut(),

            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )

        ) {

            Surface(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(20.dp),

                color = Color.White,

                shadowElevation = 10.dp,

                border = BorderStroke(
                    1.dp,
                    Color(0xFFE7E2EF)
                )

            ) {

                Row(

                    modifier = Modifier.padding(16.dp),

                    verticalAlignment = Alignment.CenterVertically,

                    horizontalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    Box(

                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEDE5FF)),

                        contentAlignment = Alignment.Center

                    ) {

                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = Color(0xFF7041C6),
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = notificationTitle,
                            style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF39205F)
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = notificationMessage,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = Color(0xFF77737D)
                        )
                    }

                    IconButton(
                        onClick = {
                            showNotificationBanner = false
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss notification",
                            tint = Color(0xFF77737D)
                        )
                    }
                }
            }
        }

    } // Closes the outer Box
}