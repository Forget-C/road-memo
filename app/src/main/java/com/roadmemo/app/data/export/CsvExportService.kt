package com.roadmemo.app.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.roadmemo.app.data.local.dao.EnergyRecordDao
import com.roadmemo.app.data.local.dao.ExpenseRecordDao
import com.roadmemo.app.data.local.dao.MaintenanceRecordDao
import com.roadmemo.app.data.local.dao.RenewalRecordDao
import com.roadmemo.app.data.local.dao.VehicleDao
import com.roadmemo.app.data.local.entity.VehicleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class CsvExportService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val vehicleDao: VehicleDao,
    private val energyRecordDao: EnergyRecordDao,
    private val maintenanceRecordDao: MaintenanceRecordDao,
    private val expenseRecordDao: ExpenseRecordDao,
    private val renewalRecordDao: RenewalRecordDao,
) {
    suspend fun exportAllRecords(): ExportResult = withContext(Dispatchers.IO) {
        val vehicles = vehicleDao.getAll()
        val vehicleMap = vehicles.associateBy { it.id }
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "roadmemo-export-${System.currentTimeMillis()}.csv")

        val rows = buildList {
            add(
                listOf(
                    "车辆",
                    "动力类型",
                    "记录类型",
                    "发生时间",
                    "金额(元)",
                    "里程(km)",
                    "数量",
                    "数量单位",
                    "子类型",
                    "站点/门店/机构",
                    "到期日",
                    "备注",
                ),
            )

            energyRecordDao.getAll().forEach { record ->
                val vehicle = vehicleMap[record.vehicleId]
                add(
                    listOf(
                        vehicle.displayName(),
                        vehicle?.powertrainType.orEmpty(),
                        if (record.energyType == "ELECTRIC") "能源-充电" else "能源-加油",
                        formatDateTime(record.occurredAtEpochMillis),
                        centsToYuan(record.amountInCent),
                        record.odometerKm.toString(),
                        (record.quantityInThousandth / 1000.0).toString(),
                        if (record.energyType == "ELECTRIC") "kWh" else "L",
                        record.chargeMode ?: record.fuelLabel.orEmpty(),
                        record.stationName.orEmpty(),
                        "",
                        record.note.orEmpty(),
                    ),
                )
            }

            maintenanceRecordDao.getAll().forEach { record ->
                val vehicle = vehicleMap[record.vehicleId]
                add(
                    listOf(
                        vehicle.displayName(),
                        vehicle?.powertrainType.orEmpty(),
                        "保养",
                        formatDateTime(record.occurredAtEpochMillis),
                        centsToYuan(record.amountInCent),
                        record.odometerKm?.toString().orEmpty(),
                        "",
                        "",
                        record.maintenanceType,
                        record.storeName.orEmpty(),
                        record.nextDueDateEpochDay?.let(LocalDate::ofEpochDay)?.toString().orEmpty(),
                        record.note.orEmpty(),
                    ),
                )
            }

            expenseRecordDao.getAll().forEach { record ->
                val vehicle = vehicleMap[record.vehicleId]
                add(
                    listOf(
                        vehicle.displayName(),
                        vehicle?.powertrainType.orEmpty(),
                        "费用",
                        formatDateTime(record.occurredAtEpochMillis),
                        centsToYuan(record.amountInCent),
                        "",
                        "",
                        "",
                        record.category,
                        "",
                        "",
                        record.note.orEmpty(),
                    ),
                )
            }

            renewalRecordDao.getAll().forEach { record ->
                val vehicle = vehicleMap[record.vehicleId]
                add(
                    listOf(
                        vehicle.displayName(),
                        vehicle?.powertrainType.orEmpty(),
                        "续期",
                        formatDateTime(record.createdAtEpochMillis),
                        centsToYuan(record.amountInCent),
                        "",
                        "",
                        "",
                        record.type,
                        record.providerName.orEmpty(),
                        LocalDate.ofEpochDay(record.validUntilEpochDay).toString(),
                        record.note.orEmpty(),
                    ),
                )
            }
        }

        file.writeText(rows.joinToString(separator = "\n") { row -> row.joinToString(",") { csvEscape(it) } })
        ExportResult(
            fileName = file.name,
            uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            ),
        )
    }
}

data class ExportResult(
    val fileName: String,
    val uri: Uri,
)

private fun VehicleEntity?.displayName(): String = when {
    this == null -> ""
    plateNumber.isNullOrBlank() -> "$brand $model"
    else -> "$brand $model $plateNumber"
}

private fun formatDateTime(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime())

private fun centsToYuan(amountInCent: Long): String = (amountInCent / 100.0).toString()

private fun csvEscape(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return if (escaped.contains(',') || escaped.contains('"') || escaped.contains('\n')) {
        "\"$escaped\""
    } else {
        escaped
    }
}
