package com.woweverstudio.exit_aos.presentation.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.presentation.ui.components.AmountEditSheet
import com.woweverstudio.exit_aos.presentation.ui.components.AmountEditType
import com.woweverstudio.exit_aos.presentation.ui.components.PlanHeaderView
import com.woweverstudio.exit_aos.presentation.ui.components.ProgressRingView
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitGradients
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitRadius
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import com.woweverstudio.exit_aos.presentation.viewmodel.AppStateViewModel
import com.woweverstudio.exit_aos.presentation.viewmodel.MainTab
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import com.woweverstudio.exit_aos.util.rememberHaptic

/**
 * 대시보드 화면
 */
@Composable
fun DashboardScreen(
    viewModel: AppStateViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currentAsset by viewModel.currentAsset.collectAsState()
    val retirementResult by viewModel.retirementResult.collectAsState()
    val hideAmounts by viewModel.hideAmounts.collectAsState()
    
    // 햅틱 피드백
    val haptic = rememberHaptic()
    
    // PlanHeader 펼침 상태
    var isHeaderExpanded by rememberSaveable { mutableStateOf(false) }
    
    // 금액 편집 시트 상태 (편집 요청)
    var amountEditState by remember { mutableStateOf<Pair<AmountEditType, Double>?>(null) }
    // 금액 편집 결과 (PlanHeaderView로 전달)
    var amountEditResult by remember { mutableStateOf<Pair<AmountEditType, Double>?>(null) }
    
    // 스크롤 상태
    val listState = rememberLazyListState()
    
    // 헤더가 닫힐 때 스크롤을 맨 위로 (iOS: proxy.scrollTo("container", anchor: .top))
    LaunchedEffect(isHeaderExpanded) {
        if (!isHeaderExpanded) {
            listState.animateScrollToItem(0)
        }
    }
    
    // Pull-to-expand/close: 제스처로 헤더 펼치기/접기
    var accumulatedPullDown by remember { mutableStateOf(0f) }
    var accumulatedPullUp by remember { mutableStateOf(0f) }
    val pullThreshold = 150f // 당기는 임계값 (더 많이 당겨야 동작)
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isAtTop = listState.firstVisibleItemIndex == 0 && 
                              listState.firstVisibleItemScrollOffset == 0
                
                // 아래로 당기기 (available.y > 0) - 헤더 펼치기
                if (isAtTop && available.y > 0 && !isHeaderExpanded) {
                    accumulatedPullDown += available.y
                    accumulatedPullUp = 0f
                    
                    if (accumulatedPullDown > pullThreshold) {
                        isHeaderExpanded = true
                        accumulatedPullDown = 0f
                    }
                    return Offset(0f, available.y)
                }
                
                // 위로 스와이프 (available.y < 0) - 헤더 접기
                if (isAtTop && available.y < 0 && isHeaderExpanded) {
                    accumulatedPullUp += -available.y
                    accumulatedPullDown = 0f
                    
                    if (accumulatedPullUp > pullThreshold) {
                        isHeaderExpanded = false
                        accumulatedPullUp = 0f
                    }
                    return Offset(0f, available.y)
                }
                
                // 다른 상황에서는 누적값 리셋
                if (available.y > 0) {
                    accumulatedPullUp = 0f
                } else if (available.y < 0) {
                    accumulatedPullDown = 0f
                }
                
                return Offset.Zero
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(ExitColors.Background)
        ) {
            // 상단 플로팅 헤더 (스크롤과 무관하게 고정)
            PlanHeaderView(
                userProfile = userProfile,
                currentAssetAmount = currentAsset?.amount ?: 0.0,
                hideAmounts = hideAmounts,
                isExpanded = isHeaderExpanded,
                onExpandedChange = { isHeaderExpanded = it },
                onApplyChanges = { asset, income, investment, preRate, postRate ->
                    viewModel.updateCurrentAsset(asset)
                    viewModel.updateSettings(
                        desiredMonthlyIncome = income,
                        monthlyInvestment = investment,
                        preRetirementReturnRate = preRate,
                        postRetirementReturnRate = postRate
                    )
                },
                onAmountEditRequest = { type, value ->
                    amountEditState = type to value
                },
                amountEditResult = amountEditResult,
                onAmountEditResultConsumed = { amountEditResult = null }
            )
            
            // 스크롤 컨텐츠 (iOS: VStack spacing = LG, padding.vertical = LG)
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = ExitSpacing.LG),
                verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                // D-Day 헤더
                item {
                    DDayHeader(
                        retirementResult = retirementResult,
                        onExpandHeader = {
                            haptic.light()
                            isHeaderExpanded = true
                        },
                        modifier = Modifier.padding(horizontal = ExitSpacing.MD)
                    )
                }
                
                // 진행률 섹션
                item {
                    ProgressSection(
                        viewModel = viewModel,
                        retirementResult = retirementResult,
                        userProfile = userProfile,
                        hideAmounts = hideAmounts,
                        onToggleHideAmounts = {
                            haptic.light()
                            viewModel.toggleHideAmounts()
                        },
                        onExpandHeader = {
                            haptic.light()
                            isHeaderExpanded = true
                        }
                    )
                }
                
                // 자산 성장 차트 (은퇴 전 사용자만)
                item {
                    retirementResult?.let { result ->
                        userProfile?.let { profile ->
                            if (!result.isRetirementReady) {
                                AssetGrowthChart(
                                    currentAsset = result.currentAssets,
                                    targetAsset = result.targetAssets,
                                    monthlyInvestment = profile.monthlyInvestment,
                                    preRetirementReturnRate = profile.preRetirementReturnRate,
                                    monthsToRetirement = result.monthsToRetirement,
                                    modifier = Modifier.padding(horizontal = ExitSpacing.MD)
                                )
                            }
                        }
                    }
                }
                
                // 시뮬레이션 유도 버튼
                item {
                    SimulationPromptButton(
                        onClick = {
                            haptic.medium()
                            viewModel.selectTab(MainTab.SIMULATION)
                        },
                        modifier = Modifier.padding(horizontal = ExitSpacing.MD)
                    )
                }
            }
        }
        
        // 금액 편집 ModalBottomSheet
        amountEditState?.let { (type, initialValue) ->
            AmountEditSheet(
                type = type,
                initialValue = initialValue,
                onConfirm = { newValue ->
                    // PlanHeaderView의 편집 값만 업데이트 (실제 계산은 적용 시)
                    amountEditResult = type to newValue
                    amountEditState = null
                },
                onDismiss = { amountEditState = null }
            )
        }
    }
}

