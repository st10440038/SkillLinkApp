package com.example.skilllinkapp.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.ui.theme.SkillLinkBackground
import com.example.skilllinkapp.ui.theme.SkillLinkLightPurple
import com.example.skilllinkapp.ui.theme.SkillLinkNavy
import com.example.skilllinkapp.ui.theme.SkillLinkPurple
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit
) {

    var fullName by remember { mutableStateOf("User") }
    var email by remember { mutableStateOf("") }
    var biography by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var roadmapCount by remember { mutableStateOf(0) }
    var skillsCount by remember { mutableStateOf(0) }

    val user = supabase.auth.currentUserOrNull()

    // =========================================================
    // LOAD PROFILE DATA
    // =========================================================

    LaunchedEffect(user?.id) {

        if (user == null) {
            return@LaunchedEffect
        }

        try {

            // -------------------------------------------------
            // Load profile
            // -------------------------------------------------

            val profile = supabase
                .from("profiles")
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<JsonObject>()
                .firstOrNull()

            if (profile != null) {

                fullName =
                    profile["full_name"]
                        ?.jsonPrimitive
                        ?.content
                        ?.takeIf { it.isNotBlank() }
                        ?: "User"

                email =
                    profile["email"]
                        ?.jsonPrimitive
                        ?.content
                        ?.takeIf { it.isNotBlank() }
                        ?: user.email
                                ?: ""

                biography =
                    profile["biography"]
                        ?.jsonPrimitive
                        ?.content
                        ?: ""

                location =
                    profile["location"]
                        ?.jsonPrimitive
                        ?.content
                        ?: ""

            } else {

                fullName = "User"
                email = user.email ?: ""
                biography = ""
                location = ""
            }

            // -------------------------------------------------
            // Load roadmap count
            // -------------------------------------------------

            val roadmaps = supabase
                .from("roadmaps")
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<JsonObject>()

            roadmapCount = roadmaps.size

            // -------------------------------------------------
            // Load skills count
            // -------------------------------------------------

            val skills = supabase
                .from("user_skills")
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<JsonObject>()

            skillsCount = skills.size

        } catch (e: Exception) {

            println(
                "Profile loading error: ${e.message}"
            )
        }
    }

    // =========================================================
    // INITIALS
    // =========================================================

    val initials = fullName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") {
            it.first().uppercase()
        }

    // =========================================================
    // SCREEN
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillLinkBackground)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // =====================================================
        // PROFILE PICTURE
        // =====================================================

        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(SkillLinkLightPurple),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(SkillLinkPurple),
                contentAlignment = Alignment.Center
            ) {

                if (initials.isNotEmpty()) {

                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                } else {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(42.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // =====================================================
        // NAME
        // =====================================================

        Text(
            text = fullName,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = SkillLinkNavy
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        // =====================================================
        // EMAIL
        // =====================================================

        Text(
            text = email,
            fontSize = 14.sp,
            color = Color(0xFF85849A)
        )

        Spacer(
            modifier = Modifier.height(26.dp)
        )

        // =====================================================
        // BIO
        // =====================================================

        if (biography.isNotBlank()) {

            ProfileSectionCard {

                Text(
                    text = "About me",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillLinkNavy
                )

                Spacer(
                    modifier = Modifier.height(9.dp)
                )

                Text(
                    text = biography,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Color(0xFF656376)
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )
        }

        // =====================================================
        // LOCATION
        // =====================================================

        if (location.isNotBlank()) {

            ProfileSectionCard {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkillLinkLightPurple),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = SkillLinkPurple,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.size(13.dp)
                    )

                    Column {

                        Text(
                            text = "Location",
                            fontSize = 12.sp,
                            color = Color(0xFF85849A)
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text = location,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SkillLinkNavy
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }

        // =====================================================
        // STATISTICS
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            ProfileStatCard(
                title = "Roadmaps",
                value = roadmapCount.toString(),
                modifier = Modifier.weight(1f)
            )

            ProfileStatCard(
                title = "Skills",
                value = skillsCount.toString(),
                modifier = Modifier.weight(1f)
            )

            ProfileStatCard(
                title = "Day streak",
                value = "0",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        // =====================================================
        // EDIT PROFILE
        // =====================================================

        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SkillLinkPurple
            )
        ) {

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit profile",
                modifier = Modifier.size(19.dp)
            )

            Spacer(
                modifier = Modifier.size(8.dp)
            )

            Text(
                text = "Edit Profile",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }
}


// =============================================================
// PROFILE SECTION CARD
// =============================================================

@Composable
private fun ProfileSectionCard(
    content: @Composable () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            content()
        }
    }
}


// =============================================================
// PROFILE STAT CARD
// =============================================================

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 17.dp,
                    horizontal = 6.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SkillLinkPurple
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF85849A)
            )
        }
    }
}