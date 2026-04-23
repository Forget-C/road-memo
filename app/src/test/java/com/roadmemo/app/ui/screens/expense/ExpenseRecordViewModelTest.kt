package com.roadmemo.app.ui.screens.expense

import androidx.lifecycle.SavedStateHandle
import com.roadmemo.app.MainDispatcherRule
import com.roadmemo.app.domain.model.ExpenseCategory
import com.roadmemo.app.domain.model.ExpenseRecord
import com.roadmemo.app.domain.model.Money
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.model.VehiclePowertrainType
import com.roadmemo.app.domain.repository.ExpenseRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseRecordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submit shows error when default vehicle missing`() = runTest {
        val vehicleRepository = FakeVehicleRepository(defaultVehicle = null)
        val expenseRepository = FakeExpenseRepository()
        val viewModel = ExpenseRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = vehicleRepository,
            expenseRepository = expenseRepository,
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateAmount("88.5")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("请先添加并设置默认车辆", viewModel.uiState.value.errorMessage)
        assertTrue(expenseRepository.addCalls.isEmpty())
        collector.cancel()
    }

    @Test
    fun `submit writes record and resets form when amount valid`() = runTest {
        val vehicle = sampleVehicle()
        val vehicleRepository = FakeVehicleRepository(defaultVehicle = vehicle)
        val expenseRepository = FakeExpenseRepository()
        val viewModel = ExpenseRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = vehicleRepository,
            expenseRepository = expenseRepository,
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateCategory(ExpenseCategory.TOLL)
        viewModel.updateAmount("25.50")
        viewModel.updateNote("高速收费")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, expenseRepository.addCalls.size)
        val call = expenseRepository.addCalls.single()
        assertEquals(vehicle.id, call.vehicleId)
        assertEquals("TOLL", call.category)
        assertEquals(2550L, call.amountInCent)
        assertEquals("高速收费", call.note)
        assertEquals("", viewModel.uiState.value.amountText)
        assertEquals("", viewModel.uiState.value.note)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(null, viewModel.uiState.value.errorMessage)
        collector.cancel()
    }

    private data class AddCall(
        val vehicleId: Long,
        val category: String,
        val amountInCent: Long,
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

    private class FakeExpenseRepository : ExpenseRepository {
        val addCalls = mutableListOf<AddCall>()

        override fun observeRecords(vehicleId: Long): Flow<List<ExpenseRecord>> = flowOf(emptyList())

        override suspend fun getRecord(recordId: Long): ExpenseRecord? = null

        override suspend fun addRecord(
            vehicleId: Long,
            category: String,
            amountInCent: Long,
            note: String?,
        ): Long {
            addCalls += AddCall(vehicleId, category, amountInCent, note)
            return addCalls.size.toLong()
        }

        override suspend fun updateRecord(
            recordId: Long,
            category: String,
            amountInCent: Long,
            note: String?,
        ) = Unit

        override suspend fun deleteRecord(recordId: Long) = Unit
    }
}

private fun sampleVehicle(): Vehicle = Vehicle(
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
