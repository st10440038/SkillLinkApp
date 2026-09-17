package com.example.skilllinkapp.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp
import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var biography by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val user = supabase.auth.currentUserOrNull()

    // Load existing profile
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
                        profile["full_name"]
                            ?.toString()
                            ?.removeSurrounding("\"")
                            ?: ""

                    biography =
                        profile["biography"]
                            ?.toString()
                            ?.removeSurrounding("\"")
                            ?: ""

                    location =
                        profile["location"]
                            ?.toString()
                            ?.removeSurrounding("\"")
                            ?: ""
                }
            }
        } catch (e: Exception) {
            println("Edit profile loading error: ${e.message}")
        }

        isLoading = false
    }

    val initials = fullName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") {
            it.first().uppercase()
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F2FF))
            .padding(20.dp)
    ) {

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF202035)
                )
            }

            Text(
                text = "Edit Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202035)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Loading profile...",
                    color = Color(0xFF85849A)
                )
            }

        } else {

            // Profile picture
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7656E8))
                            .padding(25.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Full Name
            Text(
                text = "Full Name",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202035)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text("Enter your full name")
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Bio
            Text(
                text = "Bio",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202035)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = biography,
                onValueChange = {
                    biography = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text("Tell people a little about yourself")
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // City / Location
            Text(
                text = "City / Location",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202035)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text("e.g. Cape Town")
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save button
            Button(
                onClick = {

                    if (user != null && !isSaving) {

                        isSaving = true

                        coroutineScope.launch {

                            try {

                                supabase
                                    .from("profiles")
                                    .update(
                                        buildJsonObject {
                                            put(
                                                "full_name",
                                                fullName.trim()
                                            )

                                            put(
                                                "biography",
                                                biography.trim()
                                            )

                                            put(
                                                "location",
                                                location.trim()
                                            )
                                        }
                                    ) {
                                        filter {
                                            eq(
                                                "user_id",
                                                user.id
                                            )
                                        }
                                    }

                                onSave()

                            } catch (e: Exception) {

                                println(
                                    "Edit profile save error: ${e.message}"
                                )

                            } finally {

                                isSaving = false
                            }
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7656E8)
                )
            ) {

                Text(
                    text = if (isSaving) {
                        "Saving..."
                    } else {
                        "Save Changes"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}