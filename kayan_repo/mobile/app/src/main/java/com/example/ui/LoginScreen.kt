package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    var networkNameInput by remember { mutableStateOf("شبكة الدحشة") }
    var serialInput by remember { mutableStateOf("") }
    var serialVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current
    val handleActivationAttempt = {
        val trimmedInput = serialInput.trim()
        if (trimmedInput.isEmpty()) {
            errorMessage = "⚠️ يرجى إدخال رمز السيريال أولاً!"
        } else if (com.example.security.DeviceSecurity.isDeviceRooted() || com.example.security.DeviceSecurity.isRunningOnEmulator()) {
            errorMessage = "عذراً! لا يمكن تشغيل التطبيق على أجهزة مروتة أو محاكيات لأسباب أمنية."
        } else if (trimmedInput == "PY_7MD") {
            errorMessage = ""
            viewModel.updateNetworkName(networkNameInput.trim().ifEmpty { "شبكة الدحشة" })
            viewModel.setActivated(true)
            viewModel.verifyPassword("PY_7MD") // set isLoggedIn to true in ViewModel session
            keyboardController?.hide()
            onLoginSuccess()
        } else {
            errorMessage = ""
            isVerifying = true
            com.example.security.SecurityApiService.validateSerial(
                context,
                trimmedInput,
                com.example.security.DeviceSecurity.getSecureDeviceId(context)
            ) { success, msg ->
                isVerifying = false
                if (success) {
                    errorMessage = ""
                    viewModel.updateNetworkName(networkNameInput.trim().ifEmpty { "شبكة الدحشة" })
                    viewModel.setActivated(true)
                    viewModel.verifyPassword("PY_7MD")
                    keyboardController?.hide()
                    onLoginSuccess()
                } else {
                    errorMessage = msg ?: "رمز التفعيل (السيريال) غير صحيح أو انتهى!"
                    serialInput = ""
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("activation_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Identity & Icon Combining Wifi and Card theme (Minimalist Outline)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .size(110.dp)
                        .background(
                            Brush.radialGradient(
                                if (isDark) {
                                    listOf(Color(0xFFE91E63).copy(alpha = 0.15f), Color.Transparent)
                                } else {
                                    listOf(Color(0xFF7B1FA2).copy(alpha = 0.08f), Color.Transparent)
                                }
                            ),
                            CircleShape
                        )
                        .border(
                            BorderStroke(
                                1.5.dp, 
                                MaterialTheme.colorScheme.outline
                            ),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Wifi,
                        contentDescription = "كروتك",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "كروتك (Kurotek)",
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "نظام إدارة وتوزيع كروت شبكات الـ Wi-Fi والاتصالات بنقرة واحدة",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp, modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Central Activation Form (Soft elevation in light mode, subtle glow border in dark mode)
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تفاصيل التفعيل والاشتراك",
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()
                    )

                    // Network Name Input with rounded corners
                    OutlinedTextField(
                        value = networkNameInput,
                        onValueChange = {
                            networkNameInput = it
                            errorMessage = ""
                        },
                        label = { Text("اسم الشبكة") },
                        placeholder = { Text("مثال: شبكة الدحشة") },
                        singleLine = true,
                        enabled = !isVerifying,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.WifiTethering, 
                                contentDescription = null, 
                                tint = Color(0xFFFF4081).copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_network_name")
                    )

                    // Serial Number Input with rounded corners
                    OutlinedTextField(
                        value = serialInput,
                        onValueChange = {
                            serialInput = it
                            errorMessage = ""
                        },
                        label = { Text("السيريال نمبر") },
                        placeholder = { Text("أدخل رمز السيريل هنا...") },
                        singleLine = true,
                        enabled = !isVerifying,
                        visualTransformation = if (serialVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { handleActivationAttempt() }
                        ),
                        trailingIcon = {
                            val icon = if (serialVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                            IconButton(onClick = { serialVisible = !serialVisible }) {
                                Icon(
                                    imageVector = icon, 
                                    contentDescription = null, 
                                    tint = Color(0xFFFF4081).copy(alpha = 0.8f)
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock, 
                                contentDescription = null, 
                                tint = Color(0xFFFF4081).copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_serial")
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error, modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Premium button with Vibrant Gradient background and no harsh borders
                    Button(
                        onClick = { handleActivationAttempt() },
                        enabled = !isVerifying,
                        contentPadding = PaddingValues(), modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("activation_submit_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(Color(0xFF9C27B0), Color(0xFFFF4081))))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = "تفعيل وترخيص التطبيق 🔑",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Contact / Support Information Static Display
            Card(
modifier = Modifier.fillMaxWidth()
) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تواصل مع فريق الدعم لطلب رمز التفعيل 773086403",
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
