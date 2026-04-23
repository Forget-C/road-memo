package com.roadmemo.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.roadmemo.app.data.local.dao.EnergyRecordDao
import com.roadmemo.app.data.local.dao.ExpenseRecordDao
import com.roadmemo.app.data.local.dao.MaintenanceRecordDao
import com.roadmemo.app.data.local.dao.ReminderDao
import com.roadmemo.app.data.local.dao.RenewalRecordDao
import com.roadmemo.app.data.local.dao.VehicleDao
import com.roadmemo.app.data.local.entity.EnergyRecordEntity
import com.roadmemo.app.data.local.entity.ExpenseRecordEntity
import com.roadmemo.app.data.local.entity.MaintenanceRecordEntity
import com.roadmemo.app.data.local.entity.ReminderEntity
import com.roadmemo.app.data.local.entity.RenewalRecordEntity
import com.roadmemo.app.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        EnergyRecordEntity::class,
        MaintenanceRecordEntity::class,
        ExpenseRecordEntity::class,
        RenewalRecordEntity::class,
        ReminderEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class RoadMemoDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun energyRecordDao(): EnergyRecordDao
    abstract fun maintenanceRecordDao(): MaintenanceRecordDao
    abstract fun expenseRecordDao(): ExpenseRecordDao
    abstract fun renewalRecordDao(): RenewalRecordDao
    abstract fun reminderDao(): ReminderDao
}
