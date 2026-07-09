package com.example.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotificationBus {
    data class NewCardExtractedEvent(
        val amount: Int,
        val walletType: String,
        val recipientPhone: String,
        val cardDetails: String,
        val isAutoSent: Boolean
    )

    private val _newCardExtractedEvents = MutableSharedFlow<NewCardExtractedEvent>(extraBufferCapacity = 64)
    val newCardExtractedEvents = _newCardExtractedEvents.asSharedFlow()

    fun emitEvent(amount: Int, walletType: String, recipientPhone: String, cardDetails: String, isAutoSent: Boolean) {
        _newCardExtractedEvents.tryEmit(
            NewCardExtractedEvent(amount, walletType, recipientPhone, cardDetails, isAutoSent)
        )
    }
}
