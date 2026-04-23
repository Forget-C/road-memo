package com.roadmemo.app.ui.screens.vehicle

import com.roadmemo.app.MainDispatcherRule
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.model.VehiclePowertrainType
import com.roadmemo.app.domain.repository.VehicleRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submit shows brand error when brand missing`() = runTest {
        val repository = FakeVehicleRepository()
        val viewModel = VehicleViewModel(repository)
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateModel("Model 3")
        viewModel.submitVehicle()
        advanceUntilIdle()

        assertEquals("请输入车辆品牌", viewModel.uiState.value.errorMessage)
        assertTrue(repository.addCalls.isEmpty())
        collector.cancel()
    }

    @Test
    fun `submit adds vehicle and resets form`() = runTest {
        val repository = FakeVehicleRepository()
        val viewModel = VehicleViewModel(repository)
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateBrand("特斯拉")
        viewModel.updateModel("Model Y")
        viewModel.updatePlateNumber("沪B12345")
        viewModel.updatePowertrainType("EV")
        viewModel.updateNote("家用")
        viewModel.submitVehicle()
        advanceUntilIdle()

        assertEquals(1, repository.addCalls.size)
        val call = repository.addCalls.single()
        assertEquals("特斯拉", call.brand)
        assertEquals("Model Y", call.model)
        assertEquals("沪B12345", call.plateNumber)
        assertEquals("EV", call.powertrainType)
        assertEquals("家用", call.note)
        assertEquals("", viewModel.uiState.value.form.brand)
        assertEquals("", viewModel.uiState.value.form.model)
        assertEquals("", viewModel.uiState.value.form.plateNumber)
        assertEquals("EV", viewModel.uiState.value.form.powertrainType)
        assertFalse(viewModel.uiState.value.isSaving)
        collector.cancel()
    }

    @Test
    fun `set default forwards vehicle id to repository`() = runTest {
        val repository = FakeVehicleRepository(
            initialVehicles = listOf(sampleVehicle(id = 1L), sampleVehicle(id = 2L, isDefault = false)),
        )
        val viewModel = VehicleViewModel(repository)
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.setDefaultVehicle(2L)
        advanceUntilIdle()

        assertEquals(listOf(2L), repository.defaultCalls)
        collector.cancel()
    }

    private data class AddCall(
        val brand: String,
        val model: String,
        val plateNumber: String?,
        val powertrainType: String,
        val note: String?,
    )

    private class FakeVehicleRepository(
        initialVehicles: List<Vehicle> = emptyList(),
    ) : VehicleRepository {
        private val vehicles = MutableStateFlow(initialVehicles)
        val addCalls = mutableListOf<AddCall>()
        val defaultCalls = mutableListOf<Long>()

        override fun observeVehicles(): Flow<List<Vehicle>> = vehicles

        override fun observeDefaultVehicle(): Flow<Vehicle?> =
            MutableStateFlow(vehicles.value.firstOrNull { it.isDefault })

        override suspend fun addVehicle(
            brand: String,
            model: String,
            plateNumber: String?,
            powertrainType: String,
            note: String?,
        ): Long {
            addCalls += AddCall(brand, model, plateNumber, powertrainType, note)
            return addCalls.size.toLong()
        }

        override suspend fun setDefaultVehicle(vehicleId: Long) {
            defaultCalls += vehicleId
        }
    }
}

private fun sampleVehicle(
    id: Long,
    isDefault: Boolean = true,
): Vehicle = Vehicle(
    id = id,
    brand = "比亚迪",
    model = "海豹",
    plateNumber = "沪A12345",
    purchaseDate = null,
    powertrainType = VehiclePowertrainType.EV,
    note = null,
    isDefault = isDefault,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)
