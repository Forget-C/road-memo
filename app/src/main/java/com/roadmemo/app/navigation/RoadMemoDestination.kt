package com.roadmemo.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.roadmemo.app.R

enum class RoadMemoDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    AddEnergy(
        route = "energy/add",
        labelRes = R.string.tab_records,
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    EditEnergy(
        route = "energy/edit/{recordId}",
        labelRes = R.string.tab_records,
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    AddMaintenance(
        route = "maintenance/add",
        labelRes = R.string.tab_records,
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    EditMaintenance(
        route = "maintenance/edit/{recordId}",
        labelRes = R.string.tab_records,
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    AddRenewal(
        route = "renewal/add",
        labelRes = R.string.tab_records,
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    EditRenewal(
        route = "renewal/edit/{recordId}",
        labelRes = R.string.tab_records,
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    AddExpense(
        route = "expense/add",
        labelRes = R.string.tab_records,
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    EditExpense(
        route = "expense/edit/{recordId}",
        labelRes = R.string.tab_records,
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    Reminders(
        route = "reminders",
        labelRes = R.string.tab_settings,
        icon = Icons.Outlined.Settings,
    ),
    Vehicles(
        route = "vehicles",
        labelRes = R.string.tab_settings,
        icon = Icons.Outlined.Settings,
    ),
    Home(
        route = "home",
        labelRes = R.string.tab_home,
        icon = Icons.Outlined.Home,
    ),
    Records(
        route = "records",
        labelRes = R.string.tab_records,
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    Statistics(
        route = "statistics",
        labelRes = R.string.tab_statistics,
        icon = Icons.Outlined.BarChart,
    ),
    Settings(
        route = "settings",
        labelRes = R.string.tab_settings,
        icon = Icons.Outlined.Settings,
    ),
}
