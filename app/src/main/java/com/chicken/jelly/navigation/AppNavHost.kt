package com.chicken.jelly.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.chicken.jelly.sound.SoundManager
import com.chicken.jelly.ui.screens.GameScreen
import com.chicken.jelly.ui.screens.GarageScreen
import com.chicken.jelly.ui.screens.MenuScreen
import com.chicken.jelly.ui.screens.SettingsScreen
import com.chicken.jelly.ui.screens.SplashScreen
import com.chicken.jelly.viewmodel.GameViewModel

sealed class AppDestination(val route: String) {
    data object Splash : AppDestination("splash")
    data object Menu : AppDestination("menu")
    data object Garage : AppDestination("garage")
    data object Game : AppDestination("game")
    data object Settings : AppDestination("settings")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(soundManager: SoundManager, modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    val gameViewModel: GameViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        gameViewModel.observeSound(soundManager)
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Splash.route,
        modifier = modifier
    ) {
        composable(AppDestination.Splash.route) {
            SplashScreen(onFinished = {
                navController.navigate(AppDestination.Menu.route) {
                    popUpTo(AppDestination.Splash.route) { inclusive = true }
                }
            })
        }
        composable(AppDestination.Menu.route) {
            MenuScreen(
                onPlay = {
                    soundManager.playGameMusic()
                    navController.navigate(AppDestination.Game.route)
                },
                onGarage = { navController.navigate(AppDestination.Garage.route) },
                onSettings = { navController.navigate(AppDestination.Settings.route) },
                soundManager = soundManager
            )
        }
        composable(AppDestination.Garage.route) {
            GarageScreen(
                viewModel = gameViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppDestination.Game.route) {
            GameScreen(
                viewModel = gameViewModel,
                onExit = {
                    navController.popBackStack()
                    soundManager.playMenuMusic()
                },
                soundManager = soundManager
            )
        }
        composable(AppDestination.Settings.route) {
            SettingsScreen(
                viewModel = gameViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
