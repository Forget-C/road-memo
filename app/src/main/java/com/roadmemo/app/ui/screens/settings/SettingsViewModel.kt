package com.roadmemo.app.ui.screens.settings

import android.net.Uri
import com.roadmemo.app.data.export.BackupService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadmemo.app.data.export.CsvExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isExporting: Boolean = false,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val exportMessage: String? = null,
    val isErrorMessage: Boolean = false,
) {
    val isBusy: Boolean
        get() = isExporting || isBackingUp || isRestoring
}

sealed interface SettingsEffect {
    data class ShareCsv(val uri: Uri) : SettingsEffect
    data class ShareBackup(val uri: Uri) : SettingsEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val csvExportService: CsvExportService,
    private val backupService: BackupService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsEffect>()
    val effects: SharedFlow<SettingsEffect> = _effects.asSharedFlow()

    fun exportCsv() {
        if (_uiState.value.isExporting || _uiState.value.isBackingUp || _uiState.value.isRestoring) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportMessage = null,
                isErrorMessage = false,
            )
            val result = csvExportService.exportAllRecords()
            _uiState.value = _uiState.value.copy(
                isExporting = false,
                exportMessage = "已生成 ${result.fileName}",
                isErrorMessage = false,
            )
            _effects.emit(SettingsEffect.ShareCsv(result.uri))
        }
    }

    fun exportBackup() {
        if (_uiState.value.isExporting || _uiState.value.isBackingUp || _uiState.value.isRestoring) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBackingUp = true,
                exportMessage = null,
                isErrorMessage = false,
            )
            val result = backupService.exportBackup()
            _uiState.value = _uiState.value.copy(
                isBackingUp = false,
                exportMessage = "已生成 ${result.fileName}",
                isErrorMessage = false,
            )
            _effects.emit(SettingsEffect.ShareBackup(result.uri))
        }
    }

    fun importBackup(uri: Uri) {
        if (_uiState.value.isExporting || _uiState.value.isBackingUp || _uiState.value.isRestoring) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRestoring = true,
                exportMessage = null,
                isErrorMessage = false,
            )
            val result = runCatching { backupService.importBackup(uri) }
            _uiState.value = _uiState.value.copy(
                isRestoring = false,
                exportMessage = result.getOrElse { error -> "恢复失败：${error.message ?: "未知错误"}" },
                isErrorMessage = result.isFailure,
            )
        }
    }
}
