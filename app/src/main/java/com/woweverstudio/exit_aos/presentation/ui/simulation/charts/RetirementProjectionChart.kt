package com.woweverstudio.exit_aos.presentation.ui.simulation.charts

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.woweverstudio.exit_aos.domain.usecase.RetirementCalculator
import com.woweverstudio.exit_aos.domain.usecase.RetirementSimulationResult
import com.woweverstudio.exit_aos.presentation.ui.theme.*
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import kotlin.math.max
import androidx.compose.ui.text.style.TextAlign

/**
 * 은퇴 후 장기(40년) 자산 변화 예측 차트
 * iOS의 RetirementProjectionChart.swift와 동일
 */
@Composable
fun RetirementProjectionChart(
    result: RetirementSimulationResult,
    userProfile: UserProfile,
    spendingRatio: Double = 1.0,
    modifier: Modifier = Modifier
) {
    // 시뮬레이션 시작 자산
    val startingAsset = result.medianPath.yearlyAssets.firstOrNull()
        ?: RetirementCalculator.calculateTargetAssets(
            desiredMonthlyIncome = userProfile.desiredMonthlyIncome,
            postRetirementReturnRate = userProfile.postRetirementReturnRate
        )
    
    // 각 시나리오 경로 데이터
    val veryBestPath = result.veryBestPath.yearlyAssets
    val luckyPath = result.luckyPath.yearlyAssets
    val medianPath = result.medianPath.yearlyAssets
    val unluckyPath = result.unluckyPath.yearlyAssets
    val veryWorstPath = result.veryWorstPath.yearlyAssets
    val deterministicPath = result.deterministicPath.yearlyAssets
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        // 1. 타이틀 + 설명
        HeaderSection()
        
        // 1.5. 기준 설명
        ContextSection(startingAsset = startingAsset)
        
        // 2. 시나리오 카드들
        KeyMessageSection(
            veryBestPath = veryBestPath,
            luckyPath = luckyPath,
            medianPath = medianPath,
            unluckyPath = unluckyPath,
            veryWorstPath = veryWorstPath,
            veryBestDepletion = result.veryBestPath.depletionYear,
            luckyDepletion = result.luckyPath.depletionYear,
            medianDepletion = result.medianPath.depletionYear,
            unluckyDepletion = result.unluckyPath.depletionYear,
            veryWorstDepletion = result.veryWorstPath.depletionYear
        )
        
        // 3. 차트
        ProjectionLineChart(
            veryBest = veryBestPath,
            lucky = luckyPath,
            median = medianPath,
            unlucky = unluckyPath,
            veryWorst = veryWorstPath,
            deterministic = deterministicPath,
            startingAsset = startingAsset
        )
        
        // 4. 범례
        LegendSection()
        
        // 5. 연도별 테이블
        YearlyAssetTable(
            veryBest = veryBestPath,
            lucky = luckyPath,
            median = medianPath,
            unlucky = unluckyPath,
            veryWorst = veryWorstPath
        )
        
        // 6. 도움말
        HelpSection()
        
        // 7. 시뮬레이션 조건
        SimulationConditionSection(
            startingAsset = startingAsset,
            userProfile = userProfile,
            spendingRatio = spendingRatio
        )
    }
}

@Composable
private fun HeaderSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = ExitColors.Accent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "은퇴 후 40년, 어떻게 될까?",
                style = ExitTypography.Title3,
                color = ExitColors.PrimaryText
            )
        }
        
        Text(
            text = "장기적인 관점에서 시장 상황에 따라 자산이 어떻게 변할지 예측합니다.",
            style = ExitTypography.Caption,
            color = ExitColors.SecondaryText
        )
    }
}

@Composable
private fun ContextSection(startingAsset: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.SecondaryCardBackground)
            .padding(ExitSpacing.MD),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "시작 자산",
                style = ExitTypography.Caption2,
                color = ExitColors.SecondaryText
            )
            Text(
                text = formatSimple(startingAsset),
                style = ExitTypography.Body,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.Accent
            )
        }
        
        Spacer(modifier = Modifier.width(ExitSpacing.MD))
        
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = ExitColors.SecondaryText
        )
        
        Spacer(modifier = Modifier.width(ExitSpacing.MD))
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "40년 후",
                style = ExitTypography.Caption2,
                color = ExitColors.SecondaryText
            )
            Text(
                text = "시장 상황에 따라",
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText
            )
        }
    }
}

