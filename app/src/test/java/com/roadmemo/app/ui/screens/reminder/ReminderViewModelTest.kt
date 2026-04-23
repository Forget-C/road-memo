package com.roadmemo.app.ui.screens.reminder

import com.roadmemo.app.MainDispatcherRule
import com.roadmemo.app.domain.model.Reminder
import com.roadmemo.app.domain.model.ReminderSourceType
import com.roadmemo.app.domain.model.ReminderStatus
import com.roadmemo.app.domain.model.ReminderType
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.model.VehiclePowertrainType
import com.roadmemo.app.domain.repository.ReminderRepository
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
class ReminderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ui state falls back to empty when default vehicle missing`() = runTest {
        val viewModel = ReminderViewModel(
            vehicleRepository = FakeVehicleRepository(defaultVehicle = null),
            reminderRepository = FakeReminderRepository(),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertEquals("暂无默认车辆", viewModel.uiState.value.vehicleSummary)
        assertEquals("0 条提醒", viewModel.uiState.value.reminderCountText)
        assertTrue(viewModel.uiState.value.reminders.isEmpty())
        collector.cancel()
    }

    @Test
    fun `ui state maps reminders for default vehicle`() = runTest {
        val vehicle = sampleVehicle()
        val reminders = listOf(
            sampleReminder(
                id = 1L,
                title = "车险即将到期",
                sourceType = ReminderSourceType.RENEWAL_RECORD,
                sourceId = 88L,
            ),
        )
        val viewModel = ReminderViewModel(
            vehicleRepository = FakeVehicleRepository(defaultVehicle = vehicle),
            reminderRepository = FakeReminderRepository(reminders = reminders),
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertEquals("比亚迪 宋 PLUS · 沪A12345", viewModel.uiState.value.vehicleSummary)
        assertEquals("1 条提醒", viewModel.uiState.value.reminderCountText)
        assertEquals(1, viewModel.uiState.value.reminders.size)
        assertEquals("车险即将到期", viewModel.uiState.value.reminders.first().title)
        assertEquals(ReminderSourceType.RENEWAL_RECORD, viewModel.uiState.value.reminders.first().sourceType)
        assertEquals(88L, viewModel.uiState.value.reminders.first().sourceId)
        collector.cancel()
    }

    @Test
    fun `mark done and dismiss forward to repository`() = runTest {
        val repository = FakeReminderRepository()
        val viewModel = ReminderViewModel(
            vehicleRepository = FakeVehicleRepository(defaultVehicle = sampleVehicle()),
            reminderRepository = repository,
        )
        val collector = backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.markDone(11L)
        viewModel.dismiss(22L)
        advanceUntilIdle()

        assertEquals(listOf(11L), repository.doneCalls)
        assertEquals(listOf(22L), repository.dismissCalls)
        collector.cancel()
    }

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

    private class FakeReminderRepository(
        reminders: List<Reminder> = emptyList(),
    ) : ReminderRepository {
        private val flow = MutableStateFlow(reminders)
        val doneCalls = mutableListOf<Long>()
        val dismissCalls = mutableListOf<Long>()

        override fun observeUpcoming(vehicleId: Long): Flow<List<Reminder>> = flow

        override suspend fun markDone(reminderId: Long) {
            doneCalls += reminderId
        }

        override suspend fun dismiss(reminderId: Long) {
            dismissCalls += reminderId
        }
    }
}

private fun sampleReminder(
    id: Long,
    title: String,
    sourceType: ReminderSourceType?,
    sourceId: Long?,
): Reminder = Reminder(
    id = id,
    vehicleId = 1L,
    type = ReminderType.INSURANCE,
    title = title,
    remindAt = Instant.parse("2026-05-01T10:00:00Z"),
    remindOdometerKm = null,
    advanceDays = 7,
    isEnabled = true,
    status = ReminderStatus.PENDING,
    sourceType = sourceType,
    sourceId = sourceId,
    note = null,
    lastTriggeredAt = null,
    completedAt = null,
    dismissedAt = null,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
)

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
