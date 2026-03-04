package kr.mjc.kdj.web.service

import kr.mjc.kdj.web.repository.*
import org.springframework.stereotype.Service

@Service
class StatisticsService(
    private val lightDataRepository: LightDataRepository,
    private val motorDataRepository: MotorDataRepository,
    private val motorControlRepository: MotorControlRepository,
    private val voiceLogRepository: VoiceLogRepository,
    private val voiceCommandRepository: VoiceCommandRepository
) {

    /**
     * 조명 통계 계산
     */
    fun getLightStats(productId: Long): Map<String, Any> {
        val allData = lightDataRepository.findByProductIdOrderByRecordedAtDesc(productId,
            org.springframework.data.domain.PageRequest.of(0, 10000)).content

        if (allData.isEmpty()) {
            return mapOf(
                "totalRecords" to 0,
                "avgBrightness" to 0,
                "autoModePercent" to 0,
                "appModePercent" to 0,
                "modeDistribution" to emptyMap<String, Int>(),
                "topColors" to emptyList<Map<String, Any>>()
            )
        }

        val totalRecords = allData.size

        // 평균 밝기 계산
        val avgBrightness = allData
            .mapNotNull { it.brightnessSensorValue }
            .average()
            .toInt()

        // 모드 분포
        val modeDistribution = allData
            .mapNotNull { it.brightnessMode?.name }
            .groupingBy { it }
            .eachCount()

        val autoCount = modeDistribution["AUTO"] ?: 0
        val appCount = modeDistribution["APP"] ?: 0
        val autoModePercent = if (totalRecords > 0) (autoCount * 100 / totalRecords) else 0
        val appModePercent = if (totalRecords > 0) (appCount * 100 / totalRecords) else 0

        // 자주 사용한 색상 (상위 5개)
        val topColors = allData
            .filter { it.ledColorR != null && it.ledColorG != null && it.ledColorB != null }
            .groupBy { Triple(it.ledColorR, it.ledColorG, it.ledColorB) }
            .map { (rgb, list) ->
                mapOf(
                    "r" to rgb.first,
                    "g" to rgb.second,
                    "b" to rgb.third,
                    "count" to list.size
                )
            }
            .sortedByDescending { it["count"] as Int }
            .take(5)

        return mapOf(
            "totalRecords" to totalRecords,
            "avgBrightness" to avgBrightness,
            "autoModePercent" to autoModePercent,
            "appModePercent" to appModePercent,
            "modeDistribution" to modeDistribution,
            "topColors" to topColors
        )
    }

    /**
     * 모터 통계 계산
     */
    fun getMotorStats(productId: Long): Map<String, Any> {
        val allData = motorDataRepository.findByProductIdOrderByRecordedAtDesc(productId,
            org.springframework.data.domain.PageRequest.of(0, 10000)).content
        val allCommands = motorControlRepository.findByProductIdAndExecutedFalseOrderByCreatedAtDesc(productId)

        if (allData.isEmpty()) {
            return mapOf(
                "totalRecords" to 0,
                "totalCommands" to allCommands.size,
                "trackModePercent" to 0,
                "avgDistance" to 0,
                "directionDistribution" to emptyMap<String, Int>(),
                "modeDistribution" to emptyMap<String, Int>()
            )
        }

        val totalRecords = allData.size

        // 모드 분포
        val modeDistribution = allData
            .mapNotNull { it.mode?.name }
            .groupingBy { it }
            .eachCount()

        val trackCount = modeDistribution["TRACK"] ?: 0
        val trackModePercent = if (totalRecords > 0) (trackCount * 100 / totalRecords) else 0

        // 방향 분포
        val directionDistribution = allData
            .mapNotNull { it.direction?.name }
            .groupingBy { it }
            .eachCount()

        // 평균 감지 거리
        val avgDistance = allData
            .mapNotNull { it.ultrasonicCm }
            .average()
            .toInt()

        return mapOf(
            "totalRecords" to totalRecords,
            "totalCommands" to allCommands.size,
            "trackModePercent" to trackModePercent,
            "avgDistance" to avgDistance,
            "directionDistribution" to directionDistribution,
            "modeDistribution" to modeDistribution
        )
    }

    /**
     * 음성 통계 계산
     */
    fun getVoiceStats(productId: Long): Map<String, Any> {
        val allLogs = voiceLogRepository.findByProductIdOrderByCreatedAtDesc(productId)

        if (allLogs.isEmpty()) {
            return mapOf(
                "totalLogs" to 0,
                "matchedCount" to 0,
                "unmatchedCount" to 0,
                "successRate" to 0,
                "actionDistribution" to emptyMap<String, Int>(),
                "topCommands" to emptyList<Map<String, Any>>()
            )
        }

        val totalLogs = allLogs.size
        val matchedCount = allLogs.count { it.matchedAction != null }
        val unmatchedCount = totalLogs - matchedCount
        val successRate = if (totalLogs > 0) (matchedCount * 100 / totalLogs) else 0

        // 액션별 분포
        val actionDistribution = allLogs
            .mapNotNull { it.matchedAction?.name }
            .groupingBy { it }
            .eachCount()

        // 자주 사용한 명령어 (상위 5개)
        val topCommands = allLogs
            .groupBy { it.inputText }
            .map { (text, list) ->
                mapOf(
                    "inputText" to text,
                    "count" to list.size
                )
            }
            .sortedByDescending { it["count"] as Int }
            .take(5)

        return mapOf(
            "totalLogs" to totalLogs,
            "matchedCount" to matchedCount,
            "unmatchedCount" to unmatchedCount,
            "successRate" to successRate,
            "actionDistribution" to actionDistribution,
            "topCommands" to topCommands
        )
    }
}