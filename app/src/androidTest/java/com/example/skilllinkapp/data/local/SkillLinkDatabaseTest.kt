
package com.example.skilllinkapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SkillLinkDatabaseTest {

    private lateinit var database: SkillLinkDatabase
    private lateinit var dao: SkillLinkDao

    // Create a fresh in-memory database before each test.
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            SkillLinkDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = database.skillLinkDao()
    }

    // Close the database after each test.
    @After
    fun tearDown() {
        database.close()
    }

    // Test that saved lesson progress can be retrieved.
    @Test
    fun savedProgressCanBeRetrieved() = runBlocking {
        val progress = CachedProgressEntity(
            userId = "user1",
            roadmapId = "roadmap1",
            stepOrder = 1,
            stepTitle = "Introduction",
            isCompleted = true,
            needsSync = false
        )

        dao.saveProgress(progress)

        val result = dao.getProgress("roadmap1", "user1")

        assertEquals(1, result.size)
        assertEquals("Introduction", result[0].stepTitle)
        assertEquals(true, result[0].isCompleted)
    }

    // Test that saving an existing step updates its progress.
    @Test
    fun savingSameStepUpdatesExistingProgress() = runBlocking {
        val firstProgress = CachedProgressEntity(
            userId = "user1",
            roadmapId = "roadmap1",
            stepOrder = 1,
            stepTitle = "Introduction",
            isCompleted = false
        )

        val updatedProgress = firstProgress.copy(
            isCompleted = true,
            updatedAt = firstProgress.updatedAt + 1
        )

        dao.saveProgress(firstProgress)
        dao.saveProgress(updatedProgress)

        val result = dao.getProgress("roadmap1", "user1")

        assertEquals(1, result.size)
        assertEquals(true, result[0].isCompleted)
    }

    // Test that synchronized progress is removed from pending sync.
    @Test
    fun synchronizedProgressIsRemovedFromPendingList() = runBlocking {
        val progress = CachedProgressEntity(
            userId = "user1",
            roadmapId = "roadmap1",
            stepOrder = 1,
            stepTitle = "Introduction",
            isCompleted = true,
            needsSync = true,
            updatedAt = 1000L
        )

        dao.saveProgress(progress)

        assertEquals(1, dao.getPendingProgress("user1").size)

        dao.markProgressSynced(
            userId = "user1",
            roadmapId = "roadmap1",
            stepOrder = 1,
            expectedUpdatedAt = 1000L
        )

        assertEquals(0, dao.getPendingProgress("user1").size)
    }
}
