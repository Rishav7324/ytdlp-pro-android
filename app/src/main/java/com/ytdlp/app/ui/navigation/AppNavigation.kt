package com.ytdlp.app.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.browser.WebBrowserScreen
import com.ytdlp.app.ui.components.LiquidGlassNavigationBar
import com.ytdlp.app.ui.player.AudioPlayerSheet
import com.ytdlp.app.ui.player.MiniPlayerBar
import com.ytdlp.app.ui.player.VideoPlayerView
import com.ytdlp.app.ui.screens.HomeScreen
import com.ytdlp.app.ui.screens.LibraryScreen
import com.ytdlp.app.ui.screens.QueueScreen
import com.ytdlp.app.ui.screens.SettingsScreen
import com.ytdlp.app.viewmodel.HomeViewModel

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Browser : Screen("browser", "Browser")
    object Queue : Screen("queue", "Queue")
    object Library : Screen("library", "Library")
    object Settings : Screen("settings", "Settings")
}

val navItems = listOf(Screen.Home, Screen.Browser, Screen.Queue, Screen.Library, Screen.Settings)

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController(), sharedUrl: String? = null) {
    val homeViewModel: HomeViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    val entry by navController.currentBackStackEntryAsState()
    val currentDestination = entry?.destination?.route
    val playerManager = MediaPlayerManager.getInstance(context)
    val currentMedia by playerManager.currentMedia.collectAsState()
    val isVideoExpanded by playerManager.isVideoExpanded.collectAsState()
    val isAudioSheetOpen by playerManager.isAudioSheetOpen.collectAsState()
    val contentBackdrop = rememberLayerBackdrop()

    androidx.compose.runtime.LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            homeViewModel.onUrlChanged(sharedUrl)
            homeViewModel.parseUrl(sharedUrl)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().layerBackdrop(contentBackdrop)) {
            AnimatedContent(
                targetState = currentDestination ?: Screen.Home.route,
                modifier = Modifier.fillMaxSize().padding(bottom = 104.dp),
                transitionSpec = {
                    (fadeIn(tween(220, easing = FastOutSlowInEasing)) + slideInVertically(initialOffsetY = { it / 30 })) togetherWith
                        (fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { -it / 30 }))
                },
                label = "navigation-transition"
            ) { _ ->
                NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.fillMaxSize()) {
                    composable(Screen.Home.route) {
                        HomeScreen(homeViewModel, onNavigateToQueue = { navController.navigate(Screen.Queue.route) }, onNavigateToBrowser = { navController.navigate(Screen.Browser.route) })
                    }
                    composable(Screen.Browser.route) {
                        WebBrowserScreen(onDownloadUrl = { url ->
                            homeViewModel.onUrlChanged(url)
                            homeViewModel.parseUrl(url)
                            navController.navigate(Screen.Home.route) { launchSingleTop = true }
                        })
                    }
                    composable(Screen.Queue.route) { QueueScreen() }
                    composable(Screen.Library.route) { LibraryScreen() }
                    composable(Screen.Settings.route) { SettingsScreen() }
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedVisibility(
                visible = currentMedia != null && !isVideoExpanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) { MiniPlayerBar() }

            LiquidGlassNavigationBar(
                currentRoute = currentDestination,
                onNavigate = { route ->
                    if (currentDestination != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                backdrop = contentBackdrop,
                modifier = Modifier.navigationBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }

        AnimatedVisibility(visible = isVideoExpanded, enter = slideInVertically(initialOffsetY = { it }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()) {
            VideoPlayerView(onClose = { playerManager.setVideoExpanded(false) })
        }
        AnimatedVisibility(visible = isAudioSheetOpen, enter = fadeIn(), exit = fadeOut()) {
            AudioPlayerSheet(onDismiss = { playerManager.setAudioSheetOpen(false) })
        }
    }
}
