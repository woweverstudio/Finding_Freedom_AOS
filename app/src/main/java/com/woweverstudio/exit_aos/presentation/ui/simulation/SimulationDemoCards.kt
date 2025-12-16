package com.woweverstudio.exit_aos.presentation.ui.simulation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woweverstudio.exit_aos.presentation.ui.theme.*

/**
 * 시뮬레이션 데모 카드들 (실제 UI 미리보기용)
 * - iOS의 SimulationDemoCards.swift와 99% 일치
 */
@Composable
fun SimulationDemoCards(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        SectionHeader(
            icon = Icons.Default.Visibility,
            title = "이런 결과를 볼 수 있어요",
            modifier = Modifier.padding(horizontal = ExitSpacing.MD)
        )
        
        ExampleDataNotice()
        
        // 1. 성공률 카드
        DemoSuccessRateCard()
        
        // 2. 자산 변화 예측 차트
        DemoAssetPathChart()
        
        // 3. 목표 달성 시점 분포
        DemoDistributionChart()
        
        // 4. 은퇴 후 10년 분석
        DemoRetirementShortTermCard()
    }
}

// MARK: - Example Data Notice

@Composable
private fun ExampleDataNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.Accent.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = ExitColors.Accent.copy(alpha = 0.3f),
                shape = RoundedCornerShape(ExitRadius.LG)
            )
            .padding(ExitSpacing.LG),
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = ExitColors.Accent
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            Text(
                text = "아래는 예시 데이터예요",
                style = ExitTypography.Subheadline,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.PrimaryText
            )
            
            Text(
                text = "실제 분석은 내 데이터를 기반으로 더 정확하고 상세한 결과를 보여드려요.",
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText
            )
        }
    }
}

// MARK: - Demo Success Rate Card

@Composable
private fun DemoSuccessRateCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.XL))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        // 타이틀
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            DemoBadge()
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Percent,
                contentDescription = null,
                tint = ExitColors.Accent
            )
            Text(
                text = "성공 확률",
                style = ExitTypography.Title3,
                color = ExitColors.PrimaryText
            )
        }
        
        // 큰 성공률 표시
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Text(
                text = "계획대로 회사 탈출에 성공할 확률",
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText
            )
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "78",
                    style = ExitTypography.LargeTitle.copy(fontSize = 72.sp),
                    fontWeight = FontWeight.Black,
                    color = ExitColors.Accent
                )
                
                Text(
                    text = "%",
                    style = ExitTypography.Title,
                    color = ExitColors.SecondaryText,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            
            Text(
                text = "높음",
                style = ExitTypography.Body,
                color = ExitColors.Accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(ExitRadius.Full))
                    .background(ExitColors.Accent.copy(alpha = 0.15f))
                    .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.XS)
            )
        }
        
        // 코칭 메시지
        Text(
            text = "목표 달성 가능성이 높습니다. 현재 계획을 유지하세요",
            style = ExitTypography.Body,
            color = ExitColors.PrimaryText
        )
        
        // 도움말
        HelpBox(
            title = "이 확률이 의미하는 것",
            description = "30,000가지 다른 미래를 시뮬레이션해봤어요. 계획보다 10% 넘게 늦어지면 '실패'로 봤어요."
        )
    }
}

// MARK: - Demo Asset Path Chart

@Composable
private fun DemoAssetPathChart() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            DemoBadge()
        }
        
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
        
        // 커스텀 라인 차트
        AssetLineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        
        // 범례
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = ExitColors.Positive, label = "행운(상위10%)")
            LegendItem(color = ExitColors.Accent, label = "평균(50%)")
            LegendItem(color = ExitColors.Caution, label = "불행(하위10%)")
        }
        
        // 목표 달성 시점 비교
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
        ) {
            Text(
                text = "목표 자산 달성 시점",
                style = ExitTypography.Subheadline,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.PrimaryText
            )
            
            DemoTimelineChart()
        }
        
        // 도움말
        HelpBox(
            title = "이 그래프가 알려주는 것",
            description = "시장 상황에 따라 자산이 어떻게 변할지 3가지 시나리오로 보여줘요. 대부분의 경우가 이 범위 안에 들어요."
        )
    }
}

