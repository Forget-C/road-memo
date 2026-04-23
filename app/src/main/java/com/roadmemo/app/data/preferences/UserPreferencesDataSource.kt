package com.roadmemo.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            useDarkTheme = prefs[UseDarkThemeKey] ?: false,
            currencyCode = prefs[CurrencyCodeKey] ?: "CNY",
            distanceUnit = prefs[DistanceUnitKey] ?: "km",
            fuelConsumptionUnit = prefs[FuelConsumptionUnitKey] ?: "L/100km",
            reminderAdvanceDays = prefs[ReminderAdvanceDaysKey] ?: 7,
        )
    }

    suspend fun setReminderAdvanceDays(days: Int) {
        dataStore.edit { prefs ->
            prefs[ReminderAdvanceDaysKey] = days
        }
    }

    private companion object {
        val UseDarkThemeKey = booleanPreferencesKey("use_dark_theme")
        val CurrencyCodeKey = stringPreferencesKey("currency_code")
        val DistanceUnitKey = stringPreferencesKey("distance_unit")
        val FuelConsumptionUnitKey = stringPreferencesKey("fuel_consumption_unit")
        val ReminderAdvanceDaysKey = intPreferencesKey("reminder_advance_days")
    }
}
