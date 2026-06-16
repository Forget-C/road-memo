package com.roadmemo.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.domain.EnergyConsumptionCalculator
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.ExpenseRecord
import com.roadmemo.app.domain.model.MaintenanceRecord
import com.roadmemo.app.domain.model.Reminder
import com.roadmemo.app.domain.model.RenewalRecord
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.repository.EnergyRepository
import com.roadmemo.app.domain.repository.ExpenseRepository
import com.roadmemo.app.domain.repository.MaintenanceRepository
import com.roadmemo.app.domain.repository.ReminderRepository
import com.roadmemo.app.domain.repository.RenewalRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import com.roadmemo.app.ui.util.toCurrencyText
import com.roadmemo.app.ui.util.toDateText
import com.roadmemo.app.ui.util.toDateTimeText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val title: String = "长路有记",
    val vehicleTitle: String = "暂无默认车辆",
    val monthlyTotalText: String = "¥0.00",
    val summaryItems: List<String> = emptyList(),
    val consumptionItems: List<Pair<String, String>> = emptyList(),
    val hasEnergyRecords: Boolean = false,
    val recentEnergyText: String? = null,
    val recentMaintenanceText: String? = null,
    val upcomingReminderTexts: List<String> = emptyList(),
    val isEmpty: Boolean = true,
    val isLoading: Boolean = false,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    vehicleRepository: VehicleRepository,
    private val energyRepository: EnergyRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val expenseRepository: ExpenseRepository,
    private val renewalRepository: RenewalRepository,
    private val reminderRepository: ReminderRepository,
    private val energyConsumptionCalculator: EnergyConsumptionCalculator,
) : ViewModel() {

    private val defaultVehicle: Flow<Vehicle?> = vehicleRepository.observeDefaultVehicle()

    val uiState = defaultVehicle.flatMapLatest { vehicle ->
        if (vehicle == null) {
            flowOf(HomeUiState())
        } else {
            combine(
                combine(
                    energyRepository.observeRecords(vehicle.id),
                    energyRepository.observeLatest(vehicle.id),
                    maintenanceRepository.observeRecords(vehicle.id),
                    maintenanceRepository.observeLatest(vehicle.id),
                ) { energyRecords, latestEnergy, maintenanceRecords, latestMaintenance ->
                    HomePrimaryData(
                        energyRecords = energyRecords,
                        latestEnergy = latestEnergy,
                        maintenanceRecords = maintenanceRecords,
                        latestMaintenance = latestMaintenance,
                    )
                },
                combine(
                    expenseRepository.observeRecords(vehicle.id),
                    renewalRepository.observeRecords(vehicle.id),
                    reminderRepository.observeUpcoming(vehicle.id),
                ) { expenseRecords, renewalRecords, reminders ->
                    HomeSecondaryData(
                        expenseRecords = expenseRecords,
                        renewalRecords = renewalRecords,
                        reminders = reminders,
                    )
                },
            ) { primary, secondary ->
                vehicle.toHomeUiState(
                    energyRecords = primary.energyRecords,
                    latestEnergy = primary.latestEnergy,
                    maintenanceRecords = primary.maintenanceRecords,
                    latestMaintenance = primary.latestMaintenance,
                    expenseRecords = secondary.expenseRecords,
                    renewalRecords = secondary.renewalRecords,
                    reminders = secondary.reminders,
                    calculator = energyConsumptionCalculator,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )
}

private data class HomePrimaryData(
    val energyRecords: List<EnergyRecord>,
    val latestEnergy: EnergyRecord?,
    val maintenanceRecords: List<MaintenanceRecord>,
    val latestMaintenance: MaintenanceRecord?,
)

private data class HomeSecondaryData(
    val expenseRecords: List<ExpenseRecord>,
    val renewalRecords: List<RenewalRecord>,
    val reminders: List<Reminder>,
)

private fun Vehicle.toHomeUiState(
    energyRecords: List<EnergyRecord>,
    latestEnergy: EnergyRecord?,
    maintenanceRecords: List<MaintenanceRecord>,
    latestMaintenance: MaintenanceRecord?,
    expenseRecords: List<ExpenseRecord>,
    renewalRecords: List<RenewalRecord>,
    reminders: List<Reminder>,
    calculator: EnergyConsumptionCalculator,
): HomeUiState {
    val currentMonth = YearMonth.now()
    val energyTotal = energyRecords.sumOfMonth(currentMonth) { it.occurredAt.toEpochMilli() to it.totalCost.amountInCent }
    val maintenanceTotal = maintenanceRecords.sumOfMonth(currentMonth) { it.occurredAt.toEpochMilli() to it.amount.amountInCent }
    val expenseTotal = expenseRecords.sumOfMonth(currentMonth) { it.occurredAt.toEpochMilli() to it.amount.amountInCent }
    val renewalTotal = renewalRecords.sumOfMonth(currentMonth) { it.createdAt.toEpochMilli() to it.amount.amountInCent }
    val total = energyTotal + maintenanceTotal + expenseTotal + renewalTotal
    val consumptionSummary = calculator.calculate(energyRecords)

    return HomeUiState(
        vehicleTitle = buildString {
            append(brand)
            append(' ')
            append(model)
            if (!plateNumber.isNullOrBlank()) {
                append(" · ")
                append(plateNumber)
            }
        },
        monthlyTotalText = com.roadmemo.app.domain.model.Money(total).toCurrencyText(),
        summaryItems = listOf(
            "能源 ${com.roadmemo.app.domain.model.Money(energyTotal).toCurrencyText()}",
            "保养 ${com.roadmemo.app.domain.model.Money(maintenanceTotal).toCurrencyText()}",
            "费用 ${com.roadmemo.app.domain.model.Money(expenseTotal).toCurrencyText()}",
            "续期 ${com.roadmemo.app.domain.model.Money(renewalTotal).toCurrencyText()}",
        ),
        consumptionItems = buildList {
            consumptionSummary.fuel?.latestText?.let { add("最近油耗" to it) }
            consumptionSummary.electric?.latestText?.let { add("最近电耗" to it) }
        },
        hasEnergyRecords = energyRecords.isNotEmpty(),
        recentEnergyText = latestEnergy?.let { record ->
            val quantityText = if (record.detail.energyType.name == "FUEL") {
                "${record.detail.quantityInThousandth / 1000.0} L"
            } else {
                "${record.detail.quantityInThousandth / 1000.0} kWh"
            }
            "${record.occurredAt.toDateTimeText()}  $quantityText / ${record.totalCost.toCurrencyText()}"
        },
        recentMaintenanceText = latestMaintenance?.let { record ->
            "${record.occurredAt.toDateText()}  ${record.maintenanceType.name} / ${record.amount.toCurrencyText()}"
        },
        upcomingReminderTexts = reminders.take(3).map { reminder ->
            reminder.remindAt?.let { "${reminder.title} · ${it.toDateText()}" } ?: reminder.title
        },
        isEmpty = energyRecords.isEmpty() && maintenanceRecords.isEmpty() && expenseRecords.isEmpty() && renewalRecords.isEmpty(),
        isLoading = false,
    )
}

private fun <T> List<T>.sumOfMonth(
    yearMonth: YearMonth,
    extractor: (T) -> Pair<Long, Long>,
): Long = sumOf { item ->
    val (epochMillis, amountInCent) = extractor(item)
    val itemYearMonth = YearMonth.from(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
    if (itemYearMonth == yearMonth) amountInCent else 0L
}
