package com.avengers.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avengers.ai.ui.ArcReactor
import com.avengers.ai.ui.HudButton
import com.avengers.ai.ui.HudCard
import com.avengers.ai.ui.TwoCol
import com.avengers.ai.ui.rememberBattery
import com.avengers.ai.ui.rememberOnline
import com.avengers.ai.ui.theme.Hud
import com.avengers.ai.ui.theme.LocalAccent
import com.avengers.ai.vm.HomeViewModel

@Composable
fun HomeScreen(onNav: (String) -> Unit) {
    val vm = tonyVm { HomeViewModel(it) }
    val battery by rememberBattery()
    val online by rememberOnline()
    val accent = LocalAccent.current
    val keySet = vm.hasKey()

    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Hud.Bg, Hud.Bg2)))
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("AVENGERS AI", color = accent, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, modifier = Modifier.weight(1f))
            Text("Battery $battery%", color = Hud.Dim, fontSize = 13.sp)
            Spacer(Modifier.size(12.dp))
            Text(if (online) "Online" else "Offline", color = if (online) accent else Hud.Red, fontSize = 13.sp)
        }

        Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            ArcReactor(Modifier.fillMaxSize(), accent)
            Text("T", color = Hud.Bg, fontWeight = FontWeight.Black, fontSize = 40.sp)
        }

        Text("TONY", color = Hud.Text, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = 8.sp)
        Text(
            if (keySet) "Brain: Gemini (key saved)" else "Brain offline: Gemini key missing",
            color = if (keySet) Hud.Dim else Hud.Red, fontSize = 13.sp, textAlign = TextAlign.Center
        )

        if (!keySet) {
            HudCard {
                Text("Tony ko chalu karne ke liye apni Gemini API key daalo.", color = Hud.Text)
                HudButton("Open AI Settings", { onNav("settings") }, Modifier.fillMaxWidth())
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HudButton("Talk to Tony", { onNav("voice") }, Modifier.weight(1f).height(52.dp))
            HudButton("Text chat", { onNav("chat") }, Modifier.weight(1f).height(52.dp))
        }

        TwoCol(
            listOf(
                "Phone control" to { onNav("phone") },
                "Coding studio" to { onNav("code") },
                "Chat history" to { onNav("history") },
                "AI settings" to { onNav("settings") },
                "About" to { onNav("about") }
            )
        )
    }
}
