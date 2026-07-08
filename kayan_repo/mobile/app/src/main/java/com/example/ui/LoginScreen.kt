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
    var errorMessage by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val handleActivationAttempt = {
        if (serialInput.trim() == "PY_7MD") {
            errorMessage = ""
            viewModel.updateNetworkName(networkNameInput.trim().ifEmpty { "شبكة الدحشة" })
            viewModel.setActivated(true)
            viewModel.verifyPassword("PY_7MD") // set isLoggedIn to true in ViewModel session
            keyboardController?.hide()
            onLoginSuccess()
        } else {
            errorMessage = "رمز التفعيل (السيريال) غير صحيح! يرجى التواصل مع فريق الدعم."
            serialInput = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
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
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            Brush.radialGradient(
                                colors = if (isDark) {
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
                                if (isDark) Color(0xFFE91E63).copy(alpha = 0.3f) else Color(0xFF7B1FA2).copy(alpha = 0.2f)
                            ),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Wifi,
                        contentDescription = "كروتك",
                        tint = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "كروتك (Kurotek)",
                    color = if (isDark) PureWhite else Color(0xFF7B1FA2),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "نظام إدارة وتوزيع كروت شبكات الـ Wi-Fi والاتصالات بنقرة واحدة",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Central Activation Form (Soft elevation in light mode, subtle glow border in dark mode)
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isDark) Color(0xFF2C2C2C) else Color(0x0D000000)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تفاصيل التفعيل والاشتراك",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.WifiTethering, 
                                contentDescription = null, 
                                tint = if (isDark) GlowPurplePink.copy(alpha = 0.7f) else Color(0xFF7B1FA2).copy(alpha = 0.7f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f),
                            focusedLabelColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            cursorColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2)
                        ),
                        shape = RoundedCornerShape(16.dp),
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
                                    tint = if (isDark) GlowPurplePink.copy(alpha = 0.8f) else Color(0xFF7B1FA2).copy(alpha = 0.8f)
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock, 
                                contentDescription = null, 
                                tint = if (isDark) GlowPurplePink.copy(alpha = 0.7f) else Color(0xFF7B1FA2).copy(alpha = 0.7f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f),
                            focusedLabelColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            cursorColor = if (isDark) GlowPurplePink else Color(0xFF7B1FA2)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_serial")
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = StatusRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Premium button with Vibrant Gradient background and no harsh borders
                    Button(
                        onClick = { handleActivationAttempt() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("activation_submit_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PurplePinkGradient)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "تفعيل التطبيق",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Contact / Support Information Static Display
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isDark) Color(0xFF2C2C2C).copy(alpha = 0.5f) else Color(0x0A000000)
                ),
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
                        tint = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تواصل مع فريق الدعم لطلب رمز التفعيل 773086403",
                        color = if (isDark) GlowPurplePink else Color(0xFF7B1FA2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
