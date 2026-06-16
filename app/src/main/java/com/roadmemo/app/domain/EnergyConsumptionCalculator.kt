package com.roadmemo.app.domain

import com.roadmemo.app.domain.model.ConsumptionMetric
import com.roadmemo.app.domain.model.ConsumptionSegment
import com.roadmemo.app.domain.model.ConsumptionSummary
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.EnergyType
import javax.inject.Inject

class EnergyConsumptionCalculator @Inject constructor() {

    fun calculate(records: List<EnergyRecord>): ConsumptionSummary {
        val fuelSegments = buildSegments(records, EnergyType.FUEL)
        val electricSegments = buildSegments(records, EnergyType.ELECTRIC)

        return ConsumptionSummary(
            fuel = fuelSegments.toMetric(unit = "L/100km"),
            electric = electricSegments.toMetric(unit = "kWh/100km"),
            fuelSegments = fuelSegments,
            electricSegments = electricSegments,
        )
    }

    private fun buildSegments(
        records: List<EnergyRecord>,
        energyType: EnergyType,
    ): List<ConsumptionSegment> {
        val candidates = records
            .filter { it.detail.energyType == energyType && it.detail.isFull }
            .sortedWith(compareBy<EnergyRecord> { it.occurredAt }.thenBy { it.id })

        val segments = mutableListOf<ConsumptionSegment>()
        for (index in 1 until candidates.size) {
            val previous = candidates[index - 1]
            val current = candidates[index]
            val distanceKm = current.odometerKm - previous.odometerKm
            if (distanceKm <= 0) continue

            val quantity = current.detail.quantityInThousandth / 1000.0
            if (quantity <= 0.0) continue

            segments += ConsumptionSegment(
                vehicleId = current.vehicleId,
                energyType = energyType,
                startRecordId = previous.id,
                endRecordId = current.id,
                startOdometerKm = previous.odometerKm,
                endOdometerKm = current.odometerKm,
                distanceKm = distanceKm,
                quantityInThousandth = current.detail.quantityInThousandth,
                valuePer100 = quantity / distanceKm * 100,
                occurredAt = current.occurredAt,
            )
        }
        return segments
    }
}

private fun List<ConsumptionSegment>.toMetric(
    unit: String,
): ConsumptionMetric? {
    if (isEmpty()) return null
    return ConsumptionMetric(
        unit = unit,
        latestValuePer100 = last().valuePer100,
        averageValuePer100 = map { it.valuePer100 }.average(),
        segmentCount = size,
    )
}
