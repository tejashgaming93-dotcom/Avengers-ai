package com.avengers.ai.phone

import android.accessibilityservice.AccessibilityService
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.MediaStore
import android.provider.Settings
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhoneAction(val type: String, val arg: String)

/** Executes phone actions with official Android APIs. Every action returns a real, honest result string. */
class ActionExecutor(private val ctx: Context) {

    private val tag = Regex("""\[\[ACTION:(\{.*?\})\]\]""", RegexOption.DOT_MATCHES_ALL)

    fun parse(raw: String): Pair<String, List<PhoneAction>> {
        val acts = tag.findAll(raw).mapNotNull { m ->
            try {
                val j = JSONObject(m.groupValues[1])
                PhoneAction(j.getString("type"), j.optString("arg", ""))
            } catch (_: Exception) { null }
        }.toList()
        return tag.replace(raw, "").trim() to acts
    }

    fun execute(a: PhoneAction): String = try {
        when (a.type) {
            "open_app" -> openApp(a.arg)
            "youtube_search" -> youtubeSearch(a.arg)
            "open_settings" -> openSettings(a.arg)
            "open_camera" -> start(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), "Camera khol diya", "Camera app nahi mila")
            "open_gallery" -> start(
                Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                "Gallery khol di", "Gallery app nahi mili"
            )
            "flashlight_on" -> torch(true)
            "flashlight_off" -> torch(false)
            "battery" -> "Battery ${battery()}% hai"
            "time" -> SimpleDateFormat("EEEE, d MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            "go_back" -> a11y("Back dabaya") { it.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }
            "go_home" -> a11y("Home gaya") { it.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) }
            "show_recents" -> a11y("Recents khola") { it.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) }
            "scroll_down" -> a11y("Neeche scroll kiya") { it.swipe(true) }
            "scroll_up" -> a11y("Upar scroll kiya") { it.swipe(false) }
            else -> "Unknown action: ${a.type}"
        }
    } catch (e: Exception) {
        "${a.type} fail hua: ${e.javaClass.simpleName}"
    }

    fun battery(): Int =
        (ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager)
            .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    private fun start(i: Intent, ok: String, fail: String): String = try {
        ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ok
    } catch (_: ActivityNotFoundException) { fail }

    @Suppress("DEPRECATION")
    private fun openApp(name: String): String {
        val q = name.trim().lowercase()
        if (q.isEmpty()) return "App ka naam nahi mila"
        val pm = ctx.packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(main, 0)
        val hit = apps.firstOrNull { it.loadLabel(pm).toString().lowercase() == q }
            ?: apps.firstOrNull { it.loadLabel(pm).toString().lowercase().contains(q) }
            ?: return "\"$name\" naam ka app installed nahi mila"
        val label = hit.loadLabel(pm).toString()
        val intent = pm.getLaunchIntentForPackage(hit.activityInfo.packageName)
            ?: return "$label ko launch nahi kar paya"
        return start(intent, "$label khol diya", "$label launch nahi hua")
    }

    private fun youtubeSearch(query: String): String {
        if (query.isBlank()) return openApp("YouTube")
        val uri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
        val inApp = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.youtube")
        return try {
            ctx.startActivity(inApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            "YouTube par \"$query\" search khola, video tap kar lo"
        } catch (_: ActivityNotFoundException) {
            start(Intent(Intent.ACTION_VIEW, uri), "Browser me \"$query\" search khola", "YouTube/browser nahi mila")
        }
    }

    private fun openSettings(which: String): String {
        val action = when (which.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound" -> Settings.ACTION_SOUND_SETTINGS
            "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        return start(Intent(action), "Settings khol di", "Settings nahi khuli")
    }

    /** setTorchMode needs no CAMERA permission. */
    private fun torch(on: Boolean): String {
        val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return try {
            val id = cm.cameraIdList.firstOrNull {
                cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return "Is phone me flashlight nahi mili"
            cm.setTorchMode(id, on)
            if (on) "Flashlight ON" else "Flashlight OFF"
        } catch (e: Exception) {
            "Flashlight control nahi ho paya: ${e.javaClass.simpleName}"
        }
    }

    private fun a11y(label: String, f: (TonyAccessibilityService) -> Boolean): String {
        val s = TonyAccessibilityService.instance
            ?: return "Accessibility service ON nahi hai. Phone Control screen se enable karo."
        return if (f(s)) label else "$label — system ne action accept nahi kiya"
    }
}
