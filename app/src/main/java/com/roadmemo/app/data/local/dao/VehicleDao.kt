package com.roadmemo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roadmemo.app.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY is_default DESC, updated_at_epoch_millis DESC")
    fun observeVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles ORDER BY is_default DESC, updated_at_epoch_millis DESC")
    suspend fun getAll(): List<VehicleEntity>

    @Query("SELECT * FROM vehicles WHERE is_default = 1 LIMIT 1")
    fun observeDefaultVehicle(): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId LIMIT 1")
    suspend fun getById(vehicleId: Long): VehicleEntity?

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun countVehicles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: VehicleEntity): Long

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET is_default = 0")
    suspend fun clearDefaultFlag()

    @Query("UPDATE vehicles SET is_default = 1, updated_at_epoch_millis = :updatedAtEpochMillis WHERE id = :vehicleId")
    suspend fun setDefaultVehicle(
        vehicleId: Long,
        updatedAtEpochMillis: Long,
    )

    @Query("DELETE FROM vehicles WHERE id = :vehicleId")
    suspend fun deleteById(vehicleId: Long)
}
