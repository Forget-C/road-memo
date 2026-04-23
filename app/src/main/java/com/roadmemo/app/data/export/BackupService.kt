package com.roadmemo.app.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.roadmemo.app.data.local.dao.EnergyRecordDao
import com.roadmemo.app.data.local.dao.ExpenseRecordDao
import com.roadmemo.app.data.local.dao.MaintenanceRecordDao
import com.roadmemo.app.data.local.dao.ReminderDao
import com.roadmemo.app.data.local.dao.RenewalRecordDao
import com.roadmemo.app.data.local.dao.VehicleDao
import com.roadmemo.app.data.local.database.RoadMemoDatabase
import com.roadmemo.app.data.local.entity.EnergyRecordEntity
import com.roadmemo.app.data.local.entity.ExpenseRecordEntity
import com.roadmemo.app.data.local.entity.MaintenanceRecordEntity
import com.roadmemo.app.data.local.entity.ReminderEntity
import com.roadmemo.app.data.local.entity.RenewalRecordEntity
import com.roadmemo.app.data.local.entity.VehicleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class BackupService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: RoadMemoDatabase,
    private val vehicleDao: VehicleDao,
    private val energyRecordDao: EnergyRecordDao,
    private val maintenanceRecordDao: MaintenanceRecordDao,
    private val expenseRecordDao: ExpenseRecordDao,
    private val renewalRecordDao: RenewalRecordDao,
    private val reminderDao: ReminderDao,
) {
    suspend fun exportBackup(): ExportResult = withContext(Dispatchers.IO) {
        val root = JSONObject().apply {
            put("schemaVersion", 1)
            put("exportedAtEpochMillis", Instant.now().toEpochMilli())
            put("vehicles", JSONArray(vehicleDao.getAll().map { it.toJson() }))
            put("energyRecords", JSONArray(energyRecordDao.getAll().map { it.toJson() }))
            put("maintenanceRecords", JSONArray(maintenanceRecordDao.getAll().map { it.toJson() }))
            put("expenseRecords", JSONArray(expenseRecordDao.getAll().map { it.toJson() }))
            put("renewalRecords", JSONArray(renewalRecordDao.getAll().map { it.toJson() }))
            put("reminders", JSONArray(reminderDao.getAll().map { it.toJson() }))
        }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "roadmemo-backup-${System.currentTimeMillis()}.json")
        file.writeText(root.toString(2))
        ExportResult(
            fileName = file.name,
            uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            ),
        )
    }

    suspend fun importBackup(uri: Uri): String = withContext(Dispatchers.IO) {
        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("无法读取备份文件")
        val root = JSONObject(content)
        require(root.optInt("schemaVersion", -1) == 1) { "不支持的备份版本" }

        val vehicles = root.getJSONArray("vehicles").toVehicleEntities()
        val energyRecords = root.getJSONArray("energyRecords").toEnergyRecordEntities()
        val maintenanceRecords = root.getJSONArray("maintenanceRecords").toMaintenanceRecordEntities()
        val expenseRecords = root.getJSONArray("expenseRecords").toExpenseRecordEntities()
        val renewalRecords = root.getJSONArray("renewalRecords").toRenewalRecordEntities()
        val reminders = root.getJSONArray("reminders").toReminderEntities()

        database.withTransaction {
            database.clearAllTables()
            vehicles.forEach { vehicleDao.insert(it) }
            energyRecords.forEach { energyRecordDao.insert(it) }
            maintenanceRecords.forEach { maintenanceRecordDao.insert(it) }
            expenseRecords.forEach { expenseRecordDao.insert(it) }
            renewalRecords.forEach { renewalRecordDao.insert(it) }
            reminders.forEach { reminderDao.insert(it) }
        }
        "已恢复 ${vehicles.size} 辆车、${energyRecords.size + maintenanceRecords.size + expenseRecords.size + renewalRecords.size} 条记录"
    }
}

private fun VehicleEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("brand", brand)
    put("model", model)
    put("plateNumber", plateNumber)
    put("purchaseDateEpochDay", purchaseDateEpochDay)
    put("powertrainType", powertrainType)
    put("note", note)
    put("isDefault", isDefault)
    put("createdAtEpochMillis", createdAtEpochMillis)
    put("updatedAtEpochMillis", updatedAtEpochMillis)
}

