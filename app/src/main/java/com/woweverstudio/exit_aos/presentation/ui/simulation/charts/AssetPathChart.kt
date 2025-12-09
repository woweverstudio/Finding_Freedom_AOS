package com.woweverstudio.exit_aos.presentation.ui.simulation.charts

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woweverstudio.exit_aos.domain.model.UserProfile
import com.woweverstudio.exit_aos.domain.usecase.MonteCarloResult
import com.woweverstudio.exit_aos.domain.usecase.RepresentativePaths
import com.woweverstudio.exit_aos.domain.usecase.RetirementCalculator
import com.woweverstudio.exit_aos.presentation.ui.theme.*
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import kotlin.math.abs

/**
 * 자산 변화 예측 차트 + FIRE 달성 시점 비교
 * iOS의 AssetPathChart.swift와 동일
 */
@Composable
fun AssetPathChart(
    paths: RepresentativePaths,
    userProfile: UserProfile,
    result: MonteCarloResult?,
    originalDDayMonths: Int,
    currentAssetAmount: Double = 0.0,
    effectiveVolatility: Double = 0.0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        // 1. 타이틀
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = ExitColors.Accent
            )
            Text(
                text = "자산 변화 예측",
                style = ExitTypography.Title3,
                color = ExitColors.PrimaryText
            )
        }
        
        // 2. 차트
        AssetLineChart(
            paths = paths,
            userProfile = userProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
        
        // 3. 범례
        LegendView()
        
        // 4. FIRE 달성 시점 비교
        if (result != null && originalDDayMonths > 0) {
            TimelineSection(
                result = result,
                originalDDayMonths = originalDDayMonths
            )
        }
        
        // 5. 도움말
        HelpSection()
        
        // 6. 시뮬레이션 조건
        if (currentAssetAmount > 0) {
            SimulationConditionSection(
                userProfile = userProfile,
                currentAssetAmount = currentAssetAmount,
                effectiveVolatility = effectiveVolatility
            )
        }
    }
}

