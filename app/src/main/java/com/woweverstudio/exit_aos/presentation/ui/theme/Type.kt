package com.woweverstudio.exit_aos.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Exit 앱 타이포그래피
 */
object ExitTypography {
    
    // MARK: - 제목
    
    /** 대형 제목 (42sp Heavy) */
    val LargeTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        lineHeight = 48.sp
    )
    
    /** 제목 (32sp Bold) */
    val Title = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    )
    
    /** 중형 제목 (24sp Semibold) */
    val Title2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    )
    
    /** 소형 제목 (20sp Semibold) */
    val Title3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    )
    
    // MARK: - 본문
    
    /** 본문 (18sp Medium) */
    val Body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )
    
    /** 서브 본문 (16sp Regular) */
    val Subheadline = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
    
    /** 캡션 (14sp Regular) */
    val Caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
    
    /** 소형 캡션 (12sp Regular) */
    val Caption2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    
    /** 소형 캡션 (13sp Semibold, Rounded) */
    val Caption3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 17.sp
    )
    
    // MARK: - 숫자
    
    /** 대형 점수 숫자 (96sp Heavy, Monospace) */
    val ScoreDisplay = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 96.sp,
        lineHeight = 104.sp
    )
    
    /** 중형 숫자 (42sp Heavy, Monospace) */
    val NumberDisplay = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        lineHeight = 48.sp
    )
    
    /** 숫자 (24sp Semibold, Monospace) */
    val Number = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    )
    
    /** 키패드 숫자 (28sp Medium) */
    val Keypad = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 34.sp
    )
}

/**
 * Material3 Typography 확장
 */
val ExitMaterial3Typography = Typography(
    displayLarge = ExitTypography.LargeTitle,
    displayMedium = ExitTypography.Title,
    displaySmall = ExitTypography.Title2,
    headlineLarge = ExitTypography.Title,
    headlineMedium = ExitTypography.Title2,
    headlineSmall = ExitTypography.Title3,
    titleLarge = ExitTypography.Title2,
    titleMedium = ExitTypography.Title3,
    titleSmall = ExitTypography.Body,
    bodyLarge = ExitTypography.Body,
    bodyMedium = ExitTypography.Subheadline,
    bodySmall = ExitTypography.Caption,
    labelLarge = ExitTypography.Subheadline,
    labelMedium = ExitTypography.Caption,
    labelSmall = ExitTypography.Caption2
)

