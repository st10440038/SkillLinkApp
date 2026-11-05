
package com.example.skilllinkapp.notifications

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.LanguageSupport
import com.example.skilllinkapp.data.NotificationTranslationCache
import com.example.skilllinkapp.data.supabase
import com.example.skilllinkapp.network.GeminiService
import com.example.skilllinkapp.network.NotificationTranslationInput
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onUnreadCountChanged: (Int) -> Unit
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var deletingNotificationIds by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    var notificationToDelete by remember {
        mutableStateOf<Notification?>(null)
    }

    var notifications by remember {
        mutableStateOf<List<Notification>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isMarkingAllRead by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // Stores translated notification titles and messages.
    var translatedNotifications by remember {
        mutableStateOf<Map<String, Pair<String, String>>>(emptyMap())
    }

    // Observe the selected application language.
    val selectedLanguage = LanguageSupport.currentLanguage.value

    // Local translations for the notification screen interface.
    val uiTexts = when (selectedLanguage) {

        LanguageSupport.ZULU -> mapOf(
            "notifications" to "Izaziso",
            "read_all" to "Maka konke njengokufundiwe",
            "new" to "OKUSHA",
            "delete_notification" to "Susa isaziso",
            "delete_question" to "Susa isaziso?",
            "delete_confirmation" to "Uqinisekile ukuthi ufuna ukususa lesi saziso unomphela? Lesi senzo ngeke sihlehliswe.",
            "delete" to "Susa",
            "cancel" to "Khansela",
            "retry" to "Zama futhi",
            "no_notifications" to "Awunazo izaziso okwamanje.",
            "load_error" to "Ayikwazanga ukulayisha izaziso. Sicela uzame futhi.",
            "mark_all_error" to "Ayikwazanga ukumaka zonke izaziso njengezifundiwe.",
            "mark_read_error" to "Ayikwazanga ukumaka isaziso njengesifundiwe.",
            "delete_error" to "Ayikwazanga ukususa isaziso. Sicela uzame futhi."
        )

        LanguageSupport.AFRIKAANS -> mapOf(
            "notifications" to "Kennisgewings",
            "read_all" to "Merk alles as gelees",
            "new" to "NUUT",
            "delete_notification" to "Verwyder kennisgewing",
            "delete_question" to "Verwyder kennisgewing?",
            "delete_confirmation" to "Is jy seker jy wil hierdie kennisgewing permanent verwyder? Hierdie aksie kan nie ongedaan gemaak word nie.",
            "delete" to "Verwyder",
            "cancel" to "Kanselleer",
            "retry" to "Probeer weer",
            "no_notifications" to "Jy het nog geen kennisgewings nie.",
            "load_error" to "Kon nie kennisgewings laai nie. Probeer asseblief weer.",
            "mark_all_error" to "Kon nie alle kennisgewings as gelees merk nie.",
            "mark_read_error" to "Kon nie die kennisgewing as gelees merk nie.",
            "delete_error" to "Kon nie die kennisgewing verwyder nie. Probeer asseblief weer."
        )

        else -> mapOf(
            "notifications" to "Notifications",
            "read_all" to "Read all",
            "new" to "NEW",
            "delete_notification" to "Delete notification",
            "delete_question" to "Delete notification?",
            "delete_confirmation" to "Are you sure you want to permanently delete this notification? This action cannot be undone.",
            "delete" to "Delete",
            "cancel" to "Cancel",
            "retry" to "Retry",
            "no_notifications" to "You don't have any notifications yet.",
            "load_error" to "Unable to load notifications. Please try again.",
            "mark_all_error" to "Unable to mark all notifications as read.",
            "mark_read_error" to "Could not mark notification as read.",
            "delete_error" to "Unable to delete notification. Please try again."
        )
    }

    // Prepare notifications for batch translation.
    val notificationTranslationInputs = remember(notifications) {
        notifications.map { notification ->
            NotificationTranslationInput(
                id = notification.notificationId,
                title = notification.title,
                message = notification.message
            )
        }
    }

    suspend fun loadNotifications() {

        val user = supabase.auth.currentUserOrNull()

        if (user == null) {
            notifications = emptyList()
            isLoading = false
            onUnreadCountChanged(0)
            return
        }

        try {

            notifications =
                NotificationRepository.getNotifications(user.id)

            onUnreadCountChanged(
                notifications.count { !it.isRead }
            )

            errorMessage = null

        } catch (e: Exception) {

            errorMessage = uiTexts["load_error"]!!

        } finally {

            isLoading = false
        }
    }

    // Load notifications when the screen opens.
    LaunchedEffect(Unit) {
        loadNotifications()
    }

    // Refresh when a new notification arrives.
    LaunchedEffect(Unit) {
        NotificationEventBus.events.collect {
            loadNotifications()
        }
    }

    // Translate all uncached notifications in one Gemini request.
    LaunchedEffect(notificationTranslationInputs, selectedLanguage) {

        translatedNotifications = emptyMap()

        val targetLanguage = when (selectedLanguage) {
            LanguageSupport.ZULU -> "isiZulu"
            LanguageSupport.AFRIKAANS -> "Afrikaans"
            else -> "English"
        }

        // English is the original notification language.
        if (targetLanguage == "English") {
            return@LaunchedEffect
        }

        val cachedTranslations =
            mutableMapOf<String, Pair<String, String>>()

        val notificationsToTranslate =
            mutableListOf<NotificationTranslationInput>()

        // Retrieve cached translations first.
        for (notification in notificationTranslationInputs) {

            val cachedTranslation = NotificationTranslationCache.get(
                context = context,
                notificationId = notification.id,
                language = selectedLanguage
            )

            if (cachedTranslation != null) {

                cachedTranslations[notification.id] = cachedTranslation

            } else {

                notificationsToTranslate.add(notification)
            }
        }

        // Display cached translations immediately.
        translatedNotifications = cachedTranslations

        // Translate all uncached notifications together.
        if (notificationsToTranslate.isNotEmpty()) {

            try {

                val newTranslations =
                    GeminiService.translateNotifications(
                        notifications = notificationsToTranslate,
                        targetLanguage = targetLanguage
                    )

                // Save each translation locally.
                newTranslations.forEach { (notificationId, translation) ->

                    NotificationTranslationCache.save(
                        context = context,
                        notificationId = notificationId,
                        language = selectedLanguage,
                        title = translation.first,
                        message = translation.second
                    )
                }

                // Combine cached and newly generated translations.
                translatedNotifications =
                    cachedTranslations + newTranslations

            } catch (exception: CancellationException) {

                throw exception

            } catch (exception: Exception) {

                android.util.Log.e(
                    "NotificationTranslation",
                    "Translation failed: ${exception.message}",
                    exception
                )

                Toast.makeText(
                    context,
                    "Translation failed. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {

            TopAppBar(
                modifier = Modifier.height(68.dp),
                windowInsets = WindowInsets(0.dp),

                title = {
                    Text(
                        text = uiTexts["notifications"]!!,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when {

                isLoading -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),

                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(errorMessage!!)

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    loadNotifications()
                                }
                            }
                        ) {
                            Text(uiTexts["retry"]!!)
                        }
                    }
                }

                notifications.isEmpty() -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiTexts["no_notifications"]!!,
                            color = Color.Gray
                        )
                    }
                }

                else -> {

                    val unreadCount =
                        notifications.count { !it.isRead }

                    // Mark all notifications as read.
                    if (unreadCount > 0) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),

                            horizontalArrangement = Arrangement.End
                        ) {

                            TextButton(
                                enabled = !isMarkingAllRead,

                                onClick = {
                                    scope.launch {

                                        val user =
                                            supabase.auth.currentUserOrNull()

                                        if (user != null) {

                                            isMarkingAllRead = true

                                            try {

                                                NotificationRepository
                                                    .markAllAsRead(user.id)

                                                notifications =
                                                    notifications.map {
                                                        it.copy(isRead = true)
                                                    }

                                                onUnreadCountChanged(0)

                                                errorMessage = null

                                            } catch (e: Exception) {

                                                Toast.makeText(
                                                    context,
                                                    uiTexts["mark_all_error"]!!,
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            } finally {

                                                isMarkingAllRead = false
                                            }
                                        }
                                    }
                                }

                            ) {

                                if (isMarkingAllRead) {

                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )

                                } else {

                                    Text(uiTexts["read_all"]!!)
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),

                        contentPadding = PaddingValues(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)

                    ) {

                        items(
                            items = notifications,
                            key = { it.notificationId }

                        ) { notification ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        if (!notification.isRead) {

                                            scope.launch {

                                                try {

                                                    NotificationRepository
                                                        .markAsRead(
                                                            notificationId =
                                                                notification.notificationId,

                                                            userId =
                                                                notification.userId
                                                        )

                                                    notifications =
                                                        notifications.map {

                                                            if (
                                                                it.notificationId ==
                                                                notification.notificationId
                                                            ) {

                                                                it.copy(isRead = true)

                                                            } else {

                                                                it
                                                            }
                                                        }

                                                    onUnreadCountChanged(
                                                        notifications.count {
                                                            !it.isRead
                                                        }
                                                    )

                                                } catch (e: Exception) {

                                                    Toast.makeText(
                                                        context,
                                                        uiTexts["mark_read_error"]!!,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    },

                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 4.dp
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (notification.isRead) MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                )
                            ) {

                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        // Display translated title.
                                        Text(
                                            text = translatedNotifications[
                                                notification.notificationId
                                            ]?.first ?: notification.title,

                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (!notification.isRead) {

                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = MaterialTheme.shapes.small

                                            ) {

                                                Text(
                                                    text = uiTexts["new"]!!,
                                                    color = Color.White,

                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 4.dp
                                                    ),

                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }

                                        IconButton(
                                            enabled =
                                                notification.notificationId !in deletingNotificationIds,

                                            onClick = {
                                                notificationToDelete = notification
                                            }

                                        ) {

                                            if (
                                                notification.notificationId in deletingNotificationIds
                                            ) {

                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp
                                                )

                                            } else {

                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = uiTexts["delete_notification"]!!,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(8.dp)
                                    )

                                    // Display translated message.
                                    Text(
                                        text = translatedNotifications[
                                            notification.notificationId
                                        ]?.second ?: notification.message,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(
                                        modifier = Modifier.height(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete notification confirmation dialog.
    notificationToDelete?.let { notification ->

        AlertDialog(

            onDismissRequest = {
                notificationToDelete = null
            },

            title = {
                Text(uiTexts["delete_question"]!!)
            },

            text = {
                Text(uiTexts["delete_confirmation"]!!)
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        scope.launch {

                            val notificationId =
                                notification.notificationId

                            deletingNotificationIds =
                                deletingNotificationIds + notificationId

                            try {

                                NotificationRepository.deleteNotification(
                                    notificationId = notificationId,
                                    userId = notification.userId
                                )

                                val updatedNotifications =
                                    notifications.filterNot {
                                        it.notificationId == notificationId
                                    }

                                notifications = updatedNotifications

                                // Remove its displayed translation.
                                translatedNotifications =
                                    translatedNotifications - notificationId

                                onUnreadCountChanged(
                                    updatedNotifications.count {
                                        !it.isRead
                                    }
                                )

                                notificationToDelete = null

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    uiTexts["delete_error"]!!,
                                    Toast.LENGTH_SHORT
                                ).show()

                            } finally {

                                deletingNotificationIds =
                                    deletingNotificationIds - notificationId
                            }
                        }
                    }

                ) {

                    Text(
                        uiTexts["delete"]!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        notificationToDelete = null
                    }
                ) {
                    Text(uiTexts["cancel"]!!)
                }
            }
        )
    }
}
