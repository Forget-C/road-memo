package com.roadmemo.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
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
import com.roadmemo.app.ui.components.RoadMemoScreenHeader
import com.roadmemo.app.ui.components.RoadMemoSection
import com.roadmemo.app.ui.components.RoadMemoSecondaryButton
import com.roadmemo.app.ui.components.RoadMemoSnackbarHost
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

    Scaffold(
        snackbarHost = { RoadMemoSnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                RoadMemoScreenHeader(
                    title = "设置",
                    description = "管理偏好、备份与车辆入口",
                )
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RoadMemoStatusBadge(
                                text = "浅色主题",
                                tone = RoadMemoBadgeTone.PRIMARY,
                            )
                            RoadMemoStatusBadge(
                                text = "准扁平",
                                tone = RoadMemoBadgeTone.NEUTRAL,
                            )
                        }
                        Text("默认采用浅色主题和更克制的工具化风格。")
                    }
                }
            }

            item {
                RoadMemoInfoGroupCard(
                    title = "单位与货币",
                    description = "当前统一按公里和人民币展示，后续会补更多单位设置。",
                )
            }

            item {
                RoadMemoSection(title = "提醒偏好") {
                    Text("集中查看即将到期事项，并直接处理或跳回来源。")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        RoadMemoSecondaryButton(
                            text = "提醒",
                            onClick = onOpenReminders,
                        )
                    }
                }
            }

            item {
                RoadMemoSection(title = "数据管理") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RoadMemoStatusBadge(
                            text = "支持 CSV 导出",
                            tone = RoadMemoBadgeTone.NEUTRAL,
                        )
                        RoadMemoStatusBadge(
                            text = "支持完整备份",
                            tone = RoadMemoBadgeTone.NEUTRAL,
                        )
                    }
                    Text("可以导出 CSV，也可以导出完整备份并在换机后恢复。")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RoadMemoSecondaryButton(
                            text = "导出 CSV",
                            onClick = viewModel::exportCsv,
                            enabled = !uiState.isBusy,
                            isLoading = uiState.isExporting,
                            loadingText = "导出中...",
                            modifier = Modifier.weight(1f),
                        )
                        RoadMemoSecondaryButton(
                            text = "导出备份",
                            onClick = viewModel::exportBackup,
                            enabled = !uiState.isBusy,
                            isLoading = uiState.isBackingUp,
                            loadingText = "备份中...",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        RoadMemoSecondaryButton(
                            text = "恢复备份",
                            onClick = { showRestoreConfirm = true },
                            enabled = !uiState.isBusy,
                            isLoading = uiState.isRestoring,
                            loadingText = "恢复中...",
                        )
                    }
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
                    Text("维护车辆档案和默认车辆，首页与统计都会跟着切换。")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        RoadMemoSecondaryButton(
                            text = "车辆",
                            onClick = onOpenVehicles,
                        )
                    }
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
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}
