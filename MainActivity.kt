package com.avengers.ai

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.avengers.ai.data.Mode
import com.avengers.ai.ui.screens.AboutScreen
import com.avengers.ai.ui.screens.ChatScreen
import com.avengers.ai.ui.screens.HistoryScreen
import com.avengers.ai.ui.screens.HomeScreen
import com.avengers.ai.ui.screens.PhoneControlScreen
import com.avengers.ai.ui.screens.SettingsScreen
import com.avengers.ai.ui.screens.VoiceScreen
import com.avengers.ai.ui.theme.AvengersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        val container = (application as AvengersApp).container
        setContent {
            val theme by container.settings.theme.collectAsState()
            AvengersTheme(theme) { TonyNav() }
        }
    }
}

@Composable
private fun TonyNav() {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }
    NavHost(
        navController = nav,
        startDestination = "home",
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable("home") { HomeScreen(onNav = { nav.navigate(it) }) }
        composable(
            "chat?sid={sid}",
            arguments = listOf(navArgument("sid") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { e -> ChatScreen(Mode.CHAT, e.arguments?.getString("sid"), back) }
        composable(
            "code?sid={sid}",
            arguments = listOf(navArgument("sid") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { e -> ChatScreen(Mode.CODE, e.arguments?.getString("sid"), back) }
        composable("voice") { VoiceScreen(back) }
        composable("phone") { PhoneControlScreen(back) }
        composable("settings") { SettingsScreen(back) }
        composable("about") { AboutScreen(back) }
        composable("history") {
            HistoryScreen(back) { s ->
                nav.navigate(if (s.mode == Mode.CODE) "code?sid=${s.id}" else "chat?sid=${s.id}")
            }
        }
    }
}
