package com.example.skilllinkapp.data.local

import androidx.room.Entity

@Entity(
    tableName = "cached_progress",
    primaryKeys = ["userId", "roadmapId", "stepOrder"]
)
data class CachedProgressEntity(
    val userId: String,
    val roadmapId: String,
    val stepOrder: Int,
    val stepTitle: String,
    val stepDescription: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: String? = null,

    // True until the change has reached Supabase.
    val needsSync: Boolean = true,

    val updatedAt: Long = System.currentTimeMillis()
)