package com.roadmemo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roadmemo.app.data.local.entity.RenewalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RenewalRecordDao {
    @Query("SELECT * FROM renewal_records WHERE vehicle_id = :vehicleId ORDER BY valid_until_epoch_day ASC")
    fun observeByVehicleId(vehicleId: Long): Flow<List<RenewalRecordEntity>>

    @Query("SELECT * FROM renewal_records ORDER BY valid_until_epoch_day ASC")
    suspend fun getAll(): List<RenewalRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RenewalRecordEntity): Long

    @Query("SELECT * FROM renewal_records WHERE id = :recordId LIMIT 1")
    suspend fun getById(recordId: Long): RenewalRecordEntity?

    @Update
    suspend fun update(record: RenewalRecordEntity)

    @Query("DELETE FROM renewal_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)
}
