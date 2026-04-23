package com.roadmemo.app.data.preferences

data class UserPreferences(
    val useDarkTheme: Boolean = false,
    val currencyCode: String = "CNY",
    val distanceUnit: String = "km",
    val fuelConsumptionUnit: String = "L/100km",
    val reminderAdvanceDays: Int = 7,
)
