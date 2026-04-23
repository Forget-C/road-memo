package com.roadmemo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.roadmemo.app.ui.theme.RoadMemoSpacing
import com.roadmemo.app.ui.theme.RoadMemoWarning

enum class RoadMemoBadgeTone {
    PRIMARY,
    NEUTRAL,
    WARNING,
}

@Composable
fun RoadMemoStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: RoadMemoBadgeTone = RoadMemoBadgeTone.PRIMARY,
) {
    val containerColor = when (tone) {
        RoadMemoBadgeTone.PRIMARY ->
            MaterialTheme.colorScheme.primaryContainer
        RoadMemoBadgeTone.NEUTRAL ->
            MaterialTheme.colorScheme.surfaceVariant
        RoadMemoBadgeTone.WARNING ->
            RoadMemoWarning.copy(alpha = 0.14f)
    }
    val contentColor = when (tone) {
        RoadMemoBadgeTone.PRIMARY ->
            MaterialTheme.colorScheme.primary
        RoadMemoBadgeTone.NEUTRAL ->
            MaterialTheme.colorScheme.onSurfaceVariant
        RoadMemoBadgeTone.WARNING ->
            RoadMemoWarning
    }

    Box(
        modifier = modifier
            .background(
                color = containerColor,
                shape = MaterialTheme.shapes.large,
            )
            .padding(
                horizontal = RoadMemoSpacing.small,
                vertical = RoadMemoSpacing.xxSmall,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Composable
fun RoadMemoRecordCard(
    typeLabel: String,
    title: String,
    subtitle: String,
    amountText: String,
    modifier: Modifier = Modifier,
    badgeTone: RoadMemoBadgeTone = RoadMemoBadgeTone.PRIMARY,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
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
            verticalArrangement = Arrangement.spacedBy(RoadMemoSpacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(RoadMemoSpacing.small),
                ) {
                    RoadMemoStatusBadge(
                        text = typeLabel,
                        tone = badgeTone,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (onEdit != null || onDelete != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        RoadMemoSpacing.medium,
                        Alignment.End,
                    ),
                ) {
                    onEdit?.let {
                        RoadMemoSecondaryButton(
                            text = "编辑",
                            onClick = it,
                        )
                    }
                    onDelete?.let {
                        RoadMemoSecondaryButton(
                            text = "删除",
                            onClick = it,
                        )
                    }
                }
            }
        }
    }
}
