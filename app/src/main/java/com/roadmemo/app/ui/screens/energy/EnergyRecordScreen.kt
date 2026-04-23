package com.roadmemo.app.ui.screens.energy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.domain.model.EnergyType
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoInlineError
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSubmitButton
import com.roadmemo.app.ui.components.RoadMemoVehicleSummaryCard

private val energyTabs = listOf(EnergyType.ELECTRIC.name, EnergyType.FUEL.name)

@Composable
fun EnergyRecordScreen(
    onComplete: (() -> Unit)? = null,
    viewModel: EnergyRecordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember(uiState.form.energyType) {
        mutableIntStateOf(energyTabs.indexOf(uiState.form.energyType).coerceAtLeast(0))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RoadMemoFormHeader(
                title = uiState.screenTitle,
                description = "记录一次加油或充电，首页和统计页会自动刷新。",
            )
        }

        item {
            val vehicle = uiState.defaultVehicle
            RoadMemoVehicleSummaryCard(
                summary = vehicle?.let { "${it.brand} ${it.model}" } ?: "当前没有默认车辆",
                supportingText = vehicle?.plateNumber?.takeIf { it.isNotBlank() }
                    ?: "请先添加车辆并设置默认车辆后再保存。",
            )
        }

        item {
            RoadMemoSection(title = "主要信息") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                        energyTabs.forEachIndexed { index, label ->
                            Tab(
                                selected = index == selectedTabIndex,
                                onClick = {
                                    selectedTabIndex = index
                                    viewModel.updateEnergyType(label)
                                },
                                text = {
                                    Text(if (label == EnergyType.ELECTRIC.name) "充电" else "加油")
                                },
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.form.odometerKm,
                        onValueChange = viewModel::updateOdometer,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("里程（km）") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.form.quantityText,
                        onValueChange = viewModel::updateQuantity,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(if (uiState.form.energyType == EnergyType.ELECTRIC.name) "电量（kWh）" else "油量（L）")
                        },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.form.amountText,
                        onValueChange = viewModel::updateAmount,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("总金额（元）") },
                        singleLine = true,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "补能细节") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (uiState.form.energyType == EnergyType.FUEL.name) {
                        OutlinedTextField(
                            value = uiState.form.fuelLabel,
                            onValueChange = viewModel::updateFuelLabel,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("油号") },
                            singleLine = true,
                        )
                    } else {
                        OutlinedTextField(
                            value = uiState.form.chargeMode,
                            onValueChange = viewModel::updateChargeMode,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("充电方式") },
                            singleLine = true,
                        )
                    }

                    OutlinedTextField(
                        value = uiState.form.stationName,
                        onValueChange = viewModel::updateStationName,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("站点名称（可选）") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.form.note,
                        onValueChange = viewModel::updateNote,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("备注（可选）") },
                    )

                    Card {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = uiState.form.isFull,
                                onCheckedChange = { viewModel.updateIsFull(it) },
                            )
                            Text("本次为补满/充满")
                        }
                    }
                }
            }
        }

        item {
            RoadMemoSection(title = "备注与保存") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.errorMessage?.let { message ->
                        RoadMemoInlineError(message = message)
                    }

                    RoadMemoSubmitButton(
                        text = if (uiState.isSaving) "保存中..." else uiState.submitLabel,
                        enabled = uiState.canSubmit && !uiState.isSaving,
                        onClick = { viewModel.submit { onComplete?.invoke() } },
                    )
                }
            }
        }
    }
}
