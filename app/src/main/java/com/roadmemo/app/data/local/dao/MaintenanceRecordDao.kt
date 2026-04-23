package com.roadmemo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roadmemo.app.data.local.entity.MaintenanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceRecordDao {
    @Query("SELECT * FROM maintenance_records WHERE vehicle_id = :vehicleId ORDER BY occurred_at_epoch_millis DESC")
    fun observeByVehicleId(vehicleId: Long): Flow<List<MaintenanceRecordEntity>>

    @Query("SELECT * FROM maintenance_records ORDER BY occurred_at_epoch_millis DESC")
    suspend fun getAll(): List<MaintenanceRecordEntity>

    @Query("SELECT * FROM maintenance_records WHERE vehicle_id = :vehicleId ORDER BY occurred_at_epoch_millis DESC LIMIT 1")
    fun observeLatestByVehicleId(vehicleId: Long): Flow<MaintenanceRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MaintenanceRecordEntity): Long

    @Query("SELECT * FROM maintenance_records WHERE id = :recordId LIMIT 1")
    suspend fun getById(recordId: Long): MaintenanceRecordEntity?

    @Update
    suspend fun update(record: MaintenanceRecordEntity)

    @Query("DELETE FROM maintenance_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)
}
