package com.roadmemo.app.domain.model

import java.time.Instant
import java.util.Locale

data class ConsumptionSegment(
    val vehicleId: Long,
    val energyType: EnergyType,
    val startRecordId: Long,
    val endRecordId: Long,
    val startOdometerKm: Int,
    val endOdometerKm: Int,
    val distanceKm: Int,
    val quantityInThousandth: Long,
    val valuePer100: Double,
    val occurredAt: Instant,
)

data class ConsumptionMetric(
    val unit: String,
    val latestValuePer100: Double?,
    val averageValuePer100: Double?,
    val segmentCount: Int,
) {
    val latestText: String?
        get() = latestValuePer100?.toConsumptionText(unit)

    val averageText: String?
        get() = averageValuePer100?.toConsumptionText(unit)
}

data class ConsumptionSummary(
    val fuel: ConsumptionMetric?,
    val electric: ConsumptionMetric?,
    val fuelSegments: List<ConsumptionSegment>,
    val electricSegments: List<ConsumptionSegment>,
)

private fun Double.toConsumptionText(unit: String): String =
    String.format(Locale.US, "%.1f %s", this, unit)
