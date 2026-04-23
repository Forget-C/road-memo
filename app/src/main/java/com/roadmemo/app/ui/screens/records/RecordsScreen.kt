package com.roadmemo.app.ui.screens.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
        Card(
            modifier = Modifier
                .padding(start = 20.dp, top = 20.dp, end = 20.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "当前车辆",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = uiState.vehicleTitle,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        val primaryAction = when (selectedTabIndex) {
            1 -> onAddMaintenanceRecord
            2 -> onAddExpenseRecord
            3 -> onAddRenewalRecord
            else -> onAddEnergyRecord
        }
        val primaryActionLabel = when (selectedTabIndex) {
            1 -> "新增保养记录"
            2 -> "新增费用记录"
            3 -> "新增续期事项"
            else -> "新增能源记录"
        }
        FilledTonalButton(
            onClick = primaryAction,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(primaryActionLabel)
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (records.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = tabs[selectedTabIndex],
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.padding(top = 6.dp))
                            Text(
                                text = "当前类型还没有记录",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            } else {
                items(records, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = tabs[selectedTabIndex],
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.padding(top = 6.dp))
                            Text(
                                text = record.text,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.padding(top = 12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                            ) {
                                if (selectedTabIndex == 0) {
                                    Button(onClick = { onEditEnergyRecord(record.id) }) {
                                        Text("编辑")
                                    }
                                } else if (selectedTabIndex == 1) {
                                    Button(onClick = { onEditMaintenanceRecord(record.id) }) {
                                        Text("编辑")
                                    }
                                } else if (selectedTabIndex == 3) {
                                    Button(onClick = { onEditRenewalRecord(record.id) }) {
                                        Text("编辑")
                                    }
                                } else if (selectedTabIndex == 2) {
                                    Button(onClick = { onEditExpenseRecord(record.id) }) {
                                        Text("编辑")
                                    }
                                }
                                Button(
                                    onClick = {
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
                                ) {
                                    Text("删除")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteAction?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingDeleteAction = null },
            title = { Text(action.title) },
            text = { Text(action.message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        action.onConfirm()
                        pendingDeleteAction = null
                    },
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteAction = null }) {
                    Text("取消")
                }
            },
        )
    }
}
