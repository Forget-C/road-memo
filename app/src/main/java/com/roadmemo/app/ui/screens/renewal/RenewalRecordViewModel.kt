package com.roadmemo.app.ui.screens.renewal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.domain.model.RenewalRecord
import com.roadmemo.app.domain.model.RenewalType
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.repository.RenewalRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RenewalRecordUiState(
    val recordId: Long? = null,
    val vehicleSummary: String = "暂无默认车辆",
    val renewalType: RenewalType = RenewalType.INSURANCE,
    val providerName: String = "",
    val policyNumber: String = "",
    val amountText: String = "",
    val validFromText: String = "",
    val validUntilText: String = "",
    val reminderEnabled: Boolean = true,
    val note: String = "",
    val screenTitle: String = "新增续期事项",
    val submitLabel: String = "保存续期事项",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class RenewalRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    vehicleRepository: VehicleRepository,
    private val renewalRepository: RenewalRepository,
) : ViewModel() {
    private val recordId: Long? = savedStateHandle.get<Long>("recordId")

    private val defaultVehicle = vehicleRepository.observeDefaultVehicle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val formState = MutableStateFlow(RenewalRecordUiState())

    val uiState: StateFlow<RenewalRecordUiState> = combine(defaultVehicle, formState) { vehicle, form ->
        form.copy(vehicleSummary = vehicle?.toSummary() ?: "暂无默认车辆")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RenewalRecordUiState(),
    )

    init {
        if (recordId != null) {
            viewModelScope.launch {
                renewalRepository.getRecord(recordId)?.let { record ->
                    formState.value = record.toEditUiState()
                }
            }
        }
    }

    fun updateType(type: RenewalType) {
        formState.value = formState.value.copy(renewalType = type, errorMessage = null)
    }

    fun updateProviderName(value: String) {
        formState.value = formState.value.copy(providerName = value, errorMessage = null)
    }

    fun updatePolicyNumber(value: String) {
        formState.value = formState.value.copy(policyNumber = value, errorMessage = null)
    }

    fun updateAmount(value: String) {
        formState.value = formState.value.copy(amountText = value, errorMessage = null)
    }

    fun updateValidFrom(value: String) {
        formState.value = formState.value.copy(validFromText = value, errorMessage = null)
    }

    fun updateValidUntil(value: String) {
        formState.value = formState.value.copy(validUntilText = value, errorMessage = null)
    }

    fun updateReminderEnabled(value: Boolean) {
        formState.value = formState.value.copy(reminderEnabled = value)
    }

    fun updateNote(value: String) {
        formState.value = formState.value.copy(note = value, errorMessage = null)
    }

    fun submit(onComplete: (() -> Unit)? = null) {
        val vehicle = defaultVehicle.value
        if (vehicle == null) {
            formState.value = formState.value.copy(errorMessage = "请先添加并设置默认车辆")
            return
        }

        val state = formState.value
        val amountInCent = parseAmountToCent(state.amountText)
        if (amountInCent == null || amountInCent <= 0L) {
            formState.value = state.copy(errorMessage = "请输入有效金额")
            return
        }

        val validUntil = parseDate(state.validUntilText)
        if (validUntil == null) {
            formState.value = state.copy(errorMessage = "请输入有效的到期日期，格式如 2026-12-31")
            return
        }

        val validFrom = state.validFromText.takeIf { it.isNotBlank() }?.let(::parseDate)
        if (state.validFromText.isNotBlank() && validFrom == null) {
            formState.value = state.copy(errorMessage = "生效日期格式不正确，格式如 2026-01-01")
            return
        }

        formState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            if (state.recordId == null) {
                renewalRepository.addRecord(
                    vehicleId = vehicle.id,
                    type = state.renewalType.name,
                    providerName = state.providerName,
                    policyNumber = state.policyNumber,
                    amountInCent = amountInCent,
                    validFromEpochDay = validFrom?.toEpochDay(),
                    validUntilEpochDay = validUntil.toEpochDay(),
                    reminderEnabled = state.reminderEnabled,
                    note = state.note,
                )
            } else {
                renewalRepository.updateRecord(
                    recordId = state.recordId,
                    type = state.renewalType.name,
                    providerName = state.providerName,
                    policyNumber = state.policyNumber,
                    amountInCent = amountInCent,
                    validFromEpochDay = validFrom?.toEpochDay(),
                    validUntilEpochDay = validUntil.toEpochDay(),
                    reminderEnabled = state.reminderEnabled,
                    note = state.note,
                )
            }
            formState.value = RenewalRecordUiState()
            onComplete?.invoke()
        }
    }
}

private fun RenewalRecord.toEditUiState(): RenewalRecordUiState = RenewalRecordUiState(
    recordId = id,
    renewalType = type,
    providerName = providerName.orEmpty(),
    policyNumber = policyNumber.orEmpty(),
    amountText = (amount.amountInCent / 100.0).toString(),
    validFromText = validFrom?.toString().orEmpty(),
    validUntilText = validUntil.toString(),
    reminderEnabled = reminderEnabled,
    note = note.orEmpty(),
    screenTitle = "编辑续期事项",
    submitLabel = "保存修改",
    isEditing = true,
)

private fun Vehicle.toSummary(): String = buildString {
    append(brand)
    append(' ')
    append(model)
    if (!plateNumber.isNullOrBlank()) {
        append(" · ")
        append(plateNumber)
    }
}

private fun parseAmountToCent(value: String): Long? =
    value.trim().takeIf { it.isNotBlank() }?.toBigDecimalOrNull()?.movePointRight(2)?.longValueExact()

private fun parseDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.trim()) }.getOrNull()