@Composable
private fun DDayHeader(
    retirementResult: com.woweverstudio.exit_aos.domain.usecase.RetirementCalculationResult?,
    onExpandHeader: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.XL))
            .background(ExitGradients.Card)
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.LG)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (retirementResult != null) {
                if (retirementResult.isRetirementReady) {
                    // 은퇴 가능 상태
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
                    ) {
                        Text(text = "🎉", style = ExitTypography.Title)
                        Text(
                            text = "은퇴 가능합니다!",
                            style = ExitTypography.Title2,
                            color = ExitColors.Accent
                        )
                        
                        retirementResult.requiredReturnRate?.let { rate ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
                            ) {
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
                        }
                    }
                } else {
                    // D-Day 표시 (iOS: VStack spacing = SM = 8pt)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
                    ) {
                        Text(
                            text = "회사 탈출까지",
                            style = ExitTypography.Body,
                            color = ExitColors.SecondaryText
                        )
                        
                        Text(
                            text = retirementResult.dDayString,
                            style = ExitTypography.Title2,
                            fontWeight = FontWeight.ExtraBold,
                            color = ExitColors.Accent
                        )
                        
                        Text(
                            text = "남았습니다.",
                            style = ExitTypography.Body,
                            color = ExitColors.SecondaryText
                        )
                    }
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
    hideAmounts: Boolean,
    onToggleHideAmounts: () -> Unit,
    onExpandHeader: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (retirementResult != null) {
            // 진행률 링
            ProgressRingView(
                progress = viewModel.progressValue.toFloat(),
                currentAmount = ExitNumberFormatter.formatToEokManWon(retirementResult.currentAssets),
                targetAmount = ExitNumberFormatter.formatToEokManWon(retirementResult.targetAssets),
                percentText = ExitNumberFormatter.formatPercentInt(retirementResult.progressPercent),
                hideAmounts = hideAmounts
            )
            
            Spacer(modifier = Modifier.height(ExitSpacing.SM))
            
            // 금액 숨김 토글 (우측 끝에 배치)
            Box(modifier = Modifier.fillMaxWidth()) {
                AmountVisibilityToggle(
                    hideAmounts = hideAmounts,
                    onClick = onToggleHideAmounts,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
            
            Spacer(modifier = Modifier.height(ExitSpacing.MD))
            
            // 상세 계산 카드
            DetailedCalculationCard(
                retirementResult = retirementResult,
                userProfile = userProfile,
                hideAmounts = hideAmounts,
                onExpandHeader = onExpandHeader
            )
        }
    }
}

