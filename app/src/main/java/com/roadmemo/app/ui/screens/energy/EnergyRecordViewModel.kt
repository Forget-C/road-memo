package com.roadmemo.app.ui.screens.energy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.domain.model.ElectricEnergyDetail
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.EnergyType
import com.roadmemo.app.domain.model.FuelEnergyDetail
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.repository.EnergyRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EnergyRecordFormState(
    val energyType: String = EnergyType.ELECTRIC.name,
    val odometerKm: String = "",
    val quantityText: String = "",
    val amountText: String = "",
    val isFull: Boolean = true,
    val fuelLabel: String = "92",
    val chargeMode: String = "HOME_AC",
    val stationName: String = "",
    val note: String = "",
)

data class EnergyRecordUiState(
    val recordId: Long? = null,
    val defaultVehicle: Vehicle? = null,
    val form: EnergyRecordFormState = EnergyRecordFormState(),
    val screenTitle: String = "新增能源记录",
    val submitLabel: String = "保存能源记录",
    val canSubmit: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

private data class EnergyRecordMetaState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class EnergyRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    vehicleRepository: VehicleRepository,
    private val energyRepository: EnergyRepository,
) : ViewModel() {
    private val recordId: Long? = savedStateHandle.get<Long>("recordId")
    private val formState = MutableStateFlow(EnergyRecordFormState())
    private val metaState = MutableStateFlow(EnergyRecordMetaState())

    val uiState: StateFlow<EnergyRecordUiState> = combine(
        vehicleRepository.observeDefaultVehicle(),
        formState,
        metaState,
    ) { vehicle, form, meta ->
        EnergyRecordUiState(
            recordId = recordId,
            defaultVehicle = vehicle,
            form = form,
            screenTitle = if (recordId == null) "新增能源记录" else "编辑能源记录",
            submitLabel = if (recordId == null) "保存能源记录" else "保存修改",
            canSubmit = vehicle != null &&
                (form.odometerKm.toIntOrNull()?.let { it >= 0 } == true) &&
                (form.quantityText.toDoubleOrNull()?.let { it > 0.0 } == true) &&
                (form.amountText.toDoubleOrNull()?.let { it > 0.0 } == true),
            isSaving = meta.isSaving,
            errorMessage = meta.errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EnergyRecordUiState(),
    )

    init {
        if (recordId != null) {
            viewModelScope.launch {
                energyRepository.getRecord(recordId)?.let { record ->
                    formState.value = record.toFormState()
                }
            }
        }
    }

    fun updateEnergyType(value: String) {
        formState.value = formState.value.copy(energyType = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateOdometer(value: String) {
        formState.value = formState.value.copy(odometerKm = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateQuantity(value: String) {
        formState.value = formState.value.copy(quantityText = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateAmount(value: String) {
        formState.value = formState.value.copy(amountText = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateIsFull(value: Boolean) {
        formState.value = formState.value.copy(isFull = value)
    }

    fun updateFuelLabel(value: String) {
        formState.value = formState.value.copy(fuelLabel = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateChargeMode(value: String) {
        formState.value = formState.value.copy(chargeMode = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateStationName(value: String) {
        formState.value = formState.value.copy(stationName = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateNote(value: String) {
        formState.value = formState.value.copy(note = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun submit(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val state = uiState.value
            val vehicle = state.defaultVehicle
            if (vehicle == null) {
                return@launch updateError("请先添加并设置默认车辆")
            }
            val odometer = state.form.odometerKm.toIntOrNull()
            if (odometer == null || odometer < 0) {
                return@launch updateError("请输入有效里程")
            }
            val quantity = state.form.quantityText.toDoubleOrNull()
            if (quantity == null || quantity <= 0.0) {
                return@launch updateError(
                    if (state.form.energyType == EnergyType.ELECTRIC.name) "请输入有效电量" else "请输入有效油量",
                )
            }
            val amount = state.form.amountText.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                return@launch updateError("请输入有效金额")
            }
            if (state.form.energyType == EnergyType.FUEL.name && state.form.fuelLabel.isBlank()) {
                return@launch updateError("请输入油号")
            }
            if (state.form.energyType == EnergyType.ELECTRIC.name && state.form.chargeMode.isBlank()) {
                return@launch updateError("请输入充电方式")
            }

            metaState.value = EnergyRecordMetaState(isSaving = true, errorMessage = null)

            if (state.recordId == null) {
                energyRepository.addRecord(
                    vehicleId = vehicle.id,
                    energyType = state.form.energyType,
                    odometerKm = odometer,
                    quantityInThousandth = (quantity * 1000).toLong(),
                    amountInCent = (amount * 100).toLong(),
                    isFull = state.form.isFull,
                    fuelLabel = if (state.form.energyType == EnergyType.FUEL.name) state.form.fuelLabel else null,
                    chargeMode = if (state.form.energyType == EnergyType.ELECTRIC.name) state.form.chargeMode else null,
                    stationName = state.form.stationName,
                    note = state.form.note,
                )
            } else {
                energyRepository.updateRecord(
                    recordId = state.recordId,
                    energyType = state.form.energyType,
                    odometerKm = odometer,
                    quantityInThousandth = (quantity * 1000).toLong(),
                    amountInCent = (amount * 100).toLong(),
                    isFull = state.form.isFull,
                    fuelLabel = if (state.form.energyType == EnergyType.FUEL.name) state.form.fuelLabel else null,
                    chargeMode = if (state.form.energyType == EnergyType.ELECTRIC.name) state.form.chargeMode else null,
                    stationName = state.form.stationName,
                    note = state.form.note,
                )
            }
            formState.value = EnergyRecordFormState(energyType = state.form.energyType)
            metaState.value = EnergyRecordMetaState()
            onComplete()
        }
    }

    private fun updateError(message: String) {
        metaState.value = metaState.value.copy(isSaving = false, errorMessage = message)
    }
}

private fun EnergyRecord.toFormState(): EnergyRecordFormState = EnergyRecordFormState(
    energyType = detail.energyType.name,
    odometerKm = odometerKm.toString(),
    quantityText = (detail.quantityInThousandth / 1000.0).toString(),
    amountText = (totalCost.amountInCent / 100.0).toString(),
    isFull = detail.isFull,
    fuelLabel = (detail as? FuelEnergyDetail)?.fuelLabel ?: "92",
    chargeMode = (detail as? ElectricEnergyDetail)?.chargeMode?.name ?: "HOME_AC",
    stationName = detail.stationName.orEmpty(),
    note = note.orEmpty(),
)
