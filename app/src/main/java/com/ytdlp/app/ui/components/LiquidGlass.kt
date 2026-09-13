package com.ytdlp.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ytdlp.app.ui.theme.NovaCyan
import com.ytdlp.app.ui.theme.NovaCyanDeep

// Apple-inspired tactile spring specs
val AppleSpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)

val AppleSmoothSpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium
)

/**
 * Ambient Liquid Glass Backdrop Mesh.
 * Gives screens a subtle gradient mesh so liquid glass cards have radiant depth to refract.
 */
@Composable
fun Modifier.ambientLiquidBackground(
    isDark: Boolean = isSystemInDarkTheme()
): Modifier {
    val ambientBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF070F18),
                Color(0xFF0B192A),
                Color(0xFF060D15)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF1F7FD),
                Color(0xFFE5F1FA),
                Color(0xFFEDF5FC)
            )
        )
    }
    return this.background(ambientBrush)
}

/**
 * Ultra iOS-style Liquid Glass Modifier 2.0.
 * Implements high-contrast crystalline/obsidian frosted glass, beveled specular highlights,
 * and colored ambient depth shadows.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    borderAlpha: Float = 0.65f,
    elevation: Dp = 10.dp,
    tintColor: Color? = null,
    isDark: Boolean = isSystemInDarkTheme()
): Modifier {
    val backgroundBrush = if (isDark) {
        val baseTop = tintColor?.copy(alpha = 0.28f) ?: Color(0xFF142436).copy(alpha = 0.78f)
        val baseBottom = Color(0xFF0A1522).copy(alpha = 0.88f)
        Brush.verticalGradient(listOf(baseTop, baseBottom))
    } else {
        val baseTop = tintColor?.copy(alpha = 0.20f) ?: Color.White.copy(alpha = 0.92f)
        val baseBottom = Color(0xFFEAF5FD).copy(alpha = 0.78f)
        Brush.verticalGradient(listOf(baseTop, baseBottom))
    }

    // Beveled specular rim reflection
    val borderBrush = Brush.linearGradient(
        listOf(
            if (isDark) Color.White.copy(alpha = 0.55f * borderAlpha) else Color.White.copy(alpha = 0.98f),
            if (isDark) NovaCyan.copy(alpha = 0.40f * borderAlpha) else Color(0xFF00B4D8).copy(alpha = 0.45f),
            if (isDark) Color.White.copy(alpha = 0.15f * borderAlpha) else Color.White.copy(alpha = 0.30f)
        )
    )

    val ambientGlow = if (isDark) {
        tintColor?.copy(alpha = 0.25f) ?: Color(0xFF00E5FF).copy(alpha = 0.12f)
    } else {
        Color(0xFF0077B6).copy(alpha = 0.12f)
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.65f) else ambientGlow,
            spotColor = ambientGlow
        )
        .background(backgroundBrush, shape)
        .border(1.2.dp, borderBrush, shape)
}

/**
 * Interactive Liquid Glass Card with spring physics on touch.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color? = null,
    borderAlpha: Float = 0.65f,
    elevation: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.965f else 1.0f,
        animationSpec = AppleSpringSpec,
        label = "liquid-glass-card-scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .liquidGlass(shape = shape, borderAlpha = borderAlpha, elevation = elevation, tintColor = tintColor)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        content = content
    )
}

/**
 * Capsule / Pill for tags, chips, and quick action triggers.
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    elevation: Dp = 4.dp,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.92f else 1.0f,
        animationSpec = AppleSpringSpec,
        label = "pill-scale"
    )

    val shape = CircleShape
    val isDark = isSystemInDarkTheme()

    val bgBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(
                selectedColor.copy(alpha = if (isDark) 0.90f else 0.95f),
                if (isDark) NovaCyanDeep else NovaCyan
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                if (isDark) Color(0xFF162B3D).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.92f),
                if (isDark) Color(0xFF0F1E2C).copy(alpha = 0.85f) else Color(0xFFE5F1FA).copy(alpha = 0.70f)
            )
        )
    }

    val borderBrush = Brush.linearGradient(
        listOf(
            if (isSelected) Color.White.copy(alpha = 0.90f) else if (isDark) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.98f),
            if (isSelected) Color.White.copy(alpha = 0.40f) else if (isDark) NovaCyan.copy(alpha = 0.30f) else Color(0xFF00B4D8).copy(alpha = 0.45f)
        )
    )

    Row(
        modifier = modifier
            .scale(scale)
            .shadow(elevation = elevation, shape = shape, spotColor = if (isSelected) selectedColor.copy(alpha = 0.35f) else Color.Transparent)
            .background(bgBrush, shape)
            .border(1.2.dp, borderBrush, shape)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
