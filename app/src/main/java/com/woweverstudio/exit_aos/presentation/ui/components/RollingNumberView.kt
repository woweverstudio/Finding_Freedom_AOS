package com.woweverstudio.exit_aos.presentation.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import kotlinx.coroutines.delay

/**
 * 파친코 스타일 숫자 롤링 애니메이션 뷰
 * iOS의 RollingNumberView와 동일한 동작
 *
 * @param value 표시할 목표 숫자
 * @param style 텍스트 스타일
 * @param color 텍스트 색상
 * @param animationTrigger 애니메이션 트리거 (이 값이 변경되면 애니메이션 재시작)
 */
@Composable
fun RollingNumberView(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = ExitTypography.Title2,
    color: Color = ExitColors.Accent,
    animationTrigger: String = ""
) {
    var displayedValue by remember { mutableIntStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    
    // Spring 애니메이션 (iOS: response: 0.15, dampingFraction: 0.8)
    val animatedValue by animateIntAsState(
        targetValue = displayedValue,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "rollingNumber"
    )
    
    // 애니메이션 트리거가 변경되거나 처음 로드 시 애니메이션 시작
    LaunchedEffect(animationTrigger, value) {
        if (isAnimating) return@LaunchedEffect
        isAnimating = true
        displayedValue = 0
        
        // 단계별 애니메이션으로 숫자가 올라가는 효과
        // iOS: totalDuration = 0.4초, steps = min(12, max(6, value / 4))
        val totalDuration = 400L // 0.4초
        val steps = minOf(12, maxOf(6, value / 4))
        val stepDuration = totalDuration / steps
        
        for (step in 0..steps) {
            val progress = step.toFloat() / steps
            val targetValue = (value * progress).toInt()
            displayedValue = targetValue
            
            if (step < steps) {
                delay(stepDuration)
            }
        }
        
        // 최종값 보정
        displayedValue = value
        isAnimating = false
    }
    
    Text(
        text = "$animatedValue",
        style = style,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        modifier = modifier
    )
}

/**
 * D-Day 롤링 애니메이션 뷰 (X년 Y개월 형식)
 * iOS의 DDayRollingView와 동일한 동작
 *
 * @param months 총 개월 수
 * @param animationTrigger 애니메이션 트리거 (이 값이 변경되면 애니메이션 재시작)
 */
@Composable
fun DDayRollingView(
    months: Int,
    animationTrigger: String,
    modifier: Modifier = Modifier
) {
    val targetYears = months / 12
    val targetMonths = months % 12
    
    var displayedYears by remember { mutableIntStateOf(0) }
    var displayedMonths by remember { mutableIntStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    
    // Spring 애니메이션 for years (iOS: response: 0.12, dampingFraction: 0.85)
    val animatedYears by animateIntAsState(
        targetValue = displayedYears,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rollingYears"
    )
    
    // Spring 애니메이션 for months
    val animatedMonths by animateIntAsState(
        targetValue = displayedMonths,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rollingMonths"
    )
    
    // 애니메이션 트리거가 변경되거나 처음 로드 시 애니메이션 시작
    LaunchedEffect(animationTrigger, months) {
        if (isAnimating) return@LaunchedEffect
        isAnimating = true
        displayedYears = 0
        displayedMonths = 0
        
        // 단계별 애니메이션
        // iOS: totalDuration = 0.5초, steps = 15
        val totalDuration = 500L // 0.5초
        val steps = 15
        val stepDuration = totalDuration / steps
        
        for (step in 0..steps) {
            val progress = step.toFloat() / steps
            displayedYears = (targetYears * progress).toInt()
            displayedMonths = (targetMonths * progress).toInt()
            
            if (step < steps) {
                delay(stepDuration)
            }
        }
        
        // 최종값 보정
        displayedYears = targetYears
        displayedMonths = targetMonths
        isAnimating = false
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (targetYears > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$animatedYears",
                    style = ExitTypography.Title2,
                    fontWeight = FontWeight.ExtraBold,
                    color = ExitColors.Accent
                )
                Text(
                    text = "년",
                    style = ExitTypography.Title3,
                    fontWeight = FontWeight.SemiBold,
                    color = ExitColors.Accent
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$animatedMonths",
                style = ExitTypography.Title2,
                fontWeight = FontWeight.ExtraBold,
                color = ExitColors.Accent
            )
            Text(
                text = "개월",
                style = ExitTypography.Title3,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.Accent
            )
        }
    }
}

