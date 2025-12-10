package com.woweverstudio.exit_aos.presentation.ui.simulation

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.woweverstudio.exit_aos.data.billing.BillingService
import com.woweverstudio.exit_aos.data.billing.BillingState
import com.woweverstudio.exit_aos.domain.usecase.MonteCarloResult
import com.woweverstudio.exit_aos.domain.usecase.RetirementCalculator
import com.woweverstudio.exit_aos.presentation.ui.simulation.cards.SimulationInfoCard
import com.woweverstudio.exit_aos.presentation.ui.simulation.cards.SuccessRateCard
import com.woweverstudio.exit_aos.presentation.ui.simulation.charts.AssetPathChart
import com.woweverstudio.exit_aos.presentation.ui.simulation.charts.DistributionChart
import com.woweverstudio.exit_aos.presentation.ui.simulation.charts.RetirementProjectionChart
import com.woweverstudio.exit_aos.presentation.ui.simulation.charts.RetirementShortTermChart
import com.woweverstudio.exit_aos.presentation.ui.theme.*
import com.woweverstudio.exit_aos.presentation.viewmodel.SimulationScreenState
import com.woweverstudio.exit_aos.presentation.viewmodel.SimulationViewModel
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import kotlinx.coroutines.launch

/**
 * 시뮬레이션 화면
 * iOS의 SimulationView.swift와 동일
 */
@Composable
fun SimulationScreen(
    viewModel: SimulationViewModel = hiltViewModel(),
    billingService: BillingService,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val monteCarloResult by viewModel.monteCarloResult.collectAsState()
    val retirementResult by viewModel.retirementResult.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()
    val simulationProgress by viewModel.simulationProgress.collectAsState()
    val simulationPhase by viewModel.simulationPhase.collectAsState()
    val currentAssetAmount by viewModel.currentAssetAmount.collectAsState()
    
    // Billing state
    val billingState by billingService.billingState.collectAsState()
    val isMontecarloUnlocked by billingService.isMontecarloUnlocked.collectAsState()
    val errorMessage by billingService.errorMessage.collectAsState()
    
    // 화면 상태 관리 (ViewModel에서 관리하여 탭 전환 시에도 유지됨)
    val currentScreen by viewModel.currentScreenState.collectAsState()
    
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // 구매 완료 시 설정 화면으로 이동 (iOS의 onChange와 동일)
    LaunchedEffect(isMontecarloUnlocked) {
        if (isMontecarloUnlocked && currentScreen == SimulationScreenState.Empty) {
            viewModel.navigateToSetup()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ExitColors.Background)
    ) {
        when {
            userProfile == null -> {
                // 데이터 로딩 중
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ExitColors.Accent)
                }
            }
            
            else -> {
                // 화면 상태에 따른 뷰 전환 (iOS와 동일한 애니메이션)
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState == SimulationScreenState.Setup) {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        } else if (targetState == SimulationScreenState.Empty && initialState == SimulationScreenState.Setup) {
                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                        } else if (targetState == SimulationScreenState.Results) {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        } else {
                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                        }
                    },
                    label = "SimulationScreenTransition"
                ) { screen ->
                    when (screen) {
                        SimulationScreenState.Empty -> {
                            // Empty Screen (iOS의 emptyScreenView와 동일)
                            SimulationEmptyView(
                                userProfile = userProfile,
                                currentAssetAmount = currentAssetAmount,
                                onStart = {
                                    // 이미 구입한 경우 설정 화면으로
                                    if (isMontecarloUnlocked) {
                                        viewModel.navigateToSetup()
                                    }
                                    // 미구입인 경우 EmptyView에서 구입 처리
                                },
                                isPurchased = isMontecarloUnlocked,
                                displayPrice = billingService.displayPrice,
                                errorMessage = errorMessage,
                                isPurchasing = billingState is BillingState.Purchasing,
                                onPurchase = {
                                    activity?.let { act ->
                                        scope.launch {
                                            billingService.purchaseMontecarloSimulation(act)
                                        }
                                    }
                                },
                                onRestore = {
                                    scope.launch {
                                        billingService.restorePurchases()
                                    }
                                }
                            )
                        }
                        
                        SimulationScreenState.Setup -> {
                            // Setup Screen (iOS의 SimulationSetupView와 동일)
                            SimulationSetupView(
                                viewModel = viewModel,
                                userProfile = userProfile,
                                currentAssetAmount = currentAssetAmount,
                                onBack = {
                                    // 결과가 있으면 결과로, 없으면 empty로 (iOS와 동일)
                                    viewModel.navigateBack()
                                },
                                onStart = {
                                    viewModel.navigateToResults()
                                }
                            )
                        }
                        
                        SimulationScreenState.Results -> {
                            // Results Screen (iOS의 resultsScreenView와 동일)
                            when {
                                isSimulating -> {
                                    SimulationLoadingView(
                                        progress = simulationProgress,
                                        phaseDescription = simulationPhase.description
                                    )
                                }
                                monteCarloResult != null -> {
                                    SimulationResultsView(
                                        viewModel = viewModel,
                                        result = monteCarloResult!!,
                                        onRestart = {
                                            // 다시 시뮬레이션 → setup 화면으로 (iOS와 동일)
                                            viewModel.navigateToSetup()
                                        }
                                    )
                                }
                                else -> {
                                    // 결과가 없으면 로딩 화면
                                    SimulationLoadingView(
                                        progress = 0f,
                                        phaseDescription = "준비 중..."
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 시뮬레이션 로딩 뷰
 */
@Composable
private fun SimulationLoadingView(
    progress: Float,
    phaseDescription: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ExitSpacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 아이콘
        Icon(
            imageVector = Icons.Default.ShowChart,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = ExitColors.Accent
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.XL))
        
        // 제목
        Text(
            text = "시뮬레이션 진행 중",
            style = ExitTypography.Title2,
            fontWeight = FontWeight.Bold,
            color = ExitColors.PrimaryText
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.SM))
        
        // 시뮬레이션 단계
        Text(
            text = phaseDescription,
            style = ExitTypography.Body,
            color = ExitColors.SecondaryText
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        // 진행률 바
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ExitColors.Divider)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ExitColors.Accent)
                )
            }
            
            Text(
                text = "${(progress * 100).toInt()}%",
                style = ExitTypography.Title3,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.Accent
            )
        }
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        // 설명
        Text(
            text = "30,000가지 미래를 시뮬레이션하고 있습니다",
            style = ExitTypography.Caption,
            color = ExitColors.SecondaryText
        )
    }
}

