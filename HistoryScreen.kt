package com.avengers.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avengers.ai.data.Mode
import com.avengers.ai.data.Session
import com.avengers.ai.ui.HudScreen
import com.avengers.ai.ui.theme.Hud
import com.avengers.ai.ui.theme.LocalAccent
import com.avengers.ai.vm.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(onBack: () -> Unit, onOpen: (Session) -> Unit) {
    val vm = tonyVm { HistoryViewModel(it) }
    val sessions by vm.sessions.collectAsState()
    val accent = LocalAccent.current
    val fmt = SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault())

    HudScreen("Chat History", onBack) {
        if (sessions.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Abhi koi chat nahi hai. Tony se baat shuru karo.", color = Hud.Dim)
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.id }) { s ->
                    Row(
                        Modifier.fillMaxWidth()
                            .background(Hud.Panel, RoundedCornerShape(12.dp))
                            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .clickable { onOpen(s) }
                            .padding(start = 14.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(s.title, color = Hud.Text, maxLines = 1)
                            Text(
                                "${modeLabel(s.mode)}, ${s.messages.size} messages, ${fmt.format(Date(s.updated))}",
                                color = Hud.Dim, fontSize = 12.sp
                            )
                        }
                        TextButton(onClick = { vm.delete(s.id) }) { Text("Delete", color = Hud.Red) }
                    }
                }
            }
        }
    }
}

private fun modeLabel(m: Mode) = when (m) {
    Mode.CHAT -> "Chat"
    Mode.CODE -> "Coding"
    Mode.VOICE -> "Voice"
}
