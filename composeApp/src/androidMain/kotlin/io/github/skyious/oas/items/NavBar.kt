package io.github.skyious.oas.items

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavSelected: (String) -> Unit
) {
    NavigationBar {
        listOf(
            NavItem.Discover,
            NavItem.Categories,
            NavItem.Library,
            NavItem.Fdroid,
            NavItem.Settings
        ).forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavSelected(item.route) }
            )
        }
    }
}