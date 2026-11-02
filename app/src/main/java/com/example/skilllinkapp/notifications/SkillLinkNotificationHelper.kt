
package com.example.skilllinkapp.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.skilllinkapp.MainActivity
import com.example.skilllinkapp.R
import com.example.skilllinkapp.SkillLinkApplication

object SkillLinkNotificationHelper {

    fun showNotification(
        context: Context,
        title: String,
        body: String
    ) {
        val preferences = context.getSharedPreferences(
            "skilllink_preferences",
            Context.MODE_PRIVATE
        )

        if (!preferences.getBoolean("notifications_enabled", false)) {
            return
        }

        if (
            android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            SkillLinkApplication.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(body)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}
