package com.example.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val isDarkThemeState = mutableStateOf(true)

// Vibrant Gradients
val PurplePinkGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF7B1FA2), Color(0xFFE91E63))
)
val OrangeGoldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF5722), Color(0xFFFFB300))
)
val EmeraldGreenGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00796B), Color(0xFF00E676))
)

val GlowPurplePink = Color(0xFFE91E63)
val GlowOrangeGold = Color(0xFFFF9800)
val GlowEmeraldGreen = Color(0xFF00E676)

// Premium Design Palette
val GoldPrimary = Color(0xFFE5C060)      // Premium Soft Satin Gold
val GoldAccent = Color(0xFFD4B350)       // Muted Elegant Gold
val GoldDark = Color(0xFF9F8335)         // Deep Luxury Gold Accent

// Dynamic Backgrounds and Cards matching user specifications
val DeepBlack: Color get() = if (isDarkThemeState.value) Color(0xFF121212) else Color(0xFFF5F7FA) // Application background
val SurfaceDark: Color get() = if (isDarkThemeState.value) Color(0xFF1E1E1E) else Color(0xFFFFFFFF) // Card & Main container background
val SurfaceLight: Color get() = if (isDarkThemeState.value) Color(0xFF2A2A2A) else Color(0xFFECEFF1) // Lighter overlay surface

val PureWhite: Color get() = if (isDarkThemeState.value) Color(0xFFF3F3F5) else Color(0xFF121212)
val TextSecondary: Color get() = if (isDarkThemeState.value) Color(0xFF9C9EA5) else Color(0xFF5B5D64)
val StatusGreen = Color(0xFF00E676)      // Emerald luxury green for success
val StatusRed = Color(0xFFE05252)        // Soft Crimson red for warnings/errors
val CardGoldBackground: Color get() = if (isDarkThemeState.value) Color(0xFF1E1C1A) else Color(0xFFFFFEEB)

// Special category colors requested by user (vibrant colors)
val Category100Cardboard = Color(0xFFC5A059) // كرتوني (Paper/Cardboard Gold Color)
val Category200Blue = Color(0xFF1976D2)      // أزرق (Blue)
val Category250Purple = Color(0xFF9C27B0)    // بنفسجي (Purple)
val Category300Green = Color(0xFF2E7D32)     // أخضر (Green)
val Category500Turmeric = Color(0xFFFFB300)  // كركمي (Turmeric/Curcuma Yellow)
