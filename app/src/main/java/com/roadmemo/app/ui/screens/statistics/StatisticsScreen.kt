package com.roadmemo.app.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.ui.components.RoadMemoSection

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
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
                    text = "统计",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = uiState.vehicleSummary,
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        text = uiState.monthlyBreakdownText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "近 6 个月趋势") {
                if (uiState.monthTrendItems.isEmpty()) {
                    Text("还没有形成趋势数据")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.monthTrendItems) { item ->
                            TrendCard(item = item)
                        }
                    }
                }
            }
        }

        item {
            RoadMemoSection(title = "分类汇总") {
                if (uiState.categorySummaryItems.isEmpty()) {
                    Text("还没有可汇总的分类记录")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.categorySummaryItems.forEachIndexed { index, item ->
                            CategoryRow(
                                title = item,
                                ratio = when (index) {
                                    0 -> 1f
                                    1 -> 0.82f
                                    2 -> 0.68f
                                    else -> 0.54f
                                },
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    title = "记录规模",
                    value = uiState.recordCountText,
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    title = "当前状态",
                    value = "默认按当前车辆统计",
                )
            }
        }

        item {
            RoadMemoSection(title = "最近表现") {
                if (uiState.recentHighlights.isEmpty()) {
                    Text("暂无最近记录")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.recentHighlights.forEach { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                                ),
                            ) {
                                Text(
                                    text = item,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            RoadMemoSection(title = "当前说明") {
                Text("V0.1 统计页先围绕默认车辆做真实汇总，多车对比和能耗曲线下一轮继续补。")
            }
        }
    }
}

@Composable
private fun TrendCard(
    item: String,
) {
    Card(
        modifier = Modifier.width(148.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val parts = item.split(" / ")
            Text(
                text = parts.firstOrNull().orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = parts.getOrNull(1).orEmpty(),
                style = MaterialTheme.typography.titleMedium,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.small),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.64f)
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    title: String,
    ratio: Float,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
}
