package com.roadmemo.app.domain.repository

import com.roadmemo.app.domain.model.EnergyRecord
import com.roadmemo.app.domain.model.ExpenseRecord
import com.roadmemo.app.domain.model.MaintenanceRecord
import com.roadmemo.app.domain.model.Reminder
import com.roadmemo.app.domain.model.RenewalRecord
import com.roadmemo.app.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    fun observeDefaultVehicle(): Flow<Vehicle?>
    suspend fun addVehicle(
        brand: String,
        model: String,
        plateNumber: String?,
        powertrainType: String,
        note: String?,
    ): Long
    suspend fun setDefaultVehicle(vehicleId: Long)
}

interface EnergyRepository {
    fun observeRecords(vehicleId: Long): Flow<List<EnergyRecord>>
    fun observeLatest(vehicleId: Long): Flow<EnergyRecord?>
    suspend fun getRecord(recordId: Long): EnergyRecord?
    suspend fun addRecord(
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
    ): Long
    suspend fun updateRecord(
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
    )
    suspend fun deleteRecord(recordId: Long)
}

interface MaintenanceRepository {
    fun observeRecords(vehicleId: Long): Flow<List<MaintenanceRecord>>
    fun observeLatest(vehicleId: Long): Flow<MaintenanceRecord?>
    suspend fun getRecord(recordId: Long): MaintenanceRecord?
    suspend fun addRecord(
        vehicleId: Long,
        maintenanceType: String,
        amountInCent: Long,
        odometerKm: Int?,
        storeName: String?,
        note: String?,
        nextDueDateEpochDay: Long?,
        nextDueOdometerKm: Int?,
    ): Long
    suspend fun updateRecord(
        recordId: Long,
        maintenanceType: String,
        amountInCent: Long,
        odometerKm: Int?,
        storeName: String?,
        note: String?,
        nextDueDateEpochDay: Long?,
        nextDueOdometerKm: Int?,
    )
    suspend fun deleteRecord(recordId: Long)
}

interface ExpenseRepository {
    fun observeRecords(vehicleId: Long): Flow<List<ExpenseRecord>>
    suspend fun getRecord(recordId: Long): ExpenseRecord?
    suspend fun addRecord(
        vehicleId: Long,
        category: String,
        amountInCent: Long,
        note: String?,
    ): Long
    suspend fun updateRecord(
        recordId: Long,
        category: String,
        amountInCent: Long,
        note: String?,
    )
    suspend fun deleteRecord(recordId: Long)
}

interface RenewalRepository {
    fun observeRecords(vehicleId: Long): Flow<List<RenewalRecord>>
    suspend fun getRecord(recordId: Long): RenewalRecord?
    suspend fun addRecord(
        vehicleId: Long,
        type: String,
        providerName: String?,
        policyNumber: String?,
        amountInCent: Long,
        validFromEpochDay: Long?,
        validUntilEpochDay: Long,
        reminderEnabled: Boolean,
        note: String?,
    ): Long
    suspend fun updateRecord(
        recordId: Long,
        type: String,
        providerName: String?,
        policyNumber: String?,
        amountInCent: Long,
        validFromEpochDay: Long?,
        validUntilEpochDay: Long,
        reminderEnabled: Boolean,
        note: String?,
    )
    suspend fun deleteRecord(recordId: Long)
}

interface ReminderRepository {
    fun observeUpcoming(vehicleId: Long): Flow<List<Reminder>>
    suspend fun markDone(reminderId: Long)
    suspend fun dismiss(reminderId: Long)
}
