package com.example.skilllinkapp.notifications

import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object NotificationRepository {

    suspend fun getNotifications(
        userId: String
    ): List<Notification> {
        return supabase
            .from("notifications")
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Notification>()
    }

    suspend fun markAsRead(
        notificationId: String,
        userId: String
    ) {
        supabase
            .from("notifications")
            .update({
                set("is_read", true)
            }) {
                filter {
                    eq("notification_id", notificationId)
                    eq("user_id", userId)
                }
            }
    }

    suspend fun markAllAsRead(userId: String) {
        supabase
            .from("notifications")
            .update({
                set("is_read", true)
            }) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
    }

    suspend fun deleteNotification(
        notificationId: String,
        userId: String
    ) {
        val deletedNotifications = supabase
            .from("notifications")
            .delete {
                select()

                filter {
                    eq("notification_id", notificationId)
                    eq("user_id", userId)
                }
            }
            .decodeList<Notification>()

        if (deletedNotifications.isEmpty()) {
            throw IllegalStateException(
                "No notification was deleted. Check the notification ID and Supabase permissions."
            )
        }
    }

    suspend fun hasNotification(
        userId: String,
        title: String,
        notificationType: String
    ): Boolean {
        return supabase
            .from("notifications")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("title", title)
                    eq("notification_type", notificationType)
                }
            }
            .decodeList<Notification>()
            .isNotEmpty()
    }

    suspend fun createNotification(
        userId: String,
        title: String,
        message: String,
        notificationType: String
    ) {
        supabase
            .from("notifications")
            .insert(
                buildJsonObject {
                    put("user_id", userId)
                    put("title", title)
                    put("message", message)
                    put("notification_type", notificationType)
                    put("is_read", false)
                }
            )
    }
}