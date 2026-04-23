package com.roadmemo.app.ui.screens.statistics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.ui.components.RoadMemoEmptyListState
import com.roadmemo.app.ui.components.RoadMemoHeroPill
import com.roadmemo.app.ui.components.RoadMemoHeroSurface
import com.roadmemo.app.ui.components.RoadMemoBadgeTone
import com.roadmemo.app.ui.components.RoadMemoMiniInfoCard
import com.roadmemo.app.ui.components.RoadMemoScreenHeader
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSecondaryButton
import com.roadmemo.app.ui.components.RoadMemoSkeletonBlock
import com.roadmemo.app.ui.components.RoadMemoSkeletonCard
import com.roadmemo.app.ui.components.RoadMemoStatusBadge
import com.roadmemo.app.ui.theme.RoadMemoIcons

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RoadMemoScreenHeader(
                title = "统计",
                description = "汇总本月支出与变化",
                trailing = {
                    RoadMemoStatusBadge(
                        text = "本月",
                        tone = RoadMemoBadgeTone.NEUTRAL,
                    )
                },
            )
        }

        item {
            if (uiState.isLoading) {
                RoadMemoSkeletonCard(lineHeights = listOf(14.dp, 22.dp, 42.dp, 16.dp))
            } else {
                StatisticsHeroCard(
                    vehicleSummary = uiState.vehicleSummary,
                    monthlyTotalText = uiState.monthlyTotalText,
                    monthlyBreakdownText = uiState.monthlyBreakdownText,
                )
            }
        }

        item {
            if (uiState.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RoadMemoSkeletonBlock(
                        height = 92.dp,
                        modifier = Modifier.weight(1f),
                    )
                    RoadMemoSkeletonBlock(
                        height = 92.dp,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MiniStatCard(
                        modifier = Modifier.weight(1f),
                        title = "本月记录",
                        value = uiState.recordCountText,
                    )
                    MiniStatCard(
                        modifier = Modifier.weight(1f),
                        title = "当前范围",
                        value = "本月 · 默认车辆",
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "近 6 个月趋势") {
                if (uiState.isLoading) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(3) {
                            RoadMemoSkeletonBlock(
                                height = 132.dp,
                                modifier = Modifier.width(164.dp),
                            )
                        }
                    }
                } else if (uiState.monthTrendItems.isEmpty()) {
                    RoadMemoEmptyListState(
                        title = "暂无趋势数据",
                        description = "继续记录后，这里会逐步形成近 6 个月变化趋势。",
                        icon = RoadMemoIcons.Trend,
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.monthTrendItems) { item ->
                            TrendCard(item = item)
                        }
                    }
                }
            }
        }

        item {
            RoadMemoSection(title = "分类汇总") {
                if (uiState.isLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RoadMemoSkeletonCard(lineHeights = listOf(12.dp, 14.dp))
                        RoadMemoSkeletonCard(lineHeights = listOf(12.dp, 14.dp))
                        RoadMemoSkeletonCard(lineHeights = listOf(12.dp, 14.dp))
                    }
                } else if (uiState.categorySummaryItems.isEmpty()) {
                    RoadMemoEmptyListState(
                        title = "暂无分类汇总",
                        description = "有费用、保养、能源或续期记录后，这里会自动汇总分类表现。",
                        icon = RoadMemoIcons.Analytics,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.categorySummaryItems.forEachIndexed { index, item ->
                            CategoryRow(
                                title = item,
                                ratio = when (index) {
                                    0 -> 1f
                                    1 -> 0.78f
                                    2 -> 0.58f
                                    else -> 0.44f
                                },
                            )
                        }
                    }
                }
            }
        }

        item {
            RoadMemoSection(title = "最近表现") {
                if (uiState.isLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RoadMemoSkeletonCard(lineHeights = listOf(12.dp, 16.dp))
                        RoadMemoSkeletonCard(lineHeights = listOf(12.dp, 16.dp))
                    }
                } else if (uiState.recentHighlights.isEmpty()) {
                    RoadMemoEmptyListState(
                        title = "暂无最近记录",
                        description = "开始记录后，这里会显示最近补能、保养、费用和续期动态。",
                        icon = RoadMemoIcons.Vehicle,
                    )
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
            RoadMemoSection(title = "后续能力") {
                Text("后续会继续补多车对比、时间筛选和更完整的趋势图表，这一版先把默认车辆的核心月度账本跑通。")
            }
        }
    }
}

@Composable
private fun StatisticsHeroCard(
    vehicleSummary: String,
    monthlyTotalText: String,
    monthlyBreakdownText: String,
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
                            text = "当前统计对象",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                        Text(
                            text = vehicleSummary,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    RoadMemoStatusBadge(
                        text = "本月视图",
                        tone = RoadMemoBadgeTone.NEUTRAL,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "本月总支出",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Text(
                        text = monthlyTotalText,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = monthlyBreakdownText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HeroStatPill(icon = RoadMemoIcons.Analytics, text = "真实汇总")
                    HeroStatPill(icon = RoadMemoIcons.Trend, text = "趋势追踪")
                    HeroStatPill(icon = RoadMemoIcons.Vehicle, text = "默认")
                }
            }
    }
}

@Composable
private fun HeroStatPill(
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
private fun TrendCard(
    item: String,
) {
    Card(
        modifier = Modifier.width(168.dp),
        shape = RoundedCornerShape(24.dp),
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
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.64f)
                        .height(8.dp)
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
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
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
    RoadMemoMiniInfoCard(
        title = title,
        value = value,
        modifier = modifier,
    )
}
