package com.avengers.ai.phone

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

/**
 * Only active after the user enables it in system Accessibility settings.
 * It does NOT read screen content (canRetrieveWindowContent=false); it only performs
 * global actions (Back/Home/Recents) and swipe gestures when Tony is asked to.
 */
class TonyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() { instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    /** fingerUp = true scrolls content down (finger moves up). */
    fun swipe(fingerUp: Boolean): Boolean {
        val dm = resources.displayMetrics
        val x = dm.widthPixels / 2f
        val h = dm.heightPixels.toFloat()
        val (y1, y2) = if (fingerUp) h * 0.70f to h * 0.30f else h * 0.30f to h * 0.70f
        val path = Path().apply { moveTo(x, y1); lineTo(x, y2) }
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()
        return dispatchGesture(g, null, null)
    }

    companion object {
        @Volatile var instance: TonyAccessibilityService? = null
    }
}
