package com.example.skilllinkapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_roadmaps")
data class CachedRoadmapEntity(
    @PrimaryKey
    val roadmapId: String,

    val userId: String,
    val title: String,
    val roadmapJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)