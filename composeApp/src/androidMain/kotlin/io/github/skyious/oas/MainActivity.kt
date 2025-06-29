package io.github.skyious.oas


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import io.github.skyious.oas.data.IndexRepository
import io.github.skyious.oas.data.SettingsRepository
import io.github.skyious.oas.items.NavItem
import io.github.skyious.oas.screens.Categoriesscreen
import io.github.skyious.oas.screens.Discoverscreen
import io.github.skyious.oas.screens.LibraryScreen
import io.github.skyious.oas.screens.SearchScreen
import io.github.skyious.oas.screens.Settingsscreen
import io.github.skyious.oas.ui.SharedViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Apps()
        }
    }
}

@Composable
actual fun AppLogo() {
    Image(
        painter = painterResource(id = R.drawable.oas_logo),
        contentDescription = "Logo",
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
    )
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Discover : Screen("discover", "Discover", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Categories : Screen("categories", "Categories", Icons.Default.List)
    object Library : Screen("library", "Library", Icons.Default.Star)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val items = listOf(
    Screen.Discover,
    Screen.Search,
    Screen.Categories,
    Screen.Library,
    Screen.Settings
)

@Preview
@Composable
fun Apps() {
    MaterialTheme {
        val sharedVM: SharedViewModel = viewModel<SharedViewModel>()
        val context = LocalContext.current
        val navController = rememberNavController()
        val settingsRepository = remember { SettingsRepository(context) }
        val indexRepository = remember { IndexRepository(context, settingsRepository) }


        var currentScreen: Screen by remember { mutableStateOf(Screen.Discover) }
        var darkModeEnabled by rememberSaveable { mutableStateOf(false) }


        Scaffold(
            bottomBar = {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                // Simple navigation based on the selected screen
                when (currentScreen) {
                    Screen.Discover -> Discoverscreen(
                        indexRepository = indexRepository,
                        onAppClick = { appInfo ->
                            sharedVM.selectApp(appInfo)
                            navController.navigate("detail")
                        },
                        category = null // it.arguments?.getString("category")
                    )
                    Screen.Search -> SearchScreen()
                    Screen.Categories -> Categoriesscreen(
                        indexRepository = indexRepository,
                        onCategoryClick = { category ->
                            navController.navigate(NavItem.Discover.route + "?category=$category")
                        }
                    ) // Placeholder
                    Screen.Library -> LibraryScreen(
                        onAppClick = null
                    ) // Placeholder
                    Screen.Settings -> Settingsscreen(
                        notificationsEnabled = true,
                        onNotificationsChanged = { /*...*/ },
                        darkModeEnabled = darkModeEnabled,
                        onDarkModeChanged = { darkModeEnabled = it}

                    ) // Placeholder
                }
            }
        }
    }
}