package com.ytdlp.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ytdlp.app.ui.screens.HomeScreen
import com.ytdlp.app.ui.screens.LibraryScreen
import com.ytdlp.app.ui.screens.QueueScreen
import com.ytdlp.app.ui.screens.SettingsScreen
import com.ytdlp.app.viewmodel.HomeViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Queue : Screen("queue", "Queue", Icons.Default.Download)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val navItems = listOf(
    Screen.Home,
    Screen.Queue,
    Screen.Library,
    Screen.Settings
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    sharedUrl: String? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    val homeViewModel: HomeViewModel = viewModel()

    if (sharedUrl != null && sharedUrl.isNotBlank()) {
        homeViewModel.onUrlChanged(sharedUrl)
        homeViewModel.parseUrl(sharedUrl)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { screen ->
                    val selected = currentDestination == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            if (currentDestination != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToQueue = {
                        navController.navigate(Screen.Queue.route)
                    }
                )
            }
            composable(Screen.Queue.route) {
                QueueScreen()
            }
            composable(Screen.Library.route) {
                LibraryScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
