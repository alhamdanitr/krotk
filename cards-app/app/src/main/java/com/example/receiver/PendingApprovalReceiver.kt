package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import com.example.database.CardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PendingApprovalReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingId = intent.getIntExtra("pending_id", -1)
        val notificationId = intent.getIntExtra("notification_id", -1)

        // Cancel notification close dialog/shade
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationId != -1) {
            notificationManager.cancel(notificationId)
        }

        if (pendingId == -1) return

        val goAsyncPending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = CardRepository(context)
                val pending = repository.getPendingApproval(pendingId)
                if (pending == null) {
                    goAsyncPending.finish()
                    return@launch
                }

                if (action == "com.example.action.APPROVE_PENDING") {
                    // Approve and Send
                    val amount = pending.amount
                    
                    // Fallback check: look up in customer mappings first if recipientPhone is currently a name
                    val mappedCustomer = repository.getMappingByUniqueId(pending.phone.trim())
                    val recipientPhone = mappedCustomer?.basicPhone?.trim() ?: pending.phone
                    val walletType = pending.walletType
                    val isAccountCode = pending.isAccountCode || (mappedCustomer != null)

                    // Get an unused card
                    val card = repository.getUnusedCardByCategory(amount)
                    if (card != null) {
                        // Mark card as used immediately
                        repository.markCardAsUsed(card.id)

                        // Format card details
                        // تفكيك وتنسيق مخرجات نص الكارت لتصبح رأسية بالكامل مع استخدام فواصل الأسطر (\n)
                        // بحيث لا تجتمع التسمية والقيمة في سطر واحد لتجنب تجميع التسمية مع القيمة عملاً بالتعليمات المحددة.
                        val cardDetails = if (card.password.isNotEmpty()) {
                            "اسم المستخدم :\n${card.username}\nكلمة السر :\n${card.password}"
                        } else {
                            card.code
                        }

                        // Format reply message Text
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

                        // Send direct SMS in background
                        val isSent = com.example.utils.SmsSender.sendSmsInBackground(context, recipientPhone, replyMessage)

                        // Log transaction success
                        val logDetails = if (isSent) "$cardDetails (تم الإرسال بعد الموافقة ✔)" else "$cardDetails (فشل إرسال SMS ✖)"
                        repository.insertTransaction(recipientPhone, amount, logDetails, walletType)
                        repository.updateDepositSharing(pending.depositId, isShared = isSent, cardDetails = cardDetails)

                        // Emit event to NotificationBus for in-app alert
                        com.example.utils.NotificationBus.emitEvent(
                            amount = amount,
                            walletType = walletType,
                            recipientPhone = recipientPhone,
                            cardDetails = cardDetails,
                            isAutoSent = isSent
                        )

                        if (!isSent) {
                            com.example.utils.SmsSender.showManualShareNotification(
                                context,
                                "فشل الإرسال التلقائي لمبلغ $amount ر.ي عبر $walletType",
                                replyMessage,
                                walletType
                            )
                        }
                    } else {
                        // Out of Stock
                        val replyMessage = "تم استلام دفعتك بمبلغ $amount ر.ي بنجاح عبر $walletType.\nنعتذر، لا يوجد كروت متوفرة حالياً لهذه الفئة. يرجى التواصل مع الإدارة."
                        com.example.utils.SmsSender.sendSmsInBackground(context, recipientPhone, replyMessage)

                        // Log failure transaction
                        repository.insertTransaction(recipientPhone, amount, "كرت غير متوفر (موافقة معلقة فاشلة - نفذ المخزن)", walletType)
                        repository.updateDepositSharing(pending.depositId, isShared = false, cardDetails = "نفذ المخزن")
                    }
                } else if (action == "com.example.action.REJECT_PENDING") {
                    val isAutoApproved = repository.autoApprovedAmounts.value.contains(pending.amount)
                    if (isAutoApproved) {
                        // Reject / ignore standard: just log a cancelled transaction
                        repository.insertTransaction(pending.phone, pending.amount, "تم رفض وإلغاء إرسال الكرت يدوياً", pending.walletType)
                        repository.updateDepositSharing(pending.depositId, isShared = false, cardDetails = "تم الرفض يدوياً")
                    } else {
                        // Unapproved amount ignored completely - delete deposit so no financial log exists!
                        repository.deleteDeposit(pending.depositId)
                    }
                }

                // Always delete the pending approval from DB once processed
                repository.deletePendingApproval(pendingId)

            } catch (e: Exception) {
                Log.e("PendingApprovalReceiver", "Error handling pending approval action", e)
            } finally {
                goAsyncPending.finish()
            }
        }
    }
}
