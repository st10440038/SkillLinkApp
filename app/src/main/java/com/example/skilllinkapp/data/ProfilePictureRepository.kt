package com.example.skilllinkapp.data

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ProfilePictureRepository {

    private const val SUPABASE_URL =
        "https://kaehvrkpcmrxrrhkzdgf.supabase.co"

    private const val SUPABASE_KEY =
        "sb_publishable_8sTlz0kg6lTEaQ2fGgRZMg_72ghlOTv"

    private const val BUCKET = "profile-pictures"

    private val client = HttpClient(Android)

    // Local persistent cache

    private fun getCachedImageFile(
        context: Context,
        userId: String
    ): File {
        val directory = File(context.filesDir, "profile_pictures")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val safeUserId = userId.replace(Regex("[^a-zA-Z0-9_-]"), "_")

        return File(directory, "profile_$safeUserId.jpg")
    }

    suspend fun saveCachedProfilePicture(
        context: Context,
        userId: String,
        imageBytes: ByteArray
    ) {
        withContext(Dispatchers.IO) {
            require(imageBytes.isNotEmpty()) {
                "The profile picture is empty."
            }

            val targetFile = getCachedImageFile(context, userId)
            val temporaryFile = File(targetFile.parentFile, "${targetFile.name}.tmp")

            temporaryFile.writeBytes(imageBytes)

            if (!temporaryFile.renameTo(targetFile)) {
                temporaryFile.copyTo(targetFile, overwrite = true)
                temporaryFile.delete()
            }
        }
    }

    suspend fun loadCachedProfilePicture(
        context: Context,
        userId: String
    ): ByteArray? {
        return withContext(Dispatchers.IO) {
            val file = getCachedImageFile(context, userId)

            if (file.exists() && file.length() > 0) {
                file.readBytes()
            } else {
                null
            }
        }
    }

    suspend fun deleteCachedProfilePicture(
        context: Context,
        userId: String
    ) {
        withContext(Dispatchers.IO) {
            val file = getCachedImageFile(context, userId)

            if (file.exists()) {
                file.delete()
            }
        }
    }

    // Supabase Storage

    suspend fun uploadProfilePicture(
        userId: String,
        accessToken: String,
        imageBytes: ByteArray
    ): String {
        require(imageBytes.isNotEmpty()) {
            "The selected image is empty."
        }

        val path = "$userId/profile.jpg"

        val response = client.post(
            "$SUPABASE_URL/storage/v1/object/$BUCKET/$path"
        ) {
            header("apikey", SUPABASE_KEY)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header("x-upsert", "true")
            contentType(ContentType.Image.JPEG)
            setBody(imageBytes)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Image upload failed: ${response.bodyAsText()}")
        }

        return path
    }

    suspend fun downloadProfilePicture(
        path: String,
        accessToken: String
    ): ByteArray {
        val response = client.get(
            "$SUPABASE_URL/storage/v1/object/$BUCKET/$path"
        ) {
            header("apikey", SUPABASE_KEY)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Image download failed: ${response.bodyAsText()}")
        }

        return response.bodyAsBytes()
    }

    suspend fun deleteProfilePicture(
        accessToken: String,
        userId: String
    ) {
        val path = "$userId/profile.jpg"

        val response = client.delete(
            "$SUPABASE_URL/storage/v1/object/$BUCKET"
        ) {
            header("apikey", SUPABASE_KEY)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody("""{"prefixes":["$path"]}""")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Image deletion failed: ${response.bodyAsText()}")
        }
    }
}