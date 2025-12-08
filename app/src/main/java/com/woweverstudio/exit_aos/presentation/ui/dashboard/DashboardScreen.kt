package com.woweverstudio.exit_aos.presentation.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.presentation.ui.components.ProgressRingView
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitGradients
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitRadius
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import com.woweverstudio.exit_aos.presentation.viewmodel.AppStateViewModel
import com.woweverstudio.exit_aos.presentation.viewmodel.MainTab
import com.woweverstudio.exit_aos.util.ExitNumberFormatter

/**
 * 대시보드 화면
 */
@Composable
fun DashboardScreen(
    viewModel: AppStateViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val retirementResult by viewModel.retirementResult.collectAsState()
    val hideAmounts by viewModel.hideAmounts.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ExitColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = ExitSpacing.LG)
    ) {
        // D-Day 헤더
        DDayHeader(
            retirementResult = retirementResult,
            modifier = Modifier.padding(horizontal = ExitSpacing.MD)
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        // 진행률 섹션
        ProgressSection(
            viewModel = viewModel,
            retirementResult = retirementResult,
            userProfile = userProfile,
            hideAmounts = hideAmounts
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        // 시뮬레이션 유도 버튼
        SimulationPromptButton(
            onClick = { viewModel.selectTab(MainTab.SIMULATION) },
            modifier = Modifier.padding(horizontal = ExitSpacing.MD)
        )
    }
}

@Composable
private fun DDayHeader(
    retirementResult: com.woweverstudio.exit_aos.domain.usecase.RetirementCalculationResult?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.XL))
            .background(ExitGradients.Card)
            .padding(ExitSpacing.LG)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (retirementResult != null) {
                if (retirementResult.isRetirementReady) {
                    // 은퇴 가능 상태
                    Text(text = "🎉", style = ExitTypography.LargeTitle)
                    Spacer(modifier = Modifier.height(ExitSpacing.MD))
                    Text(
                        text = "은퇴 가능합니다!",
                        style = ExitTypography.Title2,
                        color = ExitColors.Accent
                    )
                    
                    retirementResult.requiredReturnRate?.let { rate ->
                        Spacer(modifier = Modifier.height(ExitSpacing.MD))
                        Text(
                            text = "필요 수익률",
                            style = ExitTypography.Caption,
                            color = ExitColors.SecondaryText
                        )
                        Text(
                            text = String.format("연 %.2f%%", rate),
                            style = ExitTypography.Title3,
                            color = if (rate < 4) ExitColors.Positive else ExitColors.Accent
                        )
                    }
                } else {
                    Text(
                        text = "회사 탈출까지",
                        style = ExitTypography.Body,
                        color = ExitColors.SecondaryText
                    )
                    
                    Spacer(modifier = Modifier.height(ExitSpacing.SM))
                    
                    // D-Day 표시
                    Text(
                        text = retirementResult.dDayString,
                        style = ExitTypography.LargeTitle,
                        color = ExitColors.Accent
                    )
                    
                    Spacer(modifier = Modifier.height(ExitSpacing.SM))
                    
                    Text(
                        text = "남았습니다.",
                        style = ExitTypography.Body,
                        color = ExitColors.SecondaryText
                    )
                }
            } else {
                Text(
                    text = "계산 중...",
                    style = ExitTypography.Title2,
                    color = ExitColors.SecondaryText
                )
            }
        }
    }
}

@Composable
private fun ProgressSection(
    viewModel: AppStateViewModel,
    retirementResult: com.woweverstudio.exit_aos.domain.usecase.RetirementCalculationResult?,
    userProfile: com.woweverstudio.exit_aos.domain.model.UserProfile?,
    hideAmounts: Boolean
) {
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (retirementResult != null) {
            // 진행률 링
            Box(contentAlignment = Alignment.BottomEnd) {
                ProgressRingView(
                    progress = viewModel.progressValue.toFloat(),
                    currentAmount = ExitNumberFormatter.formatToEokManWon(retirementResult.currentAssets),
                    targetAmount = ExitNumberFormatter.formatToEokManWon(retirementResult.targetAssets),
                    percentText = ExitNumberFormatter.formatPercentInt(retirementResult.progressPercent),
                    hideAmounts = hideAmounts
                )
                
                // 금액 숨김 토글
                AmountVisibilityToggle(
                    hideAmounts = hideAmounts,
                    onClick = { viewModel.toggleHideAmounts() }
                )
            }
            
            Spacer(modifier = Modifier.height(ExitSpacing.LG))
            
            // 상세 계산 카드
            DetailedCalculationCard(
                retirementResult = retirementResult,
                userProfile = userProfile,
                hideAmounts = hideAmounts
            )
        }
    }
}

