package com.woweverstudio.exit_aos.domain.usecase

import com.woweverstudio.exit_aos.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
 * 청크 결과 (병렬 처리용)
 */
private data class ChunkResult(
    val successMonths: List<Int>,
    val paths: List<AssetPath>,
    val failureCount: Int
)

/**
 * 몬테카를로 시뮬레이터 (순수 비즈니스 로직)
 * iOS와 동일한 로직 + 병렬 처리로 성능 최적화
 */
object MonteCarloSimulator {
    
    // 병렬 처리를 위한 코어 수
    private val parallelism = Runtime.getRuntime().availableProcessors().coerceIn(2, 8)
    
    /**
     * 몬테카를로 시뮬레이션 실행 (병렬 처리)
     */
    suspend fun simulate(
        initialAsset: Double,
        monthlyInvestment: Double,
        targetAsset: Double,
        meanReturn: Double,
        volatility: Double,
        simulationCount: Int = 30_000,
        maxMonths: Int = 1200,
        trackPaths: Boolean = true,
        progressCallback: ProgressCallback? = null
    ): MonteCarloResult = coroutineScope {
        // 월 수익률 파라미터 미리 계산 (ln 계산 안전하게 처리)
        val annualMean = (meanReturn / 100.0).coerceAtLeast(-0.99) // ln(0) 방지: 최소 -99%
        val annualVolatility = (volatility / 100.0).coerceAtLeast(0.0)
        val monthlyMean = ln(1 + annualMean) / 12.0
        val monthlyVolatility = annualVolatility / sqrt(12.0)
        
        // 메모리 최적화: 경로 추적은 샘플링만 (전체의 1%만 저장)
        val trackPathsSampled = trackPaths
        val sampleRate = if (trackPaths) 100 else Int.MAX_VALUE // 100개당 1개만 저장
        
        // 청크 크기 계산
        val chunkSize = simulationCount / parallelism
        val completed = java.util.concurrent.atomic.AtomicInteger(0)
        
        // 병렬로 시뮬레이션 실행
        val chunkResults = (0 until parallelism).map { chunkIndex ->
            async(Dispatchers.Default) {
                val random = Random(System.nanoTime() + chunkIndex)
                val startIndex = chunkIndex * chunkSize
                val endIndex = if (chunkIndex == parallelism - 1) simulationCount else startIndex + chunkSize
                val chunkCount = endIndex - startIndex
                
                val successMonths = ArrayList<Int>(chunkCount)
                val paths = if (trackPaths) ArrayList<AssetPath>(chunkCount) else ArrayList()
                var failureCount = 0
                
                for (i in 0 until chunkCount) {
                    // 메모리 최적화: 샘플링 (100개당 1개만 경로 저장)
                    val shouldTrackPath = trackPathsSampled && (i % sampleRate == 0)
                    
                    val (months, path) = runSingleSimulation(
                        initialAsset = initialAsset,
                        monthlyInvestment = monthlyInvestment,
                        targetAsset = targetAsset,
                        monthlyMean = monthlyMean,
                        monthlyVolatility = monthlyVolatility,
                        maxMonths = maxMonths,
                        trackPath = shouldTrackPath,
                        random = random
                    )
                    
                    if (months != null) {
                        successMonths.add(months)
                    } else {
                        failureCount++
                    }
                    
                    if (shouldTrackPath && path != null) {
                        paths.add(path)
                    }
                    
                    // 진행률 콜백 (1000번마다)
                    val totalCompleted = completed.incrementAndGet()
                    if (totalCompleted % 1000 == 0) {
                        progressCallback?.invoke(totalCompleted, emptyList(), emptyList())
                    }
                }
                
                ChunkResult(successMonths, paths, failureCount)
            }
        }.awaitAll()
        
        // 결과 합치기
        val allSuccessMonths = chunkResults.flatMap { it.successMonths }
        val allPaths = chunkResults.flatMap { it.paths }
        val totalFailureCount = chunkResults.sumOf { it.failureCount }
        
        // 최종 콜백
        progressCallback?.invoke(simulationCount, allSuccessMonths, allPaths)
        
        val successRate = allSuccessMonths.size.toDouble() / simulationCount
        
        // 대표 경로 추출 (샘플링된 경로 중에서 선택)
        val representativePaths = if (trackPathsSampled && allPaths.isNotEmpty()) {
            extractRepresentativePaths(allPaths, allSuccessMonths)
        } else null
        
        MonteCarloResult(
            successRate = successRate,
            successMonthsDistribution = allSuccessMonths,
            failureCount = totalFailureCount,
            totalSimulations = simulationCount,
            representativePaths = representativePaths
        )
    }
    
