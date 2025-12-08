package com.woweverstudio.exit_aos.domain.usecase

import com.woweverstudio.exit_aos.domain.model.UserProfile
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 자산 변화 경로 (단일 시뮬레이션)
 */
data class AssetPath(
    /** 월별 자산 값 */
    val monthlyAssets: List<Double>,
    
    /** 목표 달성 개월 수 (실패 시 null) */
    val monthsToTarget: Int?
) {
    /** 성공 여부 */
    val isSuccess: Boolean
        get() = monthsToTarget != null
}

/**
 * 대표 경로들 (시각화용)
 */
data class RepresentativePaths(
    /** 최선의 경우 (10 percentile) */
    val best: AssetPath,
    
    /** 중앙값 (50 percentile) */
    val median: AssetPath,
    
    /** 최악의 경우 (90 percentile) */
    val worst: AssetPath
)

/**
 * 몬테카를로 시뮬레이션 결과
 */
data class MonteCarloResult(
    /** 성공률 (0.0 ~ 1.0) */
    val successRate: Double,
    
    /** 성공한 시뮬레이션들의 도달 개월 수 분포 */
    val successMonthsDistribution: List<Int>,
    
    /** 실패한 시뮬레이션 수 */
    val failureCount: Int,
    
    /** 전체 시뮬레이션 횟수 */
    val totalSimulations: Int,
    
    /** 대표 자산 경로 (시각화용) */
    val representativePaths: RepresentativePaths?
) {
    /** 성공 횟수 */
    val successCount: Int
        get() = successMonthsDistribution.size
    
    /** 성공 시 평균 도달 개월 수 */
    val averageMonthsToSuccess: Double
        get() {
            if (successMonthsDistribution.isEmpty()) return 0.0
            return successMonthsDistribution.sum().toDouble() / successMonthsDistribution.size
        }
    
    /** 중앙값 (50% 확률) */
    val medianMonths: Int
        get() {
            if (successMonthsDistribution.isEmpty()) return 0
            val sorted = successMonthsDistribution.sorted()
            return sorted[sorted.size / 2]
        }
    
    /** 10% 최악의 경우 (90 percentile) */
    val worstCase10Percent: Int
        get() {
            if (successMonthsDistribution.isEmpty()) return 0
            val sorted = successMonthsDistribution.sorted()
            val index = (sorted.size * 0.9).toInt()
            return sorted[min(index, sorted.size - 1)]
        }
    
    /** 10% 최선의 경우 (10 percentile) */
    val bestCase10Percent: Int
        get() {
            if (successMonthsDistribution.isEmpty()) return 0
            val sorted = successMonthsDistribution.sorted()
            val index = (sorted.size * 0.1).toInt()
            return sorted[index]
        }
    
    /** 성공 여부 (50% 이상) */
    val isLikelyToSucceed: Boolean
        get() = successRate >= 0.5
    
    /** 신뢰도 등급 */
    val confidenceLevel: ConfidenceLevel
        get() = when {
            successRate >= 0.95 -> ConfidenceLevel.VERY_HIGH
            successRate >= 0.85 -> ConfidenceLevel.HIGH
            successRate >= 0.70 -> ConfidenceLevel.MODERATE
            successRate >= 0.50 -> ConfidenceLevel.LOW
            else -> ConfidenceLevel.VERY_LOW
        }
    
    /**
     * 연도별 분포 (히스토그램용)
     * @return [연도: 해당 연도에 도달한 시뮬레이션 수]
     */
    fun yearDistribution(): Map<Int, Int> {
        val distribution = mutableMapOf<Int, Int>()
        
        for (months in successMonthsDistribution) {
            val years = months / 12
            distribution[years] = distribution.getOrDefault(years, 0) + 1
        }
        
        return distribution
    }
    
    enum class ConfidenceLevel(val displayName: String, val colorHex: String) {
        VERY_HIGH("매우 높음", "00D4AA"),
        HIGH("높음", "00D4AA"),
        MODERATE("보통", "FFD60A"),
        LOW("낮음", "FF9500"),
        VERY_LOW("매우 낮음", "FF3B30")
    }
}

