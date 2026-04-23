package com.roadmemo.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.theme.RoadMemoWarning

@Composable
fun HomeScreen(
    onOpenVehicles: () -> Unit,
    onAddEnergyRecord: () -> Unit,
    onAddMaintenanceRecord: () -> Unit,
    onAddExpenseRecord: () -> Unit,
    onAddRenewalRecord: () -> Unit,
    onOpenReminders: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "当前车辆",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = uiState.vehicleTitle,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "本月总支出",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = uiState.monthlyTotalText,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = if (uiState.isEmpty) {
                            "还没有记录数据，下一步可以先添加第一辆车和第一条记录。"
                        } else {
                            "默认按当前车辆统计，本月数据会随车辆切换同步刷新。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    )
                    if (uiState.isEmpty) {
                        Button(onClick = onOpenVehicles) {
                            Text("添加第一辆车")
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                FilledTonalButton(onClick = onAddEnergyRecord) {
                                    Text("新增能源")
                                }
                            }
                            item {
                                FilledTonalButton(onClick = onAddMaintenanceRecord) {
                                    Text("新增保养")
                                }
                            }
                            item {
                                FilledTonalButton(onClick = onAddExpenseRecord) {
                                    Text("新增费用")
                                }
                            }
                            item {
                                FilledTonalButton(onClick = onAddRenewalRecord) {
                                    Text("新增续期")
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            RoadMemoSection(title = "分类摘要") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.summaryItems.forEach { item ->
                        item {
                            AssistChip(
                                onClick = {},
                                label = { Text(item) },
                                colors = AssistChipDefaults.assistChipColors(),
                            )
                        }
                    }
                }
            }
        }

        item {
            RoadMemoSection(title = "最近记录") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RecentLine(
                        title = "最近补能",
                        value = uiState.recentEnergyText ?: "暂无记录",
                    )
                    RecentLine(
                        title = "最近保养",
                        value = uiState.recentMaintenanceText ?: "暂无记录",
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "即将到期提醒") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.upcomingReminderTexts.isEmpty()) {
                        Text(
                            text = "暂无即将到期事项",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        uiState.upcomingReminderTexts.forEach { reminder ->
                            Text(
                                text = reminder,
                                color = RoadMemoWarning,
                            )
                        }
                    }
                    FilledTonalButton(onClick = onOpenReminders) {
                        Text("查看全部提醒")
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentLine(
    title: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
