package com.example.skilllinkapp.data

import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.from

@Serializable
data class SkillRecord(
    val skill_id: String,
    val skill_name: String,
    val category_id: String? = null,
    val description: String? = null,
    val difficulty: String? = null
)

@Serializable
private data class NewSkillRecord(
    val skill_name: String,
    val category_id: String? = null,
    val description: String? = null,
    val difficulty: String? = null
)

@Serializable
private data class UserSkillRecord(
    val user_skill_id: String? = null,
    val user_id: String,
    val skill_id: String,
    val skill_type: String,
    val experience_level: String? = null
)

object SkillRepository {

    suspend fun saveKnownSkill(
        userId: String,
        skillName: String
    ): SkillRecord {

        val cleanedName = skillName.trim()

        require(cleanedName.isNotEmpty()) {
            "Skill name cannot be empty."
        }

        /*
         * 1. Check whether this skill already exists.
         */
        val existingSkills = supabase
            .from("skills")
            .select {
                filter {
                    ilike("skill_name", cleanedName)
                }
            }
            .decodeList<SkillRecord>()

        val skill = if (existingSkills.isNotEmpty()) {

            existingSkills.first()

        } else {

            /*
             * 2. If it doesn't exist, create it.
             */
            supabase
                .from("skills")
                .insert(
                    NewSkillRecord(
                        skill_name = cleanedName
                    )
                ) {
                    select()
                }
                .decodeSingle<SkillRecord>()
        }

        /*
         * 3. Check whether this user already has
         *    this skill as a KNOWN skill.
         */
        val existingUserSkills = supabase
            .from("user_skills")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("skill_id", skill.skill_id)
                    eq("skill_type", "KNOWN")
                }
            }
            .decodeList<UserSkillRecord>()

        /*
         * 4. Only create the user_skills record
         *    if it doesn't already exist.
         */
        if (existingUserSkills.isEmpty()) {

            supabase
                .from("user_skills")
                .insert(
                    UserSkillRecord(
                        user_id = userId,
                        skill_id = skill.skill_id,
                        skill_type = "KNOWN"
                    )
                )
        }

        return skill
    }
}