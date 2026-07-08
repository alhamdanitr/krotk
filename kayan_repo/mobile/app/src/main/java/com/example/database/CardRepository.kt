package com.example.database

import android.content.Context
import android.content.SharedPreferences
import com.example.models.Card
import com.example.models.Transaction
import com.example.models.PendingApproval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class CardRepository(val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val cardDao = database.cardDao()
    private val transactionDao = database.transactionDao()
    private val pendingApprovalDao = database.pendingApprovalDao()
    private val depositDao = database.depositDao()
    private val customerMappingDao = database.customerMappingDao()
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("dahsha_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_SERVICE_ENABLED = "service_enabled"
        private const val PREF_APP_PASSWORD = "app_password"
        private const val PREF_JEEB_ENABLED = "jeeb_enabled"
        private const val PREF_JAWALI_ENABLED = "jawali_enabled"
        private const val PREF_KURAIMI_ENABLED = "kuraimi_enabled"
        private const val PREF_HASEB_ENABLED = "haseb_enabled"
        private const val PREF_ONECASH_ENABLED = "onecash_enabled"
        private const val PREF_MFLOOS_ENABLED = "mfloos_enabled"
        private const val PREF_GEMINI_API_KEY = "gemini_api_key"
        private const val PREF_ACCOUNT_CODE_SMS_TEMPLATE = "account_code_sms_template"
        private const val PREF_ACCOUNT_CODE_SMS_PHONE = "account_code_sms_phone"
        private const val PREF_AUTO_SEND_SMS = "auto_send_sms"
        private const val PREF_NOTIFICATION_CLICK_COMPOSE = "notification_click_compose"
        private const val PREF_APPROVED_SMS_TEMPLATES = "approved_sms_templates"
        private const val PREF_IS_DARK_THEME = "is_dark_theme"
        private const val PREF_NETWORK_NAME = "network_name"
        private const val PREF_GENERAL_SMS_TEMPLATE = "general_sms_template"
        private const val PREF_CARD_FORMAT_MODE = "card_format_mode"
        private const val PREF_CUSTOM_CATEGORIES = "custom_categories"
        private const val PREF_IS_ACTIVATED = "is_activated"
    }

    // Card format mode ("user_pass" or "user_only")
    private val _cardFormatMode = MutableStateFlow(sharedPrefs.getString(PREF_CARD_FORMAT_MODE, "user_pass") ?: "user_pass")
    val cardFormatMode: StateFlow<String> = _cardFormatMode

    fun setCardFormatMode(mode: String) {
        sharedPrefs.edit().putString(PREF_CARD_FORMAT_MODE, mode).apply()
        _cardFormatMode.value = mode
    }

    // Dynamic Categories management
    private val defaultCategories = setOf("100", "200", "250", "300", "500")
    private val _categories = MutableStateFlow<List<Int>>(
        (sharedPrefs.getStringSet(PREF_CUSTOM_CATEGORIES, defaultCategories) ?: defaultCategories)
            .mapNotNull { it.toIntOrNull() }
            .sorted()
    )
    val categories: StateFlow<List<Int>> = _categories

    fun addCategory(cat: Int) {
        val currentSet = (sharedPrefs.getStringSet(PREF_CUSTOM_CATEGORIES, defaultCategories) ?: defaultCategories).toMutableSet()
        currentSet.add(cat.toString())
        sharedPrefs.edit().putStringSet(PREF_CUSTOM_CATEGORIES, currentSet).apply()
        _categories.value = currentSet.mapNotNull { it.toIntOrNull() }.sorted()
    }

    suspend fun removeCategory(cat: Int) = withContext(Dispatchers.IO) {
        cardDao.deleteCardsByCategory(cat)
        val currentSet = (sharedPrefs.getStringSet(PREF_CUSTOM_CATEGORIES, defaultCategories) ?: defaultCategories).toMutableSet()
        currentSet.remove(cat.toString())
        sharedPrefs.edit().putStringSet(PREF_CUSTOM_CATEGORIES, currentSet).apply()
        _categories.value = currentSet.mapNotNull { it.toIntOrNull() }.sorted()
    }

    // Theme preference (defaulting to dark mode)
    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean(PREF_IS_DARK_THEME, true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    // Activation Status
    private val _isActivated = MutableStateFlow(sharedPrefs.getBoolean(PREF_IS_ACTIVATED, false))
    val isActivated: StateFlow<Boolean> = _isActivated

    fun setActivated(activated: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_IS_ACTIVATED, activated).apply()
        _isActivated.value = activated
    }

    fun setDarkTheme(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_IS_DARK_THEME, enabled).apply()
        _isDarkTheme.value = enabled
    }

    // Network name preference
    private val _networkName = MutableStateFlow(sharedPrefs.getString(PREF_NETWORK_NAME, "كروت الدحشة") ?: "كروت الدحشة")
    val networkName: StateFlow<String> = _networkName

    fun setNetworkName(name: String) {
        sharedPrefs.edit().putString(PREF_NETWORK_NAME, name).apply()
        _networkName.value = name
    }

    // General customizable SMS template
    private val _generalSmsTemplate = MutableStateFlow(
        sharedPrefs.getString(
            PREF_GENERAL_SMS_TEMPLATE,
            "تفاصيل كرت الشحن الخاص بك (فئة %category ر.ي) هي:\n%details"
        ) ?: "تفاصيل كرت الشحن الخاص بك (فئة %category ر.ي) هي:\n%details"
    )
    val generalSmsTemplate: StateFlow<String> = _generalSmsTemplate

    fun setGeneralSmsTemplate(template: String) {
        sharedPrefs.edit().putString(PREF_GENERAL_SMS_TEMPLATE, template).apply()
        _generalSmsTemplate.value = template
    }

    // Auto send SMS directly (via SmsManager)
    private val _isAutoSendSmsEnabled = MutableStateFlow(sharedPrefs.getBoolean(PREF_AUTO_SEND_SMS, false))
    val isAutoSendSmsEnabled: StateFlow<Boolean> = _isAutoSendSmsEnabled

    fun setAutoSendSmsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_AUTO_SEND_SMS, enabled).apply()
        _isAutoSendSmsEnabled.value = enabled
    }

    // Notification click action: if true, compose/share via SMS composer; if false, just standard info log notification
    private val _isNotificationClickComposeEnabled = MutableStateFlow(sharedPrefs.getBoolean(PREF_NOTIFICATION_CLICK_COMPOSE, true))
    val isNotificationClickComposeEnabled: StateFlow<Boolean> = _isNotificationClickComposeEnabled

    fun setNotificationClickComposeEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_NOTIFICATION_CLICK_COMPOSE, enabled).apply()
        _isNotificationClickComposeEnabled.value = enabled
    }

    // List of custom approved message templates
    private val _approvedSmsTemplates = MutableStateFlow<List<String>>(
        sharedPrefs.getStringSet(PREF_APPROVED_SMS_TEMPLATES, emptySet())?.toList() ?: emptyList()
    )
    val approvedSmsTemplates: StateFlow<List<String>> = _approvedSmsTemplates

    fun addApprovedSmsTemplate(template: String) {
        val current = _approvedSmsTemplates.value.toMutableList()
        if (!current.contains(template)) {
            current.add(template)
            sharedPrefs.edit().putStringSet(PREF_APPROVED_SMS_TEMPLATES, current.toSet()).apply()
            _approvedSmsTemplates.value = current
        }
    }

    fun removeApprovedSmsTemplate(template: String) {
        val current = _approvedSmsTemplates.value.toMutableList()
        if (current.remove(template)) {
            sharedPrefs.edit().putStringSet(PREF_APPROVED_SMS_TEMPLATES, current.toSet()).apply()
            _approvedSmsTemplates.value = current
        }
    }

    // Service status
    private val _isServiceEnabled = MutableStateFlow(sharedPrefs.getBoolean(PREF_SERVICE_ENABLED, true))
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled

    fun setServiceEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_SERVICE_ENABLED, enabled).apply()
        _isServiceEnabled.value = enabled
    }

    // Wallet filters status
    private val _isJeebEnabled = MutableStateFlow(sharedPrefs.getBoolean(PREF_JEEB_ENABLED, true))
    val isJeebEnabled: StateFlow<Boolean> = _isJeebEnabled

    fun setJeebEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_JEEB_ENABLED, enabled).apply()
        _isJeebEnabled.value = enabled
    }

    private val _isJawaliEnabled = MutableStateFlow(sharedPrefs.getBoolean(PREF_JAWALI_ENABLED, true))
    val isJawaliEnabled: StateFlow<Boolean> = _isJawaliEnabled

    fun setJawaliEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_JAWALI_ENABLED, enabled).apply()
        _isJawaliEnabled.value = enabled
    }

    private val _isKuraimiEnabled = MutableStateFlow(sharedPrefs.getBoolean(PREF_KURAIMI_ENABLED, true))
    val isKuraimiEnabled: StateFlow<Boolean> = _isKuraimiEnabled

    fun setKuraimiEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_KURAIMI_ENABLED, enabled).apply()
        _isKuraimiEnabled.value = enabled
    }

    private val _isHasebEnabled = MutableStateFlow(sharedPrefs.getBoolean(PREF_HASEB_ENABLED, true))
    val isHasebEnabled: StateFlow<Boolean> = _isHasebEnabled

    fun setHasebEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_HASEB_ENABLED, enabled).apply()
        _isHasebEnabled.value = enabled
    }

    private val _isOneCashEnabled = MutableStateFlow(sharedPrefs.getBoolean(PREF_ONECASH_ENABLED, true))
    val isOneCashEnabled: StateFlow<Boolean> = _isOneCashEnabled

    fun setOneCashEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_ONECASH_ENABLED, enabled).apply()
        _isOneCashEnabled.value = enabled
    }

    private val _isMFloosEnabled = MutableStateFlow(sharedPrefs.getBoolean(PREF_MFLOOS_ENABLED, true))
    val isMFloosEnabled: StateFlow<Boolean> = _isMFloosEnabled

    fun setMFloosEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(PREF_MFLOOS_ENABLED, enabled).apply()
        _isMFloosEnabled.value = enabled
    }

    // Custom text and configuration for account code transactions
    private val _accountCodeSmsTemplate = MutableStateFlow(
        sharedPrefs.getString(
            PREF_ACCOUNT_CODE_SMS_TEMPLATE,
            "تم استلام دفعتك بمبلغ %amount ر.ي بنجاح.\nكود كرت الشحن الخاص بحسابك (%account) هو:\n%code"
        ) ?: "تم استلام دفعتك بمبلغ %amount ر.ي بنجاح.\nكود كرت الشحن الخاص بحسابك (%account) هو:\n%code"
    )
    val accountCodeSmsTemplate: StateFlow<String> = _accountCodeSmsTemplate

    fun setAccountCodeSmsTemplate(template: String) {
        sharedPrefs.edit().putString(PREF_ACCOUNT_CODE_SMS_TEMPLATE, template).apply()
        _accountCodeSmsTemplate.value = template
    }

    private val _accountCodeSmsPhone = MutableStateFlow(sharedPrefs.getString(PREF_ACCOUNT_CODE_SMS_PHONE, "") ?: "")
    val accountCodeSmsPhone: StateFlow<String> = _accountCodeSmsPhone

    fun setAccountCodeSmsPhone(phone: String) {
        sharedPrefs.edit().putString(PREF_ACCOUNT_CODE_SMS_PHONE, phone).apply()
        _accountCodeSmsPhone.value = phone
    }

    // Custom Gemini API Key configuration
    private val _customGeminiApiKey = MutableStateFlow(sharedPrefs.getString(PREF_GEMINI_API_KEY, "") ?: "")
    val customGeminiApiKey: StateFlow<String> = _customGeminiApiKey

    fun setCustomGeminiApiKey(apiKey: String) {
        sharedPrefs.edit().putString(PREF_GEMINI_API_KEY, apiKey).apply()
        _customGeminiApiKey.value = apiKey
    }

    fun getActiveGeminiApiKey(): String {
        val customKey = _customGeminiApiKey.value.trim()
        return if (customKey.isNotEmpty()) customKey else com.example.BuildConfig.GEMINI_API_KEY
    }

    // App Pin/Password Lock
    fun getAppPassword(): String {
        return sharedPrefs.getString(PREF_APP_PASSWORD, "PY_7MD") ?: "PY_7MD"
    }

    fun setAppPassword(password: String) {
        sharedPrefs.edit().putString(PREF_APP_PASSWORD, password).apply()
    }

    // Cards Access
    fun getUnusedCountByCategory(category: Int): Flow<Int> = cardDao.getUnusedCountByCategory(category)
    
    fun getUnusedCardsCount(): Flow<Int> = cardDao.getUnusedCardsCount()

    fun getAllCards(): Flow<List<Card>> = cardDao.getAllCards()

    suspend fun getUnusedCardByCategory(category: Int): Card? = withContext(Dispatchers.IO) {
        cardDao.getUnusedCardByCategory(category)
    }

    suspend fun markCardAsUsed(id: Int) = withContext(Dispatchers.IO) {
        cardDao.markCardAsUsed(id)
    }

    suspend fun deleteCard(id: Int) = withContext(Dispatchers.IO) {
        cardDao.deleteCard(id)
    }

    suspend fun clearAllCards() = withContext(Dispatchers.IO) {
        cardDao.deleteAllCards()
    }

    suspend fun insertCardsBulk(category: Int, codesBlock: String): Int = withContext(Dispatchers.IO) {
        val cardsToInsert = mutableListOf<Card>()
        
        // If the block contains dot separators on their own lines, handle as block mode first
        if (codesBlock.contains(Regex("(?m)^\\.\\s*$"))) {
            val rawBlocks = codesBlock.split(Regex("(?m)^\\.\\s*$"))
            for (block in rawBlocks) {
                val lines = block.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                if (lines.isEmpty()) continue
                val username = lines.getOrNull(0) ?: ""
                val password = lines.getOrNull(1) ?: ""
                if (username.isNotEmpty()) {
                    val code = if (password.isNotEmpty()) "$username / $password" else username
                    cardsToInsert.add(
                        Card(
                            category = category,
                            code = code,
                            username = username,
                            password = password,
                            used = false
                        )
                    )
                }
            }
        } else {
            // Line-by-line parser mode (splits strictly by newline)
            val lines = codesBlock.split("\n")
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty() || line == ".") continue

                var username = ""
                var password = ""

                if (line.contains("|")) {
                    val parts = line.split("|")
                    val userPart = parts.getOrNull(0)?.trim() ?: ""
                    val passPart = parts.getOrNull(1)?.trim() ?: ""

                    // Clean Arabic prefixes
                    username = userPart.replace("المستخدم:", "").replace("المستخدم", "").trim()
                    password = passPart.replace("السر:", "").replace("السر", "").trim()
                } else if (line.contains("/")) {
                    val parts = line.split("/")
                    username = parts.getOrNull(0)?.trim() ?: ""
                    password = parts.getOrNull(1)?.trim() ?: ""
                } else {
                    // Check if line matches Arabic labels like: المستخدم: XXX السر: YYY
                    val match = Regex("المستخدم[:\\s]*(\\w+).*?السر[:\\s]*(\\w+)").find(line)
                    if (match != null) {
                        username = match.groupValues[1].trim()
                        password = match.groupValues[2].trim()
                    } else {
                        // Single code
                        username = line
                        password = ""
                    }
                }

                if (username.isNotEmpty()) {
                    val code = if (password.isNotEmpty()) "$username / $password" else username
                    cardsToInsert.add(
                        Card(
                            category = category,
                            code = code,
                            username = username,
                            password = password,
                            used = false
                        )
                    )
                }
            }
        }

        if (cardsToInsert.isNotEmpty()) {
            cardDao.insertCards(cardsToInsert)
        }
        return@withContext cardsToInsert.size
    }

    suspend fun insertCardsList(cards: List<Card>): Int = withContext(Dispatchers.IO) {
        if (cards.isEmpty()) return@withContext 0
        cardDao.insertCards(cards)
        return@withContext cards.size
    }

    // Transactions Access
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insertTransaction(phone: String, amount: Int, cardCode: String, walletType: String = "") = withContext(Dispatchers.IO) {
        val transaction = Transaction(
            phone = phone,
            amount = amount,
            cardCode = cardCode,
            walletType = walletType
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun clearAllTransactions() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
    }

    // Pending Approvals Access
    fun getAllPendingApprovals(): Flow<List<PendingApproval>> = pendingApprovalDao.getAllPendingApprovals()

    suspend fun insertPendingApproval(phone: String, amount: Int, walletType: String, isAccountCode: Boolean, depositId: Int): Long = withContext(Dispatchers.IO) {
        val pending = PendingApproval(
            phone = phone,
            amount = amount,
            walletType = walletType,
            isAccountCode = isAccountCode,
            depositId = depositId
        )
        pendingApprovalDao.insertPendingApproval(pending)
    }

    suspend fun deletePendingApproval(id: Int) = withContext(Dispatchers.IO) {
        pendingApprovalDao.deletePendingApproval(id)
    }

    suspend fun updatePendingApprovalPhone(id: Int, phone: String) = withContext(Dispatchers.IO) {
        pendingApprovalDao.updatePendingApprovalPhone(id, phone)
    }

    suspend fun getPendingApproval(id: Int): PendingApproval? = withContext(Dispatchers.IO) {
        pendingApprovalDao.getPendingApprovalById(id)
    }

    suspend fun clearAllPendingApprovals() = withContext(Dispatchers.IO) {
        pendingApprovalDao.deleteAllPendingApprovals()
    }

    // Deposits Access
    fun getAllDeposits(): Flow<List<com.example.models.Deposit>> = depositDao.getAllDeposits()

    suspend fun insertDeposit(phone: String, amount: Int, walletType: String, isShared: Boolean, cardDetails: String = ""): Long = withContext(Dispatchers.IO) {
        val deposit = com.example.models.Deposit(
            phone = phone,
            amount = amount,
            walletType = walletType,
            isShared = isShared,
            cardDetails = cardDetails
        )
        depositDao.insertDeposit(deposit)
    }

    suspend fun updateDepositSharing(id: Int, isShared: Boolean, cardDetails: String) = withContext(Dispatchers.IO) {
        depositDao.updateDepositSharing(id, isShared, cardDetails)
    }

    suspend fun clearAllDeposits() = withContext(Dispatchers.IO) {
        depositDao.deleteAllDeposits()
    }

    // Customer Mappings Access
    fun getAllMappings(): Flow<List<com.example.models.CustomerMapping>> = customerMappingDao.getAllMappings()

    suspend fun insertMapping(customerUniqueId: String, basicPhone: String, customerName: String, walletType: String): Long = withContext(Dispatchers.IO) {
        val mapping = com.example.models.CustomerMapping(
            customerUniqueId = customerUniqueId,
            basicPhone = basicPhone,
            customerName = customerName,
            walletType = walletType
        )
        customerMappingDao.insertMapping(mapping)
    }

    suspend fun deleteMapping(id: Int) = withContext(Dispatchers.IO) {
        customerMappingDao.deleteMapping(id)
    }

    suspend fun getMappingByUniqueId(uniqueId: String): com.example.models.CustomerMapping? = withContext(Dispatchers.IO) {
        customerMappingDao.getMappingByUniqueId(uniqueId)
    }
}
