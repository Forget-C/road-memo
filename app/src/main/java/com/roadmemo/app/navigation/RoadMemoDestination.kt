package com.roadmemo.app.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.roadmemo.app.R
import com.roadmemo.app.ui.theme.RoadMemoIcons

enum class RoadMemoDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    AddEnergy(
        route = "energy/add",
        labelRes = R.string.tab_records,
        icon = RoadMemoIcons.Records,
    ),
    EditEnergy(
        route = "energy/edit/{recordId}",
        labelRes = R.string.tab_records,
        icon = RoadMemoIcons.Records,
    ),
    AddMaintenance(
        route = "maintenance/add",
        labelRes = R.string.tab_records,
        icon = RoadMemoIcons.Records,
    ),
    EditMaintenance(
        route = "maintenance/edit/{recordId}",
        labelRes = R.string.tab_records,
        icon = RoadMemoIcons.Records,
    ),
    AddRenewal(
        route = "renewal/add",
        labelRes = R.string.tab_records,
        icon = RoadMemoIcons.Records,
    ),
    EditRenewal(
        route = "renewal/edit/{recordId}",
        labelRes = R.string.tab_records,
        icon = RoadMemoIcons.Records,
    ),
    AddExpense(
        route = "expense/add",
        labelRes = R.string.tab_records,
        icon = RoadMemoIcons.Records,
    ),
    EditExpense(
        route = "expense/edit/{recordId}",
        labelRes = R.string.tab_records,
        icon = RoadMemoIcons.Records,
    ),
    Reminders(
        route = "reminders",
        labelRes = R.string.tab_settings,
        icon = RoadMemoIcons.Settings,
    ),
    Vehicles(
        route = "vehicles",
        labelRes = R.string.tab_settings,
        icon = RoadMemoIcons.Settings,
    ),
    Home(
        route = "home",
        labelRes = R.string.tab_home,
        icon = RoadMemoIcons.Home,
    ),
    Records(
        route = "records",
        labelRes = R.string.tab_records,
        icon = RoadMemoIcons.Records,
    ),
    Statistics(
        route = "statistics",
        labelRes = R.string.tab_statistics,
        icon = RoadMemoIcons.Statistics,
    ),
    Settings(
        route = "settings",
        labelRes = R.string.tab_settings,
        icon = RoadMemoIcons.Settings,
    ),
}
