package com.roadmemo.app.ui.screens.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
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
import com.roadmemo.app.domain.model.MaintenanceType
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoInlineError
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSubmitButton
import com.roadmemo.app.ui.components.RoadMemoVehicleSummaryCard

private val maintenanceTabs = listOf(
    MaintenanceType.MINOR_SERVICE.name,
    MaintenanceType.MAJOR_SERVICE.name,
    MaintenanceType.ENGINE_OIL.name,
    MaintenanceType.TIRE.name,
)

@Composable
fun MaintenanceRecordScreen(
    onComplete: (() -> Unit)? = null,
    viewModel: MaintenanceRecordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember(uiState.form.maintenanceType) {
        mutableIntStateOf(maintenanceTabs.indexOf(uiState.form.maintenanceType).coerceAtLeast(0))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RoadMemoFormHeader(
                title = uiState.screenTitle,
                description = "记录保养成本和下次保养计划，后续会自动进入提醒链路。",
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
                        maintenanceTabs.forEachIndexed { index, label ->
                            Tab(
                                selected = index == selectedTabIndex,
                                onClick = {
                                    selectedTabIndex = index
                                    viewModel.updateMaintenanceType(label)
                                },
                                text = { Text(label.toMaintenanceLabel()) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = uiState.form.amountText,
                        onValueChange = viewModel::updateAmount,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("金额（元）") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.form.odometerKm,
                        onValueChange = viewModel::updateOdometer,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("里程（可选）") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.form.storeName,
                        onValueChange = viewModel::updateStoreName,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("门店名称（可选）") },
                        singleLine = true,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "下次计划") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.form.nextDueDateText,
                        onValueChange = viewModel::updateNextDueDate,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("下次保养日期（YYYY-MM-DD，可选）") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.form.nextDueOdometerKm,
                        onValueChange = viewModel::updateNextDueOdometer,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("下次保养里程（可选）") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.form.note,
                        onValueChange = viewModel::updateNote,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("备注（可选）") },
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "保存") {
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

private fun String.toMaintenanceLabel(): String = when (this) {
    MaintenanceType.MINOR_SERVICE.name -> "小保养"
    MaintenanceType.MAJOR_SERVICE.name -> "大保养"
    MaintenanceType.ENGINE_OIL.name -> "更换机油"
    MaintenanceType.TIRE.name -> "轮胎"
    else -> this
}
