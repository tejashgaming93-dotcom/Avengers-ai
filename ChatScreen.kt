package com.avengers.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avengers.ai.data.Message
import com.avengers.ai.data.Mode
import com.avengers.ai.ui.HudButton
import com.avengers.ai.ui.HudScreen
import com.avengers.ai.ui.theme.Hud
import com.avengers.ai.ui.theme.LocalAccent
import com.avengers.ai.vm.ChatViewModel

/** Used for both AI Chat and the Coding Studio (mode decides Tony's behaviour). */
@Composable
fun ChatScreen(mode: Mode, sid: String?, onBack: () -> Unit) {
    val vm = tonyVm(key = "chat-$mode-$sid") { ChatViewModel(it, mode, sid) }
    val messages by vm.messages.collectAsState()
    val busy by vm.busy.collectAsState()
    val error by vm.error.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val accent = LocalAccent.current
    val isCode = mode == Mode.CODE

    LaunchedEffect(messages.size, busy) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    HudScreen(
        title = if (isCode) "Coding Studio" else "AI Chat",
        onBack = onBack,
        actions = { TextButton(onClick = { vm.newChat() }) { Text("New chat", color = accent) } }
    ) {
        if (messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (isCode) "Bolo kya banana hai: HTML, CSS, JS, Python ya Android code. Error paste karo to debug bhi karunga."
                    else "Bol bhai, kya chal raha hai?",
                    color = Hud.Dim, textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { Bubble(it) }
                if (busy) item { Text("Tony soch raha hai…", color = Hud.Dim, fontSize = 13.sp) }
            }
        }

        error?.let { Text(it, color = Hud.Red, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp), fontSize = 13.sp) }

        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (isCode) "Code ya error likho…" else "Tony se kuch bhi pucho…", color = Hud.Dim) },
                maxLines = 5
            )
            HudButton(
                "Send",
                onClick = { vm.send(input); input = "" },
                modifier = Modifier.padding(start = 8.dp),
                enabled = input.isNotBlank() && !busy
            )
        }
    }
}

@Composable
private fun Bubble(m: Message) {
    val accent = LocalAccent.current
    val mine = m.role == "user"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Box(
            Modifier.widthIn(max = 320.dp)
                .background(if (mine) accent.copy(alpha = 0.14f) else Hud.Panel, RoundedCornerShape(12.dp))
                .border(1.dp, accent.copy(alpha = if (mine) 0.5f else 0.2f), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) { MessageText(m.text) }
    }
}

@Composable
private fun MessageText(text: String) {
    val parts = text.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        parts.forEachIndexed { i, p ->
            if (i % 2 == 1) CodeBlock(p.substringAfter('\n', p).trimEnd())
            else if (p.isNotBlank()) Text(p.trim(), color = Hud.Text)
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    val clip = LocalClipboardManager.current
    Column(Modifier.fillMaxWidth().background(Hud.Bg, RoundedCornerShape(8.dp)).padding(8.dp)) {
        Text(
            code,
            color = Hud.Cyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        )
        TextButton(onClick = { clip.setText(AnnotatedString(code)) }) { Text("Copy code", color = LocalAccent.current) }
    }
}
