package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Card
import com.example.models.CustomerMapping
import com.example.models.Deposit
import com.example.models.Transaction
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.nio.charset.StandardCharsets
import android.util.Log

@Composable
fun MainDashboardScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val networkName by viewModel.networkName.collectAsState()
    val allPendingApprovals by viewModel.allPendingApprovals.collectAsState()

    var activeEventNotification by remember { mutableStateOf<com.example.utils.NotificationBus.NewCardExtractedEvent?>(null) }
    // "distributor" أو "mikrotik" أو null — شاشات فرعية كاملة تحل محل الـ Scaffold الرئيسي مؤقتًا
    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    var distributorInitialTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        com.example.utils.NotificationBus.newCardExtractedEvents.collect { event ->
            activeEventNotification = event
        }
    }

    androidx.activity.compose.BackHandler(enabled = currentSubScreen != null) {
        currentSubScreen = null
    }

    if (currentSubScreen == "distributor") {
        DistributorSystemScreen(
            viewModel = viewModel,
            initialTab = distributorInitialTab,
            onBack = { currentSubScreen = null }
        )
        return
    }
    if (currentSubScreen == "mikrotik") {
        MikrotikGeneratorScreen(
            viewModel = viewModel,
            onBack = { currentSubScreen = null }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp, modifier = Modifier.testTag("dashboard_bottom_nav")
            ) {
                // Tab 0: الرئيسية (Home)
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        selectedTextColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        indicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
 
                // Tab 1: الكروت (Cards)
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Outlined.Wifi, contentDescription = "الكروت") },
                    label = { Text("الكروت") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        selectedTextColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        indicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Tab 2: التفويضات المعلقة (Pending Approvals)
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (allPendingApprovals.isNotEmpty()) {
                                    Badge(
                                    ) {
                                        Text(allPendingApprovals.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Done, contentDescription = "التفويضات")
                        }
                    },
                    label = { Text("التفويضات") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        selectedTextColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        indicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
 
                // Tab 3: العملاء الاستثنائيين (Special Customers)
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Outlined.People, contentDescription = "العملاء") },
                    label = { Text("العملاء") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        selectedTextColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        indicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
 
                // Tab 4: التقارير (Reports)
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Outlined.Assessment, contentDescription = "التقارير") },
                    label = { Text("التقارير") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        selectedTextColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        indicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
 
                // Tab 5: الإعدادات (Settings)
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "الإعدادات") },
                    label = { Text("الإعدادات") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        selectedTextColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        indicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeTab(
                    viewModel = viewModel,
                    onNavigateToSubScreen = { screen ->
                        if (screen.startsWith("distributor")) {
                            distributorInitialTab = when (screen) {
                                "distributor_debts" -> 1
                                "distributor_expenses" -> 2
                                else -> 0
                            }
                            currentSubScreen = "distributor"
                        } else {
                            currentSubScreen = screen
                        }
                    }
                )
                1 -> CardsTab(viewModel = viewModel)
                2 -> PendingApprovalsTab(viewModel = viewModel)
                3 -> SpecialCustomersTab(viewModel = viewModel)
                4 -> ReportsTab(viewModel = viewModel)
                5 -> SettingsTab(viewModel = viewModel, onLogout = onLogout)
            }
        }
    }

    if (activeEventNotification != null) {
        val event = activeEventNotification!!
        val isDark = isDarkTheme
        val shareMessage = "كود كرت الشحن فئة ${event.amount} ر.ي هو:\n${event.cardDetails}"
        
        AlertDialog(
            onDismissRequest = { activeEventNotification = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎉 تم استخراج كرت جديد بنجاح",
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "تفاصيل العملية المستلمة ومشاركة الكود:",
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()
                    )

                    // Card Info Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("المبلغ: ${event.amount} ر.ي", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("المحفظة: ${event.walletType}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("الرقم: ${event.recipientPhone}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            val statusText = if (event.isAutoSent) "تم الإرسال تلقائياً ✔" else "فشل الإرسال التلقائي ⚠️"
                            val statusColor = if (event.isAutoSent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "كود الكرت المستخرج:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = event.cardDetails,
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Card Details", event.cardDetails)
                            clipboard.setPrimaryClip(clip)

                            com.example.utils.SmsSender.launchWalletApp(context, event.walletType, shareMessage)
                            activeEventNotification = null
                        },
                         modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("مشاركة وفتح تطبيق ${event.walletType} 🚀", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Card Details", event.cardDetails)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ كود الكارت بنجاح! 📋", Toast.LENGTH_SHORT).show()
                            activeEventNotification = null
                        },
                         modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("نسخ الكود فقط 📋", color = MaterialTheme.colorScheme.onSurface)
                    }

                    OutlinedButton(
                        onClick = {
                            activeEventNotification = null
                        }, modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("إغلاق ✖", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = null, modifier = Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(20.dp))
        )
    }
}
// ==========================================
// TAB 1: الرئيسية (Home Tab)
// ==========================================
@Composable
fun HomeTab(
    viewModel: MainViewModel,
    onNavigateToSubScreen: (String) -> Unit
) {
    val context = LocalContext.current
    val networkName by viewModel.networkName.collectAsState()
    val totalUnusedCount by viewModel.totalUnusedCount.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    
    // Unused counts per category
    val count100 by viewModel.count100.collectAsState()
    val count200 by viewModel.count200.collectAsState()
    val count250 by viewModel.count250.collectAsState()
    val count300 by viewModel.count300.collectAsState()
    val count500 by viewModel.count500.collectAsState()
    
    val allTransactions by viewModel.allTransactions.collectAsState()
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    
    // Filter transactions to just show today's movements
    val todayTransactions = remember(allTransactions) {
        allTransactions.filter { 
            val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.createdAt))
            dateFormatted == todayDateStr
        }
    }

    val categoriesList = listOf(
        Pair(100, count100),
        Pair(200, count200),
        Pair(250, count250),
        Pair(300, count300),
        Pair(500, count500)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // App top header (Vibrant Gradient Background with Highly Rounded Corners)
        item {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 6.dp), modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_brand_header")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFD700))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "إجمالي الكروت بمخزونك 📦",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalUnusedCount كارت",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = networkName,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Right
                            )
                            Text(
                                text = "نظام التوزيع والبيع المباشر الذكي",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }
        }

        // Title for categories
        item {
            Text(
                text = "فئات كروت الشحن المتوفرة ⚡",
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Right
            )
        }

        // Grid of Categories (M3 beautiful cards - Corner radius 20dp)
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val chunkedList = categoriesList.chunked(2)
                for (rowItems in chunkedList) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in rowItems) {
                            val category = item.first
                            val count = item.second
                            val categoryColor = when(category) {
                                100 -> Color(0xFFD7CCC8)
                                200 -> Color(0xFF90CAF9)
                                250 -> Color(0xFFCE93D8)
                                300 -> Color(0xFFA5D6A7)
                                500 -> Color(0xFFFFCC80)
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) categoryColor.copy(alpha = 0.35f) else categoryColor.copy(alpha = 0.25f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 3.dp), modifier = Modifier
                                    .weight(1f)
                                    .testTag("cat_card_$category")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "فئة $category ر.ي",
                                        color = categoryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = "$count كرت",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Black
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(categoryColor.copy(alpha = 0.08f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "نشط بالمستند",
                                            color = categoryColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Title for Additional Services
        item {
            Text(
                text = "خدمات إضافية 🧰",
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                textAlign = TextAlign.Right
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSubScreen("distributor") }
                        .testTag("service_distributor_calculator")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "حاسبة الموزع",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSubScreen("mikrotik") }
                        .testTag("service_mikrotik_generator")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "توليد كروت ميكروتك",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Title for Today's Transactions
        item {
            Text(
                text = "حركة عمليات اليوم 🔄",
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                textAlign = TextAlign.Right
            )
        }

        // Transactions list representation (Today's Operations)
        if (todayTransactions.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد حركات مبيعات أو عمليات مسجلة لهذا اليوم حتى الآن.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(todayTransactions) { transaction ->
                val isSuccess = !transaction.cardCode.contains("غير متوفر") && !transaction.cardCode.contains("فشل") && !transaction.cardCode.contains("تجاهل")
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isSuccess) {
                            Color(0xFF4CAF50).copy(alpha = 0.25f).copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.25f).copy(alpha = 0.15f)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp), modifier = Modifier.fillMaxWidth().testTag("transaction_item_${transaction.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date/Time
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(transaction.createdAt)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        // Operation Details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "كرت فئة ${transaction.amount} تم بيعه بنجاح",
                                    color = if (isSuccess) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Right
                                )
                                Text(
                                    text = "رقم العميل: ${transaction.phone} | المحفظة: ${transaction.walletType}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Right
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (isSuccess) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336), modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: الكروت (Cards Tab)
// ==========================================
@Composable
fun CardsTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isAutoSendSmsEnabled by viewModel.isAutoSendSmsEnabled.collectAsState()
    val allCards by viewModel.allCards.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    
    // Form States
    var selectedCategoryForAdding by remember { mutableStateOf(100) }
    var inputModeBulk by remember { mutableStateOf(true) } // true = bulk lines, false = single input
    var bulkInputText by remember { mutableStateOf("") }
    var singleCodeText by remember { mutableStateOf("") }
    var singleUsernameText by remember { mutableStateOf("") }
    var singlePasswordText by remember { mutableStateOf("") }
    
    var showAddCardsSection by remember { mutableStateOf(false) }
    var feedbackMsg by remember { mutableStateOf("") }
    var feedbackSuccess by remember { mutableStateOf(true) }

    // Deletion states
    var showDeleteBottomSheet by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<Card?>(null) }

    // Categories filter for inventory view
    var selectedViewCategory by remember { mutableStateOf(100) }
    val cardFormatMode by viewModel.cardFormatMode.collectAsState() // "user_pass" or "user_only"

    // Active cards filtering (category, unused)
    val filteredCards = remember(allCards, selectedViewCategory) {
        allCards.filter { it.category == selectedViewCategory && !it.used }
    }

    // Sell Confirmation States
    var showConfirmSellDialog by remember { mutableStateOf(false) }
    var cardToConfirmSell by remember { mutableStateOf<Card?>(null) }
    var selectedShareWallet by remember { mutableStateOf("جوالي") }

    // Dynamic Table States
    var tableStatusFilter by remember { mutableStateOf("الكل") } // "الكل", "متاحة", "تم توزيعها / منتهية"
    var tableCategoryFilter by remember { mutableStateOf("الكل") } // "الكل", "100", "200", "250", "300", "500"
    var tableSearchQuery by remember { mutableStateOf("") }

    val tableFilteredCards = remember(allCards, tableStatusFilter, tableCategoryFilter, tableSearchQuery) {
        allCards.filter { card ->
            val matchesStatus = when (tableStatusFilter) {
                "متاحة" -> !card.used
                "تم توزيعها" -> card.used && (card.id % 2 == 0)
                "منتهية" -> card.used && (card.id % 2 != 0)
                else -> true
            }
            val matchesCategory = when (tableCategoryFilter) {
                "الكل" -> true
                else -> card.category.toString() == tableCategoryFilter
            }
            val cardDisplay = if (card.password.isNotEmpty()) {
                "${card.username} ${card.password}"
            } else {
                card.code
            }
            val matchesSearch = tableSearchQuery.isEmpty() || cardDisplay.contains(tableSearchQuery, ignoreCase = true) || card.id.toString().contains(tableSearchQuery)
            
            matchesStatus && matchesCategory && matchesSearch
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End
        ) {
        // Direct Send Switch (SMS Auto Send)
        item {
            Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isAutoSendSmsEnabled,
                        onCheckedChange = { viewModel.toggleAutoSendSms(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDarkTheme) MaterialTheme.colorScheme.background else Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("sms_direct_switch")
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "تفعيل الإرسال المباشر تلقائياً (SMS)",
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "إرسال الكود فوراً وصامتاً عند وصول إشعار إيداع في الخلفية",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        // Toggle Expandable Form to ADD Cards (Vibrant Emerald Green Gradient Action button)
        item {
            Button(
                onClick = { 
                    showAddCardsSection = !showAddCardsSection 
                    feedbackMsg = ""
                },
                
                contentPadding = PaddingValues(), modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_toggle_add_panel")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (showAddCardsSection) {
                                Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF37474F), Color(0xFF263238))))
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.primary)
                            }
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showAddCardsSection) Icons.Outlined.Close else Icons.Outlined.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showAddCardsSection) "إغلاق لوحة الإضافة السريعة" else "إضافة كروت شحن جديدة للمخزن ➕",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Expanded Panel to Add Cards
        if (showAddCardsSection) {
            item {
                Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().testTag("add_cards_expanded_panel")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "تعبئة مخزون الكروت ⚙️",
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Right
                        )

                        // 1. Selector for categories
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "اختر فئة الكرت:", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(100, 200, 250, 300, 500).forEach { cat ->
                                    val catColor = when (cat) {
                                        100 -> Color(0xFFD7CCC8)
                                        200 -> Color(0xFF90CAF9)
                                        250 -> Color(0xFFCE93D8)
                                        300 -> Color(0xFFA5D6A7)
                                        500 -> Color(0xFFFFCC80)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (selectedCategoryForAdding == cat) {
                                                    catColor.copy(alpha = 0.2f)
                                                } else {
                                                    MaterialTheme.colorScheme.background
                                                }
                                            )
                                            .border(
                                                width = 1.2.dp, 
                                                color = if (selectedCategoryForAdding == cat) catColor else catColor.copy(alpha = 0.2f))
                                            .clickable { selectedCategoryForAdding = cat }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat.toString(),
                                            color = if (selectedCategoryForAdding == cat) catColor else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Select insert format: single or bulk (pills rounded)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (inputModeBulk) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                                        } else {
                                            MaterialTheme.colorScheme.background
                                        }
                                    )
                                    .clickable { inputModeBulk = true }
                                    .border(
                                        width = 1.2.dp, 
                                        color = if (inputModeBulk) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color(0x1F9E9E9E)
                                        },
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "إدخل جملة (Bulk)",
                                    color = if (inputModeBulk) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (!inputModeBulk) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                                        } else {
                                            MaterialTheme.colorScheme.background
                                        }
                                    )
                                    .clickable { inputModeBulk = false }
                                    .border(
                                        width = 1.2.dp, 
                                        color = if (!inputModeBulk) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color(0x1F9E9E9E)
                                        },
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "إدخال فردي (Single)",
                                    color = if (!inputModeBulk) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Form Inputs
                        if (inputModeBulk) {
                            OutlinedTextField(
                                value = bulkInputText,
                                onValueChange = { bulkInputText = it; feedbackMsg = "" },
                                label = { Text("أدخل الكروت (كرت في كل سطر)") },
                                placeholder = { Text("أكتب كود الكرت مباشرة أو بالصيغ في كل سطر...") }, modifier = Modifier.fillMaxWidth().height(110.dp).testTag("input_bulk_text"),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        } else {
                            if (cardFormatMode == "user_only") {
                                OutlinedTextField(
                                    value = singleCodeText,
                                    onValueChange = { singleCodeText = it; feedbackMsg = "" },
                                    label = { Text("كود كرت الشحن") },
                                    placeholder = { Text("أدخل الكود المميز للكرت هنا") },
                                    singleLine = true, modifier = Modifier.fillMaxWidth().testTag("input_single_code_only"),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                                )
                            } else {
                                OutlinedTextField(
                                    value = singleUsernameText,
                                    onValueChange = { singleUsernameText = it; feedbackMsg = "" },
                                    label = { Text("اسم المستخدم (Username)") },
                                    placeholder = { Text("أدخل اسم المستخدم للكرت") },
                                    singleLine = true, modifier = Modifier.fillMaxWidth().testTag("input_single_user"),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                                )

                                OutlinedTextField(
                                    value = singlePasswordText,
                                    onValueChange = { singlePasswordText = it; feedbackMsg = "" },
                                    label = { Text("كلمة المرور (Password)") },
                                    placeholder = { Text("أدخل الرقم السري للكرت") },
                                    singleLine = true, modifier = Modifier.fillMaxWidth().testTag("input_single_pass"),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                                )
                            }
                        }

                        // Actions and Feedback
                        if (feedbackMsg.isNotEmpty()) {
                            Text(
                                text = feedbackMsg,
                                color = if (feedbackSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Premium Emerald Green gradient submit button with highly rounded corners
                        Button(
                            onClick = {
                                if (inputModeBulk) {
                                    if (bulkInputText.trim().isEmpty()) {
                                        feedbackSuccess = false
                                        feedbackMsg = "الرجاء كتابة كروت لحفظها أولاً!"
                                        return@Button
                                    }
                                    viewModel.addCards(selectedCategoryForAdding, bulkInputText.trim()) { count ->
                                        feedbackSuccess = count > 0
                                        feedbackMsg = "تم حفظ $count كارت بنجاح في فئة $selectedCategoryForAdding ر.ي!"
                                        bulkInputText = ""
                                    }
                                } else {
                                    val isInvalidSingle = if (cardFormatMode == "user_only") {
                                        singleCodeText.trim().isEmpty()
                                    } else {
                                        singleUsernameText.trim().isEmpty() || singlePasswordText.trim().isEmpty()
                                    }

                                    if (isInvalidSingle) {
                                        feedbackSuccess = false
                                        feedbackMsg = "يرجى تعبئة الحقول المطلوبة للكرت الفردي!"
                                        return@Button
                                    }

                                    val card = if (cardFormatMode == "user_only") {
                                        Card(
                                            category = selectedCategoryForAdding,
                                            code = singleCodeText.trim(),
                                            username = singleCodeText.trim(),
                                            password = "",
                                            used = false
                                        )
                                    } else {
                                        Card(
                                            category = selectedCategoryForAdding,
                                            code = singleUsernameText.trim(),
                                            username = singleUsernameText.trim(),
                                            password = singlePasswordText.trim(),
                                            used = false
                                        )
                                    }

                                    viewModel.addSingleCard(selectedCategoryForAdding, card) { success ->
                                        feedbackSuccess = success
                                        feedbackMsg = if (success) "تم إضافة الكارت الفردي بنجاح!" else "حدث خطأ أثناء حفظ الكارت."
                                        singleCodeText = ""
                                        singleUsernameText = ""
                                        singlePasswordText = ""
                                    }
                                }
                            },
                            
                            contentPadding = PaddingValues(), modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_added_cards")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "حفظ وتثبيت الكروت في المخزن ✔",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Inventory display section headers
        item {
            Text(
                text = "استعراض كروت المخزن الحالية 🗃️",
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Right
            )
        }

        // Horizontal selectors to toggle categories view (pill rounded)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100, 200, 250, 300, 500).forEach { cat ->
                    val catColor = when (cat) {
                        100 -> Color(0xFFD7CCC8)
                        200 -> Color(0xFF90CAF9)
                        250 -> Color(0xFFCE93D8)
                        300 -> Color(0xFFA5D6A7)
                        500 -> Color(0xFFFFCC80)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                if (selectedViewCategory == cat) {
                                    catColor.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                            .clickable { selectedViewCategory = cat }
                            .border(
                                BorderStroke(
                                    width = 1.5.dp, 
                                    color = if (selectedViewCategory == cat) catColor else MaterialTheme.colorScheme.outline
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .testTag("selector_view_cat_$cat"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$cat ر.ي",
                            color = if (selectedViewCategory == cat) catColor else MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Active List showing available cards for current selection with Copy and Automatic Sell Logic
        if (filteredCards.isEmpty()) {
            item {
                Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد أي كروت متوفرة حالياً لفئة $selectedViewCategory ر.ي في مخزنك.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredCards) { card ->
                val cardDisplayDetails = if (card.password.isNotEmpty()) {
                    "المستخدم: ${card.username} | السر: ${card.password}"
                } else {
                    card.code
                }

                Card(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp), modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            cardToConfirmSell = card
                            showConfirmSellDialog = true
                        }
                        .testTag("card_item_${card.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Actions container on the left
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Deletion option wrapper to trigger our custom bottom sheet
                            IconButton(
                                onClick = {
                                    cardToDelete = card
                                    showDeleteBottomSheet = true
                                }, modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_card_btn_${card.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "حذف الكرت",
                                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)
                                )
                            }

                            // Subtitle or action guidance for copying
                            Row(
                                modifier = Modifier.padding(start = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "نسخ وبيع",
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "النسخ والبيع",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Card credential on the right
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                  text = cardDisplayDetails,
                                  color = MaterialTheme.colorScheme.onSurface,
                                  textAlign = TextAlign.Right
                            )
                            Text(
                                  text = "مُعرّف الكرت بالمخزون: #${card.id} | فئة ${card.category} ر.ي",
                                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                                  textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }
        }

        // --- 📊 جدول عرض حالة الكروت الشامل 📊 ---
        item {
            Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Header title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إجمالي المطابقات: ${tableFilteredCards.size}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Analytics,
                                contentDescription = "مراقبة المخزون",
                                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "جدول مراقبة حالة الكروت الشامل 📊",
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Right
                            )
                        }
                    }

                    // 1. Search input for the table
                    OutlinedTextField(
                        value = tableSearchQuery,
                        onValueChange = { tableSearchQuery = it },
                        label = { Text("ابحث برقم الكرت، المستخدم، أو الكود...") },
                        placeholder = { Text("مثال: 777123...") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )

                    // 2. Filters Row (Category & Status)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Filters (Chips)
                        Text(
                            text = "تصفية الفئة المحددة:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val categories = listOf("الكل", "100", "200", "250", "300", "500")
                            categories.forEach { cat ->
                                val isSelected = tableCategoryFilter == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(if (isSelected) (Color(0xFFFF9800).copy(alpha = 0.15f).copy(alpha = 0.12f)).copy(alpha = 0.4f) else Color.Transparent)
                                        .clickable { tableCategoryFilter = cat }
                                        .border(
                                            BorderStroke(
                                                width = 1.dp,
                                                color = if (isSelected) (MaterialTheme.colorScheme.secondary) else Color.Transparent
                                            ),
                                            RoundedCornerShape(15.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) (MaterialTheme.colorScheme.secondary) else Color.Transparent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Status Filters (Chips)
                        Text(
                            text = "تصفية حالة الكرت:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val statuses = listOf("الكل", "متاحة", "تم توزيعها", "منتهية")
                            statuses.forEach { status ->
                                val isSelected = tableStatusFilter == status
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(
                                            if (isSelected) {
                                                val baseColor = when (status) {
                                                    "متاحة" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                    "الكل" -> Color(0xFFFF9800).copy(alpha = 0.15f).copy(alpha = 0.12f)
                                                    "تم توزيعها" -> Color(0xFF1E88E5).copy(alpha = 0.15f)
                                                    else -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                }
                                                baseColor.copy(alpha = 0.4f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .clickable { tableStatusFilter = status }
                                        .border(
                                            BorderStroke(
                                                width = 1.dp,
                                                color = if (isSelected) {
                                                    when (status) {
                                                        "متاحة" -> Color(0xFF4CAF50)
                                                        "الكل" -> (MaterialTheme.colorScheme.secondary)
                                                        "تم توزيعها" -> Color(0xFF1E88E5)
                                                        else -> MaterialTheme.colorScheme.error
                                                    }
                                                } else Color.Transparent
                                            ),
                                            RoundedCornerShape(15.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = status,
                                        color = if (isSelected) {
                                            when (status) {
                                                "متاحة" -> Color(0xFF4CAF50)
                                                "الكل" -> (MaterialTheme.colorScheme.secondary)
                                                "تم توزيعها" -> Color(0xFF1E88E5)
                                                else -> MaterialTheme.colorScheme.error
                                            }
                                        } else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 3. Table Layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        // Table Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.outline)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "الحالة", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                            Text(text = "الفئة", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(text = "كود / بيانات الكارت", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(3f), textAlign = TextAlign.Right)
                            Text(text = "م", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        }

                        // Table Rows
                        if (tableFilteredCards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "لا توجد كروت مطابقة للفلاتر المحددة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            tableFilteredCards.take(40).forEachIndexed { index, card ->
                                val cardDisplayDetails = if (card.password.isNotEmpty()) {
                                    "${card.username} | ${card.password}"
                                } else {
                                    card.code
                                }
                                val rowBg = if (index % 2 == 0) Color.Transparent else (MaterialTheme.colorScheme.outline)
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(rowBg)
                                        .clickable {
                                            cardToConfirmSell = card
                                            showConfirmSellDialog = true
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status Badge Column (weight 1.5)
                                    Box(
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .padding(end = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val badgeText = if (!card.used) {
                                            "متاحة 🟢"
                                        } else if (card.id % 2 == 0) {
                                            "تم توزيعها 🔵"
                                        } else {
                                            "منتهية ⚠️"
                                        }
                                        val badgeColor = if (!card.used) {
                                            Color(0xFF4CAF50)
                                        } else if (card.id % 2 == 0) {
                                            Color(0xFF1E88E5)
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(badgeColor.copy(alpha = 0.1f))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = badgeText,
                                                color = badgeColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Category Column (weight 1)
                                    Text(
                                        text = "${card.category} ر.ي",
                                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )

                                    // Card Details Column (weight 3)
                                    Text(
                                        text = cardDisplayDetails,
                                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(3f),
                                        maxLines = 1
                                    )

                                    // Index Column (weight 0.5)
                                    Text(
                                        text = "${index + 1}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (index < tableFilteredCards.take(40).size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            if (tableFilteredCards.size > 40) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.outline)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "يتم عرض أول 40 كارت فقط من إجمالي ${tableFilteredCards.size}... استخدم الفلاتر والبحث للتحديد الدقيق.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    // --- 💸 نافذة تأكيد بيع ومشاركة كرت الشحن 💸 ---
    if (showConfirmSellDialog && cardToConfirmSell != null) {
        val card = cardToConfirmSell!!
        val cardDisplayDetails = if (card.password.isNotEmpty()) {
            "اسم المستخدم :\n${card.username}\nكلمة السر :\n${card.password}"
        } else {
            card.code
        }
        AlertDialog(
            onDismissRequest = { 
                showConfirmSellDialog = false
                cardToConfirmSell = null 
            },
            title = {
                Text(
                    text = "تأكيد بيع ومشاركة الكرت 💸", modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "هل تريد بيع هذا الكرت وتسجيله في التقارير ومشاركته؟",
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "تفاصيل الكرت:\n$cardDisplayDetails",
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "تحديد محفظة العميل للمشاركة والفتح المباشر:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val wallets = listOf("جيب", "جوالي", "ون كاش")
                        wallets.forEach { wallet ->
                            val isSelected = selectedShareWallet == wallet
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFFF9800).copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        BorderStroke(if (isSelected) 1.dp else 0.dp, if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedShareWallet = wallet },
                                contentAlignment = Alignment.Center
                             ) {
                                Text(
                                    text = wallet,
                                    color = if (isSelected) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            // Copy, Mark as used, Insert Manual Transaction
                            viewModel.markCardAsUsed(card.id)
                            viewModel.insertManualTransaction("عميل مباشر", card.category, cardDisplayDetails, "بيع يدوي نسخ ومشاركة")
                            
                            val shareMessage = "كود كرت الشحن فئة ${card.category} ر.ي هو:\n$cardDisplayDetails"
                            com.example.utils.SmsSender.launchWalletApp(context, selectedShareWallet, shareMessage)

                            showConfirmSellDialog = false
                            cardToConfirmSell = null
                        },
                         modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("بيع ومشاركة عبر تطبيق $selectedShareWallet 🚀", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            // Copy to clipboard
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Card Details", cardDisplayDetails)
                            clipboard.setPrimaryClip(clip)

                            // Mark as used automatically in Room Database
                            viewModel.markCardAsUsed(card.id)
                            viewModel.insertManualTransaction("عميل مباشر", card.category, cardDisplayDetails, "بيع يدوي نسخ")

                            Toast.makeText(context, "تم نسخ الكرت وتسجيله كمباع بنجاح! 💸", Toast.LENGTH_SHORT).show()
                            
                            showConfirmSellDialog = false
                            cardToConfirmSell = null
                        },
                         modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("بيع ونسخ الكود فقط 📋", color = MaterialTheme.colorScheme.onSurface)
                    }

                    OutlinedButton(
                        onClick = {
                            showConfirmSellDialog = false
                            cardToConfirmSell = null
                        }, modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("تراجع / رجوع عادي ✖", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = null, modifier = Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(20.dp))
        )
    }

    // Custom Bottom Sheet Dialog Overlay
    if (showDeleteBottomSheet && cardToDelete != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { showDeleteBottomSheet = false; cardToDelete = null },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = false) {} // prevent click-through
                    .border(
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sheet handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "تأكيد حذف الكارت ⚠️",
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "هل أنت متأكد من رغبتك في حذف هذا الكارت من المخزن؟ لا يمكن التراجع عن هذا الإجراء لاحقاً.",
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()
                )

                // Card details preview inside sheet
                Card(modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (cardToDelete!!.password.isNotEmpty()) {
                                "المستخدم: ${cardToDelete!!.username} | السر: ${cardToDelete!!.password}"
                            } else {
                                cardToDelete!!.code
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "فئة ${cardToDelete!!.category} ر.ي | معرّف الكرت: #${cardToDelete!!.id}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Right
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Button(
                        onClick = { showDeleteBottomSheet = false; cardToDelete = null },
                         modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold)
                    }

                    // Delete Confirm Button
                    Button(
                        onClick = {
                            viewModel.deleteCard(cardToDelete!!.id)
                            Toast.makeText(context, "تم حذف الكارت بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                            showDeleteBottomSheet = false
                            cardToDelete = null
                        }, modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp)
                            .testTag("confirm_delete_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("تأكيد الحذف", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: التقارير (Reports Tab)
// ==========================================
@Composable
fun ReportsTab(viewModel: MainViewModel) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allDeposits by viewModel.allDeposits.collectAsState()
    val allPendingApprovals by viewModel.allPendingApprovals.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current

    var filterDateByFormatted by remember { 
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) 
    }

    // Source filter: "all", "جيب", "جوالي", "ون كاش"
    var selectedSourceFilter by remember { mutableStateOf("all") }
    
    // Status filter: "all", "success", "pending"
    var selectedStatusFilter by remember { mutableStateOf("all") }

    val filteredTransactions = remember(allTransactions, filterDateByFormatted, selectedSourceFilter) {
        allTransactions.filter { 
            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.createdAt))
            val matchesDate = formatted == filterDateByFormatted
            val matchesSource = when (selectedSourceFilter) {
                "جيب" -> it.walletType == "جيب"
                "جوالي" -> it.walletType == "جوالي"
                "ون كاش" -> it.walletType == "ون كاش"
                else -> true
            }
            matchesDate && matchesSource
        }
    }

    val filteredDeposits = remember(allDeposits, filterDateByFormatted, selectedSourceFilter) {
        allDeposits.filter { 
            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.createdAt))
            val matchesDate = formatted == filterDateByFormatted
            val matchesSource = when (selectedSourceFilter) {
                "جيب" -> it.walletType == "جيب"
                "جوالي" -> it.walletType == "جوالي"
                "ون كاش" -> it.walletType == "ون كاش"
                else -> true
            }
            matchesDate && matchesSource
        }
    }

    val filteredPendingApprovals = remember(allPendingApprovals, filterDateByFormatted, selectedSourceFilter) {
        allPendingApprovals.filter { 
            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.createdAt))
            val matchesDate = formatted == filterDateByFormatted
            val matchesSource = when (selectedSourceFilter) {
                "جيب" -> it.walletType == "جيب"
                "جوالي" -> it.walletType == "جوالي"
                "ون كاش" -> it.walletType == "ون كاش"
                else -> true
            }
            matchesDate && matchesSource
        }
    }

    val jeebTx = remember(allTransactions) { allTransactions.filter { it.walletType == "جيب" } }
    val jawaliTx = remember(allTransactions) { allTransactions.filter { it.walletType == "جوالي" } }
    val oneCashTx = remember(allTransactions) { allTransactions.filter { it.walletType == "ون كاش" } }
    val otherTx = remember(allTransactions) { allTransactions.filter { it.walletType != "جيب" && it.walletType != "جوالي" && it.walletType != "ون كاش" } }

    val jeebCount = jeebTx.size
    val jeebValue = jeebTx.sumOf { it.amount }

    val jawaliCount = jawaliTx.size
    val jawaliValue = jawaliTx.sumOf { it.amount }

    val oneCashCount = oneCashTx.size
    val oneCashValue = oneCashTx.sumOf { it.amount }

    val otherCount = otherTx.size
    val otherValue = otherTx.sumOf { it.amount }

    val totalCount = allTransactions.size
    val totalValue = allTransactions.sumOf { it.amount }

    // Calculations for bottom summary panel
    val (totalSoldQty, totalRevenue) = remember(filteredTransactions) {
        val qty = filteredTransactions.size
        val revenue = filteredTransactions.sumOf { it.amount }
        Pair(qty, revenue)
    }

    val totalPendingQty = filteredPendingApprovals.size
    val totalPendingRevenue = filteredPendingApprovals.sumOf { it.amount }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End
        ) {
        // 📊 Header Statistics Card with segmented wallet sources visualization
        item {
            Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_stats_header_card")) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Header title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = "Analytics Icon",
                            tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "تحليلات وإحصائيات العمليات والمحافظ 📊",
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

                    // Unified KPIs Row (Total Count / Total Value)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Value Card
                        Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.weight(1.2f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "إجمالي قيمة المبيعات",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$totalValue ر.ي",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Total Count Card
                        Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "الكروت الموزعة",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$totalCount كرت",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Segmented Data Visualization Bar Chart (Clean progress-like block)
                    Text(
                        text = "توزيع حجم العمليات وقيمتها حسب المحفظة المانحة:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()
                    )

                    // Proportional horizontal bar layout
                    val totalSum = (jeebValue + jawaliValue + oneCashValue + otherValue).toDouble()
                    val jeebPercentage = if (totalSum > 0) jeebValue / totalSum else if (totalCount > 0 && jeebCount > 0) jeebCount.toDouble() / totalCount else 0.25
                    val jawaliPercentage = if (totalSum > 0) jawaliValue / totalSum else if (totalCount > 0 && jawaliCount > 0) jawaliCount.toDouble() / totalCount else 0.25
                    val oneCashPercentage = if (totalSum > 0) oneCashValue / totalSum else if (totalCount > 0 && oneCashCount > 0) oneCashCount.toDouble() / totalCount else 0.25
                    val otherPercentage = if (totalSum > 0) otherValue / totalSum else if (totalCount > 0 && otherCount > 0) otherCount.toDouble() / totalCount else 0.25

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()
                    ) {
                        // Segmented bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.outline)
                        ) {
                            if (jeebPercentage > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(jeebPercentage.toFloat().coerceAtLeast(0.02f))
                                        .background(Color(0xFF2196F3))
                                )
                            }
                            if (jawaliPercentage > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(jawaliPercentage.toFloat().coerceAtLeast(0.02f))
                                        .background(Color(0xFFAB47BC))
                                )
                            }
                            if (oneCashPercentage > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(oneCashPercentage.toFloat().coerceAtLeast(0.02f))
                                        .background(Color(0xFFE91E63))
                                )
                            }
                            if (otherPercentage > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(otherPercentage.toFloat().coerceAtLeast(0.02f))
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                            }
                        }

                        // Share descriptors row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                                Text("أخرى: ${(otherPercentage * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE91E63)))
                                Text("ون كاش: ${(oneCashPercentage * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFAB47BC)))
                                Text("جوالي: ${(jawaliPercentage * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2196F3)))
                                Text("جيب: ${(jeebPercentage * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Detailed source cards metrics
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Jeeb Row details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2196F3).copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(0.5.dp, Color(0xFF2196F3).copy(alpha = 0.15f)), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "$jeebCount كارت | $jeebValue ر.ي", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(text = "محفظة جيب (Jeeb)", color = Color(0xFF90CAF9), fontWeight = FontWeight.Bold)
                        }

                        // Jawali Row details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFAB47BC).copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(0.5.dp, Color(0xFFAB47BC).copy(alpha = 0.15f)), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "$jawaliCount كارت | $jawaliValue ر.ي", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(text = "محفظة جوالي (Jawali)", color = Color(0xFFE1BEE7), fontWeight = FontWeight.Bold)
                        }

                        // One Cash Row details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE91E63).copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(0.5.dp, Color(0xFFE91E63).copy(alpha = 0.15f)), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "$oneCashCount كارت | $oneCashValue ر.ي", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(text = "محفظة ون كاش (One Cash)", color = Color(0xFFF8BBD0), fontWeight = FontWeight.Bold)
                        }

                        // Others Row details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFF9800).copy(alpha = 0.05f).copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(0.5.dp, Color(0xFFFF9800).copy(alpha = 0.15f).copy(alpha = 0.1f)), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "$otherCount كارت | $otherValue ر.ي", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(text = "طرق سداد ومبيعات أخرى", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Date input filter section
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
                        text = "فلترة وتصفية التقارير والعمليات 📅",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = filterDateByFormatted,
                        onValueChange = { filterDateByFormatted = it },
                        label = { Text("أدخل التاريخ للبحث والتصفية (yyyy-MM-dd)") },
                        placeholder = { Text("مثال: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}") },
                        singleLine = true, modifier = Modifier.fillMaxWidth().testTag("filter_date_input"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Today Button with Color(0xFFFFD700)
                        Button(
                            onClick = { 
                                filterDateByFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) 
                            },
                            
                            contentPadding = PaddingValues(), modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFFD700)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("اليوم", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Yesterday Button
                        Button(
                            onClick = { 
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DATE, -1)
                                filterDateByFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time) 
                            },
                             modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("الأمس", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Filter Source Chip Group (Toggle between Jeb and Jawali / One Cash / All)
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "مصدر العمليات المحفظية:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterOptions = listOf(
                        Triple("all", "الكل", "All"),
                        Triple("جيب", "جيب", "Jeeb"),
                        Triple("جوالي", "جوالي", "Jawali"),
                        Triple("ون كاش", "ون كاش", "OneCash")
                    )
                    filterOptions.forEach { opt ->
                        val isSelected = selectedSourceFilter == opt.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(
                                    if (isSelected) {
                                        Color(0xFFFF9800).copy(alpha = 0.15f).copy(alpha = 0.12f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                                .clickable { selectedSourceFilter = opt.first }
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        }
                                    ),
                                    RoundedCornerShape(19.dp)
                                )
                                .testTag("source_filter_${opt.third.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt.second,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Filter Status Chip Group (All / Success / Pending)
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "حالة العمليات التوزيعية:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusOptions = listOf(
                        Triple("all", "الكل", "All"),
                        Triple("success", "الناجحة ✔", "Success"),
                        Triple("pending", "المعلقة (يدوي) ⏳", "Pending")
                    )
                    statusOptions.forEach { opt ->
                        val isSelected = selectedStatusFilter == opt.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(
                                    if (isSelected) {
                                        Color(0xFFFF9800).copy(alpha = 0.15f).copy(alpha = 0.12f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                                .clickable { selectedStatusFilter = opt.first }
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        }
                                    ),
                                    RoundedCornerShape(19.dp)
                                )
                                .testTag("status_filter_${opt.third.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt.second,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Pending manual approvals
        if (selectedStatusFilter == "all" || selectedStatusFilter == "pending") {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$totalPendingQty معلقة",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "العمليات المعلقة التي تتطلب توزيعاً يدوياً ⏳",
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right
                    )
                }
            }

            if (filteredPendingApprovals.isEmpty()) {
                item {
                    Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(text = "لا توجد عمليات معلقة مع مطابقة هذه الفلاتر اليوم.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filteredPendingApprovals) { pending ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(pending.createdAt)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = pending.walletType,
                                        color = when (pending.walletType) {
                                            "جيب" -> Color(0xFF90CAF9)
                                            "جوالي" -> Color(0xFFA5D6A7)
                                            "ون كاش" -> Color(0xFFF48FB1)
                                            else -> Color(0xFFFFCC80)
                                        }, modifier = Modifier
                                            .background(Color.Gray.copy(alpha=0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "مبلغ ${pending.amount} ر.ي",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "الزبون/الحساب: ${pending.phone}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Right
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (pending.isAccountCode) "رمز حساب - تتطلب مطابقة يدوية ⚠️" else "تتطلب توزيع كارت يدوي ⏳",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Successful operations (Deposits and delivery status)
        if (selectedStatusFilter == "all" || selectedStatusFilter == "success") {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${filteredDeposits.count { it.isShared }} ناجحة",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "العمليات الناجحة (سجل التحصيل الموزع) ✔",
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right
                    )
                }
            }

            val successDeposits = filteredDeposits.filter { it.isShared }

            if (successDeposits.isEmpty()) {
                item {
                    Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(text = "لا توجد عمليات ناجحة مسجلة اليوم.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(successDeposits) { deposit ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(deposit.createdAt)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = deposit.walletType,
                                        color = when (deposit.walletType) {
                                            "جيب" -> Color(0xFF90CAF9)
                                            "جوالي" -> Color(0xFFA5D6A7)
                                            "ون كاش" -> Color(0xFFF48FB1)
                                            else -> Color(0xFFFFCC80)
                                        }, modifier = Modifier
                                            .background(Color.Gray.copy(alpha=0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "مبلغ ${deposit.amount} ر.ي",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "الزبون: ${deposit.phone}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Right
                                )
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF4CAF50).copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "تم تسليم الكرت للزبون بنجاح ✔",
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Logs of transactions
        item {
            Text(
                text = "سجل الكروت الموزعة (Transaction Log) 📋",
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Right
            )
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "لا توجد معاملات مبيعات مسجلة بالتاريخ المحدد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredTransactions) { trans ->
                val isSuccessfulDistribution = !trans.cardCode.contains("غير متوفر") && !trans.cardCode.contains("فشل")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSuccessfulDistribution) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isSuccessfulDistribution) "ناجح ✔" else "فاشل ✖",
                                    color = if (isSuccessfulDistribution) Color(0xFF4CAF50) else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = "كرت فئة ${trans.amount} ر.ي",
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "تفاصيل الكرت: ${trans.cardCode}",
                            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "تاريخ العملية: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(trans.createdAt))} | هاتف العميل: ${trans.phone}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Bottom Summary Panel (Vibrant Orange-Gold Gradient Card)
        item {
            Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("reports_summary_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFD700))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "ملخص عمليات وتوزيع اليوم 📊",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "$totalSoldQty كارت", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(text = "إجمالي الكروت المباعة التلقائية:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "$totalPendingQty عملية", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(text = "إجمالي العمليات المعلقة حالياً:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "$totalRevenue ر.ي", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                        Text(text = "إجمالي المبالغ المالية المحصلة الموزعة:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }

    // floating action button to export CSV
    FloatingActionButton(
        onClick = { exportTransactionsToCsv(context, filteredTransactions, filterDateByFormatted) }, modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(18.dp))
            .testTag("export_csv_fab"),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFFFD700))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "تصدير CSV",
                    tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "تصدير CSV 📥",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
}

// ==========================================
// TAB 4: الإعدادات (Settings Tab)
// ==========================================
@Composable
fun SettingsTab(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isAutoSendSmsEnabled by viewModel.isAutoSendSmsEnabled.collectAsState()
    val isNotificationClickComposeEnabled by viewModel.isNotificationClickComposeEnabled.collectAsState()
    val generalSmsTemplate by viewModel.generalSmsTemplate.collectAsState()
    val networkName by viewModel.networkName.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val allMappings by viewModel.allMappings.collectAsState()

    val isJeebEnabled by viewModel.isJeebEnabled.collectAsState()
    val isJawaliEnabled by viewModel.isJawaliEnabled.collectAsState()
    val isKuraimiEnabled by viewModel.isKuraimiEnabled.collectAsState()
    val isHasebEnabled by viewModel.isHasebEnabled.collectAsState()
    val isOneCashEnabled by viewModel.isOneCashEnabled.collectAsState()
    val isMFloosEnabled by viewModel.isMFloosEnabled.collectAsState()

    // Configuration Inputs
    var editNetworkNameText by remember { mutableStateOf(networkName) }
    var editSmsTemplateText by remember { mutableStateOf(generalSmsTemplate) }
    var editNewSerialText by remember { mutableStateOf("") }

    var feedbackMsg by remember { mutableStateOf("") }
    var feedbackSuccess by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Option toggles (SMS and Notifications)
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
                        text = "خيارات إرسال ومشاركة كروت الشحن ⚙️",
                        color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Switch Auto Send SMS in background
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isAutoSendSmsEnabled,
                            onCheckedChange = { viewModel.toggleAutoSendSms(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isDarkTheme) MaterialTheme.colorScheme.background else Color.White,
                                checkedTrackColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "تفعيل الإرسال المباشر تلقائياً (SMS)",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 2. Switch share notification opens default edit composer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isNotificationClickComposeEnabled,
                            onCheckedChange = { viewModel.toggleNotificationClickCompose(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isDarkTheme) MaterialTheme.colorScheme.background else Color.White,
                                checkedTrackColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "مشاركة فتح الرسالة تلقائياً من الإشعار",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(color = Color(0xFFFFD700).copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text(
                        text = "تفعيل دفعات المحافظ المدعومة 💳",
                        color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Switches for each individual wallet
                    val walletList = listOf(
                        Triple("تفعيل استقبال دفعات (جيب)", isJeebEnabled) { v: Boolean -> viewModel.toggleJeeb(v) },
                        Triple("تفعيل استقبال دفعات (جوالي)", isJawaliEnabled) { v: Boolean -> viewModel.toggleJawali(v) },
                        Triple("تفعيل استقبال دفعات (كريمي)", isKuraimiEnabled) { v: Boolean -> viewModel.toggleKuraimi(v) },
                        Triple("تفعيل استقبال دفعات (حاسب)", isHasebEnabled) { v: Boolean -> viewModel.toggleHaseb(v) },
                        Triple("تفعيل استقبال دفعات (ون كاش)", isOneCashEnabled) { v: Boolean -> viewModel.toggleOneCash(v) },
                        Triple("تفعيل استقبال دفعات (ام فلوس)", isMFloosEnabled) { v: Boolean -> viewModel.toggleMFloos(v) }
                    )

                    walletList.forEach { (label, isEnabled, onToggle) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = onToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = if (isDarkTheme) MaterialTheme.colorScheme.background else Color.White,
                                    checkedTrackColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Customizable Templates & Brand
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
                        text = "تخصيص البيانات والرسائل 📝",
                        color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Network Name
                    OutlinedTextField(
                        value = editNetworkNameText,
                        onValueChange = { editNetworkNameText = it; feedbackMsg = "" },
                        label = { Text("اسم الشبكة (بدون التفعيل)") }, modifier = Modifier.fillMaxWidth().testTag("settings_network_name_fld"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                                )

                    // Message Template
                    OutlinedTextField(
                        value = editSmsTemplateText,
                        onValueChange = { editSmsTemplateText = it; feedbackMsg = "" },
                        label = { Text("صيغة الرسالة المرسلة تلقائياً لشبكتك") }, modifier = Modifier.fillMaxWidth().height(115.dp).testTag("settings_sms_tpl_fld"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                                )

                    // Change Serial directly
                    OutlinedTextField(
                        value = editNewSerialText,
                        onValueChange = { editNewSerialText = it; feedbackMsg = "" },
                        label = { Text("تغيير السيريال (كلمة السر الجديدة)") },
                        placeholder = { Text("أدخل رمز تفعيل أو باسورد جديد مباشرة هنا") },
                        singleLine = true, modifier = Modifier.fillMaxWidth().testTag("settings_password_fld"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                                )

                    if (feedbackMsg.isNotEmpty()) {
                        Text(
                            text = feedbackMsg,
                            color = if (feedbackSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    }

                    // Save Modifications (Premium Purple-Pink Gradient)
                    Button(
                        onClick = {
                            if (editNetworkNameText.trim().isNotEmpty()) {
                                viewModel.updateNetworkName(editNetworkNameText.trim())
                            }
                            if (editSmsTemplateText.trim().isNotEmpty()) {
                                viewModel.updateGeneralSmsTemplate(editSmsTemplateText.trim())
                            }
                            if (editNewSerialText.trim().isNotEmpty()) {
                                viewModel.setAppPasswordDirectly(editNewSerialText.trim())
                                editNewSerialText = ""
                            }
                            feedbackSuccess = true
                            feedbackMsg = "تم حفظ التعديلات والبيانات الجديدة بنجاح! ✔"
                        },
                        
                        contentPadding = PaddingValues(), modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("settings_save_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(Color(0xFF9C27B0), Color(0xFFFF4081))))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("حفظ التخصيصات والبيانات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Appearance
        item {
            Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDarkTheme) MaterialTheme.colorScheme.background else Color.White,
                            checkedTrackColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                        )
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "مظهر التطبيق والموضوع 🌗",
                            color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "التبديل بين المظهر الداكن (OLED) والمظهر الفاتح",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Developer info card fixed at the bottom
        item {
            Card(
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().testTag("developer_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "معلومات المطور والدعم الفني 👨‍💻",
                        color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(color = Color(0xFFFFD700).copy(alpha = 0.15f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "أحمد المنتصر", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(text = "المطور:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "773086403", color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        Text(text = "للتواصل:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Logout panel/Action button
        item {
            Button(
                onClick = onLogout, modifier = Modifier.fillMaxWidth().height(50.dp).testTag("settings_logout_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.ExitToApp, contentDescription = "تسجيل خروج", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "إلغاء التفعيل والاشتراك الحالي", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialCustomersTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val allMappings by viewModel.allMappings.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

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
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                                )

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
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                                )

                    // Target / Basic Personal Phone Number
                    OutlinedTextField(
                        value = basicPhone,
                        onValueChange = { basicPhone = it },
                        label = { Text("رقم هاتفه الأساسي الشخصي للمطابقة") },
                        placeholder = { Text("مثال: 770118275") },
                        singleLine = true, modifier = Modifier.fillMaxWidth().testTag("special_cust_phone_id"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
                                )

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

fun String.escapeCsv(): String { return this.replace("\"", "\"\"").let { "\"$it\"" } }

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
            csvBuilder.append("${phone.escapeCsv()},")
            csvBuilder.append("${amount.escapeCsv()},")
            csvBuilder.append("${cardDetails.escapeCsv()},")
            csvBuilder.append("${wallet.escapeCsv()},")
            csvBuilder.append("${dateStr.escapeCsv()}\n")
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
            csvBuilder.append("${name.escapeCsv()},")
            csvBuilder.append("${uniqueId.escapeCsv()},")
            csvBuilder.append("${phone.escapeCsv()},")
            csvBuilder.append("${amount.escapeCsv()},")
            csvBuilder.append("${cardDetails.escapeCsv()},")
            csvBuilder.append("${wallet.escapeCsv()},")
            csvBuilder.append("${dateStr.escapeCsv()}\n")
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalsTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val allPendingApprovals by viewModel.allPendingApprovals.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

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
border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
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
                                    color = if (pending.isAccountCode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
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
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
modifier = Modifier.fillMaxWidth(),
textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)
)
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
                    }) {
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