private fun EnergyRecordEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("vehicleId", vehicleId)
    put("occurredAtEpochMillis", occurredAtEpochMillis)
    put("energyType", energyType)
    put("odometerKm", odometerKm)
    put("quantityInThousandth", quantityInThousandth)
    put("amountInCent", amountInCent)
    put("isFull", isFull)
    put("fuelLabel", fuelLabel)
    put("chargeMode", chargeMode)
    put("stationName", stationName)
    put("note", note)
    put("createdAtEpochMillis", createdAtEpochMillis)
    put("updatedAtEpochMillis", updatedAtEpochMillis)
}

private fun MaintenanceRecordEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("vehicleId", vehicleId)
    put("occurredAtEpochMillis", occurredAtEpochMillis)
    put("odometerKm", odometerKm)
    put("maintenanceType", maintenanceType)
    put("amountInCent", amountInCent)
    put("storeName", storeName)
    put("note", note)
    put("nextDueDateEpochDay", nextDueDateEpochDay)
    put("nextDueOdometerKm", nextDueOdometerKm)
    put("createdAtEpochMillis", createdAtEpochMillis)
    put("updatedAtEpochMillis", updatedAtEpochMillis)
}

private fun ExpenseRecordEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("vehicleId", vehicleId)
    put("occurredAtEpochMillis", occurredAtEpochMillis)
    put("category", category)
    put("amountInCent", amountInCent)
    put("note", note)
    put("createdAtEpochMillis", createdAtEpochMillis)
    put("updatedAtEpochMillis", updatedAtEpochMillis)
}

private fun RenewalRecordEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("vehicleId", vehicleId)
    put("type", type)
    put("providerName", providerName)
    put("policyNumber", policyNumber)
    put("amountInCent", amountInCent)
    put("validFromEpochDay", validFromEpochDay)
    put("validUntilEpochDay", validUntilEpochDay)
    put("reminderEnabled", reminderEnabled)
    put("note", note)
    put("createdAtEpochMillis", createdAtEpochMillis)
    put("updatedAtEpochMillis", updatedAtEpochMillis)
}

private fun ReminderEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("vehicleId", vehicleId)
    put("type", type)
    put("title", title)
    put("remindAtEpochMillis", remindAtEpochMillis)
    put("remindOdometerKm", remindOdometerKm)
    put("advanceDays", advanceDays)
    put("isEnabled", isEnabled)
    put("status", status)
    put("sourceType", sourceType)
    put("sourceId", sourceId)
    put("note", note)
    put("lastTriggeredAtEpochMillis", lastTriggeredAtEpochMillis)
    put("completedAtEpochMillis", completedAtEpochMillis)
    put("dismissedAtEpochMillis", dismissedAtEpochMillis)
    put("createdAtEpochMillis", createdAtEpochMillis)
    put("updatedAtEpochMillis", updatedAtEpochMillis)
}

private fun JSONArray.toVehicleEntities(): List<VehicleEntity> = (0 until length()).map { index ->
    getJSONObject(index).let { json ->
        VehicleEntity(
            id = json.getLong("id"),
            brand = json.getString("brand"),
            model = json.getString("model"),
            plateNumber = json.optNullableString("plateNumber"),
            purchaseDateEpochDay = json.optNullableLong("purchaseDateEpochDay"),
            powertrainType = json.getString("powertrainType"),
            note = json.optNullableString("note"),
            isDefault = json.getBoolean("isDefault"),
            createdAtEpochMillis = json.getLong("createdAtEpochMillis"),
            updatedAtEpochMillis = json.getLong("updatedAtEpochMillis"),
        )
    }
}

private fun JSONArray.toEnergyRecordEntities(): List<EnergyRecordEntity> = (0 until length()).map { index ->
    getJSONObject(index).let { json ->
        EnergyRecordEntity(
            id = json.getLong("id"),
            vehicleId = json.getLong("vehicleId"),
            occurredAtEpochMillis = json.getLong("occurredAtEpochMillis"),
            energyType = json.getString("energyType"),
            odometerKm = json.getInt("odometerKm"),
            quantityInThousandth = json.getLong("quantityInThousandth"),
            amountInCent = json.getLong("amountInCent"),
            isFull = json.getBoolean("isFull"),
            fuelLabel = json.optNullableString("fuelLabel"),
            chargeMode = json.optNullableString("chargeMode"),
            stationName = json.optNullableString("stationName"),
            note = json.optNullableString("note"),
            createdAtEpochMillis = json.getLong("createdAtEpochMillis"),
            updatedAtEpochMillis = json.getLong("updatedAtEpochMillis"),
        )
    }
}

