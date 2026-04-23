package com.roadmemo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_records",
    indices = [
        Index("vehicle_id"),
        Index(value = ["vehicle_id", "occurred_at_epoch_millis"]),
        Index(value = ["vehicle_id", "category", "occurred_at_epoch_millis"]),
    ],
)
data class ExpenseRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    @ColumnInfo(name = "occurred_at_epoch_millis")
    val occurredAtEpochMillis: Long,
    val category: String,
    @ColumnInfo(name = "amount_in_cent")
    val amountInCent: Long,
    val note: String?,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
