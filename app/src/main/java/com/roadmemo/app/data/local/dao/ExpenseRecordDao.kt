package com.roadmemo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roadmemo.app.data.local.entity.ExpenseRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseRecordDao {
    @Query("SELECT * FROM expense_records WHERE vehicle_id = :vehicleId ORDER BY occurred_at_epoch_millis DESC")
    fun observeByVehicleId(vehicleId: Long): Flow<List<ExpenseRecordEntity>>

    @Query("SELECT * FROM expense_records ORDER BY occurred_at_epoch_millis DESC")
    suspend fun getAll(): List<ExpenseRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ExpenseRecordEntity): Long

    @Query("SELECT * FROM expense_records WHERE id = :recordId LIMIT 1")
    suspend fun getById(recordId: Long): ExpenseRecordEntity?

    @Update
    suspend fun update(record: ExpenseRecordEntity)

    @Query("DELETE FROM expense_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)
}
