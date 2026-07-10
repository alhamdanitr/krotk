package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.database.CardRepository
import com.example.network.SyncService
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    LOGIN,
    MAIN,
    ADD_CARDS,
    LOGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val repository = remember { CardRepository(context) }
            val factory = remember { MainViewModelFactory(repository) }
            val viewModel: MainViewModel = viewModel(factory = factory)
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val isActivated by viewModel.isActivated.collectAsState()
                var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }

                // Check and request run-time SMS & notification permissions
                val requiredPermissions = remember {
                    val permissions = mutableListOf(
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS
                    )
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissions.toTypedArray()
                }

                var hasSmsPermissions by remember {
                    mutableStateOf(
                        requiredPermissions.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissionsResult ->
                    hasSmsPermissions = permissionsResult.values.all { it }
                }

                // Auto route from/to activation screen and request permissions
                LaunchedEffect(isActivated, hasSmsPermissions) {
                    if (isActivated) {
                        currentScreen = AppScreen.MAIN
                        if (!hasSmsPermissions) {
                            permissionLauncher.launch(requiredPermissions)
                        }
                    } else {
                        currentScreen = AppScreen.LOGIN
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("app_scaffold")
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.LOGIN -> {
                                LoginScreen(
                                    viewModel = viewModel,
                                    onLoginSuccess = { currentScreen = AppScreen.MAIN }
                                )
                            }
                            AppScreen.MAIN -> {
                                LaunchedEffect(Unit) {
                                    SyncService.startService(context)
                                }
                                MainDashboardScreen(
                                    viewModel = viewModel,
                                    onLogout = {
                                        viewModel.setActivated(false)
                                        SyncService.stopService(context)
                                        currentScreen = AppScreen.LOGIN
                                    }
                                )
                            }
                            else -> {
                                // Fallback
                                MainDashboardScreen(
                                    viewModel = viewModel,
                                    onLogout = {
                                        viewModel.setActivated(false)
                                        currentScreen = AppScreen.LOGIN
                                    }
                                )
                            }
                        }

                        // Display custom permission notice if logged in but permissions are missing
                        AnimatedVisibility(
                            visible = isActivated && !hasSmsPermissions,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth().testTag("permission_dialog_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sms,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp)
                                        )

                                        Text(
                                            text = "مطلوب صلاحيات الرسائل SMS",
                                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()
                                        )

                                        Text(
                                            text = "لكي يتمكن تطبيق كروت الدحشة من قراءة وتصفية رسائل الإشعارات الواردة وتحديداً من محفظة 'جيب' ومحفظة 'جوالي' وتوزيع كروت الشحن تلقائياً لعملائك، يتطلب منح الهاتف صلاحية قراءة واستقبال وإرسال رسائل SMS.",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 20.sp, modifier = Modifier.fillMaxWidth()
                                        )

                                        Button(
                                            onClick = {
                                                permissionLauncher.launch(requiredPermissions)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.background
                                            ),
                                            shape = RoundedCornerShape(8.dp), modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("btn_request_permissions")
                                        ) {
                                            Text(
                                                text = "منح الصلاحيات المطلوبة والبدء",
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
        }
    }
}
