package com.ytdlp.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.browser.WebBrowserScreen
import com.ytdlp.app.ui.components.LiquidGlassNavigationBar
import com.ytdlp.app.ui.player.AudioPlayerSheet
import com.ytdlp.app.ui.player.MiniPlayerBar
import com.ytdlp.app.ui.player.VideoPlayerView
import com.ytdlp.app.ui.screens.HomeScreen
import com.ytdlp.app.ui.screens.LegalScreen
import com.ytdlp.app.ui.screens.LibraryScreen
import com.ytdlp.app.ui.screens.PermissionScreen
import com.ytdlp.app.ui.screens.QueueScreen
import com.ytdlp.app.ui.screens.SettingsScreen
import com.ytdlp.app.ui.screens.SplashScreen
import com.ytdlp.app.viewmodel.HomeViewModel

sealed class Screen(val route: String, val title: String) {
    object Splash : Screen("splash", "Splash")
    object Permissions : Screen("permissions", "Permissions")
    object Home : Screen("home", "Home")
    object Browser : Screen("browser", "Browser")
    object Queue : Screen("queue", "Queue")
    object Library : Screen("library", "Library")
    object Settings : Screen("settings", "Settings")
    object Legal : Screen("legal", "Legal")
}

val navItems = listOf(Screen.Home, Screen.Browser, Screen.Queue, Screen.Library, Screen.Settings)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    sharedUrl: String? = null
) {
    val homeViewModel: HomeViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    val entry by navController.currentBackStackEntryAsState()
    val currentDestination = entry?.destination?.route
    val playerManager = MediaPlayerManager.getInstance(context)
    val currentMedia by playerManager.currentMedia.collectAsState()
    val isVideoExpanded by playerManager.isVideoExpanded.collectAsState()
    val isAudioSheetOpen by playerManager.isAudioSheetOpen.collectAsState()

    val isMainTab = currentDestination in listOf(
        Screen.Home.route,
        Screen.Browser.route,
        Screen.Queue.route,
        Screen.Library.route,
        Screen.Settings.route
    )

    androidx.compose.runtime.LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            homeViewModel.onUrlChanged(sharedUrl)
            homeViewModel.parseUrl(sharedUrl)
            if (currentDestination != Screen.Home.route) {
                navController.navigate(Screen.Home.route) {
                    launchSingleTop = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(tween(220, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(160)) }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateNext = { destinationRoute ->
                        navController.navigate(destinationRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Permissions.route) {
                PermissionScreen(
                    onPermissionsCompleted = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Permissions.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    homeViewModel,
                    onNavigateToQueue = { navController.navigate(Screen.Queue.route) },
                    onNavigateToBrowser = { url ->
                        if (url.isNotBlank()) {
                            homeViewModel.onUrlChanged(url)
                            homeViewModel.parseUrl(url)
                        }
                        navController.navigate(Screen.Browser.route)
                    }
                )
            }

            composable(Screen.Browser.route) {
                WebBrowserScreen(
                    onDownloadUrl = { url ->
                        homeViewModel.onUrlChanged(url)
                        homeViewModel.parseUrl(url)
                        navController.navigate(Screen.Home.route) { launchSingleTop = true }
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
                SettingsScreen(
                    onOpenLegal = { navController.navigate(Screen.Legal.route) }
                )
            }

            composable(Screen.Legal.route) {
                LegalScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Bottom Navigation Bar & Mini Player
        if (isMainTab) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Bottom
            ) {
                AnimatedVisibility(
                    visible = currentMedia != null && !isVideoExpanded,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    MiniPlayerBar()
                }

                LiquidGlassNavigationBar(
                    currentRoute = currentDestination,
                    onNavigate = { route ->
                        if (currentDestination != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Expanded Video Player
        AnimatedVisibility(
            visible = isVideoExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(200)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(200))
        ) {
            VideoPlayerView(onClose = { playerManager.setVideoExpanded(false) })
        }

        // Audio Player Sheet
        AnimatedVisibility(
            visible = isAudioSheetOpen,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            AudioPlayerSheet(onDismiss = { playerManager.setAudioSheetOpen(false) })
        }
    }
}
