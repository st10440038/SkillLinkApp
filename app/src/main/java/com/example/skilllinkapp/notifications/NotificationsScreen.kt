package com.example.skilllinkapp.notifications

import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.data.supabase
import io.github.jan.supabase.auth.auth
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

            errorMessage =
                "Unable to load notifications. Please try again."

        } finally {

            isLoading = false
        }
    }

    // Load notifications when the screen opens.
    LaunchedEffect(Unit) {
        loadNotifications()
    }

    // Refresh the list when a new push arrives.
    LaunchedEffect(Unit) {

        NotificationEventBus.events.collect {

            loadNotifications()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(64.dp),

                windowInsets = WindowInsets(0.dp),

                title = {
                    Text(
                        text = "Notifications",
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

                            Text("Retry")
                        }
                    }
                }

                notifications.isEmpty() -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "You don't have any notifications yet.",
                            color = Color.Gray
                        )
                    }
                }

                else -> {

                    val unreadCount =
                        notifications.count { !it.isRead }

                    // Read all button.
                    if (unreadCount > 0) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),

                            horizontalArrangement =
                                Arrangement.End
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

                                                errorMessage =
                                                    "Unable to mark all notifications as read."

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

                                    Text("Read all")
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),

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

                                                                it.copy(
                                                                    isRead = true
                                                                )

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

                                                    errorMessage =
                                                        "Could not mark notification as read."
                                                }
                                            }
                                        }
                                    },

                                colors = CardDefaults.cardColors(

                                    containerColor =
                                        if (notification.isRead) {

                                            MaterialTheme.colorScheme.surfaceVariant

                                        } else {

                                            MaterialTheme.colorScheme.primaryContainer
                                        }
                                )

                            ) {

                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Text(
                                            text = notification.title,

                                            fontWeight = FontWeight.Bold,

                                            modifier = Modifier.weight(1f)
                                        )

                                        if (!notification.isRead) {

                                            Surface(

                                                color =
                                                    MaterialTheme.colorScheme.primary,

                                                shape =
                                                    MaterialTheme.shapes.small

                                            ) {

                                                Text(
                                                    text = "NEW",

                                                    color = Color.White,

                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 4.dp
                                                    ),

                                                    style =
                                                        MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }

                                        IconButton(
                                            enabled = notification.notificationId !in deletingNotificationIds,
                                            onClick = {
                                                notificationToDelete = notification
                                            }
                                        ) {
                                            if (notification.notificationId in deletingNotificationIds) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete notification",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(8.dp)
                                    )

                                    Text(notification.message)

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

    notificationToDelete?.let { notification ->

        AlertDialog(
            onDismissRequest = {
                notificationToDelete = null
            },
            title = {
                Text("Delete notification?")
            },
            text = {
                Text(
                    "Are you sure you want to permanently delete this notification? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val notificationId = notification.notificationId

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

                                onUnreadCountChanged(
                                    updatedNotifications.count { !it.isRead }
                                )

                                notificationToDelete = null

                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Unable to delete notification. Please try again.",
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
                        "Delete",
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
                    Text("Cancel")
                }
            }
        )
    }
}