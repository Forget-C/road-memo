package com.roadmemo.app.ui.screens.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.domain.model.Reminder
import com.roadmemo.app.domain.model.ReminderSourceType
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.repository.ReminderRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import com.roadmemo.app.ui.util.toDateText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReminderListItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val sourceType: ReminderSourceType?,
    val sourceId: Long?,
)

data class ReminderUiState(
    val vehicleSummary: String = "暂无默认车辆",
    val reminderCountText: String = "0 条提醒",
    val reminders: List<ReminderListItem> = emptyList(),
    val emptyText: String = "暂无即将到期提醒",
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderViewModel @Inject constructor(
    vehicleRepository: VehicleRepository,
    private val reminderRepository: ReminderRepository,
) : ViewModel() {

    val uiState = vehicleRepository.observeDefaultVehicle().flatMapLatest { vehicle ->
        if (vehicle == null) {
            flowOf(ReminderUiState())
        } else {
            reminderRepository.observeUpcoming(vehicle.id).map { reminders ->
                ReminderUiState(
                    vehicleSummary = vehicle.toSummary(),
                    reminderCountText = "${reminders.size} 条提醒",
                    reminders = reminders.map { it.toListItem() },
                    emptyText = "当前默认车辆还没有即将到期事项",
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReminderUiState(),
    )

    fun markDone(reminderId: Long) {
        viewModelScope.launch { reminderRepository.markDone(reminderId) }
    }

    fun dismiss(reminderId: Long) {
        viewModelScope.launch { reminderRepository.dismiss(reminderId) }
    }
}

private fun Vehicle.toSummary(): String = buildString {
    append(brand)
    append(' ')
    append(model)
    if (!plateNumber.isNullOrBlank()) {
        append(" · ")
        append(plateNumber)
    }
}

private fun Reminder.toListItem(): ReminderListItem {
    val remindDate = remindAt?.toDateText()
    return ReminderListItem(
        id = id,
        title = title,
        subtitle = if (remindDate == null) {
            "等待处理"
        } else {
            "提醒时间 $remindDate"
        },
        badgeText = when (sourceType) {
            ReminderSourceType.RENEWAL_RECORD -> "续期提醒"
            ReminderSourceType.MAINTENANCE_RECORD -> "保养提醒"
            ReminderSourceType.MANUAL -> "手动提醒"
            null -> "待处理"
        },
        sourceType = sourceType,
        sourceId = sourceId,
    )
}