@Composable
private fun KeyMessageSection(
    veryBestPath: List<Double>,
    luckyPath: List<Double>,
    medianPath: List<Double>,
    unluckyPath: List<Double>,
    veryWorstPath: List<Double>,
    veryBestDepletion: Int?,
    luckyDepletion: Int?,
    medianDepletion: Int?,
    unluckyDepletion: Int?,
    veryWorstDepletion: Int?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        // 첫 번째 줄: 매우 행운, 행운, 평균
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            ProjectionScenarioCard(
                title = "매우 행운",
                amount = veryBestPath.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(veryBestPath),
                depletionYear = veryBestDepletion,
                backgroundColor = ExitColors.Positive.copy(alpha = 0.15f),
                accentColor = ExitColors.Positive,
                modifier = Modifier.weight(1f)
            )
            ProjectionScenarioCard(
                title = "행운",
                amount = luckyPath.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(luckyPath),
                depletionYear = luckyDepletion,
                backgroundColor = ExitColors.Accent.copy(alpha = 0.15f),
                accentColor = ExitColors.Accent,
                modifier = Modifier.weight(1f)
            )
            ProjectionScenarioCard(
                title = "평균",
                amount = medianPath.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(medianPath),
                depletionYear = medianDepletion,
                backgroundColor = ExitColors.PrimaryText.copy(alpha = 0.1f),
                accentColor = ExitColors.PrimaryText,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 두 번째 줄: 불행, 매우 불행
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            ProjectionScenarioCard(
                title = "불행",
                amount = unluckyPath.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(unluckyPath),
                depletionYear = unluckyDepletion,
                backgroundColor = ExitColors.Caution.copy(alpha = 0.15f),
                accentColor = ExitColors.Caution,
                modifier = Modifier.weight(1f)
            )
            ProjectionScenarioCard(
                title = "매우 불행",
                amount = veryWorstPath.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(veryWorstPath),
                depletionYear = veryWorstDepletion,
                backgroundColor = ExitColors.Warning.copy(alpha = 0.15f),
                accentColor = ExitColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProjectionScenarioCard(
    title: String,
    amount: Double,
    changeRate: Double,
    depletionYear: Int?,
    backgroundColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(backgroundColor)
            .padding(vertical = ExitSpacing.SM),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        Text(
            text = title,
            style = ExitTypography.Caption2,
            color = ExitColors.SecondaryText
        )
        
        if (depletionYear != null) {
            Text(
                text = "${depletionYear}년 뒤 소진",
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = "자산 소진",
                style = ExitTypography.Caption2,
                color = ExitColors.Warning
            )
        } else {
            Text(
                text = formatSimple(amount),
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = if (changeRate >= 0) "+${changeRate.toInt()}%" else "${changeRate.toInt()}%",
                style = ExitTypography.Caption2,
                color = if (changeRate >= 0) ExitColors.Positive else ExitColors.Warning
            )
        }
    }
}

@Composable
private fun ProjectionLineChart(
    veryBest: List<Double>,
    lucky: List<Double>,
    median: List<Double>,
    unlucky: List<Double>,
    veryWorst: List<Double>,
    deterministic: List<Double>,
    startingAsset: Double
) {
    // Y축 최대값 계산 (매우행운 제외, 스케일 안정화)
    val maxFromLucky = lucky.maxOrNull() ?: startingAsset
    val maxFromMedian = median.maxOrNull() ?: startingAsset
    val maxY = max(max(maxFromLucky, maxFromMedian), startingAsset) * 1.1
    
    val gridColor = ExitColors.Divider
    val labelColor = ExitColors.TertiaryText
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        
        // 축 라벨을 위한 여백 설정
        val leftPadding = 50.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        val topPadding = 16.dp.toPx()
        val rightPadding = 16.dp.toPx()
        
        val chartLeft = leftPadding
        val chartRight = width - rightPadding
        val chartTop = topPadding
        val chartBottom = height - bottomPadding
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop
        
        // Y축 정규화 함수
        fun normalizeY(value: Double): Float {
            val clampedValue = max(0.0, value)
            return (chartBottom - (clampedValue / maxY * chartHeight)).toFloat()
        }
        
        // X축 정규화 함수 (0~40년)
        fun normalizeX(index: Int, total: Int): Float {
            return (chartLeft + (index.toFloat() / (total - 1).coerceAtLeast(1)) * chartWidth)
        }
        
        // Y축 그리드 라인 및 라벨 (4개)
        val yAxisCount = 4
        for (i in 0..yAxisCount) {
            val value = maxY * i / yAxisCount
            val y = normalizeY(value)
            
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
                    ExitNumberFormatter.formatChartAxis(value),
                    chartLeft - 6.dp.toPx(),
                    y + 4.dp.toPx(),
                    paint
                )
            }
        }
        
        // X축 라벨 (0, 10, 20, 30, 40년)
        val xAxisYears = listOf(0, 10, 20, 30, 40)
        xAxisYears.forEach { year ->
            val x = chartLeft + (year.toFloat() / 40) * chartWidth
            
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
                    "${year}년",
                    x,
                    chartBottom + 18.dp.toPx(),
                    paint
                )
            }
        }
        
        // 클리핑 적용하여 차트 그리기
        clipRect(
            left = chartLeft,
            top = chartTop,
            right = chartRight,
            bottom = chartBottom
        ) {
            // 0선 (점선)
            val zeroY = normalizeY(0.0)
            if (zeroY < chartBottom) {
                drawLine(
                    color = ExitColors.Warning.copy(alpha = 0.5f),
                    start = Offset(chartLeft, zeroY),
                    end = Offset(chartRight, zeroY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
            
            // 기존 예측선 (점선)
            if (deterministic.size >= 2) {
                val path = Path()
                deterministic.forEachIndexed { index, value ->
                    val x = normalizeX(index, deterministic.size)
                    val y = normalizeY(value)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = ExitColors.TertiaryText,
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                        cap = StrokeCap.Round
                    )
                )
            }
            
            // 매우 불행 (빨간색)
            drawProjectionLine(veryWorst, ExitColors.Warning, ::normalizeX, ::normalizeY)
            
            // 불행 (주황색)
            drawProjectionLine(unlucky, ExitColors.Caution, ::normalizeX, ::normalizeY)
            
            // 평균 (회색)
            drawProjectionLine(median, ExitColors.PrimaryText.copy(alpha = 0.7f), ::normalizeX, ::normalizeY)
            
            // 행운 (액센트)
            drawProjectionLine(lucky, ExitColors.Accent, ::normalizeX, ::normalizeY)
            
            // 매우 행운 (초록색)
            drawProjectionLine(veryBest, ExitColors.Positive, ::normalizeX, ::normalizeY)
            
            // 시작점 표시
            drawCircle(
                color = ExitColors.Accent,
                radius = 8f,
                center = Offset(chartLeft, normalizeY(startingAsset))
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawProjectionLine(
    data: List<Double>,
    color: Color,
    normalizeX: (Int, Int) -> Float,
    normalizeY: (Double) -> Float
) {
    if (data.size < 2) return
    
    val path = Path()
    data.forEachIndexed { index, value ->
        val x = normalizeX(index, data.size)
        val y = normalizeY(max(0.0, value))
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 4f, cap = StrokeCap.Round)
    )
}

@Composable
private fun LegendSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
        ) {
            LegendItem(color = ExitColors.Positive, label = "매우행운(10%)")
            LegendItem(color = ExitColors.Accent, label = "행운(30%)")
            LegendItem(color = ExitColors.PrimaryText.copy(alpha = 0.7f), label = "평균(50%)")
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
        ) {
            LegendItem(color = ExitColors.Caution, label = "불행(70%)")
            LegendItem(color = ExitColors.Warning, label = "매우불행(90%)")
            LegendItem(color = ExitColors.TertiaryText, label = "기존예측", isDashed = true)
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isDashed: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        if (isDashed) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 2.dp)
                            .background(color, RoundedCornerShape(1.dp))
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 3.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
        Text(
            text = label,
            style = ExitTypography.Caption2,
            color = ExitColors.SecondaryText
        )
    }
}

