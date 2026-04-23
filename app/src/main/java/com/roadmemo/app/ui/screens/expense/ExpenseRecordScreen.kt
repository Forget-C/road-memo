package com.roadmemo.app.ui.screens.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.domain.model.ExpenseCategory
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoInlineError
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSubmitButton
import com.roadmemo.app.ui.components.RoadMemoVehicleSummaryCard

private val expenseCategories = listOf(
    ExpenseCategory.REPAIR,
    ExpenseCategory.PARKING,
    ExpenseCategory.TOLL,
    ExpenseCategory.CAR_WASH,
    ExpenseCategory.TRAFFIC_FINE,
    ExpenseCategory.ACCESSORY,
    ExpenseCategory.OTHER,
)

@Composable
fun ExpenseRecordScreen(
    onComplete: (() -> Unit)? = null,
    viewModel: ExpenseRecordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTabIndex = expenseCategories.indexOf(uiState.category).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RoadMemoFormHeader(
                title = uiState.screenTitle,
                description = "把停车、过路费、洗车等支出统一记进本月成本。",
            )
        }

        item {
            RoadMemoVehicleSummaryCard(
                summary = uiState.vehicleSummary,
                supportingText = "费用会进入首页摘要和分类统计。",
            )
        }

        item {
            RoadMemoSection(title = "费用分类") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                        expenseCategories.forEachIndexed { index, category ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { viewModel.updateCategory(category) },
                                text = { Text(category.toLabel()) },
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.amountText,
                        onValueChange = viewModel::updateAmount,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("金额（元）") },
                        singleLine = true,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "备注与保存") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

private fun ExpenseCategory.toLabel(): String = when (this) {
    ExpenseCategory.REPAIR -> "维修"
    ExpenseCategory.PARKING -> "停车"
    ExpenseCategory.TOLL -> "过路费"
    ExpenseCategory.CAR_WASH -> "洗车"
    ExpenseCategory.TRAFFIC_FINE -> "违章"
    ExpenseCategory.ACCESSORY -> "装饰改装"
    ExpenseCategory.OTHER -> "其他"
}
