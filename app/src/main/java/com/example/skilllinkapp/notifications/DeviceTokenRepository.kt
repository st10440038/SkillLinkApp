package com.example.skilllinkapp.notifications

import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class DeviceToken(
    @SerialName("user_id")
    val userId: String,

    @SerialName("fcm_token")
    val fcmToken: String,

    @SerialName("updated_at")
    val updatedAt: String
)

object DeviceTokenRepository {

    suspend fun registerDevice(
        userId: String,
        token: String
    ) {
        val device = DeviceToken(
            userId = userId,
            fcmToken = token,
            updatedAt = Instant.now().toString()
        )

        supabase
            .from("user_devices")
            .upsert(device) {
                onConflict = "user_id,fcm_token"
            }
    }

    suspend fun removeDevice(
        userId: String,
        token: String
    ) {
        supabase
            .from("user_devices")
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("fcm_token", token)
                }
            }
    }
}