@Composable
private fun YearlyAssetTable(
    veryBest: List<Double>,
    lucky: List<Double>,
    median: List<Double>,
    unlucky: List<Double>,
    veryWorst: List<Double>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Text(
            text = "연도별 예상 자산",
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Medium,
            color = ExitColors.SecondaryText
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.MD))
                .background(ExitColors.SecondaryCardBackground)
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ExitColors.Divider.copy(alpha = 0.5f))
                    .padding(horizontal = ExitSpacing.SM, vertical = ExitSpacing.XS)
            ) {
                Text(
                    text = "시나리오",
                    style = ExitTypography.Caption2,
                    color = ExitColors.SecondaryText,
                    modifier = Modifier.width(60.dp)
                )
                listOf(10, 20, 30, 40).forEach { year ->
                    Text(
                        text = "${year}년",
                        style = ExitTypography.Caption2,
                        color = ExitColors.SecondaryText,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            // 데이터 행
            AssetRow("매우행운", veryBest, ExitColors.Positive)
            AssetRow("행운", lucky, ExitColors.Accent)
            AssetRow("평균", median, ExitColors.PrimaryText)
            AssetRow("불행", unlucky, ExitColors.Caution)
            AssetRow("매우불행", veryWorst, ExitColors.Warning)
        }
    }
}

