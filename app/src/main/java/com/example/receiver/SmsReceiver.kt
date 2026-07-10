package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.database.CardRepository
import com.example.utils.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.network.SyncManager
import org.json.JSONObject

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        val bundle = intent.extras ?: return
        val pdus = bundle["pdus"] as? Array<*> ?: return
        val format = bundle.getString("format")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (pdu in pdus) {
                    val sms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        SmsMessage.createFromPdu(pdu as ByteArray, format)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsMessage.createFromPdu(pdu as ByteArray)
                    }
                    val message = sms.messageBody ?: continue
                    handleMessage(context, message)
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error receiving SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleMessage(context: Context, message: String) {
        val repository = CardRepository(context)
        
        // Only proceed if the service is turned on in settings
        if (!repository.isServiceEnabled.value) {
            Log.d("SmsReceiver", "Service is disabled. SMS ignored.")
            return
        }

        // Try parsing defaults (Jeeb / Jawali regex)
        var parsed = SmsParser.parse(message)

        // Try custom approved templates if default parser returned null
        if (parsed == null) {
            val templates = repository.approvedSmsTemplates.value
            parsed = SmsParser.parseCustomTemplate(message, templates)
        }

        if (parsed != null) {
            sendCard(context, repository, parsed.amount, parsed.phone, parsed.walletType, parsed.isAccountCode)
        }
    }

    private suspend fun sendCard(
        context: Context,
        repository: CardRepository,
        amount: Int,
        identifier: String,
        walletType: String,
        isAccountCode: Boolean
    ) {
        // Check specifically enabled wallet types
        if (walletType == "جيب" && !repository.isJeebEnabled.value) {
            Log.d("SmsReceiver", "Jeeb wallet distributions are configured OFF in settings. Ignored.")
            return
        }
        if (walletType == "جوالي" && !repository.isJawaliEnabled.value) {
            Log.d("SmsReceiver", "Jawali wallet distributions are configured OFF in settings. Ignored.")
            return
        }
        if (walletType == "كريمي" && !repository.isKuraimiEnabled.value) {
            Log.d("SmsReceiver", "Kuraimi wallet distributions are configured OFF in settings. Ignored.")
            return
        }
        if (walletType == "حاسب" && !repository.isHasebEnabled.value) {
            Log.d("SmsReceiver", "Haseb wallet distributions are configured OFF in settings. Ignored.")
            return
        }
        if (walletType == "ون كاش" && !repository.isOneCashEnabled.value) {
            Log.d("SmsReceiver", "One Cash wallet distributions are configured OFF in settings. Ignored.")
            return
        }
        if (walletType == "ام فلوس" && !repository.isMFloosEnabled.value) {
            Log.d("SmsReceiver", "M-Floos wallet distributions are configured OFF in settings. Ignored.")
            return
        }

        val channelId = "dahsha_notifications"
        val notificationId = 1001 + (System.currentTimeMillis() % 100000).toInt()

        // Create notification channel for modern Android
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تأكيدات كروت الدحشة"
            val descriptionText = "إشعارات سريعة لمشاركة كروت الشحن عبر SMS يدوي"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Destination for sending SMS with Mapping lookup
        val mappedCustomer = repository.getMappingByUniqueId(identifier.trim())
        val mappedRecipientPhone = mappedCustomer?.basicPhone?.trim()

        val recipientPhone = if (!mappedRecipientPhone.isNullOrEmpty()) {
            mappedRecipientPhone
        } else if (isAccountCode) {
            val configuredPhone = repository.accountCodeSmsPhone.value.trim()
            if (configuredPhone.isNotEmpty()) configuredPhone else identifier
        } else {
            identifier
        }

        if (repository.isAutoSendSmsEnabled.value) {
            // Automatic send (without requiring manual confirmation)
            val card = repository.getUnusedCardByCategory(amount)
            if (card != null) {
                // Formatting card
                // تفكيك وتنسيق مخرجات نص الكارت لتصبح رأسية بالكامل مع استخدام فواصل الأسطر (\n)
                // بحيث لا تجتمع التسمية والقيمة في سطر واحد لتجنب تجميع التسمية مع القيمة عملاً بالتعليمات المحددة.
                val cardDetails = if (card.password.isNotEmpty()) {
                    "اسم المستخدم :\n${card.username}\nكلمة السر :\n${card.password}"
                } else {
                    card.code
                }

                val replyMessage = if (isAccountCode) {
                    repository.accountCodeSmsTemplate.value
                        .replace("%amount", amount.toString())
                        .replace("%account", identifier)
                        .replace("%code", cardDetails)
                        .replace("%wallet", walletType)
                } else {
                    "تم استلام دفعتك بمبلغ $amount ر.ي بنجاح عبر $walletType.\nكود كرت الشحن الخاص بك هو:\n$cardDetails"
                }

                // Send background SMS
                val isSent = com.example.utils.SmsSender.sendSmsInBackground(context, recipientPhone, replyMessage)
                val logDetails = if (isSent) "$cardDetails (تم الإرسال تلقائياً ✔)" else "$cardDetails (فشل إرسال SMS ✖)"

                repository.markCardAsUsed(card.id)
                repository.insertTransaction(recipientPhone, amount, logDetails, walletType)
                repository.insertDeposit(recipientPhone, amount, walletType, isShared = isSent, cardDetails = cardDetails)

                // Sync with cloud backend
                val syncManager = SyncManager(context)
                syncManager.enqueueMessage(JSONObject().apply {
                    put("type", "SMS_PROCESSED")
                    put("status", "AUTO_SENT")
                    put("amount", amount)
                    put("recipientPhone", recipientPhone)
                    put("walletType", walletType)
                    put("cardDetails", cardDetails)
                })

                // Emit event to NotificationBus for in-app alert
                com.example.utils.NotificationBus.emitEvent(
                    amount = amount,
                    walletType = walletType,
                    recipientPhone = recipientPhone,
                    cardDetails = cardDetails,
                    isAutoSent = isSent
                )

                if (isSent) {
                    // Show notification of automatic success
                    val builder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.sym_action_chat)
                        .setContentTitle("تم إرسال كرت تلقائي بقيمة $amount ر.ي")
                        .setContentText("إلى الرقم: $recipientPhone عبر $walletType")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setColor(0xFF2E7D32.toInt()) // green
                        .setStyle(NotificationCompat.BigTextStyle().bigText(
                            "تم شحن فئة $amount ر.ي لـ $recipientPhone تلقائياً بنجاح.\nالكود المرسل: $cardDetails"
                        ))
                    notificationManager.notify(notificationId, builder.build())
                } else {
                    com.example.utils.SmsSender.showManualShareNotification(
                        context,
                        "فشل الإرسال التلقائي لمبلغ $amount ر.ي عبر $walletType",
                        replyMessage,
                        walletType
                    )
                }
            } else {
                // Out of stock
                val replyMessage = "نعتذر، لا يوجد كروت متوفرة حالياً لهذه الفئة ($amount ر.ي). يرجى التواصل مع الإدارة."
                com.example.utils.SmsSender.sendSmsInBackground(context, recipientPhone, replyMessage)

                repository.insertTransaction(recipientPhone, amount, "كرت غير متوفر تلقائياً (نفذ المخزن)", walletType)
                repository.insertDeposit(recipientPhone, amount, walletType, isShared = false, cardDetails = "نفذ المخزن")

                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("نفذ المخزن! إيداع $walletType بقيمة $amount ر.ي")
                    .setContentText("لم يتم إرسال كرت لعدم وجود مخزن")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setColor(0xFFC62828.toInt()) // red

                notificationManager.notify(notificationId, builder.build())
            }
        } else {
            // Manual confirmation mode (requires approval first)
            // Insert Deposit first, then insert PendingApproval linked with its ID
            val depositId = repository.insertDeposit(recipientPhone, amount, walletType, isShared = false, cardDetails = "معلق بانتظار الموافقة")
            val pendingId = repository.insertPendingApproval(recipientPhone, amount, walletType, isAccountCode, depositId.toInt()).toInt()

            // Sync with cloud backend
            val syncManager = SyncManager(context)
            syncManager.enqueueMessage(JSONObject().apply {
                put("type", "SMS_PROCESSED")
                put("status", "PENDING_APPROVAL")
                put("amount", amount)
                put("recipientPhone", recipientPhone)
                put("walletType", walletType)
                put("pendingId", pendingId)
            })

            // Setup Approve intent
            val approveIntent = Intent(context, PendingApprovalReceiver::class.java).apply {
                action = "com.example.action.APPROVE_PENDING"
                putExtra("pending_id", pendingId)
                putExtra("notification_id", notificationId)
            }
            val approvePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 2,
                approveIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Setup Reject intent
            val rejectIntent = Intent(context, PendingApprovalReceiver::class.java).apply {
                action = "com.example.action.REJECT_PENDING"
                putExtra("pending_id", pendingId)
                putExtra("notification_id", notificationId)
            }
            val rejectPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 2 + 1,
                rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Main notification intent opening MainActivity
            val appIntent = Intent(context, com.example.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val appPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .setContentTitle("إيداع معلق بقيمة $amount ر.ي عبر $walletType")
                .setContentText("اضغط للموافقة على إرسال الكرت")
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    "تم تلقي إيداع بقيمة $amount ر.ي من الرقم $recipientPhone عبر $walletType.\n" +
                    "يرجى تحديد الإجراء المناسب أدناه لتأكيد إرسال الكارت أو إلغاء المعاملة."
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setColor(0xFFFFD700.toInt()) // gold
                .setContentIntent(appPendingIntent)
                .addAction(android.R.drawable.ic_media_play, "تأكيد وإرسال ✔", approvePendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "تجاهل ✖", rejectPendingIntent)

            notificationManager.notify(notificationId, builder.build())
        }
    }

    private fun sendSmsInBackground(context: Context, recipientPhone: String, messageText: String): Boolean {
        return com.example.utils.SmsSender.sendSmsInBackground(context, recipientPhone, messageText)
    }
}
