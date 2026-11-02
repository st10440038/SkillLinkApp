package com.example.skilllinkapp.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    @SerialName("notification_id")
    val notificationId: String,

    @SerialName("user_id")
    val userId: String,

    val title: String,

    val message: String,

    @SerialName("notification_type")
    val notificationType: String,

    @SerialName("is_read")
    val isRead: Boolean,

    @SerialName("created_at")
    val createdAt: String
)