package com.example.utils

object SmsParser {
    // Regex for Jeeb wallet: Matches amount before "ر.ي" and everything after a dash (phone or account code)
    val jeebRegex = Regex("(\\d+)ر\\.ي.*-(.+)")
    
    // Regex for Jawali wallet: Matches amount after "مبلغ" and everything after "من" (phone or account code)
    val jawaliRegex = Regex("استلمت مبلغ (\\d+) YER من\\s*(.+?)\\s*(?:رصيدك|\\(|\\s*$|\\n)", RegexOption.DOT_MATCHES_ALL)

    // New wallet patterns requested by the user:
    // Creamy/Kuraimi pattern: أودع/احمد جابر حسن المنتصر لحسابك مبلغ 7400 رصيدك 7450 YER
    val kuraimiRegex = Regex("أودع/([^/\\n]+)\\s+لحسابك\\s+مبلغ\\s+(\\d+)")

    // One Cash pattern: استلمت 200.00 من بدر حسين حنين رصيدك1,252.59 ر.ي
    val oneCashRegex = Regex("استلمت\\s+(\\d+(?:\\.\\d+)?)\\s+من\\s+(.+?)\\s+رصيدك", RegexOption.DOT_MATCHES_ALL)

    // M-Floos pattern: تم إيداع مبلغ 3000 YER من: قابوس سعيد المرجع: 42556517
    val mFloosRegex = Regex("تم إيداع مبلغ\\s+(\\d+)\\s+YER\\s+من:\\s*(.+?)\\s*(?:المرجع|\\s*$|\\n)", RegexOption.DOT_MATCHES_ALL)

    // Haseb pattern: اضيف 200ر.ي تحويل مشترك رص:41692.31ر.ي من 120025
    val hasebRegex = Regex("اضيف\\s+(\\d+)\\s*ر\\.ي\\s+تحويل مشترك.*من\\s+(\\d+)")

    data class ParsedMessage(
        val amount: Int,
        val phone: String,
        val walletType: String,
        val isAccountCode: Boolean = false
    )

    fun parse(message: String): ParsedMessage? {
        val trimmedMsg = message.trim()
        
        // Try parsing Jeeb wallet message
        val jeebMatch = jeebRegex.find(trimmedMsg)
        if (jeebMatch != null) {
            val amount = jeebMatch.groupValues[1].toIntOrNull() ?: 0
            val identifier = jeebMatch.groupValues[2].trim()
            val isPhone = identifier.length == 9 && identifier.all { it.isDigit() }
            return ParsedMessage(amount, identifier, "جيب", !isPhone)
        }

        // Try parsing Jawali wallet message
        val jawaliMatch = jawaliRegex.find(trimmedMsg)
        if (jawaliMatch != null) {
            val amount = jawaliMatch.groupValues[1].toIntOrNull() ?: 0
            val identifier = jawaliMatch.groupValues[2].trim()
            val isPhone = identifier.length == 9 && identifier.all { it.isDigit() }
            return ParsedMessage(amount, identifier, "جوالي", !isPhone)
        }

        // Try parsing Kuraimi wallet message
        val kuraimiMatch = kuraimiRegex.find(trimmedMsg)
        if (kuraimiMatch != null) {
            val identifier = kuraimiMatch.groupValues[1].trim()
            val amount = kuraimiMatch.groupValues[2].toIntOrNull() ?: 0
            return ParsedMessage(amount, identifier, "كريمي", true)
        }

        // Try parsing One Cash wallet message
        val oneCashMatch = oneCashRegex.find(trimmedMsg)
        if (oneCashMatch != null) {
            val amountDouble = oneCashMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val amount = amountDouble.toInt()
            val identifier = oneCashMatch.groupValues[2].trim()
            return ParsedMessage(amount, identifier, "ون كاش", true)
        }

        // Try parsing M-Floos wallet message
        val mFloosMatch = mFloosRegex.find(trimmedMsg)
        if (mFloosMatch != null) {
            val amount = mFloosMatch.groupValues[1].toIntOrNull() ?: 0
            val identifier = mFloosMatch.groupValues[2].trim()
            return ParsedMessage(amount, identifier, "ام فلوس", true)
        }

        // Try parsing Haseb wallet message
        val hasebMatch = hasebRegex.find(trimmedMsg)
        if (hasebMatch != null) {
            val amount = hasebMatch.groupValues[1].toIntOrNull() ?: 0
            val identifier = hasebMatch.groupValues[2].trim()
            return ParsedMessage(amount, identifier, "حاسب", true)
        }

        return null
    }

    fun parseCustomTemplate(message: String, templates: List<String>): ParsedMessage? {
        val trimmedMsg = message.trim()
        for (template in templates) {
            val trimmedTemplate = template.trim()
            if (trimmedTemplate.isEmpty()) continue
            
            try {
                // Escape special regex characters except our custom placeholder keys
                val escapedAmount = Regex.escape("%amount")
                val escapedPhone = Regex.escape("%phone")
                val escapedAccount = Regex.escape("%account")
                
                var regexPattern = Regex.escape(trimmedTemplate)
                var isAccount = false
                
                if (regexPattern.contains(escapedAmount)) {
                    regexPattern = regexPattern.replace(escapedAmount, "(\\d+)")
                }
                if (regexPattern.contains(escapedPhone)) {
                    regexPattern = regexPattern.replace(escapedPhone, "([\\d\\w+]+)")
                } else if (regexPattern.contains(escapedAccount)) {
                    regexPattern = regexPattern.replace(escapedAccount, "([^\\s،,\\|]+)")
                    isAccount = true
                }
                
                // Allow dynamic matches within lines
                val regex = Regex(regexPattern, RegexOption.IGNORE_CASE)
                val match = regex.find(trimmedMsg)
                if (match != null) {
                    val amountIndex = trimmedTemplate.indexOf("%amount")
                    val identifierIndex = if (isAccount) trimmedTemplate.indexOf("%account") else trimmedTemplate.indexOf("%phone")
                    
                    if (amountIndex != -1 && identifierIndex != -1) {
                        val firstGroupVal = match.groupValues.getOrNull(1)?.trim() ?: ""
                        val secondGroupVal = match.groupValues.getOrNull(2)?.trim() ?: ""
                        
                        val amountStr = if (amountIndex < identifierIndex) firstGroupVal else secondGroupVal
                        val identifier = if (amountIndex < identifierIndex) secondGroupVal else firstGroupVal
                        
                        val amount = amountStr.toIntOrNull() ?: 0
                        if (amount > 0 && identifier.isNotEmpty()) {
                            val isPhone = identifier.length == 9 && identifier.all { it.isDigit() }
                            return ParsedMessage(
                                amount = amount,
                                phone = identifier,
                                walletType = "إرسال تلقائي خاص",
                                isAccountCode = isAccount || !isPhone
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore template-specific compilation exceptions
            }
        }
        return null
    }
}
