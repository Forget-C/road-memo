package com.roadmemo.app.ui.screens.energy

import androidx.lifecycle.SavedStateHandle
import com.roadmemo.app.MainDispatcherRule
import com.roadmemo.app.domain.model.ChargeMode
import com.roadmemo.app.domain.model.ElectricEnergyDetail
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.EnergyType
import com.roadmemo.app.domain.model.Money
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.model.VehiclePowertrainType
import com.roadmemo.app.domain.repository.EnergyRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import java.time.Instant
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
class EnergyRecordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submit shows vehicle error when default vehicle missing`() = runTest {
        val viewModel = EnergyRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = null),
            energyRepository = FakeEnergyRepository(),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateOdometer("1234")
        viewModel.updateQuantity("15")
        viewModel.updateAmount("40")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("请先添加并设置默认车辆", viewModel.uiState.value.errorMessage)
        collector.cancel()
    }

    @Test
    fun `submit shows fuel label error for fuel record`() = runTest {
        val viewModel = EnergyRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = sampleEnergyVehicle()),
            energyRepository = FakeEnergyRepository(),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateEnergyType(EnergyType.FUEL.name)
        viewModel.updateOdometer("3000")
        viewModel.updateQuantity("35")
        viewModel.updateAmount("280")
        viewModel.updateFuelLabel("")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("请输入油号", viewModel.uiState.value.errorMessage)
        collector.cancel()
    }

    @Test
    fun `submit adds electric record with parsed values`() = runTest {
        val repository = FakeEnergyRepository()
        val vehicle = sampleEnergyVehicle()
        val viewModel = EnergyRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = vehicle),
            energyRepository = repository,
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateEnergyType(EnergyType.ELECTRIC.name)
        viewModel.updateOdometer("4567")
        viewModel.updateQuantity("32.5")
        viewModel.updateAmount("48.80")
        viewModel.updateChargeMode("PUBLIC_DC")
        viewModel.updateStationName("特来电")
        viewModel.updateNote("晚间快充")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, repository.addCalls.size)
        val call = repository.addCalls.single()
        assertEquals(vehicle.id, call.vehicleId)
        assertEquals("ELECTRIC", call.energyType)
        assertEquals(4567, call.odometerKm)
        assertEquals(32500L, call.quantityInThousandth)
        assertEquals(4880L, call.amountInCent)
        assertEquals("PUBLIC_DC", call.chargeMode)
        assertEquals("特来电", call.stationName)
        assertEquals("晚间快充", call.note)
        assertEquals("", viewModel.uiState.value.form.amountText)
        assertEquals(EnergyType.ELECTRIC.name, viewModel.uiState.value.form.energyType)
        collector.cancel()
    }

    @Test
    fun `edit mode loads existing record`() = runTest {
        val record = sampleEnergyRecord()
        val viewModel = EnergyRecordViewModel(
            savedStateHandle = SavedStateHandle(mapOf("recordId" to record.id)),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = sampleEnergyVehicle()),
            energyRepository = FakeEnergyRepository(record = record),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals("编辑能源记录", viewModel.uiState.value.screenTitle)
        assertEquals("4567", viewModel.uiState.value.form.odometerKm)
        assertEquals("32.5", viewModel.uiState.value.form.quantityText)
        assertEquals("48.8", viewModel.uiState.value.form.amountText)
        assertEquals("PUBLIC_DC", viewModel.uiState.value.form.chargeMode)
        collector.cancel()
    }

    private data class AddCall(
        val vehicleId: Long,
        val energyType: String,
        val odometerKm: Int,
        val quantityInThousandth: Long,
        val amountInCent: Long,
        val isFull: Boolean,
        val fuelLabel: String?,
        val chargeMode: String?,
        val stationName: String?,
        val note: String?,
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

    private class FakeEnergyRepository(
        private val record: EnergyRecord? = null,
    ) : EnergyRepository {
        val addCalls = mutableListOf<AddCall>()

        override fun observeRecords(vehicleId: Long): Flow<List<EnergyRecord>> = flowOf(emptyList())

        override fun observeLatest(vehicleId: Long): Flow<EnergyRecord?> = flowOf(null)

        override suspend fun getRecord(recordId: Long): EnergyRecord? = record

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
        ): Long {
            addCalls += AddCall(
                vehicleId = vehicleId,
                energyType = energyType,
                odometerKm = odometerKm,
                quantityInThousandth = quantityInThousandth,
                amountInCent = amountInCent,
                isFull = isFull,
                fuelLabel = fuelLabel,
                chargeMode = chargeMode,
                stationName = stationName,
                note = note,
            )
            return addCalls.size.toLong()
        }

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
}

private fun sampleEnergyVehicle(): Vehicle = Vehicle(
    id = 1L,
    brand = "小鹏",
    model = "G6",
    plateNumber = "沪C12345",
    purchaseDate = null,
    powertrainType = VehiclePowertrainType.EV,
    note = null,
    isDefault = true,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

private fun sampleEnergyRecord(): EnergyRecord = EnergyRecord(
    id = 8L,
    vehicleId = 1L,
    occurredAt = Instant.EPOCH,
    odometerKm = 4567,
    totalCost = Money(4880L),
    detail = ElectricEnergyDetail(
        quantityInThousandth = 32500L,
        isFull = true,
        stationName = "特来电",
        chargeMode = ChargeMode.PUBLIC_DC,
    ),
    note = "晚间快充",
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)
