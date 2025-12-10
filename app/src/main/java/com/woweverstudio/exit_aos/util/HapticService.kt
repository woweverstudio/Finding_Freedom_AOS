package com.woweverstudio.exit_aos.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * 햅틱 피드백 서비스
 * iOS의 HapticService.swift와 동일한 기능 제공
 */
object HapticService {
    
    private var vibrator: Vibrator? = null
    
    /**
     * 초기화 (Application에서 호출)
     */
    fun init(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    // MARK: - Impact Feedback
    
    /**
     * Light 햅틱 (가벼운 탭)
     */
    fun light(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            ?: vibrateLight()
    }
    
    /**
     * Medium 햅틱 (중간 탭)
     */
    fun medium(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            ?: vibrateMedium()
    }
    
    /**
     * Heavy 햅틱 (강한 탭)
     */
    fun heavy(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            ?: vibrateHeavy()
    }
    
    /**
     * Soft 햅틱 (부드러운 탭 - 키패드용)
     */
    fun soft(view: View? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (view == null) vibrateSoft()
    }
    
    /**
     * 선택 변경 햅틱 (피커, 스위치 등)
     */
    fun selection(view: View? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        if (view == null) vibrateSelection()
    }
    
    // MARK: - Notification Feedback
    
    /**
     * 성공 햅틱
     */
    fun success(view: View? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        if (view == null) vibrateSuccess()
    }
    
    /**
     * 경고 햅틱
     */
    fun warning(view: View? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        if (view == null) vibrateWarning()
    }
    
    /**
     * 에러 햅틱
     */
    fun error(view: View? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        if (view == null) vibrateError()
    }
    
    // MARK: - Vibrator fallback methods
    
    private fun vibrateLight() {
        vibrate(20, 50)
    }
    
    private fun vibrateMedium() {
        vibrate(30, 100)
    }
    
    private fun vibrateHeavy() {
        vibrate(50, 150)
    }
    
    private fun vibrateSoft() {
        vibrate(10, 30)
    }
    
    private fun vibrateSelection() {
        vibrate(5, 20)
    }
    
    private fun vibrateSuccess() {
        vibrate(30, 100)
    }
    
    private fun vibrateWarning() {
        vibrate(40, 120)
    }
    
    private fun vibrateError() {
        vibrate(50, 150)
    }
    
    private fun vibrate(duration: Long, amplitude: Int) {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(duration)
            }
        }
    }
}

/**
 * Compose에서 HapticService를 쉽게 사용하기 위한 헬퍼
 */
class HapticHelper(private val view: View) {
    fun light() = HapticService.light(view)
    fun medium() = HapticService.medium(view)
    fun heavy() = HapticService.heavy(view)
    fun soft() = HapticService.soft(view)
    fun selection() = HapticService.selection(view)
    fun success() = HapticService.success(view)
    fun warning() = HapticService.warning(view)
    fun error() = HapticService.error(view)
}

/**
 * Compose에서 HapticHelper를 사용하기 위한 remember 함수
 */
@Composable
fun rememberHaptic(): HapticHelper {
    val view = LocalView.current
    return remember(view) { HapticHelper(view) }
}

