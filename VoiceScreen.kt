package com.avengers.ai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.avengers.ai.ui.ArcReactor
import com.avengers.ai.ui.HudButton
import com.avengers.ai.ui.HudScreen
import com.avengers.ai.ui.Waveform
import com.avengers.ai.ui.theme.Hud
import com.avengers.ai.vm.VState
import com.avengers.ai.vm.VoiceViewModel

@Composable
fun VoiceScreen(onBack: () -> Unit) {
    val vm = tonyVm { VoiceViewModel(it) }
    val ui by vm.ui.collectAsState()
    val level by vm.level.collectAsState()
    val speaking by vm.speaking.collectAsState()
    val ctx = LocalContext.current

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.startListening() else vm.setError("Mic permission nahi mili, voice chat nahi chalega.")
    }
    fun onMic() {
        if (ui.state != VState.IDLE || speaking) { vm.stop(); return }
        val ok = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (ok) vm.startListening() else permission.launch(Manifest.permission.RECORD_AUDIO)
    }

    val activity = when {
        ui.state == VState.LISTENING -> level
        speaking -> 0.6f
        else -> 0f
    }
    val label = when {
        ui.state == VState.LISTENING -> "Sun raha hoon…"
        ui.state == VState.THINKING -> "Soch raha hoon…"
        speaking -> "Bol raha hoon…"
        else -> "Mic dabao aur bolo"
    }

    HudScreen("Voice Assistant", onBack) {
        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(230.dp)) { ArcReactor(Modifier.size(230.dp), activity = activity) }
            Text(label, color = Hud.Text, fontSize = 16.sp)
            Waveform(level = activity)
            if (ui.heard.isNotBlank()) Text("Tum: ${ui.heard}", color = Hud.Dim, textAlign = TextAlign.Center)
            if (ui.reply.isNotBlank()) Text("Tony: ${ui.reply}", color = Hud.Text, textAlign = TextAlign.Center)
            ui.error?.let { Text(it, color = Hud.Red, textAlign = TextAlign.Center, fontSize = 13.sp) }
        }
        HudButton(
            if (ui.state != VState.IDLE || speaking) "Stop" else "Tap to speak",
            onClick = { onMic() },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
        )
    }
}
