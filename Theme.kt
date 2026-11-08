package com.avengers.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

object Hud {
    val Bg = Color(0xFF05080F)
    val Bg2 = Color(0xFF0A1226)
    val Panel = Color(0xFF0B1424)
    val Cyan = Color(0xFF00E5FF)
    val Blue = Color(0xFF2979FF)
    val Red = Color(0xFFFF1744)
    val Gold = Color(0xFFFFC107)
    val Text = Color(0xFFE6F1FF)
    val Dim = Color(0xFF7F93B2)
}

fun accentOf(name: String): Color = when (name) {
    "red" -> Hud.Red
    "gold" -> Hud.Gold
    else -> Hud.Cyan
}

val LocalAccent = compositionLocalOf { Hud.Cyan }

@Composable
fun AvengersTheme(accentName: String, content: @Composable () -> Unit) {
    val accent = accentOf(accentName)
    CompositionLocalProvider(LocalAccent provides accent) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = accent,
                secondary = Hud.Blue,
                tertiary = Hud.Red,
                background = Hud.Bg,
                surface = Hud.Panel,
                onBackground = Hud.Text,
                onSurface = Hud.Text
            ),
            content = content
        )
    }
}
