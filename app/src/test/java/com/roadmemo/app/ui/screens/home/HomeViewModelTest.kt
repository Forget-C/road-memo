package com.roadmemo.app.ui.screens.home

import com.roadmemo.app.MainDispatcherRule
import com.roadmemo.app.domain.model.ChargeMode
import com.roadmemo.app.domain.model.ElectricEnergyDetail
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.ExpenseCategory
import com.roadmemo.app.domain.model.ExpenseRecord
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
import com.roadmemo.app.domain.repository.EnergyRepository
import com.roadmemo.app.domain.repository.ExpenseRepository
import com.roadmemo.app.domain.repository.MaintenanceRepository
import com.roadmemo.app.domain.repository.ReminderRepository
import com.roadmemo.app.domain.repository.RenewalRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ui state falls back to empty when no default vehicle`() = runTest {
        val viewModel = HomeViewModel(
            vehicleRepository = FakeVehicleRepository(null),
            energyRepository = FakeEnergyRepository(),
            maintenanceRepository = FakeMaintenanceRepository(),
            expenseRepository = FakeExpenseRepository(),
            renewalRepository = FakeRenewalRepository(),
            reminderRepository = FakeReminderRepository(),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals("暂无默认车辆", viewModel.uiState.value.vehicleTitle)
        assertEquals("¥0.00", viewModel.uiState.value.monthlyTotalText)
        assertTrue(viewModel.uiState.value.isEmpty)
        collector.cancel()
    }

    @Test
    fun `ui state aggregates current vehicle records`() = runTest {
        val month = YearMonth.now()
        val vehicle = sampleHomeVehicle()
        val energyRecord = sampleEnergyRecord(month)
        val maintenanceRecord = sampleMaintenanceRecord(month)
        val expenseRecord = sampleExpenseRecord(month)
        val renewalRecord = sampleRenewalRecord(month)
        val reminder = sampleReminder(month)

        val viewModel = HomeViewModel(
            vehicleRepository = FakeVehicleRepository(vehicle),
            energyRepository = FakeEnergyRepository(records = listOf(energyRecord), latest = energyRecord),
            maintenanceRepository = FakeMaintenanceRepository(records = listOf(maintenanceRecord), latest = maintenanceRecord),
            expenseRepository = FakeExpenseRepository(records = listOf(expenseRecord)),
            renewalRepository = FakeRenewalRepository(records = listOf(renewalRecord)),
            reminderRepository = FakeReminderRepository(reminders = listOf(reminder)),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("比亚迪 宋 PLUS · 沪A12345", uiState.vehicleTitle)
        assertEquals("¥293.00", uiState.monthlyTotalText)
        assertEquals(
            listOf("能源 ¥48.00", "保养 ¥120.00", "费用 ¥25.00", "续期 ¥100.00"),
            uiState.summaryItems,
        )
        assertTrue(uiState.recentEnergyText!!.contains("48.0 kWh"))
        assertTrue(uiState.recentMaintenanceText!!.contains("MINOR_SERVICE"))
        assertEquals(1, uiState.upcomingReminderTexts.size)
        assertFalse(uiState.isEmpty)
        collector.cancel()
    }

    private class FakeVehicleRepository(
        defaultVehicle: Vehicle?,
    ) : VehicleRepository {
        private val default = MutableStateFlow(defaultVehicle)
        private val vehicles = MutableStateFlow(defaultVehicle?.let(::listOf) ?: emptyList())

        override fun observeVehicles(): Flow<List<Vehicle>> = vehicles

        override fun observeDefaultVehicle(): Flow<Vehicle?> = default

        override suspend fun addVehicle(
            brand: String,
            model: String,
            plateNumber: String?,
            powertrainType: String,
            note: String?,
        ): Long = error("Not needed in test")

        override suspend fun setDefaultVehicle(vehicleId: Long) = Unit
    }

    private class FakeEnergyRepository(
        private val records: List<EnergyRecord> = emptyList(),
        private val latest: EnergyRecord? = null,
    ) : EnergyRepository {
        override fun observeRecords(vehicleId: Long): Flow<List<EnergyRecord>> = flowOf(records)
        override fun observeLatest(vehicleId: Long): Flow<EnergyRecord?> = flowOf(latest)
        override suspend fun getRecord(recordId: Long): EnergyRecord? = null
        override suspend fun addRecord(
            vehicleId: Long,
            energyType: String,
            odometerKm: Int,
            quantityInThousandth: Long,
            amountInCent: Long,
            isFull: Boolean,
            fuelLabel: String?,
            chargeMode: String?,
            stationName: String?,
            note: String?,
        ): Long = error("Not needed in test")
        override suspend fun updateRecord(
            recordId: Long,
            energyType: String,
            odometerKm: Int,
            quantityInThousandth: Long,
            amountInCent: Long,
            isFull: Boolean,
            fuelLabel: String?,
            chargeMode: String?,
            stationName: String?,
            note: String?,
        ) = Unit
        override suspend fun deleteRecord(recordId: Long) = Unit
    }

    private class FakeMaintenanceRepository(
        private val records: List<MaintenanceRecord> = emptyList(),
        private val latest: MaintenanceRecord? = null,
    ) : MaintenanceRepository {
        override fun observeRecords(vehicleId: Long): Flow<List<MaintenanceRecord>> = flowOf(records)
        override fun observeLatest(vehicleId: Long): Flow<MaintenanceRecord?> = flowOf(latest)
        override suspend fun getRecord(recordId: Long): MaintenanceRecord? = null
        override suspend fun addRecord(
            vehicleId: Long,
            maintenanceType: String,
            amountInCent: Long,
            odometerKm: Int?,
            storeName: String?,
            note: String?,
            nextDueDateEpochDay: Long?,
            nextDueOdometerKm: Int?,
        ): Long = error("Not needed in test")
        override suspend fun updateRecord(
            recordId: Long,
            maintenanceType: String,
            amountInCent: Long,
            odometerKm: Int?,
            storeName: String?,
            note: String?,
            nextDueDateEpochDay: Long?,
            nextDueOdometerKm: Int?,
        ) = Unit
        override suspend fun deleteRecord(recordId: Long) = Unit
    }

    private class FakeExpenseRepository(
        private val records: List<ExpenseRecord> = emptyList(),
    ) : ExpenseRepository {
        override fun observeRecords(vehicleId: Long): Flow<List<ExpenseRecord>> = flowOf(records)
        override suspend fun getRecord(recordId: Long): ExpenseRecord? = null
        override suspend fun addRecord(
            vehicleId: Long,
            category: String,
            amountInCent: Long,
            note: String?,
        ): Long = error("Not needed in test")
        override suspend fun updateRecord(
            recordId: Long,
            category: String,
            amountInCent: Long,
            note: String?,
        ) = Unit
        override suspend fun deleteRecord(recordId: Long) = Unit
    }

    private class FakeRenewalRepository(
        private val records: List<RenewalRecord> = emptyList(),
    ) : RenewalRepository {
        override fun observeRecords(vehicleId: Long): Flow<List<RenewalRecord>> = flowOf(records)
        override suspend fun getRecord(recordId: Long): RenewalRecord? = null
        override suspend fun addRecord(
            vehicleId: Long,
            type: String,
            providerName: String?,
            policyNumber: String?,
            amountInCent: Long,
            validFromEpochDay: Long?,
            validUntilEpochDay: Long,
            reminderEnabled: Boolean,
            note: String?,
        ): Long = error("Not needed in test")
        override suspend fun updateRecord(
            recordId: Long,
            type: String,
            providerName: String?,
            policyNumber: String?,
            amountInCent: Long,
            validFromEpochDay: Long?,
            validUntilEpochDay: Long,
            reminderEnabled: Boolean,
            note: String?,
        ) = Unit
        override suspend fun deleteRecord(recordId: Long) = Unit
    }

    private class FakeReminderRepository(
        private val reminders: List<Reminder> = emptyList(),
    ) : ReminderRepository {
        override fun observeUpcoming(vehicleId: Long): Flow<List<Reminder>> = flowOf(reminders)
        override suspend fun markDone(reminderId: Long) = Unit
        override suspend fun dismiss(reminderId: Long) = Unit
    }
}

private fun sampleHomeVehicle(): Vehicle = Vehicle(
    id = 1L,
    brand = "比亚迪",
    model = "宋 PLUS",
    plateNumber = "沪A12345",
    purchaseDate = null,
    powertrainType = VehiclePowertrainType.PHEV,
    note = null,
    isDefault = true,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun sampleEnergyRecord(month: YearMonth): EnergyRecord = EnergyRecord(
    id = 1L,
    vehicleId = 1L,
    occurredAt = monthInstant(month, 5),
    odometerKm = 12345,
    totalCost = Money(4800L),
    detail = ElectricEnergyDetail(
        quantityInThousandth = 48000L,
        isFull = true,
        stationName = "特来电",
        chargeMode = ChargeMode.PUBLIC_DC,
    ),
    note = "午后补能",
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun sampleMaintenanceRecord(month: YearMonth): MaintenanceRecord = MaintenanceRecord(
    id = 2L,
    vehicleId = 1L,
    occurredAt = monthInstant(month, 8),
    odometerKm = 12400,
    maintenanceType = MaintenanceType.MINOR_SERVICE,
    amount = Money(12000L),
    storeName = "4S 店",
    note = null,
    nextDueDate = null,
    nextDueOdometerKm = null,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun sampleExpenseRecord(month: YearMonth): ExpenseRecord = ExpenseRecord(
    id = 3L,
    vehicleId = 1L,
    occurredAt = monthInstant(month, 10),
    category = ExpenseCategory.PARKING,
    amount = Money(2500L),
    note = null,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun sampleRenewalRecord(month: YearMonth): RenewalRecord = RenewalRecord(
    id = 4L,
    vehicleId = 1L,
    type = RenewalType.INSURANCE,
    providerName = "保司 A",
    policyNumber = "POL-001",
    amount = Money(10000L),
    validFrom = null,
    validUntil = month.atEndOfMonth(),
    reminderEnabled = true,
    note = null,
    createdAt = monthInstant(month, 2),
    updatedAt = Instant.EPOCH,
)

private fun sampleReminder(month: YearMonth): Reminder = Reminder(
    id = 5L,
    vehicleId = 1L,
    type = ReminderType.INSURANCE,
    title = "车险即将到期",
    remindAt = monthInstant(month, 20),
    remindOdometerKm = null,
    advanceDays = 7,
    isEnabled = true,
    status = ReminderStatus.PENDING,
    sourceType = ReminderSourceType.RENEWAL_RECORD,
    sourceId = 4L,
    note = null,
    lastTriggeredAt = null,
    completedAt = null,
    dismissedAt = null,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun monthInstant(month: YearMonth, day: Int): Instant =
    month.atDay(day).atStartOfDay(ZoneId.systemDefault()).toInstant()
