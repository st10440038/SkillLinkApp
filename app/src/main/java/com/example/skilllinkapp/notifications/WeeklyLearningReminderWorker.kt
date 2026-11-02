package com.example.skilllinkapp.notifications

import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.workDataOf
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.auth.auth

class WeeklyLearningReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        val preferences =
            applicationContext.getSharedPreferences(
                "skilllink_preferences",
                Context.MODE_PRIVATE
            )

        if (
            !preferences.getBoolean(
                "notifications_enabled",
                false
            )
        ) {
            return Result.success()
        }

        return try {

            val user = supabase.auth.currentUserOrNull()
                ?: return Result.success()

            val now = System.currentTimeMillis()

            val sevenDays =
                7L * 24L * 60L * 60L * 1000L

            val lastActivity = preferences.getLong(
                SkillLinkNotificationDispatcher.LAST_LEARNING_ACTIVITY,
                0L
            )

            if (
                lastActivity == 0L ||
                now - lastActivity < sevenDays
            ) {
                return Result.success()
            }

            val lastReminder = preferences.getLong(
                SkillLinkNotificationDispatcher.LAST_WEEKLY_REMINDER,
                0L
            )

            if (
                lastReminder != 0L &&
                now - lastReminder < sevenDays
            ) {
                return Result.success()
            }

            SkillLinkNotificationDispatcher.send(
                context = applicationContext,
                userId = user.id,
                title = "Time to get back to learning!",
                message = "It's been a while since your last study session. Continue your roadmap and keep making progress.",
                notificationType = "learning_reminder"
            )

            preferences.edit()
                .putLong(
                    SkillLinkNotificationDispatcher.LAST_WEEKLY_REMINDER,
                    now
                )
                .apply()

            Result.success()

        } catch (e: Exception) {

            Log.e(
                "SkillLinkReminder",
                "Weekly learning reminder failed.",
                e
            )

            Result.retry()
        }
    }
}