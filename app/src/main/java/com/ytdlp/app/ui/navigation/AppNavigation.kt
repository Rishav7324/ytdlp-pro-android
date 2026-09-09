package com.ytdlp.app.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

val navItems = listOf(Screen.Home, Screen.Browser, Screen.Queue, Screen.Library, Screen.Settings)

@Composable
private fun FloatingNavigationBar(
    currentDestination: String?,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .navigationBarsPadding()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.86f),
        tonalElevation = 8.dp,
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { screen ->
                val selected = currentDestination == screen.route
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.22f else 0.94f,
                    animationSpec = tween(240, easing = FastOutSlowInEasing),
                    label = "nav-scale-${screen.route}"
                )
                val pillAlpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = tween(180),
                    label = "nav-pill-${screen.route}"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(screen) }
                        .padding(vertical = 1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = pillAlpha),
                                RoundedCornerShape(17.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.scale(scale)
                        )
                    }
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    sharedUrl: String? = null
) {
    val context = LocalContext.current
    val entry by navController.currentBackStackEntryAsState()
    val currentDestination = entry?.destination?.route
    val homeViewModel: HomeViewModel = viewModel()
    val playerManager = MediaPlayerManager.getInstance(context)
    val currentMedia by playerManager.currentMedia.collectAsState()
    val isVideoExpanded by playerManager.isVideoExpanded.collectAsState()
    val isAudioSheetOpen by playerManager.isAudioSheetOpen.collectAsState()

    androidx.compose.runtime.LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            homeViewModel.onUrlChanged(sharedUrl)
            homeViewModel.parseUrl(sharedUrl)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    AnimatedVisibility(
                        visible = currentMedia != null && !isVideoExpanded,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) { MiniPlayerBar() }
                    FloatingNavigationBar(
                        currentDestination = currentDestination,
                        onNavigate = { screen ->
                            if (currentDestination != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentDestination ?: Screen.Home.route,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInVertically(initialOffsetY = { it / 28 })) togetherWith
                        (fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { -it / 28 }))
                },
                label = "navigation-transition"
            ) { _ ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToQueue = { navController.navigate(Screen.Queue.route) },
                            onNavigateToBrowser = { navController.navigate(Screen.Browser.route) }
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
                    composable(Screen.Queue.route) { QueueScreen() }
                    composable(Screen.Library.route) { LibraryScreen() }
                    composable(Screen.Settings.route) { SettingsScreen() }
                }
            }
        }

        AnimatedVisibility(
            visible = isVideoExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) { VideoPlayerView(onClose = { playerManager.setVideoExpanded(false) }) }

        AnimatedVisibility(
            visible = isAudioSheetOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) { AudioPlayerSheet(onDismiss = { playerManager.setAudioSheetOpen(false) }) }
    }
}
