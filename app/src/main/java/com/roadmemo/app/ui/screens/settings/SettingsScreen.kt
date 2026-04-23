package com.roadmemo.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.ui.components.RoadMemoBadgeTone
import com.roadmemo.app.ui.components.RoadMemoConfirmDialog
import com.roadmemo.app.ui.components.RoadMemoFeedbackMessage
import com.roadmemo.app.ui.components.RoadMemoFeedbackTone
import com.roadmemo.app.ui.components.RoadMemoInfoGroupCard
import com.roadmemo.app.ui.components.RoadMemoMiniInfoCard
import com.roadmemo.app.ui.components.RoadMemoPageScaffold
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSecondaryButton
import com.roadmemo.app.ui.components.RoadMemoStatusBadge
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onOpenVehicles: () -> Unit,
    onOpenReminders: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRestoreConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
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
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("已调起系统分享，可选择 CSV 文件去向")
                    }
                }
                is SettingsEffect.ShareBackup -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "导出备份"))
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("已调起系统分享，可选择备份文件去向")
                    }
                }
            }
        }
    }

    RoadMemoPageScaffold(
        title = "设置",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RoadMemoMiniInfoCard(
                        title = "存储方式",
                        value = "本地优先",
                        modifier = Modifier.weight(1f),
                    )
                    RoadMemoMiniInfoCard(
                        title = "当前状态",
                        value = if (uiState.isBusy) "处理中" else "可操作",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                RoadMemoSection(title = "显示与主题") {
                    ColumnScopeContent {
                        RoadMemoStatusBadge(
                            text = "浅色主题",
                            tone = RoadMemoBadgeTone.PRIMARY,
                        )
                        RoadMemoStatusBadge(
                            text = "准扁平",
                            tone = RoadMemoBadgeTone.NEUTRAL,
                        )
                        Text("V0.1 默认采用浅色主题、扁平卡片和更克制的商业软件风格。")
                    }
                }
            }

            item {
                RoadMemoInfoGroupCard(
                    title = "单位与货币",
                    description = "后续会在这里承接距离单位、油耗单位和货币设置。",
                )
            }

            item {
                RoadMemoSection(title = "提醒偏好") {
                    Text("查看即将到期事项，并在提醒页里直接标记已处理、忽略或跳回来源。")
                    RoadMemoSecondaryButton(
                        text = "进入提醒页",
                        onClick = onOpenReminders,
                    )
                }
            }

            item {
                RoadMemoSection(title = "数据管理") {
                    RoadMemoStatusBadge(
                        text = "支持 CSV / JSON 备份",
                        tone = RoadMemoBadgeTone.NEUTRAL,
                    )
                    Text("支持导出 CSV、导出完整本地备份，以及通过 JSON 备份恢复整库数据。")
                    RoadMemoSecondaryButton(
                        text = "导出 CSV",
                        onClick = viewModel::exportCsv,
                        enabled = !uiState.isBusy,
                        isLoading = uiState.isExporting,
                        loadingText = "导出中...",
                    )
                    RoadMemoSecondaryButton(
                        text = "导出备份",
                        onClick = viewModel::exportBackup,
                        enabled = !uiState.isBusy,
                        isLoading = uiState.isBackingUp,
                        loadingText = "备份中...",
                    )
                    RoadMemoSecondaryButton(
                        text = "恢复备份",
                        onClick = { showRestoreConfirm = true },
                        enabled = !uiState.isBusy,
                        isLoading = uiState.isRestoring,
                        loadingText = "恢复中...",
                    )
                    uiState.exportMessage?.let { message ->
                        RoadMemoFeedbackMessage(
                            message = message,
                            tone = if (uiState.isErrorMessage) {
                                RoadMemoFeedbackTone.ERROR
                            } else {
                                RoadMemoFeedbackTone.SUCCESS
                            },
                        )
                    }
                }
            }

            item {
                RoadMemoSection(title = "车辆管理") {
                    Text("管理车辆档案、默认车辆和动力类型。")
                    RoadMemoSecondaryButton(
                        text = "进入车辆管理",
                        onClick = onOpenVehicles,
                    )
                }
            }
        }
    }

    if (showRestoreConfirm) {
        RoadMemoConfirmDialog(
            title = "恢复本地备份",
            message = "恢复会覆盖当前设备上的本地数据，且无法撤销。确认继续吗？",
            confirmText = "继续恢复",
            onConfirm = {
                showRestoreConfirm = false
                importLauncher.launch(arrayOf("application/json"))
            },
            onDismiss = { showRestoreConfirm = false },
        )
    }
}

@Composable
private fun ColumnScopeContent(
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}
