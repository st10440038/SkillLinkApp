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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

    LaunchedEffect(Unit) {
        try {
            if (user != null) {

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
                        profile["full_name"]?.jsonPrimitive?.content
                            ?: "User"

                    email =
                        profile["email"]?.jsonPrimitive?.content
                            ?: user.email
                                    ?: ""

                    biography =
                        profile["biography"]?.jsonPrimitive?.content
                            ?: ""

                    location =
                        profile["location"]?.jsonPrimitive?.content
                            ?: ""
                } else {
                    email = user.email ?: ""
                }

                val roadmaps = supabase
                    .from("roadmaps")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<JsonObject>()

                roadmapCount = roadmaps.size

                val skills = supabase
                    .from("user_skills")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<JsonObject>()

                skillsCount = skills.size
            }
        } catch (e: Exception) {
            println("Profile loading error: ${e.message}")
        }
    }

    val initials = fullName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F2FF))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // Profile picture
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7656E8))
                        .padding(25.dp),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7656E8))
                        .padding(25.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = fullName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202035)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email,
                fontSize = 14.sp,
                color = Color(0xFF85849A)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bio
        if (biography.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "About me",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF202035)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = biography,
                        fontSize = 14.sp,
                        color = Color(0xFF85849A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Location
        if (location.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF7656E8)
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    Column {
                        Text(
                            text = "Location",
                            fontSize = 13.sp,
                            color = Color(0xFF85849A)
                        )

                        Text(
                            text = location,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF202035)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

        Spacer(modifier = Modifier.height(20.dp))

        // Edit profile
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7656E8)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit profile"
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = "Edit Profile",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7656E8)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF85849A)
            )
        }
    }
}