package com.roadmemo.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.ui.components.RoadMemoHeroPill
import com.roadmemo.app.ui.components.RoadMemoHeroSurface
import com.roadmemo.app.ui.components.RoadMemoListSectionHeader
import com.roadmemo.app.ui.components.RoadMemoMetricCard
import com.roadmemo.app.ui.components.RoadMemoScreenHeader
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSecondaryButton
import com.roadmemo.app.ui.components.RoadMemoSkeletonBlock
import com.roadmemo.app.ui.components.RoadMemoSkeletonCard
import com.roadmemo.app.ui.theme.RoadMemoIcons
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
            RoadMemoScreenHeader(
                title = "首页",
                description = "查看本月花费与最近动态",
                trailing = {
                    RoadMemoSecondaryButton(
                        text = "车辆",
                        onClick = onOpenVehicles,
                        icon = RoadMemoIcons.Vehicle,
                    )
                },
            )
        }

        item {
            if (uiState.isLoading) {
                RoadMemoSkeletonCard(lineHeights = listOf(14.dp, 22.dp, 22.dp, 16.dp))
            } else {
                HomeHeroCard(
                    appTitle = uiState.title,
                    vehicleTitle = uiState.vehicleTitle,
                    reminderCount = uiState.upcomingReminderTexts.size,
                    isEmpty = uiState.isEmpty,
                    onOpenVehicles = onOpenVehicles,
                    onOpenReminders = onOpenReminders,
                )
            }
        }

        item {
            if (uiState.isLoading) {
                RoadMemoSkeletonCard(lineHeights = listOf(14.dp, 42.dp, 16.dp, 18.dp))
            } else {
                Card(
                    shape = RoundedCornerShape(28.dp),
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
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = if (uiState.isEmpty) {
                                "先加一辆车，再记第一笔，首页就会开始形成你的本月账本。"
                            } else {
                                "当前默认车辆的本月花费，会随着新记录和切车实时更新。"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                        )
                        if (uiState.isEmpty) {
                            RoadMemoSecondaryButton(
                                text = "添加第一辆车",
                                onClick = onOpenVehicles,
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                item {
                                    QuickActionPill(label = "能源", onClick = onAddEnergyRecord)
                                }
                                item {
                                    QuickActionPill(label = "保养", onClick = onAddMaintenanceRecord)
                                }
                                item {
                                    QuickActionPill(label = "费用", onClick = onAddExpenseRecord)
                                }
                                item {
                                    QuickActionPill(label = "续期", onClick = onAddRenewalRecord)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RoadMemoListSectionHeader(title = "本月构成")
                if (uiState.isLoading) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(3) {
                            RoadMemoSkeletonBlock(
                                height = 96.dp,
                                modifier = Modifier.width(168.dp).height(96.dp),
                            )
                        }
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.summaryItems.forEach { item ->
                            item { SummaryMetricCard(text = item) }
                        }
                    }
                }
            }
        }

        item {
            RoadMemoSection(title = "最近记录") {
                if (uiState.isLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RoadMemoSkeletonCard(lineHeights = listOf(12.dp, 16.dp))
                        RoadMemoSkeletonCard(lineHeights = listOf(12.dp, 16.dp))
                    }
                } else {
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
        }

        item {
            RoadMemoSection(title = "即将到期提醒") {
                if (uiState.isLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RoadMemoSkeletonCard(lineHeights = listOf(12.dp, 16.dp))
                    }
                } else {
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
                        RoadMemoSecondaryButton(
                            text = "查看全部提醒",
                            onClick = onOpenReminders,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
    appTitle: String,
    vehicleTitle: String,
    reminderCount: Int,
    isEmpty: Boolean,
    onOpenVehicles: () -> Unit,
    onOpenReminders: () -> Unit,
) {
    RoadMemoHeroSurface {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = appTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                        Text(
                            text = "当前车辆",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.72f),
                        )
                        Text(
                            text = vehicleTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    RoadMemoSecondaryButton(
                        text = if (reminderCount == 0) "提醒" else "$reminderCount 条",
                        onClick = onOpenReminders,
                        icon = RoadMemoIcons.Reminder,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeroInfoChip(
                        icon = RoadMemoIcons.Timeline,
                        text = if (isEmpty) "先开始记第一笔" else "本月持续记账中",
                    )
                    RoadMemoSecondaryButton(
                        text = if (isEmpty) "添加" else "切车",
                        onClick = onOpenVehicles,
                        icon = RoadMemoIcons.Vehicle,
                    )
                }
            }
    }
}

@Composable
private fun HeroInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    RoadMemoHeroPill(
        text = text,
        leading = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(16.dp),
            )
        },
    )
}

@Composable
private fun QuickActionPill(
    label: String,
    onClick: () -> Unit,
) {
    RoadMemoSecondaryButton(
        text = "新增$label",
        onClick = onClick,
        icon = RoadMemoIcons.Add,
    )
}

@Composable
private fun SummaryMetricCard(text: String) {
    RoadMemoMetricCard(text = text)
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