@Composable
private fun AssetLineChart(
    modifier: Modifier = Modifier
) {
    val positiveColor = ExitColors.Positive
    val accentColor = ExitColors.Accent
    val cautionColor = ExitColors.Caution
    val gridColor = ExitColors.Divider
    val textColor = ExitColors.TertiaryText
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        
        val maxValue = 48.0f // 48억
        val minValue = 0f
        
        // 그리드 라인
        val gridLines = listOf(0f, 12f, 24f, 36f, 48f)
        gridLines.forEach { value ->
            val y = padding + chartHeight * (1 - (value - minValue) / (maxValue - minValue))
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }
        
        // 데이터 포인트 정규화
        fun normalizeY(value: Double): Float {
            val normalized = ((value / 100_000_000).toFloat() - minValue) / (maxValue - minValue)
            return padding + chartHeight * (1 - normalized)
        }
        
        fun normalizeX(index: Int): Float {
            return padding + (index.toFloat() / (demoAssetData.best.size - 1)) * chartWidth
        }
        
        // 목표선 (6억)
        val targetY = normalizeY(600_000_000.0)
        drawLine(
            color = accentColor.copy(alpha = 0.3f),
            start = Offset(padding, targetY),
            end = Offset(width - padding, targetY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )
        
        // 불행 경로 (worst)
        val worstPath = Path().apply {
            demoAssetData.worst.forEachIndexed { index, value ->
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
        
        // 평균 경로 (median)
        val medianPath = Path().apply {
            demoAssetData.median.forEachIndexed { index, value ->
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
        
        // 행운 경로 (best)
        val bestPath = Path().apply {
            demoAssetData.best.forEachIndexed { index, value ->
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

@Composable
private fun DemoTimelineChart() {
    val timelineData = listOf(
        TimelineData("행운", 96, ExitColors.Positive, "🍀"),
        TimelineData("평균", 144, ExitColors.Accent, "📊"),
        TimelineData("불행", 192, ExitColors.Caution, "🌧️"),
        TimelineData("기존 예측", 120, ExitColors.TertiaryText, "📌")
    )
    
    val maxMonths = 192
    
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
                    Text(
                        text = item.icon,
                        fontSize = 12.sp
                    )
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
                    val fraction = item.months.toFloat() / maxMonths
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction.coerceAtLeast(0.15f))
                            .clip(RoundedCornerShape(4.dp))
                            .background(item.color.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = formatYears(item.months),
                            style = ExitTypography.Caption2,
                            fontWeight = FontWeight.SemiBold,
                            color = if (fraction > 0.3f) Color.White else item.color,
                            modifier = Modifier
                                .align(if (fraction > 0.3f) Alignment.CenterEnd else Alignment.CenterStart)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class TimelineData(
    val label: String,
    val months: Int,
    val color: Color,
    val icon: String
)

// MARK: - Demo Distribution Chart

@Composable
private fun DemoDistributionChart() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            DemoBadge()
        }
        
        // 타이틀
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
        
        // 핵심 수치
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Text(
                text = "12년차",
                style = ExitTypography.Title.copy(fontSize = 36.sp),
                fontWeight = FontWeight.Bold,
                color = ExitColors.Accent
            )
            
            Text(
                text = "에 달성할 가능성이 가장 높아요",
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // 커스텀 바 차트
        DistributionBarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )
        
        // 범위 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RangeIndicator(
                icon = Icons.Default.Schedule,
                label = "빠르면",
                value = "8년",
                color = ExitColors.Positive
            )
            RangeIndicator(
                icon = Icons.Default.GpsFixed,
                label = "대부분",
                value = "12년",
                color = ExitColors.Accent
            )
            RangeIndicator(
                icon = Icons.Default.Warning,
                label = "늦으면",
                value = "16년",
                color = ExitColors.Caution
            )
        }
        
        // 도움말
        HelpBox(
            title = "이 그래프가 알려주는 것",
            description = "막대가 높을수록 그 시점에 목표를 달성할 확률이 높아요. 대부분(80%)은 8~16년 사이에 달성해요."
        )
    }
}

@Composable
private fun DistributionBarChart(
    modifier: Modifier = Modifier
) {
    val accentColor = ExitColors.Accent
    val gridColor = ExitColors.Divider
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 30.dp.toPx()
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        
        val maxProb = 25f
        val barCount = demoDistributionData.size
        val barWidth = chartWidth / barCount * 0.6f
        val barSpacing = chartWidth / barCount
        
        // 그리드 라인
        listOf(0f, 10f, 20f).forEach { value ->
            val y = padding + chartHeight * (1 - value / maxProb)
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }
        
        // 바 그리기
        demoDistributionData.forEachIndexed { index, data ->
            val barHeight = ((data.probability / maxProb) * chartHeight).toFloat()
            val x = padding + index * barSpacing + (barSpacing - barWidth) / 2
            val y = padding + chartHeight - barHeight
            
            val color = if (data.year == 12) accentColor else accentColor.copy(alpha = 0.4f)
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
        }
    }
}

// MARK: - Demo Retirement Short Term Card

@Composable
private fun DemoRetirementShortTermCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            DemoBadge()
        }
        
        // 헤더
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = ExitColors.Accent
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
        
        // 기준 설명
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.MD))
                .background(ExitColors.SecondaryCardBackground)
                .padding(ExitSpacing.MD),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "은퇴 시점",
                    style = ExitTypography.Caption2,
                    color = ExitColors.SecondaryText
                )
                Text(
                    text = "6억",
                    style = ExitTypography.Body,
                    fontWeight = FontWeight.SemiBold,
                    color = ExitColors.Accent
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ExitColors.SecondaryText
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
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
        
        // 시나리오 카드
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
                modifier = Modifier.fillMaxWidth()
            ) {
                ScenarioCard(
                    title = "매우 행운",
                    amount = "9.2억",
                    change = "+53%",
                    color = ExitColors.Positive,
                    modifier = Modifier.weight(1f)
                )
                ScenarioCard(
                    title = "행운",
                    amount = "7.5억",
                    change = "+25%",
                    color = ExitColors.Accent,
                    modifier = Modifier.weight(1f)
                )
                ScenarioCard(
                    title = "평균",
                    amount = "5.8억",
                    change = "-3%",
                    color = ExitColors.PrimaryText,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
                modifier = Modifier.fillMaxWidth()
            ) {
                ScenarioCard(
                    title = "불행",
                    amount = "4.2억",
                    change = "-30%",
                    color = ExitColors.Caution,
                    modifier = Modifier.weight(1f)
                )
                ScenarioCard(
                    title = "매우 불행",
                    amount = "2.8억",
                    change = "-53%",
                    color = ExitColors.Warning,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // 도움말
        HelpBox(
            title = "왜 처음 10년이 중요할까요?",
            description = "은퇴 직후 시장이 하락하면 회복할 시간이 부족해요. 이를 '시퀀스 리스크'라고 해요."
        )
    }
}

@Composable
private fun ScenarioCard(
    title: String,
    amount: String,
    change: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(color.copy(alpha = 0.15f))
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
            text = amount,
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = change,
            style = ExitTypography.Caption2,
            color = if (change.startsWith("+")) ExitColors.Positive else ExitColors.Warning
        )
    }
}

