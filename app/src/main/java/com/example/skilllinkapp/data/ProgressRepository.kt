
package com.example.skilllinkapp.data

import android.util.Log
import com.example.skilllinkapp.data.local.CachedProgressEntity
import com.example.skilllinkapp.data.local.SkillLinkDao
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ProgressRecord(
    val progress_id: String? = null,
    val roadmap_id: String,
    val user_id: String,
    val step_title: String,
    val step_description: String? = null,
    val step_order: Int,
    val is_completed: Boolean = false,
    val completed_at: String? = null,
)

object ProgressRepository {

    private var dao: SkillLinkDao? = null

    fun initialize(skillLinkDao: SkillLinkDao) {
        dao = skillLinkDao
    }

    private fun localDao(): SkillLinkDao =
        checkNotNull(dao) {
            "ProgressRepository has not been initialized."
        }

    /**
     * Save locally first. The app can therefore retain progress
     * even when Supabase cannot be reached.
     */
    suspend fun saveProgress(
        roadmapId: String,
        userId: String,
        stepTitle: String,
        stepDescription: String?,
        stepOrder: Int,
        isCompleted: Boolean
    ) {
        val database = localDao()

        val existingLocal = database.getProgress(
            roadmapId = roadmapId,
            userId = userId
        ).firstOrNull { it.stepOrder == stepOrder }

        val completedAt = when {
            !isCompleted -> null

            existingLocal?.isCompleted == true ->
                existingLocal.completedAt ?: Instant.now().toString()

            else -> Instant.now().toString()
        }

        val localRecord = CachedProgressEntity(
            userId = userId,
            roadmapId = roadmapId,
            stepOrder = stepOrder,
            stepTitle = stepTitle,
            stepDescription = stepDescription,
            isCompleted = isCompleted,
            completedAt = completedAt,
            needsSync = true,
            updatedAt = System.currentTimeMillis()
        )

        // Local persistence happens before any network operation.
        database.saveProgress(localRecord)

        // Attempt immediate sync. If it fails, needsSync stays true.
        try {
            syncOne(database, localRecord)
        } catch (e: Exception) {
            Log.w(
                "ProgressRepository",
                "Progress is saved locally; synchronization is pending.",
                e
            )
        }
    }

    /**
     * Load remote progress when possible, but never replace an
     * unsynchronized local record with an older server record.
     */
    suspend fun getProgress(
        roadmapId: String,
        userId: String
    ): List<ProgressRecord> {
        val database = localDao()

        return try {
            val remoteRecords = supabase
                .from("progress")
                .select {
                    filter {
                        eq("roadmap_id", roadmapId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<ProgressRecord>()

            val cached = database.getProgress(
                roadmapId = roadmapId,
                userId = userId
            )

            val pendingOrders = cached
                .filter { it.needsSync }
                .map { it.stepOrder }
                .toSet()

            val recordsToCache = remoteRecords
                .filterNot { it.step_order in pendingOrders }
                .map { record ->
                    CachedProgressEntity(
                        userId = record.user_id,
                        roadmapId = record.roadmap_id,
                        stepOrder = record.step_order,
                        stepTitle = record.step_title,
                        stepDescription = record.step_description,
                        isCompleted = record.is_completed,
                        completedAt = record.completed_at,
                        needsSync = false
                    )
                }

            if (recordsToCache.isNotEmpty()) {
                database.saveProgressRecords(recordsToCache)
            }

            // Retry unsynchronized changes for this account.
            syncPendingProgress(userId)

            // Room remains the source displayed to the UI.
            database.getProgress(roadmapId, userId).map {
                it.toProgressRecord()
            }
        } catch (e: Exception) {
            Log.w(
                "ProgressRepository",
                "Server unavailable; returning locally cached progress.",
                e
            )

            database.getProgress(roadmapId, userId).map {
                it.toProgressRecord()
            }
        }
    }

    /**
     * Retry all pending progress for the signed-in user.
     * Failed records remain pending for the next attempt.
     */
    suspend fun syncPendingProgress(userId: String) {
        val database = localDao()
        val pending = database.getPendingProgress(userId)

        for (progress in pending) {
            try {
                syncOne(database, progress)
            } catch (e: Exception) {
                Log.w(
                    "ProgressRepository",
                    "Could not synchronize roadmap " +
                            "${progress.roadmapId}, step ${progress.stepOrder}.",
                    e
                )
            }
        }
    }

    /**
     * Upload a local record, then mark it synchronized only if
     * the local version has not changed during the upload.
     */
    private suspend fun syncOne(
        database: SkillLinkDao,
        progress: CachedProgressEntity
    ) {
        val existing = supabase
            .from("progress")
            .select {
                filter {
                    eq("roadmap_id", progress.roadmapId)
                    eq("user_id", progress.userId)
                    eq("step_order", progress.stepOrder)
                }
            }
            .decodeList<ProgressRecord>()

        val completedAt = if (progress.isCompleted) {
            progress.completedAt ?: Instant.now().toString()
        } else {
            null
        }

        if (existing.isEmpty()) {
            supabase
                .from("progress")
                .insert(
                    ProgressRecord(
                        roadmap_id = progress.roadmapId,
                        user_id = progress.userId,
                        step_title = progress.stepTitle,
                        step_description = progress.stepDescription,
                        step_order = progress.stepOrder,
                        is_completed = progress.isCompleted,
                        completed_at = completedAt
                    )
                )
        } else {
            supabase
                .from("progress")
                .update(
                    {
                        set("step_title", progress.stepTitle)
                        set("step_description", progress.stepDescription)
                        set("is_completed", progress.isCompleted)
                        set("completed_at", completedAt)
                    }
                ) {
                    filter {
                        eq("roadmap_id", progress.roadmapId)
                        eq("user_id", progress.userId)
                        eq("step_order", progress.stepOrder)
                    }
                }
        }

        // Only mark this exact local version as synchronized.
        // If the user changed it during upload, it stays pending.
        database.markProgressSynced(
            userId = progress.userId,
            roadmapId = progress.roadmapId,
            stepOrder = progress.stepOrder,
            expectedUpdatedAt = progress.updatedAt
        )
    }

    suspend fun updateLastAccessed(
        roadmapId: String,
        userId: String
    ) {
        supabase
            .from("roadmaps")
            .update(
                {
                    set("last_accessed", Instant.now().toString())
                }
            ) {
                filter {
                    eq("roadmap_id", roadmapId)
                    eq("user_id", userId)
                }
            }
    }

    private fun CachedProgressEntity.toProgressRecord() =
        ProgressRecord(
            roadmap_id = roadmapId,
            user_id = userId,
            step_title = stepTitle,
            step_description = stepDescription,
            step_order = stepOrder,
            is_completed = isCompleted,
            completed_at = completedAt
        )

    suspend fun getCachedProgress(
        roadmapId: String,
        userId: String
    ): List<ProgressRecord> {
        return localDao()
            .getProgress(roadmapId, userId)
            .map { it.toProgressRecord() }
    }
}
