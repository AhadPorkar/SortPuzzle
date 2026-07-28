package com.parsgames.sortpuzzle.ui.nav

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.parsgames.sortpuzzle.LocalAppContainer
import com.parsgames.sortpuzzle.ui.screens.GameScreen
import com.parsgames.sortpuzzle.ui.screens.HomeScreen
import com.parsgames.sortpuzzle.ui.screens.LevelMapScreen
import com.parsgames.sortpuzzle.ui.screens.SettingsScreen
import com.parsgames.sortpuzzle.ui.screens.ShopScreen
import com.parsgames.sortpuzzle.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val LEVELS = "levels"
    const val SHOP = "shop"
    const val SETTINGS = "settings"
    const val GAME = "game/{level}"
    fun game(level: Int) = "game/$level"
}

@Composable
fun SortPuzzleNavHost() {
    val navController = rememberNavController()
    val container = LocalAppContainer.current
    val player by container.player.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(260)) + scaleIn(tween(260), initialScale = 0.98f) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(240)) },
        popExitTransition = { fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.98f) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                container.music.play(0)
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                player = player,
                onPlay = { level -> navController.navigate(Routes.game(level)) },
                onLevels = { navController.navigate(Routes.LEVELS) },
                onShop = { navController.navigate(Routes.SHOP) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.LEVELS) {
            LevelMapScreen(
                player = player,
                onBack = { navController.popBackStack() },
                onPlay = { level -> navController.navigate(Routes.game(level)) }
            )
        }

        composable(Routes.SHOP) {
            ShopScreen(
                player = player,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                player = player,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { entry ->
            val level = entry.arguments?.getInt("level") ?: 1
            GameScreen(
                level = level,
                onBack = {
                    container.music.play(0)
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.HOME)
                    }
                },
                onNextLevel = { next ->
                    navController.navigate(Routes.game(next)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
                onShop = { navController.navigate(Routes.SHOP) }
            )
        }
    }
}

/** یافتنِ Activity میزبان؛ برای نمایش تبلیغ و باز کردن پنجره‌ی خرید لازم است. */
@Composable
fun findActivity(): Activity? {
    var context: Context? = LocalContext.current
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
