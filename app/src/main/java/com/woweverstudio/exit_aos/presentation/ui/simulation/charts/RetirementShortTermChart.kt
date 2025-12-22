package com.woweverstudio.exit_aos.presentation.ui.simulation.charts

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.text.style.TextAlign

/**
 * 은퇴 후 단기(1~10년) 자산 변화 차트
 * iOS의 RetirementShortTermChart.swift와 동일
 */
@Composable
fun RetirementShortTermChart(
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
    
    // 단기 데이터 (0~10년, 최대 11개 포인트)
    val shortTermYears = 10
    val veryBestShortTerm = result.shortTermVeryBestPath.yearlyAssets.take(shortTermYears + 1)
    val luckyShortTerm = result.shortTermLuckyPath.yearlyAssets.take(shortTermYears + 1)
    val medianShortTerm = result.shortTermMedianPath.yearlyAssets.take(shortTermYears + 1)
    val unluckyShortTerm = result.shortTermUnluckyPath.yearlyAssets.take(shortTermYears + 1)
    val veryWorstShortTerm = result.shortTermVeryWorstPath.yearlyAssets.take(shortTermYears + 1)
    val deterministicShortTerm = result.deterministicPath.yearlyAssets.take(shortTermYears + 1)
    
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
            veryBestShortTerm = veryBestShortTerm,
            luckyShortTerm = luckyShortTerm,
            medianShortTerm = medianShortTerm,
            unluckyShortTerm = unluckyShortTerm,
            veryWorstShortTerm = veryWorstShortTerm
        )
        
        // 3. 차트
        ShortTermLineChart(
            veryBest = veryBestShortTerm,
            lucky = luckyShortTerm,
            median = medianShortTerm,
            unlucky = unluckyShortTerm,
            veryWorst = veryWorstShortTerm,
            deterministic = deterministicShortTerm,
            startingAsset = startingAsset
        )
        
        // 4. 범례
        LegendSection()
        
        // 5. 연도별 상세
        YearlyDetailSection(medianShortTerm = medianShortTerm)
        
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
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = ExitColors.Accent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "은퇴 초반 10년, 어떻게 될까?",
                style = ExitTypography.Title3,
                color = ExitColors.PrimaryText
            )
        }
        
        Text(
            text = "은퇴 직후가 가장 중요해요. 처음 10년의 시장 상황이 전체를 좌우합니다.",
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
                text = "10년 후",
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
    veryBestShortTerm: List<Double>,
    luckyShortTerm: List<Double>,
    medianShortTerm: List<Double>,
    unluckyShortTerm: List<Double>,
    veryWorstShortTerm: List<Double>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        // 첫 번째 줄: 매우 행운, 행운, 평균
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            ScenarioCard(
                title = "매우 행운",
                amount = veryBestShortTerm.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(veryBestShortTerm),
                backgroundColor = ExitColors.Positive.copy(alpha = 0.15f),
                accentColor = ExitColors.Positive,
                modifier = Modifier.weight(1f)
            )
            ScenarioCard(
                title = "행운",
                amount = luckyShortTerm.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(luckyShortTerm),
                backgroundColor = ExitColors.Accent.copy(alpha = 0.15f),
                accentColor = ExitColors.Accent,
                modifier = Modifier.weight(1f)
            )
            ScenarioCard(
                title = "평균",
                amount = medianShortTerm.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(medianShortTerm),
                backgroundColor = ExitColors.PrimaryText.copy(alpha = 0.1f),
                accentColor = ExitColors.PrimaryText,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 두 번째 줄: 불행, 매우 불행
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            ScenarioCard(
                title = "불행",
                amount = unluckyShortTerm.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(unluckyShortTerm),
                backgroundColor = ExitColors.Caution.copy(alpha = 0.15f),
                accentColor = ExitColors.Caution,
                modifier = Modifier.weight(1f)
            )
            ScenarioCard(
                title = "매우 불행",
                amount = veryWorstShortTerm.lastOrNull() ?: 0.0,
                changeRate = calculateChangeRate(veryWorstShortTerm),
                backgroundColor = ExitColors.Warning.copy(alpha = 0.15f),
                accentColor = ExitColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScenarioCard(
    title: String,
    amount: Double,
    changeRate: Double,
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

@Composable
private fun ShortTermLineChart(
    veryBest: List<Double>,
    lucky: List<Double>,
    median: List<Double>,
    unlucky: List<Double>,
    veryWorst: List<Double>,
    deterministic: List<Double>,
    startingAsset: Double
) {
    val allData = listOf(veryBest, lucky, median, unlucky, veryWorst, deterministic).flatten()
    val minY = (allData.minOrNull() ?: 0.0).coerceAtLeast(0.0)
    val maxY = (allData.maxOrNull() ?: startingAsset) * 1.1
    
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
            return (chartBottom - ((value - minY) / (maxY - minY) * chartHeight)).toFloat()
        }
        
        // X축 정규화 함수 (0~10년)
        fun normalizeX(index: Int, total: Int): Float {
            return (chartLeft + (index.toFloat() / (total - 1).coerceAtLeast(1)) * chartWidth)
        }
        
        // Y축 그리드 라인 및 라벨 (4개)
        val yAxisCount = 4
        for (i in 0..yAxisCount) {
            val value = minY + (maxY - minY) * i / yAxisCount
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
        
        // X축 라벨 (0~10년)
        for (year in 0..10 step 2) {
            val x = chartLeft + (year.toFloat() / 10) * chartWidth
            
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
            // 기존 예측선 (점선) - Catmull-Rom 스플라인
            if (deterministic.size >= 2) {
                val path = Path()
                val points = deterministic.mapIndexed { index, value ->
                    Offset(normalizeX(index, deterministic.size), normalizeY(value))
                }
                
                path.moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = if (i > 0) points[i - 1] else points[i]
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val p3 = if (i < points.size - 2) points[i + 2] else points[i + 1]
                    
                    val cp1x = p1.x + (p2.x - p0.x) / 6f
                    val cp1y = p1.y + (p2.y - p0.y) / 6f
                    val cp2x = p2.x - (p3.x - p1.x) / 6f
                    val cp2y = p2.y - (p3.y - p1.y) / 6f
                    
                    path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                }
                
                drawPath(
                    path = path,
                    color = ExitColors.TertiaryText,
                    style = Stroke(
                        width = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)),
                        cap = StrokeCap.Round
                    )
                )
            }
            
            // 매우 불행 (빨간색)
            drawScenarioLine(veryWorst, ExitColors.Warning, ::normalizeX, ::normalizeY)
            
            // 불행 (주황색)
            drawScenarioLine(unlucky, ExitColors.Caution, ::normalizeX, ::normalizeY)
            
            // 평균 (회색)
            drawScenarioLine(median, ExitColors.PrimaryText.copy(alpha = 0.7f), ::normalizeX, ::normalizeY)
            
            // 행운 (액센트)
            drawScenarioLine(lucky, ExitColors.Accent, ::normalizeX, ::normalizeY)
            
            // 매우 행운 (초록색)
            drawScenarioLine(veryBest, ExitColors.Positive, ::normalizeX, ::normalizeY)
            
            // 시작점 표시
            drawCircle(
                color = ExitColors.Accent,
                radius = 10f,
                center = Offset(chartLeft, normalizeY(startingAsset))
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScenarioLine(
    data: List<Double>,
    color: Color,
    normalizeX: (Int, Int) -> Float,
    normalizeY: (Double) -> Float
) {
    if (data.size < 2) return
    
    val path = Path()
    val points = data.mapIndexed { index, value ->
        Offset(normalizeX(index, data.size), normalizeY(value))
    }
    
    // Catmull-Rom 스플라인으로 부드러운 곡선 생성
    path.moveTo(points.first().x, points.first().y)
    
    for (i in 0 until points.size - 1) {
        val p0 = if (i > 0) points[i - 1] else points[i]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = if (i < points.size - 2) points[i + 2] else points[i + 1]
        
        // Catmull-Rom을 Cubic Bezier로 변환
        val cp1x = p1.x + (p2.x - p0.x) / 6f
        val cp1y = p1.y + (p2.y - p0.y) / 6f
        val cp2x = p2.x - (p3.x - p1.x) / 6f
        val cp2y = p2.y - (p3.y - p1.y) / 6f
        
        path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 6f, cap = StrokeCap.Round)
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
private fun YearlyDetailSection(medianShortTerm: List<Double>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Text(
            text = "연도별 예상 자산 (평균)",
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Medium,
            color = ExitColors.SecondaryText
        )
        
        val keyYears = listOf(1, 3, 5, 7, 10)
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            keyYears.forEach { year ->
                if (year < medianShortTerm.size) {
                    YearColumn(
                        year = year,
                        amount = medianShortTerm[year],
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun YearColumn(
    year: Int,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ExitRadius.SM))
            .background(ExitColors.SecondaryCardBackground)
            .padding(vertical = ExitSpacing.SM),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        Text(
            text = "${year}년",
            style = ExitTypography.Caption2,
            color = ExitColors.TertiaryText
        )
        Text(
            text = formatSimple(amount),
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Medium,
            color = ExitColors.PrimaryText
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
                text = "왜 처음 10년이 중요할까요?",
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Medium,
                color = ExitColors.SecondaryText
            )
            Text(
                text = "은퇴 직후 시장이 하락하면(불행) 회복할 시간이 부족해요. 반면 처음 몇 년이 좋으면 여유가 생겨요. 이를 '시퀀스 리스크'라고 해요.",
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
