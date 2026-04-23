package com.roadmemo.app.ui.screens.energy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.domain.model.EnergyType
import com.roadmemo.app.ui.components.RoadMemoFeedbackMessage
import com.roadmemo.app.ui.components.RoadMemoFeedbackTone
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSubmitButton
import com.roadmemo.app.ui.components.RoadMemoTextField
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
    val odometerError = uiState.errorMessage.takeIf { it == "请输入有效里程" }
    val quantityError = uiState.errorMessage.takeIf {
        it == "请输入有效电量" || it == "请输入有效油量"
    }
    val amountError = uiState.errorMessage.takeIf { it == "请输入有效金额" }
    val fuelLabelError = uiState.errorMessage.takeIf { it == "请输入油号" }
    val chargeModeError = uiState.errorMessage.takeIf { it == "请输入充电方式" }

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

                    RoadMemoTextField(
                        value = uiState.form.odometerKm,
                        onValueChange = viewModel::updateOdometer,
                        label = "里程（km）",
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        isError = odometerError != null,
                        supportingText = odometerError,
                    )
                    RoadMemoTextField(
                        value = uiState.form.quantityText,
                        onValueChange = viewModel::updateQuantity,
                        label = if (uiState.form.energyType == EnergyType.ELECTRIC.name) {
                            "电量（kWh）"
                        } else {
                            "油量（L）"
                        },
                        singleLine = true,
                        keyboardType = KeyboardType.Decimal,
                        isError = quantityError != null,
                        supportingText = quantityError,
                    )
                    RoadMemoTextField(
                        value = uiState.form.amountText,
                        onValueChange = viewModel::updateAmount,
                        label = "总金额（元）",
                        singleLine = true,
                        keyboardType = KeyboardType.Decimal,
                        isError = amountError != null,
                        supportingText = amountError,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "补能细节") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (uiState.form.energyType == EnergyType.FUEL.name) {
                        RoadMemoTextField(
                            value = uiState.form.fuelLabel,
                            onValueChange = viewModel::updateFuelLabel,
                            label = "油号",
                            singleLine = true,
                            keyboardType = KeyboardType.Number,
                            isError = fuelLabelError != null,
                            supportingText = fuelLabelError,
                        )
                    } else {
                        RoadMemoTextField(
                            value = uiState.form.chargeMode,
                            onValueChange = viewModel::updateChargeMode,
                            label = "充电方式",
                            singleLine = true,
                            capitalization = KeyboardCapitalization.Words,
                            isError = chargeModeError != null,
                            supportingText = chargeModeError,
                        )
                    }

                    RoadMemoTextField(
                        value = uiState.form.stationName,
                        onValueChange = viewModel::updateStationName,
                        label = "站点名称（可选）",
                        singleLine = true,
                        capitalization = KeyboardCapitalization.Words,
                    )
                    RoadMemoTextField(
                        value = uiState.form.note,
                        onValueChange = viewModel::updateNote,
                        label = "备注（可选）",
                        capitalization = KeyboardCapitalization.Sentences,
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
                    uiState.errorMessage?.takeUnless {
                        it == odometerError ||
                            it == quantityError ||
                            it == amountError ||
                            it == fuelLabelError ||
                            it == chargeModeError
                    }?.let { message ->
                        RoadMemoFeedbackMessage(
                            message = message,
                            tone = RoadMemoFeedbackTone.ERROR,
                        )
                    }

                    RoadMemoSubmitButton(
                        text = uiState.submitLabel,
                        enabled = uiState.canSubmit,
                        isLoading = uiState.isSaving,
                        loadingText = "保存中...",
                        onClick = { viewModel.submit { onComplete?.invoke() } },
                    )
                }
            }
        }
    }
}
