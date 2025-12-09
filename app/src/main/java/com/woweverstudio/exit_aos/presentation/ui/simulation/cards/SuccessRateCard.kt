package com.woweverstudio.exit_aos.presentation.ui.simulation.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woweverstudio.exit_aos.domain.model.UserProfile
import com.woweverstudio.exit_aos.domain.usecase.MonteCarloResult
import com.woweverstudio.exit_aos.presentation.ui.theme.*
import com.woweverstudio.exit_aos.util.ExitNumberFormatter

/**
 * 성공률 카드
 * iOS의 SuccessRateCard.swift와 동일
 */
@Composable
fun SuccessRateCard(
    result: MonteCarloResult,
    originalDDayMonths: Int,
    failureThresholdMultiplier: Double = 1.1,
    userProfile: UserProfile? = null,
    currentAssetAmount: Double = 0.0,
    effectiveVolatility: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val failureThresholdMonths = (originalDDayMonths * failureThresholdMultiplier).toInt()
    val confidenceColor = Color(android.graphics.Color.parseColor("#${result.confidenceLevel.colorHex}"))
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.XL))
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
        
        // 2. 큰 성공률 표시
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
                    text = "${(result.successRate * 100).toInt()}",
                    style = ExitTypography.LargeTitle.copy(fontSize = 72.sp),
                    fontWeight = FontWeight.Black,
                    color = confidenceColor
                )
                
                Text(
                    text = "%",
                    style = ExitTypography.Title,
                    color = ExitColors.SecondaryText,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            
            Text(
                text = result.confidenceLevel.displayName,
                style = ExitTypography.Body,
                color = confidenceColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(ExitRadius.Full))
                    .background(confidenceColor.copy(alpha = 0.15f))
                    .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.XS)
            )
        }
        
        // 코칭 메시지
        Text(
            text = getSuccessRateMessage(result.confidenceLevel),
            style = ExitTypography.Body,
            color = ExitColors.PrimaryText
        )
        
        // 3. 도움말
        HelpSection(
            result = result,
            originalDDayMonths = originalDDayMonths,
            failureThresholdMonths = failureThresholdMonths,
            failureThresholdMultiplier = failureThresholdMultiplier
        )
        
        // 4. 시뮬레이션 조건
        if (userProfile != null) {
            SimulationConditionSection(
                userProfile = userProfile,
                currentAssetAmount = currentAssetAmount,
                effectiveVolatility = effectiveVolatility
            )
        }
    }
}

@Composable
private fun HelpSection(
    result: MonteCarloResult,
    originalDDayMonths: Int,
    failureThresholdMonths: Int,
    failureThresholdMultiplier: Double
) {
    val originalDDayText = formatYearsMonths(originalDDayMonths)
    val failureThresholdText = formatYearsMonths(failureThresholdMonths)
    val failurePercent = ((failureThresholdMultiplier - 1) * 100).toInt()
    
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
                text = "이 확률이 의미하는 것",
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Medium,
                color = ExitColors.SecondaryText
            )
            
            Text(
                text = "주식 시장은 매년 오르락내리락해요. 그래서 ${result.totalSimulations}가지 다른 미래를 시뮬레이션해봤어요.",
                style = ExitTypography.Caption2,
                color = ExitColors.TertiaryText
            )
            
            Text(
                text = "현재 계획대로면 $originalDDayText 후에 FIRE를 달성해요. 여기서는 계획보다 ${failurePercent}% 넘게 늦어지면($failureThresholdText) '실패'로 봤어요.",
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
            DataItem(label = "월 투자", value = ExitNumberFormatter.formatToManWon(userProfile.monthlyInvestment))
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

private fun getSuccessRateMessage(confidenceLevel: MonteCarloResult.ConfidenceLevel): String {
    return when (confidenceLevel) {
        MonteCarloResult.ConfidenceLevel.VERY_HIGH -> "현재 계획대로라면 목표 달성이 거의 확실합니다! 훌륭해요 🎉"
        MonteCarloResult.ConfidenceLevel.HIGH -> "목표 달성 가능성이 높습니다. 현재 계획을 유지하세요"
        MonteCarloResult.ConfidenceLevel.MODERATE -> "계획대로 진행하면 달성 가능합니다. 입금을 조금 더 늘리면 더 안전해요"
        MonteCarloResult.ConfidenceLevel.LOW -> "목표 달성이 불확실합니다. 월 저축액을 늘리거나 목표를 조정하세요"
        MonteCarloResult.ConfidenceLevel.VERY_LOW -> "현재 계획으로는 목표 달성이 어렵습니다. 계획을 재검토하세요"
    }
}

private fun formatYearsMonths(months: Int): String {
    val years = months / 12
    val remainingMonths = months % 12
    return when {
        remainingMonths == 0 -> "${years}년"
        years == 0 -> "${remainingMonths}개월"
        else -> "${years}년 ${remainingMonths}개월"
    }
}

