package com.roadmemo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.roadmemo.app.ui.theme.RoadMemoRadius
import com.roadmemo.app.ui.theme.RoadMemoSpacing

@Composable
fun RoadMemoHeroSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(RoadMemoRadius.hero),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                            Color(0xFF7EB7E8),
                        ),
                    ),
                )
                .padding(RoadMemoSpacing.hero),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(RoadMemoSpacing.hero * 4.5f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
            )
            content()
        }
    }
}

@Composable
fun RoadMemoHeroPill(
    text: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RoadMemoRadius.pill))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = RoadMemoSpacing.medium, vertical = RoadMemoSpacing.small + RoadMemoSpacing.xxSmall),
        horizontalArrangement = Arrangement.spacedBy(RoadMemoSpacing.xSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun RoadMemoMetricCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(RoadMemoRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = RoadMemoSpacing.large,
                vertical = RoadMemoSpacing.large - RoadMemoSpacing.xxSmall,
            ),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun RoadMemoMiniInfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(RoadMemoRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(RoadMemoSpacing.large),
            verticalArrangement = Arrangement.spacedBy(RoadMemoSpacing.small),
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
