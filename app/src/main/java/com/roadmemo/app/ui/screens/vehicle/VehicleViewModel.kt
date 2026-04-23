package com.roadmemo.app.ui.screens.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VehicleFormState(
    val brand: String = "",
    val model: String = "",
    val plateNumber: String = "",
    val powertrainType: String = "PHEV",
    val note: String = "",
)

data class VehicleUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val form: VehicleFormState = VehicleFormState(),
    val canSubmit: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

private data class VehicleMetaState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(VehicleFormState())
    private val metaState = MutableStateFlow(VehicleMetaState())

    val uiState: StateFlow<VehicleUiState> = combine(
        vehicleRepository.observeVehicles(),
        formState,
        metaState,
    ) { vehicles, form, meta ->
        VehicleUiState(
            vehicles = vehicles,
            form = form,
            canSubmit = form.brand.isNotBlank() && form.model.isNotBlank(),
            isSaving = meta.isSaving,
            errorMessage = meta.errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VehicleUiState(),
    )

    fun updateBrand(value: String) {
        formState.value = formState.value.copy(brand = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateModel(value: String) {
        formState.value = formState.value.copy(model = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updatePlateNumber(value: String) {
        formState.value = formState.value.copy(plateNumber = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updatePowertrainType(value: String) {
        formState.value = formState.value.copy(powertrainType = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun updateNote(value: String) {
        formState.value = formState.value.copy(note = value)
        metaState.value = metaState.value.copy(errorMessage = null)
    }

    fun submitVehicle(onComplete: () -> Unit = {}) {
        val form = formState.value
        if (form.brand.isBlank()) {
            metaState.value = metaState.value.copy(errorMessage = "请输入车辆品牌")
            return
        }
        if (form.model.isBlank()) {
            metaState.value = metaState.value.copy(errorMessage = "请输入车辆车型")
            return
        }

        viewModelScope.launch {
            metaState.value = VehicleMetaState(isSaving = true, errorMessage = null)
            vehicleRepository.addVehicle(
                brand = form.brand,
                model = form.model,
                plateNumber = form.plateNumber,
                powertrainType = form.powertrainType,
                note = form.note,
            )
            formState.value = VehicleFormState(powertrainType = form.powertrainType)
            metaState.value = VehicleMetaState()
            onComplete()
        }
    }

    fun setDefaultVehicle(vehicleId: Long) {
        viewModelScope.launch {
            vehicleRepository.setDefaultVehicle(vehicleId)
        }
    }
}
