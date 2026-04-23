package com.roadmemo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roadmemo.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query(
        """
        SELECT * FROM reminders
        WHERE vehicle_id = :vehicleId
          AND is_enabled = 1
          AND status IN ('PENDING', 'TRIGGERED')
        ORDER BY remind_at_epoch_millis ASC
        """
    )
    fun observeUpcomingByVehicleId(vehicleId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY updated_at_epoch_millis DESC")
    suspend fun getAll(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ReminderEntity): Long

    @Query("SELECT * FROM reminders WHERE id = :reminderId LIMIT 1")
    suspend fun getById(reminderId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE source_type = :sourceType AND source_id = :sourceId LIMIT 1")
    suspend fun getBySource(sourceType: String, sourceId: Long): ReminderEntity?

    @Update
    suspend fun update(record: ReminderEntity)

    @Query("DELETE FROM reminders WHERE source_type = :sourceType AND source_id = :sourceId")
    suspend fun deleteBySource(sourceType: String, sourceId: Long)
}
