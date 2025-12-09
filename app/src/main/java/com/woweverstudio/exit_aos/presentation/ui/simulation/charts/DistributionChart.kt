package com.woweverstudio.exit_aos.presentation.ui.simulation.charts

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woweverstudio.exit_aos.domain.model.UserProfile
import com.woweverstudio.exit_aos.domain.usecase.MonteCarloResult
import com.woweverstudio.exit_aos.presentation.ui.theme.*
import com.woweverstudio.exit_aos.util.ExitNumberFormatter

/**
 * 목표 달성 시점 분포 차트
 * iOS의 DistributionChart.swift와 동일
 */
@Composable
fun DistributionChart(
    yearDistributionData: List<YearDistributionData>,
    result: MonteCarloResult,
    userProfile: UserProfile?,
    currentAssetAmount: Double = 0.0,
    targetAssetAmount: Double = 0.0,
    effectiveVolatility: Double = 0.0,
    modifier: Modifier = Modifier
) {
    // 최빈 연도 찾기
    val peakYear = yearDistributionData.maxByOrNull { it.count }?.year ?: 0
    
    // 총 성공 횟수
    val totalSuccess = yearDistributionData.sumOf { it.count }
    
    // 80% 확률 구간 계산
    val probabilityRange = calculateProbabilityRange(yearDistributionData, totalSuccess)
    
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
                imageVector = Icons.Default.GpsFixed,
                contentDescription = null,
                tint = ExitColors.Accent
            )
            Text(
                text = "언제 달성할 가능성이 높을까?",
                style = ExitTypography.Title3,
                color = ExitColors.PrimaryText
            )
        }
        
        // 2. 핵심 수치
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Text(
                text = "${peakYear}년차",
                style = ExitTypography.Title.copy(fontSize = 36.sp),
                fontWeight = FontWeight.Bold,
                color = ExitColors.Accent
            )
            
            Text(
                text = "에 달성할 가능성이 가장 높아요",
                style = ExitTypography.Body,
                color = ExitColors.SecondaryText,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // 3. 차트
        DistributionBarChart(
            data = yearDistributionData,
            peakYear = peakYear,
            totalSuccess = totalSuccess,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )
        
        // 4. 범위 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RangeIndicator(
                icon = Icons.Default.Schedule,
                label = "빠르면",
                value = "${probabilityRange.first}년",
                color = ExitColors.Positive
            )
            RangeIndicator(
                icon = Icons.Default.GpsFixed,
                label = "대부분",
                value = "${peakYear}년",
                color = ExitColors.Accent
            )
            RangeIndicator(
                icon = Icons.Default.Warning,
                label = "늦으면",
                value = "${probabilityRange.second}년",
                color = ExitColors.Caution
            )
        }
        
        // 5. 도움말
        HelpSection(probabilityRange = probabilityRange)
        
        // 6. 시뮬레이션 조건
        if (userProfile != null && currentAssetAmount > 0) {
            SimulationConditionSection(
                userProfile = userProfile,
                currentAssetAmount = currentAssetAmount,
                targetAssetAmount = targetAssetAmount,
                effectiveVolatility = effectiveVolatility
            )
        }
    }
}

private fun calculateProbabilityRange(data: List<YearDistributionData>, totalSuccess: Int): Pair<Int, Int> {
    if (totalSuccess <= 0 || data.isEmpty()) return Pair(0, 0)
    
    val sortedData = data.sortedBy { it.year }
    var cumulative = 0
    var startYear = sortedData.first().year
    var endYear = sortedData.last().year
    
    // 10% 지점 찾기
    for (item in sortedData) {
        cumulative += item.count
        if (cumulative.toDouble() / totalSuccess >= 0.1) {
            startYear = item.year
            break
        }
    }
    
    // 90% 지점 찾기
    cumulative = 0
    for (item in sortedData) {
        cumulative += item.count
        if (cumulative.toDouble() / totalSuccess >= 0.9) {
            endYear = item.year
            break
        }
    }
    
    return Pair(startYear, endYear)
}

