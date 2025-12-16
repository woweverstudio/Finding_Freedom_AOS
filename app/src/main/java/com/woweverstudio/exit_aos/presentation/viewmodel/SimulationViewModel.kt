package com.woweverstudio.exit_aos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woweverstudio.exit_aos.data.repository.ExitRepository
import com.woweverstudio.exit_aos.domain.model.Asset
import com.woweverstudio.exit_aos.domain.model.UserProfile
import com.woweverstudio.exit_aos.domain.usecase.MonteCarloResult
import com.woweverstudio.exit_aos.domain.usecase.MonteCarloSimulator
import com.woweverstudio.exit_aos.domain.usecase.RetirementCalculator
import com.woweverstudio.exit_aos.domain.usecase.RetirementSimulationResult
import com.woweverstudio.exit_aos.domain.usecase.RetirementSimulator
import com.woweverstudio.exit_aos.presentation.ui.simulation.charts.YearDistributionData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 시뮬레이션 진행 단계
 */
enum class SimulationPhase(val description: String) {
    IDLE(""),
    PRE_RETIREMENT("목표 달성까지 시뮬레이션 중..."),
    POST_RETIREMENT("은퇴 후 시뮬레이션 중...")
}

/**
 * 시뮬레이션 화면 상태
 * iOS의 SimulationScreen enum과 동일
 */
enum class SimulationScreenState {
    Empty,   // 미구입 또는 초기 화면
    Setup,   // 설정 화면
    Results  // 결과 화면
}

/**
 * 퍼센타일 포인트 데이터
 */
data class PercentilePoint(
    val label: String,
    val months: Int,
    val percentile: Int
) {
    val years: Double
        get() = months / 12.0
    
    val displayText: String
        get() {
            val years = months / 12
            val remainingMonths = months % 12
            return if (remainingMonths == 0) {
                "${years}년"
            } else {
                "${years}년 ${remainingMonths}개월"
            }
        }
}

/**
 * 시뮬레이션 ViewModel
 */