    // MARK: - Single Simulation
    
    /**
     * 단일 시뮬레이션 실행
     */
    private fun runSingleSimulation(
        initialAsset: Double,
        monthlyInvestment: Double,
        targetAsset: Double,
        monthlyMean: Double,
        monthlyVolatility: Double,
        maxMonths: Int,
        trackPath: Boolean,
        random: Random
    ): Pair<Int?, AssetPath?> {
        var currentAsset = initialAsset
        var months = 0
        
        // 경로 추적용 배열 (최적화: 예상 크기로 초기화)
        val assetHistory = if (trackPath) {
            DoubleArray(maxMonths + 1).also { it[0] = initialAsset }
        } else null
        
        while (currentAsset < targetAsset && months < maxMonths) {
            // 1. 월초 투자금 추가
            currentAsset += monthlyInvestment
            
            // 2. Box-Muller 변환 (u1이 0이면 ln(0) = -Infinity 방지)
            val u1 = random.nextDouble().coerceIn(1e-10, 1.0 - 1e-10)
            val u2 = random.nextDouble()
            val z0 = sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
            val monthlyReturn = monthlyMean + z0 * monthlyVolatility
            
            // 3. 수익 적용
            currentAsset *= exp(monthlyReturn)
            if (currentAsset < 0 || currentAsset.isNaN() || currentAsset.isInfinite()) {
                currentAsset = 0.0
            }
            
            months++
            
            if (trackPath && assetHistory != null) {
                assetHistory[months] = currentAsset
            }
        }
        
        val success = currentAsset >= targetAsset
        val finalMonths = if (success) months else null
        
        val path = if (trackPath && assetHistory != null) {
            AssetPath(
                monthlyAssets = assetHistory.take(months + 1),
                monthsToTarget = finalMonths
            )
        } else null
        
        return Pair(finalMonths, path)
    }
    
    // MARK: - Representative Paths
    
    /**
     * 대표 경로 추출 (iOS와 동일한 로직)
     */
    fun extractRepresentativePaths(
        paths: List<AssetPath>,
        successMonths: List<Int>
    ): RepresentativePaths? {
        if (successMonths.isEmpty()) return null
        if (paths.isEmpty()) return null
        
        val sorted = successMonths.sorted()
        val sortedSize = sorted.size
        
        // 안전한 인덱스 계산 (IndexOutOfBoundsException 방지)
        val safeIndex = { size: Int, percent: Double -> 
            (size * percent).toInt().coerceIn(0, size - 1)
        }
        
        val bestIndex = safeIndex(sortedSize, 0.1)
        val medianIndex = sortedSize / 2
        val worstIndex = safeIndex(sortedSize, 0.9)
        
        val bestMonths = sorted[bestIndex]
        val medianMonths = sorted[medianIndex]
        val worstMonths = sorted[worstIndex]
        
        // 해당 개월수와 가장 가까운 경로 찾기 (안전한 폴백)
        val pathSize = paths.size
        val bestPath = paths.find { it.monthsToTarget == bestMonths }
            ?: paths.getOrNull(safeIndex(pathSize, 0.1))
            ?: paths.first()
        val medianPath = paths.find { it.monthsToTarget == medianMonths }
            ?: paths.getOrNull(pathSize / 2)
            ?: paths.first()
        val worstPath = paths.find { it.monthsToTarget == worstMonths }
            ?: paths.getOrNull(safeIndex(pathSize, 0.9))
            ?: paths.first()
        
        return RepresentativePaths(
            best = bestPath,
            median = medianPath,
            worst = worstPath
        )
    }
    
    // MARK: - UserProfile-based Simulation
    
    /**
     * UserProfile 기반 시뮬레이션 (편의 메서드)
     */
    suspend fun simulate(
        profile: UserProfile,
        currentAsset: Double,
        volatility: Double = 15.0,
        simulationCount: Int = 30_000,
        trackPaths: Boolean = true,
        progressCallback: ProgressCallback? = null
    ): MonteCarloResult {
        val targetAsset = RetirementCalculator.calculateTargetAssets(
            desiredMonthlyIncome = profile.desiredMonthlyIncome,
            postRetirementReturnRate = profile.postRetirementReturnRate
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
