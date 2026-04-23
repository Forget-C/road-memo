package com.roadmemo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.roadmemo.app.ui.theme.RoadMemoSpacing

enum class RoadMemoFeedbackTone {
    INFO,
    ERROR,
    SUCCESS,
}

@Composable
fun RoadMemoFeedbackMessage(
    message: String,
    modifier: Modifier = Modifier,
    tone: RoadMemoFeedbackTone = RoadMemoFeedbackTone.INFO,
) {
    val containerColor = when (tone) {
        RoadMemoFeedbackTone.ERROR -> MaterialTheme.colorScheme.errorContainer
        RoadMemoFeedbackTone.SUCCESS -> MaterialTheme.colorScheme.secondaryContainer
        RoadMemoFeedbackTone.INFO -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = when (tone) {
        RoadMemoFeedbackTone.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        RoadMemoFeedbackTone.SUCCESS -> MaterialTheme.colorScheme.onSecondaryContainer
        RoadMemoFeedbackTone.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = RoadMemoSpacing.medium,
                vertical = RoadMemoSpacing.small + RoadMemoSpacing.xxSmall,
            ),
            verticalArrangement = Arrangement.spacedBy(RoadMemoSpacing.xxSmall),
        ) {
            Text(
                text = when (tone) {
                    RoadMemoFeedbackTone.ERROR -> "处理失败"
                    RoadMemoFeedbackTone.SUCCESS -> "处理完成"
                    RoadMemoFeedbackTone.INFO -> "当前状态"
                },
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
        }
    }
}
