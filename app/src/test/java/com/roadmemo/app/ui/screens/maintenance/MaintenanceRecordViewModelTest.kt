package com.roadmemo.app.ui.screens.maintenance

import androidx.lifecycle.SavedStateHandle
import com.roadmemo.app.MainDispatcherRule
import com.roadmemo.app.domain.model.MaintenanceRecord
import com.roadmemo.app.domain.model.MaintenanceType
import com.roadmemo.app.domain.model.Money
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.model.VehiclePowertrainType
import com.roadmemo.app.domain.repository.MaintenanceRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import java.time.Instant
import java.time.LocalDate
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
class MaintenanceRecordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submit shows vehicle error when default vehicle missing`() = runTest {
        val viewModel = MaintenanceRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = null),
            maintenanceRepository = FakeMaintenanceRepository(),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateAmount("280")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("请先添加并设置默认车辆", viewModel.uiState.value.errorMessage)
        collector.cancel()
    }

    @Test
    fun `submit shows next due date error when date invalid`() = runTest {
        val viewModel = MaintenanceRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = sampleMaintenanceVehicle()),
            maintenanceRepository = FakeMaintenanceRepository(),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateAmount("280")
        viewModel.updateNextDueDate("bad-date")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("下次保养日期格式不正确，格式如 2026-12-31", viewModel.uiState.value.errorMessage)
        collector.cancel()
    }

    @Test
    fun `submit adds maintenance record with parsed values`() = runTest {
        val repository = FakeMaintenanceRepository()
        val vehicle = sampleMaintenanceVehicle()
        val viewModel = MaintenanceRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = vehicle),
            maintenanceRepository = repository,
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateMaintenanceType(MaintenanceType.ENGINE_OIL.name)
        viewModel.updateAmount("399.50")
        viewModel.updateOdometer("8888")
        viewModel.updateStoreName("途虎")
        viewModel.updateNextDueDate("2026-08-01")
        viewModel.updateNextDueOdometer("13888")
        viewModel.updateNote("换机油和检查")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, repository.addCalls.size)
        val call = repository.addCalls.single()
        assertEquals(vehicle.id, call.vehicleId)
        assertEquals("ENGINE_OIL", call.maintenanceType)
        assertEquals(39950L, call.amountInCent)
        assertEquals(8888, call.odometerKm)
        assertEquals("途虎", call.storeName)
        assertEquals(LocalDate.parse("2026-08-01").toEpochDay(), call.nextDueDateEpochDay)
        assertEquals(13888, call.nextDueOdometerKm)
        assertEquals("换机油和检查", call.note)
        assertEquals("", viewModel.uiState.value.form.amountText)
        assertEquals(MaintenanceType.ENGINE_OIL.name, viewModel.uiState.value.form.maintenanceType)
        collector.cancel()
    }

    @Test
    fun `edit mode loads existing record`() = runTest {
        val record = sampleMaintenanceRecord()
        val viewModel = MaintenanceRecordViewModel(
            savedStateHandle = SavedStateHandle(mapOf("recordId" to record.id)),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = sampleMaintenanceVehicle()),
            maintenanceRepository = FakeMaintenanceRepository(record = record),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals("编辑保养记录", viewModel.uiState.value.screenTitle)
        assertEquals("8888", viewModel.uiState.value.form.odometerKm)
        assertEquals("399.5", viewModel.uiState.value.form.amountText)
        assertEquals("途虎", viewModel.uiState.value.form.storeName)
        assertEquals("2026-08-01", viewModel.uiState.value.form.nextDueDateText)
        collector.cancel()
    }

    private data class AddCall(
        val vehicleId: Long,
        val maintenanceType: String,
        val amountInCent: Long,
        val odometerKm: Int?,
        val storeName: String?,
        val note: String?,
        val nextDueDateEpochDay: Long?,
        val nextDueOdometerKm: Int?,
    )

    private class FakeVehicleRepository(
        defaultVehicle: Vehicle?,
    ) : VehicleRepository {
        private val vehicles = MutableStateFlow(defaultVehicle?.let(::listOf) ?: emptyList())
        private val default = MutableStateFlow(defaultVehicle)

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

    private class FakeMaintenanceRepository(
        private val record: MaintenanceRecord? = null,
    ) : MaintenanceRepository {
        val addCalls = mutableListOf<AddCall>()

        override fun observeRecords(vehicleId: Long): Flow<List<MaintenanceRecord>> = flowOf(emptyList())

        override fun observeLatest(vehicleId: Long): Flow<MaintenanceRecord?> = flowOf(null)

        override suspend fun getRecord(recordId: Long): MaintenanceRecord? = record

        override suspend fun addRecord(
            vehicleId: Long,
            maintenanceType: String,
            amountInCent: Long,
            odometerKm: Int?,
            storeName: String?,
            note: String?,
            nextDueDateEpochDay: Long?,
            nextDueOdometerKm: Int?,
        ): Long {
            addCalls += AddCall(
                vehicleId = vehicleId,
                maintenanceType = maintenanceType,
                amountInCent = amountInCent,
                odometerKm = odometerKm,
                storeName = storeName,
                note = note,
                nextDueDateEpochDay = nextDueDateEpochDay,
                nextDueOdometerKm = nextDueOdometerKm,
            )
            return addCalls.size.toLong()
        }

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
}

private fun sampleMaintenanceVehicle(): Vehicle = Vehicle(
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

private fun sampleMaintenanceRecord(): MaintenanceRecord = MaintenanceRecord(
    id = 9L,
    vehicleId = 1L,
    occurredAt = Instant.EPOCH,
    odometerKm = 8888,
    maintenanceType = MaintenanceType.ENGINE_OIL,
    amount = Money(39950L),
    storeName = "途虎",
    note = "换机油和检查",
    nextDueDate = LocalDate.parse("2026-08-01"),
    nextDueOdometerKm = 13888,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)
