
package com.example.skilllinkapp.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import com.example.skilllinkapp.data.ProfilePictureRepository
import kotlinx.serialization.json.JsonNull
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skilllinkapp.LanguageSupport
import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private fun editProfileText(english: String): String {
    return when (LanguageSupport.currentLanguage.value) {
        LanguageSupport.AFRIKAANS -> when (english) {
            "Edit Profile" -> "Wysig profiel"
            "Back" -> "Terug"
            "Loading profile..." -> "Profiel word gelaai..."
            "Profile picture" -> "Profielfoto"
            "Full Name" -> "Volle naam"
            "Enter your full name" -> "Voer jou volle naam in"
            "Bio" -> "Biografie"
            "Tell people a little about yourself" ->
                "Vertel mense 'n bietjie van jouself"
            "City / Location" -> "Stad / Ligging"
            "e.g. Cape Town" -> "bv. Kaapstad"
            "Saving..." -> "Stoor tans..."
            "Save Changes" -> "Stoor veranderinge"
            else -> english
        }

        LanguageSupport.ZULU -> when (english) {
            "Edit Profile" -> "Hlela iphrofayela"
            "Back" -> "Emuva"
            "Loading profile..." -> "Iphrofayela iyalayishwa..."
            "Profile picture" -> "Isithombe sephrofayela"
            "Full Name" -> "Igama eligcwele"
            "Enter your full name" -> "Faka igama lakho eligcwele"
            "Bio" -> "Mayelana nawe"
            "Tell people a little about yourself" ->
                "Tshela abantu okuncane ngawe"
            "City / Location" -> "Idolobha / Indawo"
            "e.g. Cape Town" -> "isb. eKapa"
            "Saving..." -> "Kuyagcinwa..."
            "Save Changes" -> "Gcina izinguquko"
            else -> english
        }

        else -> english
    }
}

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedImageBitmap by remember {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }

    var imageBytes by remember {
        mutableStateOf<ByteArray?>(null)
    }

    var removeProfilePicture by remember { mutableStateOf(false) }
    var hasSavedProfilePicture by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var biography by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val user = supabase.auth.currentUserOrNull()

    val context = androidx.compose.ui.platform.LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val originalBytes = stream.readBytes()

                    val bitmap = BitmapFactory.decodeByteArray(
                        originalBytes,
                        0,
                        originalBytes.size
                    )

                    if (bitmap != null) {
                        val outputStream = java.io.ByteArrayOutputStream()

                        removeProfilePicture = false

                        bitmap.compress(
                            android.graphics.Bitmap.CompressFormat.JPEG,
                            85,
                            outputStream
                        )

                        imageBytes = outputStream.toByteArray()
                        selectedImageUri = uri
                        selectedImageBitmap = bitmap.asImageBitmap()
                    }
                }
            } catch (e: Exception) {
                println("Profile image selection error: ${e.message}")
            }
        }
    }

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        try {
            if (user != null) {

                // Load the local picture first, including when offline.
                val cachedBytes =
                    ProfilePictureRepository.loadCachedProfilePicture(
                        context = context,
                        userId = user.id
                    )

                if (cachedBytes != null) {
                    val cachedBitmap = BitmapFactory.decodeByteArray(
                        cachedBytes,
                        0,
                        cachedBytes.size
                    )

                    if (cachedBitmap != null) {
                        selectedImageBitmap = cachedBitmap.asImageBitmap()
                        hasSavedProfilePicture = true
                    }
                }

                // Then try loading the latest profile information from Supabase.
                val profile = supabase
                    .from("profiles")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<JsonObject>()
                    .firstOrNull()

                if (profile != null) {
                    fullName = profile["full_name"]
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: ""

                    biography = profile["biography"]
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: ""

                    location = profile["location"]
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: ""

                    val imagePath = profile["profile_image"]
                        ?.toString()
                        ?.removeSurrounding("\"")

                    if (!imagePath.isNullOrBlank()) {
                        val accessToken = supabase.auth.currentSessionOrNull()
                            ?.accessToken

                        if (!accessToken.isNullOrBlank()) {
                            val bytes = ProfilePictureRepository.downloadProfilePicture(
                                path = imagePath,
                                accessToken = accessToken
                            )

                            val bitmap = BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.size
                            )

                            if (bitmap != null) {
                                selectedImageBitmap = bitmap.asImageBitmap()
                                hasSavedProfilePicture = true

                                ProfilePictureRepository.saveCachedProfilePicture(
                                    context = context,
                                    userId = user.id,
                                    imageBytes = bytes
                                )
                            }
                        }
                    }
                    else {
                        ProfilePictureRepository.deleteCachedProfilePicture(
                            context = context,
                            userId = user.id
                        )

                        selectedImageBitmap = null
                        hasSavedProfilePicture = false
                    }
                }
            }
        } catch (e: Exception) {
            println("Edit profile loading error: ${e.message}")
        }

        isLoading = false
    }

    val initials = fullName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = editProfileText("Back"),
                    tint = Color(0xFF7656E8)
                )
            }

            Text(
                text = editProfileText("Edit Profile"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202035)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = editProfileText("Loading profile..."),
                    color = Color(0xFF85849A),
                    fontSize = 15.sp
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7656E8))
                        .clickable {
                            photoPicker.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageBitmap != null) {
                        Image(
                            bitmap = selectedImageBitmap!!,
                            contentDescription = "Selected profile picture",
                            modifier = Modifier.fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else if (initials.isNotEmpty()) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = editProfileText("Profile picture"),
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap to choose a profile picture",
                    color = Color(0xFF7656E8),
                    fontSize = 13.sp,
                    modifier = Modifier.clickable {
                        photoPicker.launch("image/*")
                    }
                )

                if (selectedImageBitmap != null || hasSavedProfilePicture) {
                    TextButton(
                        onClick = {
                            selectedImageBitmap = null
                            imageBytes = null
                            removeProfilePicture = true
                            hasSavedProfilePicture = false
                        }
                    ) {
                        Text(
                            text = "Remove profile picture",
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = editProfileText("Full Name"),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202035)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(editProfileText("Enter your full name"))
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = editProfileText("Bio"),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202035)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = biography,
                onValueChange = { biography = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(
                        editProfileText(
                            "Tell people a little about yourself"
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = editProfileText("City / Location"),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202035)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(editProfileText("e.g. Cape Town"))
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (user != null && !isSaving) {
                        isSaving = true

                        coroutineScope.launch {
                            try {
                                val accessToken = supabase.auth.currentSessionOrNull()
                                    ?.accessToken
                                    ?: throw Exception("Your session has expired. Please log in again.")

                                var uploadedPath: String? = null

                                if (imageBytes != null) {
                                    uploadedPath =
                                        ProfilePictureRepository.uploadProfilePicture(
                                            userId = user.id,
                                            accessToken = accessToken,
                                            imageBytes = imageBytes!!
                                        )
                                } else if (removeProfilePicture) {
                                    ProfilePictureRepository.deleteProfilePicture(
                                        accessToken = accessToken,
                                        userId = user.id
                                    )
                                }

                                supabase
                                    .from("profiles")
                                    .update(
                                        buildJsonObject {
                                            put("full_name", fullName.trim())
                                            put("biography", biography.trim())
                                            put("location", location.trim())

                                            if (uploadedPath != null) {
                                                put("profile_image", uploadedPath)
                                            } else if (removeProfilePicture) {
                                                put("profile_image", JsonNull)
                                            }
                                        }
                                    ) {
                                        filter {
                                            eq("user_id", user.id)
                                        }
                                    }

                                if (imageBytes != null) {
                                    ProfilePictureRepository.saveCachedProfilePicture(
                                        context = context,
                                        userId = user.id,
                                        imageBytes = imageBytes!!
                                    )
                                } else if (removeProfilePicture) {
                                    ProfilePictureRepository.deleteCachedProfilePicture(
                                        context = context,
                                        userId = user.id
                                    )
                                }

                                onSave()

                            } catch (e: Exception) {
                                println("Edit profile save error: ${e.message}")
                                android.widget.Toast.makeText(
                                    context,
                                    "Unable to save profile: ${e.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7656E8)
                )
            ) {
                Text(
                    text = if (isSaving) {
                        editProfileText("Saving...")
                    } else {
                        editProfileText("Save Changes")
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
