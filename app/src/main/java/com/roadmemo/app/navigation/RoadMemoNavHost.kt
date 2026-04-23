package com.roadmemo.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.roadmemo.app.ui.screens.energy.EnergyRecordScreen
import com.roadmemo.app.ui.screens.expense.ExpenseRecordScreen
import com.roadmemo.app.ui.screens.home.HomeScreen
import com.roadmemo.app.ui.screens.maintenance.MaintenanceRecordScreen
import com.roadmemo.app.ui.screens.reminder.ReminderScreen
import com.roadmemo.app.ui.screens.records.RecordsScreen
import com.roadmemo.app.ui.screens.renewal.RenewalRecordScreen
import com.roadmemo.app.ui.screens.settings.SettingsScreen
import com.roadmemo.app.ui.screens.statistics.StatisticsScreen
import com.roadmemo.app.ui.screens.vehicle.VehicleScreen

@Composable
fun RoadMemoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = RoadMemoDestination.Home.route,
        modifier = modifier,
    ) {
        composable(RoadMemoDestination.Home.route) {
            HomeScreen(
                onOpenVehicles = { navController.navigate(RoadMemoDestination.Vehicles.route) },
                onAddEnergyRecord = { navController.navigate(RoadMemoDestination.AddEnergy.route) },
                onAddMaintenanceRecord = { navController.navigate(RoadMemoDestination.AddMaintenance.route) },
                onAddExpenseRecord = { navController.navigate(RoadMemoDestination.AddExpense.route) },
                onAddRenewalRecord = { navController.navigate(RoadMemoDestination.AddRenewal.route) },
                onOpenReminders = { navController.navigate(RoadMemoDestination.Reminders.route) },
            )
        }
        composable(RoadMemoDestination.Records.route) {
            RecordsScreen(
                onAddEnergyRecord = { navController.navigate(RoadMemoDestination.AddEnergy.route) },
                onAddMaintenanceRecord = { navController.navigate(RoadMemoDestination.AddMaintenance.route) },
                onAddExpenseRecord = { navController.navigate(RoadMemoDestination.AddExpense.route) },
                onAddRenewalRecord = { navController.navigate(RoadMemoDestination.AddRenewal.route) },
                onEditEnergyRecord = { recordId -> navController.navigate("energy/edit/$recordId") },
                onEditMaintenanceRecord = { recordId -> navController.navigate("maintenance/edit/$recordId") },
                onEditExpenseRecord = { recordId -> navController.navigate("expense/edit/$recordId") },
                onEditRenewalRecord = { recordId -> navController.navigate("renewal/edit/$recordId") },
            )
        }
        composable(RoadMemoDestination.Statistics.route) {
            StatisticsScreen()
        }
        composable(RoadMemoDestination.Settings.route) {
            SettingsScreen(
                onOpenVehicles = { navController.navigate(RoadMemoDestination.Vehicles.route) },
                onOpenReminders = { navController.navigate(RoadMemoDestination.Reminders.route) },
            )
        }
        composable(RoadMemoDestination.Reminders.route) {
            ReminderScreen(
                onOpenSource = { reminder ->
                    when (reminder.sourceType) {
                        com.roadmemo.app.domain.model.ReminderSourceType.RENEWAL_RECORD -> {
                            reminder.sourceId?.let { navController.navigate("renewal/edit/$it") }
                        }
                        com.roadmemo.app.domain.model.ReminderSourceType.MAINTENANCE_RECORD -> {
                            reminder.sourceId?.let { navController.navigate("maintenance/edit/$it") }
                        }
                        com.roadmemo.app.domain.model.ReminderSourceType.MANUAL, null -> Unit
                    }
                },
            )
        }
        composable(RoadMemoDestination.Vehicles.route) {
            VehicleScreen(
                onBackToHome = { navController.popBackStack() },
            )
        }
        composable(RoadMemoDestination.AddEnergy.route) {
            EnergyRecordScreen(
                onComplete = { navController.popBackStack() },
            )
        }
        composable(
            route = RoadMemoDestination.EditEnergy.route,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
        ) {
            EnergyRecordScreen(
                onComplete = { navController.popBackStack() },
            )
        }
        composable(RoadMemoDestination.AddMaintenance.route) {
            MaintenanceRecordScreen(
                onComplete = { navController.popBackStack() },
            )
        }
        composable(
            route = RoadMemoDestination.EditMaintenance.route,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
        ) {
            MaintenanceRecordScreen(
                onComplete = { navController.popBackStack() },
            )
        }
        composable(RoadMemoDestination.AddExpense.route) {
            ExpenseRecordScreen(
                onComplete = { navController.popBackStack() },
            )
        }
        composable(
            route = RoadMemoDestination.EditExpense.route,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
        ) {
            ExpenseRecordScreen(
                onComplete = { navController.popBackStack() },
            )
        }
        composable(RoadMemoDestination.AddRenewal.route) {
            RenewalRecordScreen(
                onComplete = { navController.popBackStack() },
            )
        }
        composable(
            route = RoadMemoDestination.EditRenewal.route,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
        ) {
            RenewalRecordScreen(
                onComplete = { navController.popBackStack() },
            )
        }
    }
}
