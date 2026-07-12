package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: Int,
    val code: String,
    val username: String = "",
    val password: String = "",
    val used: Boolean = false
)

