package io.github.skyious.oas

import AppDetailsScreen
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import io.github.skyious.oas.data.IndexRepository
import io.github.skyious.oas.data.SettingsRepository
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.ui.SharedViewModel
import io.github.skyious.oas.screens.Discoverscreen
import io.github.skyious.oas.screens.Categoriesscreen
import io.github.skyious.oas.screens.LibraryScreen
import io.github.skyious.oas.screens.Settingsscreen
import io.github.skyious.oas.items.BottomNavBar
import io.github.skyious.oas.items.NavItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val sharedVM: SharedViewModel = viewModel<SharedViewModel>()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                var darkModeEnabled by rememberSaveable { mutableStateOf(false) }

                val context = LocalContext.current
                val settingsRepository = remember { SettingsRepository(context) }
                val indexRepository = remember { IndexRepository(context, settingsRepository) }

                val showBottomBar = remember(currentRoute) {
                    listOf(
                        NavItem.Discover.route,
                        NavItem.Categories.route,
                        NavItem.Library.route,
                        NavItem.Settings.route
                    ).any { currentRoute?.startsWith(it) == true }
                }

                AppTheme(darkTheme = darkModeEnabled) {
                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavBar(currentRoute.toString()) { selectedRoute ->
                                    navController.navigate(selectedRoute) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
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
                            composable(
                                route = NavItem.Discover.route + "?category={category}",
                                arguments = listOf(navArgument("category") {
                                    type = NavType.StringType
                                    nullable = true
                                })
                            ) {
                                Discoverscreen(
                                    indexRepository = indexRepository,
                                    onAppClick = { appInfo ->
                                        sharedVM.selectApp(appInfo)
                                        navController.navigate("detail")
                                    },
                                    category = it.arguments?.getString("category")
                                )
                            }
                            composable(NavItem.Categories.route) {
                                Categoriesscreen(
                                    indexRepository = indexRepository,
                                    onCategoryClick = { category ->
                                        navController.navigate(NavItem.Discover.route + "?category=$category")
                                    }
                                )
                            }
                            composable(NavItem.Library.route) {
                                LibraryScreen(
                                    onAppClick = { /* TODO: Open app or details */ }
                                )
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
                                sharedVM.selectedApp.collectAsState().value?.let { appInfo ->
                                    AppDetailsScreen(
                                        appInfo = appInfo,
                                        onBackPress = { navController.popBackStack() }
                                    )
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