/**
 * 진행률 콜백 타입
 */
typealias ProgressCallback = (completed: Int, successMonths: List<Int>, paths: List<AssetPath>) -> Unit

/**
 * 몬테카를로 시뮬레이터 (순수 비즈니스 로직)
 */
object MonteCarloSimulator {
    
    /**
     * 몬테카를로 시뮬레이션 실행
     * 
     * @param initialAsset 초기 자산 (원)
     * @param monthlyInvestment 월 투자 금액 (원)
     * @param targetAsset 목표 자산 (원)
     * @param meanReturn 평균 연 수익률 (%, 예: 6.5)
     * @param volatility 수익률 표준편차 (%, 예: 15)
     * @param simulationCount 시뮬레이션 횟수 (기본 30,000)
     * @param maxMonths 최대 개월 수 (기본 1200개월 = 100년)
     * @param trackPaths 자산 경로 추적 여부 (차트용)
     * @param progressCallback 진행률 콜백 (completed, successMonths, paths)
     * @return 시뮬레이션 결과
     */
    fun simulate(
        initialAsset: Double,
        monthlyInvestment: Double,
        targetAsset: Double,
        meanReturn: Double,
        volatility: Double,
        simulationCount: Int = 30_000,
        maxMonths: Int = 1200,
        trackPaths: Boolean = true,
        progressCallback: ProgressCallback? = null
    ): MonteCarloResult {
        val successMonths = mutableListOf<Int>()
        val allPaths = mutableListOf<AssetPath>()
        var failureCount = 0
        
        // 업데이트 간격 (200번마다 한 번씩 콜백)
        val updateInterval = 200
        
        // 시뮬레이션 실행
        for (i in 0 until simulationCount) {
            val (months, path) = runSingleSimulation(
                initialAsset = initialAsset,
                monthlyInvestment = monthlyInvestment,
                targetAsset = targetAsset,
                meanReturn = meanReturn,
                volatility = volatility,
                maxMonths = maxMonths,
                trackPath = trackPaths
            )
            
            if (months != null) {
                successMonths.add(months)
            } else {
                failureCount++
            }
            
            if (trackPaths && path != null) {
                allPaths.add(path)
            }
            
            // 진행률 콜백
            if ((i + 1) % updateInterval == 0 || i == simulationCount - 1) {
                progressCallback?.invoke(i + 1, successMonths.toList(), allPaths.toList())
            }
        }
        
        val successRate = successMonths.size.toDouble() / simulationCount
        
        // 대표 경로 추출
        val representativePaths = if (trackPaths) {
            extractRepresentativePaths(allPaths, successMonths)
        } else null
        
        return MonteCarloResult(
            successRate = successRate,
            successMonthsDistribution = successMonths,
            failureCount = failureCount,
            totalSimulations = simulationCount,
            representativePaths = representativePaths
        )
    }
    
    // MARK: - Single Simulation
    
    /**
     * 단일 시뮬레이션 실행
     * @return (목표 달성 개월 수, 자산 경로)
     */
    private fun runSingleSimulation(
        initialAsset: Double,
        monthlyInvestment: Double,
        targetAsset: Double,
        meanReturn: Double,
        volatility: Double,
        maxMonths: Int,
        trackPath: Boolean
    ): Pair<Int?, AssetPath?> {
        var currentAsset = initialAsset
        var months = 0
        val assetHistory = if (trackPath) mutableListOf(initialAsset) else mutableListOf()
        
        // 월 평균 수익률과 표준편차 계산
        val annualMean = meanReturn / 100.0
        val annualVolatility = volatility / 100.0
        
        // 로그 정규분포를 위한 파라미터 변환
        val monthlyMean = ln(1 + annualMean) / 12.0
        val monthlyVolatility = annualVolatility / sqrt(12.0)
        
        while (currentAsset < targetAsset && months < maxMonths) {
            // 1. 월초 투자금 추가
            currentAsset += monthlyInvestment
            
            // 2. 정규분포에서 수익률 샘플링 (Box-Muller 변환)
            val monthlyReturn = sampleNormalDistribution(
                mean = monthlyMean,
                standardDeviation = monthlyVolatility
            )
            
            // 3. 수익 적용 (로그 정규분포)
            currentAsset *= exp(monthlyReturn)
            
            // 자산이 0 이하로 떨어져도 월 저축으로 회복 가능하므로 최소 0으로 유지
            if (currentAsset < 0) {
                currentAsset = 0.0
            }
            
            months++
            
            // 경로 추적
            if (trackPath) {
                assetHistory.add(currentAsset)
            }
        }
        
        // 목표 달성 여부 확인
        val success = currentAsset >= targetAsset
        val finalMonths = if (success) months else null
        
        val path = if (trackPath) {
            AssetPath(
                monthlyAssets = assetHistory,
                monthsToTarget = finalMonths
            )
        } else null
        
        return Pair(finalMonths, path)
    }
    
