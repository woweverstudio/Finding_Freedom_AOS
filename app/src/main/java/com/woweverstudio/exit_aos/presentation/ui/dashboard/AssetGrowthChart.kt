package com.woweverstudio.exit_aos.presentation.ui.dashboard

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woweverstudio.exit_aos.presentation.ui.theme.*
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 자산 성장 예측 차트 (인터랙티브)
 */
@Composable
fun AssetGrowthChart(
    currentAsset: Double,
    targetAsset: Double,
    monthlyInvestment: Double,
    preRetirementReturnRate: Double,
    monthsToRetirement: Int,
    modifier: Modifier = Modifier
) {
    var selectedProgress by remember { mutableStateOf<Float?>(null) }
    
    val yearsToRetirement = max(1, monthsToRetirement / 12)
    
    // 선택된 데이터 계산
    val selectedData = remember(selectedProgress, monthsToRetirement, currentAsset, monthlyInvestment, preRetirementReturnRate) {
        selectedProgress?.let { progress ->
            val clampedProgress = progress.coerceIn(0f, 1f)
            val month = (monthsToRetirement * clampedProgress).toInt()
            
            // 날짜 계산
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, month)
            val dateFormat = SimpleDateFormat("yyyy년 M월", Locale.KOREA)
            val dateString = dateFormat.format(calendar.time)
            
            // 자산 계산
            val asset = calculateAssetAtMonth(currentAsset, monthlyInvestment, preRetirementReturnRate, month)
            
            SelectedData(dateString, asset)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        // 헤더
        HeaderSection(selectedData = selectedData)
        
        // 차트
        InteractiveChartSection(
            currentAsset = currentAsset,
            targetAsset = targetAsset,
            monthlyInvestment = monthlyInvestment,
            preRetirementReturnRate = preRetirementReturnRate,
            monthsToRetirement = monthsToRetirement,
            selectedProgress = selectedProgress,
            onProgressChanged = { selectedProgress = it }
        )
        
        // 2년 이상일 때만 연도별 마일스톤 표시
        if (yearsToRetirement >= 2) {
            YearlyMilestonesSection(
                currentAsset = currentAsset,
                monthlyInvestment = monthlyInvestment,
                preRetirementReturnRate = preRetirementReturnRate,
                yearsToRetirement = yearsToRetirement
            )
        }
    }
}

private data class SelectedData(
    val date: String,
    val asset: Double
)

@Composable
private fun HeaderSection(selectedData: SelectedData?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = ExitColors.Accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "자산 성장 예측",
                style = ExitTypography.Subheadline,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.PrimaryText
            )
        }
        
        if (selectedData != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
                modifier = Modifier
                    .clip(RoundedCornerShape(ExitRadius.SM))
                    .background(ExitColors.Accent.copy(alpha = 0.1f))
                    .padding(horizontal = ExitSpacing.SM, vertical = ExitSpacing.XS)
            ) {
                Text(
                    text = selectedData.date,
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
                Text(
                    text = ExitNumberFormatter.formatToEokMan(selectedData.asset),
                    style = ExitTypography.Caption,
                    fontWeight = FontWeight.SemiBold,
                    color = ExitColors.Accent
                )
            }
        } else {
            Text(
                text = "차트를 터치하여 시점별 자산을 확인하세요",
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText
            )
        }
    }
}

