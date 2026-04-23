package com.roadmemo.app.ui.screens.statistics

import com.roadmemo.app.MainDispatcherRule
import com.roadmemo.app.domain.model.ChargeMode
import com.roadmemo.app.domain.model.ElectricEnergyDetail
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.ExpenseCategory
import com.roadmemo.app.domain.model.ExpenseRecord
import com.roadmemo.app.domain.model.MaintenanceRecord
import com.roadmemo.app.domain.model.MaintenanceType
import com.roadmemo.app.domain.model.Money
import com.roadmemo.app.domain.model.RenewalRecord
import com.roadmemo.app.domain.model.RenewalType
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.model.VehiclePowertrainType
import com.roadmemo.app.domain.repository.EnergyRepository
import com.roadmemo.app.domain.repository.ExpenseRepository
import com.roadmemo.app.domain.repository.MaintenanceRepository
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ui state falls back when no default vehicle`() = runTest {
        val viewModel = StatisticsViewModel(
            vehicleRepository = FakeVehicleRepository(null),
            energyRepository = FakeEnergyRepository(),
            maintenanceRepository = FakeMaintenanceRepository(),
            expenseRepository = FakeExpenseRepository(),
            renewalRepository = FakeRenewalRepository(),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals("暂无默认车辆", viewModel.uiState.value.vehicleSummary)
        assertEquals("¥0.00", viewModel.uiState.value.monthlyTotalText)
        collector.cancel()
    }

    @Test
    fun `ui state aggregates statistics for current vehicle`() = runTest {
        val month = YearMonth.now()
        val vehicle = sampleStatisticsVehicle()
        val energy = sampleStatisticsEnergy(month)
        val maintenance = sampleStatisticsMaintenance(month)
        val expense = sampleStatisticsExpense(month)
        val renewal = sampleStatisticsRenewal(month)

        val viewModel = StatisticsViewModel(
            vehicleRepository = FakeVehicleRepository(vehicle),
            energyRepository = FakeEnergyRepository(records = listOf(energy)),
            maintenanceRepository = FakeMaintenanceRepository(records = listOf(maintenance)),
            expenseRepository = FakeExpenseRepository(records = listOf(expense)),
            renewalRepository = FakeRenewalRepository(records = listOf(renewal)),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("本田 思域 · 沪D12345", uiState.vehicleSummary)
        assertEquals("¥468.00", uiState.monthlyTotalText)
        assertEquals(
            "能源 ¥48.00 · 保养 ¥320.00 · 费用 ¥50.00 · 续期 ¥50.00",
            uiState.monthlyBreakdownText,
        )
        assertTrue(uiState.categorySummaryItems.any { it.contains("能源 / ¥48.00 / 1 条") })
        assertEquals("共 4 条记录，其中能源 1、保养 1、费用 1、续期 1", uiState.recordCountText)
        assertEquals(6, uiState.monthTrendItems.size)
        assertTrue(uiState.recentHighlights.any { it.contains("最近补能") })
        assertTrue(uiState.recentHighlights.any { it.contains("最近到期") })
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
    ) : EnergyRepository {
        override fun observeRecords(vehicleId: Long): Flow<List<EnergyRecord>> = flowOf(records)
        override fun observeLatest(vehicleId: Long): Flow<EnergyRecord?> = flowOf(records.maxByOrNull { it.occurredAt })
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
    ) : MaintenanceRepository {
        override fun observeRecords(vehicleId: Long): Flow<List<MaintenanceRecord>> = flowOf(records)
        override fun observeLatest(vehicleId: Long): Flow<MaintenanceRecord?> = flowOf(records.maxByOrNull { it.occurredAt })
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
}

private fun sampleStatisticsVehicle(): Vehicle = Vehicle(
    id = 1L,
    brand = "本田",
    model = "思域",
    plateNumber = "沪D12345",
    purchaseDate = null,
    powertrainType = VehiclePowertrainType.GASOLINE,
    note = null,
    isDefault = true,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun sampleStatisticsEnergy(month: YearMonth): EnergyRecord = EnergyRecord(
    id = 1L,
    vehicleId = 1L,
    occurredAt = statisticsMonthInstant(month, 3),
    odometerKm = 8000,
    totalCost = Money(4800L),
    detail = ElectricEnergyDetail(
        quantityInThousandth = 30000L,
        isFull = true,
        stationName = "站点 A",
        chargeMode = ChargeMode.PUBLIC_DC,
    ),
    note = null,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun sampleStatisticsMaintenance(month: YearMonth): MaintenanceRecord = MaintenanceRecord(
    id = 2L,
    vehicleId = 1L,
    occurredAt = statisticsMonthInstant(month, 6),
    odometerKm = 8200,
    maintenanceType = MaintenanceType.MINOR_SERVICE,
    amount = Money(32000L),
    storeName = "途虎",
    note = null,
    nextDueDate = null,
    nextDueOdometerKm = null,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun sampleStatisticsExpense(month: YearMonth): ExpenseRecord = ExpenseRecord(
    id = 3L,
    vehicleId = 1L,
    occurredAt = statisticsMonthInstant(month, 12),
    category = ExpenseCategory.PARKING,
    amount = Money(5000L),
    note = null,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun sampleStatisticsRenewal(month: YearMonth): RenewalRecord = RenewalRecord(
    id = 4L,
    vehicleId = 1L,
    type = RenewalType.INSPECTION,
    providerName = "检测站",
    policyNumber = "XJ-001",
    amount = Money(5000L),
    validFrom = null,
    validUntil = month.atEndOfMonth(),
    reminderEnabled = true,
    note = null,
    createdAt = statisticsMonthInstant(month, 1),
    updatedAt = Instant.EPOCH,
)

private fun statisticsMonthInstant(month: YearMonth, day: Int): Instant =
    month.atDay(day).atStartOfDay(ZoneId.systemDefault()).toInstant()
