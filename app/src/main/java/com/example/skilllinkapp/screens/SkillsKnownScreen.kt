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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
data class Skill(
    val skill_id: String,
    val skill_name: String,
    val category_id: String? = null,
    val description: String? = null,
    val difficulty: String? = null
)

@Serializable
data class NewSkill(
    val skill_name: String,
    val category_id: String? = null,
    val description: String? = null,
    val difficulty: String? = null
)

@Serializable
data class UserSkill(
    val user_skill_id: String? = null,
    val user_id: String,
    val skill_id: String,
    val skill_type: String,
    val experience_level: String? = null
)

@Composable
fun SkillsKnownScreen(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var skills by remember { mutableStateOf<List<Skill>>(emptyList()) }
    val selectedSkills = remember { mutableStateListOf<String>() }

    var customSkill by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isAddingSkill by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            skills = supabase
                .from("skills")
                .select()
                .decodeList<Skill>()

        } catch (e: Exception) {
            message = "${e::class.simpleName}: ${e.message}"
        } finally {
            isLoading = false
        }
    }

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
            text = "Step 2 of 3",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "What skills do you already know?",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 24.dp)
        )

        if (isLoading) {

            CircularProgressIndicator(
                modifier = Modifier.padding(top = 24.dp)
            )

        } else {

            skills.forEach { skill ->

                Button(
                    onClick = {
                        if (selectedSkills.contains(skill.skill_id)) {
                            selectedSkills.remove(skill.skill_id)
                        } else {
                            selectedSkills.add(skill.skill_id)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        if (selectedSkills.contains(skill.skill_id)) {
                            "✓ ${skill.skill_name}"
                        } else {
                            skill.skill_name
                        }
                    )
                }
            }

            OutlinedTextField(
                value = customSkill,
                onValueChange = { customSkill = it },
                label = {
                    Text("Add your own skill")
                },
                placeholder = {
                    Text("e.g. Machine Learning")
                },
                enabled = !isAddingSkill && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Button(
                onClick = {

                    val newSkillName = customSkill.trim()

                    if (newSkillName.isBlank()) {
                        message = "Please enter a skill."
                        return@Button
                    }

                    val existingSkill = skills.firstOrNull {
                        it.skill_name.equals(
                            newSkillName,
                            ignoreCase = true
                        )
                    }

                    if (existingSkill != null) {

                        if (!selectedSkills.contains(existingSkill.skill_id)) {
                            selectedSkills.add(existingSkill.skill_id)
                        }

                        customSkill = ""
                        message = "${existingSkill.skill_name} selected."

                        return@Button
                    }

                    scope.launch {

                        isAddingSkill = true
                        message = ""

                        try {

                            val newSkill = NewSkill(
                                skill_name = newSkillName
                            )

                            val createdSkill = supabase
                                .from("skills")
                                .insert(newSkill) {
                                    select()
                                }
                                .decodeSingle<Skill>()

                            skills = skills + createdSkill

                            selectedSkills.add(createdSkill.skill_id)

                            customSkill = ""

                            message = "${createdSkill.skill_name} added."

                        } catch (e: Exception) {

                            message =
                                "${e::class.simpleName}: ${e.message}"

                        } finally {

                            isAddingSkill = false
                        }
                    }
                },
                enabled = !isAddingSkill && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {

                if (isAddingSkill) {
                    CircularProgressIndicator()
                } else {
                    Text("Add skill")
                }
            }

            Button(
                onClick = onBack,
                enabled = !isSaving && !isAddingSkill,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Back")
            }

            Button(
                onClick = {

                    scope.launch {

                        isSaving = true
                        message = ""

                        try {

                            val currentUser =
                                supabase.auth.currentUserOrNull()

                            if (currentUser == null) {
                                message = "You are not logged in."
                                return@launch
                            }

                            selectedSkills.forEach { skillId ->

                                val userSkill = UserSkill(
                                    user_id = currentUser.id,
                                    skill_id = skillId,
                                    skill_type = "KNOWN",
                                    experience_level = null
                                )

                                supabase
                                    .from("user_skills")
                                    .insert(userSkill)
                            }

                            onNext()

                        } catch (e: Exception) {

                            message =
                                "${e::class.simpleName}: ${e.message}"

                        } finally {

                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving && !isAddingSkill,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {

                if (isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Next")
                }
            }
        }

        if (message.isNotEmpty()) {

            Text(
                text = message,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}