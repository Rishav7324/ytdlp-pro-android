package com.ytdlp.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NovaPrimary,
    onPrimary = ColorOnPrimaryDark,
    primaryContainer = NovaPrimaryContainerDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = NovaSecondary,
    onSecondary = ColorOnSecondaryDark,
    tertiary = NovaTertiary,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = NovaPrimaryDark,
    onPrimary = ColorOnPrimaryLight,
    primaryContainer = NovaPrimaryContainerLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = NovaSecondary,
    onSecondary = ColorOnSecondaryLight,
    tertiary = NovaTertiary,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight
)

@Composable
fun YtDlpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = NovaShapes,
        content = content
    )
}

private val ColorOnPrimaryDark = BackgroundDark
private val ColorOnSecondaryDark = BackgroundDark
private val NovaPrimaryContainerDark = Color(0xFF241C4A)
private val ColorOnPrimaryLight = Color.White
private val ColorOnSecondaryLight = BackgroundDark
private val NovaPrimaryContainerLight = Color(0xFFE7E0FF)
