package com.woweverstudio.exit_aos.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Exit 앱 타이포그래피
 * Android는 iOS보다 폰트가 크게 보이므로 1~2sp씩 줄여서 적용
 */
object ExitTypography {
    
    // MARK: - 제목
    
    /** 대형 제목 (iOS 42pt → Android 40sp) */
    val LargeTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 46.sp
    )
    
    /** 제목 (iOS 32pt → Android 30sp) */
    val Title = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    )
    
    /** 중형 제목 (iOS 24pt → Android 22sp) */
    val Title2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )
    
    /** 소형 제목 (iOS 20pt → Android 18sp) */
    val Title3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )
    
    // MARK: - 본문
    
    /** 본문 (iOS 18pt → Android 16sp) */
    val Body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
    
    /** 서브 본문 (iOS 16pt → Android 15sp) */
    val Subheadline = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    )
    
    /** 캡션 (iOS 14pt → Android 13sp) */
    val Caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.sp
    )
    
    /** 소형 캡션 (iOS 12pt → Android 11sp) */
    val Caption2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
    
    /** 소형 캡션 (iOS 13pt → Android 12sp Semibold) */
    val Caption3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    
    // MARK: - 숫자
    
    /** 대형 점수 숫자 (iOS 96pt → Android 92sp) */
    val ScoreDisplay = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 92.sp,
        lineHeight = 100.sp
    )
    
    /** 중형 숫자 (iOS 42pt → Android 40sp) */
    val NumberDisplay = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 46.sp
    )
    
    /** 숫자 (iOS 24pt → Android 22sp) */
    val Number = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )
    
    /** 키패드 숫자 (iOS 28pt → Android 26sp) */
    val Keypad = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp
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

