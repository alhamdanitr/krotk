package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_mikrotik_cards")
data class GeneratedMikrotikCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: Int,
    val pin: String,
    val username: String,
    val password: String,
    val printed: Boolean = false,
    val transferred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
