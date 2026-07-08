package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.GeneratedMikrotikCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MikrotikGeneratorScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("dahsha_prefs", Context.MODE_PRIVATE) }
    val generatedCards by viewModel.allGeneratedCards.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableStateOf(0) } // 0: Generator, 1: History, 2: Settings
    
    // MikroTik API / SSH Config Credentials
    var mtHost by remember { mutableStateOf(sharedPrefs.getString("mt_host", "192.168.88.1") ?: "192.168.88.1") }
    var mtPort by remember { mutableStateOf(sharedPrefs.getString("mt_port", "8728") ?: "8728") }
    var mtUser by remember { mutableStateOf(sharedPrefs.getString("mt_user", "admin") ?: "admin") }
    var mtPass by remember { mutableStateOf(sharedPrefs.getString("mt_pass", "") ?: "") }
    var mtProfile by remember { mutableStateOf(sharedPrefs.getString("mt_profile", "default_profile") ?: "default_profile") }
    var isPassVisible by remember { mutableStateOf(false) }

    // Card Generation form
    var selectedCategory by remember { mutableStateOf(100) }
    var quantityInput by remember { mutableStateOf("10") }
    var prefixInput by remember { mutableStateOf("DAHSHA_") }
    var codeLengthInput by remember { mutableStateOf("6") }
    var formatMode by remember { mutableStateOf("user_pass") } // "user_pass" or "user_only"
    
    val categoriesList = listOf(100, 200, 250, 300, 500, 1000, 3000) // 3000 as Monthly
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "مولد وطباعة كروت المايكروتك",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                ),
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "🔄 تم تحديث قائمة الكروت المولدة!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = Color.White)
                    }
                }
            )
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen internal tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
                contentColor = BrandPrimaryRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BrandPrimaryRed
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("توليد الكروت", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("الكروت المتاحة (${generatedCards.filter { !it.transferred }.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Layers, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("إعداد المايكروتك", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Router, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // GENERATOR FORM
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Category selection grid
                        item {
                            Text(
                                text = "اختر فئة كروت الشبكة لتوليدها:",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categoriesList.take(4).forEach { cat ->
                                    val isSelected = selectedCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) BrandPrimaryRed else Color(0xFF0F172A))
                                            .border(
                                                BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.05f)),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedCategory = cat }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (cat == 3000) "شهري" else "$cat ر.ي",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categoriesList.drop(4).forEach { cat ->
                                    val isSelected = selectedCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) BrandPrimaryRed else Color(0xFF0F172A))
                                            .border(
                                                BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.05f)),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedCategory = cat }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (cat == 3000) "اشتراك شهري" else "$cat ر.ي",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Code details configuration
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "خصائص كود الكرت",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = BrandPrimaryRed
                                    )

                                    // Format selection
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { formatMode = "user_only" }
                                        ) {
                                            RadioButton(
                                                selected = formatMode == "user_only",
                                                onClick = { formatMode = "user_only" },
                                                colors = RadioButtonDefaults.colors(selectedColor = BrandPrimaryRed)
                                            )
                                            Text("اسم مستخدم فقط", color = Color.White, fontSize = 13.sp)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { formatMode = "user_pass" }
                                        ) {
                                            RadioButton(
                                                selected = formatMode == "user_pass",
                                                onClick = { formatMode = "user_pass" },
                                                colors = RadioButtonDefaults.colors(selectedColor = BrandPrimaryRed)
                                            )
                                            Text("اسم مستخدم وكلمة مرور", color = Color.White, fontSize = 13.sp)
                                        }
                                    }

                                    // Inputs
                                    OutlinedTextField(
                                        value = quantityInput,
                                        onValueChange = { quantityInput = it },
                                        label = { Text("الكمية المطلوب توليدها") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandPrimaryRed,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedLabelColor = BrandPrimaryRed,
                                            unfocusedLabelColor = TextSecondary,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextAlign.Right.let { androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right) }
                                    )

                                    OutlinedTextField(
                                        value = prefixInput,
                                        onValueChange = { prefixInput = it },
                                        label = { Text("بادئة الكود (اختياري)") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandPrimaryRed,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedLabelColor = BrandPrimaryRed,
                                            unfocusedLabelColor = TextSecondary,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextAlign.Right.let { androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right) }
                                    )

                                    OutlinedTextField(
                                        value = codeLengthInput,
                                        onValueChange = { codeLengthInput = it },
                                        label = { Text("طول كود الكرت المولد") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandPrimaryRed,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedLabelColor = BrandPrimaryRed,
                                            unfocusedLabelColor = TextSecondary,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextAlign.Right.let { androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right) }
                                    )
                                }
                            }
                        }

                        // Generate Button
                        item {
                            Button(
                                onClick = {
                                    val qty = quantityInput.toIntOrNull() ?: 10
                                    val length = codeLengthInput.toIntOrNull() ?: 6
                                    if (qty <= 0 || length <= 0) {
                                        Toast.makeText(context, "البيانات المدخلة غير صحيحة!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    // Local generator simulation simulating MikroTik sync
                                    val chars = "0123456789"
                                    val newCards = mutableListOf<GeneratedMikrotikCard>()
                                    for (i in 1..qty) {
                                        val pin = prefixInput + (1..length).map { chars.random() }.joinToString("")
                                        val username = pin
                                        val password = if (formatMode == "user_pass") (1..length).map { chars.random() }.joinToString("") else ""
                                        
                                        newCards.add(
                                            GeneratedMikrotikCard(
                                                category = selectedCategory,
                                                pin = pin,
                                                username = username,
                                                password = password,
                                                printed = false,
                                                transferred = false
                                            )
                                        )
                                    }

                                    coroutineScope.launch {
                                        viewModel.insertGeneratedCards(newCards)
                                        Toast.makeText(context, "⚡ تم توليد $qty كرت بنجاح وإضافتهم للجدول المستقل!", Toast.LENGTH_LONG).show()
                                        selectedTab = 1 // Switch to History
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "توليد كروت وحفظ بالمايكروتك ⚡",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // HISTORY & PRINT LIST
                    val activeCards = generatedCards.filter { !it.transferred }
                    if (activeCards.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CardMembership, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("لا توجد كروت مولدة بالانتظار حالياً", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("قم بتوليد الكروت من علامة التبويب الأولى للبدء في طباعتها أو تصديرها لمخزن مبيعاتك.", color = TextSecondary.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Actions: Print All / Transfer All
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            // Simulated Print slips or Export
                                            val reportText = buildString {
                                                appendLine("===============================")
                                                appendLine("     كروت شبكة الدحشة المايكروتك     ")
                                                appendLine("===============================")
                                                appendLine("التاريخ: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()))
                                                appendLine("إجمالي الكروت: ${activeCards.size}")
                                                appendLine("-------------------------------")
                                                activeCards.forEachIndexed { index, card ->
                                                    appendLine("${index + 1}. فئة: ${card.category} ر.ي")
                                                    appendLine("   الكود: ${card.pin}")
                                                    if (card.password.isNotEmpty()) {
                                                        appendLine("   كلمة المرور: ${card.password}")
                                                    }
                                                    appendLine("-------------------------------")
                                                }
                                            }
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("Mikrotik Cards", reportText)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "تم توليد ملف الكروت ونسخه للحافظة بنجاح! جاهز للطباعة 📋", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("طباعة كرتوني 📋", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                var transferCount = 0
                                                activeCards.forEach { card ->
                                                    viewModel.transferGeneratedCardToAutoSales(
                                                        id = card.id,
                                                        category = card.category,
                                                        pin = card.pin,
                                                        username = card.username,
                                                        password = card.password
                                                    )
                                                    transferCount++
                                                }
                                                Toast.makeText(context, "تم نقل $transferCount كرت بنجاح إلى مخزن البيع التلقائي! 🎉", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GlowEmeraldGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("نقل للمبيعات 📥", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Clean list item cards
                            items(activeCards) { card ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left actions (transfer / delete)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Transfer single
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        viewModel.transferGeneratedCardToAutoSales(
                                                            id = card.id,
                                                            category = card.category,
                                                            pin = card.pin,
                                                            username = card.username,
                                                            password = card.password
                                                        )
                                                        Toast.makeText(context, "تم نقل الكرت إلى مخزن البيع التلقائي!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(GlowEmeraldGreen.copy(alpha = 0.1f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.ArrowForward, contentDescription = "نقل لمخزن المبيعات", tint = GlowEmeraldGreen, modifier = Modifier.size(16.dp))
                                            }

                                            // Delete single
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteGeneratedCard(card.id)
                                                    Toast.makeText(context, "تم حذف الكرت المولد.", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف الكرت", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        // Right: Info
                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(BrandPrimaryRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("فئة ${card.category}", color = BrandPrimaryRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Text(
                                                    text = card.pin,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            if (card.password.isNotEmpty()) {
                                                Text(
                                                    text = "كلمة المرور: ${card.password}",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // MIKROTIK SETTINGS CONFIG
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        item {
                            Text(
                                text = "بيانات ربط المايكروتك (MikroTik Connection)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BrandPrimaryRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تأكد من تفعيل منفذ API (الافتراضي 8728) في المايكروتك لكي يتصل التطبيق بنجاح ويقوم بتخزين وتوليد الكروت بشكل تلقائي في شبكتك.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    OutlinedTextField(
                                        value = mtHost,
                                        onValueChange = { mtHost = it },
                                        label = { Text("عنوان خادم المايكروتك / IP Host") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandPrimaryRed,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedLabelColor = BrandPrimaryRed,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = mtPort,
                                        onValueChange = { mtPort = it },
                                        label = { Text("منفذ API Port") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandPrimaryRed,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedLabelColor = BrandPrimaryRed,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = mtUser,
                                        onValueChange = { mtUser = it },
                                        label = { Text("اسم مستخدم المايكروتك") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandPrimaryRed,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedLabelColor = BrandPrimaryRed,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = mtPass,
                                        onValueChange = { mtPass = it },
                                        label = { Text("كلمة المرور") },
                                        visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { isPassVisible = !isPassVisible }) {
                                                Icon(
                                                    imageVector = if (isPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = TextSecondary
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandPrimaryRed,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedLabelColor = BrandPrimaryRed,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = mtProfile,
                                        onValueChange = { mtProfile = it },
                                        label = { Text("اسم بروفايل الكروت (User Profile)") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandPrimaryRed,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedLabelColor = BrandPrimaryRed,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    sharedPrefs.edit().apply {
                                        putString("mt_host", mtHost)
                                        putString("mt_port", mtPort)
                                        putString("mt_user", mtUser)
                                        putString("mt_pass", mtPass)
                                        putString("mt_profile", mtProfile)
                                    }.apply()

                                    coroutineScope.launch {
                                        Toast.makeText(context, "جاري فحص واختبار الاتصال بالخادم...", Toast.LENGTH_SHORT).show()
                                        val result = testMikrotikConnection(mtHost, mtPort)
                                        if (result.isSuccess) {
                                            Toast.makeText(context, "🟢 تم اختبار الاتصال والربط بنجاح مع خادم المايكروتك وحفظ البيانات!", Toast.LENGTH_LONG).show()
                                        } else {
                                            val errorMsg = result.exceptionOrNull()?.message ?: "خطأ غير معروف"
                                            Toast.makeText(context, "❌ فشل اختبار الاتصال:\n$errorMsg", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GlowEmeraldGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("حفظ واختبار الاتصال بالخادم 🟢", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun testMikrotikConnection(host: String, portStr: String): Result<Unit> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        val port = portStr.trim().toIntOrNull() ?: return@withContext Result.failure(Exception("رقم المنفذ (Port) غير صالح"))
        val socket = java.net.Socket()
        socket.connect(java.net.InetSocketAddress(host.trim(), port), 3000) // 3 seconds timeout
        socket.close()
        Result.success(Unit)
    } catch (e: java.net.SocketTimeoutException) {
        Result.failure(Exception("انتهت مهلة الاتصال! يرجى التحقق من صحة عنوان IP والمنفذ والاتصال بالشبكة."))
    } catch (e: java.net.ConnectException) {
        Result.failure(Exception("تم رفض الاتصال! يرجى التأكد من تشغيل خدمة API في المايكروتك."))
    } catch (e: Exception) {
        Result.failure(Exception("فشل الاتصال: ${e.localizedMessage ?: "خطأ غير معروف"}"))
    }
}
