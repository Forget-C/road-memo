package com.roadmemo.app.domain

import com.roadmemo.app.domain.model.ChargeMode
import com.roadmemo.app.domain.model.ElectricEnergyDetail
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.EnergyType
import com.roadmemo.app.domain.model.FuelEnergyDetail
import com.roadmemo.app.domain.model.Money
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnergyConsumptionCalculatorTest {

    private val calculator = EnergyConsumptionCalculator()

    @Test
    fun `calculates fuel consumption between two full fuel records`() {
        val summary = calculator.calculate(
            listOf(
                fuelRecord(id = 1L, odometerKm = 10_000, liters = 40.0, isFull = true, occurredAt = 1_000L),
                fuelRecord(id = 2L, odometerKm = 10_500, liters = 30.0, isFull = true, occurredAt = 2_000L),
            ),
        )

        assertEquals(1, summary.fuelSegments.size)
        assertEquals(EnergyType.FUEL, summary.fuelSegments.first().energyType)
        assertEquals(6.0, summary.fuelSegments.first().valuePer100, 0.0001)
        assertEquals("6.0 L/100km", summary.fuel?.latestText)
        assertEquals("6.0 L/100km", summary.fuel?.averageText)
        assertNull(summary.electric)
    }

    @Test
    fun `calculates electric consumption between two full electric records`() {
        val summary = calculator.calculate(
            listOf(
                electricRecord(id = 1L, odometerKm = 5_000, kwh = 50.0, isFull = true, occurredAt = 1_000L),
                electricRecord(id = 2L, odometerKm = 5_400, kwh = 60.0, isFull = true, occurredAt = 2_000L),
            ),
        )

        assertEquals(1, summary.electricSegments.size)
        assertEquals(15.0, summary.electricSegments.first().valuePer100, 0.0001)
        assertEquals("15.0 kWh/100km", summary.electric?.latestText)
        assertEquals("15.0 kWh/100km", summary.electric?.averageText)
        assertNull(summary.fuel)
    }

    @Test
    fun `ignores non full records as interval endpoints`() {
        val summary = calculator.calculate(
            listOf(
                fuelRecord(id = 1L, odometerKm = 10_000, liters = 40.0, isFull = true, occurredAt = 1_000L),
                fuelRecord(id = 2L, odometerKm = 10_250, liters = 10.0, isFull = false, occurredAt = 2_000L),
                fuelRecord(id = 3L, odometerKm = 10_500, liters = 30.0, isFull = true, occurredAt = 3_000L),
            ),
        )

        assertEquals(1, summary.fuelSegments.size)
        assertEquals(500, summary.fuelSegments.first().distanceKm)
        assertEquals(6.0, summary.fuelSegments.first().valuePer100, 0.0001)
    }

    @Test
    fun `skips invalid intervals when odometer does not increase`() {
        val summary = calculator.calculate(
            listOf(
                fuelRecord(id = 1L, odometerKm = 10_000, liters = 40.0, isFull = true, occurredAt = 1_000L),
                fuelRecord(id = 2L, odometerKm = 9_900, liters = 30.0, isFull = true, occurredAt = 2_000L),
            ),
        )

        assertTrue(summary.fuelSegments.isEmpty())
        assertNull(summary.fuel)
    }

    @Test
    fun `builds separate fuel and electric summaries for phev style mixed records`() {
        val summary = calculator.calculate(
            listOf(
                fuelRecord(id = 1L, odometerKm = 10_000, liters = 35.0, isFull = true, occurredAt = 1_000L),
                electricRecord(id = 2L, odometerKm = 10_020, kwh = 20.0, isFull = true, occurredAt = 1_500L),
                fuelRecord(id = 3L, odometerKm = 10_500, liters = 25.0, isFull = true, occurredAt = 2_000L),
                electricRecord(id = 4L, odometerKm = 10_220, kwh = 25.0, isFull = true, occurredAt = 2_500L),
            ),
        )

        assertNotNull(summary.fuel)
        assertNotNull(summary.electric)
        assertEquals("5.0 L/100km", summary.fuel?.latestText)
        assertEquals("12.5 kWh/100km", summary.electric?.latestText)
    }

    @Test
    fun `returns null metrics when there are not enough full records`() {
        val summary = calculator.calculate(
            listOf(
                electricRecord(id = 1L, odometerKm = 5_000, kwh = 50.0, isFull = true, occurredAt = 1_000L),
            ),
        )

        assertTrue(summary.electricSegments.isEmpty())
        assertNull(summary.electric)
        assertNull(summary.fuel)
    }

    private fun fuelRecord(
        id: Long,
        odometerKm: Int,
        liters: Double,
        isFull: Boolean,
        occurredAt: Long,
    ): EnergyRecord = EnergyRecord(
        id = id,
        vehicleId = 1L,
        occurredAt = Instant.ofEpochMilli(occurredAt),
        odometerKm = odometerKm,
        totalCost = Money(10_000),
        detail = FuelEnergyDetail(
            quantityInThousandth = (liters * 1000).toLong(),
            isFull = isFull,
            stationName = "测试油站",
            fuelLabel = "95",
        ),
        note = null,
        createdAt = Instant.ofEpochMilli(occurredAt),
        updatedAt = Instant.ofEpochMilli(occurredAt),
    )

    private fun electricRecord(
        id: Long,
        odometerKm: Int,
        kwh: Double,
        isFull: Boolean,
        occurredAt: Long,
    ): EnergyRecord = EnergyRecord(
        id = id,
        vehicleId = 1L,
        occurredAt = Instant.ofEpochMilli(occurredAt),
        odometerKm = odometerKm,
        totalCost = Money(8_000),
        detail = ElectricEnergyDetail(
            quantityInThousandth = (kwh * 1000).toLong(),
            isFull = isFull,
            stationName = "测试充电站",
            chargeMode = ChargeMode.PUBLIC_DC,
        ),
        note = null,
        createdAt = Instant.ofEpochMilli(occurredAt),
        updatedAt = Instant.ofEpochMilli(occurredAt),
    )
}
