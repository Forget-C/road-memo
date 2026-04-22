package com.roadmemo.app.data.local.mapper

import com.roadmemo.app.data.local.entity.EnergyRecordEntity
import com.roadmemo.app.data.local.entity.ExpenseRecordEntity
import com.roadmemo.app.data.local.entity.MaintenanceRecordEntity
import com.roadmemo.app.data.local.entity.ReminderEntity
import com.roadmemo.app.data.local.entity.RenewalRecordEntity
import com.roadmemo.app.data.local.entity.VehicleEntity
import com.roadmemo.app.domain.model.ChargeMode
import com.roadmemo.app.domain.model.EnergyType
import com.roadmemo.app.domain.model.ExpenseCategory
import com.roadmemo.app.domain.model.FuelEnergyDetail
import com.roadmemo.app.domain.model.MaintenanceType
import com.roadmemo.app.domain.model.ReminderSourceType
import com.roadmemo.app.domain.model.ReminderStatus
import com.roadmemo.app.domain.model.ReminderType
import com.roadmemo.app.domain.model.RenewalType
import com.roadmemo.app.domain.model.VehiclePowertrainType
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadMemoMappersTest {

    @Test
    fun `vehicle entity maps to domain vehicle`() {
        val entity = VehicleEntity(
            id = 1L,
            brand = "比亚迪",
            model = "宋 PLUS",
            plateNumber = "沪A12345",
            purchaseDateEpochDay = LocalDate.parse("2025-01-01").toEpochDay(),
            powertrainType = "PHEV",
            note = "家用",
            isDefault = true,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 2000L,
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals(VehiclePowertrainType.PHEV, domain.powertrainType)
        assertEquals(LocalDate.parse("2025-01-01"), domain.purchaseDate)
        assertEquals("家用", domain.note)
        assertEquals(Instant.ofEpochMilli(1000L), domain.createdAt)
    }

    @Test
    fun `fuel energy entity maps to fuel detail`() {
        val entity = EnergyRecordEntity(
            id = 2L,
            vehicleId = 1L,
            occurredAtEpochMillis = 3000L,
            energyType = "FUEL",
            odometerKm = 12345,
            quantityInThousandth = 42000L,
            amountInCent = 31500L,
            isFull = true,
            fuelLabel = "95",
            chargeMode = null,
            stationName = "中石化",
            note = "周末加油",
            createdAtEpochMillis = 3000L,
            updatedAtEpochMillis = 4000L,
        )

        val domain = entity.toDomain()

        assertEquals(EnergyType.FUEL, domain.detail.energyType)
        assertTrue(domain.detail is FuelEnergyDetail)
        assertEquals("95", (domain.detail as FuelEnergyDetail).fuelLabel)
        assertEquals(31500L, domain.totalCost.amountInCent)
        assertEquals("周末加油", domain.note)
    }

    @Test
    fun `electric energy entity maps to electric detail with fallback mode`() {
        val entity = EnergyRecordEntity(
            id = 3L,
            vehicleId = 1L,
            occurredAtEpochMillis = 5000L,
            energyType = "ELECTRIC",
            odometerKm = 8888,
            quantityInThousandth = 36000L,
            amountInCent = 4800L,
            isFull = false,
            fuelLabel = null,
            chargeMode = null,
            stationName = "特来电",
            note = null,
            createdAtEpochMillis = 5000L,
            updatedAtEpochMillis = 6000L,
        )

        val domain = entity.toDomain()

        assertEquals(EnergyType.ELECTRIC, domain.detail.energyType)
        assertEquals(ChargeMode.OTHER, (domain.detail as com.roadmemo.app.domain.model.ElectricEnergyDetail).chargeMode)
        assertEquals("特来电", domain.detail.stationName)
    }

    @Test
    fun `maintenance entity maps to domain maintenance`() {
        val entity = MaintenanceRecordEntity(
            id = 4L,
            vehicleId = 1L,
            occurredAtEpochMillis = 7000L,
            odometerKm = 9999,
            maintenanceType = "ENGINE_OIL",
            amountInCent = 39950L,
            storeName = "途虎",
            note = "换机油",
            nextDueDateEpochDay = LocalDate.parse("2026-08-01").toEpochDay(),
            nextDueOdometerKm = 14999,
            createdAtEpochMillis = 7000L,
            updatedAtEpochMillis = 8000L,
        )

        val domain = entity.toDomain()

        assertEquals(MaintenanceType.ENGINE_OIL, domain.maintenanceType)
        assertEquals(LocalDate.parse("2026-08-01"), domain.nextDueDate)
        assertEquals(14999, domain.nextDueOdometerKm)
    }

    @Test
    fun `expense entity maps to domain expense`() {
        val entity = ExpenseRecordEntity(
            id = 5L,
            vehicleId = 1L,
            occurredAtEpochMillis = 9000L,
            category = "PARKING",
            amountInCent = 1800L,
            note = "地库停车",
            createdAtEpochMillis = 9000L,
            updatedAtEpochMillis = 10000L,
        )

        val domain = entity.toDomain()

        assertEquals(ExpenseCategory.PARKING, domain.category)
        assertEquals(1800L, domain.amount.amountInCent)
        assertEquals("地库停车", domain.note)
    }

    @Test
    fun `renewal entity maps to domain renewal`() {
        val entity = RenewalRecordEntity(
            id = 6L,
            vehicleId = 1L,
            type = "INSURANCE",
            providerName = "保司 A",
            policyNumber = "POL-001",
            amountInCent = 50000L,
            validFromEpochDay = LocalDate.parse("2026-01-01").toEpochDay(),
            validUntilEpochDay = LocalDate.parse("2026-12-31").toEpochDay(),
            reminderEnabled = true,
            note = "商业险",
            createdAtEpochMillis = 11000L,
            updatedAtEpochMillis = 12000L,
        )

        val domain = entity.toDomain()

        assertEquals(RenewalType.INSURANCE, domain.type)
        assertEquals(LocalDate.parse("2026-12-31"), domain.validUntil)
        assertEquals("POL-001", domain.policyNumber)
    }

    @Test
    fun `reminder entity maps to domain reminder`() {
        val entity = ReminderEntity(
            id = 7L,
            vehicleId = 1L,
            type = "INSURANCE",
            title = "车险即将到期",
            remindAtEpochMillis = 13000L,
            remindOdometerKm = null,
            advanceDays = 7,
            isEnabled = true,
            status = "PENDING",
            sourceType = "RENEWAL_RECORD",
            sourceId = 77L,
            note = null,
            lastTriggeredAtEpochMillis = null,
            completedAtEpochMillis = null,
            dismissedAtEpochMillis = null,
            createdAtEpochMillis = 13000L,
            updatedAtEpochMillis = 14000L,
        )

        val domain = entity.toDomain()

        assertEquals(ReminderType.INSURANCE, domain.type)
        assertEquals(ReminderStatus.PENDING, domain.status)
        assertEquals(ReminderSourceType.RENEWAL_RECORD, domain.sourceType)
        assertEquals(77L, domain.sourceId)
        assertEquals(Instant.ofEpochMilli(13000L), domain.remindAt)
        assertNull(domain.completedAt)
    }
}
