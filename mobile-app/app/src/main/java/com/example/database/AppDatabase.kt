package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.models.Card
import com.example.models.Transaction
import com.example.models.PendingApproval
import com.example.models.Deposit
import com.example.models.CustomerMapping

@Dao
interface CustomerMappingDao {
    @Query("SELECT * FROM customer_mappings ORDER BY id DESC")
    fun getAllMappings(): Flow<List<CustomerMapping>>

    @Query("SELECT * FROM customer_mappings WHERE customerUniqueId = :uniqueId LIMIT 1")
    suspend fun getMappingByUniqueId(uniqueId: String): CustomerMapping?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: CustomerMapping): Long

    @Query("DELETE FROM customer_mappings WHERE id = :id")
    suspend fun deleteMapping(id: Int)

    @Query("DELETE FROM customer_mappings")
    suspend fun deleteAllMappings()
}

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE category = :category AND used = 0 LIMIT 1")
    suspend fun getUnusedCardByCategory(category: Int): Card?

    @Query("UPDATE cards SET used = 1 WHERE id = :id")
    suspend fun markCardAsUsed(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<Card>)

    @Query("SELECT COUNT(*) FROM cards WHERE category = :category AND used = 0")
    fun getUnusedCountByCategory(category: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE used = 0")
    fun getUnusedCardsCount(): Flow<Int>

    @Query("SELECT * FROM cards ORDER BY id DESC")
    fun getAllCards(): Flow<List<Card>>

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCard(id: Int)

    @Query("DELETE FROM cards WHERE category = :category")
    suspend fun deleteCardsByCategory(category: Int)

    @Query("DELETE FROM cards")
    suspend fun deleteAllCards()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

@Dao
interface PendingApprovalDao {
    @Query("SELECT * FROM pending_approvals ORDER BY createdAt DESC")
    fun getAllPendingApprovals(): Flow<List<PendingApproval>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingApproval(pending: PendingApproval): Long

    @Query("SELECT * FROM pending_approvals WHERE id = :id")
    suspend fun getPendingApprovalById(id: Int): PendingApproval?

    @Query("UPDATE pending_approvals SET phone = :phone WHERE id = :id")
    suspend fun updatePendingApprovalPhone(id: Int, phone: String)

    @Query("DELETE FROM pending_approvals WHERE id = :id")
    suspend fun deletePendingApproval(id: Int)

    @Query("DELETE FROM pending_approvals")
    suspend fun deleteAllPendingApprovals()
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits ORDER BY createdAt DESC")
    fun getAllDeposits(): Flow<List<Deposit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: Deposit): Long

    @Query("UPDATE deposits SET isShared = :isShared, cardDetails = :cardDetails WHERE id = :id")
    suspend fun updateDepositSharing(id: Int, isShared: Boolean, cardDetails: String)

    @Query("DELETE FROM deposits")
    suspend fun deleteAllDeposits()
}

@Database(entities = [Card::class, Transaction::class, PendingApproval::class, Deposit::class, CustomerMapping::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun transactionDao(): TransactionDao
    abstract fun pendingApprovalDao(): PendingApprovalDao
    abstract fun depositDao(): DepositDao
    abstract fun customerMappingDao(): CustomerMappingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dahsha_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
