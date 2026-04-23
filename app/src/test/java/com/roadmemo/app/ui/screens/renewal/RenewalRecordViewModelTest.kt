package com.roadmemo.app.ui.screens.renewal

import androidx.lifecycle.SavedStateHandle
import com.roadmemo.app.MainDispatcherRule
import com.roadmemo.app.domain.model.Money
import com.roadmemo.app.domain.model.RenewalRecord
import com.roadmemo.app.domain.model.RenewalType
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.model.VehiclePowertrainType
import com.roadmemo.app.domain.repository.RenewalRepository
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
class RenewalRecordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submit shows vehicle error when default vehicle missing`() = runTest {
        val viewModel = RenewalRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = null),
            renewalRepository = FakeRenewalRepository(),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateAmount("500")
        viewModel.updateValidUntil("2026-12-31")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("请先添加并设置默认车辆", viewModel.uiState.value.errorMessage)
        collector.cancel()
    }

    @Test
    fun `submit shows date error when valid until invalid`() = runTest {
        val viewModel = RenewalRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = sampleRenewalVehicle()),
            renewalRepository = FakeRenewalRepository(),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateAmount("680")
        viewModel.updateValidUntil("bad-date")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("请输入有效的到期日期，格式如 2026-12-31", viewModel.uiState.value.errorMessage)
        collector.cancel()
    }

    @Test
    fun `submit adds renewal record with parsed values`() = runTest {
        val repository = FakeRenewalRepository()
        val vehicle = sampleRenewalVehicle()
        val viewModel = RenewalRecordViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = vehicle),
            renewalRepository = repository,
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateType(RenewalType.INSPECTION)
        viewModel.updateProviderName("检测站")
        viewModel.updatePolicyNumber("XJ-2026")
        viewModel.updateAmount("680.50")
        viewModel.updateValidFrom("2026-01-01")
        viewModel.updateValidUntil("2026-12-31")
        viewModel.updateReminderEnabled(false)
        viewModel.updateNote("年度年检")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, repository.addCalls.size)
        val call = repository.addCalls.single()
        assertEquals(vehicle.id, call.vehicleId)
        assertEquals("INSPECTION", call.type)
        assertEquals("检测站", call.providerName)
        assertEquals("XJ-2026", call.policyNumber)
        assertEquals(68050L, call.amountInCent)
        assertEquals(LocalDate.parse("2026-01-01").toEpochDay(), call.validFromEpochDay)
        assertEquals(LocalDate.parse("2026-12-31").toEpochDay(), call.validUntilEpochDay)
        assertEquals(false, call.reminderEnabled)
        assertEquals("年度年检", call.note)
        assertEquals("", viewModel.uiState.value.amountText)
        assertEquals(true, viewModel.uiState.value.reminderEnabled)
        collector.cancel()
    }

    @Test
    fun `edit mode loads existing record`() = runTest {
        val record = sampleRenewalRecord()
        val viewModel = RenewalRecordViewModel(
            savedStateHandle = SavedStateHandle(mapOf("recordId" to record.id)),
            vehicleRepository = FakeVehicleRepository(defaultVehicle = sampleRenewalVehicle()),
            renewalRepository = FakeRenewalRepository(record = record),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEditing)
        assertEquals("编辑续期事项", viewModel.uiState.value.screenTitle)
        assertEquals("保司 A", viewModel.uiState.value.providerName)
        assertEquals("POL-001", viewModel.uiState.value.policyNumber)
        assertEquals("2026-12-31", viewModel.uiState.value.validUntilText)
        collector.cancel()
    }

    private data class AddCall(
        val vehicleId: Long,
        val type: String,
        val providerName: String?,
        val policyNumber: String?,
        val amountInCent: Long,
        val validFromEpochDay: Long?,
        val validUntilEpochDay: Long,
        val reminderEnabled: Boolean,
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

    private class FakeRenewalRepository(
        private val record: RenewalRecord? = null,
    ) : RenewalRepository {
        val addCalls = mutableListOf<AddCall>()

        override fun observeRecords(vehicleId: Long): Flow<List<RenewalRecord>> = flowOf(emptyList())

        override suspend fun getRecord(recordId: Long): RenewalRecord? = record

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
        ): Long {
            addCalls += AddCall(
                vehicleId = vehicleId,
                type = type,
                providerName = providerName,
                policyNumber = policyNumber,
                amountInCent = amountInCent,
                validFromEpochDay = validFromEpochDay,
                validUntilEpochDay = validUntilEpochDay,
                reminderEnabled = reminderEnabled,
                note = note,
            )
            return addCalls.size.toLong()
        }

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

private fun sampleRenewalVehicle(): Vehicle = Vehicle(
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

private fun sampleRenewalRecord(): RenewalRecord = RenewalRecord(
    id = 7L,
    vehicleId = 1L,
    type = RenewalType.INSURANCE,
    providerName = "保司 A",
    policyNumber = "POL-001",
    amount = Money(120000L),
    validFrom = LocalDate.parse("2026-01-01"),
    validUntil = LocalDate.parse("2026-12-31"),
    reminderEnabled = true,
    note = "商业险",
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)
