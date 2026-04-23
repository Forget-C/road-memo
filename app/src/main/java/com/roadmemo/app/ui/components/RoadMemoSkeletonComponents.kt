package com.roadmemo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.roadmemo.app.ui.theme.RoadMemoSpacing

@Composable
fun RoadMemoSkeletonBlock(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)),
    )
}

@Composable
fun RoadMemoSkeletonCard(
    modifier: Modifier = Modifier,
    lineHeights: List<Dp> = listOf(14.dp, 28.dp, 14.dp),
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(RoadMemoSpacing.large),
            verticalArrangement = Arrangement.spacedBy(RoadMemoSpacing.small),
        ) {
            lineHeights.forEachIndexed { index, height ->
                RoadMemoSkeletonBlock(
                    height = height,
                    modifier = Modifier.fillMaxWidth(
                        fraction = when (index) {
                            0 -> 0.42f
                            1 -> 0.7f
                            else -> 1f
                        },
                    ),
                )
            }
        }
    }
}
