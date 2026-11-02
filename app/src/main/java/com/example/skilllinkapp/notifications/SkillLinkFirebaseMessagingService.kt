package com.example.skilllinkapp.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SkillLinkFirebaseMessagingService :
    FirebaseMessagingService() {


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val preferences = getSharedPreferences(
            "skilllink_preferences",
            Context.MODE_PRIVATE
        )

        // Respect the user's notification setting.
        if (!preferences.getBoolean("notifications_enabled", false)) {
            return
        }

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "SkillLink"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new learning update."

        //

        // Keep the existing in-app banner.
        NotificationEventBus.publish(
            title = title,
            message = body
        )

        // Keep the existing Android system notification.
        SkillLinkNotificationHelper.showNotification(
            this,
            title,
            body
        )
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Save the latest token for registration after login.
        getSharedPreferences(
            "skilllink_preferences",
            Context.MODE_PRIVATE
        )
            .edit()
            .putString("pending_fcm_token", token)
            .apply()

        Log.d(
            "SkillLinkFCM",
            "A new FCM token was generated."
        )
    }
}