/**
 * 시뮬레이션 결과 뷰
 * iOS의 resultsView와 동일 - 은퇴 전/후 레이아웃 구분
 */
@Composable
private fun SimulationResultsView(
    viewModel: SimulationViewModel,
    result: MonteCarloResult,
    onRestart: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currentAssetAmount by viewModel.currentAssetAmount.collectAsState()
    val retirementResult by viewModel.retirementResult.collectAsState()
    
    val isAlreadyRetired = viewModel.originalDDayMonths == 0
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        if (isAlreadyRetired) {
            // 이미 은퇴 가능한 경우: 은퇴 후 시뮬레이션만 표시 (iOS와 동일)
            RetirementReadyHeader(
                userProfile = userProfile,
                currentAssetAmount = currentAssetAmount
            )
            
            // 은퇴 후 단기(1~10년) 자산 변화
            retirementResult?.let { retirement ->
                userProfile?.let { profile ->
                    RetirementShortTermChart(
                        result = retirement,
                        userProfile = profile,
                        spendingRatio = viewModel.spendingRatio.collectAsState().value
                    )
                }
            }
            
            // 은퇴 후 장기(40년) 자산 변화 예측
            retirementResult?.let { retirement ->
                userProfile?.let { profile ->
                    RetirementProjectionChart(
                        result = retirement,
                        userProfile = profile,
                        spendingRatio = viewModel.spendingRatio.collectAsState().value
                    )
                }
            }
            
            // 시뮬레이션 정보 카드
            userProfile?.let { profile ->
                SimulationInfoCard(
                    userProfile = profile,
                    currentAssetAmount = currentAssetAmount,
                    effectiveVolatility = viewModel.effectiveVolatility,
                    result = result
                )
            }
        } else {
            // 아직 은퇴 전: 전체 시뮬레이션 표시 (iOS와 동일)
            
            // 1. 성공률 카드
            SuccessRateCard(
                result = result,
                originalDDayMonths = viewModel.originalDDayMonths,
                failureThresholdMultiplier = viewModel.failureThresholdMultiplier.collectAsState().value,
                userProfile = userProfile,
                currentAssetAmount = currentAssetAmount,
                effectiveVolatility = viewModel.effectiveVolatility
            )
            
            // 2. 자산 변화 예측 차트 + FIRE 달성 시점 비교
            viewModel.representativePaths?.let { paths ->
                userProfile?.let { profile ->
                    AssetPathChart(
                        paths = paths,
                        userProfile = profile,
                        result = result,
                        originalDDayMonths = viewModel.originalDDayMonths,
                        currentAssetAmount = currentAssetAmount,
                        effectiveVolatility = viewModel.effectiveVolatility
                    )
                }
            }
            
            // 3. 목표 달성 시점 분포 차트
            DistributionChart(
                yearDistributionData = viewModel.yearDistributionData,
                result = result,
                userProfile = userProfile,
                currentAssetAmount = currentAssetAmount,
                targetAssetAmount = viewModel.targetAsset,
                effectiveVolatility = viewModel.effectiveVolatility
            )
            
            // 4. 은퇴 후 단기(1~10년) 자산 변화
            retirementResult?.let { retirement ->
                userProfile?.let { profile ->
                    RetirementShortTermChart(
                        result = retirement,
                        userProfile = profile,
                        spendingRatio = viewModel.spendingRatio.collectAsState().value
                    )
                }
            }
            
            // 5. 은퇴 후 장기(40년) 자산 변화 예측
            retirementResult?.let { retirement ->
                userProfile?.let { profile ->
                    RetirementProjectionChart(
                        result = retirement,
                        userProfile = profile,
                        spendingRatio = viewModel.spendingRatio.collectAsState().value
                    )
                }
            }
            
            // 6. 시뮬레이션 정보 카드
            userProfile?.let { profile ->
                SimulationInfoCard(
                    userProfile = profile,
                    currentAssetAmount = currentAssetAmount,
                    effectiveVolatility = viewModel.effectiveVolatility,
                    result = result
                )
            }
        }
        
        // 다시 시뮬레이션 버튼 (iOS의 actionButtons와 동일)
        ActionButtons(onRestart = onRestart)
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
    }
}

