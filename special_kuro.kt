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
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()
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
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)
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
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = (MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = walletType == option,
                                    borderColor = if (walletType == option) (MaterialTheme.colorScheme.primary).copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline,
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
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = (MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = walletType == option,
                                    borderColor = if (walletType == option) (MaterialTheme.colorScheme.primary).copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline,
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
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)
                         MaterialTheme.colorScheme.primary
))

                    // Target / Basic Personal Phone Number
                    OutlinedTextField(
                        value = basicPhone,
                        onValueChange = { basicPhone = it },
                        label = { Text("رقم هاتفه الأساسي الشخصي للمطابقة") },
                        placeholder = { Text("مثال: 770118275") },
                        singleLine = true, modifier = Modifier.fillMaxWidth().testTag("special_cust_phone_id"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)
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
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()
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
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
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
