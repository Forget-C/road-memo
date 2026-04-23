package com.roadmemo.app.ui.screens.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
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
import com.roadmemo.app.domain.model.MaintenanceType
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoDateField
import com.roadmemo.app.ui.components.RoadMemoFeedbackMessage
import com.roadmemo.app.ui.components.RoadMemoFeedbackTone
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSubmitButton
import com.roadmemo.app.ui.components.RoadMemoTextField
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
    val amountError = uiState.errorMessage.takeIf { it == "请输入有效金额" }
    val odometerError = uiState.errorMessage.takeIf { it == "请输入有效里程" }
    val nextDueDateError = uiState.errorMessage.takeIf {
        it == "下次保养日期格式不正确，格式如 2026-12-31"
    }
    val nextDueOdometerError = uiState.errorMessage.takeIf { it == "请输入有效的下次保养里程" }

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
                    RoadMemoTextField(
                        value = uiState.form.amountText,
                        onValueChange = viewModel::updateAmount,
                        label = "金额（元）",
                        singleLine = true,
                        keyboardType = KeyboardType.Decimal,
                        isError = amountError != null,
                        supportingText = amountError,
                    )
                    RoadMemoTextField(
                        value = uiState.form.odometerKm,
                        onValueChange = viewModel::updateOdometer,
                        label = "里程（可选）",
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        isError = odometerError != null,
                        supportingText = odometerError,
                    )
                    RoadMemoTextField(
                        value = uiState.form.storeName,
                        onValueChange = viewModel::updateStoreName,
                        label = "门店名称（可选）",
                        singleLine = true,
                        capitalization = KeyboardCapitalization.Words,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "下次计划") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoadMemoDateField(
                        value = uiState.form.nextDueDateText,
                        onValueChange = viewModel::updateNextDueDate,
                        label = "下次保养日期（YYYY-MM-DD，可选）",
                        placeholder = "例如 2026-05-01",
                        supportingText = nextDueDateError ?: "点击选择日期，便于后续提醒",
                        isError = nextDueDateError != null,
                    )
                    RoadMemoTextField(
                        value = uiState.form.nextDueOdometerKm,
                        onValueChange = viewModel::updateNextDueOdometer,
                        label = "下次保养里程（可选）",
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        isError = nextDueOdometerError != null,
                        supportingText = nextDueOdometerError,
                    )
                    RoadMemoTextField(
                        value = uiState.form.note,
                        onValueChange = viewModel::updateNote,
                        label = "备注（可选）",
                        capitalization = KeyboardCapitalization.Sentences,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "保存") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.errorMessage?.takeUnless {
                        it == amountError ||
                            it == odometerError ||
                            it == nextDueDateError ||
                            it == nextDueOdometerError
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

private fun String.toMaintenanceLabel(): String = when (this) {
    MaintenanceType.MINOR_SERVICE.name -> "小保养"
    MaintenanceType.MAJOR_SERVICE.name -> "大保养"
    MaintenanceType.ENGINE_OIL.name -> "更换机油"
    MaintenanceType.TIRE.name -> "轮胎"
    else -> this
}
