package com.roadmemo.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.roadmemo.app.data.local.dao.EnergyRecordDao
import com.roadmemo.app.data.local.dao.ExpenseRecordDao
import com.roadmemo.app.data.local.dao.MaintenanceRecordDao
import com.roadmemo.app.data.local.dao.ReminderDao
import com.roadmemo.app.data.local.dao.RenewalRecordDao
import com.roadmemo.app.data.local.dao.VehicleDao
import com.roadmemo.app.data.local.database.RoadMemoDatabase
import com.roadmemo.app.data.repository.DefaultEnergyRepository
import com.roadmemo.app.data.repository.DefaultExpenseRepository
import com.roadmemo.app.data.repository.DefaultMaintenanceRepository
import com.roadmemo.app.data.repository.DefaultReminderRepository
import com.roadmemo.app.data.repository.DefaultRenewalRepository
import com.roadmemo.app.data.repository.DefaultVehicleRepository
import com.roadmemo.app.domain.repository.EnergyRepository
import com.roadmemo.app.domain.repository.ExpenseRepository
import com.roadmemo.app.domain.repository.MaintenanceRepository
import com.roadmemo.app.domain.repository.ReminderRepository
import com.roadmemo.app.domain.repository.RenewalRepository
import com.roadmemo.app.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): RoadMemoDatabase = Room.databaseBuilder(
        context,
        RoadMemoDatabase::class.java,
        "roadmemo.db",
    ).build()

    @Provides
    fun provideVehicleDao(database: RoadMemoDatabase): VehicleDao = database.vehicleDao()

    @Provides
    fun provideEnergyRecordDao(database: RoadMemoDatabase): EnergyRecordDao = database.energyRecordDao()

    @Provides
    fun provideMaintenanceRecordDao(database: RoadMemoDatabase): MaintenanceRecordDao = database.maintenanceRecordDao()

    @Provides
    fun provideExpenseRecordDao(database: RoadMemoDatabase): ExpenseRecordDao = database.expenseRecordDao()

    @Provides
    fun provideRenewalRecordDao(database: RoadMemoDatabase): RenewalRecordDao = database.renewalRecordDao()

    @Provides
    fun provideReminderDao(database: RoadMemoDatabase): ReminderDao = database.reminderDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.filesDir.resolve("roadmemo.preferences_pb") },
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {
    @Binds
    abstract fun bindVehicleRepository(impl: DefaultVehicleRepository): VehicleRepository

    @Binds
    abstract fun bindEnergyRepository(impl: DefaultEnergyRepository): EnergyRepository

    @Binds
    abstract fun bindMaintenanceRepository(impl: DefaultMaintenanceRepository): MaintenanceRepository

    @Binds
    abstract fun bindExpenseRepository(impl: DefaultExpenseRepository): ExpenseRepository

    @Binds
    abstract fun bindRenewalRepository(impl: DefaultRenewalRepository): RenewalRepository

    @Binds
    abstract fun bindReminderRepository(impl: DefaultReminderRepository): ReminderRepository
}