@Composable
private fun InteractiveChartSection(
    currentAsset: Double,
    targetAsset: Double,
    monthlyInvestment: Double,
    preRetirementReturnRate: Double,
    monthsToRetirement: Int,
    selectedProgress: Float?,
    onProgressChanged: (Float) -> Unit
) {
    val accentColor = ExitColors.Accent
    val gridColor = ExitColors.Divider
    val labelColor = ExitColors.TertiaryText
    
    val yMin = currentAsset * 0.95
    val yMax = targetAsset * 1.05
    val yRange = yMax - yMin
    
    // 차트 데이터 생성
    val chartData = remember(currentAsset, monthlyInvestment, preRetirementReturnRate, monthsToRetirement) {
        generateChartData(currentAsset, monthlyInvestment, preRetirementReturnRate, monthsToRetirement)
    }
    
    // X축 라벨 생성
    val xAxisLabels = remember(monthsToRetirement) {
        listOf(0f, 0.25f, 0.5f, 0.75f, 1f).map { progress ->
            val label = when {
                progress == 0f -> "현재"
                progress == 1f -> {
                    val years = monthsToRetirement / 12
                    if (years > 0) "${years}년" else "${monthsToRetirement}개월"
                }
                else -> {
                    val months = (monthsToRetirement * progress).toInt()
                    val years = months / 12
                    if (years > 0) "${years}년" else "${months}개월"
                }
            }
            progress to label
        }
    }
    
    // Y축 값 생성 (4등분)
    val yAxisValues = remember(yMin, yMax) {
        listOf(0f, 0.25f, 0.5f, 0.75f, 1f).map { ratio ->
            yMin + yRange * ratio
        }
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val leftPadding = 45.dp.toPx()
                    val bottomPadding = 24.dp.toPx()
                    val chartWidth = size.width - leftPadding
                    val progress = ((offset.x - leftPadding) / chartWidth).coerceIn(0f, 1f)
                    onProgressChanged(progress)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val leftPadding = 45.dp.toPx()
                    val chartWidth = size.width - leftPadding
                    val progress = ((change.position.x - leftPadding) / chartWidth).coerceIn(0f, 1f)
                    onProgressChanged(progress)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        
        val leftPadding = 45.dp.toPx()
        val bottomPadding = 24.dp.toPx()
        val chartLeft = leftPadding
        val chartRight = width
        val chartTop = 0f
        val chartBottom = height - bottomPadding
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop
        
        // Y축 정규화 함수
        fun normalizeY(value: Double): Float {
            return (chartBottom - ((value - yMin) / yRange * chartHeight)).toFloat()
        }
        
        // X축 정규화 함수
        fun normalizeX(progress: Float): Float {
            return chartLeft + chartWidth * progress
        }
        
        // Y축 그리드 및 라벨
        yAxisValues.forEach { value ->
            val y = normalizeY(value)
            
            // 그리드 라인
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1f
            )
            
            // Y축 라벨
            drawContext.canvas.nativeCanvas.apply {
                val paint = Paint().apply {
                    color = labelColor.toArgb()
                    textSize = 9.sp.toPx()
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                drawText(
                    ExitNumberFormatter.formatChartAxis(value),
                    chartLeft - 4.dp.toPx(),
                    y + 4.dp.toPx(),
                    paint
                )
            }
        }
        
        // X축 그리드 및 라벨
        xAxisLabels.forEach { (progress, label) ->
            val x = normalizeX(progress)
            
            // 그리드 라인
            drawLine(
                color = gridColor,
                start = Offset(x, chartTop),
                end = Offset(x, chartBottom),
                strokeWidth = 1f
            )
            
            // X축 라벨
            drawContext.canvas.nativeCanvas.apply {
                val paint = Paint().apply {
                    color = labelColor.toArgb()
                    textSize = 9.sp.toPx()
                    textAlign = when {
                        progress == 0f -> Paint.Align.LEFT
                        progress == 1f -> Paint.Align.RIGHT
                        else -> Paint.Align.CENTER
                    }
                    isAntiAlias = true
                }
                drawText(
                    label,
                    x,
                    height - 4.dp.toPx(),
                    paint
                )
            }
        }
        
        // 그라데이션 영역 Path
        val areaPath = Path().apply {
            moveTo(chartLeft, chartBottom)
            chartData.forEach { point ->
                lineTo(normalizeX(point.progress), normalizeY(point.asset))
            }
            lineTo(chartRight, chartBottom)
            close()
        }
        
        // 그라데이션 영역 그리기
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.3f),
                    accentColor.copy(alpha = 0.05f)
                )
            )
        )
        
        // 선 차트 Path
        val linePath = Path().apply {
            chartData.forEachIndexed { index, point ->
                val x = normalizeX(point.progress)
                val y = normalizeY(point.asset)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        
        // 선 차트 그리기
        drawPath(
            path = linePath,
            color = accentColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
        
        // 선택된 위치 표시
        selectedProgress?.let { progress ->
            val clampedProgress = progress.coerceIn(0f, 1f)
            val month = (monthsToRetirement * clampedProgress).toInt()
            val asset = calculateAssetAtMonth(currentAsset, monthlyInvestment, preRetirementReturnRate, month)
            
            val x = normalizeX(clampedProgress)
            val y = normalizeY(asset)
            
            // 수직 점선
            drawLine(
                color = accentColor.copy(alpha = 0.5f),
                start = Offset(x, chartTop),
                end = Offset(x, chartBottom),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
            
            // 선택 포인트
            drawCircle(
                color = accentColor,
                radius = 12f,
                center = Offset(x, y)
            )
            drawCircle(
                color = ExitColors.CardBackground,
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun YearlyMilestonesSection(
    currentAsset: Double,
    monthlyInvestment: Double,
    preRetirementReturnRate: Double,
    yearsToRetirement: Int
) {
    val milestones = remember(currentAsset, monthlyInvestment, preRetirementReturnRate, yearsToRetirement) {
        val monthlyRate = preRetirementReturnRate / 100 / 12
        (1..yearsToRetirement).map { year ->
            val month = year * 12
            var asset = currentAsset
            repeat(month) {
                asset = asset * (1 + monthlyRate) + monthlyInvestment
            }
            YearlyMilestone(year = year, asset = asset)
        }
    }
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        items(milestones) { milestone ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(ExitRadius.SM))
                    .background(ExitColors.Background)
                    .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.SM)
            ) {
                Text(
                    text = "${milestone.year}년",
                    style = ExitTypography.Caption2,
                    color = ExitColors.TertiaryText
                )
                Text(
                    text = ExitNumberFormatter.formatToEokMan(milestone.asset),
                    style = ExitTypography.Caption,
                    fontWeight = FontWeight.Medium,
                    color = ExitColors.PrimaryText
                )
            }
        }
    }
}

// MARK: - Data Classes

private data class ChartPoint(
    val progress: Float,
    val asset: Double
)

private data class YearlyMilestone(
    val year: Int,
    val asset: Double
)

// MARK: - Helper Functions

private fun calculateAssetAtMonth(
    currentAsset: Double,
    monthlyInvestment: Double,
    preRetirementReturnRate: Double,
    month: Int
): Double {
    val monthlyRate = preRetirementReturnRate / 100 / 12
    var asset = currentAsset
    repeat(month) {
        asset = asset * (1 + monthlyRate) + monthlyInvestment
    }
    return asset
}

private fun generateChartData(
    currentAsset: Double,
    monthlyInvestment: Double,
    preRetirementReturnRate: Double,
    monthsToRetirement: Int
): List<ChartPoint> {
    val data = mutableListOf<ChartPoint>()
    val monthlyRate = preRetirementReturnRate / 100 / 12
    val totalMonths = max(monthsToRetirement, 1)
    
    // 50개 포인트로 부드러운 곡선 생성
    val pointCount = 50
    
    for (i in 0..pointCount) {
        val progress = i.toFloat() / pointCount
        val month = (totalMonths * progress).toInt()
        
        var asset = currentAsset
        repeat(month) {
            asset = asset * (1 + monthlyRate) + monthlyInvestment
        }
        
        data.add(ChartPoint(progress = progress, asset = asset))
    }
    
    return data
}
