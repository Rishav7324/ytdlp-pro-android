package com.ytdlp.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.browser.WebBrowserScreen
import com.ytdlp.app.ui.player.AudioPlayerSheet
import com.ytdlp.app.ui.player.MiniPlayerBar
import com.ytdlp.app.ui.player.VideoPlayerView
import com.ytdlp.app.ui.screens.HomeScreen
import com.ytdlp.app.ui.screens.LibraryScreen
import com.ytdlp.app.ui.screens.QueueScreen
import com.ytdlp.app.ui.screens.SettingsScreen
import com.ytdlp.app.viewmodel.HomeViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Browser : Screen("browser", "Browser", Icons.Default.Language)
    object Queue : Screen("queue", "Queue", Icons.Default.Download)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val navItems = listOf(
    Screen.Home,
    Screen.Browser,
    Screen.Queue,
    Screen.Library,
    Screen.Settings
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    sharedUrl: String? = null
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    val homeViewModel: HomeViewModel = viewModel()

    val playerManager = MediaPlayerManager.getInstance(context)
    val currentMedia by playerManager.currentMedia.collectAsState()
    val isVideoExpanded by playerManager.isVideoExpanded.collectAsState()
    val isAudioSheetOpen by playerManager.isAudioSheetOpen.collectAsState()

    if (sharedUrl != null && sharedUrl.isNotBlank()) {
        homeViewModel.onUrlChanged(sharedUrl)
        homeViewModel.parseUrl(sharedUrl)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    // Persistent Mini Player above Bottom Bar
                    if (currentMedia != null && !isVideoExpanded) {
                        MiniPlayerBar()
                    }

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
                composable(Screen.Browser.route) {
                    WebBrowserScreen(
                        onDownloadUrl = { url ->
                            homeViewModel.onUrlChanged(url)
                            homeViewModel.parseUrl(url)
                            navController.navigate(Screen.Home.route)
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

        // Fullscreen Video Player Overlay
        AnimatedVisibility(
            visible = isVideoExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            VideoPlayerView(
                onClose = { playerManager.setVideoExpanded(false) }
            )
        }

        // Expanded Audio Music Player Modal
        if (isAudioSheetOpen) {
            AudioPlayerSheet(
                onDismiss = { playerManager.setAudioSheetOpen(false) }
            )
        }
    }
}
