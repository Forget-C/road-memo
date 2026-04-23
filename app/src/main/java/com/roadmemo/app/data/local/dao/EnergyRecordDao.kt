package com.roadmemo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roadmemo.app.data.local.entity.EnergyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnergyRecordDao {
    @Query("SELECT * FROM energy_records WHERE vehicle_id = :vehicleId ORDER BY occurred_at_epoch_millis DESC")
    fun observeByVehicleId(vehicleId: Long): Flow<List<EnergyRecordEntity>>

    @Query("SELECT * FROM energy_records ORDER BY occurred_at_epoch_millis DESC")
    suspend fun getAll(): List<EnergyRecordEntity>

    @Query("SELECT * FROM energy_records WHERE vehicle_id = :vehicleId ORDER BY occurred_at_epoch_millis DESC LIMIT 1")
    fun observeLatestByVehicleId(vehicleId: Long): Flow<EnergyRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: EnergyRecordEntity): Long

    @Query("SELECT * FROM energy_records WHERE id = :recordId LIMIT 1")
    suspend fun getById(recordId: Long): EnergyRecordEntity?

    @Update
    suspend fun update(record: EnergyRecordEntity)

    @Query("DELETE FROM energy_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)
}
