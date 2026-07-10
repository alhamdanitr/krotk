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
