package com.example.skilllinkapp.data

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class ProgressRecord(
    val progress_id: String? = null,
    val roadmap_id: String,
    val user_id: String,
    val step_title: String,
    val step_description: String? = null,
    val step_order: Int,
    val is_completed: Boolean = false,
    val completed_at: String? = null
)

object ProgressRepository {

    suspend fun saveProgress(
        roadmapId: String,
        userId: String,
        stepTitle: String,
        stepDescription: String?,
        stepOrder: Int,
        isCompleted: Boolean
    ) {
        val existing = supabase
            .from("progress")
            .select {
                filter {
                    eq("roadmap_id", roadmapId)
                    eq("step_order", stepOrder)
                    eq("user_id", userId)
                }
            }
            .decodeList<ProgressRecord>()

        if (existing.isEmpty()) {

            supabase
                .from("progress")
                .insert(
                    ProgressRecord(
                        roadmap_id = roadmapId,
                        user_id = userId,
                        step_title = stepTitle,
                        step_description = stepDescription,
                        step_order = stepOrder,
                        is_completed = isCompleted,
                        completed_at = if (isCompleted) {
                            java.time.Instant.now().toString()
                        } else {
                            null
                        }
                    )
                )

        } else {

            val progressId = existing.first().progress_id

            if (progressId != null) {

                supabase
                    .from("progress")
                    .update(
                        {
                            set("is_completed", isCompleted)
                            set(
                                "completed_at",
                                if (isCompleted) {
                                    java.time.Instant.now().toString()
                                } else {
                                    null
                                }
                            )
                        }
                    ) {
                        filter {
                            eq("progress_id", progressId)
                        }
                    }
            }
        }
    }

    suspend fun getProgress(
        roadmapId: String,
        userId: String
    ): List<ProgressRecord> {

        return supabase
            .from("progress")
            .select {
                filter {
                    eq("roadmap_id", roadmapId)
                    eq("user_id", userId)
                }
            }
            .decodeList<ProgressRecord>()
    }

    suspend fun updateLastAccessed(
        roadmapId: String,
        userId: String
    ) {
        supabase
            .from("roadmaps")
            .update(
                {
                    set(
                        "last_accessed",
                        java.time.Instant.now().toString()
                    )
                }
            ) {
                filter {
                    eq("roadmap_id", roadmapId)
                    eq("user_id", userId)
                }
            }
    }
}