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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import kotlinx.coroutines.delay

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
    
    // 이전 값 저장 (값 변경 감지용)
    var previousMonths by remember { mutableIntStateOf(months) }
    var previousTrigger by remember { mutableStateOf(animationTrigger) }
    var isFirstLoad by remember { mutableStateOf(true) }
    
    var displayedYears by remember { mutableIntStateOf(targetYears) }
    var displayedMonths by remember { mutableIntStateOf(targetMonths) }
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
    
    // 값이 변경되었을 때만 애니메이션 실행 (iOS와 동일)
    LaunchedEffect(animationTrigger, months) {
        // 첫 로드 시에는 애니메이션 없이 값만 설정
        if (isFirstLoad) {
            isFirstLoad = false
            displayedYears = targetYears
            displayedMonths = targetMonths
            previousMonths = months
            previousTrigger = animationTrigger
            return@LaunchedEffect
        }
        
        // 값이 변경되지 않았으면 애니메이션 하지 않음
        val monthsChanged = previousMonths != months
        val triggerChanged = previousTrigger != animationTrigger
        
        if (!monthsChanged && !triggerChanged) {
            return@LaunchedEffect
        }
        
        // 트리거만 변경되고 값이 같으면 애니메이션 하지 않음
        if (!monthsChanged && triggerChanged) {
            previousTrigger = animationTrigger
            return@LaunchedEffect
        }
        
        // 이전 값 업데이트
        previousMonths = months
        previousTrigger = animationTrigger
        
        // 이미 애니메이션 중이면 스킵
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

