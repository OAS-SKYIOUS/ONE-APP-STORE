//package io.github.skyious.oas
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.List
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.vector.ImageVector
//import io.github.skyious.oas.screens.SearchScreen
//import org.jetbrains.compose.ui.tooling.preview.Preview
//
//sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
//    object Discover : Screen("discover", "Discover", Icons.Default.Home)
//    object Search : Screen("search", "Search", Icons.Default.Search)
//    object Categories : Screen("categories", "Categories", Icons.Default.List)
//    object Library : Screen("library", "Library", Icons.Default.Star)
//    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
//}
//
//val items = listOf(
//    Screen.Discover,
//    Screen.Search,
//    Screen.Categories,
//    Screen.Library,
//    Screen.Settings
//)
//
//@Preview
//@Composable
//fun App() {
//    MaterialTheme {
//        var currentScreen: Screen by remember { mutableStateOf(Screen.Discover) }
//
//        Scaffold(
//            bottomBar = {
//                NavigationBar {
//                    items.forEach { screen ->
//                        NavigationBarItem(
//                            icon = { Icon(screen.icon, contentDescription = screen.title) },
//                            label = { Text(screen.title) },
//                            selected = currentScreen == screen,
//                            onClick = { currentScreen = screen }
//                        )
//                    }
//                }
//            }
//        ) { innerPadding ->
//            Box(modifier = Modifier.padding(innerPadding)) {
//                // Simple navigation based on the selected screen
//                when (currentScreen) {
//                    Screen.Discover -> Discoverscreen()
//                    Screen.Search -> SearchScreen()
//                    Screen.Categories -> Text("Categories Screen") // Placeholder
//                    Screen.Library -> Text("Installed Apps Screen") // Placeholder
//                    Screen.Settings -> Text("Settings Screen") // Placeholder
//                }
//            }
//        }
//    }
//}