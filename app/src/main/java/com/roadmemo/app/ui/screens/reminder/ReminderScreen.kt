package com.roadmemo.app.ui.screens.reminder

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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.domain.model.ReminderSourceType
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoSection

@Composable
fun ReminderScreen(
    onOpenSource: (ReminderListItem) -> Unit = {},
    viewModel: ReminderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RoadMemoFormHeader(
                title = "提醒",
                description = "集中处理即将到期事项，支持忽略、标记完成，或直接回到来源记录继续操作。",
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
                        text = "提醒概览",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = uiState.reminderCountText,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = uiState.vehicleSummary,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "当前车辆") {
                Text(
                    text = "默认车辆的提醒会优先出现在这里，处理完成后会自动从列表中移除。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (uiState.reminders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "没有待处理提醒",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = uiState.emptyText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(uiState.reminders, key = { it.id }) { reminder ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = reminder.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = reminder.subtitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                        ) {
                            if (reminder.sourceType != null && reminder.sourceId != null) {
                                FilledTonalButton(onClick = { onOpenSource(reminder) }) {
                                    Text(sourceLabel(reminder.sourceType))
                                }
                            }
                            FilledTonalButton(onClick = { viewModel.dismiss(reminder.id) }) {
                                Text("忽略")
                            }
                            FilledTonalButton(onClick = { viewModel.markDone(reminder.id) }) {
                                Text("已处理")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sourceLabel(sourceType: ReminderSourceType): String = when (sourceType) {
    ReminderSourceType.RENEWAL_RECORD -> "查看来源"
    ReminderSourceType.MAINTENANCE_RECORD -> "查看来源"
    ReminderSourceType.MANUAL -> "查看来源"
}
