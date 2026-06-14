package uk.co.inkbinder.noto.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uk.co.inkbinder.noto.di.AppContainer
import uk.co.inkbinder.noto.feature.day.DayDetailRoute
import uk.co.inkbinder.noto.feature.home.HomeRoute
import uk.co.inkbinder.noto.feature.settings.SettingsRoute
import uk.co.inkbinder.noto.feature.tags.TagsRoute
import uk.co.inkbinder.noto.navigation.NotoDestination
import uk.co.inkbinder.noto.navigation.topLevelDestinations

@Composable
fun NotoApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination?.route != NotoDestination.DayDetail.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = {
                                when (destination) {
                                    NotoDestination.Home -> Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                    NotoDestination.Tags -> Icon(Icons.Default.Style, contentDescription = null)
                                    NotoDestination.Settings -> Icon(Icons.Default.Settings, contentDescription = null)
                                    NotoDestination.DayDetail -> Unit
                                }
                            },
                            label = {
                                destination.labelRes?.let { Text(stringResource(it)) }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NotoDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(NotoDestination.Home.route) {
                HomeRoute(
                    appContainer = appContainer,
                    onOpenDay = { dayKey ->
                        navController.navigate(NotoDestination.DayDetail.routeFor(dayKey))
                    },
                )
            }
            composable(NotoDestination.Tags.route) {
                TagsRoute(appContainer = appContainer)
            }
            composable(NotoDestination.Settings.route) {
                SettingsRoute(appContainer = appContainer)
            }
            composable(
                route = NotoDestination.DayDetail.route,
                arguments = listOf(navArgument("date") { type = NavType.StringType }),
            ) { backStack ->
                val dayKey = backStack.arguments?.getString("date").orEmpty()
                DayDetailRoute(
                    appContainer = appContainer,
                    dayKey = dayKey,
                    onBack = navController::popBackStack,
                )
            }
        }
    }
}
