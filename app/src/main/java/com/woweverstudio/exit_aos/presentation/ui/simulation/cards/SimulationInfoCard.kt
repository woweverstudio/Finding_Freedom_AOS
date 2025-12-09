package com.woweverstudio.exit_aos.presentation.ui.simulation.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.domain.model.UserProfile
import com.woweverstudio.exit_aos.domain.usecase.MonteCarloResult
import com.woweverstudio.exit_aos.domain.usecase.RetirementCalculator
import com.woweverstudio.exit_aos.presentation.ui.theme.*
import com.woweverstudio.exit_aos.presentation.viewmodel.SimulationViewModel
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import java.text.NumberFormat
import java.util.Locale

/**
 * 시뮬레이션 정보 카드
 * iOS의 SimulationInfoCard.swift와 동일
 */
@Composable
fun SimulationInfoCard(
    userProfile: UserProfile,
    currentAssetAmount: Double,
    effectiveVolatility: Double,
    result: MonteCarloResult,
    modifier: Modifier = Modifier
) {
    val targetAsset = RetirementCalculator.calculateTargetAssets(
        desiredMonthlyIncome = userProfile.desiredMonthlyIncome,
        postRetirementReturnRate = userProfile.postRetirementReturnRate,
        inflationRate = userProfile.inflationRate
    )
    
    val preRetirementVolatility = SimulationViewModel.calculateVolatility(userProfile.preRetirementReturnRate)
    val postRetirementVolatility = SimulationViewModel.calculateVolatility(userProfile.postRetirementReturnRate)
    
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.SecondaryCardBackground)
            .padding(ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        // 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = ExitColors.Accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "시뮬레이션 정보",
                style = ExitTypography.Subheadline,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.PrimaryText
            )
        }
        
        // 기본 정보
        InfoSection(title = "기본 정보") {
            InfoRow(label = "현재 자산", value = ExitNumberFormatter.formatToEokManWon(currentAssetAmount))
            InfoRow(label = "월 저축액", value = ExitNumberFormatter.formatToManWon(userProfile.monthlyInvestment))
            InfoRow(label = "희망 월수입", value = ExitNumberFormatter.formatToManWon(userProfile.desiredMonthlyIncome))
            InfoRow(label = "목표 자산", value = ExitNumberFormatter.formatToEokManWon(targetAsset), valueColor = ExitColors.Accent)
        }
        
        // 은퇴 전 시뮬레이션
        InfoSection(title = "은퇴 전 시뮬레이션") {
            InfoRow(label = "목표 수익률", value = String.format("%.1f%%", userProfile.preRetirementReturnRate))
            InfoRow(label = "수익률 변동성", value = String.format("%.1f%%", preRetirementVolatility), valueColor = ExitColors.SecondaryText)
        }
        
        // 은퇴 후 시뮬레이션
        InfoSection(title = "은퇴 후 시뮬레이션") {
            InfoRow(label = "목표 수익률", value = String.format("%.1f%%", userProfile.postRetirementReturnRate))
            InfoRow(label = "수익률 변동성", value = String.format("%.1f%%", postRetirementVolatility), valueColor = ExitColors.SecondaryText)
            InfoRow(label = "물가 상승률", value = String.format("%.1f%%", userProfile.inflationRate))
        }
        
        // 시뮬레이션 결과
        InfoSection(title = "시뮬레이션 결과") {
            InfoRow(label = "시뮬레이션 횟수", value = "${numberFormat.format(result.totalSimulations)}회")
            InfoRow(label = "성공", value = "${numberFormat.format(result.successCount)}회", valueColor = ExitColors.Positive)
            InfoRow(label = "실패", value = "${numberFormat.format(result.failureCount)}회", valueColor = ExitColors.Warning)
            InfoRow(label = "성공률", value = String.format("%.1f%%", result.successRate * 100), valueColor = ExitColors.Accent)
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Text(
            text = title,
            style = ExitTypography.Caption2,
            fontWeight = FontWeight.Medium,
            color = ExitColors.TertiaryText
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.SM))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.SM),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
            content = content
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = ExitColors.PrimaryText
) {
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
            text = value,
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

