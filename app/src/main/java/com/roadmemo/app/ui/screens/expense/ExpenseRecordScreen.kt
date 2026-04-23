package com.roadmemo.app.ui.screens.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.domain.model.ExpenseCategory
import com.roadmemo.app.ui.components.RoadMemoFeedbackMessage
import com.roadmemo.app.ui.components.RoadMemoFeedbackTone
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSubmitButton
import com.roadmemo.app.ui.components.RoadMemoTextField
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
    val amountError = uiState.errorMessage.takeIf { it == "请输入有效金额" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
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

                    RoadMemoTextField(
                        value = uiState.amountText,
                        onValueChange = viewModel::updateAmount,
                        label = "金额（元）",
                        singleLine = true,
                        keyboardType = KeyboardType.Decimal,
                        isError = amountError != null,
                        supportingText = amountError,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "备注与保存") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoadMemoTextField(
                        value = uiState.note,
                        onValueChange = viewModel::updateNote,
                        label = "备注（可选）",
                        minLines = 3,
                        capitalization = KeyboardCapitalization.Sentences,
                    )

                    uiState.errorMessage?.takeUnless { it == amountError }?.let { message ->
                        RoadMemoFeedbackMessage(
                            message = message,
                            tone = RoadMemoFeedbackTone.ERROR,
                        )
                    }

                    RoadMemoSubmitButton(
                        text = uiState.submitLabel,
                        enabled = true,
                        isLoading = uiState.isSaving,
                        loadingText = "保存中...",
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
