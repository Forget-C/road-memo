package com.roadmemo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    indices = [
        Index("is_default"),
        Index(value = ["brand", "model"]),
    ],
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val brand: String,
    val model: String,
    @ColumnInfo(name = "plate_number")
    val plateNumber: String?,
    @ColumnInfo(name = "purchase_date_epoch_day")
    val purchaseDateEpochDay: Long?,
    @ColumnInfo(name = "powertrain_type")
    val powertrainType: String,
    val note: String?,
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