// MARK: - Helper Components

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = ExitColors.Accent
        )
        
        Text(
            text = title,
            style = ExitTypography.Title3,
            fontWeight = FontWeight.Bold,
            color = ExitColors.PrimaryText
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
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
private fun HelpBox(
    title: String,
    description: String
) {
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
                text = title,
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Medium,
                color = ExitColors.SecondaryText
            )
            
            Text(
                text = description,
                style = ExitTypography.Caption2,
                color = ExitColors.TertiaryText
            )
        }
    }
}

private fun formatYears(months: Int): String {
    val years = months / 12
    val remainingMonths = months % 12
    
    return when {
        remainingMonths == 0 -> "${years}년"
        years == 0 -> "${remainingMonths}개월"
        else -> "${years}년 ${remainingMonths}개월"
    }
}

// MARK: - Demo Data

private data class DemoAssetDataSet(
    val best: List<Double>,
    val median: List<Double>,
    val worst: List<Double>
)

private val demoAssetData = DemoAssetDataSet(
    best = listOf(
        100_000_000.0, 180_000_000.0, 280_000_000.0, 420_000_000.0, 580_000_000.0,
        780_000_000.0, 1_020_000_000.0, 1_300_000_000.0, 1_650_000_000.0, 2_050_000_000.0,
        2_500_000_000.0, 3_000_000_000.0, 3_550_000_000.0, 4_150_000_000.0, 4_800_000_000.0
    ),
    median = listOf(
        100_000_000.0, 150_000_000.0, 210_000_000.0, 280_000_000.0, 360_000_000.0,
        450_000_000.0, 560_000_000.0, 680_000_000.0, 820_000_000.0, 980_000_000.0,
        1_160_000_000.0, 1_360_000_000.0, 1_580_000_000.0, 1_820_000_000.0, 2_100_000_000.0
    ),
    worst = listOf(
        100_000_000.0, 120_000_000.0, 140_000_000.0, 170_000_000.0, 210_000_000.0,
        260_000_000.0, 320_000_000.0, 390_000_000.0, 470_000_000.0, 560_000_000.0,
        670_000_000.0, 800_000_000.0, 950_000_000.0, 1_120_000_000.0, 1_320_000_000.0
    )
)

private data class DistributionData(
    val year: Int,
    val probability: Double
)

private val demoDistributionData = listOf(
    DistributionData(8, 4.5),
    DistributionData(9, 8.9),
    DistributionData(10, 14.2),
    DistributionData(11, 18.5),
    DistributionData(12, 21.0),
    DistributionData(13, 16.8),
    DistributionData(14, 9.8),
    DistributionData(15, 4.2),
    DistributionData(16, 1.5),
    DistributionData(17, 0.6)
)
