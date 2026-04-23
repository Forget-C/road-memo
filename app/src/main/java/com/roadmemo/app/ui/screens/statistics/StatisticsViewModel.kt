package com.roadmemo.app.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.ExpenseRecord
import com.roadmemo.app.domain.model.MaintenanceRecord
import com.roadmemo.app.domain.model.Money
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
import java.time.Instant
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class StatisticsUiState(
    val vehicleSummary: String = "暂无默认车辆",
    val monthlyTotalText: String = "¥0.00",
    val monthlyBreakdownText: String = "暂无记录",
    val monthTrendItems: List<String> = emptyList(),
    val categorySummaryItems: List<String> = emptyList(),
    val recordCountText: String = "共 0 条记录",
    val recentHighlights: List<String> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel @Inject constructor(
    vehicleRepository: VehicleRepository,
    private val energyRepository: EnergyRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val expenseRepository: ExpenseRepository,
    private val renewalRepository: RenewalRepository,
) : ViewModel() {

    val uiState = vehicleRepository.observeDefaultVehicle().flatMapLatest { vehicle ->
        if (vehicle == null) {
            flowOf(StatisticsUiState())
        } else {
            combine(
                energyRepository.observeRecords(vehicle.id),
                maintenanceRepository.observeRecords(vehicle.id),
                expenseRepository.observeRecords(vehicle.id),
                renewalRepository.observeRecords(vehicle.id),
            ) { energy, maintenance, expense, renewal ->
                vehicle.toStatisticsUiState(
                    energy = energy,
                    maintenance = maintenance,
                    expense = expense,
                    renewal = renewal,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState(isLoading = true),
    )
}

private fun Vehicle.toStatisticsUiState(
    energy: List<EnergyRecord>,
    maintenance: List<MaintenanceRecord>,
    expense: List<ExpenseRecord>,
    renewal: List<RenewalRecord>,
): StatisticsUiState {
    val currentMonth = YearMonth.now()
    val energyTotal = energy.sumOfMonth(currentMonth) { it.occurredAt.toEpochMilli() to it.totalCost.amountInCent }
    val maintenanceTotal = maintenance.sumOfMonth(currentMonth) { it.occurredAt.toEpochMilli() to it.amount.amountInCent }
    val expenseTotal = expense.sumOfMonth(currentMonth) { it.occurredAt.toEpochMilli() to it.amount.amountInCent }
    val renewalTotal = renewal.sumOfMonth(currentMonth) { it.createdAt.toEpochMilli() to it.amount.amountInCent }
    val total = energyTotal + maintenanceTotal + expenseTotal + renewalTotal

    val recentMonths = (0..5).map { currentMonth.minusMonths(it.toLong()) }.reversed()
    val monthTrendItems = recentMonths.map { month ->
        val monthTotal =
            energy.sumOfMonth(month) { it.occurredAt.toEpochMilli() to it.totalCost.amountInCent } +
                maintenance.sumOfMonth(month) { it.occurredAt.toEpochMilli() to it.amount.amountInCent } +
                expense.sumOfMonth(month) { it.occurredAt.toEpochMilli() to it.amount.amountInCent } +
                renewal.sumOfMonth(month) { it.createdAt.toEpochMilli() to it.amount.amountInCent }
        "${month.year}-${month.monthValue.toString().padStart(2, '0')} / ${Money(monthTotal).toCurrencyText()}"
    }

    val categorySummaryItems = buildList {
        add("能源 / ${Money(energy.sumOf { it.totalCost.amountInCent }).toCurrencyText()} / ${energy.size} 条")
        add("保养 / ${Money(maintenance.sumOf { it.amount.amountInCent }).toCurrencyText()} / ${maintenance.size} 条")
        add("费用 / ${Money(expense.sumOf { it.amount.amountInCent }).toCurrencyText()} / ${expense.size} 条")
        add("续期 / ${Money(renewal.sumOf { it.amount.amountInCent }).toCurrencyText()} / ${renewal.size} 条")
    }

    val recentHighlights = buildList {
        energy.maxByOrNull { it.occurredAt }?.let { record ->
            val quantity = record.detail.quantityInThousandth / 1000.0
            val unit = if (record.detail.energyType.name == "FUEL") "L" else "kWh"
            add("最近补能：${record.occurredAt.toDateTimeText()} / $quantity $unit / ${record.totalCost.toCurrencyText()}")
        }
        maintenance.maxByOrNull { it.occurredAt }?.let { record ->
            add("最近保养：${record.occurredAt.toDateText()} / ${record.maintenanceType.name} / ${record.amount.toCurrencyText()}")
        }
        expense.maxByOrNull { it.occurredAt }?.let { record ->
            add("最近费用：${record.occurredAt.toDateText()} / ${record.category.name} / ${record.amount.toCurrencyText()}")
        }
        renewal.minByOrNull { it.validUntil }?.let { record ->
            add("最近到期：${record.type.name} / ${record.validUntil} / ${record.amount.toCurrencyText()}")
        }
    }

    return StatisticsUiState(
        vehicleSummary = buildString {
            append(brand)
            append(' ')
            append(model)
            if (!plateNumber.isNullOrBlank()) {
                append(" · ")
                append(plateNumber)
            }
        },
        monthlyTotalText = Money(total).toCurrencyText(),
        monthlyBreakdownText = "能源 ${Money(energyTotal).toCurrencyText()} · 保养 ${Money(maintenanceTotal).toCurrencyText()} · 费用 ${Money(expenseTotal).toCurrencyText()} · 续期 ${Money(renewalTotal).toCurrencyText()}",
        monthTrendItems = monthTrendItems,
        categorySummaryItems = categorySummaryItems,
        recordCountText = "共 ${energy.size + maintenance.size + expense.size + renewal.size} 条记录，其中能源 ${energy.size}、保养 ${maintenance.size}、费用 ${expense.size}、续期 ${renewal.size}",
        recentHighlights = recentHighlights,
        isLoading = false,
    )
}

private fun <T> List<T>.sumOfMonth(
    yearMonth: YearMonth,
    extractor: (T) -> Pair<Long, Long>,
): Long = sumOf { item ->
    val (epochMillis, amountInCent) = extractor(item)
    val itemYearMonth = YearMonth.from(Instant.ofEpochMilli(epochMillis).atZone(java.time.ZoneId.systemDefault()))
    if (itemYearMonth == yearMonth) amountInCent else 0L
}
