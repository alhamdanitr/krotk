package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "distributor_customers")
data class DistributorCustomer(
    @PrimaryKey val id: String, // UUID as String
    val name: String,
    val totalSales: Double = 0.0,
    val totalPayments: Double = 0.0,
    val currentBalance: Double = 0.0, // Sales - Payments (Outstanding Debt they owe)
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING_SYNC",
    val version: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "distributor_transactions")
data class DistributorTransaction(
    @PrimaryKey val id: String, // UUID
    val customerId: String,
    val date: Long = System.currentTimeMillis(),
    val type: String, // "sale" or "payment"
    val amount: Double,
    val notes: String = "",
    val syncStatus: String = "PENDING_SYNC",
    val version: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "distributor_expenses")
data class DistributorExpense(
    @PrimaryKey val id: String, // UUID
    val category: String, // fuel, rent, salaries, maintenance, other
    val amount: Double,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING_SYNC",
    val version: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "distributor_capitals")
data class DistributorCapital(
    @PrimaryKey val id: String, // UUID
    val type: String, // "deposit" or "withdraw"
    val amount: Double,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING_SYNC",
    val version: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)
