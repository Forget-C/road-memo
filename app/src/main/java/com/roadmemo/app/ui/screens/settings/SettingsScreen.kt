package com.roadmemo.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.ui.components.RoadMemoFormHeader
import com.roadmemo.app.ui.components.RoadMemoSection

@Composable
fun SettingsScreen(
    onOpenVehicles: () -> Unit,
    onOpenReminders: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRestoreConfirm by remember { mutableStateOf(false) }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.ShareCsv -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "导出 CSV"))
                }
                is SettingsEffect.ShareBackup -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "导出备份"))
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RoadMemoFormHeader(
                title = "设置",
                description = "统一管理提醒、数据导出和本地备份，让这份车辆账本更可迁移、更可靠。",
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "数据与提醒概览",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "本地优先，支持导出和完整恢复",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "恢复备份会覆盖当前设备上的本地数据，适合迁移或重装后恢复使用。",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "显示与主题") {
                Text("V0.1 默认采用浅色主题、扁平卡片和更克制的商业软件风格。")
            }
        }

        item {
            RoadMemoSection(title = "单位与货币") {
                Text("后续会在这里承接距离单位、油耗单位和货币设置。")
            }
        }

        item {
            RoadMemoSection(title = "提醒偏好") {
                Text("查看即将到期事项，并在提醒页里直接标记已处理、忽略或跳回来源。")
                FilledTonalButton(onClick = onOpenReminders) {
                    Text("进入提醒页")
                }
            }
        }

        item {
            RoadMemoSection(title = "数据管理") {
                Text("支持导出 CSV、导出完整本地备份，以及通过 JSON 备份恢复整库数据。")
                FilledTonalButton(
                    onClick = viewModel::exportCsv,
                    enabled = !uiState.isExporting && !uiState.isBackingUp && !uiState.isRestoring,
                ) {
                    Text(if (uiState.isExporting) "导出中..." else "导出 CSV")
                }
                FilledTonalButton(
                    onClick = viewModel::exportBackup,
                    enabled = !uiState.isExporting && !uiState.isBackingUp && !uiState.isRestoring,
                ) {
                    Text(if (uiState.isBackingUp) "备份中..." else "导出备份")
                }
                FilledTonalButton(
                    onClick = { showRestoreConfirm = true },
                    enabled = !uiState.isExporting && !uiState.isBackingUp && !uiState.isRestoring,
                ) {
                    Text(if (uiState.isRestoring) "恢复中..." else "恢复备份")
                }
                uiState.exportMessage?.let { message ->
                    Text(
                        text = message,
                        color = if (uiState.isErrorMessage) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }

        item {
            RoadMemoSection(title = "车辆管理") {
                Text("管理车辆档案、默认车辆和动力类型。")
                FilledTonalButton(onClick = onOpenVehicles) {
                    Text("进入车辆管理")
                }
            }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("恢复本地备份") },
            text = { Text("恢复会覆盖当前设备上的本地数据，且无法撤销。确认继续吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirm = false
                        importLauncher.launch(arrayOf("application/json"))
                    },
                ) {
                    Text("继续恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }
}
