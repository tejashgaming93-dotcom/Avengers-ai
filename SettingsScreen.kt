package com.avengers.ai.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.avengers.ai.ai.ProviderFactory
import com.avengers.ai.data.SettingsStore
import com.avengers.ai.phone.TonyAccessibilityService
import com.avengers.ai.ui.HudButton
import com.avengers.ai.ui.HudCard
import com.avengers.ai.ui.HudScreen
import com.avengers.ai.ui.SectionTitle
import com.avengers.ai.ui.theme.Hud
import com.avengers.ai.ui.theme.LocalAccent
import com.avengers.ai.vm.SettingsViewModel
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val vm = tonyVm { SettingsViewModel(it) }
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val accent = LocalAccent.current

    var keyInput by remember { mutableStateOf("") }
    var modelInput by remember(ui.model) { mutableStateOf(ui.model) }
    var persona by remember(ui.persona) { mutableStateOf(ui.persona) }
    var confirmClear by remember { mutableStateOf(false) }
    var micGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { micGranted = it }
    val a11yOn by produceState(false) {
        while (true) { value = TonyAccessibilityService.instance != null; delay(1000) }
    }

    HudScreen("AI Settings", onBack) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ui.note?.let { Text(it, color = accent, fontSize = 13.sp) }

            HudCard {
                SectionTitle("AI provider")
                Text(
                    "Provider: ${ProviderFactory.AVAILABLE.first().second}. Naye providers AiProvider interface se add hote hain.",
                    color = Hud.Dim, fontSize = 13.sp
                )
                SectionTitle("Gemini API key")
                Text(if (ui.hasKey) "Key saved (encrypted)" else "Key set nahi hai", color = if (ui.hasKey) Hud.Cyan else Hud.Red)
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Paste API key", color = Hud.Dim) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HudButton("Save key", { vm.saveKey(keyInput); keyInput = "" }, enabled = keyInput.isNotBlank())
                    HudButton("Test connection", { vm.testConnection() }, enabled = ui.hasKey && !ui.testing)
                    if (ui.hasKey) HudButton("Remove", { vm.removeKey() })
                }
                if (ui.testing) Text("Testing…", color = Hud.Dim)
                ui.testResult?.let { Text(it, color = if (it.startsWith("✔")) Hud.Cyan else Hud.Red) }
            }

            HudCard {
                SectionTitle("Model")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsStore.MODELS.forEach { m ->
                        FilterChip(selected = ui.model == m, onClick = { vm.setModel(m) }, label = { Text(m) })
                    }
                }
                OutlinedTextField(
                    value = modelInput,
                    onValueChange = { modelInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Custom model id") },
                    singleLine = true
                )
                HudButton("Use this model", { vm.setModel(modelInput) }, enabled = modelInput.isNotBlank() && modelInput != ui.model)
            }

            HudCard {
                SectionTitle("Voice language")
                Text("Speech recognition aur Tony ki awaaz isi language me chalegi. Awaaz system TTS engine ki hai.", color = Hud.Dim, fontSize = 13.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsStore.LANGS.forEach { (tag, label) ->
                        FilterChip(selected = ui.language == tag, onClick = { vm.setLanguage(tag) }, label = { Text(label) })
                    }
                }
            }

            HudCard {
                SectionTitle("Theme")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val current by com.avengers.ai.ui.screens.themeState()
                    SettingsStore.THEMES.forEach { (name, label) ->
                        FilterChip(selected = current == name, onClick = { vm.setTheme(name) }, label = { Text(label) })
                    }
                }
            }

            HudCard {
                SectionTitle("Tony's personality")
                Text("Extra instructions jo Tony ke base personality ke upar lagte hain.", color = Hud.Dim, fontSize = 13.sp)
                OutlinedTextField(
                    value = persona,
                    onValueChange = { persona = it.take(1000) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Mujhe Bhai bulao, thoda sarcastic raho", color = Hud.Dim) },
                    minLines = 3
                )
                HudButton("Save personality", { vm.savePersona(persona) }, enabled = persona != ui.persona)
            }

            HudCard {
                SectionTitle("Permissions")
                Text("Microphone: ${if (micGranted) "granted" else "not granted"}", color = if (micGranted) Hud.Cyan else Hud.Dim)
                if (!micGranted) HudButton("Allow microphone", { micLauncher.launch(Manifest.permission.RECORD_AUDIO) })
                Text("Accessibility (touch control): ${if (a11yOn) "ON" else "OFF"}", color = if (a11yOn) Hud.Cyan else Hud.Dim)
                HudButton(
                    "Accessibility settings",
                    { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
                    Modifier.fillMaxWidth()
                )
                HudButton(
                    "App permission settings",
                    {
                        ctx.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    Modifier.fillMaxWidth()
                )
            }

            HudCard {
                SectionTitle("Wake word")
                Text("\"Hey Tony\" wake word abhi implement nahi hua. Isme alag wake-word engine chahiye, isliye ye future phase me hai.", color = Hud.Dim, fontSize = 13.sp)
            }

            HudCard {
                SectionTitle("Privacy")
                Text(
                    "Chat history sirf is phone me hai. API key encrypted hai. Messages Gemini ko tab jaate hain jab tum Tony ko bhejte ho.",
                    color = Hud.Dim, fontSize = 13.sp
                )
                HudButton("Clear chat history", { confirmClear = true }, Modifier.fillMaxWidth())
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all chats?") },
            text = { Text("Saari chat history permanently delete ho jayegi.") },
            confirmButton = { TextButton(onClick = { vm.clearHistory(); confirmClear = false }) { Text("Delete", color = Hud.Red) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } }
        )
    }
}
