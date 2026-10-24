package com.example.skilllinkapp.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillLinkDao {

    // Roadmaps

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRoadmap(roadmap: CachedRoadmapEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRoadmaps(roadmaps: List<CachedRoadmapEntity>)

    @Query("DELETE FROM cached_roadmaps WHERE userId = :userId")
    suspend fun deleteRoadmapsForUser(userId: String)

    @Query(
        """
        SELECT * FROM cached_roadmaps
        WHERE userId = :userId
        ORDER BY cachedAt DESC
        """
    )
    fun observeRoadmaps(
        userId: String
    ): Flow<List<CachedRoadmapEntity>>

    @Query(
        """
        SELECT * FROM cached_roadmaps
        WHERE roadmapId = :roadmapId AND userId = :userId
        LIMIT 1
        """
    )
    suspend fun getRoadmap(
        roadmapId: String,
        userId: String
    ): CachedRoadmapEntity?

    // Lesson progress

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: CachedProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgressRecords(
        progress: List<CachedProgressEntity>
    )

    @Query(
        """
        SELECT * FROM cached_progress
        WHERE roadmapId = :roadmapId AND userId = :userId
        ORDER BY stepOrder
        """
    )
    fun observeProgress(
        roadmapId: String,
        userId: String
    ): Flow<List<CachedProgressEntity>>

    @Query(
        """
        SELECT * FROM cached_progress
        WHERE roadmapId = :roadmapId AND userId = :userId
        ORDER BY stepOrder
        """
    )
    suspend fun getProgress(
        roadmapId: String,
        userId: String
    ): List<CachedProgressEntity>

    @Query(
        """
    SELECT * FROM cached_progress
    WHERE needsSync = 1 AND userId = :userId
    ORDER BY updatedAt
    """
    )
    suspend fun getPendingProgress(
        userId: String
    ): List<CachedProgressEntity>

    @Query(
        """
    UPDATE cached_progress
    SET needsSync = 0
    WHERE userId = :userId
      AND roadmapId = :roadmapId
      AND stepOrder = :stepOrder
      AND updatedAt = :expectedUpdatedAt
    """
    )
    suspend fun markProgressSynced(
        userId: String,
        roadmapId: String,
        stepOrder: Int,
        expectedUpdatedAt: Long
    )

    @Query(
        """
        SELECT COUNT(*) FROM cached_progress
        WHERE needsSync = 1
        """
    )
    suspend fun countPendingProgress(): Int
}

@Database(
    entities = [
        CachedRoadmapEntity::class,
        CachedProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SkillLinkDatabase : RoomDatabase() {

    abstract fun skillLinkDao(): SkillLinkDao

    companion object {

        @Volatile
        private var INSTANCE: SkillLinkDatabase? = null

        fun getInstance(context: Context): SkillLinkDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SkillLinkDatabase::class.java,
                    "skilllink_database"
                ).build().also { database ->
                    INSTANCE = database
                }
            }
        }
    }
}