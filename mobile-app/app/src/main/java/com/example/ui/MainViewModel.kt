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

    fun setActivated(activated: Boolean) {
        repository.setActivated(activated)
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
