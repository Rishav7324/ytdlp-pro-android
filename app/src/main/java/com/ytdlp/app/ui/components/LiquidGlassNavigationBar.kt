package com.ytdlp.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ytdlp.app.ui.theme.NovaAqua
import com.ytdlp.app.ui.theme.NovaAquaSoft
import com.ytdlp.app.ui.theme.NovaInk

@Composable
fun LiquidGlassNavigationBar(currentRoute: String?, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    val items = listOf(
        GlassNavItem("home", "Home", Icons.Rounded.Home),
        GlassNavItem("browser", "Browser", Icons.Rounded.Language),
        GlassNavItem("queue", "Queue", Icons.Rounded.Download),
        GlassNavItem("library", "Library", Icons.Rounded.LibraryMusic),
        GlassNavItem("settings", "Settings", Icons.Rounded.Settings)
    )
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier = modifier.fillMaxWidth().height(76.dp)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = .92f), NovaAquaSoft.copy(alpha = .80f))), shape)
            .border(1.dp, Color.White.copy(alpha = .88f), shape).padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
    ) { items.forEach { item -> GlassNavButton(item, currentRoute == item.route) { onNavigate(item.route) } } }
}

@Composable
private fun RowScope.GlassNavButton(item: GlassNavItem, selected: Boolean, onClick: () -> Unit) {
    val scale = animateFloatAsState(if (selected) 1.16f else .94f, spring(dampingRatio = .62f, stiffness = 520f), label = "nav-scale-${item.route}")
    val tint = animateColorAsState(if (selected) NovaInk else MaterialTheme.colorScheme.onSurfaceVariant, spring(stiffness = 500f), label = "nav-tint-${item.route}")
    val pillShape = RoundedCornerShape(20.dp)
    Box(Modifier.weight(1f).height(64.dp).padding(horizontal = 2.dp).clip(pillShape)
        .background(if (selected) Color.White.copy(alpha = .72f) else Color.Transparent, pillShape)
        .clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(item.icon, item.label, tint = tint.value, modifier = Modifier.size(24.dp).scale(scale.value))
            if (selected) {
                Text(item.label, color = NovaInk, style = MaterialTheme.typography.labelSmall)
                Box(Modifier.padding(top = 2.dp).size(4.dp).background(NovaAqua, RoundedCornerShape(50.dp)))
            }
        }
    }
}

private data class GlassNavItem(val route: String, val label: String, val icon: ImageVector)
