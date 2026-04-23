package com.roadmemo.app.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.ExpenseRecord
import com.roadmemo.app.domain.model.MaintenanceRecord
import com.roadmemo.app.domain.model.RenewalRecord
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.repository.EnergyRepository
import com.roadmemo.app.domain.repository.ExpenseRepository
import com.roadmemo.app.domain.repository.MaintenanceRepository
import com.roadmemo.app.domain.repository.RenewalRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import com.roadmemo.app.ui.util.toCurrencyText
import com.roadmemo.app.ui.util.toDateText
import com.roadmemo.app.ui.util.toDateTimeText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class RecordListItem(
    val id: Long,
    val typeLabel: String,
    val title: String,
    val subtitle: String,
    val amountText: String,
)

data class RecordsUiState(
    val vehicleTitle: String = "暂无默认车辆",
    val energyRecords: List<RecordListItem> = emptyList(),
    val maintenanceRecords: List<RecordListItem> = emptyList(),
    val expenseRecords: List<RecordListItem> = emptyList(),
    val renewalRecords: List<RecordListItem> = emptyList(),
    val isEmpty: Boolean = true,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModel @Inject constructor(
    vehicleRepository: VehicleRepository,
    private val energyRepository: EnergyRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val expenseRepository: ExpenseRepository,
    private val renewalRepository: RenewalRepository,
) : ViewModel() {
    val uiState = vehicleRepository.observeDefaultVehicle().flatMapLatest { vehicle ->
        if (vehicle == null) {
            flowOf(RecordsUiState())
        } else {
            combine(
                energyRepository.observeRecords(vehicle.id),
                maintenanceRepository.observeRecords(vehicle.id),
                expenseRepository.observeRecords(vehicle.id),
                renewalRepository.observeRecords(vehicle.id),
            ) { energy, maintenance, expense, renewal ->
                vehicle.toRecordsUiState(energy, maintenance, expense, renewal)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordsUiState(),
    )

    fun deleteEnergyRecord(recordId: Long) {
        viewModelScope.launch { energyRepository.deleteRecord(recordId) }
    }

    fun deleteMaintenanceRecord(recordId: Long) {
        viewModelScope.launch { maintenanceRepository.deleteRecord(recordId) }
    }

    fun deleteExpenseRecord(recordId: Long) {
        viewModelScope.launch { expenseRepository.deleteRecord(recordId) }
    }

    fun deleteRenewalRecord(recordId: Long) {
        viewModelScope.launch { renewalRepository.deleteRecord(recordId) }
    }
}

private fun Vehicle.toRecordsUiState(
    energy: List<EnergyRecord>,
    maintenance: List<MaintenanceRecord>,
    expense: List<ExpenseRecord>,
    renewal: List<RenewalRecord>,
): RecordsUiState = RecordsUiState(
    vehicleTitle = "$brand $model",
    energyRecords = energy.map { record ->
        val quantityText = if (record.detail.energyType.name == "FUEL") {
            "${record.detail.quantityInThousandth / 1000.0} L"
        } else {
            "${record.detail.quantityInThousandth / 1000.0} kWh"
        }
        RecordListItem(
            id = record.id,
            typeLabel = if (record.detail.energyType.name == "FUEL") "能源 · 加油" else "能源 · 充电",
            title = quantityText,
            subtitle = "${record.occurredAt.toDateTimeText()} · 里程 ${record.odometerKm}",
            amountText = record.totalCost.toCurrencyText(),
        )
    },
    maintenanceRecords = maintenance.map { record ->
        RecordListItem(
            id = record.id,
            typeLabel = "保养",
            title = record.maintenanceType.name,
            subtitle = "${record.occurredAt.toDateText()}${record.storeName?.let { " · $it" }.orEmpty()}",
            amountText = record.amount.toCurrencyText(),
        )
    },
    expenseRecords = expense.map { record ->
        RecordListItem(
            id = record.id,
            typeLabel = "费用",
            title = record.category.name,
            subtitle = "${record.occurredAt.toDateText()}${record.note?.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()}",
            amountText = record.amount.toCurrencyText(),
        )
    },
    renewalRecords = renewal.map { record ->
        RecordListItem(
            id = record.id,
            typeLabel = "续期",
            title = record.type.name,
            subtitle = "到期 ${record.validUntil}${record.providerName?.let { " · $it" }.orEmpty()}",
            amountText = record.amount.toCurrencyText(),
        )
    },
    isEmpty = energy.isEmpty() && maintenance.isEmpty() && expense.isEmpty() && renewal.isEmpty(),
)
