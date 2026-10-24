package com.example.skilllinkapp

import com.example.skilllinkapp.LanguageSupport
import com.example.skilllinkapp.localizedText
import android.content.Intent
import android.os.Bundle

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

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


@Composable
fun SkillLinkApp() {

    val scope = rememberCoroutineScope()


    // =========================================================
    // LOGIN
    // =========================================================

    var isLoggedIn by remember {
        mutableStateOf(false)
    }

    var showRegister by remember {
        mutableStateOf(true)
    }


    // =========================================================
    // MAIN NAVIGATION
    // =========================================================

    var selectedPage by remember {
        mutableStateOf("home")
    }


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

    Scaffold(

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

                    onGenerate = {
                            _, _, _, _, _, _ ->
                    },

                    onRoadmapGenerated = {
                            roadmapJson,
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

                    onOpenRoadmap = {
                            roadmapId,
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
                            showRegister = true

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
            }
        }
    }
}