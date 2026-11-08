package com.avengers.ai.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.avengers.ai.ui.theme.Hud
import com.avengers.ai.ui.theme.LocalAccent
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/** Animated circular AI core: rotating arcs (accent/blue/red) around a pulsing light. */
@Composable
fun ArcReactor(modifier: Modifier = Modifier, accent: Color = LocalAccent.current, activity: Float = 0f) {
    val t = rememberInfiniteTransition(label = "core")
    val rot by t.animateFloat(0f, 360f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "r1")
    val rot2 by t.animateFloat(360f, 0f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "r2")
    val pulse by t.animateFloat(
        0.9f, 1f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val act by animateFloatAsState(activity.coerceIn(0f, 1f), label = "act")

    Canvas(modifier.aspectRatio(1f)) {
        val c = center
        val r = size.minDimension / 2f
        drawCircle(
            Brush.radialGradient(listOf(accent.copy(alpha = 0.30f + 0.35f * act), Color.Transparent), center = c, radius = r),
            radius = r
        )
        drawCircle(accent.copy(alpha = 0.35f), radius = r * 0.95f, style = Stroke(r * 0.01f))
        rotate(rot, c) {
            repeat(6) { i ->
                drawArc(
                    accent, i * 60f + 8f, 40f, false,
                    Offset(c.x - r * 0.88f, c.y - r * 0.88f), Size(r * 1.76f, r * 1.76f),
                    style = Stroke(r * 0.05f, cap = StrokeCap.Round)
                )
            }
        }
        rotate(rot2, c) {
            drawCircle(
                Hud.Blue, r * 0.70f,
                style = Stroke(r * 0.025f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.06f, r * 0.05f)))
            )
        }
        rotate(rot, c) {
            repeat(3) { i ->
                drawArc(
                    Hud.Red, i * 120f + 20f, 50f, false,
                    Offset(c.x - r * 0.55f, c.y - r * 0.55f), Size(r * 1.1f, r * 1.1f),
                    style = Stroke(r * 0.04f, cap = StrokeCap.Round)
                )
            }
        }
        val coreR = r * 0.30f * (pulse + 0.15f * act)
        drawCircle(
            Brush.radialGradient(listOf(Color.White, accent, Color.Transparent), center = c, radius = coreR * 1.7f),
            radius = coreR * 1.7f
        )
        drawCircle(Color.White.copy(alpha = 0.92f), radius = coreR * 0.95f)
    }
}

@Composable
fun Waveform(level: Float, modifier: Modifier = Modifier, bars: Int = 32) {
    val accent = LocalAccent.current
    val t = rememberInfiniteTransition(label = "wave")
    val phase by t.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "phase"
    )
    val amp by animateFloatAsState(level.coerceIn(0f, 1f), label = "amp")
    Canvas(modifier.fillMaxWidth().height(64.dp)) {
        val gap = size.width / bars
        for (i in 0 until bars) {
            val base = 0.06f + amp * 0.94f * (0.5f + 0.5f * sin(phase + i * 0.55f))
            val h = size.height * base
            val x = gap * i + gap / 2
            drawLine(
                if (i % 3 == 0) Hud.Blue else accent,
                Offset(x, (size.height - h) / 2), Offset(x, (size.height + h) / 2),
                strokeWidth = gap * 0.5f, cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun HudScreen(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val accent = LocalAccent.current
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Hud.Bg, Hud.Bg2)))
            .systemBarsPadding()
            .imePadding()
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←", color = accent, fontSize = 22.sp) }
            Text(title, color = Hud.Text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            actions()
        }
        HorizontalDivider(color = accent.copy(alpha = 0.3f))
        content()
    }
}

@Composable
fun HudButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val accent = LocalAccent.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (enabled) accent else Hud.Dim.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = accent, disabledContentColor = Hud.Dim)
    ) { Text(text) }
}

@Composable
fun HudCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val accent = LocalAccent.current
    Column(
        modifier.fillMaxWidth()
            .background(Hud.Panel, RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(text, color = LocalAccent.current, fontWeight = FontWeight.Bold, fontSize = 15.sp)
}

/** Two buttons per row, equal width. */
@Composable
fun TwoCol(items: List<Pair<String, () -> Unit>>, enabled: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, act) -> HudButton(label, act, Modifier.weight(1f), enabled) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun rememberBattery(): State<Int> {
    val ctx = LocalContext.current
    return produceState(initialValue = readBattery(ctx)) {
        while (true) { value = readBattery(ctx); delay(10_000) }
    }
}

@Composable
fun rememberOnline(): State<Boolean> {
    val ctx = LocalContext.current
    return produceState(initialValue = isOnline(ctx)) {
        while (true) { value = isOnline(ctx); delay(4_000) }
    }
}

private fun readBattery(ctx: Context): Int =
    (ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager)
        .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

private fun isOnline(ctx: Context): Boolean {
    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val n = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(n) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
