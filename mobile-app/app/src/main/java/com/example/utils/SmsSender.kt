package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

object SmsSender {
    fun sendSmsInBackground(context: Context, recipientPhone: String, messageText: String): Boolean {
        // Simple numeric/phone sanity check. If recipient contains alphabet letters or Arabic letters, it's a name, not a number!
        val containsLetters = recipientPhone.any { it.isLetter() || Character.isLetter(it) }
        if (containsLetters || recipientPhone.trim().isEmpty()) {
            Log.d("SmsSender", "Recipient phone looks like a name or is empty ($recipientPhone). Skipping direct background SMS.")
            return false
        }

        return try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(messageText)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(recipientPhone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(recipientPhone, null, messageText, null, null)
            }
            Log.d("SmsSender", "Direct background SMS successful to $recipientPhone")
            true
        } catch (e: Exception) {
            Log.e("SmsSender", "Failed to send background SMS directly to $recipientPhone", e)
            false
        }
    }

    fun launchWalletApp(context: Context, walletType: String, messageText: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Card Details", messageText)
            clipboard.setPrimaryClip(clip)

            val packageName = when (walletType) {
                "جيب" -> "com.jeeb.app"
                "جوالي" -> "com.ama.wecashmobileapp"
                "ون كاش" -> "com.one.onecustomer"
                else -> null
            }

            if (packageName != null) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.putExtra(Intent.EXTRA_TEXT, messageText)
                    launchIntent.putExtra("sms_body", messageText)
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    Toast.makeText(context, "تم نسخ الكود وفتح تطبيق $walletType بنجاح! 💸", Toast.LENGTH_LONG).show()
                    return
                }
            }

            // Fallback: Default system sharing sheet
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "مشاركة كود الكرت الشحن")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            Toast.makeText(context, "تم نسخ الكود في الحافظة وتوجيهك للمشاركة! 📋", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("SmsSender", "Error sharing or opening wallet", e)
            Toast.makeText(context, "تم نسخ كود الكرت للحافظة! 📋", Toast.LENGTH_LONG).show()
        }
    }

    fun showManualShareNotification(context: Context, title: String, messageText: String, walletType: String) {
        try {
            val channelId = "manual_share_channel"
            val notificationId = System.currentTimeMillis().toInt()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= 26) {
                val channel = NotificationChannel(
                    channelId,
                    "مشاركة الكروت يدوياً",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "إشعارات لمشاركة كروت الشحن يدوياً عند فشل الإرسال التلقائي"
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Intent to launch the share sheet directly
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val chooser = Intent.createChooser(shareIntent, "مشاركة كود الكرت الشحن")
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                chooser,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(title)
                .setContentText("اضغط لمشاركة الكود يدوياً عبر $walletType 📲")
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    "فشل إرسال كرت الشحن تلقائياً للعميل عبر $walletType (المستلم اسم).\n" +
                    "اضغط هنا لمشاركة أو نسخ الكرت يدوياً ومتابعة عملية البيع."
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(0xFFFF5252.toInt())

            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            Log.e("SmsSender", "Error displaying manual share notification", e)
        }
    }
}
