package com.woweverstudio.exit_aos.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography

/**
 * 진행률 링 차트
 */
@Composable
fun ProgressRingView(
    progress: Float, // 0.0 ~ 1.0
    currentAmount: String,
    targetAmount: String,
    percentText: String,
    hideAmounts: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 12.dp
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1500),
        label = "progress"
    )
    
    LaunchedEffect(progress) {
        animatedProgress = progress
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 배경 링
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            drawArc(
                color = ExitColors.Divider,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = Size(this.size.width - strokePx, this.size.height - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        
        // 진행률 링
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val sweepAngle = animatedValue * 360f
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(ExitColors.Accent, ExitColors.AccentSecondary, ExitColors.Accent)
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = Size(this.size.width - strokePx, this.size.height - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        
        // 중앙 텍스트 (iOS 순서: 현재자산 → /목표자산 → 퍼센트)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 현재 자산
            Text(
                text = if (hideAmounts) "•••" else currentAmount,
                style = ExitTypography.Title3,
                color = ExitColors.PrimaryText,
                textAlign = TextAlign.Center
            )
            
            // / 목표 자산
            Text(
                text = if (hideAmounts) "/ •••" else "/ $targetAmount",
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(ExitSpacing.XS))
            
            // 퍼센트
            Text(
                text = percentText,
                style = ExitTypography.Title2,
                color = ExitColors.Accent
            )
        }
    }
}

