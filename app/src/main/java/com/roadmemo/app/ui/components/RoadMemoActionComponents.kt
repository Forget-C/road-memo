package com.roadmemo.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun RoadMemoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = text,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        RoadMemoButtonContent(
            text = if (isLoading) loadingText else text,
            icon = if (isLoading) null else icon,
            isLoading = isLoading,
        )
    }
}

@Composable
fun RoadMemoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = text,
    icon: ImageVector? = null,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
    ) {
        RoadMemoButtonContent(
            text = if (isLoading) loadingText else text,
            icon = if (isLoading) null else icon,
            isLoading = isLoading,
        )
    }
}

@Composable
private fun RoadMemoButtonContent(
    text: String,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.size(8.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(6.dp))
        }
        Text(text = text)
    }
}
