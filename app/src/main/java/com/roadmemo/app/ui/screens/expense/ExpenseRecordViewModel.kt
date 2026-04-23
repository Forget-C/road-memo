package com.roadmemo.app.ui.screens.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.domain.model.ExpenseCategory
import com.roadmemo.app.domain.model.ExpenseRecord
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.repository.ExpenseRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpenseRecordUiState(
    val recordId: Long? = null,
    val vehicleSummary: String = "暂无默认车辆",
    val category: ExpenseCategory = ExpenseCategory.PARKING,
    val amountText: String = "",
    val note: String = "",
    val screenTitle: String = "新增费用记录",
    val submitLabel: String = "保存费用记录",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ExpenseRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    vehicleRepository: VehicleRepository,
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {
    private val recordId: Long? = savedStateHandle.get<Long>("recordId")

    private val defaultVehicle = vehicleRepository.observeDefaultVehicle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val formState = MutableStateFlow(ExpenseRecordUiState())

    val uiState: StateFlow<ExpenseRecordUiState> = combine(defaultVehicle, formState) { vehicle, form ->
        form.copy(vehicleSummary = vehicle?.toSummary() ?: "暂无默认车辆")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseRecordUiState(),
    )

    init {
        if (recordId != null) {
            viewModelScope.launch {
                expenseRepository.getRecord(recordId)?.let { record ->
                    formState.value = record.toEditUiState()
                }
            }
        }
    }

    fun updateCategory(category: ExpenseCategory) {
        formState.value = formState.value.copy(category = category, errorMessage = null)
    }

    fun updateAmount(value: String) {
        formState.value = formState.value.copy(amountText = value, errorMessage = null)
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

        formState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            if (state.recordId == null) {
                expenseRepository.addRecord(
                    vehicleId = vehicle.id,
                    category = state.category.name,
                    amountInCent = amountInCent,
                    note = state.note,
                )
            } else {
                expenseRepository.updateRecord(
                    recordId = state.recordId,
                    category = state.category.name,
                    amountInCent = amountInCent,
                    note = state.note,
                )
            }
            formState.value = ExpenseRecordUiState()
            onComplete?.invoke()
        }
    }
}

private fun ExpenseRecord.toEditUiState(): ExpenseRecordUiState = ExpenseRecordUiState(
    recordId = id,
    category = category,
    amountText = (amount.amountInCent / 100.0).toString(),
    note = note.orEmpty(),
    screenTitle = "编辑费用记录",
    submitLabel = "保存修改",
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
