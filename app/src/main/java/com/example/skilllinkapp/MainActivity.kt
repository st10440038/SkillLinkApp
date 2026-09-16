package com.example.skilllinkapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.screens.GenerateRoadmapScreen
import com.example.skilllinkapp.screens.HomeScreen
import com.example.skilllinkapp.screens.LoginScreen
import com.example.skilllinkapp.screens.MyLearningScreen
import com.example.skilllinkapp.screens.ProfileSetupScreen
import com.example.skilllinkapp.screens.RegisterScreen
import com.example.skilllinkapp.screens.RoadmapScreen
import com.example.skilllinkapp.screens.SkillsKnownScreen
import com.example.skilllinkapp.screens.SkillsToLearnScreen
import com.example.skilllinkapp.ui.theme.SkillLinkAppTheme
import io.github.jan.supabase.auth.handleDeeplinks
import androidx.compose.foundation.layout.padding

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supabase.handleDeeplinks(intent)

        setContent {
            SkillLinkAppTheme {
                AuthScreen()
            }
        }
    }
}

@Composable
fun AuthScreen() {

    var showLogin by remember {
        mutableStateOf(false)
    }

    var isLoggedIn by remember {
        mutableStateOf(false)
    }

    var showProfileSetup by remember {
        mutableStateOf(false)
    }

    var profileSetupStep by remember {
        mutableStateOf(1)
    }

    var showGenerateRoadmap by remember {
        mutableStateOf(false)
    }

    var generatedRoadmap by remember {
        mutableStateOf("")
    }

    var generatedRoadmapId by remember {
        mutableStateOf("")
    }

    /*
     * Controls which main page is displayed.
     */
    var selectedPage by remember {
        mutableStateOf("home")
    }

    if (showProfileSetup) {

        when (profileSetupStep) {

            1 -> {
                ProfileSetupScreen(
                    onNext = {
                        profileSetupStep = 2
                    },
                    onSkip = {
                        showProfileSetup = false
                    }
                )
            }

            2 -> {
                SkillsKnownScreen(
                    onNext = {
                        profileSetupStep = 3
                    },
                    onBack = {
                        profileSetupStep = 1
                    }
                )
            }

            3 -> {
                SkillsToLearnScreen(
                    onFinish = {
                        showProfileSetup = false
                        profileSetupStep = 1
                    },
                    onBack = {
                        profileSetupStep = 2
                    }
                )
            }
        }

    } else if (showGenerateRoadmap) {

        GenerateRoadmapScreen(
            targetSkill = "",

            onBack = {
                showGenerateRoadmap = false
            },

            onRoadmapGenerated = { roadmap, roadmapId ->

                generatedRoadmap = roadmap
                generatedRoadmapId = roadmapId

                showGenerateRoadmap = false
            },

            onGenerate = {
                    targetSkill,
                    experienceLevel,
                    knownSkills,
                    learningGoal,
                    weeklyTime,
                    learningPace ->

                // Roadmap generation is handled
                // inside GenerateRoadmapScreen.
            }
        )

    } else if (generatedRoadmap.isNotEmpty()) {

        RoadmapScreen(
            roadmapJson = generatedRoadmap,
            roadmapId = generatedRoadmapId,

            onBack = {
                generatedRoadmap = ""
                generatedRoadmapId = ""
            }
        )

    } else if (isLoggedIn) {

        Scaffold(

            bottomBar = {

                NavigationBar {

                    NavigationBarItem(
                        selected = selectedPage == "home",
                        onClick = {
                            selectedPage = "home"
                        },
                        icon = {
                            Text("⌂")
                        },
                        label = {
                            Text("Home")
                        }
                    )

                    NavigationBarItem(
                        selected = selectedPage == "learning",
                        onClick = {
                            selectedPage = "learning"
                        },
                        icon = {
                            Text("✓")
                        },
                        label = {
                            Text("My Learning")
                        }
                    )
                }
            }

        ) { paddingValues ->

            Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                when (selectedPage) {

                    "home" -> {

                        HomeScreen(
                            roadmapId = generatedRoadmapId,
                            roadmapJson = generatedRoadmap,

                            onCompleteProfile = {
                                profileSetupStep = 1
                                showProfileSetup = true
                            },

                            onGenerateRoadmap = {
                                showGenerateRoadmap = true
                            },

                            onLogout = {
                                isLoggedIn = false
                                showLogin = true
                            }
                        )
                    }

                    "learning" -> {

                        MyLearningScreen(
                            onOpenRoadmap = { roadmapId, roadmapJson ->
                                generatedRoadmapId = roadmapId
                                generatedRoadmap = roadmapJson
                            },
                            onGenerateRoadmap = {
                                showGenerateRoadmap = true
                            }
                        )
                    }
                }
            }
        }

    } else if (showLogin) {

        LoginScreen(
            onRegisterClick = {
                showLogin = false
            },

            onLoginSuccess = {
                isLoggedIn = true
            }
        )

    } else {

        RegisterScreen(
            onLoginClick = {
                showLogin = true
            }
        )
    }
}