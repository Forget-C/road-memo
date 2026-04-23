package com.roadmemo.app.data.repository

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
import com.roadmemo.app.data.local.mapper.toDomain
import com.roadmemo.app.data.preferences.UserPreferencesDataSource
import com.roadmemo.app.domain.model.ReminderSourceType
import com.roadmemo.app.domain.model.ReminderStatus
import com.roadmemo.app.domain.model.ReminderType
import com.roadmemo.app.domain.model.RenewalType
import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.ExpenseRecord
import com.roadmemo.app.domain.model.MaintenanceRecord
import com.roadmemo.app.domain.model.Reminder
import com.roadmemo.app.domain.model.RenewalRecord
import com.roadmemo.app.domain.model.Vehicle
import com.roadmemo.app.domain.repository.EnergyRepository
import com.roadmemo.app.domain.repository.ExpenseRepository
import com.roadmemo.app.domain.repository.MaintenanceRepository
import com.roadmemo.app.domain.repository.ReminderRepository
import com.roadmemo.app.domain.repository.RenewalRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import androidx.room.withTransaction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultVehicleRepository @Inject constructor(
    private val database: RoadMemoDatabase,
    private val vehicleDao: VehicleDao,
) : VehicleRepository {
    override fun observeVehicles(): Flow<List<Vehicle>> = vehicleDao.observeVehicles().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun observeDefaultVehicle(): Flow<Vehicle?> = vehicleDao.observeDefaultVehicle().map { entity ->
        entity?.toDomain()
    }

    override suspend fun addVehicle(
        brand: String,
        model: String,
        plateNumber: String?,
        powertrainType: String,
        note: String?,
    ): Long = database.withTransaction {
        val now = Instant.now().toEpochMilli()
        val shouldBeDefault = vehicleDao.countVehicles() == 0
        if (shouldBeDefault) {
            vehicleDao.clearDefaultFlag()
        }
        vehicleDao.insert(
            VehicleEntity(
                brand = brand.trim(),
                model = model.trim(),
                plateNumber = plateNumber?.trim().orEmpty().ifBlank { null },
                purchaseDateEpochDay = null,
                powertrainType = powertrainType,
                note = note?.trim().orEmpty().ifBlank { null },
                isDefault = shouldBeDefault,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun setDefaultVehicle(vehicleId: Long) {
        database.withTransaction {
            if (vehicleDao.getById(vehicleId) != null) {
                val now = Instant.now().toEpochMilli()
                vehicleDao.clearDefaultFlag()
                vehicleDao.setDefaultVehicle(vehicleId, now)
            }
        }
    }
}

class DefaultEnergyRepository @Inject constructor(
    private val energyRecordDao: EnergyRecordDao,
) : EnergyRepository {
    override fun observeRecords(vehicleId: Long): Flow<List<EnergyRecord>> =
        energyRecordDao.observeByVehicleId(vehicleId).map { entities -> entities.map { it.toDomain() } }

    override fun observeLatest(vehicleId: Long): Flow<EnergyRecord?> =
        energyRecordDao.observeLatestByVehicleId(vehicleId).map { it?.toDomain() }

    override suspend fun getRecord(recordId: Long): EnergyRecord? =
        energyRecordDao.getById(recordId)?.toDomain()

    override suspend fun addRecord(
        vehicleId: Long,
        energyType: String,
        odometerKm: Int,
        quantityInThousandth: Long,
        amountInCent: Long,
        isFull: Boolean,
        fuelLabel: String?,
        chargeMode: String?,
        stationName: String?,
        note: String?,
    ): Long {
        val now = Instant.now().toEpochMilli()
        return energyRecordDao.insert(
            EnergyRecordEntity(
                vehicleId = vehicleId,
                occurredAtEpochMillis = now,
                energyType = energyType,
                odometerKm = odometerKm,
                quantityInThousandth = quantityInThousandth,
                amountInCent = amountInCent,
                isFull = isFull,
                fuelLabel = fuelLabel?.trim().orEmpty().ifBlank { null },
                chargeMode = chargeMode?.trim().orEmpty().ifBlank { null },
                stationName = stationName?.trim().orEmpty().ifBlank { null },
                note = note?.trim().orEmpty().ifBlank { null },
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun updateRecord(
        recordId: Long,
        energyType: String,
        odometerKm: Int,
        quantityInThousandth: Long,
        amountInCent: Long,
        isFull: Boolean,
        fuelLabel: String?,
        chargeMode: String?,
        stationName: String?,
        note: String?,
    ) {
        val existing = energyRecordDao.getById(recordId) ?: return
        val now = Instant.now().toEpochMilli()
        energyRecordDao.update(
            existing.copy(
                energyType = energyType,
                odometerKm = odometerKm,
                quantityInThousandth = quantityInThousandth,
                amountInCent = amountInCent,
                isFull = isFull,
                fuelLabel = fuelLabel?.trim().orEmpty().ifBlank { null },
                chargeMode = chargeMode?.trim().orEmpty().ifBlank { null },
                stationName = stationName?.trim().orEmpty().ifBlank { null },
                note = note?.trim().orEmpty().ifBlank { null },
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun deleteRecord(recordId: Long) {
        energyRecordDao.deleteById(recordId)
    }
}

class DefaultMaintenanceRepository @Inject constructor(
    private val maintenanceRecordDao: MaintenanceRecordDao,
) : MaintenanceRepository {
    override fun observeRecords(vehicleId: Long): Flow<List<MaintenanceRecord>> =
        maintenanceRecordDao.observeByVehicleId(vehicleId).map { entities -> entities.map { it.toDomain() } }

    override fun observeLatest(vehicleId: Long): Flow<MaintenanceRecord?> =
        maintenanceRecordDao.observeLatestByVehicleId(vehicleId).map { it?.toDomain() }

    override suspend fun getRecord(recordId: Long): MaintenanceRecord? =
        maintenanceRecordDao.getById(recordId)?.toDomain()

    override suspend fun addRecord(
        vehicleId: Long,
        maintenanceType: String,
        amountInCent: Long,
        odometerKm: Int?,
        storeName: String?,
        note: String?,
        nextDueDateEpochDay: Long?,
        nextDueOdometerKm: Int?,
    ): Long {
        val now = Instant.now().toEpochMilli()
        return maintenanceRecordDao.insert(
            MaintenanceRecordEntity(
                vehicleId = vehicleId,
                occurredAtEpochMillis = now,
                odometerKm = odometerKm,
                maintenanceType = maintenanceType,
                amountInCent = amountInCent,
                storeName = storeName?.trim().orEmpty().ifBlank { null },
                note = note?.trim().orEmpty().ifBlank { null },
                nextDueDateEpochDay = nextDueDateEpochDay,
                nextDueOdometerKm = nextDueOdometerKm,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun updateRecord(
        recordId: Long,
        maintenanceType: String,
        amountInCent: Long,
        odometerKm: Int?,
        storeName: String?,
        note: String?,
        nextDueDateEpochDay: Long?,
        nextDueOdometerKm: Int?,
    ) {
        val existing = maintenanceRecordDao.getById(recordId) ?: return
        val now = Instant.now().toEpochMilli()
        maintenanceRecordDao.update(
            existing.copy(
                odometerKm = odometerKm,
                maintenanceType = maintenanceType,
                amountInCent = amountInCent,
                storeName = storeName?.trim().orEmpty().ifBlank { null },
                note = note?.trim().orEmpty().ifBlank { null },
                nextDueDateEpochDay = nextDueDateEpochDay,
                nextDueOdometerKm = nextDueOdometerKm,
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun deleteRecord(recordId: Long) {
        maintenanceRecordDao.deleteById(recordId)
    }
}

class DefaultExpenseRepository @Inject constructor(
    private val expenseRecordDao: ExpenseRecordDao,
) : ExpenseRepository {
    override fun observeRecords(vehicleId: Long): Flow<List<ExpenseRecord>> =
        expenseRecordDao.observeByVehicleId(vehicleId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getRecord(recordId: Long): ExpenseRecord? =
        expenseRecordDao.getById(recordId)?.toDomain()

    override suspend fun addRecord(
        vehicleId: Long,
        category: String,
        amountInCent: Long,
        note: String?,
    ): Long {
        val now = Instant.now().toEpochMilli()
        return expenseRecordDao.insert(
            ExpenseRecordEntity(
                vehicleId = vehicleId,
                occurredAtEpochMillis = now,
                category = category,
                amountInCent = amountInCent,
                note = note?.trim().orEmpty().ifBlank { null },
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun updateRecord(
        recordId: Long,
        category: String,
        amountInCent: Long,
        note: String?,
    ) {
        val existing = expenseRecordDao.getById(recordId) ?: return
        val now = Instant.now().toEpochMilli()
        expenseRecordDao.update(
            existing.copy(
                category = category,
                amountInCent = amountInCent,
                note = note?.trim().orEmpty().ifBlank { null },
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun deleteRecord(recordId: Long) {
        expenseRecordDao.deleteById(recordId)
    }
}

class DefaultRenewalRepository @Inject constructor(
    private val database: RoadMemoDatabase,
    private val renewalRecordDao: RenewalRecordDao,
    private val reminderDao: ReminderDao,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : RenewalRepository {
    override fun observeRecords(vehicleId: Long): Flow<List<RenewalRecord>> =
        renewalRecordDao.observeByVehicleId(vehicleId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getRecord(recordId: Long): RenewalRecord? =
        renewalRecordDao.getById(recordId)?.toDomain()

    override suspend fun addRecord(
        vehicleId: Long,
        type: String,
        providerName: String?,
        policyNumber: String?,
        amountInCent: Long,
        validFromEpochDay: Long?,
        validUntilEpochDay: Long,
        reminderEnabled: Boolean,
        note: String?,
    ): Long = database.withTransaction {
        val now = Instant.now()
        val nowMillis = now.toEpochMilli()
        val recordId = renewalRecordDao.insert(
            RenewalRecordEntity(
                vehicleId = vehicleId,
                type = type,
                providerName = providerName?.trim().orEmpty().ifBlank { null },
                policyNumber = policyNumber?.trim().orEmpty().ifBlank { null },
                amountInCent = amountInCent,
                validFromEpochDay = validFromEpochDay,
                validUntilEpochDay = validUntilEpochDay,
                reminderEnabled = reminderEnabled,
                note = note?.trim().orEmpty().ifBlank { null },
                createdAtEpochMillis = nowMillis,
                updatedAtEpochMillis = nowMillis,
            ),
        )

        syncRenewalReminder(
            recordId = recordId,
            vehicleId = vehicleId,
            type = type,
            validUntilEpochDay = validUntilEpochDay,
            reminderEnabled = reminderEnabled,
            note = note,
            nowMillis = nowMillis,
        )

        recordId
    }

    override suspend fun updateRecord(
        recordId: Long,
        type: String,
        providerName: String?,
        policyNumber: String?,
        amountInCent: Long,
        validFromEpochDay: Long?,
        validUntilEpochDay: Long,
        reminderEnabled: Boolean,
        note: String?,
    ) = database.withTransaction {
        val existing = renewalRecordDao.getById(recordId) ?: return@withTransaction
        val nowMillis = Instant.now().toEpochMilli()
        renewalRecordDao.update(
            existing.copy(
                type = type,
                providerName = providerName?.trim().orEmpty().ifBlank { null },
                policyNumber = policyNumber?.trim().orEmpty().ifBlank { null },
                amountInCent = amountInCent,
                validFromEpochDay = validFromEpochDay,
                validUntilEpochDay = validUntilEpochDay,
                reminderEnabled = reminderEnabled,
                note = note?.trim().orEmpty().ifBlank { null },
                updatedAtEpochMillis = nowMillis,
            ),
        )
        syncRenewalReminder(
            recordId = recordId,
            vehicleId = existing.vehicleId,
            type = type,
            validUntilEpochDay = validUntilEpochDay,
            reminderEnabled = reminderEnabled,
            note = note,
            nowMillis = nowMillis,
        )
    }

    override suspend fun deleteRecord(recordId: Long) = database.withTransaction {
        reminderDao.deleteBySource(
            sourceType = ReminderSourceType.RENEWAL_RECORD.name,
            sourceId = recordId,
        )
        renewalRecordDao.deleteById(recordId)
    }

    private suspend fun syncRenewalReminder(
        recordId: Long,
        vehicleId: Long,
        type: String,
        validUntilEpochDay: Long,
        reminderEnabled: Boolean,
        note: String?,
        nowMillis: Long,
    ) {
        val sourceType = ReminderSourceType.RENEWAL_RECORD.name
        if (!reminderEnabled) {
            reminderDao.deleteBySource(sourceType = sourceType, sourceId = recordId)
            return
        }

        val advanceDays = userPreferencesDataSource.preferences.first().reminderAdvanceDays
        val validUntil = LocalDate.ofEpochDay(validUntilEpochDay)
        val remindAtEpochMillis = validUntil
            .minusDays(advanceDays.toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val existingReminder = reminderDao.getBySource(sourceType = sourceType, sourceId = recordId)

        val updatedReminder = ReminderEntity(
            id = existingReminder?.id ?: 0L,
            vehicleId = vehicleId,
            type = type.toReminderType().name,
            title = buildReminderTitle(type, validUntil),
            remindAtEpochMillis = remindAtEpochMillis,
            remindOdometerKm = null,
            advanceDays = advanceDays,
            isEnabled = true,
            status = ReminderStatus.PENDING.name,
            sourceType = sourceType,
            sourceId = recordId,
            note = note?.trim().orEmpty().ifBlank { null },
            lastTriggeredAtEpochMillis = null,
            completedAtEpochMillis = null,
            dismissedAtEpochMillis = null,
            createdAtEpochMillis = existingReminder?.createdAtEpochMillis ?: nowMillis,
            updatedAtEpochMillis = nowMillis,
        )

        if (existingReminder == null) {
            reminderDao.insert(updatedReminder)
        } else {
            reminderDao.update(updatedReminder)
        }
    }
}

class DefaultReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
) : ReminderRepository {
    override fun observeUpcoming(vehicleId: Long): Flow<List<Reminder>> =
        reminderDao.observeUpcomingByVehicleId(vehicleId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun markDone(reminderId: Long) {
        val existing = reminderDao.getById(reminderId) ?: return
        val now = Instant.now().toEpochMilli()
        reminderDao.update(
            existing.copy(
                status = ReminderStatus.DONE.name,
                isEnabled = false,
                completedAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun dismiss(reminderId: Long) {
        val existing = reminderDao.getById(reminderId) ?: return
        val now = Instant.now().toEpochMilli()
        reminderDao.update(
            existing.copy(
                status = ReminderStatus.DISMISSED.name,
                isEnabled = false,
                dismissedAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }
}

private fun String.toReminderType(): ReminderType = when (RenewalType.valueOf(this)) {
    RenewalType.INSURANCE -> ReminderType.INSURANCE
    RenewalType.INSPECTION -> ReminderType.INSPECTION
    RenewalType.TAX -> ReminderType.TAX
    RenewalType.OTHER -> ReminderType.CUSTOM
}

private fun buildReminderTitle(type: String, validUntil: LocalDate): String {
    val label = when (RenewalType.valueOf(type)) {
        RenewalType.INSURANCE -> "保险即将到期"
        RenewalType.INSPECTION -> "年检即将到期"
        RenewalType.TAX -> "车船税即将到期"
        RenewalType.OTHER -> "续期事项即将到期"
    }
    return "$label · $validUntil"
}
