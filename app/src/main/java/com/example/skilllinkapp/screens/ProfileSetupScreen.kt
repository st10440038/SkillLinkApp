package com.example.skilllinkapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val user_id: String,
    val full_name: String,
    val email: String,
    val biography: String? = null,
    val location: String? = null
)

@Composable
fun ProfileSetupScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Profile Setup",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Step 1 of 3",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Let's set up your profile",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Short bio") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("City / Location") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        Button(
            onClick = {
                if (name.isBlank()) {
                    message = "Please enter your name."
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    message = ""

                    try {
                        val currentUser = supabase.auth.currentUserOrNull()

                        if (currentUser == null) {
                            message = "You are not logged in."
                            return@launch
                        }

                        val userEmail = currentUser.email ?: ""

                        val profile = Profile(
                            user_id = currentUser.id,
                            full_name = name.trim(),
                            email = userEmail,
                            biography = bio.trim().ifBlank { null },
                            location = location.trim().ifBlank { null }
                        )

                        supabase
                            .from("profiles")
                            .upsert(profile)

                        onNext()

                    } catch (e: Exception) {
                        message = "${e::class.simpleName}: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Next")
            }
        }

        Button(
            onClick = onSkip,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Skip for now")
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}