package com.avengers.ai.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avengers.ai.phone.TonyAccessibilityService
import com.avengers.ai.ui.HudButton
import com.avengers.ai.ui.HudCard
import com.avengers.ai.ui.HudScreen
import com.avengers.ai.ui.SectionTitle
import com.avengers.ai.ui.TwoCol
import com.avengers.ai.ui.theme.Hud
import com.avengers.ai.vm.PhoneViewModel
import kotlinx.coroutines.delay

@Composable
fun PhoneControlScreen(onBack: () -> Unit) {
    val vm = tonyVm { PhoneViewModel(it) }
    val result by vm.result.collectAsState()
    var app by remember { mutableStateOf("") }
    var yt by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    val a11yOn by produceState(false) {
        while (true) { value = TonyAccessibilityService.instance != null; delay(1000) }
    }

    HudScreen("Phone Control", onBack) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HudCard {
                SectionTitle("Last result")
                Text(result, color = Hud.Text)
            }

            SectionTitle("Quick actions")
            TwoCol(
                listOf(
                    "Camera" to { vm.run("open_camera") },
                    "Gallery" to { vm.run("open_gallery") },
                    "Settings" to { vm.run("open_settings") },
                    "Wi-Fi settings" to { vm.run("open_settings", "wifi") },
                    "Flashlight on" to { vm.run("flashlight_on") },
                    "Flashlight off" to { vm.run("flashlight_off") },
                    "Battery" to { vm.run("battery") },
                    "Date & time" to { vm.run("time") },
                    "Open YouTube" to { vm.run("open_app", "YouTube") }
                )
            )

            SectionTitle("Open any app")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(app, { app = it }, Modifier.weight(1f), placeholder = { Text("App name", color = Hud.Dim) }, singleLine = true)
                HudButton("Open", { vm.run("open_app", app) }, enabled = app.isNotBlank())
            }

            SectionTitle("Search YouTube")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(yt, { yt = it }, Modifier.weight(1f), placeholder = { Text("Song or video", color = Hud.Dim) }, singleLine = true)
                HudButton("Search", { vm.run("youtube_search", yt) }, enabled = yt.isNotBlank())
            }

            HudCard {
                SectionTitle("Touch control (Accessibility)")
                Text(
                    "Status: ${if (a11yOn) "ON" else "OFF"}",
                    color = if (a11yOn) Hud.Cyan else Hud.Red
                )
                Text(
                    "Is service se Tony sirf Back, Home, Recents aur scroll kar sakta hai, jab tum bolo. " +
                        "Ye screen ka content nahi padhta. Tum isse system Settings me kabhi bhi band kar sakte ho.",
                    color = Hud.Dim, fontSize = 13.sp
                )
                HudButton(
                    "Open Accessibility settings",
                    { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
                    Modifier.fillMaxWidth()
                )
                TwoCol(
                    listOf(
                        "Back" to { vm.run("go_back") },
                        "Home" to { vm.run("go_home") },
                        "Recents" to { vm.run("show_recents") },
                        "Scroll down" to { vm.run("scroll_down") },
                        "Scroll up" to { vm.run("scroll_up") }
                    ),
                    enabled = a11yOn
                )
            }
        }
    }
}
