package com.roadmemo.app.ui.screens.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.model.VehiclePowertrainType
import com.roadmemo.app.ui.components.RoadMemoFeedbackMessage
import com.roadmemo.app.ui.components.RoadMemoFeedbackTone
import com.roadmemo.app.ui.components.RoadMemoEmptyStateCard
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoPageScaffold
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSecondaryButton
import com.roadmemo.app.ui.components.RoadMemoSubmitButton
import com.roadmemo.app.ui.components.RoadMemoTextField
import com.roadmemo.app.ui.theme.RoadMemoIcons

private val powertrainTabs = listOf("GASOLINE", "DIESEL", "HEV", "PHEV", "EV")

@Composable
fun VehicleScreen(
    onBackToHome: (() -> Unit)? = null,
    viewModel: VehicleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPowertrainIndex by remember(uiState.form.powertrainType) {
        mutableIntStateOf(powertrainTabs.indexOf(uiState.form.powertrainType).coerceAtLeast(0))
    }
    val brandError = uiState.errorMessage.takeIf { it == "请输入车辆品牌" }
    val modelError = uiState.errorMessage.takeIf { it == "请输入车辆车型" }

    RoadMemoPageScaffold(
        title = "车辆管理",
        onBack = onBackToHome,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                end = 20.dp,
                bottom = 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                RoadMemoFormHeader(
                    title = "车辆管理",
                    description = "维护默认车辆和动力类型，后续所有记录都会跟随当前默认车辆。",
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "车辆概览",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = if (uiState.vehicles.isEmpty()) "还没有车辆，先添加第一辆车。" else "已记录 ${uiState.vehicles.size} 辆车",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = uiState.vehicles.firstOrNull { it.isDefault }?.let {
                                "当前默认：${it.brand} ${it.model}${it.plateNumber?.takeIf(String::isNotBlank)?.let { plate -> " · $plate" }.orEmpty()}"
                            } ?: "添加后会自动把第一辆车设为默认。",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            item {
                RoadMemoSection(title = "新增车辆") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RoadMemoTextField(
                            value = uiState.form.brand,
                            onValueChange = viewModel::updateBrand,
                            label = "品牌",
                            singleLine = true,
                            capitalization = KeyboardCapitalization.Words,
                            isError = brandError != null,
                            supportingText = brandError,
                        )
                        RoadMemoTextField(
                            value = uiState.form.model,
                            onValueChange = viewModel::updateModel,
                            label = "车型",
                            singleLine = true,
                            capitalization = KeyboardCapitalization.Words,
                            isError = modelError != null,
                            supportingText = modelError,
                        )
                        RoadMemoTextField(
                            value = uiState.form.plateNumber,
                            onValueChange = viewModel::updatePlateNumber,
                            label = "车牌号（可选）",
                            singleLine = true,
                            keyboardType = KeyboardType.Ascii,
                        )
                        SecondaryScrollableTabRow(selectedTabIndex = selectedPowertrainIndex) {
                            powertrainTabs.forEachIndexed { index, label ->
                                Tab(
                                    selected = index == selectedPowertrainIndex,
                                    onClick = {
                                        selectedPowertrainIndex = index
                                        viewModel.updatePowertrainType(label)
                                    },
                                    text = { Text(label.toPowertrainLabel()) },
                                )
                            }
                        }
                        RoadMemoTextField(
                            value = uiState.form.note,
                            onValueChange = viewModel::updateNote,
                            label = "备注（可选）",
                            capitalization = KeyboardCapitalization.Sentences,
                        )

                        uiState.errorMessage?.takeUnless {
                            it == brandError || it == modelError
                        }?.let { message ->
                            RoadMemoFeedbackMessage(
                                message = message,
                                tone = RoadMemoFeedbackTone.ERROR,
                            )
                        }

                        RoadMemoSubmitButton(
                            text = "保存车辆",
                            enabled = uiState.canSubmit,
                            isLoading = uiState.isSaving,
                            loadingText = "保存中...",
                            onClick = { viewModel.submitVehicle { onBackToHome?.invoke() } },
                        )
                    }
                }
            }

            item {
                RoadMemoSection(title = "已有车辆") {
                    if (uiState.vehicles.isEmpty()) {
                        RoadMemoEmptyStateCard(
                            title = "还没有车辆",
                            description = "先添加第一辆车，后续记录和统计都会围绕默认车辆展开。",
                            icon = RoadMemoIcons.Vehicle,
                        )
                    } else {
                        Text(
                            text = "默认车辆会优先出现在首页、记录和统计页。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(uiState.vehicles, key = { it.id }) { vehicle ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (vehicle.isDefault) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "${vehicle.brand} ${vehicle.model}",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    buildString {
                                        append(vehicle.powertrainType.toLabel())
                                        if (!vehicle.plateNumber.isNullOrBlank()) {
                                            append(" · ")
                                            append(vehicle.plateNumber)
                                        }
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (vehicle.isDefault) {
                                Text(
                                    text = "默认",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        vehicle.note?.takeIf { it.isNotBlank() }?.let { note ->
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        RoadMemoSecondaryButton(
                            text = if (vehicle.isDefault) "当前默认车辆" else "设为默认车辆",
                            onClick = { viewModel.setDefaultVehicle(vehicle.id) },
                            enabled = !vehicle.isDefault,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

private fun VehiclePowertrainType.toLabel(): String = when (this) {
    VehiclePowertrainType.GASOLINE -> "汽油"
    VehiclePowertrainType.DIESEL -> "柴油"
    VehiclePowertrainType.HEV -> "油混"
    VehiclePowertrainType.PHEV -> "插混"
    VehiclePowertrainType.EV -> "纯电"
}

private fun String.toPowertrainLabel(): String = when (this) {
    "GASOLINE" -> "汽油"
    "DIESEL" -> "柴油"
    "HEV" -> "油混"
    "PHEV" -> "插混"
    "EV" -> "纯电"
    else -> this
}

private fun Vehicle.toSummary(): String = buildString {
    append(brand)
    append(' ')
    append(model)
    if (!plateNumber.isNullOrBlank()) {
        append(" · ")
        append(plateNumber)
    }
}
