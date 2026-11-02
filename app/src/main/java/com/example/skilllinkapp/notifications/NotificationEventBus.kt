package com.example.skilllinkapp.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class NotificationEvent(
    val title: String,
    val message: String
)

object NotificationEventBus {

    private val _events = MutableSharedFlow<NotificationEvent>(
        extraBufferCapacity = 10
    )

    val events = _events.asSharedFlow()

    fun publish(title: String, message: String) {
        _events.tryEmit(
            NotificationEvent(
                title = title,
                message = message
            )
        )
    }
}