@Composable
private fun DistributionBarChart(
    data: List<YearDistributionData>,
    peakYear: Int,
    totalSuccess: Int,
    modifier: Modifier = Modifier
) {
    val accentColor = ExitColors.Accent
    val gridColor = ExitColors.Divider
    val labelColor = ExitColors.TertiaryText
    
    if (data.isEmpty()) return
    
    val maxCount = data.maxOfOrNull { it.count } ?: 1
    // 확률 변환
    val maxProbability = if (totalSuccess > 0) maxCount.toDouble() / totalSuccess * 100 else 0.0
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // 축 라벨을 위한 여백 설정
        val leftPadding = 40.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        val topPadding = 16.dp.toPx()
        val rightPadding = 16.dp.toPx()
        
        val chartLeft = leftPadding
        val chartRight = width - rightPadding
        val chartTop = topPadding
        val chartBottom = height - bottomPadding
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop
        
        val barCount = data.size
        if (barCount == 0) return@Canvas
        
        val barWidth = (chartWidth / barCount * 0.7f).coerceAtMost(20.dp.toPx())
        val barSpacing = chartWidth / barCount
        
        // Y축 그리드 라인 (0%, 10%, 20%, 30%)
        val yAxisValues = listOf(0, 10, 20, 30)
        val yMax = 30.0.coerceAtLeast(maxProbability * 1.1)
        
        yAxisValues.forEach { value ->
            val y = chartTop + chartHeight * (1 - value / yMax).toFloat()
            
            // 그리드 라인
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 0.5f
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
                    "$value%",
                    chartLeft - 6.dp.toPx(),
                    y + 4.dp.toPx(),
                    paint
                )
            }
        }
        
        // X축 라벨 (적절한 간격으로)
        val sortedData = data.sortedBy { it.year }
        val xLabelInterval = when {
            barCount <= 10 -> 1
            barCount <= 20 -> 2
            else -> 5
        }
        
        sortedData.forEachIndexed { index, item ->
            if (index % xLabelInterval == 0 || item.year == peakYear) {
                val x = chartLeft + index * barSpacing + barSpacing / 2
                
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = labelColor.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawText(
                        "${item.year}년",
                        x,
                        chartBottom + 18.dp.toPx(),
                        paint
                    )
                }
            }
        }
        
        // 최빈 연도 강조 (점선)
        val peakIndex = sortedData.indexOfFirst { it.year == peakYear }
        if (peakIndex >= 0) {
            val peakX = chartLeft + peakIndex * barSpacing + barSpacing / 2
            drawLine(
                color = accentColor.copy(alpha = 0.3f),
                start = Offset(peakX, chartTop),
                end = Offset(peakX, chartBottom),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )
        }
        
        // 클리핑 적용하여 바 그리기
        clipRect(
            left = chartLeft,
            top = chartTop,
            right = chartRight,
            bottom = chartBottom
        ) {
            sortedData.forEachIndexed { index, item ->
                val probability = if (totalSuccess > 0) item.count.toDouble() / totalSuccess * 100 else 0.0
                val barHeight = ((probability / yMax) * chartHeight).toFloat()
                val x = chartLeft + index * barSpacing + (barSpacing - barWidth) / 2
                val y = chartBottom - barHeight
                
                val color = if (item.year == peakYear) accentColor else accentColor.copy(alpha = 0.4f)
                
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }
    }
}

@Composable
private fun RangeIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        
        Text(
            text = label,
            style = ExitTypography.Caption2,
            color = ExitColors.TertiaryText
        )
        
        Text(
            text = value,
            style = ExitTypography.Body,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun HelpSection(probabilityRange: Pair<Int, Int>) {
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
                text = "막대가 높을수록 그 시점에 목표를 달성할 확률이 높아요. 대부분(80%)은 ${probabilityRange.first}~${probabilityRange.second}년 사이에 달성해요.",
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
    targetAssetAmount: Double,
    effectiveVolatility: Double
) {
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
            DataItem(label = "목표 자산", value = ExitNumberFormatter.formatChartAxis(targetAssetAmount))
            DataItem(label = "월 투자", value = ExitNumberFormatter.formatToManWon(userProfile.monthlyInvestment))
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

/**
 * 연도별 분포 데이터
 */
data class YearDistributionData(
    val year: Int,
    val count: Int
) {
    val probability: Double
        get() = count.toDouble()
}
