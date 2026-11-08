package com.avengers.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avengers.ai.ui.HudCard
import com.avengers.ai.ui.HudScreen
import com.avengers.ai.ui.SectionTitle
import com.avengers.ai.ui.theme.Hud

@Composable
fun AboutScreen(onBack: () -> Unit) {
    HudScreen("About Avengers AI", onBack) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HudCard {
                SectionTitle("Avengers AI v0.1.0")
                Text("Tony, tumhara AI best friend, powered by Google Gemini.", color = Hud.Text)
            }
            HudCard {
                SectionTitle("Kaam kya karta hai")
                Text(
                    "Text chat, voice chat (Android speech recognition + text-to-speech), coding help, " +
                        "phone control (apps, YouTube search, settings, camera, gallery, flashlight, battery, time) " +
                        "aur optional Accessibility se Back/Home/Recents/scroll.",
                    color = Hud.Text, fontSize = 14.sp
                )
            }
            HudCard {
                SectionTitle("Abhi nahi hai")
                Text(
                    "Wake word \"Hey Tony\", syntax highlighting, reminders, weather, web search, " +
                        "image/screen understanding, file manager. Ye future phases me aayenge.",
                    color = Hud.Dim, fontSize = 14.sp
                )
            }
            HudCard {
                SectionTitle("Privacy")
                Text(
                    "API key sirf encrypted storage me rehti hai. Chat history sirf tumhare phone me hai. " +
                        "Tumhare messages Gemini ko bheje jaate hain jab tum Tony se baat karte ho. " +
                        "Koi hidden control nahi: Tony sirf wahi karta hai jo tum bolte ho.",
                    color = Hud.Text, fontSize = 14.sp
                )
            }
        }
    }
}