@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val repository: ExitRepository
) : ViewModel() {
    
    // MARK: - State
    
    /** 현재 자산 */
    private val _currentAsset = MutableStateFlow<Asset?>(null)
    val currentAsset: StateFlow<Asset?> = _currentAsset.asStateFlow()
    
    /** 사용자 프로필 */
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    /** 몬테카를로 시뮬레이션 결과 */
    private val _monteCarloResult = MutableStateFlow<MonteCarloResult?>(null)
    val monteCarloResult: StateFlow<MonteCarloResult?> = _monteCarloResult.asStateFlow()
    
    /** 은퇴 후 시뮬레이션 결과 */
    private val _retirementResult = MutableStateFlow<RetirementSimulationResult?>(null)
    val retirementResult: StateFlow<RetirementSimulationResult?> = _retirementResult.asStateFlow()
    
    /** 시뮬레이션 진행 중 여부 */
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()
    
    /** 시뮬레이션 진행률 (0.0 ~ 1.0) */
    private val _simulationProgress = MutableStateFlow(0f)
    val simulationProgress: StateFlow<Float> = _simulationProgress.asStateFlow()
    
    /** 현재 시뮬레이션 단계 */
    private val _simulationPhase = MutableStateFlow(SimulationPhase.IDLE)
    val simulationPhase: StateFlow<SimulationPhase> = _simulationPhase.asStateFlow()
    
    /** 시뮬레이션 횟수 */
    private val _simulationCount = MutableStateFlow(30_000)
    val simulationCount: StateFlow<Int> = _simulationCount.asStateFlow()
    
    /** 커스텀 변동성 */
    private val _customVolatility = MutableStateFlow<Double?>(null)
    val customVolatility: StateFlow<Double?> = _customVolatility.asStateFlow()
    
    /** 실패 조건 배수 */
    private val _failureThresholdMultiplier = MutableStateFlow(1.1)
    val failureThresholdMultiplier: StateFlow<Double> = _failureThresholdMultiplier.asStateFlow()
    
    /** 생활비 사용 비율 (기본값 1.0 = 100%) */
    private val _spendingRatio = MutableStateFlow(1.0)
    val spendingRatio: StateFlow<Double> = _spendingRatio.asStateFlow()
    
    /** 현재 자산 금액 (StateFlow) */
    private val _currentAssetAmount = MutableStateFlow(0.0)
    val currentAssetAmount: StateFlow<Double> = _currentAssetAmount.asStateFlow()
    
    /** 현재 화면 상태 (탭 전환 시에도 유지됨) */
    private val _currentScreenState = MutableStateFlow(SimulationScreenState.Empty)
    val currentScreenState: StateFlow<SimulationScreenState> = _currentScreenState.asStateFlow()
    
    // MARK: - Computed Properties
    
    val currentAssetAmountValue: Double
        get() = _currentAsset.value?.amount ?: 0.0
    
    val effectiveVolatility: Double
        get() = _customVolatility.value ?: 15.0
    
    val originalDDayMonths: Int
        get() {
            val profile = _userProfile.value ?: return 0
            val result = RetirementCalculator.calculate(profile, currentAssetAmountValue)
            return result.monthsToRetirement
        }
    
    val failureThresholdMonths: Int
        get() = (originalDDayMonths * _failureThresholdMultiplier.value).toInt()
    
    val targetAsset: Double
        get() {
            val profile = _userProfile.value ?: return 0.0
            return RetirementCalculator.calculateTargetAssets(
                desiredMonthlyIncome = profile.desiredMonthlyIncome,
                postRetirementReturnRate = profile.postRetirementReturnRate
            )
        }
    
    val percentileData: List<PercentilePoint>
        get() {
            val result = _monteCarloResult.value ?: return emptyList()
            return listOf(
                PercentilePoint("행운 10%", result.bestCase10Percent, 10),
                PercentilePoint("평균", result.averageMonthsToSuccess.toInt(), 50),
                PercentilePoint("중앙값", result.medianMonths, 50),
                PercentilePoint("불행 10%", result.worstCase10Percent, 90)
            )
        }
    
    /** 연도별 분포 데이터 (히스토그램용) */
    val yearDistributionData: List<YearDistributionData>
        get() {
            val result = _monteCarloResult.value ?: return emptyList()
            val distribution = result.yearDistribution()
            return distribution.entries
                .sortedBy { it.key }
                .map { YearDistributionData(it.key, it.value) }
        }
    
    /** 대표 경로 */
    val representativePaths: com.woweverstudio.exit_aos.domain.usecase.RepresentativePaths?
        get() = _monteCarloResult.value?.representativePaths
    
    // MARK: - Settings Change Detection (iOS의 planSettingsChangeTrigger와 동일한 역할)
    
    /** 마지막 시뮬레이션에 사용된 설정의 해시값 */
    private var lastSimulationSettingsHash: Int = 0
    
    /** 현재 설정의 해시값 계산 */
    private fun calculateSettingsHash(): Int {
        val asset = _currentAsset.value?.amount ?: 0.0
        val profile = _userProfile.value
        return listOf(
            asset.hashCode(),
            profile?.desiredMonthlyIncome?.hashCode() ?: 0,
            profile?.monthlyInvestment?.hashCode() ?: 0,
            profile?.preRetirementReturnRate?.hashCode() ?: 0,
            profile?.postRetirementReturnRate?.hashCode() ?: 0
        ).fold(0) { acc, hash -> acc * 31 + hash }
    }
    
    /** 설정 변경 감지 및 결과 초기화 (iOS의 onChange(planSettingsChangeTrigger)와 동일) */
    private fun checkAndResetIfSettingsChanged() {
        val currentHash = calculateSettingsHash()
        
        // 시뮬레이션 결과가 있고, 설정이 변경되었으면 리셋
        if (_monteCarloResult.value != null && 
            lastSimulationSettingsHash != 0 && 
            currentHash != lastSimulationSettingsHash &&
            !_isSimulating.value) {
            
            resetSimulationResults()
        }
    }
    
    // MARK: - Initialization
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            launch {
                repository.getAsset().collect { asset ->
                    _currentAsset.value = asset
                    _currentAssetAmount.value = asset?.amount ?: 0.0
                    // 설정 변경 감지
                    checkAndResetIfSettingsChanged()
                }
            }
            
            launch {
                repository.getUserProfile().collect { profile ->
                    _userProfile.value = profile
                    // 설정 변경 감지
                    checkAndResetIfSettingsChanged()
                }
            }
        }
    }
    
    // MARK: - Simulation
    
    /**
     * 시뮬레이션 실행 (설정값 직접 전달 가능)
     * SimulationSetupView에서 편집한 값을 바로 사용하기 위해 파라미터로 받음
     */
    fun runAllSimulations(
        overrideCurrentAsset: Double? = null,
        overrideMonthlyInvestment: Double? = null,
        overrideDesiredMonthlyIncome: Double? = null,
        overridePreReturnRate: Double? = null,
        overridePostReturnRate: Double? = null,
        overrideSpendingRatio: Double? = null
    ) {
        val profile = _userProfile.value ?: return
        
        viewModelScope.launch {
            try {
                _isSimulating.value = true
                _simulationProgress.value = 0f
                _simulationPhase.value = SimulationPhase.PRE_RETIREMENT
                
                // override 값이 있으면 사용, 없으면 기존 값 사용
                val currentAsset = overrideCurrentAsset ?: currentAssetAmountValue
                val monthlyInvestment = overrideMonthlyInvestment ?: profile.monthlyInvestment
                val desiredMonthlyIncome = overrideDesiredMonthlyIncome ?: profile.desiredMonthlyIncome
                val preReturnRate = overridePreReturnRate ?: profile.preRetirementReturnRate
                val postReturnRate = overridePostReturnRate ?: profile.postRetirementReturnRate
                val spendingRatio = overrideSpendingRatio ?: _spendingRatio.value
                
                // 실제 월 지출액 = 희망 월수입 × 생활비 사용 비율
                val actualMonthlySpending = desiredMonthlyIncome * spendingRatio
                
                val simCount = _simulationCount.value
                val preRetirementVolatility = effectiveVolatility
                val postRetirementVolatility = calculateVolatility(postReturnRate)
                
                val targetAsset = RetirementCalculator.calculateTargetAssets(
                    desiredMonthlyIncome = desiredMonthlyIncome,
                    postRetirementReturnRate = postReturnRate
                )
                
                val originalDDay = RetirementCalculator.calculateMonthsToRetirement(
                    currentAssets = currentAsset,
                    targetAssets = targetAsset,
                    monthlyInvestment = monthlyInvestment,
                    annualReturnRate = preReturnRate
                )
                
                // 이미 은퇴 가능한 경우 (originalDDay == 0)
                val isAlreadyRetired = originalDDay == 0 || currentAsset >= targetAsset
                
                // maxMonths가 0이면 최소 12개월로 설정 (0으로 나누기 방지)
                val maxMonthsForSimulation = if (isAlreadyRetired) {
                    12 // 최소값
                } else {
                    (originalDDay * _failureThresholdMultiplier.value).toInt().coerceAtLeast(12)
                }
                
                // Phase 1: 목표 달성까지 시뮬레이션 (이미 은퇴 가능하면 건너뜀)
                val monteCarloResult = if (isAlreadyRetired) {
                    // 이미 은퇴 가능: 100% 성공으로 더미 결과 생성
                    MonteCarloResult(
                        successRate = 1.0,
                        successMonthsDistribution = listOf(0),
                        failureCount = 0,
                        totalSimulations = simCount,
                        representativePaths = null
                    )
                } else {
                    MonteCarloSimulator.simulate(
                        initialAsset = currentAsset,
                        monthlyInvestment = monthlyInvestment,
                        targetAsset = targetAsset,
                        meanReturn = preReturnRate,
                        volatility = preRetirementVolatility,
                        simulationCount = simCount,
                        maxMonths = maxMonthsForSimulation,
                        trackPaths = true
                    ) { completed, _, _ ->
                        _simulationProgress.value = completed.toFloat() / simCount * 0.5f
                    }
                }
                
                _monteCarloResult.value = monteCarloResult
                _simulationPhase.value = SimulationPhase.POST_RETIREMENT
                
                // Phase 2: 은퇴 후 시뮬레이션
                val retirementStartAsset = if (isAlreadyRetired) currentAsset else targetAsset
                
                val retirementResult = RetirementSimulator.simulate(
                    initialAsset = retirementStartAsset,
                    monthlySpending = actualMonthlySpending,  // 생활비 사용 비율 적용
                    annualReturn = postReturnRate,
                    volatility = postRetirementVolatility,
                    simulationCount = simCount
                ) { completed ->
                    _simulationProgress.value = 0.5f + completed.toFloat() / simCount * 0.5f
                }
                
                _retirementResult.value = retirementResult
                _simulationPhase.value = SimulationPhase.IDLE
                
                // 시뮬레이션 완료 시 현재 설정의 해시값 저장 (설정 변경 감지용)
                lastSimulationSettingsHash = calculateSettingsHash()
            } catch (e: Exception) {
                // 에러 로깅 및 안전한 종료
                e.printStackTrace()
                _simulationPhase.value = SimulationPhase.IDLE
            } finally {
                _isSimulating.value = false
            }
        }
    }
    
    fun refreshSimulation() {
        runAllSimulations()
    }
    
    fun updateSimulationCount(count: Int) {
        _simulationCount.value = count
    }
    
    fun updateVolatility(volatility: Double) {
        _customVolatility.value = volatility
    }
    
    fun resetVolatility() {
        _customVolatility.value = null
    }
    
    fun updateFailureThreshold(multiplier: Double) {
        _failureThresholdMultiplier.value = multiplier
    }
    
    fun resetFailureThreshold() {
        _failureThresholdMultiplier.value = 1.1
    }
    
    fun updateSpendingRatio(ratio: Double) {
        _spendingRatio.value = ratio
    }
    
    fun resetSpendingRatio() {
        _spendingRatio.value = 1.0
    }
    
    // MARK: - Screen Navigation
    
    /**
     * Setup 화면으로 이동
     */
    fun navigateToSetup() {
        _currentScreenState.value = SimulationScreenState.Setup
    }
    
    /**
     * Results 화면으로 이동
     */
    fun navigateToResults() {
        _currentScreenState.value = SimulationScreenState.Results
    }
    
    /**
     * Empty 화면으로 이동
     */
    fun navigateToEmpty() {
        _currentScreenState.value = SimulationScreenState.Empty
    }
    
    /**
     * 뒤로 가기 (결과가 있으면 Results, 없으면 Empty)
     */
    fun navigateBack() {
        _currentScreenState.value = if (_monteCarloResult.value != null) {
            SimulationScreenState.Results
        } else {
            SimulationScreenState.Empty
        }
    }
    
    /**
     * 시뮬레이션 결과 초기화 및 Setup 화면으로 이동 (Plan 설정 변경 시 호출)
     */
    fun resetSimulationResults() {
        _monteCarloResult.value = null
        _retirementResult.value = null
        _simulationProgress.value = 0f
        _simulationPhase.value = SimulationPhase.IDLE
        
        // 결과 화면에 있었다면 Setup 화면으로 이동
        if (_currentScreenState.value == SimulationScreenState.Results) {
            _currentScreenState.value = SimulationScreenState.Setup
        }
    }
    
    // MARK: - Settings & Asset Update (DB 저장)
    
    /**
     * 현재 자산 업데이트 (DB 저장)
     */
    fun updateCurrentAsset(amount: Double) {
        viewModelScope.launch {
            val existingAsset = _currentAsset.value
            
            if (existingAsset != null) {
                val updatedAsset = existingAsset.update(amount)
                repository.updateAsset(updatedAsset)
            } else {
                val newAsset = Asset(amount = amount)
                repository.saveAsset(newAsset)
            }
            
            // StateFlow 즉시 업데이트
            _currentAssetAmount.value = amount
        }
    }
    
    /**
     * 사용자 설정 업데이트 (DB 저장)
     * DashboardScreen의 PlanHeaderView와 동기화됨
     */
    fun updateSettings(
        currentAsset: Double? = null,
        desiredMonthlyIncome: Double? = null,
        monthlyInvestment: Double? = null,
        preRetirementReturnRate: Double? = null,
        postRetirementReturnRate: Double? = null
    ) {
        viewModelScope.launch {
            // 현재 자산 업데이트
            currentAsset?.let { updateCurrentAsset(it) }
            
            // 프로필 업데이트
            val profile = _userProfile.value ?: return@launch
            
            val updatedProfile = profile.updateSettings(
                desiredMonthlyIncome = desiredMonthlyIncome,
                monthlyInvestment = monthlyInvestment,
                preRetirementReturnRate = preRetirementReturnRate,
                postRetirementReturnRate = postRetirementReturnRate
            )
            
            repository.updateUserProfile(updatedProfile)
        }
    }
    
    // MARK: - Volatility Calculation
    
    companion object {
        /**
         * 목표 수익률 기반 변동성 자동 계산
         */
        fun calculateVolatility(returnRate: Double): Double {
            return when {
                returnRate < 2.5 -> 1.0
                returnRate < 4 -> 4.5
                returnRate < 6 -> 7.0
                returnRate < 7 -> 11.0
                returnRate < 8 -> 13.0
                returnRate < 9 -> 15.0
                returnRate < 10 -> 17.0
                returnRate < 12 -> 21.0
                returnRate < 15 -> 27.0
                returnRate < 20 -> 30.0
                else -> 35.0
            }
        }
    }
}