@Composable
private fun AmountVisibilityToggle(
    hideAmounts: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ExitRadius.Full))
            .background(ExitColors.CardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() }
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.SM),
        contentAlignment = Alignment.Center
    ) {
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
    hideAmounts: Boolean,
    onExpandHeader: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG)
    ) {
        // 현재 자산 / 목표 자산 (5%)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (hideAmounts) "•••" else ExitNumberFormatter.formatToEokManWon(retirementResult.currentAssets),
                style = ExitTypography.Body,
                color = ExitColors.Accent
            )
            Text(
                text = " / ",
                style = ExitTypography.Body,
                color = ExitColors.TertiaryText
            )
            Text(
                text = if (hideAmounts) "•••" else ExitNumberFormatter.formatToEokManWon(retirementResult.targetAssets),
                style = ExitTypography.Body,
                color = ExitColors.SecondaryText
            )
            Text(
                text = " (${ExitNumberFormatter.formatPercentInt(retirementResult.progressPercent)})",
                style = ExitTypography.Body,
                color = ExitColors.Accent
            )
        }
        
        Divider(
            modifier = Modifier.padding(vertical = ExitSpacing.MD),
            color = ExitColors.Divider
        )
        
        // 상세 설명
        if (userProfile != null) {
            if (retirementResult.isRetirementReady && retirementResult.requiredReturnRate != null) {
                // 은퇴 가능: 필요 수익률 역산 결과 표시
                val requiredRate = retirementResult.requiredReturnRate
                Column(verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                                append("현재 자산 ")
                            }
                            withStyle(SpanStyle(color = ExitColors.Accent, fontWeight = FontWeight.SemiBold)) {
                                append(if (hideAmounts) "•••" else ExitNumberFormatter.formatToEokManWon(retirementResult.currentAssets))
                            }
                            withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                                append("으로")
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
                                append(ExitNumberFormatter.formatToManWon(userProfile.desiredMonthlyIncome))
                            }
                            withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                                append(" 현금흐름을 만들려면")
                            }
                        },
                        style = ExitTypography.Subheadline
                    )
                    
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                                append("연 ")
                            }
                            withStyle(SpanStyle(
                                color = if (requiredRate < 4) ExitColors.Positive else ExitColors.Accent,
                                fontWeight = FontWeight.Bold
                            )) {
                                append(String.format("%.2f%%", requiredRate))
                            }
                            withStyle(SpanStyle(color = ExitColors.SecondaryText)) {
                                append(" 수익률만 달성하면 됩니다")
                            }
                        },
                        style = ExitTypography.Subheadline
                    )
                    
                    // 수익률 수준 코멘트
                    RequiredRateComment(requiredRate)
                }
            } else if (!retirementResult.isRetirementReady) {
                // 은퇴 준비 중: 기존 로직
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
}

@Composable
private fun RequiredRateComment(requiredRate: Double) {
    val (message, color) = when {
        requiredRate < 3 -> "매우 안정적인 수익률입니다 (예금/채권 수준)" to ExitColors.Positive
        requiredRate < 5 -> "안정적인 수익률입니다 (배당주/채권 수준)" to ExitColors.Positive
        requiredRate < 7 -> "합리적인 수익률입니다 (인덱스펀드 수준)" to ExitColors.Accent
        else -> "다소 높은 수익률이 필요합니다" to ExitColors.Caution
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(ExitRadius.SM)
            )
            .padding(horizontal = ExitSpacing.SM, vertical = ExitSpacing.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "💡",
            style = ExitTypography.Caption
        )
        Spacer(modifier = Modifier.width(ExitSpacing.XS))
        Text(
            text = message,
            style = ExitTypography.Caption,
            color = color
        )
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
            .border(
                width = 1.dp,
                color = ExitColors.Caution,
                shape = RoundedCornerShape(ExitRadius.LG)
            )
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
