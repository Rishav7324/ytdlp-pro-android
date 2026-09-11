package com.ytdlp.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ytdlp.app.ui.theme.NovaAqua
import com.ytdlp.app.ui.theme.NovaAquaDeep
import com.ytdlp.app.ui.theme.NovaAquaSoft
import com.ytdlp.app.ui.theme.NovaInk

@Composable
fun LiquidGlassNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        GlassNavItem("home", "Home", Icons.Rounded.Home),
        GlassNavItem("browser", "Browser", Icons.Rounded.Language),
        GlassNavItem("queue", "Queue", Icons.Rounded.Download),
        GlassNavItem("library", "Library", Icons.Rounded.LibraryMusic),
        GlassNavItem("settings", "Settings", Icons.Rounded.Settings)
    )

    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(32.dp)

    val bgBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF132325).copy(alpha = 0.82f),
                Color(0xFF0C1618).copy(alpha = 0.92f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.88f),
                Color(0xFFE8F6F6).copy(alpha = 0.78f)
            )
        )
    }

    val borderBrush = Brush.verticalGradient(
        listOf(
            if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.95f),
            if (isDark) Color(0xFF5C9FA2).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.40f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.6f) else Color(0xFF5C9FA2).copy(alpha = 0.25f),
                spotColor = Color(0xFF5C9FA2).copy(alpha = 0.2f)
            )
            .background(bgBrush, shape)
            .border(1.dp, borderBrush, shape)
            .clip(shape)
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                GlassNavButton(
                    item = item,
                    selected = currentRoute == item.route,
                    isDark = isDark,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.GlassNavButton(
    item: GlassNavItem,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 0.96f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 420f),
        label = "nav-scale-${item.route}"
    )

    val activeColor = if (isDark) NovaAqua else NovaAquaDeep
    val inactiveColor = if (isDark) Color(0xFF88A5A7) else Color(0xFF678587)

    val iconTint by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = spring(stiffness = 500f),
        label = "nav-tint-${item.route}"
    )

    val pillShape = RoundedCornerShape(22.dp)
    val interactionSource = remember { MutableInteractionSource() }

    val pillBackground = if (selected) {
        if (isDark) Color(0xFF1E393C).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.85f)
    } else {
        Color.Transparent
    }

    val pillBorder = if (selected) {
        if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 2.dp)
            .scale(scale)
            .clip(pillShape)
            .background(pillBackground, pillShape)
            .border(1.dp, pillBorder, pillShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.label,
                color = iconTint,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            )
            if (selected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 3.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                )
            }
        }
    }
}

private data class GlassNavItem(val route: String, val label: String, val icon: ImageVector)
