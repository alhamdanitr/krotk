package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_mappings")
data class CustomerMapping(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerUniqueId: String,
    val basicPhone: String,
    val customerName: String = "",
    val walletType: String = "جيب"
)