@Composable
private fun AssetLineChart(
    paths: RepresentativePaths,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val positiveColor = ExitColors.Positive
    val accentColor = ExitColors.Accent
    val cautionColor = ExitColors.Caution
    val gridColor = ExitColors.Divider
    val labelColor = ExitColors.TertiaryText
    
    val targetAsset = RetirementCalculator.calculateTargetAssets(
        desiredMonthlyIncome = userProfile.desiredMonthlyIncome,
        postRetirementReturnRate = userProfile.postRetirementReturnRate,
        inflationRate = userProfile.inflationRate
    )
    
    // 최대값 계산
    val allValues = paths.best.monthlyAssets + paths.median.monthlyAssets + paths.worst.monthlyAssets
    val maxValue = (allValues.maxOrNull() ?: targetAsset).coerceAtLeast(targetAsset) * 1.1
    val minValue = 0.0
    
    // X축 최대 개월 수 (iOS와 동일하게 모든 경로 중 가장 긴 것 사용)
    val maxMonths = maxOf(
        paths.best.monthlyAssets.size,
        paths.median.monthlyAssets.size,
        paths.worst.monthlyAssets.size
    )
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // 축 라벨을 위한 여백 설정
        val leftPadding = 50.dp.toPx()   // Y축 라벨 공간
        val bottomPadding = 30.dp.toPx() // X축 라벨 공간
        val topPadding = 16.dp.toPx()
        val rightPadding = 16.dp.toPx()
        
        val chartLeft = leftPadding
        val chartRight = width - rightPadding
        val chartTop = topPadding
        val chartBottom = height - bottomPadding
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop
        
        // Y축 값들 (4개)
        val yAxisValues = listOf(0.0, maxValue * 0.33, maxValue * 0.66, maxValue)
        
        // Y축 그리드 라인 및 라벨
        yAxisValues.forEach { value ->
            val y = chartTop + chartHeight * (1 - (value - minValue) / (maxValue - minValue)).toFloat()
            
            // 그리드 라인
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1f
            )
            
            // Y축 라벨
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = labelColor.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(
                    ExitNumberFormatter.formatChartAxis(value),
                    chartLeft - 8.dp.toPx(),
                    y + 4.dp.toPx(),
                    paint
                )
            }
        }
        
        // X축 라벨 (5개 구간)
        val xAxisCount = 5
        for (i in 0 until xAxisCount) {
            val months = (maxMonths * i / (xAxisCount - 1)).coerceAtLeast(0)
            val x = chartLeft + chartWidth * i / (xAxisCount - 1)
            val years = months / 12
            
            // 세로 그리드 라인
            drawLine(
                color = gridColor,
                start = Offset(x, chartTop),
                end = Offset(x, chartBottom),
                strokeWidth = 0.5f
            )
            
            // X축 라벨
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = labelColor.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(
                    "${years}년",
                    x,
                    chartBottom + 18.dp.toPx(),
                    paint
                )
            }
        }
        
        fun normalizeY(value: Double): Float {
            val normalized = ((value - minValue) / (maxValue - minValue)).toFloat()
            return chartTop + chartHeight * (1 - normalized)
        }
        
        // 모든 경로가 동일한 X축(maxMonths)을 기준으로 정규화
        fun normalizeX(index: Int): Float {
            return chartLeft + (index.toFloat() / (maxMonths - 1).coerceAtLeast(1)) * chartWidth
        }
        
        // 목표선 (점선)
        val targetY = normalizeY(targetAsset)
        drawLine(
            color = accentColor.copy(alpha = 0.3f),
            start = Offset(chartLeft, targetY),
            end = Offset(chartRight, targetY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )
        
        // 차트 영역 클리핑 적용
        clipRect(
            left = chartLeft,
            top = chartTop,
            right = chartRight,
            bottom = chartBottom
        ) {
            // 불행 경로 (worst)
            if (paths.worst.monthlyAssets.isNotEmpty()) {
                val worstPath = Path().apply {
                    paths.worst.monthlyAssets.forEachIndexed { index, value ->
                        val x = normalizeX(index)
                        val y = normalizeY(value)
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = worstPath,
                    color = cautionColor,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
            
            // 평균 경로 (median)
            if (paths.median.monthlyAssets.isNotEmpty()) {
                val medianPath = Path().apply {
                    paths.median.monthlyAssets.forEachIndexed { index, value ->
                        val x = normalizeX(index)
                        val y = normalizeY(value)
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = medianPath,
                    color = accentColor,
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
            }
            
            // 행운 경로 (best)
            if (paths.best.monthlyAssets.isNotEmpty()) {
                val bestPath = Path().apply {
                    paths.best.monthlyAssets.forEachIndexed { index, value ->
                        val x = normalizeX(index)
                        val y = normalizeY(value)
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = bestPath,
                    color = positiveColor,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun LegendView() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = ExitColors.Positive, label = "행운(상위10%)")
        LegendItem(color = ExitColors.Accent, label = "평균(50%)")
        LegendItem(color = ExitColors.Caution, label = "불행(하위10%)")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = ExitTypography.Caption2,
            color = ExitColors.SecondaryText
        )
    }
}

@Composable
private fun TimelineSection(
    result: MonteCarloResult,
    originalDDayMonths: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        Text(
            text = "목표 자산 달성 시점",
            style = ExitTypography.Subheadline,
            fontWeight = FontWeight.SemiBold,
            color = ExitColors.PrimaryText
        )
        
        TimelineChart(result = result, originalDDayMonths = originalDDayMonths)
        
        TimelineSummary(result = result, originalDDayMonths = originalDDayMonths)
    }
}

@Composable
private fun TimelineChart(
    result: MonteCarloResult,
    originalDDayMonths: Int
) {
    val timelineData = listOf(
        TimelineData("행운", result.bestCase10Percent, ExitColors.Positive, "🍀"),
        TimelineData("평균", result.medianMonths, ExitColors.Accent, "📊"),
        TimelineData("불행", result.worstCase10Percent, ExitColors.Caution, "🌧️"),
        TimelineData("기존 예측", originalDDayMonths, ExitColors.TertiaryText, "📌")
    )
    
    val maxMonths = timelineData.maxOfOrNull { it.months } ?: 1
    
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        timelineData.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.width(70.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.icon, style = ExitTypography.Caption2)
                    Text(
                        text = item.label,
                        style = ExitTypography.Caption2,
                        color = ExitColors.SecondaryText
                    )
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                ) {
                    // 배경 바
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(ExitColors.Divider)
                    )
                    
                    // 진행률 바
                    val fraction = if (maxMonths > 0) item.months.toFloat() / maxMonths else 0f
                    val showTextInside = fraction > 0.35f
                    
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction.coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(4.dp))
                            .background(item.color.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = formatYearsMonths(item.months),
                            style = ExitTypography.Caption2,
                            fontWeight = FontWeight.SemiBold,
                            color = if (showTextInside) Color.White else item.color,
                            modifier = Modifier
                                .align(if (showTextInside) Alignment.CenterEnd else Alignment.CenterStart)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineSummary(
    result: MonteCarloResult,
    originalDDayMonths: Int
) {
    val diff = result.medianMonths - originalDDayMonths
    
    val message = when {
        abs(diff) <= 6 -> "기존 예측과 비슷해요 👍"
        diff > 0 -> "시장 변동성 고려 시 +${formatYearsMonths(diff)} 예상"
        else -> "운이 좋으면 ${formatYearsMonths(abs(diff))} 단축 가능"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.SM))
            .background(ExitColors.Accent.copy(alpha = 0.1f))
            .padding(ExitSpacing.SM),
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = ExitColors.Accent
        )
        Text(
            text = message,
            style = ExitTypography.Caption,
            color = ExitColors.SecondaryText
        )
    }
}

@Composable
private fun HelpSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.SM))
            .background(ExitColors.SecondaryCardBackground)
            .padding(ExitSpacing.SM),
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = ExitColors.Accent
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            Text(
                text = "이 그래프가 알려주는 것",
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Medium,
                color = ExitColors.SecondaryText
            )
            
            Text(
                text = "시장 상황에 따라 자산이 어떻게 변할지 3가지 시나리오로 보여줘요. 행운(상위 10%)부터 불행(하위 10%)까지, 대부분의 경우가 이 범위 안에 들어요.",
                style = ExitTypography.Caption2,
                color = ExitColors.TertiaryText
            )
        }
    }
}

