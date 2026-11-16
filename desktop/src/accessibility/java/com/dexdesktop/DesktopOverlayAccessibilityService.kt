package com.dexdesktop

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/**
 * Optional prototype overlay service, included only in the accessibility flavor.
 * Uses TYPE_ACCESSIBILITY_OVERLAY and does not require SYSTEM_ALERT_WINDOW,
 * root or Shizuku. It is a prototype and must be validated on real DeX hardware.
 */
class DesktopOverlayAccessibilityService : AccessibilityService() {
    private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())

    private var topBarView: View? = null
    private var dockView: View? = null
    private var edgeTriggerView: View? = null
    private var dockVisible = false
    private var pointerOverDock = false
    private var ownAppIsForeground = true
    private var isExternalMaximized = false

    private val hideDockRunnable = Runnable {
        if (!pointerOverDock) hideDock()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Toast.makeText(this, "Servicio DexDesktop conectado", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventPackage = event.packageName?.toString() ?: return

        if (eventPackage == packageName) {
            ownAppIsForeground = true
            removeOverlayWindows()
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED
        ) {
            ownAppIsForeground = false
            isExternalMaximized = detectMaximizedExternalWindow()
            ensureTopBar()
            ensureEdgeTrigger()
            if (isExternalMaximized) hideDock() else showDock()
        }
    }

    override fun onInterrupt() {
        removeOverlayWindows()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        removeOverlayWindows()
        super.onDestroy()
    }

    private fun detectMaximizedExternalWindow(): Boolean {
        return try {
            val metrics = android.util.DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val screenWidth = metrics.widthPixels.toFloat()
            val screenHeight = metrics.heightPixels.toFloat()
            val screenArea = screenWidth * screenHeight

            windows.orEmpty()
                .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
                .filter { it.root?.packageName?.toString() != packageName }
                .any { window ->
                    val bounds = Rect()
                    window.getBoundsInScreen(bounds)
                    val width = bounds.width().toFloat()
                    val height = bounds.height().toFloat()
                    val areaRatio = (width * height) / screenArea.coerceAtLeast(1f)
                    width >= screenWidth * 0.84f &&
                        height >= screenHeight * 0.76f &&
                        areaRatio >= 0.72f
                }
        } catch (_: Exception) {
            false
        }
    }

    private fun ensureTopBar() {
        if (ownAppIsForeground || topBarView != null) return
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = roundedBackground(0xE91A2638.toInt(), 22f)
            setPadding(dp(14), dp(7), dp(14), dp(7))
            addView(actionText("DEXDESKTOP", true) { openDesktop() })
            addView(actionText("Escritorio") { openDesktop() })
            addView(actionText("Apps") { openDesktop() })
        }
        topBarView = bar
        addOverlay(bar, dp(340), dp(46), Gravity.TOP or Gravity.CENTER_HORIZONTAL, dp(8))
    }

    private fun ensureEdgeTrigger() {
        if (ownAppIsForeground || edgeTriggerView != null || dockVisible) return
        val trigger = TextView(this).apply {
            text = "•••"
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(0xFFEAF3FC.toInt())
            background = roundedBackground(0xAA263A52.toInt(), 12f)
            setOnHoverListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_HOVER_ENTER,
                    MotionEvent.ACTION_HOVER_MOVE -> showDock()
                    MotionEvent.ACTION_HOVER_EXIT -> scheduleDockHide()
                }
                true
            }
            setOnClickListener { showDock() }
        }
        edgeTriggerView = trigger
        addOverlay(trigger, dp(112), dp(12), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, dp(2))
    }

    private fun showDock() {
        if (ownAppIsForeground) return
        handler.removeCallbacks(hideDockRunnable)
        removeView(edgeTriggerView)
        edgeTriggerView = null
        if (dockView == null) {
            val dock = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                background = roundedBackground(0xF01A2638.toInt(), 24f)
                setPadding(dp(12), dp(8), dp(12), dp(8))
                addView(actionText("Inicio") { openDesktop() })
                addView(actionText("Apps") { openDesktop() })
                addView(actionText("Archivos") { openDesktop() })
                addView(actionText("Ajustes") { openDesktop() })
                setOnHoverListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_HOVER_ENTER,
                        MotionEvent.ACTION_HOVER_MOVE -> {
                            pointerOverDock = true
                            handler.removeCallbacks(hideDockRunnable)
                        }
                        MotionEvent.ACTION_HOVER_EXIT -> {
                            pointerOverDock = false
                            scheduleDockHide()
                        }
                    }
                    true
                }
            }
            dockView = dock
            addOverlay(dock, WindowManager.LayoutParams.WRAP_CONTENT, dp(58),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, dp(12))
        }
        dockVisible = true
    }

    private fun scheduleDockHide() {
        handler.removeCallbacks(hideDockRunnable)
        handler.postDelayed(hideDockRunnable, 850L)
    }

    private fun hideDock() {
        if (pointerOverDock) return
        removeView(dockView)
        dockView = null
        dockVisible = false
        if (!ownAppIsForeground) ensureEdgeTrigger()
    }

    private fun removeOverlayWindows() {
        handler.removeCallbacks(hideDockRunnable)
        removeView(topBarView)
        removeView(dockView)
        removeView(edgeTriggerView)
        topBarView = null
        dockView = null
        edgeTriggerView = null
        dockVisible = false
        pointerOverDock = false
    }

    private fun addOverlay(view: View, width: Int, height: Int, gravity: Int, y: Int) {
        val params = WindowManager.LayoutParams(
            width, height,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            this.y = y
        }
        try {
            windowManager.addView(view, params)
        } catch (_: Exception) {
            // The service may disconnect while Android changes windows.
        }
    }

    private fun removeView(view: View?) {
        if (view == null) return
        try {
            windowManager.removeView(view)
        } catch (_: Exception) {
        }
    }

    private fun actionText(label: String, emphasized: Boolean = false, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = if (emphasized) 12f else 13f
            setTextColor(if (emphasized) 0xFF8CC8FF.toInt() else 0xFFF1F6FC.toInt())
            setPadding(dp(9), dp(5), dp(9), dp(5))
            isClickable = true
            isFocusable = false
            setOnClickListener { action() }
        }
    }

    private fun roundedBackground(color: Int, radiusDp: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp.toInt()).toFloat()
        setStroke(dp(1), 0x446F8AA8)
    }

    private fun openDesktop() {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "No se pudo abrir DexDesktop", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt().coerceAtLeast(1)
}
