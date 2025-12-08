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
    
    // MARK: - Computed Properties
    
    val currentAssetAmount: Double
        get() = _currentAsset.value?.amount ?: 0.0
    
    val effectiveVolatility: Double
        get() = _customVolatility.value ?: 15.0
    
    val originalDDayMonths: Int
        get() {
            val profile = _userProfile.value ?: return 0
            val result = RetirementCalculator.calculate(profile, currentAssetAmount)
            return result.monthsToRetirement
        }
    
    val failureThresholdMonths: Int
        get() = (originalDDayMonths * _failureThresholdMultiplier.value).toInt()
    
    val targetAsset: Double
        get() {
            val profile = _userProfile.value ?: return 0.0
            return RetirementCalculator.calculateTargetAssets(
                desiredMonthlyIncome = profile.desiredMonthlyIncome,
                postRetirementReturnRate = profile.postRetirementReturnRate,
                inflationRate = profile.inflationRate
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
    
    // MARK: - Initialization
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            launch {
                repository.getAsset().collect { asset ->
                    _currentAsset.value = asset
                }
            }
            
            launch {
                repository.getUserProfile().collect { profile ->
                    _userProfile.value = profile
                }
            }
        }
    }
    
    // MARK: - Simulation
    
    fun runAllSimulations() {
        val profile = _userProfile.value ?: return
        
        viewModelScope.launch {
            _isSimulating.value = true
            _simulationProgress.value = 0f
            _simulationPhase.value = SimulationPhase.PRE_RETIREMENT
            
            val currentAsset = currentAssetAmount
            val simCount = _simulationCount.value
            val preRetirementVolatility = effectiveVolatility
            val postRetirementVolatility = calculateVolatility(profile.postRetirementReturnRate)
            
            val targetAsset = RetirementCalculator.calculateTargetAssets(
                desiredMonthlyIncome = profile.desiredMonthlyIncome,
                postRetirementReturnRate = profile.postRetirementReturnRate,
                inflationRate = profile.inflationRate
            )
            
            val originalDDay = RetirementCalculator.calculateMonthsToRetirement(
                currentAssets = currentAsset,
                targetAssets = targetAsset,
                monthlyInvestment = profile.monthlyInvestment,
                annualReturnRate = profile.preRetirementReturnRate
            )
            
            val maxMonthsForSimulation = (originalDDay * _failureThresholdMultiplier.value).toInt()
            
            // Phase 1: 목표 달성까지 시뮬레이션
            val monteCarloResult = withContext(Dispatchers.Default) {
                MonteCarloSimulator.simulate(
                    initialAsset = currentAsset,
                    monthlyInvestment = profile.monthlyInvestment,
                    targetAsset = targetAsset,
                    meanReturn = profile.preRetirementReturnRate,
                    volatility = preRetirementVolatility,
                    simulationCount = simCount,
                    maxMonths = maxMonthsForSimulation,
                    trackPaths = true
                ) { completed, _, _ ->
                    val progress = completed.toFloat() / simCount * 0.5f
                    _simulationProgress.value = progress
                }
            }
            
            _monteCarloResult.value = monteCarloResult
            _simulationPhase.value = SimulationPhase.POST_RETIREMENT
            
            // Phase 2: 은퇴 후 시뮬레이션
            val retirementStartAsset = if (originalDDay == 0) currentAsset else targetAsset
            
            val retirementResult = withContext(Dispatchers.Default) {
                RetirementSimulator.simulate(
                    initialAsset = retirementStartAsset,
                    monthlySpending = profile.desiredMonthlyIncome,
                    annualReturn = profile.postRetirementReturnRate,
                    volatility = postRetirementVolatility,
                    simulationCount = simCount
                ) { completed ->
                    val progress = 0.5f + completed.toFloat() / simCount * 0.5f
                    _simulationProgress.value = progress
                }
            }
            
            _retirementResult.value = retirementResult
            _simulationPhase.value = SimulationPhase.IDLE
            _isSimulating.value = false
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

