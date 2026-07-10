fun SpecialCustomersTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val allMappings by viewModel.allMappings.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerUniqueId by remember { mutableStateOf("") }
    var basicPhone by remember { mutableStateOf("") }
    var walletType by remember { mutableStateOf("جيب") }

    val walletOptions = listOf("جيب", "جوالي", "كريمي", "حاسب", "ون كاش", "ام فلوس")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Title block
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "العملاء الاستثنائيين 👑",
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = "قم بإضافة وتوجيه حسابات العملاء الذين يرسلون دفعات بأرقام محافظ مجهولة لتصل لرقمه الشخصي تلقائياً.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Add Customer Form Card
        item {
            Card(
                
                 MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "إضافة عميل جديد 👤",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Partner/Customer Name
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("اسم العميل / الزبون") },
                        placeholder = { Text("مثال: احمد جابر حسن") },
                        singleLine = true, modifier = Modifier.fillMaxWidth().testTag("special_cust_name"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                         MaterialTheme.colorScheme.primary
))

                    // Wallet Type selection via customizable Chips (Material 3 style)
                    Text(
                        text = "نوع المحفظة:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        walletOptions.take(3).forEach { option ->
                            FilterChip(
                                selected = walletType == option,
                                onClick = { walletType = option },
                                label = { Text(option,
                                
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = (MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = walletType == option,
                                    borderColor = if (walletType == option) (MaterialTheme.colorScheme.primary).copy(alpha = 0.2f),
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.5.dp
                                )
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        walletOptions.drop(3).forEach { option ->
                            FilterChip(
                                selected = walletType == option,
                                onClick = { walletType = option },
                                label = { Text(option,
                                
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = (MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = walletType == option,
                                    borderColor = if (walletType == option) (MaterialTheme.colorScheme.primary).copy(alpha = 0.2f),
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.5.dp
                                )
                            )
                        }
                    }

                    // Account Unique ID / Wallet character code
                    OutlinedTextField(
                        value = customerUniqueId,
                        onValueChange = { customerUniqueId = it },
                        label = { Text("رمز الحساب أو معرّف المحفضه للزبون") },
                        placeholder = { Text("مثال: 120025 أو الاسم كما in الكريمي") },
                        singleLine = true, modifier = Modifier.fillMaxWidth().testTag("special_cust_wallet_id"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                         MaterialTheme.colorScheme.primary
))

                    // Target / Basic Personal Phone Number
                    OutlinedTextField(
                        value = basicPhone,
                        onValueChange = { basicPhone = it },
                        label = { Text("رقم هاتفه الأساسي الشخصي للمطابقة") },
                        placeholder = { Text("مثال: 770118275") },
                        singleLine = true, modifier = Modifier.fillMaxWidth().testTag("special_cust_phone_id"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                         MaterialTheme.colorScheme.primary
))

                    // Add Customer Button styled with Brush.horizontalGradient(listOf(Color(0xFF9C27B0), Color(0xFFFF4081)))
                    Button(
                        onClick = {
                            if (customerName.trim().isEmpty() || customerUniqueId.trim().isEmpty() || basicPhone.trim().isEmpty()) {
                                Toast.makeText(context, "الرجاء تعبئة كافة بيانات العميل المطلوب أولاً!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.insertMapping(
                                customerUniqueId = customerUniqueId.trim(),
                                basicPhone = basicPhone.trim(),
                                customerName = customerName.trim(),
                                walletType = walletType
                            )
                            customerName = ""
                            customerUniqueId = ""
                            basicPhone = ""
                            Toast.makeText(context, "تم إضافة العميل الاستثنائي وتنشيط المطابقة بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                        },
                        
                        contentPadding = PaddingValues(), modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(Color(0xFF9C27B0), Color(0xFFFF4081)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("إضافة وحفظ العميل", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Special Customers list header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Export CSV Button with Emerald Green Gradient
                Button(
                    onClick = {
                        exportCustomerTransactionsToCsv(context, allMappings, allTransactions)
                    },
                    
                    contentPadding = PaddingValues(), modifier = Modifier
                        .height(42.dp)
                        .testTag("export_csv_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Outlined.Share, contentDescription = "تصدير CSV", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير الحسابات (CSV)", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Text(
                    text = "قائمة العملاء الاستثنائيين النشطة 📂 (${allMappings.size})",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold)
            }
        }

        if (allMappings.isEmpty()) {
            item {
                Card(
                    
                     MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "لا يوجد أي زبائن استثنائيين مضافين حالياً.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            }
        } else {
            items(allMappings.size) { index ->
                val mapping = allMappings[index]
                Card(
                    
                     MaterialTheme.colorScheme.outline),
                    
                     0.dp else 2.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Delete Button (Outlined)
                        IconButton(
                            onClick = { viewModel.deleteMapping(mapping.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "حذف العميل",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        // Info
                        Column(
                            horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                // Wallet Badge (Beautiful pastel semi-transparent indicators)
                                val badgeBgColor = when (mapping.walletType) {
                                    "جيب" -> Color(0xFF1565C0).copy(alpha = 0.15f)
                                    "جوالي" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                    "كريمي" -> Color(0xFFC62828).copy(alpha = 0.15f)
                                    "حاسب" -> Color(0xFFEF6C00).copy(alpha = 0.15f)
                                    "ون كاش" -> Color(0xFF880E4F).copy(alpha = 0.15f)
                                    else -> Color(0xFF4527A0).copy(alpha = 0.15f)
                                }
                                val badgeTextColor = when (mapping.walletType) {
                                    "جيب" -> Color(0xFF90CAF9)
                                    "جوالي" -> Color(0xFFA5D6A7)
                                    "كريمي" -> Color(0xFFEF9A9A)
                                    "حاسب" -> Color(0xFFFFCC80)
                                    "ون كاش" -> Color(0xFFF48FB1)
                                    else -> Color(0xFFB39DDB)
                                }
                                Surface(
                                    color = badgeBgColor, modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = mapping.walletType,
                                        color = badgeTextColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                
                                Text(
                                    text = mapping.customerName.ifEmpty { "عميل استثنائي" },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "رمز مجهول: ${mapping.customerUniqueId}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Normal
                            )
                            
                            Text(
                                text = "رقم التوجيه (الأساسي): ${mapping.basicPhone}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

fun exportTransactionsToCsv(
    context: Context,
    transactions: List<Transaction>,
    dateLabel: String
) {
    try {
        if (transactions.isEmpty()) {
            Toast.makeText(context, "لا توجد أي معاملات لتصديرها!", Toast.LENGTH_LONG).show()
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val csvBuilder = StringBuilder()
        csvBuilder.append("\uFEFF") // UTF-8 BOM
        
        // Header row
        csvBuilder.append("رقم الحركة,اسم الجزء/العميل,الرقم الهاتفي,القيمة/الفئة (ريال),كود الكارت والشحن,نوع المحفظة,تاريخ المعاملة\n")
        
        for (tx in transactions) {
            val phone = tx.phone
            val amount = tx.amount.toString()
            val cardDetails = tx.cardCode
            val wallet = tx.walletType.ifEmpty { "عميل مباشر" }
            val dateStr = sdf.format(Date(tx.createdAt))
            
            csvBuilder.append("${tx.id},")
            csvBuilder.append("عميل مباشر,")
            csvBuilder.append("${escapeCsv(phone)},")
            csvBuilder.append("${escapeCsv(amount)},")
            csvBuilder.append("${escapeCsv(cardDetails)},")
            csvBuilder.append("${escapeCsv(wallet)},")
            csvBuilder.append("${escapeCsv(dateStr)}\n")
        }

        val filename = "تقرير_توزيع_الكروت_${dateLabel}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
        val cacheFile = File(context.cacheDir, filename)
        cacheFile.writeText(csvBuilder.toString(), StandardCharsets.UTF_8)

        val authority = "${context.packageName}.fileprovider"
        val fileUri: Uri = FileProvider.getUriForFile(context, authority, cacheFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "تقرير مبيعات وتوزيع الكروت - $dateLabel")
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "تصدير تقرير الكروت الموزعة CSV")
        context.startActivity(chooser)
    } catch (e: Exception) {
        Log.e("CSV_EXPORT", "Error exporting transaction CSV", e)
        Toast.makeText(context, "حدث خطأ أثناء التصدير", Toast.LENGTH_SHORT).show()
    }
}

fun exportCustomerTransactionsToCsv(
    context: Context,
    mappings: List<CustomerMapping>,
    transactions: List<Transaction>
) {
    try {
        val mappingPhones = mappings.associateBy { it.basicPhone.trim() }
        val matchedList = transactions.filter { mappingPhones.containsKey(it.phone.trim()) }
        
        if (matchedList.isEmpty()) {
            Toast.makeText(context, "لا توجد أي معاملات مسجلة للعملاء الاستثنائيين لتصديرها!", Toast.LENGTH_LONG).show()
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val csvBuilder = StringBuilder()
        csvBuilder.append("\uFEFF") // UTF-8 BOM
        
        // Header row
        csvBuilder.append("رقم الحركة,اسم العميل الاستثنائي,معرّف المحفظة الرقمي,الرقم الهاتفي الموجه,القيمة/الفئة (ريال),كود الكارت والشحن,نوع المحفظة,تاريخ ووقت المعاملة\n")
        
        for (tx in matchedList) {
            val mapping = mappingPhones[tx.phone.trim()]
            val name = mapping?.customerName?.ifEmpty { "عميل استثنائي" } ?: "عميل استثنائي"
            val uniqueId = mapping?.customerUniqueId ?: ""
            val phone = tx.phone
            val amount = tx.amount.toString()
            val cardDetails = tx.cardCode
            val wallet = tx.walletType.ifEmpty { mapping?.walletType ?: "" }
            val dateStr = sdf.format(Date(tx.createdAt))
            
            csvBuilder.append("${tx.id},")
            csvBuilder.append("${escapeCsv(name)},")
            csvBuilder.append("${escapeCsv(uniqueId)},")
            csvBuilder.append("${escapeCsv(phone)},")
            csvBuilder.append("${escapeCsv(amount)},")
            csvBuilder.append("${escapeCsv(cardDetails)},")
            csvBuilder.append("${escapeCsv(wallet)},")
            csvBuilder.append("${escapeCsv(dateStr)}\n")
        }

        val filename = "تقرير_معاملات_العملاء_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
        val cacheFile = File(context.cacheDir, filename)
        cacheFile.writeText(csvBuilder.toString(), StandardCharsets.UTF_8)

        val authority = "${context.packageName}.fileprovider"
        val fileUri: Uri = FileProvider.getUriForFile(context, authority, cacheFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "تقرير معاملات اليوم والعملاء الاستثنائيين")
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "مشاركة وتصدير ملف الحسابات الشامل CSV")
        context.startActivity(chooser)
    } catch (e: Exception) {
        Log.e("CSV_EXPORT", "Error exporting CSV", e)
        Toast.makeText(context, "حدث خطأ أثناء تصدير الملف: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun escapeCsv(value: String): String {
    val clean = value.replace("\"", "\"\"")
    return if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
        "\"$clean\""
    } else {
        clean
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalsTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val allPendingApprovals by viewModel.allPendingApprovals.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    var showLinkDialog by remember { mutableStateOf(false) }
    var pendingToLink by remember { mutableStateOf<com.example.models.PendingApproval?>(null) }
    var enteredPhoneNumber by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Tab Title
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "التفويضات المعلقة والموافقة ⏳",
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = "العمليات التي تنتظر موافقتك اليدوية لإرسال الكود وتأكيد حركة الدفع.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (allPendingApprovals.isEmpty()) {
            item {
                Card(
                    
                     MaterialTheme.colorScheme.outline),
                    
                     0.dp else 2.dp), modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "لا توجد تفويضات",
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "لا توجد عمليات معلقة حالياً 🎉",
                            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "جميع العمليات الواردة الأخرى يتم تفعيلها تلقائياً بنجاح.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            items(allPendingApprovals.size) { index ->
                val pending = allPendingApprovals[index]
                Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Wallet Badge
                            val badgeBgColor = when (pending.walletType) {
                                "جيب" -> Color(0xFF1565C0).copy(alpha = 0.15f)
                                "جوالي" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                "كريمي" -> Color(0xFFC62828).copy(alpha = 0.15f)
                                "حاسب" -> Color(0xFFEF6C00).copy(alpha = 0.15f)
                                "ون كاش" -> Color(0xFF880E4F).copy(alpha = 0.15f)
                                else -> Color(0xFF4527A0).copy(alpha = 0.15f)
                            }
                            val badgeTextColor = when (pending.walletType) {
                                "جيب" -> Color(0xFF90CAF9)
                                "جوالي" -> Color(0xFFA5D6A7)
                                "كريمي" -> Color(0xFFEF9A9A)
                                "حاسب" -> Color(0xFFFFCC80)
                                "ون كاش" -> Color(0xFFF48FB1)
                                else -> Color(0xFFB39DDB)
                            }
                            Surface(
                                color = badgeBgColor) {
                                Text(
                                    text = pending.walletType,
                                    color = badgeTextColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Amount Label
                            Text(
                                text = "فئة ${pending.amount} ر.ي",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        // Details Block
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = pending.phone,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ":رقم المودع/المستلم",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (pending.isAccountCode) "كود حساب (مطابقة فريدة)" else "شراء كارت مباشر",
                                    color = if (pending.isAccountCode) (MaterialTheme.colorScheme.secondary),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ":نوع الدفعة",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isName = pending.phone.any { it.isLetter() }
                        if (isName) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.outline)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        pendingToLink = pending
                                        enteredPhoneNumber = ""
                                        showLinkDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = "ربط", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ربط رقم العميل 📞", fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "الحساب اسم وليس رقم هاتف!",
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Right
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reject Button (Red outline)
                            OutlinedButton(
                                onClick = {
                                    viewModel.rejectPendingApproval(pending.id)
                                    Toast.makeText(context, "تم رفض المعاملة اليدوية وإلغاؤها", Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text("رفض الدفعة ✖", fontWeight = FontWeight.Bold)
                            }

                            // Approve Button (Emerald Green style)
                            Button(
                                onClick = {
                                    viewModel.approvePendingApproval(pending.id) { success, isSent, replyMsg, phone ->
                                        if (success) {
                                            if (isSent) {
                                                Toast.makeText(context, "تمت الموافقة وإرسال كود الشحن بنجاح! ✔", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "تمت الموافقة وتخصيص الكرت بنجاح ⚠️ ولكن فشل الإرسال التلقائي كرسالة. جاري المشاركة اليدوية والمتابعة...", Toast.LENGTH_LONG).show()
                                                com.example.utils.SmsSender.launchWalletApp(context, pending.walletType, replyMsg)
                                            }
                                        } else {
                                            Toast.makeText(context, "حدث خطأ أو نفذت كروت المخزون!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                
                                contentPadding = PaddingValues(), modifier = Modifier.weight(1.3f).height(42.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("موافقة وإرسال ✔", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- 📞 نافذة ربط هاتف العميل للتفويض المعلق 📞 ---
    if (showLinkDialog && pendingToLink != null) {
        val pending = pendingToLink!!
        AlertDialog(
            onDismissRequest = { 
                showLinkDialog = false
                pendingToLink = null 
            },
            title = {
                Text(
                    text = "ربط رقم هاتف للعميل 📞", modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "الاسم المُستلم: ${pending.phone}",
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "أدخل رقم الهاتف الصحيح لهذا العميل لإرسال كود الشحن إليه تلقائياً عند الموافقة وحفظه للمستقبل:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = enteredPhoneNumber,
                        onValueChange = { enteredPhoneNumber = it },
                        label = { Text("رقم جوال العميل") },
                        placeholder = { Text("مثال: 777123456") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone, modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                         MaterialTheme.colorScheme.primary
))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanedPhone = enteredPhoneNumber.trim()
                        if (cleanedPhone.isNotEmpty()) {
                            // 1. Create mapping in the database
                            viewModel.insertMapping(
                                customerUniqueId = pending.phone.trim(),
                                basicPhone = cleanedPhone,
                                customerName = pending.phone.trim(),
                                walletType = pending.walletType
                            )
                            // 2. Update the pending approval phone number itself in DB
                            viewModel.updatePendingApprovalPhone(pending.id, cleanedPhone)
                            
                            Toast.makeText(context, "تم ربط الرقم وحفظ الارتباط بنجاح! 📱", Toast.LENGTH_SHORT).show()
                            showLinkDialog = false
                            pendingToLink = null
                        } else {
                            Toast.makeText(context, "الرجاء إدخال رقم هاتف صحيح", Toast.LENGTH_SHORT).show()
                        }
                    } {
                    Text("حفظ وربط ✔", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showLinkDialog = false
                        pendingToLink = null
                    }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }, modifier = Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(20.dp))
        )
    }
}
