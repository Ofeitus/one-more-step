package com.ofeitus.onemorestep.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ofeitus.onemorestep.R
import com.ofeitus.onemorestep.data.HealthConnectManager
import com.ofeitus.onemorestep.data.SettingRepository
import com.ofeitus.onemorestep.presentation.screen.HistoryScreen
import com.ofeitus.onemorestep.presentation.screen.StepsScreen
import com.ofeitus.onemorestep.presentation.viewmodel.StepsViewModel
import com.ofeitus.onemorestep.presentation.viewmodel.StepsViewModelFactory
import com.ofeitus.onemorestep.ui.theme.BWTheme

sealed class Screen(val route: String, val title: String, val iconId: Int) {
    object Home : Screen("home", "Home", R.drawable.vscode_codicons_home)
    object History : Screen("profile", "History", R.drawable.vscode_codicons_history)
}

@Composable
fun App(healthConnectManager: HealthConnectManager, settingRepository: SettingRepository) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.History)

    val stepsViewModel: StepsViewModel = viewModel(
        factory = StepsViewModelFactory(healthConnectManager, settingRepository)
    )

    BWTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Transparent
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(
                                painterResource(screen.iconId),
                                contentDescription = screen.title
                            ) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors().copy(
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                selectedIconColor = MaterialTheme.colorScheme.background,
                                selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.primary,
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable(Screen.Home.route) {
                    StepsScreen(stepsViewModel)
                }
                composable(Screen.History.route) {
                    HistoryScreen(stepsViewModel)
                }
            }
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    App(HealthConnectManager(LocalContext.current), SettingRepository(LocalContext.current))
}