package com.example.skilllinkapp.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SkillLinkNotificationScheduler {

    fun scheduleWeeklyReminder(context: Context) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request =
            PeriodicWorkRequestBuilder<WeeklyLearningReminderWorker>(
                1,
                TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    private const val WORK_NAME =
        "skilllink_weekly_learning_reminder"
}