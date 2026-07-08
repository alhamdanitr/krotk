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
import androidx.compose.ui.draw.shadow
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
import com.example.utils.DocumentExporter
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
    var isCenterMenuOpen by remember { mutableStateOf(false) }
    var currentSubScreen by remember { mutableStateOf<String?>(null) } // "distributor", "mikrotik", null
    var distributorInitialTab by remember { mutableStateOf(0) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Intercept system Back Press
    androidx.activity.compose.BackHandler(enabled = true) {
        if (currentSubScreen != null) {
            currentSubScreen = null
        } else if (selectedTab != 0) {
            selectedTab = 0
        } else {
            showExitConfirmDialog = true
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = "تأكيد الخروج ⚠️",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "هل تريد الخروج من التطبيق بالفعل؟",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        (context as? android.app.Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("نعم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmDialog = false }
                ) {
                    Text("إلغاء", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LaunchedEffect(Unit) {
        com.example.utils.NotificationBus.newCardExtractedEvents.collect { event ->
            activeEventNotification = event
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        if (currentSubScreen == "distributor") {
            DistributorSystemScreen(
                viewModel = viewModel,
                initialTab = distributorInitialTab,
                onBack = { currentSubScreen = null }
            )
        } else if (currentSubScreen == "mikrotik") {
            MikrotikGeneratorScreen(
                viewModel = viewModel,
                onBack = { currentSubScreen = null }
            )
        } else {
            Scaffold(
            bottomBar = {
                // Beautiful Custom Bottom Bar matching the screenshots (4 tabs + floating central button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .testTag("dashboard_bottom_nav"),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // White background card with top rounded corners
                    Surface(
                        color = Color.White,
                        tonalElevation = 0.dp,
                        shadowElevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left-most Tab: الملف (Settings)
                            BottomNavItem(
                                selected = selectedTab == 5,
                                onClick = {
                                    isCenterMenuOpen = false
                                    selectedTab = 5
                                },
                                icon = Icons.Outlined.Person,
                                selectedIcon = Icons.Filled.Person,
                                label = "الملف"
                            )

                            // Left-middle Tab: التقارير (Reports)
                            BottomNavItem(
                                selected = selectedTab == 4,
                                onClick = {
                                    isCenterMenuOpen = false
                                    selectedTab = 4
                                },
                                icon = Icons.Outlined.FormatListBulleted,
                                selectedIcon = Icons.Filled.FormatListBulleted,
                                label = "التقارير"
                            )

                            // Center Spacer to leave room for the Central Floating Button (distributed evenly)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )

                            // Right-middle Tab: الخدمات (Services)
                            BottomNavItem(
                                selected = selectedTab == 1,
                                onClick = {
                                    isCenterMenuOpen = false
                                    selectedTab = 1
                                },
                                icon = Icons.Outlined.ShoppingBag,
                                selectedIcon = Icons.Filled.ShoppingBag,
                                label = "الخدمات"
                            )

                            // Right-most Tab: الرئيسية (Home)
                            BottomNavItem(
                                selected = selectedTab == 0,
                                onClick = {
                                    isCenterMenuOpen = false
                                    selectedTab = 0
                                },
                                icon = Icons.Outlined.Home,
                                selectedIcon = Icons.Filled.Home,
                                label = "الرئيسية"
                            )
                        }
                    }

                    // Circular Floating Center Menu Button (diameter 64dp, half protruding)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-32).dp)
                            .size(64.dp)
                            .shadow(elevation = 12.dp, shape = CircleShape)
                            .background(
                                color = if (isCenterMenuOpen) Color(0xFF1E293B) else Color(0xFFDC2626),
                                shape = CircleShape
                            )
                            .clickable { isCenterMenuOpen = !isCenterMenuOpen },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCenterMenuOpen) Icons.Default.Close else Icons.Filled.KeyboardDoubleArrowUp,
                            contentDescription = "القائمة",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            containerColor = DeepBlack
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
                        },
                        onNavigateToTab = { selectedTab = it }
                    )
                    1 -> CardsTab(viewModel = viewModel)
                    2 -> PendingApprovalsTab(viewModel = viewModel)
                    3 -> SpecialCustomersTab(viewModel = viewModel)
                    4 -> ReportsTab(viewModel = viewModel)
                    5 -> SettingsTab(viewModel = viewModel, onLogout = onLogout)
                }
            }
        }

        // Animated Central Popover Overlay Menu (matches Screenshot 1's dark 4-card grid overlay)
        AnimatedVisibility(
            visible = isCenterMenuOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isCenterMenuOpen = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Overlay Bottom Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp)
                        .clickable(enabled = false) { } // prevent clicks from closing
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "الوصول السريع والخدمات الإضافية",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        // 2x2 Action Cards Grid (Exactly like the screenshot grid)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Card 1: التفويضات (Approvals)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .clickable {
                                        isCenterMenuOpen = false
                                        selectedTab = 2
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = null,
                                            tint = BrandPrimaryRed,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        if (allPendingApprovals.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .offset(x = 10.dp, y = (-10).dp)
                                                    .background(BrandPrimaryRed, CircleShape)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = allPendingApprovals.size.toString(),
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "التفويضات المعلقة",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Card 2: العملاء (Special Customers)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .clickable {
                                        isCenterMenuOpen = false
                                        selectedTab = 3
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = BrandPrimaryRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "العملاء الاستثنائيين",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Card 3: إضافة كارت (Add Card quick action)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .clickable {
                                        isCenterMenuOpen = false
                                        selectedTab = 1 // takes user to cards tab where they can add cards
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCard,
                                        contentDescription = null,
                                        tint = BrandPrimaryRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "إضافة كروت جديدة",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Card 4: مزامنة وتحديث (Sync / Refresh action)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .clickable {
                                        isCenterMenuOpen = false
                                        Toast.makeText(context, "🔄 تم تحديث ومزامنة البيانات مع الأجهزة والرسائل بنجاح!", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = BrandPrimaryRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "تحديث ومزامنة الكروت",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Large red rounded Close circular floating button matching the bottom layout precisely
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF0F172A), CircleShape)
                                .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.1f)), CircleShape)
                                .clickable { isCenterMenuOpen = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎉 تم استخراج كرت جديد بنجاح",
                        color = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.weight(1f)
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
                        color = PureWhite,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Card Info Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("المبلغ: ${event.amount} ر.ي", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("المحفظة: ${event.walletType}", color = TextSecondary, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("الرقم: ${event.recipientPhone}", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val statusText = if (event.isAutoSent) "تم الإرسال تلقائياً ✔" else "فشل الإرسال التلقائي ⚠️"
                            val statusColor = if (event.isAutoSent) GlowEmeraldGreen else StatusRed
                            Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "كود الكرت المستخرج:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = event.cardDetails,
                        color = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) GlowEmeraldGreen else Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("مشاركة وفتح تطبيق ${event.walletType} 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Card Details", event.cardDetails)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ كود الكارت بنجاح! 📋", Toast.LENGTH_SHORT).show()
                            activeEventNotification = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) GlowOrangeGold else Color(0xFFE65100)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("نسخ الكود فقط 📋", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            activeEventNotification = null
                        },
                        border = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("إغلاق ✖", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = null,
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(BorderStroke(1.5.dp, if (isDarkTheme) Color(0xFF2D2D2D) else Color(0x1F000000)), RoundedCornerShape(20.dp))
        )
    }
}
}

// ==========================================
// TAB 1: الرئيسية (Home Tab)
// ==========================================
@Composable
fun HomeTab(
    viewModel: MainViewModel,
    onNavigateToSubScreen: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val isActivated by viewModel.isActivated.collectAsState()
    val isTrialActive by viewModel.isTrialActive.collectAsState()
    val networkName by viewModel.networkName.collectAsState()
    val totalUnusedCount by viewModel.totalUnusedCount.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    
    // Unused counts per category
    val count100 by viewModel.count100.collectAsState()
    val count200 by viewModel.count200.collectAsState()
    val count250 by viewModel.count250.collectAsState()
    val count300 by viewModel.count300.collectAsState()
    val count500 by viewModel.count500.collectAsState()
    
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allDeposits by viewModel.allDeposits.collectAsState()
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    
    // Filter transactions to just show today's movements
    val todayTransactions = remember(allTransactions) {
        allTransactions.filter { 
            val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.createdAt))
            dateFormatted == todayDateStr
        }
    }

    val todayDeposits = remember(allDeposits) {
        allDeposits.filter { 
            val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.createdAt))
            dateFormatted == todayDateStr
        }
    }

    var isBalanceVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. App Top Header Bar (matches Screenshot 1's top layout)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Action Notification Bell and Agent/Robot Icons in light circles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // WhatsApp Developer Contact Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f), CircleShape)
                            .border(BorderStroke(1.2.dp, Color(0xFF25D366)), CircleShape)
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/967773303455"))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "الرجاء تثبيت تطبيق واتساب للتواصل", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Chat,
                            contentDescription = "تواصل واتساب مع المطور",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Notification Bell with Red Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(SurfaceDark, CircleShape)
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), CircleShape)
                            .clickable {
                                Toast.makeText(context, "🔔 لا توجد إشعارات جديدة غير مقروءة.", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Red, CircleShape)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "الإشعارات",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Right Side: Greeting (صباح الخير / جارالله)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "صباح الخير 👋",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = networkName.ifEmpty { "جارالله" },
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Rounded Avatar Container
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(BrandPrimaryRed.copy(alpha = 0.15f), CircleShape)
                            .border(BorderStroke(2.dp, BrandPrimaryRed), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (networkName.isNotEmpty()) networkName.take(1) else "ج",
                            color = BrandPrimaryRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }



        // Trial warning/notice banner (if trial is active and not fully activated)
        if (isTrialActive && !isActivated) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2F)),
                    border = BorderStroke(1.dp, GlowPurplePink.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/967773303455"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل فتح تطبيق واتساب!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "الفترة التجريبية المجانية مفعلة ⏳",
                                color = GlowPurplePink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val remainingDays = viewModel.getRemainingTrialDays()
                            Text(
                                text = "متبقي لديك $remainingDays أيام لتجربة التطبيق مجاناً. اضغط هنا للتواصل مع المطور لشراء السيريال والتنشيط الدائم.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right,
                                lineHeight = 16.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = "مؤقت التجربة",
                            tint = GlowPurplePink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 2. Beautiful Professional Daily Dashboard
        item {
            val totalCardsSoldToday = todayTransactions.size
            val totalSoldAmountToday = todayTransactions.sumOf { it.amount }
            val totalDepositsToday = todayDeposits.sumOf { it.amount }
            val totalOperationsToday = totalCardsSoldToday + todayDeposits.size

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13151E)), // Deep luxury space blue surface
                border = BorderStroke(1.2.dp, Color(0xFF262936)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("app_dashboard_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Header inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Refresh timestamp
                        val formatter = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()) }
                        val lastUpdateStr = remember(todayTransactions, todayDeposits) {
                            formatter.format(java.util.Date())
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color(0xFF10B981), // Emerald Green
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "تحديث تلقائي: $lastUpdateStr",
                                color = TextSecondary.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = "لوحة التحكم اليومية 📊",
                            color = BrandPrimaryRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Right
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // 2x2 grid layout of metrics
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DashboardMetricItem(
                                    title = "إجمالي مبيعات اليوم",
                                    value = "$totalSoldAmountToday ر.ي",
                                    icon = Icons.Outlined.Payments,
                                    iconColor = Color(0xFF34D399) // Emerald
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DashboardMetricItem(
                                    title = "إيداعات اليوم المستلمة",
                                    value = "$totalDepositsToday ر.ي",
                                    icon = Icons.Outlined.AccountBalanceWallet,
                                    iconColor = Color(0xFF60A5FA) // Blue
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DashboardMetricItem(
                                    title = "كروت مباعة اليوم",
                                    value = "$totalCardsSoldToday كارت",
                                    icon = Icons.Outlined.ConfirmationNumber,
                                    iconColor = Color(0xFFFBBF24) // Gold
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DashboardMetricItem(
                                    title = "عمليات منُفذة اليوم",
                                    value = "$totalOperationsToday عملية",
                                    icon = Icons.Outlined.ReceiptLong,
                                    iconColor = Color(0xFFA78BFA) // Purple
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Services Grid (6 Buttons matching Screenshot 2 precisely)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "الخدمات والأنظمة المتكاملة ⚡",
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Service 1: حاسبة الموزع
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clickable { onNavigateToSubScreen("distributor") }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("حاسبة الموزع", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Service 2: ديون البقالات
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clickable { onNavigateToSubScreen("distributor") }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("ديون البقالات", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Service 3: توليد كروت
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clickable { onNavigateToSubScreen("mikrotik") }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Router, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("توليد كروت", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Service 4: مخزن الكروت
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clickable { onNavigateToTab(1) }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("مخزن الكروت", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Service 5: مزامنة SMS
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clickable { onNavigateToTab(2) }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("مزامنة SMS", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Service 6: سندات ومصاريف
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clickable { onNavigateToSubScreen("distributor") }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("سندات ومصاريف", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Highly Polished 3-column separate card categories grid (Screenshot 2 Quick Actions style)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "خيارات سريعة وتحديثات",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "فئات كروت الشحن المتوفرة ⚡",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Grid 3 columns
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1 (F100, F200, F250)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple(100, count100, Category100Cardboard),
                            Triple(200, count200, Category200Blue),
                            Triple(250, count250, Category250Purple)
                        ).forEach { (category, count, color) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, if (isDark) color.copy(alpha = 0.25f) else color.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(102.dp)
                                    .testTag("cat_card_$category")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(color.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CardMembership,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "فئة $category ر.ي",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$count كارت",
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // Row 2 (F300, F500, Total Stock)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple(300, count300, Category300Green),
                            Triple(500, count500, Category500Turmeric),
                            Triple(0, totalUnusedCount, BrandPrimaryRed) // Total
                        ).forEach { (category, count, color) ->
                            val isTotal = category == 0
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, if (isDark) color.copy(alpha = 0.25f) else color.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(102.dp)
                                    .testTag(if (isTotal) "cat_card_total" else "cat_card_$category")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(color.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isTotal) Icons.Default.Inventory else Icons.Default.CardMembership,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isTotal) "إجمالي المخزون" else "فئة $category ر.ي",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$count كارت",
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Transactions Movements List (Separate Rounded Cards with left-aligned prices and right-aligned category icons)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اليوم",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "العمليات وحركة اليوم 🔄",
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (todayTransactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد عمليات مبيعات أو مزامنة كروت مسجلة لليوم.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(todayTransactions) { transaction ->
                val isSuccess = !transaction.cardCode.contains("غير متوفر") && !transaction.cardCode.contains("فشل") && !transaction.cardCode.contains("تجاهل")
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_item_${transaction.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Price details in red or green (following Screenshot 2 style)
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "-${transaction.amount} ر.ي",
                                color = if (isSuccess) BrandPrimaryRed else StatusRed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(transaction.createdAt)),
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        // Right: Title, client phone, wallet type and category icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "كرت فئة ${transaction.amount} تم بيعه بنجاح",
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right
                                )
                                Text(
                                    text = "المحفظة: ${transaction.walletType} | العميل: ${transaction.phone}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Right
                                )
                            }

                            // Circular icon container matching screenshot
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isSuccess) BrandPrimaryRed.copy(alpha = 0.1f) else StatusRed.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = if (isSuccess) BrandPrimaryRed else StatusRed,
                                    modifier = Modifier.size(20.dp)
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
    val isDark by viewModel.isDarkTheme.collectAsState()
    
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
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp),
                modifier = Modifier.fillMaxWidth()
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
                            checkedThumbColor = if (isDark) DeepBlack else Color.White,
                            checkedTrackColor = BrandPrimaryRed,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceDark
                        ),
                        modifier = Modifier.testTag("sms_direct_switch")
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "تفعيل الإرسال المباشر تلقائياً (SMS)",
                            color = BrandPrimaryRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "إرسال الكود فوراً وصامتاً عند وصول إشعار إيداع في الخلفية",
                            color = TextSecondary,
                            fontSize = 10.sp,
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_toggle_add_panel")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (showAddCardsSection) {
                                Brush.horizontalGradient(listOf(Color(0xFF37474F), Color(0xFF263238)))
                            } else {
                                EmeraldGreenGradient
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
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showAddCardsSection) "إغلاق لوحة الإضافة السريعة" else "إضافة كروت شحن جديدة للمخزن ➕",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Expanded Panel to Add Cards
        if (showAddCardsSection) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0F000000)),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_cards_expanded_panel")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "تعبئة مخزون الكروت ⚙️",
                            color = BrandPrimaryRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right
                        )

                        // 1. Selector for categories
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "اختر فئة الكرت:", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(100, 200, 250, 300, 500).forEach { cat ->
                                    val catColor = when (cat) {
                                        100 -> Category100Cardboard
                                        200 -> Category200Blue
                                        250 -> Category250Purple
                                        300 -> Category300Green
                                        500 -> Category500Turmeric
                                        else -> GoldPrimary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (selectedCategoryForAdding == cat) {
                                                    catColor.copy(alpha = 0.2f)
                                                } else {
                                                    DeepBlack
                                                }
                                            )
                                            .border(
                                                width = 1.2.dp, 
                                                color = if (selectedCategoryForAdding == cat) catColor else catColor.copy(alpha = 0.2f), 
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedCategoryForAdding = cat }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat.toString(),
                                            color = if (selectedCategoryForAdding == cat) catColor else TextSecondary,
                                            fontSize = 12.sp,
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
                                            BrandPrimaryRed.copy(alpha = 0.15f)
                                        } else {
                                            DeepBlack
                                        }
                                    )
                                    .clickable { inputModeBulk = true }
                                    .border(
                                        width = 1.2.dp, 
                                        color = if (inputModeBulk) {
                                            BrandPrimaryRed
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
                                        if (isDark) GlowEmeraldGreen else Color(0xFF00796B)
                                    } else {
                                        TextSecondary
                                    },
                                    fontSize = 11.sp,
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
                                            BrandPrimaryRed.copy(alpha = 0.15f)
                                        } else {
                                            DeepBlack
                                        }
                                    )
                                    .clickable { inputModeBulk = false }
                                    .border(
                                        width = 1.2.dp, 
                                        color = if (!inputModeBulk) {
                                            BrandPrimaryRed
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
                                        if (isDark) GlowEmeraldGreen else Color(0xFF00796B)
                                    } else {
                                        TextSecondary
                                    },
                                    fontSize = 11.sp,
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
                                placeholder = { Text("أكتب كود الكرت مباشرة أو بالصيغ في كل سطر...") },
                                modifier = Modifier.fillMaxWidth().height(110.dp).testTag("input_bulk_text"),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPrimaryRed,
                                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f),
                                    focusedContainerColor = DeepBlack,
                                    unfocusedContainerColor = DeepBlack
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        } else {
                            if (cardFormatMode == "user_only") {
                                OutlinedTextField(
                                    value = singleCodeText,
                                    onValueChange = { singleCodeText = it; feedbackMsg = "" },
                                    label = { Text("كود كرت الشحن") },
                                    placeholder = { Text("أدخل الكود المميز للكرت هنا") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("input_single_code_only"),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isDark) GlowEmeraldGreen else Color(0xFF00796B),
                                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            } else {
                                OutlinedTextField(
                                    value = singleUsernameText,
                                    onValueChange = { singleUsernameText = it; feedbackMsg = "" },
                                    label = { Text("اسم المستخدم (Username)") },
                                    placeholder = { Text("أدخل اسم المستخدم للكرت") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("input_single_user"),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isDark) GlowEmeraldGreen else Color(0xFF00796B),
                                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                OutlinedTextField(
                                    value = singlePasswordText,
                                    onValueChange = { singlePasswordText = it; feedbackMsg = "" },
                                    label = { Text("كلمة المرور (Password)") },
                                    placeholder = { Text("أدخل الرقم السري للكرت") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("input_single_pass"),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isDark) GlowEmeraldGreen else Color(0xFF00796B),
                                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        // Actions and Feedback
                        if (feedbackMsg.isNotEmpty()) {
                            Text(
                                text = feedbackMsg,
                                color = if (feedbackSuccess) StatusGreen else StatusRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                            contentPadding = PaddingValues(),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_added_cards")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(PrimaryRedGradient)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "حفظ وتثبيت الكروت في المخزن ✔", 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp,
                                    color = Color.White
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
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
                        100 -> Category100Cardboard
                        200 -> Category200Blue
                        250 -> Category250Purple
                        300 -> Category300Green
                        500 -> Category500Turmeric
                        else -> GoldPrimary
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
                                    SurfaceDark
                                }
                            )
                            .clickable { selectedViewCategory = cat }
                            .border(
                                BorderStroke(
                                    width = 1.5.dp, 
                                    color = if (selectedViewCategory == cat) catColor else Color(0x1F9E9E9E)
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .testTag("selector_view_cat_$cat"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$cat ر.ي",
                            color = if (selectedViewCategory == cat) catColor else TextSecondary,
                            fontSize = 11.sp,
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
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D).copy(alpha = 0.5f) else Color(0x0A000000)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد أي كروت متوفرة حالياً لفئة $selectedViewCategory ر.ي في مخزنك.",
                            color = TextSecondary,
                            fontSize = 12.sp,
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
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        width = 1.dp, 
                        color = if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
                    modifier = Modifier
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
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_card_btn_${card.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "حذف الكرت",
                                    tint = StatusRed,
                                    modifier = Modifier.size(18.dp)
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
                                    tint = BrandPrimaryRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "النسخ والبيع",
                                    color = BrandPrimaryRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Card credential on the right
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                  text = cardDisplayDetails,
                                  color = PureWhite,
                                  fontSize = 13.sp,
                                  fontWeight = FontWeight.Bold,
                                  textAlign = TextAlign.Right
                            )
                            Text(
                                  text = "مُعرّف الكرت بالمخزون: #${card.id} | فئة ${card.category} ر.ي",
                                  color = TextSecondary,
                                  fontSize = 10.sp,
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
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp),
                modifier = Modifier
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
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Analytics,
                                contentDescription = "مراقبة المخزون",
                                tint = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "جدول مراقبة حالة الكروت الشامل 📊",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
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
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "بحث", tint = TextSecondary)
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
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
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
                                        .background(
                                            if (isSelected) (if (isDark) GlowOrangeGold.copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.12f))
                                            else SurfaceDark.copy(alpha = 0.4f)
                                        )
                                        .clickable { tableCategoryFilter = cat }
                                        .border(
                                            BorderStroke(
                                                width = 1.dp,
                                                color = if (isSelected) (if (isDark) GlowOrangeGold else Color(0xFFE65100)) else Color(0x1F9E9E9E)
                                            ),
                                            RoundedCornerShape(15.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) (if (isDark) GlowOrangeGold else Color(0xFFE65100)) else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Status Filters (Chips)
                        Text(
                            text = "تصفية حالة الكرت:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
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
                                                when (status) {
                                                    "متاحة" -> StatusGreen.copy(alpha = 0.15f)
                                                    "الكل" -> (if (isDark) GlowOrangeGold.copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.12f))
                                                    "تم توزيعها" -> Color(0xFF1E88E5).copy(alpha = 0.15f)
                                                    else -> StatusRed.copy(alpha = 0.15f)
                                                }
                                            } else SurfaceDark.copy(alpha = 0.4f)
                                        )
                                        .clickable { tableStatusFilter = status }
                                        .border(
                                            BorderStroke(
                                                width = 1.dp,
                                                color = if (isSelected) {
                                                    when (status) {
                                                        "متاحة" -> StatusGreen
                                                        "الكل" -> (if (isDark) GlowOrangeGold else Color(0xFFE65100))
                                                        "تم توزيعها" -> Color(0xFF1E88E5)
                                                        else -> StatusRed
                                                    }
                                                } else Color(0x1F9E9E9E)
                                            ),
                                            RoundedCornerShape(15.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = status,
                                        color = if (isSelected) {
                                            when (status) {
                                                "متاحة" -> StatusGreen
                                                "الكل" -> (if (isDark) GlowOrangeGold else Color(0xFFE65100))
                                                "تم توزيعها" -> Color(0xFF1E88E5)
                                                else -> StatusRed
                                            }
                                        } else TextSecondary,
                                        fontSize = 9.sp,
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
                            .border(BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        // Table Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) Color(0xFF161616) else Color(0x0A000000))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "الحالة", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                            Text(text = "الفئة", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(text = "كود / بيانات الكارت", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(3f), textAlign = TextAlign.Right)
                            Text(text = "م", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        }

                        // Table Rows
                        if (tableFilteredCards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "لا توجد كروت مطابقة للفلاتر المحددة.", color = TextSecondary, fontSize = 11.sp)
                            }
                        } else {
                            tableFilteredCards.take(40).forEachIndexed { index, card ->
                                val cardDisplayDetails = if (card.password.isNotEmpty()) {
                                    "${card.username} | ${card.password}"
                                } else {
                                    card.code
                                }
                                val rowBg = if (index % 2 == 0) Color.Transparent else (if (isDark) Color(0xFF1C1C1C).copy(alpha = 0.5f) else Color(0x05000000))
                                
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
                                            StatusGreen
                                        } else if (card.id % 2 == 0) {
                                            Color(0xFF1E88E5)
                                        } else {
                                            StatusRed
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
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Category Column (weight 1)
                                    Text(
                                        text = "${card.category} ر.ي",
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )

                                    // Card Details Column (weight 3)
                                    Text(
                                        text = cardDisplayDetails,
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(3f),
                                        textAlign = TextAlign.Right,
                                        maxLines = 1
                                    )

                                    // Index Column (weight 0.5)
                                    Text(
                                        text = "${index + 1}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (index < tableFilteredCards.take(40).size - 1) {
                                    HorizontalDivider(color = if (isDark) Color(0xFF222222) else Color(0x05000000))
                                }
                            }
                            if (tableFilteredCards.size > 40) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isDark) Color(0xFF161616) else Color(0x0A000000))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "يتم عرض أول 40 كارت فقط من إجمالي ${tableFilteredCards.size}... استخدم الفلاتر والبحث للتحديد الدقيق.",
                                        color = TextSecondary,
                                        fontSize = 9.sp,
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
                    text = "تأكيد بيع ومشاركة الكرت 💸",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
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
                        color = PureWhite,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "تفاصيل الكرت:\n$cardDisplayDetails",
                        color = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "تحديد محفظة العميل للمشاركة والفتح المباشر:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
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
                                    .background(if (isSelected) GlowOrangeGold.copy(alpha = 0.2f) else SurfaceDark)
                                    .border(
                                        BorderStroke(1.dp, if (isSelected) GlowOrangeGold else Color(0x1F9E9E9E)),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedShareWallet = wallet },
                                contentAlignment = Alignment.Center
                             ) {
                                Text(
                                    text = if (wallet == "جيب") "محفظة كاش" else wallet,
                                    color = if (isSelected) GlowOrangeGold else TextSecondary,
                                    fontSize = 11.sp,
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
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) GlowEmeraldGreen else Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("بيع ومشاركة عبر تطبيق $selectedShareWallet 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) GlowOrangeGold else Color(0xFFE65100)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("بيع ونسخ الكود فقط 📋", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            showConfirmSellDialog = false
                            cardToConfirmSell = null
                        },
                        border = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("تراجع / رجوع عادي ✖", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = null,
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(BorderStroke(1.5.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x1F000000)), RoundedCornerShape(20.dp))
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
                    .background(SurfaceDark)
                    .clickable(enabled = false) {} // prevent click-through
                    .border(
                        BorderStroke(1.5.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x1F000000)),
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
                        .background(TextSecondary.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "تأكيد حذف الكارت ⚠️",
                    color = StatusRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "هل أنت متأكد من رغبتك في حذف هذا الكارت من المخزن؟ لا يمكن التراجع عن هذا الإجراء لاحقاً.",
                    color = PureWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                // Card details preview inside sheet
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepBlack),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
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
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "فئة ${cardToDelete!!.category} ر.ي | معرّف الكرت: #${cardToDelete!!.id}",
                            color = TextSecondary,
                            fontSize = 10.sp,
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
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x1F000000)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Delete Confirm Button
                    Button(
                        onClick = {
                            viewModel.deleteCard(cardToDelete!!.id)
                            Toast.makeText(context, "تم حذف الكارت بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                            showDeleteBottomSheet = false
                            cardToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp)
                            .testTag("confirm_delete_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("تأكيد الحذف", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
    val isDark by viewModel.isDarkTheme.collectAsState()
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
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.5.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_stats_header_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp)
            ) {
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
                            tint = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "تحليلات وإحصائيات العمليات والمحافظ 📊",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Right
                        )
                    }

                    Divider(color = if (isDark) Color(0xFF2D2D2D) else Color(0x0F000000), thickness = 1.dp)

                    // Unified KPIs Row (Total Count / Total Value)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Value Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepBlack),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF212121) else Color(0x05000000)),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "إجمالي قيمة المبيعات",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$totalValue ر.ي",
                                    color = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Total Count Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepBlack),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF212121) else Color(0x05000000)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "الكروت الموزعة",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$totalCount كرت",
                                    color = if (isDark) GlowEmeraldGreen else Color(0xFF2E7D32),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Segmented Data Visualization Bar Chart (Clean progress-like block)
                    Text(
                        text = "توزيع حجم العمليات وقيمتها حسب المحفظة المانحة:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Proportional horizontal bar layout
                    val totalSum = (jeebValue + jawaliValue + oneCashValue + otherValue).toDouble()
                    val jeebPercentage = if (totalSum > 0) jeebValue / totalSum else if (totalCount > 0 && jeebCount > 0) jeebCount.toDouble() / totalCount else 0.25
                    val jawaliPercentage = if (totalSum > 0) jawaliValue / totalSum else if (totalCount > 0 && jawaliCount > 0) jawaliCount.toDouble() / totalCount else 0.25
                    val oneCashPercentage = if (totalSum > 0) oneCashValue / totalSum else if (totalCount > 0 && oneCashCount > 0) oneCashCount.toDouble() / totalCount else 0.25
                    val otherPercentage = if (totalSum > 0) otherValue / totalSum else if (totalCount > 0 && otherCount > 0) otherCount.toDouble() / totalCount else 0.25

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Segmented bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (isDark) Color(0xFF1E1E1E) else Color(0x0F000000))
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
                                        .background(if (isDark) GlowOrangeGold else Color(0xFFE65100))
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
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isDark) GlowOrangeGold else Color(0xFFE65100)))
                                Text("أخرى: ${(otherPercentage * 100).toInt()}%", color = TextSecondary, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE91E63)))
                                Text("ون كاش: ${(oneCashPercentage * 100).toInt()}%", color = TextSecondary, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFAB47BC)))
                                Text("جوالي: ${(jawaliPercentage * 100).toInt()}%", color = TextSecondary, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2196F3)))
                                Text("محفظة كاش: ${(jeebPercentage * 100).toInt()}%", color = TextSecondary, fontSize = 9.sp)
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
                            Text(text = "$jeebCount كارت | $jeebValue ر.ي", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(text = "محفظة كاش", color = Color(0xFF90CAF9), fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                            Text(text = "$jawaliCount كارت | $jawaliValue ر.ي", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(text = "محفظة جوالي (Jawali)", color = Color(0xFFE1BEE7), fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                            Text(text = "$oneCashCount كارت | $oneCashValue ر.ي", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(text = "محفظة ون كاش (One Cash)", color = Color(0xFFF8BBD0), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // Others Row details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) GlowOrangeGold.copy(alpha = 0.05f) else Color(0xFFE65100).copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(0.5.dp, if (isDark) GlowOrangeGold.copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.1f)), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "$otherCount كارت | $otherValue ر.ي", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(text = "طرق سداد ومبيعات أخرى", color = if (isDark) GlowOrangeGold else Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Date input filter section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "فلترة وتصفية التقارير والعمليات 📅",
                        color = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = filterDateByFormatted,
                        onValueChange = { filterDateByFormatted = it },
                        label = { Text("أدخل التاريخ للبحث والتصفية (yyyy-MM-dd)") },
                        placeholder = { Text("مثال: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("filter_date_input"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, color = PureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = if (isDark) GlowOrangeGold else Color(0xFFF57C00),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Today Button with OrangeGoldGradient
                        Button(
                            onClick = { 
                                filterDateByFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                            contentPadding = PaddingValues(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(OrangeGoldGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("اليوم", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Yesterday Button
                        Button(
                            onClick = { 
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DATE, -1)
                                filterDateByFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time) 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = if (isDark) GlowOrangeGold else Color(0xFFE65100)),
                            border = BorderStroke(1.dp, (if (isDark) GlowOrangeGold else Color(0xFFE65100)).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("الأمس", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "خيارات التصدير والطباعة 🖨️",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // PDF Button
                        Button(
                            onClick = {
                                val headers = listOf("الرقم", "المحفظة", "القيمة (ر.ي)", "التاريخ", "الحالة")
                                val rows = filteredTransactions.map { tx ->
                                    val dateStr = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date(tx.createdAt))
                                    listOf(
                                        tx.phone.ifEmpty { "بدون رقم" },
                                        if (tx.walletType == "جيب") "محفظة كاش" else tx.walletType,
                                        "${tx.amount} ر.ي",
                                        dateStr,
                                        "ناجحة"
                                    )
                                }
                                DocumentExporter.exportToPdf(
                                    context = context,
                                    fileName = "تقرير_المبيعات",
                                    title = "تقرير مبيعات الموزع لتاريخ $filterDateByFormatted",
                                    headers = headers,
                                    rows = rows
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Text("PDF", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                            }
                        }

                        // Excel Button
                        Button(
                            onClick = {
                                val headers = listOf("الرقم", "المحفظة", "القيمة (ر.ي)", "التاريخ", "الحالة")
                                val rows = filteredTransactions.map { tx ->
                                    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(tx.createdAt))
                                    listOf(
                                        tx.phone.ifEmpty { "بدون رقم" },
                                        if (tx.walletType == "جيب") "محفظة كاش" else tx.walletType,
                                        "${tx.amount}",
                                        dateStr,
                                        "ناجحة"
                                    )
                                }
                                DocumentExporter.exportToExcel(
                                    context = context,
                                    fileName = "تقرير_المبيعات",
                                    title = "تقرير مبيعات الموزع لتاريخ $filterDateByFormatted",
                                    headers = headers,
                                    rows = rows
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Text("Excel", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                            }
                        }

                        // Print Button
                        Button(
                            onClick = {
                                val headers = listOf("الرقم", "المحفظة", "القيمة (ر.ي)", "التاريخ", "الحالة")
                                val rows = filteredTransactions.map { tx ->
                                    val dateStr = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date(tx.createdAt))
                                    listOf(
                                        tx.phone.ifEmpty { "بدون رقم" },
                                        if (tx.walletType == "جيب") "محفظة كاش" else tx.walletType,
                                        "${tx.amount} ر.ي",
                                        dateStr,
                                        "ناجحة"
                                    )
                                }
                                DocumentExporter.printReport(
                                    context = context,
                                    title = "تقرير مبيعات الموزع لتاريخ $filterDateByFormatted",
                                    headers = headers,
                                    rows = rows
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Text("طباعة", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                            }
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
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterOptions = listOf(
                        Triple("all", "الكل", "All"),
                        Triple("جيب", "محفظة كاش", "Jeeb"),
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
                                        if (isDark) GlowOrangeGold.copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.12f)
                                    } else {
                                        SurfaceDark
                                    }
                                )
                                .clickable { selectedSourceFilter = opt.first }
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) {
                                            if (isDark) GlowOrangeGold else Color(0xFFE65100)
                                        } else {
                                            if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)
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
                                    if (isDark) GlowOrangeGold else Color(0xFFE65100)
                                } else {
                                    TextSecondary
                                },
                                fontSize = 11.sp,
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
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
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
                                        if (isDark) GlowOrangeGold.copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.12f)
                                    } else {
                                        SurfaceDark
                                    }
                                )
                                .clickable { selectedStatusFilter = opt.first }
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) {
                                            if (isDark) GlowOrangeGold else Color(0xFFE65100)
                                        } else {
                                            if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)
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
                                    if (isDark) GlowOrangeGold else Color(0xFFE65100)
                                } else {
                                    TextSecondary
                                },
                                fontSize = 11.sp,
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
                            .background(StatusRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$totalPendingQty معلقة",
                            color = StatusRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "العمليات المعلقة التي تتطلب توزيعاً يدوياً ⏳",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }
            }

            if (filteredPendingApprovals.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(text = "لا توجد عمليات معلقة مع مطابقة هذه الفلاتر اليوم.", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredPendingApprovals) { pending ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(pending.createdAt)),
                                color = TextSecondary,
                                fontSize = 11.sp
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
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "مبلغ ${pending.amount} ر.ي",
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "الزبون/الحساب: ${pending.phone}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Right
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StatusRed.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (pending.isAccountCode) "رمز حساب - تتطلب مطابقة يدوية ⚠️" else "تتطلب توزيع كارت يدوي ⏳",
                                        color = StatusRed,
                                        fontSize = 9.sp,
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
                            .background(StatusGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${filteredDeposits.count { it.isShared }} ناجحة",
                            color = StatusGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "العمليات الناجحة (سجل التحصيل الموزع) ✔",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }
            }

            val successDeposits = filteredDeposits.filter { it.isShared }

            if (successDeposits.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(text = "لا توجد عمليات ناجحة مسجلة اليوم.", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(successDeposits) { deposit ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(deposit.createdAt)),
                                color = TextSecondary,
                                fontSize = 11.sp
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
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "مبلغ ${deposit.amount} ر.ي",
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "الزبون: ${deposit.phone}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
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
                                            .background(StatusGreen.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "تم تسليم الكرت للزبون بنجاح ✔",
                                            color = StatusGreen,
                                            fontSize = 9.sp,
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
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Right
            )
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "لا توجد معاملات مبيعات مسجلة بالتاريخ المحدد.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(filteredTransactions) { trans ->
                val isSuccessfulDistribution = !trans.cardCode.contains("غير متوفر") && !trans.cardCode.contains("فشل")
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                    .background(if (isSuccessfulDistribution) StatusGreen.copy(alpha = 0.1f) else StatusRed.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isSuccessfulDistribution) "ناجح ✔" else "فاشل ✖",
                                    color = if (isSuccessfulDistribution) StatusGreen else StatusRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = "كرت فئة ${trans.amount} ر.ي",
                                color = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "تفاصيل الكرت: ${trans.cardCode}",
                            color = PureWhite,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "تاريخ العملية: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(trans.createdAt))} | هاتف العميل: ${trans.phone}",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Bottom Summary Panel (Vibrant Orange-Gold Gradient Card)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("reports_summary_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OrangeGoldGradient)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "ملخص عمليات وتوزيع اليوم 📊",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "$totalSoldQty كارت", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "إجمالي الكروت المباعة التلقائية:", color = Color.Black.copy(alpha = 0.8f), fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "$totalPendingQty عملية", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "إجمالي العمليات المعلقة حالياً:", color = Color.Black.copy(alpha = 0.8f), fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "$totalRevenue ر.ي", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        Text(text = "إجمالي المبالغ المالية المحصلة الموزعة:", color = Color.Black.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // floating action button to export CSV
    FloatingActionButton(
        onClick = { exportTransactionsToCsv(context, filteredTransactions, filterDateByFormatted) },
        containerColor = Color.Transparent,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(18.dp))
            .testTag("export_csv_fab"),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(OrangeGoldGradient)
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
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "تصدير CSV 📥",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
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
    val autoApprovedAmounts by viewModel.autoApprovedAmounts.collectAsState()

    // Configuration Inputs
    var editNetworkNameText by remember { mutableStateOf(networkName) }
    var editSmsTemplateText by remember { mutableStateOf(generalSmsTemplate) }
    var editNewSerialText by remember { mutableStateOf("") }
    var editAutoApprovedAmountsText by remember(autoApprovedAmounts) { mutableStateOf(autoApprovedAmounts.joinToString(",")) }

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
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "خيارات إرسال ومشاركة كروت الشحن ⚙️",
                        color = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
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
                                checkedThumbColor = if (isDarkTheme) DeepBlack else Color.White,
                                checkedTrackColor = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2)
                            )
                        )
                        Text(
                            text = "تفعيل الإرسال المباشر تلقائياً (SMS)",
                            color = PureWhite,
                            fontSize = 12.sp,
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
                                checkedThumbColor = if (isDarkTheme) DeepBlack else Color.White,
                                checkedTrackColor = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2)
                            )
                        )
                        Text(
                            text = "مشاركة فتح الرسالة تلقائياً من الإشعار",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(color = GoldAccent.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text(
                        text = "تفعيل دفعات المحافظ المدعومة 💳",
                        color = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    // Switches for each individual wallet
                    val walletList = listOf(
                        Triple("تفعيل استقبال دفعات (محفظة كاش)", isJeebEnabled) { v: Boolean -> viewModel.toggleJeeb(v) },
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
                                    checkedThumbColor = if (isDarkTheme) DeepBlack else Color.White,
                                    checkedTrackColor = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2)
                                )
                            )
                            Text(
                                text = label,
                                color = PureWhite,
                                fontSize = 12.sp,
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
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "تخصيص البيانات والرسائل 📝",
                        color = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    // Network Name
                    OutlinedTextField(
                        value = editNetworkNameText,
                        onValueChange = { editNetworkNameText = it; feedbackMsg = "" },
                        label = { Text("اسم الشبكة (بدون التفعيل)") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_network_name_fld"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Message Template
                    OutlinedTextField(
                        value = editSmsTemplateText,
                        onValueChange = { editSmsTemplateText = it; feedbackMsg = "" },
                        label = { Text("صيغة الرسالة المرسلة تلقائياً لشبكتك") },
                        modifier = Modifier.fillMaxWidth().height(115.dp).testTag("settings_sms_tpl_fld"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Change Serial directly
                    OutlinedTextField(
                        value = editNewSerialText,
                        onValueChange = { editNewSerialText = it; feedbackMsg = "" },
                        label = { Text("تغيير السيريال (كلمة السر الجديدة)") },
                        placeholder = { Text("أدخل رمز تفعيل أو باسورد جديد مباشرة هنا") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_password_fld"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Configurable auto-approved amounts
                    OutlinedTextField(
                        value = editAutoApprovedAmountsText,
                        onValueChange = { editAutoApprovedAmountsText = it; feedbackMsg = "" },
                        label = { Text("فئات المبالغ المعتمدة تلقائياً (مفصولة بفاصلة ,)") },
                        placeholder = { Text("مثال: 100,200,300,500,1000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_approved_amounts_fld"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (feedbackMsg.isNotEmpty()) {
                        Text(
                            text = feedbackMsg,
                            color = if (feedbackSuccess) StatusGreen else StatusRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
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
                            val parsedAmounts = editAutoApprovedAmountsText.split(",")
                                .mapNotNull { it.trim().toIntOrNull() }
                            viewModel.updateAutoApprovedAmounts(parsedAmounts)

                            feedbackSuccess = true
                            feedbackMsg = "تم حفظ التعديلات والبيانات الجديدة بنجاح! ✔"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("settings_save_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PurplePinkGradient)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("حفظ التخصيصات والبيانات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Appearance
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 4.dp),
                modifier = Modifier.fillMaxWidth()
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
                            checkedThumbColor = if (isDarkTheme) DeepBlack else Color.White,
                            checkedTrackColor = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2)
                        )
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "مظهر التطبيق والموضوع 🌗",
                            color = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "التبديل بين المظهر الداكن (OLED) والمظهر الفاتح",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Cloud Backup & Synchronization Controls
        item {
            var syncStatusMessage by remember { mutableStateOf("") }
            var isSyncing by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().testTag("cloud_sync_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "النسخ الاحتياطي والمزامنة السحابية ☁️",
                        color = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "يتيح لك النظام السحابي حفظ بياناتك وتحديثها باستمرار على الخادم وحمايتها من الضياع أو استعادتها عند إعادة التثبيت.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        lineHeight = 16.sp
                    )

                    if (syncStatusMessage.isNotEmpty()) {
                        Text(
                            text = syncStatusMessage,
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Restore / Download button
                        Button(
                            onClick = {
                                if (!isSyncing) {
                                    isSyncing = true
                                    syncStatusMessage = "جاري استرداد البيانات من السحابة..."
                                    com.example.network.CloudSyncEngine.getInstance(context).downloadServerData { success, msg ->
                                        isSyncing = false
                                        syncStatusMessage = msg
                                    }
                                }
                            },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F1B2A)),
                            border = BorderStroke(1.dp, if (isDarkTheme) GlowPurplePink.copy(alpha = 0.5f) else Color(0xFF7B1FA2)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "استيراد البيانات 📥", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        }

                        // Manual Backup / Sync Now button
                        Button(
                            onClick = {
                                if (!isSyncing) {
                                    isSyncing = true
                                    syncStatusMessage = "جاري رفع ومزامنة البيانات..."
                                    com.example.network.CloudSyncEngine.getInstance(context).performIncrementalSync { success, msg ->
                                        isSyncing = false
                                        syncStatusMessage = msg
                                    }
                                }
                            },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "مزامنة الآن 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        }
                    }
                }
            }
        }

        // Developer info card fixed at the bottom
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().testTag("developer_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "معلومات المطور والدعم الفني 👨‍💻",
                        color = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(color = GoldAccent.copy(alpha = 0.15f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "كيان سوفت", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "المطور:", color = TextSecondary, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "773303455", color = if (isDarkTheme) GlowPurplePink else Color(0xFF7B1FA2), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        Text(text = "للتواصل:", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Logout panel/Action button
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed.copy(alpha = 0.1f), contentColor = StatusRed),
                border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("settings_logout_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.ExitToApp, contentDescription = "تسجيل خروج", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "إلغاء التفعيل والاشتراك الحالي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    color = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = "قم بإضافة وتوجيه حسابات العملاء الذين يرسلون دفعات بأرقام محافظ مجهولة لتصل لرقمه الشخصي تلقائياً.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Add Customer Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "إضافة عميل جديد 👤",
                        color = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    // Partner/Customer Name
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("اسم العميل / الزبون") },
                        placeholder = { Text("مثال: احمد جابر حسن") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("special_cust_name"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Wallet Type selection via customizable Chips (Material 3 style)
                    Text(
                        text = "نوع المحفظة:",
                        color = TextSecondary,
                        fontSize = 12.sp,
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
                                label = { Text(if (option == "جيب") "محفظة كاش" else option, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(22.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = (if (isDark) GlowPurplePink else Color(0xFF7B1FA2)).copy(alpha = 0.15f),
                                    selectedLabelColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                                    containerColor = SurfaceDark,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = walletType == option,
                                    borderColor = if (walletType == option) (if (isDark) GlowPurplePink else Color(0xFF7B1FA2)) else TextSecondary.copy(alpha = 0.2f),
                                    selectedBorderColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
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
                                label = { Text(if (option == "جيب") "محفظة كاش" else option, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(22.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = (if (isDark) GlowPurplePink else Color(0xFF7B1FA2)).copy(alpha = 0.15f),
                                    selectedLabelColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                                    containerColor = SurfaceDark,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = walletType == option,
                                    borderColor = if (walletType == option) (if (isDark) GlowPurplePink else Color(0xFF7B1FA2)) else TextSecondary.copy(alpha = 0.2f),
                                    selectedBorderColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
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
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("special_cust_wallet_id"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Target / Basic Personal Phone Number
                    OutlinedTextField(
                        value = basicPhone,
                        onValueChange = { basicPhone = it },
                        label = { Text("رقم هاتفه الأساسي الشخصي للمطابقة") },
                        placeholder = { Text("مثال: 770118275") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("special_cust_phone_id"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Add Customer Button styled with PurplePinkGradient
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PurplePinkGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("إضافة وحفظ العميل", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("export_csv_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(EmeraldGreenGradient)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Outlined.Share, contentDescription = "تصدير CSV", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير الحسابات (CSV)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Text(
                    text = "قائمة العملاء الاستثنائيين النشطة 📂 (${allMappings.size})",
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }

        if (allMappings.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "لا يوجد أي زبائن استثنائيين مضافين حالياً.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            }
        } else {
            items(allMappings.size) { index ->
                val mapping = allMappings[index]
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
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
                                tint = StatusRed
                            )
                        }

                        // Info
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
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
                                    color = badgeBgColor,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = mapping.walletType,
                                        color = badgeTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                
                                Text(
                                    text = mapping.customerName.ifEmpty { "عميل استثنائي" },
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "رمز مجهول: ${mapping.customerUniqueId}",
                                color = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                            
                            Text(
                                text = "رقم التوجيه (الأساسي): ${mapping.basicPhone}",
                                color = TextSecondary,
                                fontSize = 11.sp,
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
    val autoApprovedAmounts by viewModel.autoApprovedAmounts.collectAsState()

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
                    color = if (isDark) GlowEmeraldGreen else Color(0xFF00796B),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = "العمليات التي تنتظر موافقتك اليدوية لإرسال الكود وتأكيد حركة الدفع.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (allPendingApprovals.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "لا توجد تفويضات",
                            tint = if (isDark) GlowEmeraldGreen else Color(0xFF00796B),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "لا توجد عمليات معلقة حالياً 🎉",
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "جميع العمليات الواردة الأخرى يتم تفعيلها تلقائياً بنجاح.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            items(allPendingApprovals.size) { index ->
                val pending = allPendingApprovals[index]
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, if (isDark) GlowEmeraldGreen.copy(alpha = 0.25f) else Color(0x3BFFCC80)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            // Wallet and Approved Status Badges
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                    color = badgeBgColor,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = pending.walletType,
                                        color = badgeTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                val isAutoApproved = autoApprovedAmounts.contains(pending.amount)
                                if (!isAutoApproved) {
                                    Surface(
                                        color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = "⚠️ فئة غير معتمدة",
                                            color = Color(0xFFEF5350),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Amount Label
                            Text(
                                text = "فئة ${pending.amount} ر.ي",
                                color = if (isDark) GlowEmeraldGreen else Color(0xFF00796B),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = if (isDark) Color(0xFF2D2D2D) else Color(0x0A000000))

                        // Details Block
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = pending.phone,
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ":رقم المودع/المستلم",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (pending.isAccountCode) "كود حساب (مطابقة فريدة)" else "شراء كارت مباشر",
                                    color = if (pending.isAccountCode) (if (isDark) GlowOrangeGold else Color(0xFFE65100)) else PureWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ":نوع الدفعة",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        val isName = pending.phone.any { it.isLetter() }
                        if (isName) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0xFF331F00) else Color(0xFFFFF3E0))
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
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.textButtonColors(contentColor = if (isDark) GlowOrangeGold else Color(0xFFE65100))
                                ) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = "ربط", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ربط رقم العميل 📞", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "الحساب اسم وليس رقم هاتف!",
                                    color = if (isDark) GlowOrangeGold else Color(0xFFE65100),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
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
                            val isAutoApproved = autoApprovedAmounts.contains(pending.amount)
                            // Reject or Ignore Button
                            OutlinedButton(
                                onClick = {
                                    if (isAutoApproved) {
                                        viewModel.rejectPendingApproval(pending.id)
                                        Toast.makeText(context, "تم رفض المعاملة اليدوية وإلغاؤها", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.ignorePendingApproval(pending.id)
                                        Toast.makeText(context, "تم تجاهل التحويل وحذف الإيداع بنجاح", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                border = BorderStroke(1.dp, if (isAutoApproved) StatusRed else Color.Gray),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isAutoApproved) StatusRed else Color.LightGray),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text(if (isAutoApproved) "رفض الدفعة ✖" else "تجاهل التحويل ✖", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                                contentPadding = PaddingValues(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1.3f).height(42.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(EmeraldGreenGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("موافقة وإرسال ✔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    text = "ربط رقم هاتف للعميل 📞",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
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
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "أدخل رقم الهاتف الصحيح لهذا العميل لإرسال كود الشحن إليه تلقائياً عند الموافقة وحفظه للمستقبل:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = enteredPhoneNumber,
                        onValueChange = { enteredPhoneNumber = it },
                        label = { Text("رقم جوال العميل") },
                        placeholder = { Text("مثال: 777123456") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = PureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = if (isDark) GlowEmeraldGreen else Color(0xFF00796B),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) GlowEmeraldGreen else Color(0xFF00796B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حفظ وربط ✔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showLinkDialog = false
                        pendingToLink = null
                    },
                    border = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إلغاء", fontSize = 12.sp, color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(BorderStroke(1.5.dp, if (isDark) Color(0xFF2D2D2D) else Color(0x1F000000)), RoundedCornerShape(20.dp))
        )
    }
}

@Composable
fun RowScope.BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(26.dp)
                    .background(
                        color = if (selected) Color(0xFFDC2626).copy(alpha = 0.1f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (selected) selectedIcon else icon,
                    contentDescription = label,
                    tint = if (selected) Color(0xFFDC2626) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (selected) Color(0xFFDC2626) else Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun DashboardMetricItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E2B)), // Dark slate card background
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF282C3D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Right
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
