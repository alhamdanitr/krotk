package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.database.CardRepository
import com.example.models.Card
import com.example.models.Transaction
import com.example.models.PendingApproval
import com.example.models.CustomerMapping
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: CardRepository) : ViewModel() {

    // Simple session login status
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    // Service Enabled Status
    val isServiceEnabled: StateFlow<Boolean> = repository.isServiceEnabled
    
    // Wallet filter toggles
    val isJeebEnabled: StateFlow<Boolean> = repository.isJeebEnabled
    val isJawaliEnabled: StateFlow<Boolean> = repository.isJawaliEnabled
    val isKuraimiEnabled: StateFlow<Boolean> = repository.isKuraimiEnabled
    val isHasebEnabled: StateFlow<Boolean> = repository.isHasebEnabled
    val isOneCashEnabled: StateFlow<Boolean> = repository.isOneCashEnabled
    val isMFloosEnabled: StateFlow<Boolean> = repository.isMFloosEnabled

    // Auto-send and auto-sharing controls
    val isAutoSendSmsEnabled: StateFlow<Boolean> = repository.isAutoSendSmsEnabled
    val isNotificationClickComposeEnabled: StateFlow<Boolean> = repository.isNotificationClickComposeEnabled
    val approvedSmsTemplates: StateFlow<List<String>> = repository.approvedSmsTemplates

    fun toggleAutoSendSms(enabled: Boolean) {
        repository.setAutoSendSmsEnabled(enabled)
    }

    fun toggleNotificationClickCompose(enabled: Boolean) {
        repository.setNotificationClickComposeEnabled(enabled)
    }

    fun addApprovedSmsTemplate(template: String) {
        repository.addApprovedSmsTemplate(template)
    }

    fun removeApprovedSmsTemplate(template: String) {
        repository.removeApprovedSmsTemplate(template)
    }

    // Custom text and configs for account code payments and Gemini API key
    val accountCodeSmsTemplate: StateFlow<String> = repository.accountCodeSmsTemplate
    val accountCodeSmsPhone: StateFlow<String> = repository.accountCodeSmsPhone
    val customGeminiApiKey: StateFlow<String> = repository.customGeminiApiKey

    // Themes and preferences keys
    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme
    val isActivated: StateFlow<Boolean> = repository.isActivated
    val isTrialActive: StateFlow<Boolean> = repository.isTrialActive
    val isInitialLoginDone: StateFlow<Boolean> = repository.isInitialLoginDone

    fun setInitialLoginDone(done: Boolean) {
        repository.setInitialLoginDone(done)
    }

    fun getRemainingTrialDays(): Int = repository.getRemainingTrialDays()

    fun forceExpireTrial() {
        repository.forceExpireTrial()
    }

    fun refreshTrialStatus() {
        repository.refreshTrialStatus()
    }
    val networkName: StateFlow<String> = repository.networkName
    val generalSmsTemplate: StateFlow<String> = repository.generalSmsTemplate
    val cardFormatMode: StateFlow<String> = repository.cardFormatMode
    val categories: StateFlow<List<Int>> = repository.categories

    fun updateCardFormatMode(mode: String) {
        repository.setCardFormatMode(mode)
    }

    fun addCategory(cat: Int) {
        repository.addCategory(cat)
    }

    fun removeCategory(cat: Int) {
        viewModelScope.launch {
            repository.removeCategory(cat)
        }
    }

    val allDeposits: StateFlow<List<com.example.models.Deposit>> = repository.getAllDeposits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAllDeposits() {
        viewModelScope.launch {
            repository.clearAllDeposits()
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        repository.setDarkTheme(enabled)
    }

    val activeSerialKey: StateFlow<String> = repository.activeSerialKey

    fun setActivated(activated: Boolean, serial: String = "") {
        repository.setActivated(activated, serial)
    }

    fun setAppPasswordDirectly(newPass: String) {
        repository.setAppPassword(newPass)
    }

    fun updateNetworkName(name: String) {
        repository.setNetworkName(name)
    }

    fun updateGeneralSmsTemplate(template: String) {
        repository.setGeneralSmsTemplate(template)
    }

    fun addSingleCard(category: Int, card: Card, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val count = repository.insertCardsList(listOf(card))
            onComplete(count > 0)
        }
    }

    fun updateAccountCodeSmsTemplate(template: String) {
        repository.setAccountCodeSmsTemplate(template)
    }

    fun updateAccountCodeSmsPhone(phone: String) {
        repository.setAccountCodeSmsPhone(phone)
    }

    fun updateCustomGeminiApiKey(apiKey: String) {
        repository.setCustomGeminiApiKey(apiKey)
    }

    fun getActiveGeminiApiKey(): String = repository.getActiveGeminiApiKey()

    // Available count flows for primary categories
    val totalUnusedCount: StateFlow<Int> = repository.getUnusedCardsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val count100: StateFlow<Int> = repository.getUnusedCountByCategory(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val count200: StateFlow<Int> = repository.getUnusedCountByCategory(200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val count250: StateFlow<Int> = repository.getUnusedCountByCategory(250)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val count300: StateFlow<Int> = repository.getUnusedCountByCategory(300)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val count500: StateFlow<Int> = repository.getUnusedCountByCategory(500)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Transaction history logs flow
    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of all physically uploaded cards
    val allCards: StateFlow<List<Card>> = repository.getAllCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending approvals flow
    val allPendingApprovals: StateFlow<List<PendingApproval>> = repository.getAllPendingApprovals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customer Mappings flow
    val allMappings: StateFlow<List<CustomerMapping>> = repository.getAllMappings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertMapping(customerUniqueId: String, basicPhone: String, customerName: String, walletType: String) {
        viewModelScope.launch {
            repository.insertMapping(customerUniqueId, basicPhone, customerName, walletType)
        }
    }

    fun deleteMapping(id: Int) {
        viewModelScope.launch {
            repository.deleteMapping(id)
        }
    }

    fun insertPendingApproval(phone: String, amount: Int, walletType: String, isAccountCode: Boolean, depositId: Int) {
        viewModelScope.launch {
            repository.insertPendingApproval(phone, amount, walletType, isAccountCode, depositId)
        }
    }

    fun deletePendingApproval(id: Int) {
        viewModelScope.launch {
            repository.deletePendingApproval(id)
        }
    }

    // Admin UI Actions
    fun toggleService(enabled: Boolean) {
        repository.setServiceEnabled(enabled)
    }

    fun toggleJeeb(enabled: Boolean) {
        repository.setJeebEnabled(enabled)
    }

    fun toggleJawali(enabled: Boolean) {
        repository.setJawaliEnabled(enabled)
    }

    fun toggleKuraimi(enabled: Boolean) {
        repository.setKuraimiEnabled(enabled)
    }

    fun toggleHaseb(enabled: Boolean) {
        repository.setHasebEnabled(enabled)
    }

    fun toggleOneCash(enabled: Boolean) {
        repository.setOneCashEnabled(enabled)
    }

    fun toggleMFloos(enabled: Boolean) {
        repository.setMFloosEnabled(enabled)
    }

    fun verifyPassword(password: String): Boolean {
        val correct = repository.getAppPassword() == password
        if (correct) {
            _isLoggedIn.value = true
        }
        return correct
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun changePassword(oldPass: String, newPass: String): Boolean {
        if (repository.getAppPassword() == oldPass) {
            repository.setAppPassword(newPass)
            return true
        }
        return false
    }

    fun addCards(category: Int, codesBlock: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.insertCardsBulk(category, codesBlock)
            onComplete(count)
        }
    }

    fun addCardsList(cards: List<Card>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.insertCardsList(cards)
            onComplete(count)
        }
    }

    fun deleteCard(cardId: Int) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
        }
    }

    fun markCardAsUsed(cardId: Int) {
        viewModelScope.launch {
            repository.markCardAsUsed(cardId)
        }
    }

    fun insertManualTransaction(phone: String, amount: Int, cardCode: String, walletType: String) {
        viewModelScope.launch {
            repository.insertTransaction(phone, amount, cardCode, walletType)
        }
    }

    fun clearAllCards() {
        viewModelScope.launch {
            repository.clearAllCards()
        }
    }

    fun clearTransactions() {
        viewModelScope.launch {
            repository.clearAllTransactions()
        }
    }

    fun rejectPendingApproval(pendingId: Int) {
        viewModelScope.launch {
            try {
                val pending = repository.getPendingApproval(pendingId)
                if (pending != null) {
                    repository.insertTransaction(pending.phone, pending.amount, "تم رفض وإلغاء إرسال الكرت يدوياً من الشاشة", pending.walletType)
                    repository.deletePendingApproval(pendingId)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to reject pending", e)
            }
        }
    }

    fun updatePendingApprovalPhone(pendingId: Int, newPhone: String) {
        viewModelScope.launch {
            repository.updatePendingApprovalPhone(pendingId, newPhone)
        }
    }

    fun approvePendingApproval(pendingId: Int, onComplete: (success: Boolean, isSent: Boolean, replyMsg: String, phone: String) -> Unit = { _, _, _, _ -> }) {
        viewModelScope.launch {
            try {
                val pending = repository.getPendingApproval(pendingId)
                if (pending == null) {
                    onComplete(false, false, "", "")
                    return@launch
                }
                val amount = pending.amount
                
                // Fallback check: look up in customer mappings first if recipientPhone is currently a name
                val mappedCustomer = repository.getMappingByUniqueId(pending.phone.trim())
                val recipientPhone = mappedCustomer?.basicPhone?.trim() ?: pending.phone
                val walletType = pending.walletType
                val isAccountCode = pending.isAccountCode || (mappedCustomer != null)

                val card = repository.getUnusedCardByCategory(amount)
                if (card != null) {
                    // Mark card as used immediately
                    repository.markCardAsUsed(card.id)

                    // تفكيك وتنسيق مخرجات نص الكارت لتصبح رأسية بالكامل مع استخدام فواصل الأسطر (\n)
                    // بحيث لا تجتمع التسمية والقيمة في سطر واحد لتجنب تجميع التسمية مع القيمة عملاً بالتعليمات المحددة.
                    val cardDetails = if (card.password.isNotEmpty()) {
                        "اسم المستخدم :\n${card.username}\nكلمة السر :\n${card.password}"
                    } else {
                        card.code
                    }

                    val replyMessage = if (isAccountCode) {
                        val accountPhone = repository.accountCodeSmsPhone.value.trim()
                        val targetRecipient = if (accountPhone.isNotEmpty()) accountPhone else recipientPhone
                        repository.accountCodeSmsTemplate.value
                            .replace("%amount", amount.toString())
                            .replace("%account", pending.phone)
                            .replace("%code", cardDetails)
                            .replace("%wallet", walletType)
                    } else {
                        "تم استلام دفعتك بمبلغ $amount ر.ي بنجاح عبر $walletType.\nكود كرت الشحن الخاص بك هو:\n$cardDetails"
                    }

                    // Send SMS in background using stable utility
                    val isSent = com.example.utils.SmsSender.sendSmsInBackground(repository.context, recipientPhone, replyMessage)

                    val logDetails = if (isSent) "$cardDetails (تم الإرسال بعد الموافقة ✔)" else "$cardDetails (فشل إرسال SMS ✖)"
                    repository.insertTransaction(recipientPhone, amount, logDetails, walletType)
                    
                    // Update deposit as shared only if it was actually sent successfully
                    repository.updateDepositSharing(pending.depositId, isShared = isSent, cardDetails = cardDetails)
                    
                    // Emit event to NotificationBus for in-app alert
                    com.example.utils.NotificationBus.emitEvent(
                        amount = amount,
                        walletType = walletType,
                        recipientPhone = recipientPhone,
                        cardDetails = cardDetails,
                        isAutoSent = isSent
                    )

                    repository.deletePendingApproval(pendingId)
                    onComplete(true, isSent, replyMessage, recipientPhone)
                } else {
                    // Out of Stock
                    val replyMessage = "تم استلام دفعتك بمبلغ $amount ر.ي بنجاح عبر $walletType.\nنعتذر، لا يوجد كروت متوفرة حالياً لهذه الفئة. يرجى التواصل مع الإدارة."
                    val isSent = com.example.utils.SmsSender.sendSmsInBackground(repository.context, recipientPhone, replyMessage)
                    repository.insertTransaction(recipientPhone, amount, "كرت غير متوفر (موافقة معلقة فاشلة - نفذ المخزن)", walletType)
                    repository.updateDepositSharing(pending.depositId, isShared = false, cardDetails = "نفذ المخزن")
                    
                    repository.deletePendingApproval(pendingId)
                    onComplete(false, false, replyMessage, recipientPhone)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to approve pending", e)
                onComplete(false, false, "", "")
            }
        }
    }

    // Serials for client activations
    val generatedSerials: StateFlow<Set<String>> = repository.generatedSerials

    fun generateNewSerial(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val randomStr = (1..6).map { chars.random() }.joinToString("")
        val serial = "DAHSHA_$randomStr"
        repository.addGeneratedSerial(serial)
        return serial
    }

    fun addCustomSerial(serial: String) {
        if (serial.trim().isNotEmpty()) {
            repository.addGeneratedSerial(serial.trim())
        }
    }

    fun deleteSerial(serial: String) {
        repository.removeGeneratedSerial(serial)
    }

    fun isValidSerial(serial: String): Boolean {
        val trimmed = serial.trim().uppercase()
        // 1. Check if it exists in manually created/stored serials
        if (repository.isSerialValid(trimmed)) {
            return true
        }

        // 2. Check if it's a deterministic key of format: IDENTIFIER-KS[HEX_CHARS]
        if (trimmed.contains("-KS")) {
            val parts = trimmed.split("-KS")
            if (parts.size == 2) {
                val identifier = parts[0].trim()
                val hashPart = parts[1].trim()
                if (identifier.isNotEmpty() && hashPart.length >= 4) {
                    val expectedHash = generateDeterministicHash(identifier)
                    return hashPart == expectedHash
                }
            }
        }
        return false
    }

    fun generateDeterministicSerial(identifier: String): String {
        val trimmed = identifier.trim().uppercase()
        val hash = generateDeterministicHash(trimmed)
        return "$trimmed-KS$hash"
    }

    private fun generateDeterministicHash(identifier: String): String {
        val salt = "KayanSoftSecureSalt2026"
        val raw = identifier + salt
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(raw.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { String.format("%02X", it.toInt() and 0xFF) }.take(6)
        } catch (e: Exception) {
            "ERROR"
        }
    }

    // Permission Dismiss Actions
    val isPermissionDismissed: StateFlow<Boolean> = repository.isPermissionDismissed

    fun setPermissionDismissed(dismissed: Boolean) {
        repository.setPermissionDismissed(dismissed)
    }

    // ==================================================
    // Generated Mikrotik Cards
    // ==================================================
    val allGeneratedCards: StateFlow<List<com.example.models.GeneratedMikrotikCard>> = repository.getAllGeneratedCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertGeneratedCard(category: Int, pin: String, username: String, password: String) {
        viewModelScope.launch {
            val card = com.example.models.GeneratedMikrotikCard(
                category = category,
                pin = pin,
                username = username,
                password = password
            )
            repository.insertGeneratedCard(card)
        }
    }

    fun insertGeneratedCards(cards: List<com.example.models.GeneratedMikrotikCard>) {
        viewModelScope.launch {
            repository.insertGeneratedCards(cards)
        }
    }

    fun markGeneratedCardAsPrinted(id: Int, printed: Boolean) {
        viewModelScope.launch {
            repository.markGeneratedCardAsPrinted(id, printed)
        }
    }

    fun transferGeneratedCardToAutoSales(id: Int, category: Int, pin: String, username: String, password: String) {
        viewModelScope.launch {
            repository.transferGeneratedCardToAutoSales(id, category, pin, username, password)
        }
    }

    fun deleteGeneratedCard(id: Int) {
        viewModelScope.launch {
            repository.deleteGeneratedCard(id)
        }
    }

    fun clearAllGeneratedCards() {
        viewModelScope.launch {
            repository.clearAllGeneratedCards()
        }
    }

    // ==================================================
    // Isolated Distributor Calculator
    // ==================================================
    val distributorCustomers: StateFlow<List<com.example.models.DistributorCustomer>> = repository.getDistributorCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distributorTransactions: StateFlow<List<com.example.models.DistributorTransaction>> = repository.getDistributorTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distributorExpenses: StateFlow<List<com.example.models.DistributorExpense>> = repository.getDistributorExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distributorCapitals: StateFlow<List<com.example.models.DistributorCapital>> = repository.getDistributorCapitals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertDistributorCustomer(name: String, customId: String? = null) {
        viewModelScope.launch {
            val customer = com.example.models.DistributorCustomer(
                id = customId ?: java.util.UUID.randomUUID().toString(),
                name = name
            )
            repository.insertDistributorCustomer(customer)
        }
    }

    fun performDistributorSale(
        customerId: String,
        quantities: Map<Int, Int>,
        totalAmount: Double,
        totalBuyingCost: Double,
        calcProfits: Double,
        receivedAmount: Double,
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Ensure CASH customer exists if selectedId is CASH
                if (customerId == "CASH") {
                    val cashCust = repository.getDistributorCustomerByIdDirect("CASH")
                    if (cashCust == null) {
                        val newCashCust = com.example.models.DistributorCustomer(
                            id = "CASH",
                            name = "زبون كاش مباشر"
                        )
                        repository.insertDistributorCustomer(newCashCust)
                    }
                }

                val insufficientCategories = mutableListOf<Int>()
                quantities.forEach { (category, qty) ->
                    if (qty > 0) {
                        val count = repository.getUnusedCountByCategoryDirect(category)
                        if (count < qty) {
                            insufficientCategories.add(category)
                        }
                    }
                }

                // Deduct whatever cards are available from the unused stock
                quantities.forEach { (category, qty) ->
                    if (qty > 0) {
                        for (i in 0 until qty) {
                            val card = repository.getUnusedCardByCategory(category)
                            if (card != null) {
                                repository.markCardAsUsed(card.id)
                            }
                        }
                    }
                }

                // Insert sale transaction with embedded metadata for accurate accounting
                val notes = "التكلفة: $totalBuyingCost | الأرباح: $calcProfits | كروت مباعة: ${quantities.filter { it.value > 0 }.map { "${it.value}×${it.key}" }.joinToString(", ")}"
                val saleTx = com.example.models.DistributorTransaction(
                    id = java.util.UUID.randomUUID().toString(),
                    customerId = customerId,
                    type = "sale",
                    amount = totalAmount,
                    notes = notes
                )
                repository.insertDistributorTransaction(saleTx)

                // Insert payment transaction if receivedAmount > 0
                if (receivedAmount > 0) {
                    val paymentTx = com.example.models.DistributorTransaction(
                        id = java.util.UUID.randomUUID().toString(),
                        customerId = customerId,
                        type = "payment",
                        amount = receivedAmount,
                        notes = "دفعة مسددة نقداً مقابل فاتورة المبيعات"
                    )
                    repository.insertDistributorTransaction(paymentTx)
                }

                // Also insert into global transactions history (الحركة المالية) to link reports
                val customerName = if (customerId == "CASH") "زبون كاش مباشر" else {
                    repository.getDistributorCustomerByIdDirect(customerId)?.name ?: "عميل موزع"
                }
                repository.insertTransaction(
                    phone = customerName,
                    amount = totalAmount.toInt(),
                    cardCode = "فاتورة بيع كروت للموزع - المسدد: $receivedAmount ر.ي | أرباح الفاتورة: $calcProfits ر.ي",
                    walletType = "حاسبة الموزع"
                )

                onComplete(true, if (insufficientCategories.isEmpty()) "🟢 تم تسجيل المبيعات وخصم الكروت بنجاح!" else "⚠️ تم تسجيل البيع، ولكن المخزون لا يحتوي على كروت كافية لبعض الفئات!")
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to perform distributor sale", e)
                onComplete(false, "🔴 فشل تنفيذ عملية البيع: ${e.localizedMessage}")
            }
        }
    }

    fun deleteDistributorCustomer(id: String) {
        viewModelScope.launch {
            repository.deleteDistributorCustomer(id)
        }
    }

    fun insertDistributorTransaction(customerId: String, type: String, amount: Double, notes: String) {
        viewModelScope.launch {
            val tx = com.example.models.DistributorTransaction(
                id = java.util.UUID.randomUUID().toString(),
                customerId = customerId,
                type = type,
                amount = amount,
                notes = notes
            )
            repository.insertDistributorTransaction(tx)
        }
    }

    fun deleteDistributorTransaction(id: String, customerId: String) {
        viewModelScope.launch {
            repository.deleteDistributorTransaction(id, customerId)
        }
    }

    fun insertDistributorExpense(category: String, amount: Double, description: String) {
        viewModelScope.launch {
            val expense = com.example.models.DistributorExpense(
                id = java.util.UUID.randomUUID().toString(),
                category = category,
                amount = amount,
                description = description
            )
            repository.insertDistributorExpense(expense)
        }
    }

    fun deleteDistributorExpense(id: String) {
        viewModelScope.launch {
            repository.deleteDistributorExpense(id)
        }
    }

    fun insertDistributorCapital(type: String, amount: Double, description: String) {
        viewModelScope.launch {
            val capital = com.example.models.DistributorCapital(
                id = java.util.UUID.randomUUID().toString(),
                type = type,
                amount = amount,
                description = description
            )
            repository.insertDistributorCapital(capital)
        }
    }

    fun deleteDistributorCapital(id: String) {
        viewModelScope.launch {
            repository.deleteDistributorCapital(id)
        }
    }

    fun clearAllDistributorData() {
        viewModelScope.launch {
            repository.clearAllDistributorData()
        }
    }
}

class MainViewModelFactory(private val repository: CardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
