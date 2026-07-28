package com.parsgames.sortpuzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parsgames.sortpuzzle.ui.nav.SortPuzzleNavHost
import com.parsgames.sortpuzzle.ui.theme.GameTheme
import com.parsgames.sortpuzzle.ui.theme.SortPuzzleTheme

class MainActivity : ComponentActivity() {

    private val container: AppContainer
        get() = (application as SortPuzzleApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val player by container.player.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalAppContainer provides container) {
                SortPuzzleTheme(theme = GameTheme.byId(player.themeId)) {
                    Box(Modifier.fillMaxSize()) {
                        SortPuzzleNavHost()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        container.music.resume()
    }

    override fun onPause() {
        super.onPause()
        container.music.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) container.release()
    }
}