private fun JSONArray.toMaintenanceRecordEntities(): List<MaintenanceRecordEntity> = (0 until length()).map { index ->
    getJSONObject(index).let { json ->
        MaintenanceRecordEntity(
            id = json.getLong("id"),
            vehicleId = json.getLong("vehicleId"),
            occurredAtEpochMillis = json.getLong("occurredAtEpochMillis"),
            odometerKm = json.optNullableInt("odometerKm"),
            maintenanceType = json.getString("maintenanceType"),
            amountInCent = json.getLong("amountInCent"),
            storeName = json.optNullableString("storeName"),
            note = json.optNullableString("note"),
            nextDueDateEpochDay = json.optNullableLong("nextDueDateEpochDay"),
            nextDueOdometerKm = json.optNullableInt("nextDueOdometerKm"),
            createdAtEpochMillis = json.getLong("createdAtEpochMillis"),
            updatedAtEpochMillis = json.getLong("updatedAtEpochMillis"),
        )
    }
}

private fun JSONArray.toExpenseRecordEntities(): List<ExpenseRecordEntity> = (0 until length()).map { index ->
    getJSONObject(index).let { json ->
        ExpenseRecordEntity(
            id = json.getLong("id"),
            vehicleId = json.getLong("vehicleId"),
            occurredAtEpochMillis = json.getLong("occurredAtEpochMillis"),
            category = json.getString("category"),
            amountInCent = json.getLong("amountInCent"),
            note = json.optNullableString("note"),
            createdAtEpochMillis = json.getLong("createdAtEpochMillis"),
            updatedAtEpochMillis = json.getLong("updatedAtEpochMillis"),
        )
    }
}

private fun JSONArray.toRenewalRecordEntities(): List<RenewalRecordEntity> = (0 until length()).map { index ->
    getJSONObject(index).let { json ->
        RenewalRecordEntity(
            id = json.getLong("id"),
            vehicleId = json.getLong("vehicleId"),
            type = json.getString("type"),
            providerName = json.optNullableString("providerName"),
            policyNumber = json.optNullableString("policyNumber"),
            amountInCent = json.getLong("amountInCent"),
            validFromEpochDay = json.optNullableLong("validFromEpochDay"),
            validUntilEpochDay = json.getLong("validUntilEpochDay"),
            reminderEnabled = json.getBoolean("reminderEnabled"),
            note = json.optNullableString("note"),
            createdAtEpochMillis = json.getLong("createdAtEpochMillis"),
            updatedAtEpochMillis = json.getLong("updatedAtEpochMillis"),
        )
    }
}

private fun JSONArray.toReminderEntities(): List<ReminderEntity> = (0 until length()).map { index ->
    getJSONObject(index).let { json ->
        ReminderEntity(
            id = json.getLong("id"),
            vehicleId = json.getLong("vehicleId"),
            type = json.getString("type"),
            title = json.getString("title"),
            remindAtEpochMillis = json.optNullableLong("remindAtEpochMillis"),
            remindOdometerKm = json.optNullableInt("remindOdometerKm"),
            advanceDays = json.optNullableInt("advanceDays"),
            isEnabled = json.getBoolean("isEnabled"),
            status = json.getString("status"),
            sourceType = json.optNullableString("sourceType"),
            sourceId = json.optNullableLong("sourceId"),
            note = json.optNullableString("note"),
            lastTriggeredAtEpochMillis = json.optNullableLong("lastTriggeredAtEpochMillis"),
            completedAtEpochMillis = json.optNullableLong("completedAtEpochMillis"),
            dismissedAtEpochMillis = json.optNullableLong("dismissedAtEpochMillis"),
            createdAtEpochMillis = json.getLong("createdAtEpochMillis"),
            updatedAtEpochMillis = json.getLong("updatedAtEpochMillis"),
        )
    }
}

private fun JSONObject.optNullableString(key: String): String? =
    if (isNull(key)) null else optString(key).ifBlank { null }

private fun JSONObject.optNullableLong(key: String): Long? =
    if (isNull(key)) null else getLong(key)

private fun JSONObject.optNullableInt(key: String): Int? =
    if (isNull(key)) null else getInt(key)
