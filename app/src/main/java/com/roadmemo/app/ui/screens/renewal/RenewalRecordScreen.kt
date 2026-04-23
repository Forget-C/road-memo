package com.roadmemo.app.ui.screens.renewal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.domain.model.RenewalType
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoInlineError
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSubmitButton
import com.roadmemo.app.ui.components.RoadMemoVehicleSummaryCard

private val renewalTypes = listOf(
    RenewalType.INSURANCE,
    RenewalType.INSPECTION,
    RenewalType.TAX,
    RenewalType.OTHER,
)

@Composable
fun RenewalRecordScreen(
    onComplete: (() -> Unit)? = null,
    viewModel: RenewalRecordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTabIndex = renewalTypes.indexOf(uiState.renewalType).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RoadMemoFormHeader(
                title = uiState.screenTitle,
                description = "管理保险、年检和车船税到期时间，并自动生成后续提醒。",
            )
        }

        item {
            RoadMemoVehicleSummaryCard(
                summary = uiState.vehicleSummary,
                supportingText = "续期事项会进入提醒页，并支持后续跳回来源处理。",
            )
        }

        item {
            RoadMemoSection(title = "续期信息") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                        renewalTypes.forEachIndexed { index, type ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { viewModel.updateType(type) },
                                text = { Text(type.toLabel()) },
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.providerName,
                        onValueChange = viewModel::updateProviderName,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("机构 / 服务方") },
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = uiState.policyNumber,
                        onValueChange = viewModel::updatePolicyNumber,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("保单号 / 业务编号（可选）") },
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = uiState.amountText,
                        onValueChange = viewModel::updateAmount,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("金额（元）") },
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = uiState.validFromText,
                        onValueChange = viewModel::updateValidFrom,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("生效日期（YYYY-MM-DD，可选）") },
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = uiState.validUntilText,
                        onValueChange = viewModel::updateValidUntil,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("到期日期（YYYY-MM-DD）") },
                        singleLine = true,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "提醒与备注") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("到期提醒")
                            Text(
                                text = if (uiState.reminderEnabled) {
                                    "保存后自动生成提醒"
                                } else {
                                    "本次不生成提醒"
                                },
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = uiState.reminderEnabled,
                            onCheckedChange = viewModel::updateReminderEnabled,
                        )
                    }

                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = viewModel::updateNote,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("备注（可选）") },
                        minLines = 3,
                    )

                    uiState.errorMessage?.let { message ->
                        RoadMemoInlineError(message = message)
                    }

                    RoadMemoSubmitButton(
                        text = if (uiState.isSaving) "保存中..." else uiState.submitLabel,
                        enabled = !uiState.isSaving,
                        onClick = { viewModel.submit(onComplete) },
                    )
                }
            }
        }
    }
}

private fun RenewalType.toLabel(): String = when (this) {
    RenewalType.INSURANCE -> "保险"
    RenewalType.INSPECTION -> "年检"
    RenewalType.TAX -> "车船税"
    RenewalType.OTHER -> "其他"
}
