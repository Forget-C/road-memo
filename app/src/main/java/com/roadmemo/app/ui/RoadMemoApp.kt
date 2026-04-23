package com.roadmemo.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roadmemo.app.navigation.RoadMemoDestination
import com.roadmemo.app.navigation.RoadMemoNavHost
import com.roadmemo.app.ui.theme.RoadMemoTheme

@Composable
fun RoadMemoApp() {
    RoadMemoTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            bottomBar = {
                NavigationBar {
                    RoadMemoDestination.entries
                        .filterNot {
                            it == RoadMemoDestination.Vehicles ||
                                it == RoadMemoDestination.AddEnergy ||
                                it == RoadMemoDestination.EditEnergy ||
                                it == RoadMemoDestination.AddMaintenance ||
                                it == RoadMemoDestination.EditMaintenance ||
                                it == RoadMemoDestination.AddRenewal ||
                                it == RoadMemoDestination.EditRenewal ||
                                it == RoadMemoDestination.AddExpense ||
                                it == RoadMemoDestination.EditExpense ||
                                it == RoadMemoDestination.Reminders
                        }
                        .forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(text = stringResource(destination.labelRes))
                            },
                        )
                    }
                }
            },
        ) { innerPadding ->
            RoadMemoNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
