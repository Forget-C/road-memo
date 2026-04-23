package com.roadmemo.app.ui.screens.maintenance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.domain.model.MaintenanceRecord
import com.roadmemo.app.domain.model.MaintenanceType
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.repository.MaintenanceRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MaintenanceRecordFormState(
    val maintenanceType: String = MaintenanceType.MINOR_SERVICE.name,
    val amountText: String = "",
    val odometerKm: String = "",
    val storeName: String = "",
    val note: String = "",
    val nextDueDateText: String = "",
    val nextDueOdometerKm: String = "",
)

data class MaintenanceRecordUiState(
    val recordId: Long? = null,
    val defaultVehicle: Vehicle? = null,
    val form: MaintenanceRecordFormState = MaintenanceRecordFormState(),
    val screenTitle: String = "新增保养记录",
    val submitLabel: String = "保存保养记录",
    val canSubmit: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

private data class MaintenanceMetaState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class MaintenanceRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
) : ViewModel() {
    private val recordId: Long? = savedStateHandle.get<Long>("recordId")
    private val formState = MutableStateFlow(MaintenanceRecordFormState())
    private val metaState = MutableStateFlow(MaintenanceMetaState())

    val uiState: StateFlow<MaintenanceRecordUiState> = combine(
        vehicleRepository.observeDefaultVehicle(),
        formState,
        metaState,
    ) { vehicle, form, meta ->
        MaintenanceRecordUiState(
            recordId = recordId,
            defaultVehicle = vehicle,
            form = form,
            screenTitle = if (recordId == null) "新增保养记录" else "编辑保养记录",
            submitLabel = if (recordId == null) "保存保养记录" else "保存修改",
            canSubmit = vehicle != null &&
                (form.amountText.toDoubleOrNull()?.let { it > 0.0 } == true) &&
                form.maintenanceType.isNotBlank(),
            isSaving = meta.isSaving,
            errorMessage = meta.errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MaintenanceRecordUiState(),
    )

    init {
        if (recordId != null) {
            viewModelScope.launch {
                maintenanceRepository.getRecord(recordId)?.let { record ->
                    formState.value = record.toFormState()
                }
            }
        }
    }

    fun updateMaintenanceType(value: String) {
        formState.value = formState.value.copy(maintenanceType = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateAmount(value: String) {
        formState.value = formState.value.copy(amountText = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateOdometer(value: String) {
        formState.value = formState.value.copy(odometerKm = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateStoreName(value: String) {
        formState.value = formState.value.copy(storeName = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateNote(value: String) {
        formState.value = formState.value.copy(note = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateNextDueDate(value: String) {
        formState.value = formState.value.copy(nextDueDateText = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateNextDueOdometer(value: String) {
        formState.value = formState.value.copy(nextDueOdometerKm = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun submit(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val state = uiState.value
            val vehicle = state.defaultVehicle
            if (vehicle == null) {
                return@launch updateError("请先添加并设置默认车辆")
            }
            val amount = state.form.amountText.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                return@launch updateError("请输入有效金额")
            }
            val odometer = state.form.odometerKm.takeIf { it.isNotBlank() }?.toIntOrNull()
            if (state.form.odometerKm.isNotBlank() && odometer == null) {
                return@launch updateError("请输入有效里程")
            }
            val nextDueDateEpochDay = state.form.nextDueDateText
                .takeIf { it.isNotBlank() }
                ?.let { runCatching { LocalDate.parse(it).toEpochDay() }.getOrNull() }
            if (state.form.nextDueDateText.isNotBlank() && nextDueDateEpochDay == null) {
                return@launch updateError("下次保养日期格式不正确，格式如 2026-12-31")
            }
            if (state.form.nextDueOdometerKm.isNotBlank() && state.form.nextDueOdometerKm.toIntOrNull() == null) {
                return@launch updateError("请输入有效的下次保养里程")
            }

            metaState.value = MaintenanceMetaState(isSaving = true, errorMessage = null)

            if (state.recordId == null) {
                maintenanceRepository.addRecord(
                    vehicleId = vehicle.id,
                    maintenanceType = state.form.maintenanceType,
                    amountInCent = (amount * 100).toLong(),
                    odometerKm = odometer,
                    storeName = state.form.storeName,
                    note = state.form.note,
                    nextDueDateEpochDay = nextDueDateEpochDay,
                    nextDueOdometerKm = state.form.nextDueOdometerKm.toIntOrNull(),
                )
            } else {
                maintenanceRepository.updateRecord(
                    recordId = state.recordId,
                    maintenanceType = state.form.maintenanceType,
                    amountInCent = (amount * 100).toLong(),
                    odometerKm = odometer,
                    storeName = state.form.storeName,
                    note = state.form.note,
                    nextDueDateEpochDay = nextDueDateEpochDay,
                    nextDueOdometerKm = state.form.nextDueOdometerKm.toIntOrNull(),
                )
            }
            formState.value = MaintenanceRecordFormState(maintenanceType = state.form.maintenanceType)
            metaState.value = MaintenanceMetaState()
            onComplete()
        }
    }

    private fun updateError(message: String) {
        metaState.value = metaState.value.copy(isSaving = false, errorMessage = message)
    }
}

private fun MaintenanceRecord.toFormState(): MaintenanceRecordFormState = MaintenanceRecordFormState(
    maintenanceType = maintenanceType.name,
    amountText = (amount.amountInCent / 100.0).toString(),
    odometerKm = odometerKm?.toString().orEmpty(),
    storeName = storeName.orEmpty(),
    note = note.orEmpty(),
    nextDueDateText = nextDueDate?.toString().orEmpty(),
    nextDueOdometerKm = nextDueOdometerKm?.toString().orEmpty(),
)
