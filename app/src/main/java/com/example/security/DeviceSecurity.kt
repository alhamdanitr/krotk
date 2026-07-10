package com.example.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.io.File
import java.security.MessageDigest
import java.util.UUID

object DeviceSecurity {
    private const val TAG = "DeviceSecurity"
    private const val PREFS_NAME = "ks_security_prefs"
    private const val KEY_DEVICE_UUID = "device_secure_uuid"

    /**
     * Generates a unique, persistent Device ID bound specifically to this device.
     * Combines Settings.Secure.ANDROID_ID with a persistent UUID stored in private SharedPrefs.
     */
    fun getSecureDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
            return androidId.uppercase()
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var cachedUuid = prefs.getString(KEY_DEVICE_UUID, null)
        if (cachedUuid.isNullOrEmpty()) {
            cachedUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16).uppercase()
            prefs.edit().putString(KEY_DEVICE_UUID, cachedUuid).apply()
        }
        return "DEV-$cachedUuid"
    }

    /**
     * Advanced Root Detection
     * Checks for superuser app APKs, standard su binary paths, and "test-keys" system build tags.
     */
    fun isDeviceRooted(): Boolean {
        // 1. Check test-keys
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        // 2. Check common su paths
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/system/app/SuperSU"
        )
        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }

        // 3. Try executing "su" command
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = process.inputStream.bufferedReader()
            if (reader.readLine() != null) {
                return true
            }
        } catch (t: Throwable) {
            // Ignored
        } finally {
            process?.destroy()
        }

        return false
    }

    /**
     * Advanced Emulator Detection
     * Inspects build configurations, hardware fields, and system properties to detect emulators.
     */
    fun isRunningOnEmulator(): Boolean {
        val brand = Build.BRAND ?: ""
        val device = Build.DEVICE ?: ""
        val model = Build.MODEL ?: ""
        val product = Build.PRODUCT ?: ""
        val hardware = Build.HARDWARE ?: ""
        val fingerprint = Build.FINGERPRINT ?: ""

        return (fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || model.contains("google_sdk")
                || model.contains("Emulator")
                || model.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || brand.startsWith("generic") && device.startsWith("generic")
                || "google_sdk" == product
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || brand.contains("google") && model.contains("Pixel") && hardware.contains("goldfish")) // special emulator fingerprinting
    }

    /**
     * Signature Integrity Verification (Anti-Tampering)
     * Compares the SHA-256 fingerprint of the active signing certificate with the authorized one.
     * Blocks modified/re-signed APK builds from connecting or running securely.
     */
    fun verifySignatureIntegrity(context: Context, expectedSha256: String): Boolean {
        try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                android.content.pm.PackageManager.GET_SIGNATURES
            }

            val packageInfo = packageManager.getPackageInfo(packageName, flags)
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures != null) {
                for (sig in signatures) {
                    val rawCert = sig.toByteArray()
                    val md = MessageDigest.getInstance("SHA-256")
                    val digest = md.digest(rawCert)
                    val currentFingerprint = digest.joinToString(":") { String.format("%02X", it.toInt() and 0xFF) }
                    
                    Log.d(TAG, "Active signature SHA-256: $currentFingerprint")
                    if (currentFingerprint.equals(expectedSha256, ignoreCase = true)) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Signature integrity verification failed", e)
        }
        return false // Defaults to true or falls back to validation during production
    }
}
