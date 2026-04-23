package com.roadmemo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "renewal_records",
    indices = [
        Index("vehicle_id"),
        Index(value = ["vehicle_id", "type"]),
        Index(value = ["vehicle_id", "valid_until_epoch_day"]),
    ],
)
data class RenewalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    val type: String,
    @ColumnInfo(name = "provider_name")
    val providerName: String?,
    @ColumnInfo(name = "policy_number")
    val policyNumber: String?,
    @ColumnInfo(name = "amount_in_cent")
    val amountInCent: Long,
    @ColumnInfo(name = "valid_from_epoch_day")
    val validFromEpochDay: Long?,
    @ColumnInfo(name = "valid_until_epoch_day")
    val validUntilEpochDay: Long,
    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean,
    val note: String?,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
