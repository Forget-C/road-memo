package com.roadmemo.app.data.local.mapper

import com.roadmemo.app.data.local.entity.EnergyRecordEntity
import com.roadmemo.app.data.local.entity.ExpenseRecordEntity
import com.roadmemo.app.data.local.entity.MaintenanceRecordEntity
import com.roadmemo.app.data.local.entity.ReminderEntity
import com.roadmemo.app.data.local.entity.RenewalRecordEntity
import com.roadmemo.app.data.local.entity.VehicleEntity
import com.roadmemo.app.domain.model.ChargeMode
import com.roadmemo.app.domain.model.ElectricEnergyDetail
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.EnergyType
import com.roadmemo.app.domain.model.ExpenseCategory
import com.roadmemo.app.domain.model.ExpenseRecord
import com.roadmemo.app.domain.model.FuelEnergyDetail
import com.roadmemo.app.domain.model.MaintenanceRecord
import com.roadmemo.app.domain.model.MaintenanceType
import com.roadmemo.app.domain.model.Money
import com.roadmemo.app.domain.model.Reminder
import com.roadmemo.app.domain.model.ReminderSourceType
import com.roadmemo.app.domain.model.ReminderStatus
import com.roadmemo.app.domain.model.ReminderType
import com.roadmemo.app.domain.model.RenewalRecord
import com.roadmemo.app.domain.model.RenewalType
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.model.VehiclePowertrainType
import java.time.Instant
import java.time.LocalDate

fun VehicleEntity.toDomain(): Vehicle = Vehicle(
    id = id,
    brand = brand,
    model = model,
    plateNumber = plateNumber,
    purchaseDate = purchaseDateEpochDay?.let(LocalDate::ofEpochDay),
    powertrainType = VehiclePowertrainType.valueOf(powertrainType),
    note = note,
    isDefault = isDefault,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun EnergyRecordEntity.toDomain(): EnergyRecord {
    val detail = if (energyType == EnergyType.FUEL.name) {
        FuelEnergyDetail(
            quantityInThousandth = quantityInThousandth,
            isFull = isFull,
            stationName = stationName,
            fuelLabel = fuelLabel.orEmpty(),
        )
    } else {
        ElectricEnergyDetail(
            quantityInThousandth = quantityInThousandth,
            isFull = isFull,
            stationName = stationName,
            chargeMode = ChargeMode.valueOf(chargeMode ?: ChargeMode.OTHER.name),
        )
    }

    return EnergyRecord(
        id = id,
        vehicleId = vehicleId,
        occurredAt = Instant.ofEpochMilli(occurredAtEpochMillis),
        odometerKm = odometerKm,
        totalCost = Money(amountInCent),
        detail = detail,
        note = note,
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    )
}

fun MaintenanceRecordEntity.toDomain(): MaintenanceRecord = MaintenanceRecord(
    id = id,
    vehicleId = vehicleId,
    occurredAt = Instant.ofEpochMilli(occurredAtEpochMillis),
    odometerKm = odometerKm,
    maintenanceType = MaintenanceType.valueOf(maintenanceType),
    amount = Money(amountInCent),
    storeName = storeName,
    note = note,
    nextDueDate = nextDueDateEpochDay?.let(LocalDate::ofEpochDay),
    nextDueOdometerKm = nextDueOdometerKm,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun ExpenseRecordEntity.toDomain(): ExpenseRecord = ExpenseRecord(
    id = id,
    vehicleId = vehicleId,
    occurredAt = Instant.ofEpochMilli(occurredAtEpochMillis),
    category = ExpenseCategory.valueOf(category),
    amount = Money(amountInCent),
    note = note,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun RenewalRecordEntity.toDomain(): RenewalRecord = RenewalRecord(
    id = id,
    vehicleId = vehicleId,
    type = RenewalType.valueOf(type),
    providerName = providerName,
    policyNumber = policyNumber,
    amount = Money(amountInCent),
    validFrom = validFromEpochDay?.let(LocalDate::ofEpochDay),
    validUntil = LocalDate.ofEpochDay(validUntilEpochDay),
    reminderEnabled = reminderEnabled,
    note = note,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    vehicleId = vehicleId,
    type = ReminderType.valueOf(type),
    title = title,
    remindAt = remindAtEpochMillis?.let(Instant::ofEpochMilli),
    remindOdometerKm = remindOdometerKm,
    advanceDays = advanceDays,
    isEnabled = isEnabled,
    status = ReminderStatus.valueOf(status),
    sourceType = sourceType?.let(ReminderSourceType::valueOf),
    sourceId = sourceId,
    note = note,
    lastTriggeredAt = lastTriggeredAtEpochMillis?.let(Instant::ofEpochMilli),
    completedAt = completedAtEpochMillis?.let(Instant::ofEpochMilli),
    dismissedAt = dismissedAtEpochMillis?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)
