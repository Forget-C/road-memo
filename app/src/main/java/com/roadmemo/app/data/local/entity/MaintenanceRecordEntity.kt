package com.roadmemo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_records",
    indices = [
        Index("vehicle_id"),
        Index(value = ["vehicle_id", "occurred_at_epoch_millis"]),
        Index(value = ["vehicle_id", "next_due_date_epoch_day"]),
    ],
)
data class MaintenanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    @ColumnInfo(name = "occurred_at_epoch_millis")
    val occurredAtEpochMillis: Long,
    @ColumnInfo(name = "odometer_km")
    val odometerKm: Int?,
    @ColumnInfo(name = "maintenance_type")
    val maintenanceType: String,
    @ColumnInfo(name = "amount_in_cent")
    val amountInCent: Long,
    @ColumnInfo(name = "store_name")
    val storeName: String?,
    val note: String?,
    @ColumnInfo(name = "next_due_date_epoch_day")
    val nextDueDateEpochDay: Long?,
    @ColumnInfo(name = "next_due_odometer_km")
    val nextDueOdometerKm: Int?,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
