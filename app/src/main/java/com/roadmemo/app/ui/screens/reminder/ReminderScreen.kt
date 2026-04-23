package com.roadmemo.app.ui.screens.reminder

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.domain.model.ReminderSourceType
import com.roadmemo.app.ui.components.RoadMemoBadgeTone
import com.roadmemo.app.ui.components.RoadMemoEmptyStateCard
import com.roadmemo.app.ui.components.RoadMemoPageScaffold
import com.roadmemo.app.ui.components.RoadMemoSecondaryButton
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoStatusBadge
import com.roadmemo.app.ui.theme.RoadMemoIcons
import kotlinx.coroutines.launch

@Composable
fun ReminderScreen(
    onOpenSource: (ReminderListItem) -> Unit = {},
    onBack: (() -> Unit)? = null,
    viewModel: ReminderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    RoadMemoPageScaffold(
        title = "提醒",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
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
                    RoadMemoEmptyStateCard(
                        title = "没有待处理提醒",
                        description = uiState.emptyText,
                        icon = RoadMemoIcons.Reminder,
                    )
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RoadMemoStatusBadge(
                                    text = reminder.badgeText,
                                    tone = reminder.sourceType.toBadgeTone(),
                                )
                                if (reminder.sourceType != null && reminder.sourceId != null) {
                                    RoadMemoSecondaryButton(
                                        text = sourceLabel(reminder.sourceType),
                                        onClick = { onOpenSource(reminder) },
                                    )
                                }
                            }
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
                                RoadMemoSecondaryButton(
                                    text = "忽略",
                                    onClick = {
                                        viewModel.dismiss(reminder.id)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("已忽略该提醒")
                                        }
                                    },
                                )
                                RoadMemoSecondaryButton(
                                    text = "已处理",
                                    onClick = {
                                        viewModel.markDone(reminder.id)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("已标记为处理完成")
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sourceLabel(sourceType: ReminderSourceType): String = when (sourceType) {
    ReminderSourceType.RENEWAL_RECORD -> "来源"
    ReminderSourceType.MAINTENANCE_RECORD -> "来源"
    ReminderSourceType.MANUAL -> "来源"
}

private fun ReminderSourceType?.toBadgeTone(): RoadMemoBadgeTone = when (this) {
    ReminderSourceType.RENEWAL_RECORD -> RoadMemoBadgeTone.WARNING
    ReminderSourceType.MAINTENANCE_RECORD -> RoadMemoBadgeTone.PRIMARY
    ReminderSourceType.MANUAL -> RoadMemoBadgeTone.NEUTRAL
    null -> RoadMemoBadgeTone.NEUTRAL
}