    // MARK: - Random Sampling
    
    /**
     * 정규분포에서 샘플링 (Box-Muller 변환 사용)
     * 
     * @param mean 평균
     * @param standardDeviation 표준편차
     * @return 샘플링된 값
     */
    private fun sampleNormalDistribution(
        mean: Double,
        standardDeviation: Double
    ): Double {
        // Box-Muller 변환
        val u1 = Random.nextDouble()
        val u2 = Random.nextDouble()
        
        // 표준정규분포 샘플
        val z0 = sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
        
        // 평균과 표준편차 적용
        return mean + z0 * standardDeviation
    }
    
    // MARK: - Representative Paths
    
    /**
     * 대표 경로 추출
     */
    fun extractRepresentativePaths(
        paths: List<AssetPath>,
        successMonths: List<Int>
    ): RepresentativePaths? {
        if (successMonths.isEmpty()) return null
        
        val sorted = successMonths.sorted()
        val bestIndex = (sorted.size * 0.1).toInt()
        val medianIndex = sorted.size / 2
        val worstIndex = (sorted.size * 0.9).toInt()
        
        val bestMonths = sorted[bestIndex]
        val medianMonths = sorted[medianIndex]
        val worstMonths = sorted[min(worstIndex, sorted.size - 1)]
        
        // 해당 개월수와 가장 가까운 경로 찾기
        val bestPath = paths.find { it.monthsToTarget == bestMonths } ?: paths[bestIndex]
        val medianPath = paths.find { it.monthsToTarget == medianMonths } ?: paths[medianIndex]
        val worstPath = paths.find { it.monthsToTarget == worstMonths } ?: paths[min(worstIndex, paths.size - 1)]
        
        return RepresentativePaths(
            best = bestPath,
            median = medianPath,
            worst = worstPath
        )
    }
    
    // MARK: - UserProfile-based Simulation
    
    /**
     * UserProfile 기반 시뮬레이션 (편의 메서드)
     * 
     * @param profile 사용자 프로필
     * @param currentAsset 현재 자산
     * @param volatility 변동성 (기본값 15%)
     * @param simulationCount 시뮬레이션 횟수
     * @param trackPaths 자산 경로 추적 여부
     * @param progressCallback 진행률 콜백
     * @return 시뮬레이션 결과
     */
    fun simulate(
        profile: UserProfile,
        currentAsset: Double,
        volatility: Double = 15.0,
        simulationCount: Int = 30_000,
        trackPaths: Boolean = true,
        progressCallback: ProgressCallback? = null
    ): MonteCarloResult {
        // 목표 자산 계산
        val targetAsset = RetirementCalculator.calculateTargetAssets(
            desiredMonthlyIncome = profile.desiredMonthlyIncome,
            postRetirementReturnRate = profile.postRetirementReturnRate,
            inflationRate = profile.inflationRate
        )
        
        return simulate(
            initialAsset = currentAsset,
            monthlyInvestment = profile.monthlyInvestment,
            targetAsset = targetAsset,
            meanReturn = profile.preRetirementReturnRate,
            volatility = volatility,
            simulationCount = simulationCount,
            trackPaths = trackPaths,
            progressCallback = progressCallback
        )
    }
}

