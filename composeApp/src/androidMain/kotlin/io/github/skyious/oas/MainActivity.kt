// androidApp/src/main/java/io/github/skyious/oas/MainActivity.kt
package io.github.skyious.oas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.ui.SharedViewModel
import io.github.skyious.oas.screens.Discoverscreen
import io.github.skyious.oas.screens.Categoriesscreen
import io.github.skyious.oas.screens.Libraryscreen
import io.github.skyious.oas.screens.Settingsscreen
import io.github.skyious.oas.screens.DetailScreen
import io.github.skyious.oas.items.BottomNavBar
import io.github.skyious.oas.items.NavItem
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Shared ViewModel for selection
                val sharedVM: SharedViewModel = viewModel<SharedViewModel>()
                val navController = rememberNavController()
                // Observe nav backstack to determine current route for bottom bar highlight
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                var darkModeEnabled by rememberSaveable { mutableStateOf(false) }


                // You may want to hide bottom bar on detail screen:
                val showBottomBar = when (currentRoute) {
                    NavItem.Discover.route,
                    NavItem.Categories.route,
                    NavItem.Library.route,
                    NavItem.Settings.route -> true
                    else -> false
                }

                AppTheme(darkTheme = darkModeEnabled) {
                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavBar(currentRoute.toString()) { selectedRoute ->
                                    // navigate to the selected bottom tab
                                    navController.navigate(selectedRoute) {
                                        // Pop up to the start destination of graph to avoid building back stack
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = NavItem.Discover.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(NavItem.Discover.route) {
                                Discoverscreen(
                                    onAppClick = { appInfo ->
                                        sharedVM.selectApp(appInfo)
                                        navController.navigate("detail")
                                    }
                                )
                            }
                            composable(NavItem.Categories.route) {
                                Categoriesscreen()
                            }
                            composable(NavItem.Library.route) {
                                Libraryscreen()
                            }
                            composable(NavItem.Settings.route) {
                                Settingsscreen(
                                    notificationsEnabled = true,
                                    onNotificationsChanged = { /*...*/ },
                                    darkModeEnabled = darkModeEnabled,
                                    onDarkModeChanged = { darkModeEnabled = it}
                                )
                            }
                            composable("detail") {
                                val app = sharedVM.selectedApp.collectAsState().value
                                if (app != null) {
                                    DetailScreen(appInfo = app as AppInfo)
                                } else {
                                    LaunchedEffect(Unit) {
                                        navController.popBackStack()
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable

actual fun AppLogo() {
    Image(
        painter = androidx.compose.ui.res.painterResource(id = R.drawable.oas_logo),
        contentDescription = "Logo",
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
    )
}