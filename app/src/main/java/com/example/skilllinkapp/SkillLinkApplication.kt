package com.example.skilllinkapp

import com.example.skilllinkapp.notifications.SkillLinkNotificationScheduler
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.example.skilllinkapp.data.ProgressRepository
import com.example.skilllinkapp.data.local.SkillLinkDatabase
import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class SkillLinkApplication : Application() {

    val database: SkillLinkDatabase by lazy {
        SkillLinkDatabase.getInstance(this)
    }

    private val applicationScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val syncInProgress = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()

        // Initialize the local Room database
        ProgressRepository.initialize(database.skillLinkDao())

        // Create the notification channel for Firebase notifications
        createNotificationChannel()

        SkillLinkNotificationScheduler.scheduleWeeklyReminder(this)

        // Monitor internet connectivity for automatic progress synchronization
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasValidatedInternet =
                    networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    ) &&
                            networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_VALIDATED
                            )

                if (hasValidatedInternet) {
                    syncPendingProgressWhenSignedIn()
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "SkillLink Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Learning updates and alerts from SkillLink"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun syncPendingProgressWhenSignedIn() {
        if (!syncInProgress.compareAndSet(false, true)) return

        applicationScope.launch {
            try {
                val user = supabase.auth.currentUserOrNull()

                if (user != null) {
                    ProgressRepository.syncPendingProgress(user.id)
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "SkillLinkAutoSync",
                    "Automatic progress sync failed",
                    e
                )
            } finally {
                syncInProgress.set(false)
            }
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "skilllink_notifications"
    }
}
