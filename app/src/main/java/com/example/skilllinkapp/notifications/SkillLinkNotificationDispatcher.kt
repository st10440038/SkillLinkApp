package com.example.skilllinkapp.notifications

import android.content.Context
import android.util.Log

object SkillLinkNotificationDispatcher {

    suspend fun send(
        context: Context,
        userId: String,
        title: String,
        message: String,
        notificationType: String,
        preventDuplicates: Boolean = false
    ) {
        if (preventDuplicates) {
            try {
                if (
                    NotificationRepository.hasNotification(
                        userId = userId,
                        title = title,
                        notificationType = notificationType
                    )
                ) {
                    return
                }
            } catch (e: Exception) {
                Log.w(
                    "SkillLinkNotification",
                    "Could not check for an existing notification.",
                    e
                )
            }
        }

        try {
            NotificationRepository.createNotification(
                userId = userId,
                title = title,
                message = message,
                notificationType = notificationType
            )
        } catch (e: Exception) {
            Log.e(
                "SkillLinkNotification",
                "Could not save notification history.",
                e
            )
        }

        NotificationEventBus.publish(
            title = title,
            message = message
        )

        SkillLinkNotificationHelper.showNotification(
            context = context,
            title = title,
            body = message
        )
    }

    fun recordLearningActivity(context: Context) {
        context.getSharedPreferences(
            "skilllink_preferences",
            Context.MODE_PRIVATE
        ).edit()
            .putLong(
                LAST_LEARNING_ACTIVITY,
                System.currentTimeMillis()
            )
            .apply()
    }

    const val LAST_LEARNING_ACTIVITY =
        "last_learning_activity"

    const val LAST_WEEKLY_REMINDER =
        "last_weekly_reminder_at"
}