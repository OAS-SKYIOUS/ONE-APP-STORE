package io.github.skyious.oas.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
    object Discover : NavItem("discover", Icons.Filled.Search, "Discover")
    object Categories : NavItem("categories", Icons.Filled.List, "Categories")
    object Library : NavItem("library", Icons.Filled.Star, "My Apps")
        object Fdroid : NavItem("fdroid", Icons.Filled.Shop, "F-Droid")
    object Settings : NavItem("settings", Icons.Filled.Settings, "Settings")
}