@Composable
private fun SimulationConditionSection(
    userProfile: UserProfile,
    currentAssetAmount: Double,
    effectiveVolatility: Double
) {
    val targetAsset = RetirementCalculator.calculateTargetAssets(
        desiredMonthlyIncome = userProfile.desiredMonthlyIncome,
        postRetirementReturnRate = userProfile.postRetirementReturnRate,
        inflationRate = userProfile.inflationRate
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Text(
            text = "📊 시뮬레이션 조건",
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Medium,
            color = ExitColors.SecondaryText
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataItem(label = "현재 자산", value = ExitNumberFormatter.formatChartAxis(currentAssetAmount))
            DataItem(label = "목표 자산", value = ExitNumberFormatter.formatChartAxis(targetAsset))
            DataItem(label = "수익률", value = String.format("%.1f%%", userProfile.preRetirementReturnRate))
            DataItem(label = "변동성", value = String.format("%.0f%%", effectiveVolatility))
        }
    }
}

@Composable
private fun DataItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = ExitTypography.Caption2,
            color = ExitColors.TertiaryText
        )
        Text(
            text = value,
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Medium,
            color = ExitColors.PrimaryText
        )
    }
}

private data class TimelineData(
    val label: String,
    val months: Int,
    val color: Color,
    val icon: String
)

private fun formatYearsMonths(months: Int): String {
    val years = months / 12
    val remainingMonths = months % 12
    return when {
        remainingMonths == 0 -> "${years}년"
        years == 0 -> "${remainingMonths}개월"
        else -> "${years}년 ${remainingMonths}개월"
    }
}
