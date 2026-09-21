package com.example.skilllinkapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit
) {

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val purple = Color(0xFF6C4AB6)
    val lightPurple = Color(0xFFF4F0FF)
    val textDark = Color(0xFF242124)
    val textGray = Color(0xFF777277)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // SkillLink logo
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(lightPurple),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "S",
                    color = purple,
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Create your account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join SkillLink and start learning from others.",
                style = MaterialTheme.typography.bodyMedium,
                color = textGray
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Full name
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Full name")
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = purple,
                    focusedLabelColor = purple,
                    cursorColor = purple
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Email address")
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = purple,
                    focusedLabelColor = purple,
                    cursorColor = purple
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Password")
                },
                trailingIcon = {

                    TextButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            style = MaterialTheme.typography.labelMedium,
                            color = purple
                        )
                    }
                },
                visualTransformation =
                    if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = purple,
                    focusedLabelColor = purple,
                    cursorColor = purple
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Confirm password")
                },
                trailingIcon = {

                    TextButton(
                        onClick = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }
                    ) {
                        Text(
                            text = if (confirmPasswordVisible) "Hide" else "Show",
                            style = MaterialTheme.typography.labelMedium,
                            color = purple
                        )
                    }
                },
                visualTransformation =
                    if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = purple,
                    focusedLabelColor = purple,
                    cursorColor = purple
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Create account
            Button(
                onClick = {

                    if (
                        fullName.isBlank() ||
                        email.isBlank() ||
                        password.isBlank() ||
                        confirmPassword.isBlank()
                    ) {
                        message = "Please fill in all fields."
                        return@Button
                    }

                    if (password != confirmPassword) {
                        message = "Passwords do not match."
                        return@Button
                    }

                    scope.launch {

                        isLoading = true
                        message = ""

                        val enteredEmail = email.trim()
                        val enteredPassword = password

                        try {

                            supabase.auth.signUpWith(
                                Email,
                                redirectUrl = "skilllink://login-callback"
                            ) {

                                this.email = enteredEmail
                                this.password = enteredPassword

                                data = buildJsonObject {
                                    put(
                                        "full_name",
                                        fullName.trim()
                                    )
                                }
                            }

                            message =
                                "Account created successfully. Check your email."

                        } catch (e: Exception) {

                            message =
                                "${e::class.simpleName}: ${e.message}"

                        } finally {

                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = purple,
                    contentColor = Color.White
                )
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text = "Create account",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Message
            if (message.isNotEmpty()) {

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (
                        message.startsWith("Account created")
                    ) {
                        purple
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = textGray
            )

            TextButton(
                onClick = onLoginClick
            ) {

                Text(
                    text = "Log in",
                    style = MaterialTheme.typography.labelLarge,
                    color = purple
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Teach. Learn. Connect.",
                style = MaterialTheme.typography.labelMedium,
                color = textGray
            )
        }
    }
}