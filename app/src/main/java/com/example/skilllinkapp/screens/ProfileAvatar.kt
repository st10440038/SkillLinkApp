package com.example.skilllinkapp.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skilllinkapp.data.ProfilePictureRepository
import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ProfileAvatar(
    userId: String?,
    fallbackText: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    backgroundColor: Color = Color(0xFF7656E8),
    textColor: Color = Color.White
) {
    val context = LocalContext.current

    var profileBitmap by remember(userId) {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }

    var isLoading by remember(userId) {
        mutableStateOf(true)
    }

    LaunchedEffect(userId) {
        profileBitmap = null
        isLoading = true

        try {
            if (userId.isNullOrBlank()) {
                return@LaunchedEffect
            }

            // 1. Load the saved local image first.
            val cachedBytes =
                ProfilePictureRepository.loadCachedProfilePicture(
                    context = context,
                    userId = userId
                )

            if (cachedBytes != null) {
                val cachedBitmap = BitmapFactory.decodeByteArray(
                    cachedBytes,
                    0,
                    cachedBytes.size
                )

                profileBitmap = cachedBitmap?.asImageBitmap()
            }

            // 2. Try refreshing the image from Supabase.
            // If the phone is offline, the cached image remains visible.
            val accessToken = supabase.auth.currentSessionOrNull()
                ?.accessToken

            if (accessToken.isNullOrBlank()) {
                return@LaunchedEffect
            }

            val profile = supabase
                .from("profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<JsonObject>()
                .firstOrNull()

            if (profile != null) {
                val imagePath = profile["profile_image"]
                    ?.jsonPrimitive
                    ?.contentOrNull

                if (imagePath.isNullOrBlank()) {
                    // The profile has no picture anymore.
                    ProfilePictureRepository.deleteCachedProfilePicture(
                        context = context,
                        userId = userId
                    )

                    profileBitmap = null
                } else {
                    val imageBytes =
                        ProfilePictureRepository.downloadProfilePicture(
                            path = imagePath,
                            accessToken = accessToken
                        )

                    // Save the latest Supabase image locally.
                    ProfilePictureRepository.saveCachedProfilePicture(
                        context = context,
                        userId = userId,
                        imageBytes = imageBytes
                    )

                    val bitmap = BitmapFactory.decodeByteArray(
                        imageBytes,
                        0,
                        imageBytes.size
                    )

                    profileBitmap = bitmap?.asImageBitmap()
                }
            }
        } catch (e: Exception) {
            println("Profile avatar loading error: ${e.message}")
            // Do not clear the cached image if the network request fails.
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            profileBitmap != null -> {
                Image(
                    bitmap = profileBitmap!!,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = textColor,
                    strokeWidth = 2.dp
                )
            }

            else -> {
                Text(
                    text = fallbackText,
                    color = textColor,
                    fontSize = 21.sp
                )
            }
        }
    }
}