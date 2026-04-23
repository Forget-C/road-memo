package com.roadmemo.domain.model

import java.time.Instant
import java.time.LocalDate

@JvmInline
value class Money(val amountInCent: Long) {
    init {
        require(amountInCent >= 0) { "Money must be >= 0 cent" }
    }
}

enum class VehiclePowertrainType {
    GASOLINE,
    DIESEL,
    HEV,
    PHEV,
    EV,
}

enum class EnergyType {
    FUEL,
    ELECTRIC,
}

enum class ChargeMode {
    HOME_AC,
    PUBLIC_AC,
    PUBLIC_DC,
    OTHER,
}

enum class MaintenanceType {
    MINOR_SERVICE,
    MAJOR_SERVICE,
    ENGINE_OIL,
    OIL_FILTER,
    AIR_FILTER,
    CABIN_FILTER,
    TIRE,
    BRAKE_PAD,
    BATTERY,
    OTHER,
}

enum class ExpenseCategory {
    REPAIR,
    PARKING,
    TOLL,
    CAR_WASH,
    TRAFFIC_FINE,
    ACCESSORY,
    OTHER,
}

enum class ReminderType {
    MAINTENANCE,
    INSURANCE,
    INSPECTION,
    TAX,
    CUSTOM,
}

enum class RenewalType {
    INSURANCE,
    INSPECTION,
    TAX,
    OTHER,
}

enum class ReminderSourceType {
    MAINTENANCE_RECORD,
    RENEWAL_RECORD,
    MANUAL,
}

enum class ReminderStatus {
    PENDING,
    TRIGGERED,
    DONE,
    DISMISSED,
}

enum class CostCategoryType {
    ENERGY,
    MAINTENANCE,
    EXPENSE,
    RENEWAL,
}

data class Vehicle(
    val id: Long,
    val brand: String,
    val model: String,
    val plateNumber: String?,
    val purchaseDate: LocalDate?,
    val powertrainType: VehiclePowertrainType,
    val note: String?,
    val isDefault: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

sealed interface EnergyDetail {
    val energyType: EnergyType
    val quantityInThousandth: Long
    val isFull: Boolean
    val stationName: String?
}

data class FuelEnergyDetail(
    override val quantityInThousandth: Long,
    override val isFull: Boolean,
    override val stationName: String?,
    val fuelLabel: String,
) : EnergyDetail {
    override val energyType: EnergyType = EnergyType.FUEL
}

data class ElectricEnergyDetail(
    override val quantityInThousandth: Long,
    override val isFull: Boolean,
    override val stationName: String?,
    val chargeMode: ChargeMode,
) : EnergyDetail {
    override val energyType: EnergyType = EnergyType.ELECTRIC
}

data class EnergyRecord(
    val id: Long,
    val vehicleId: Long,
    val occurredAt: Instant,
    val odometerKm: Int,
    val totalCost: Money,
    val detail: EnergyDetail,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class MaintenanceRecord(
    val id: Long,
    val vehicleId: Long,
    val occurredAt: Instant,
    val odometerKm: Int?,
    val maintenanceType: MaintenanceType,
    val amount: Money,
    val storeName: String?,
    val note: String?,
    val nextDueDate: LocalDate?,
    val nextDueOdometerKm: Int?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ExpenseRecord(
    val id: Long,
    val vehicleId: Long,
    val occurredAt: Instant,
    val category: ExpenseCategory,
    val amount: Money,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class RenewalRecord(
    val id: Long,
    val vehicleId: Long,
    val type: RenewalType,
    val providerName: String?,
    val policyNumber: String?,
    val amount: Money,
    val validFrom: LocalDate?,
    val validUntil: LocalDate,
    val reminderEnabled: Boolean,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class Reminder(
    val id: Long,
    val vehicleId: Long,
    val type: ReminderType,
    val title: String,
    val remindAt: Instant?,
    val remindOdometerKm: Int?,
    val advanceDays: Int?,
    val isEnabled: Boolean,
    val status: ReminderStatus,
    val sourceType: ReminderSourceType?,
    val sourceId: Long?,
    val note: String?,
    val lastTriggeredAt: Instant?,
    val completedAt: Instant?,
    val dismissedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class MonthlyCostSummary(
    val vehicleId: Long,
    val year: Int,
    val month: Int,
    val energyCost: Money,
    val maintenanceCost: Money,
    val expenseCost: Money,
    val renewalCost: Money,
    val totalCost: Money,
)

data class CategoryCostSummary(
    val categoryType: CostCategoryType,
    val categoryKey: String,
    val amount: Money,
    val percentage: Float,
)

data class EnergyConsumptionPoint(
    val vehicleId: Long,
    val energyType: EnergyType,
    val startOdometerKm: Int,
    val endOdometerKm: Int,
    val quantityInThousandth: Long,
    val consumptionPer100Km: Double,
    val occurredAt: Instant,
)

data class VehicleComparisonSummary(
    val vehicleId: Long,
    val vehicleDisplayName: String,
    val totalCost: Money,
    val totalDistanceKm: Int?,
    val averageCostPerKm: Double?,
)

data class HomeSummary(
    val vehicle: Vehicle,
    val monthlyCostSummary: MonthlyCostSummary,
    val latestEnergyRecord: EnergyRecord?,
    val latestMaintenanceRecord: MaintenanceRecord?,
    val upcomingReminders: List<Reminder>,
    val latestFuelConsumption: EnergyConsumptionPoint?,
    val latestElectricConsumption: EnergyConsumptionPoint?,
)
