package com.woweverstudio.exit_aos.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import kotlin.math.roundToInt

/**
 * 커스텀 Exit 슬라이더
 * iOS 스타일의 깔끔한 슬라이더
 * 
 * @param step 값의 증감 단위 (null이면 연속적인 값)
 */
@Composable
fun ExitSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    label: String,
    valueFormatter: (Float) -> String,
    accentColor: Color = ExitColors.Accent,
    step: Float? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    
    val thumbRadius = 12.dp
    val trackHeight = 6.dp
    
    // 값을 step 단위로 스냅
    fun snapToStep(rawValue: Float): Float {
        return if (step != null && step > 0) {
            val snapped = (rawValue / step).roundToInt() * step
            snapped.coerceIn(valueRange.start, valueRange.endInclusive)
        } else {
            rawValue.coerceIn(valueRange.start, valueRange.endInclusive)
        }
    }
    
    // 값을 0-1 범위로 정규화
    val normalizedValue = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        // 라벨 + 값 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText
            )
            Text(
                text = valueFormatter(value),
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        
        // 슬라이더 트랙
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbRadius * 2 + 8.dp)
                .onSizeChanged { sliderWidth = it.width.toFloat() }
                .pointerInput(valueRange, sliderWidth, step) {
                    detectTapGestures { offset ->
                        if (sliderWidth > 0) {
                            val thumbRadiusPx = with(density) { thumbRadius.toPx() }
                            val availableWidth = sliderWidth - thumbRadiusPx * 2
                            val newNormalized = ((offset.x - thumbRadiusPx) / availableWidth).coerceIn(0f, 1f)
                            val rawValue = valueRange.start + newNormalized * (valueRange.endInclusive - valueRange.start)
                            onValueChange(snapToStep(rawValue))
                        }
                    }
                }
                .pointerInput(valueRange, sliderWidth, step) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        if (sliderWidth > 0) {
                            val thumbRadiusPx = with(density) { thumbRadius.toPx() }
                            val availableWidth = sliderWidth - thumbRadiusPx * 2
                            val newNormalized = ((change.position.x - thumbRadiusPx) / availableWidth).coerceIn(0f, 1f)
                            val rawValue = valueRange.start + newNormalized * (valueRange.endInclusive - valueRange.start)
                            onValueChange(snapToStep(rawValue))
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // 배경 트랙
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .background(ExitColors.SecondaryCardBackground)
            )
            
            // 활성 트랙 (그라데이션)
            val thumbRadiusPx = with(density) { thumbRadius.toPx() }
            val activeWidth = if (sliderWidth > 0) {
                thumbRadiusPx + (sliderWidth - thumbRadiusPx * 2) * normalizedValue
            } else 0f
            
            Box(
                modifier = Modifier
                    .height(trackHeight)
                    .then(
                        if (activeWidth > 0) {
                            Modifier
                                .fillMaxWidth(fraction = (activeWidth / sliderWidth).coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(trackHeight / 2))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.6f),
                                            accentColor
                                        )
                                    )
                                )
                        } else Modifier
                    )
            )
            
            // 썸(Thumb)
            if (sliderWidth > 0) {
                val thumbOffset = thumbRadiusPx + (sliderWidth - thumbRadiusPx * 2) * normalizedValue - thumbRadiusPx
                
                Box(
                    modifier = Modifier
                        .offset { IntOffset(thumbOffset.roundToInt(), 0) }
                        .size(thumbRadius * 2)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = accentColor.copy(alpha = 0.3f),
                            spotColor = accentColor.copy(alpha = 0.3f)
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    // 내부 원 (액센트 색상)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(thumbRadius)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }
        }
    }
}