@Composable
private fun AmountVisibilityToggle(
    hideAmounts: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(ExitRadius.Full))
            .background(ExitColors.CardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() }
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.SM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        Icon(
            imageVector = if (hideAmounts) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = null,
            tint = if (hideAmounts) ExitColors.Accent else ExitColors.TertiaryText,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = if (hideAmounts) "금액 보기" else "금액 숨김",
            style = ExitTypography.Caption2,
            color = if (hideAmounts) ExitColors.Accent else ExitColors.TertiaryText
        )
    }
}

@Composable
private fun DetailedCalculationCard(
    retirementResult: com.woweverstudio.exit_aos.domain.usecase.RetirementCalculationResult,
    userProfile: com.woweverstudio.exit_aos.domain.model.UserProfile?,
    hideAmounts: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG)
    ) {
        // 현재 자산 / 목표 자산
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = ExitNumberFormatter.formatToEokManWon(retirementResult.currentAssets),
                style = ExitTypography.Body,
                color = ExitColors.Accent,
                modifier = if (hideAmounts) Modifier.blur(5.dp) else Modifier
            )
            Text(
                text = "/",
                style = ExitTypography.Body,
                color = ExitColors.TertiaryText
            )
            Text(
                text = ExitNumberFormatter.formatToEokManWon(retirementResult.targetAssets),
                style = ExitTypography.Body,
                color = ExitColors.SecondaryText,
                modifier = if (hideAmounts) Modifier.blur(5.dp) else Modifier
            )
            Text(
                text = "(${ExitNumberFormatter.formatPercentInt(retirementResult.progressPercent)})",
                style = ExitTypography.Body,
                color = ExitColors.Accent
            )
        }
        
        Divider(
            modifier = Modifier.padding(vertical = ExitSpacing.MD),
            color = ExitColors.Divider
        )
        
        // 상세 설명
        if (userProfile != null && !retirementResult.isRetirementReady) {
            Column(verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                            append("매월 ")
                        }
                        withStyle(SpanStyle(color = ExitColors.Accent, fontWeight = FontWeight.SemiBold)) {
                            append(ExitNumberFormatter.formatToManWon(userProfile.desiredMonthlyIncome))
                        }
                        withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                            append("의 현금흐름을 만들기 위해")
                        }
                    },
                    style = ExitTypography.Subheadline
                )
                
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                            append("매월 ")
                        }
                        withStyle(SpanStyle(color = ExitColors.Accent, fontWeight = FontWeight.SemiBold)) {
                            append(ExitNumberFormatter.formatToManWon(userProfile.monthlyInvestment))
                        }
                        withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                            append("씩 연복리 ")
                        }
                        withStyle(SpanStyle(color = ExitColors.Accent, fontWeight = FontWeight.SemiBold)) {
                            append(String.format("%.1f%%", userProfile.preRetirementReturnRate))
                        }
                        withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                            append("로 투자하면")
                        }
                    },
                    style = ExitTypography.Subheadline
                )
                
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = ExitColors.Accent, fontWeight = FontWeight.Bold)) {
                            append(retirementResult.dDayString)
                        }
                        withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                            append(" 남았습니다.")
                        }
                    },
                    style = ExitTypography.Subheadline
                )
            }
        }
    }
}

@Composable
private fun SimulationPromptButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Caution)
            ) { onClick() }
            .padding(ExitSpacing.MD)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎲 내 은퇴 성공 확률은 몇 %?",
                    style = ExitTypography.Subheadline,
                    fontWeight = FontWeight.SemiBold,
                    color = ExitColors.PrimaryText
                )
                Text(
                    text = "30,000가지 미래로 더 자세히 분석해드려요",
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ExitColors.Caution,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