@Composable
private fun AssetRow(
    label: String,
    data: List<Double>,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.SM, vertical = ExitSpacing.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = ExitTypography.Caption2,
            color = color,
            modifier = Modifier.width(60.dp),
            maxLines = 1
        )
        listOf(10, 20, 30, 40).forEach { year ->
            val asset = if (year < data.size) data[year] else (data.lastOrNull() ?: 0.0)
            Text(
                text = formatTableValue(asset),
                style = ExitTypography.Caption2,
                color = if (asset > 0) ExitColors.PrimaryText else ExitColors.Warning,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
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
                text = "40년간 시장 상황에 따라 자산이 크게 달라질 수 있어요. 매우 행운인 경우 차트 범위를 벗어날 수 있으니 상단 카드와 테이블을 함께 확인하세요.",
                style = ExitTypography.Caption2,
                color = ExitColors.TertiaryText
            )
        }
    }
}

@Composable
private fun SimulationConditionSection(
    startingAsset: Double,
    userProfile: UserProfile,
    spendingRatio: Double
) {
    val actualSpending = userProfile.desiredMonthlyIncome * spendingRatio
    val spendingDisplayValue = if (spendingRatio < 1.0) {
        "${ExitNumberFormatter.formatToManWon(actualSpending)}(${String.format("%.0f", spendingRatio * 100)}%)"
    } else {
        ExitNumberFormatter.formatToManWon(actualSpending)
    }
    
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
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            DataItem(
                label = "시작 자산",
                value = ExitNumberFormatter.formatChartAxis(startingAsset),
                modifier = Modifier.weight(1f)
            )
            DataItem(
                label = "월 지출",
                value = spendingDisplayValue,
                modifier = Modifier.weight(1f)
            )
            DataItem(
                label = "수익률",
                value = String.format("%.1f%%", userProfile.postRetirementReturnRate),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DataItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = ExitTypography.Caption2,
            color = ExitColors.TertiaryText,
            maxLines = 1
        )
        Text(
            text = value,
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Medium,
            color = ExitColors.PrimaryText,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// 유틸리티 함수
private fun calculateChangeRate(data: List<Double>): Double {
    val first = data.firstOrNull() ?: return 0.0
    val last = data.lastOrNull() ?: return 0.0
    if (first <= 0) return 0.0
    return (last - first) / first * 100
}

/** 금액을 억단위로 간결하게 표시 (예: 7230만원 → 0.72억) */
private fun formatSimple(amount: Double): String {
    if (amount <= 0) return "0원"
    val eok = amount / 100_000_000
    return when {
        eok >= 1 -> String.format("%.2f억", eok)
        eok >= 0.01 -> String.format("%.2f억", eok)  // 100만원 이상 억단위로 표시
        else -> {
            val man = amount / 10_000
            String.format("%.0f만원", man)
        }
    }
}

/** 테이블용 짧은 포맷 (억단위로 간결하게, 소수점 둘째자리) */
private fun formatTableValue(amount: Double): String {
    if (amount <= 0) return "0"
    val eok = amount / 100_000_000
    return when {
        eok >= 1 -> String.format("%.2f억", eok)
        eok >= 0.01 -> String.format("%.2f억", eok)  // 100만원 이상 억단위로 표시
        else -> {
            val man = amount / 10_000
            String.format("%.0f만", man)
        }
    }
}
