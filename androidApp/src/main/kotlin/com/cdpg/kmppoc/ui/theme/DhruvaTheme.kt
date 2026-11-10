package com.cdpg.kmppoc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** A few Dhruva brand tokens (from Dhruva Android's DanAiaPalette / iOS DESIGN_SYSTEM.md). */
object DhruvaColors {
    val Primary = Color(0xFFAA4A44)
    val PrimaryContainer = Color(0xFFF0E1DC)
    val Background = Color(0xFFF8F2ED)
    val BackgroundTop = Color(0xFFF1E4DA)
    val OnSurface = Color(0xFF1D1B20)
    val Caption = Color(0xFF49454F)
    val InputBorder = Color(0xFFB8957A)
    val Error = Color(0xFFB3261E)
    val Warning = Color(0xFF8A5A00)
    val WarningContainer = Color(0xFFFFF0D1)
}

@Composable
fun DhruvaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = DhruvaColors.Primary,
            onPrimary = Color.White,
            primaryContainer = DhruvaColors.PrimaryContainer,
            background = DhruvaColors.Background,
            surface = Color.White,
            onSurface = DhruvaColors.OnSurface,
            onSurfaceVariant = DhruvaColors.Caption,
            outline = DhruvaColors.InputBorder,
            error = DhruvaColors.Error,
        ),
        content = content,
    )
}
