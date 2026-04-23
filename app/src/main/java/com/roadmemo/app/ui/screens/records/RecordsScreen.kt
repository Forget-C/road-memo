package com.roadmemo.app.ui.screens.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roadmemo.app.ui.components.RoadMemoBadgeTone
import com.roadmemo.app.ui.components.RoadMemoConfirmDialog
import com.roadmemo.app.ui.components.RoadMemoEmptyListState
import com.roadmemo.app.ui.components.RoadMemoRecordCard
import com.roadmemo.app.ui.components.RoadMemoScreenHeader
import com.roadmemo.app.ui.components.RoadMemoSecondaryButton
import com.roadmemo.app.ui.components.RoadMemoSkeletonCard
import com.roadmemo.app.ui.theme.RoadMemoIcons

private val tabs = listOf("能源", "保养", "费用", "续期")

private data class PendingDeleteAction(
    val title: String,
    val message: String,
    val onConfirm: () -> Unit,
)

@Composable
fun RecordsScreen(
    onAddEnergyRecord: () -> Unit,
    onAddMaintenanceRecord: () -> Unit,
    onAddExpenseRecord: () -> Unit,
    onAddRenewalRecord: () -> Unit,
    onEditEnergyRecord: (Long) -> Unit,
    onEditMaintenanceRecord: (Long) -> Unit,
    onEditExpenseRecord: (Long) -> Unit,
    onEditRenewalRecord: (Long) -> Unit,
    viewModel: RecordsViewModel = hiltViewModel(),
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var pendingDeleteAction by remember { mutableStateOf<PendingDeleteAction?>(null) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val primaryAction = when (selectedTabIndex) {
                1 -> onAddMaintenanceRecord
                2 -> onAddExpenseRecord
                3 -> onAddRenewalRecord
                else -> onAddEnergyRecord
            }
            RoadMemoScreenHeader(
                title = "记录",
                description = "默认车辆 · ${uiState.vehicleTitle}",
                trailing = {
                    RoadMemoSecondaryButton(
                        onClick = primaryAction,
                        text = "新增",
                        icon = RoadMemoIcons.Add,
                    )
                },
            )
        }

        SecondaryScrollableTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) },
                )
            }
        }

        val records = when (selectedTabIndex) {
            0 -> uiState.energyRecords
            1 -> uiState.maintenanceRecords
            2 -> uiState.expenseRecords
            else -> uiState.renewalRecords
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isLoading) {
                items(4) {
                    RoadMemoSkeletonCard(lineHeights = listOf(12.dp, 18.dp, 14.dp, 18.dp))
                }
            } else if (records.isEmpty()) {
                item {
                    RoadMemoEmptyListState(
                        title = tabs[selectedTabIndex],
                        description = "当前类型还没有记录",
                        icon = RoadMemoIcons.Vehicle,
                    )
                }
            } else {
                items(records, key = { it.id }) { record ->
                    RoadMemoRecordCard(
                        typeLabel = record.typeLabel,
                        title = record.title,
                        subtitle = record.subtitle,
                        amountText = record.amountText,
                        badgeTone = when (selectedTabIndex) {
                            1 -> RoadMemoBadgeTone.WARNING
                            3 -> RoadMemoBadgeTone.NEUTRAL
                            else -> RoadMemoBadgeTone.PRIMARY
                        },
                        onEdit = when (selectedTabIndex) {
                            0 -> ({ onEditEnergyRecord(record.id) })
                            1 -> ({ onEditMaintenanceRecord(record.id) })
                            2 -> ({ onEditExpenseRecord(record.id) })
                            else -> ({ onEditRenewalRecord(record.id) })
                        },
                        onDelete = {
                            pendingDeleteAction = when (selectedTabIndex) {
                                0 -> PendingDeleteAction(
                                    title = "删除能源记录",
                                    message = "确认删除这条能源记录吗？删除后无法恢复。",
                                    onConfirm = { viewModel.deleteEnergyRecord(record.id) },
                                )
                                1 -> PendingDeleteAction(
                                    title = "删除保养记录",
                                    message = "确认删除这条保养记录吗？删除后无法恢复。",
                                    onConfirm = { viewModel.deleteMaintenanceRecord(record.id) },
                                )
                                2 -> PendingDeleteAction(
                                    title = "删除费用记录",
                                    message = "确认删除这条费用记录吗？删除后无法恢复。",
                                    onConfirm = { viewModel.deleteExpenseRecord(record.id) },
                                )
                                else -> PendingDeleteAction(
                                    title = "删除续期事项",
                                    message = "确认删除这条续期事项吗？关联提醒也会一起删除。",
                                    onConfirm = { viewModel.deleteRenewalRecord(record.id) },
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    pendingDeleteAction?.let { action ->
        RoadMemoConfirmDialog(
            title = action.title,
            message = action.message,
            confirmText = "确认删除",
            onConfirm = {
                action.onConfirm()
                pendingDeleteAction = null
            },
            onDismiss = { pendingDeleteAction = null },
        )
    }
}
