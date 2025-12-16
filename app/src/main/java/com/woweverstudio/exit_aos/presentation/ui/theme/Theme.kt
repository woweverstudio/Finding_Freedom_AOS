package com.woweverstudio.exit_aos.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Exit 앱 다크 컬러 스킴
 */
private val ExitDarkColorScheme = darkColorScheme(
    primary = ExitColors.Accent,
    secondary = ExitColors.AccentSecondary,
    tertiary = ExitColors.Positive,
    background = ExitColors.Background,
    surface = ExitColors.CardBackground,
    surfaceVariant = ExitColors.SecondaryCardBackground,
    onPrimary = ExitColors.PrimaryText,
    onSecondary = ExitColors.PrimaryText,
    onTertiary = ExitColors.PrimaryText,
    onBackground = ExitColors.PrimaryText,
    onSurface = ExitColors.PrimaryText,
    onSurfaceVariant = ExitColors.SecondaryText,
    error = ExitColors.Warning,
    onError = ExitColors.PrimaryText,
    outline = ExitColors.Divider,
    outlineVariant = ExitColors.TertiaryText
)

/**
 * Exit 앱 그라데이션
 */
object ExitGradients {
    /** 액센트 그라데이션 */
    val Accent = Brush.linearGradient(
        colors = listOf(ExitColors.Accent, ExitColors.AccentSecondary)
    )
    
    /** 경고 그라데이션 (빨강 → 주황 → 청록) */
    val Warning = Brush.linearGradient(
        colors = listOf(ExitColors.Warning, ExitColors.Caution, ExitColors.Accent)
    )
    
    /** 카드 배경 그라데이션 */
    val Card = Brush.verticalGradient(
        colors = listOf(ExitColors.SecondaryCardBackground, ExitColors.CardBackground)
    )
    
    /** 다크 그라데이션 배경 */
    val Background = Brush.verticalGradient(
        colors = listOf(ExitColors.CardBackground.copy(alpha = 0.5f), ExitColors.Background)
    )
}

/**
 * Exit 앱 테마
 */
@Composable
fun ExitTheme(
    darkTheme: Boolean = true, // 항상 다크 테마 사용
    content: @Composable () -> Unit
) {
    val colorScheme = ExitDarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ExitColors.Background.toArgb()
            window.navigationBarColor = ExitColors.Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExitMaterial3Typography,
        content = content
    )
}


