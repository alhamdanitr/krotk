package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,
    val amount: Int,
    val cardCode: String,
    val walletType: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val syncStatus: String = "PENDING_SYNC",
    val version: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

