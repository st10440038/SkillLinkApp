
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skilllinkapp.LanguageSupport
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
    var registrationSucceeded by remember { mutableStateOf(false) }

    val selectedLanguage by LanguageSupport.currentLanguage
    val scope = rememberCoroutineScope()

    val purple = Color(0xFF6C4AB6)
    val lightPurple = Color(0xFFF4F0FF)
    val textDark = Color(0xFF242124)
    val textGray = Color(0xFF777277)
    val errorColor = Color(0xFFB3261E)

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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(lightPurple)
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    color = purple,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = authText("register_title", selectedLanguage),
                color = textDark,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = authText("register_subtitle", selectedLanguage),
                color = textGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    message = ""
                    registrationSucceeded = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(authText("full_name", selectedLanguage))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    message = ""
                    registrationSucceeded = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(authText("email", selectedLanguage))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = ""
                    registrationSucceeded = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(authText("password", selectedLanguage))
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Text(
                            text = authText(
                                if (passwordVisible) "hide" else "show",
                                selectedLanguage
                            ),
                            color = purple
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    message = ""
                    registrationSucceeded = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        authText("confirm_password", selectedLanguage)
                    )
                },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(
                        onClick = {
                            confirmPasswordVisible =
                                !confirmPasswordVisible
                        }
                    ) {
                        Text(
                            text = authText(
                                if (confirmPasswordVisible) "hide" else "show",
                                selectedLanguage
                            ),
                            color = purple
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    message = ""
                    registrationSucceeded = false

                    if (
                        fullName.isBlank() ||
                        email.isBlank() ||
                        password.isBlank() ||
                        confirmPassword.isBlank()
                    ) {
                        message = authText(
                            "fill_fields",
                            selectedLanguage
                        )
                        return@Button
                    }

                    if (password != confirmPassword) {
                        message = authText(
                            "password_mismatch",
                            selectedLanguage
                        )
                        return@Button
                    }

                    scope.launch {
                        isLoading = true

                        val enteredEmail = email.trim()
                        val enteredPassword = password
                        val enteredFullName = fullName.trim()

                        try {
                            supabase.auth.signUpWith(
                                Email,
                                redirectUrl = "skilllink://login-callback"
                            ) {
                                this.email = enteredEmail
                                this.password = enteredPassword

                                data = buildJsonObject {
                                    put("full_name", enteredFullName)
                                }
                            }

                            registrationSucceeded = true
                            message = authText(
                                "account_created",
                                selectedLanguage
                            )
                        } catch (e: Exception) {
                            // Preserve backend details to help diagnose failures.
                            registrationSucceeded = false
                            message = "${e::class.simpleName}: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = purple
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(22.dp)
                    )
                } else {
                    Text(
                        text = authText(
                            "create_account",
                            selectedLanguage
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = if (registrationSucceeded) {
                        purple
                    } else {
                        errorColor
                    },
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = authText("has_account", selectedLanguage),
                color = textGray,
                fontSize = 14.sp
            )

            TextButton(onClick = onLoginClick) {
                Text(
                    text = authText("login", selectedLanguage),
                    color = purple,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = authText("tagline", selectedLanguage),
                color = textGray,
                fontSize = 13.sp
            )
        }
    }
}