/**
 * 이미 은퇴 가능한 경우 헤더
 * iOS의 retirementReadyHeader와 동일
 */
@Composable
private fun RetirementReadyHeader(
    userProfile: com.woweverstudio.exit_aos.domain.model.UserProfile?,
    currentAssetAmount: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .clip(RoundedCornerShape(ExitRadius.XL))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ExitColors.SecondaryCardBackground,
                        ExitColors.CardBackground
                    )
                )
            )
            .padding(ExitSpacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        Text(
            text = "🎉",
            fontSize = 50.sp
        )
        
        Text(
            text = "이미 은퇴 가능합니다!",
            style = ExitTypography.Title2,
            fontWeight = FontWeight.Bold,
            color = ExitColors.Accent
        )
        
        if (userProfile != null) {
            val requiredRate = RetirementCalculator.calculateRequiredReturnRate(
                currentAssets = currentAssetAmount,
                desiredMonthlyIncome = userProfile.desiredMonthlyIncome
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
            ) {
                Text(
                    text = "매월 ${ExitNumberFormatter.formatToManWon(userProfile.desiredMonthlyIncome)} 현금흐름을 위해",
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "연",
                        style = ExitTypography.Body,
                        color = ExitColors.SecondaryText
                    )
                    Text(
                        text = String.format("%.2f%%", requiredRate),
                        style = ExitTypography.Title3,
                        fontWeight = FontWeight.Bold,
                        color = if (requiredRate < 4) ExitColors.Positive else ExitColors.Accent
                    )
                    Text(
                        text = "수익률만 달성하면 됩니다",
                        style = ExitTypography.Body,
                        color = ExitColors.SecondaryText
                    )
                }
            }
        }
        
        Text(
            text = "아래는 은퇴 후 자산 변화 시뮬레이션입니다",
            style = ExitTypography.Caption,
            color = ExitColors.TertiaryText,
            modifier = Modifier.padding(top = ExitSpacing.SM)
        )
    }
}

/**
 * 액션 버튼들
 * iOS의 actionButtons와 동일
 */
@Composable
private fun ActionButtons(
    onRestart: () -> Unit
) {
    Button(
        onClick = onRestart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(ExitRadius.LG)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF00D4AA),
                            Color(0xFF00B894)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Text(
                    text = "다시 시뮬레이션",
                    style = ExitTypography.Body,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
