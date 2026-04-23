package com.roadmemo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [
        Index("vehicle_id"),
        Index(value = ["is_enabled", "remind_at_epoch_millis"]),
        Index(value = ["vehicle_id", "type"]),
        Index(value = ["status", "remind_at_epoch_millis"]),
    ],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    val type: String,
    val title: String,
    @ColumnInfo(name = "remind_at_epoch_millis")
    val remindAtEpochMillis: Long?,
    @ColumnInfo(name = "remind_odometer_km")
    val remindOdometerKm: Int?,
    @ColumnInfo(name = "advance_days")
    val advanceDays: Int?,
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean,
    val status: String,
    @ColumnInfo(name = "source_type")
    val sourceType: String?,
    @ColumnInfo(name = "source_id")
    val sourceId: Long?,
    val note: String?,
    @ColumnInfo(name = "last_triggered_at_epoch_millis")
    val lastTriggeredAtEpochMillis: Long?,
    @ColumnInfo(name = "completed_at_epoch_millis")
    val completedAtEpochMillis: Long?,
    @ColumnInfo(name = "dismissed_at_epoch_millis")
    val dismissedAtEpochMillis: Long?,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
