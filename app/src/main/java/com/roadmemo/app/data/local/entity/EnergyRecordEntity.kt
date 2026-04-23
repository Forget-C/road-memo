package com.roadmemo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "energy_records",
    indices = [
        Index("vehicle_id"),
        Index(value = ["vehicle_id", "occurred_at_epoch_millis"]),
        Index(value = ["vehicle_id", "odometer_km"]),
        Index(value = ["vehicle_id", "energy_type", "occurred_at_epoch_millis"]),
    ],
)
data class EnergyRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    @ColumnInfo(name = "occurred_at_epoch_millis")
    val occurredAtEpochMillis: Long,
    @ColumnInfo(name = "energy_type")
    val energyType: String,
    @ColumnInfo(name = "odometer_km")
    val odometerKm: Int,
    @ColumnInfo(name = "quantity_in_thousandth")
    val quantityInThousandth: Long,
    @ColumnInfo(name = "amount_in_cent")
    val amountInCent: Long,
    @ColumnInfo(name = "is_full")
    val isFull: Boolean,
    @ColumnInfo(name = "fuel_label")
    val fuelLabel: String?,
    @ColumnInfo(name = "charge_mode")
    val chargeMode: String?,
    @ColumnInfo(name = "station_name")
    val stationName: String?,
    val note: String?,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
