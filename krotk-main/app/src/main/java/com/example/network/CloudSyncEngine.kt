package com.example.network

import android.content.Context
import android.util.Log
import com.example.database.AppDatabase
import com.example.database.CardRepository
import com.example.security.DeviceSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CloudSyncEngine private constructor(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val sharedPrefs = context.getSharedPreferences("cloud_sync_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "CloudSyncEngine"
        private const val BASE_URL = "https://kayan-licensing-server.onrender.com/"
        private const val PREF_MIGRATION_DONE = "first_time_migration_done"

        @Volatile
        private var INSTANCE: CloudSyncEngine? = null

        fun getInstance(context: Context): CloudSyncEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = CloudSyncEngine(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Reads all legacy offline records from Room, transmits them via HTTP POST to the cloud backend
     * which loads them securely into PostgreSQL for this specific License Key tenant.
     * Marks all local data as SYNCED upon receipt of successful migration acknowledgment.
     */
    fun performFirstTimeMigration(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val repo = CardRepository(context)
        val licenseKey = repo.activeSerialKey.value
        if (licenseKey.isEmpty()) {
            onComplete(false, "لم يتم تفعيل التطبيق بعد")
            return
        }

        if (sharedPrefs.getBoolean(PREF_MIGRATION_DONE, false)) {
            onComplete(true, "تم الترحيل مسبقاً")
            return
        }

        scope.launch {
            try {
                val deviceId = DeviceSecurity.getSecureDeviceId(context)
                
                // Read everything from database
                val transactions = db.transactionDao().getAllTransactions().first()
                val deposits = db.depositDao().getAllDeposits().first()
                val customers = db.distributorDao().getAllCustomers().first()
                val distTransactions = db.distributorDao().getAllTransactions().first()
                val expenses = db.distributorDao().getAllExpenses().first()
                val capitals = db.distributorDao().getAllCapitals().first()

                // If completely empty, skip migration
                if (transactions.isEmpty() && deposits.isEmpty() && customers.isEmpty() && 
                    distTransactions.isEmpty() && expenses.isEmpty() && capitals.isEmpty()) {
                    sharedPrefs.edit().putBoolean(PREF_MIGRATION_DONE, true).apply()
                    onComplete(true, "لا توجد بيانات محلية للترحيل")
                    return@launch
                }

                // Build JSON payload
                val payload = JSONObject().apply {
                    put("licenseKey", licenseKey)
                    put("deviceId", deviceId)

                    // Transactions array
                    val txArray = JSONArray()
                    transactions.forEach { t ->
                        txArray.put(JSONObject().apply {
                            put("uuid", t.uuid)
                            put("phone", t.phone)
                            put("amount", t.amount)
                            put("cardCode", t.cardCode)
                            put("walletType", t.walletType)
                            put("createdAt", t.createdAt)
                            put("updatedAt", t.updatedAt)
                            put("version", t.version)
                        })
                    }
                    put("transactions", txArray)

                    // Deposits array
                    val depArray = JSONArray()
                    deposits.forEach { d ->
                        depArray.put(JSONObject().apply {
                            put("uuid", d.uuid)
                            put("phone", d.phone)
                            put("amount", d.amount)
                            put("walletType", d.walletType)
                            put("isShared", d.isShared)
                            put("cardDetails", d.cardDetails)
                            put("createdAt", d.createdAt)
                            put("updatedAt", d.updatedAt)
                            put("version", d.version)
                        })
                    }
                    put("deposits", depArray)

                    // Customers array
                    val custArray = JSONArray()
                    customers.forEach { c ->
                        custArray.put(JSONObject().apply {
                            put("id", c.id)
                            put("name", c.name)
                            put("totalSales", c.totalSales)
                            put("totalPayments", c.totalPayments)
                            put("currentBalance", c.currentBalance)
                            put("createdAt", c.createdAt)
                            put("updatedAt", c.updatedAt)
                            put("version", c.version)
                        })
                    }
                    put("distributorCustomers", custArray)

                    // Distributor Transactions array
                    val dtxArray = JSONArray()
                    distTransactions.forEach { t ->
                        dtxArray.put(JSONObject().apply {
                            put("id", t.id)
                            put("customerId", t.customerId)
                            put("date", t.date)
                            put("type", t.type)
                            put("amount", t.amount)
                            put("notes", t.notes)
                            put("updatedAt", t.updatedAt)
                            put("version", t.version)
                        })
                    }
                    put("distributorTransactions", dtxArray)

                    // Expenses
                    val expArray = JSONArray()
                    expenses.forEach { e ->
                        expArray.put(JSONObject().apply {
                            put("id", e.id)
                            put("category", e.category)
                            put("amount", e.amount)
                            put("description", e.description)
                            put("date", e.date)
                            put("updatedAt", e.updatedAt)
                            put("version", e.version)
                        })
                    }
                    put("distributorExpenses", expArray)

                    // Capitals
                    val capArray = JSONArray()
                    capitals.forEach { c ->
                        capArray.put(JSONObject().apply {
                            put("id", c.id)
                            put("type", c.type)
                            put("amount", c.amount)
                            put("description", c.description)
                            put("date", c.date)
                            put("updatedAt", c.updatedAt)
                            put("version", c.version)
                        })
                    }
                    put("distributorCapitals", capArray)
                }

                // Send to migration endpoint
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = payload.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(BASE_URL + "api/v1/sync/migrate")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val respStr = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        // Mark all as synced locally
                        transactions.forEach { t -> db.transactionDao().markAsSynced(t.uuid) }
                        deposits.forEach { d -> db.depositDao().markAsSynced(d.uuid) }
                        customers.forEach { c -> db.distributorDao().markCustomerAsSynced(c.id) }
                        distTransactions.forEach { t -> db.distributorDao().markTransactionAsSynced(t.id) }
                        expenses.forEach { e -> db.distributorDao().markExpenseAsSynced(e.id) }
                        capitals.forEach { c -> db.distributorDao().markCapitalAsSynced(c.id) }

                        sharedPrefs.edit().putBoolean(PREF_MIGRATION_DONE, true).apply()
                        Log.d(TAG, "First time cloud migration completed successfully!")
                        onComplete(true, "تم ترحيل البيانات السحابية وتأمينها بنجاح!")
                    } else {
                        val json = JSONObject(respStr)
                        val msg = json.optString("message", "خطأ غير معروف في خادم الترحيل")
                        Log.e(TAG, "First time cloud migration failed: $msg")
                        onComplete(false, msg)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during cloud migration", e)
                onComplete(false, "فشل الاتصال بالإنترنت: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Background incremental sync. Searches for PENDING_SYNC operations in all tables
     * and uploads them to the server.
     */
    fun performIncrementalSync(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val repo = CardRepository(context)
        val licenseKey = repo.activeSerialKey.value
        if (licenseKey.isEmpty()) {
            return
        }

        scope.launch {
            try {
                val deviceId = DeviceSecurity.getSecureDeviceId(context)

                // Get only unsynced operations
                val transactions = db.transactionDao().getUnsyncedTransactions()
                val deposits = db.depositDao().getUnsyncedDeposits()
                val customers = db.distributorDao().getUnsyncedCustomers()
                val distTransactions = db.distributorDao().getUnsyncedTransactions()
                val expenses = db.distributorDao().getUnsyncedExpenses()
                val capitals = db.distributorDao().getUnsyncedCapitals()

                // If absolutely nothing is unsynced, return
                if (transactions.isEmpty() && deposits.isEmpty() && customers.isEmpty() && 
                    distTransactions.isEmpty() && expenses.isEmpty() && capitals.isEmpty()) {
                    onComplete(true, "البيانات متزامنة بالكامل")
                    return@launch
                }

                val payload = JSONObject().apply {
                    put("licenseKey", licenseKey)
                    put("deviceId", deviceId)

                    // Transactions
                    val txArray = JSONArray()
                    transactions.forEach { t ->
                        txArray.put(JSONObject().apply {
                            put("uuid", t.uuid)
                            put("phone", t.phone)
                            put("amount", t.amount)
                            put("cardCode", t.cardCode)
                            put("walletType", t.walletType)
                            put("createdAt", t.createdAt)
                            put("updatedAt", t.updatedAt)
                            put("version", t.version)
                        })
                    }
                    put("transactions", txArray)

                    // Deposits
                    val depArray = JSONArray()
                    deposits.forEach { d ->
                        depArray.put(JSONObject().apply {
                            put("uuid", d.uuid)
                            put("phone", d.phone)
                            put("amount", d.amount)
                            put("walletType", d.walletType)
                            put("isShared", d.isShared)
                            put("cardDetails", d.cardDetails)
                            put("createdAt", d.createdAt)
                            put("updatedAt", d.updatedAt)
                            put("version", d.version)
                        })
                    }
                    put("deposits", depArray)

                    // Customers
                    val custArray = JSONArray()
                    customers.forEach { c ->
                        custArray.put(JSONObject().apply {
                            put("id", c.id)
                            put("name", c.name)
                            put("totalSales", c.totalSales)
                            put("totalPayments", c.totalPayments)
                            put("currentBalance", c.currentBalance)
                            put("createdAt", c.createdAt)
                            put("updatedAt", c.updatedAt)
                            put("version", c.version)
                        })
                    }
                    put("distributorCustomers", custArray)

                    // Distributor Transactions
                    val dtxArray = JSONArray()
                    distTransactions.forEach { t ->
                        dtxArray.put(JSONObject().apply {
                            put("id", t.id)
                            put("customerId", t.customerId)
                            put("date", t.date)
                            put("type", t.type)
                            put("amount", t.amount)
                            put("notes", t.notes)
                            put("updatedAt", t.updatedAt)
                            put("version", t.version)
                        })
                    }
                    put("distributorTransactions", dtxArray)

                    // Expenses
                    val expArray = JSONArray()
                    expenses.forEach { e ->
                        expArray.put(JSONObject().apply {
                            put("id", e.id)
                            put("category", e.category)
                            put("amount", e.amount)
                            put("description", e.description)
                            put("date", e.date)
                            put("updatedAt", e.updatedAt)
                            put("version", e.version)
                        })
                    }
                    put("distributorExpenses", expArray)

                    // Capitals
                    val capArray = JSONArray()
                    capitals.forEach { c ->
                        capArray.put(JSONObject().apply {
                            put("id", c.id)
                            put("type", c.type)
                            put("amount", c.amount)
                            put("description", c.description)
                            put("date", c.date)
                            put("updatedAt", c.updatedAt)
                            put("version", c.version)
                        })
                    }
                    put("distributorCapitals", capArray)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = payload.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(BASE_URL + "api/v1/sync/upload")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val respStr = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        // Mark as synced
                        transactions.forEach { t -> db.transactionDao().markAsSynced(t.uuid) }
                        deposits.forEach { d -> db.depositDao().markAsSynced(d.uuid) }
                        customers.forEach { c -> db.distributorDao().markCustomerAsSynced(c.id) }
                        distTransactions.forEach { t -> db.distributorDao().markTransactionAsSynced(t.id) }
                        expenses.forEach { e -> db.distributorDao().markExpenseAsSynced(e.id) }
                        capitals.forEach { c -> db.distributorDao().markCapitalAsSynced(c.id) }

                        Log.d(TAG, "Incremental sync uploaded successfully!")
                        onComplete(true, "تمت المزامنة بنجاح")
                    } else {
                        Log.e(TAG, "Incremental sync failed: $respStr")
                        onComplete(false, "فشلت المزامنة التلقائية")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Incremental sync exception", e)
                onComplete(false, "فشل الاتصال بالإنترنت")
            }
        }
    }

    /**
     * Download tenant changes from the cloud. Merges them into local Room database securely.
     */
    fun downloadServerData(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val repo = CardRepository(context)
        val licenseKey = repo.activeSerialKey.value
        if (licenseKey.isEmpty()) {
            onComplete(false, "مفتاح الترخيص غير متوفر")
            return
        }

        scope.launch {
            try {
                val deviceId = DeviceSecurity.getSecureDeviceId(context)
                val url = "$BASE_URL/api/v1/sync/download?licenseKey=$licenseKey&deviceId=$deviceId"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    val respStr = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val json = JSONObject(respStr)
                        
                        // Parse and store Transactions
                        val txs = json.optJSONArray("transactions")
                        if (txs != null) {
                            for (i in 0 until txs.length()) {
                                val item = txs.getJSONObject(i)
                                val t = com.example.models.Transaction(
                                    phone = item.getString("phone"),
                                    amount = item.getInt("amount"),
                                    cardCode = item.getString("cardCode"),
                                    walletType = item.optString("walletType", ""),
                                    createdAt = item.getLong("createdAt"),
                                    uuid = item.getString("uuid"),
                                    syncStatus = "SYNCED",
                                    version = item.optInt("version", 1),
                                    updatedAt = item.optLong("updatedAt", item.getLong("createdAt"))
                                )
                                db.transactionDao().insertTransaction(t)
                            }
                        }

                        // Parse and store Deposits
                        val dps = json.optJSONArray("deposits")
                        if (dps != null) {
                            for (i in 0 until dps.length()) {
                                val item = dps.getJSONObject(i)
                                val d = com.example.models.Deposit(
                                    phone = item.getString("phone"),
                                    amount = item.getInt("amount"),
                                    walletType = item.getString("walletType"),
                                    isShared = item.getBoolean("isShared"),
                                    cardDetails = item.optString("cardDetails", ""),
                                    createdAt = item.getLong("createdAt"),
                                    uuid = item.getString("uuid"),
                                    syncStatus = "SYNCED",
                                    version = item.optInt("version", 1),
                                    updatedAt = item.optLong("updatedAt", item.getLong("createdAt"))
                                )
                                db.depositDao().insertDeposit(d)
                            }
                        }

                        // Distributor Customers
                        val custs = json.optJSONArray("distributorCustomers")
                        if (custs != null) {
                            for (i in 0 until custs.length()) {
                                val item = custs.getJSONObject(i)
                                val c = com.example.models.DistributorCustomer(
                                    id = item.getString("id"),
                                    name = item.getString("name"),
                                    totalSales = item.getDouble("totalSales"),
                                    totalPayments = item.getDouble("totalPayments"),
                                    currentBalance = item.getDouble("currentBalance"),
                                    createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                                    syncStatus = "SYNCED",
                                    version = item.optInt("version", 1),
                                    updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
                                )
                                db.distributorDao().insertCustomer(c)
                            }
                        }

                        // Distributor Transactions
                        val dtxs = json.optJSONArray("distributorTransactions")
                        if (dtxs != null) {
                            for (i in 0 until dtxs.length()) {
                                val item = dtxs.getJSONObject(i)
                                val t = com.example.models.DistributorTransaction(
                                    id = item.getString("id"),
                                    customerId = item.getString("customerId"),
                                    date = item.getLong("date"),
                                    type = item.getString("type"),
                                    amount = item.getDouble("amount"),
                                    notes = item.optString("notes", ""),
                                    syncStatus = "SYNCED",
                                    version = item.optInt("version", 1),
                                    updatedAt = item.optLong("updatedAt", item.getLong("date"))
                                )
                                db.distributorDao().insertTransaction(t)
                            }
                        }

                        // Distributor Expenses
                        val exps = json.optJSONArray("distributorExpenses")
                        if (exps != null) {
                            for (i in 0 until exps.length()) {
                                val item = exps.getJSONObject(i)
                                val e = com.example.models.DistributorExpense(
                                    id = item.getString("id"),
                                    category = item.getString("category"),
                                    amount = item.getDouble("amount"),
                                    description = item.optString("description", ""),
                                    date = item.getLong("date"),
                                    syncStatus = "SYNCED",
                                    version = item.optInt("version", 1),
                                    updatedAt = item.optLong("updatedAt", item.getLong("date"))
                                )
                                db.distributorDao().insertExpense(e)
                            }
                        }

                        // Distributor Capitals
                        val caps = json.optJSONArray("distributorCapitals")
                        if (caps != null) {
                            for (i in 0 until caps.length()) {
                                val item = caps.getJSONObject(i)
                                val c = com.example.models.DistributorCapital(
                                    id = item.getString("id"),
                                    type = item.getString("type"),
                                    amount = item.getDouble("amount"),
                                    description = item.optString("description", ""),
                                    date = item.getLong("date"),
                                    syncStatus = "SYNCED",
                                    version = item.optInt("version", 1),
                                    updatedAt = item.optLong("updatedAt", item.getLong("date"))
                                )
                                db.distributorDao().insertCapital(c)
                            }
                        }

                        onComplete(true, "تم استرداد وتحديث جميع البيانات من السحابة بنجاح! ☁️")
                    } else {
                        onComplete(false, "فشل الاتصال بالخادم لاسترداد البيانات")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "DownloadServerData Error", e)
                onComplete(false, "خطأ بالاتصال بالإنترنت: ${e.localizedMessage}")
            }
        }
    }
}
