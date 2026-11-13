package com.dexdesktop.diagnostics

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Diagnostics {
    fun collect(context: Context, activity: Activity): JSONObject {
        val pm = context.packageManager
        val displays = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val widgetManager = AppWidgetManager.getInstance(context)
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = try {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
                .map { it.activityInfo.packageName }.distinct().sorted()
        } catch (_: Exception) { emptyList() }
        val widgets = try {
            widgetManager.installedProviders.map {
                JSONObject().put("package", it.provider.packageName)
                    .put("class", it.provider.className).put("label", it.loadLabel(pm).toString())
            }.sortedBy { it.optString("label").lowercase(Locale.ROOT) }
        } catch (_: Exception) { emptyList() }
        val displayArray = JSONArray()
        displays.displays.forEach { displayArray.put(displayJson(it)) }
        val freeform = pm.hasSystemFeature("android.software.freeform_window_management")
        val multiWindow = if (Build.VERSION.SDK_INT >= 24) activity.isInMultiWindowMode else false
        return JSONObject()
            .put("schemaVersion", 1)
            .put("generatedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date()))
            .put("device", JSONObject()
                .put("manufacturer", Build.MANUFACTURER.orEmpty()).put("model", Build.MODEL.orEmpty())
                .put("device", Build.DEVICE.orEmpty()).put("product", Build.PRODUCT.orEmpty())
                .put("androidRelease", Build.VERSION.RELEASE.orEmpty()).put("sdkInt", Build.VERSION.SDK_INT)
                .put("buildId", Build.DISPLAY.orEmpty()))
            .put("capabilities", JSONObject().put("freeformFeatureDeclared", freeform)
                .put("activityCurrentlyInMultiWindow", multiWindow).put("resizableActivity", true)
                .put("thirdPartyWidgetProvidersVisible", widgets.size))
            .put("displays", displayArray).put("launchableApps", JSONArray(apps))
            .put("widgetProviders", JSONArray(widgets))
            .put("manualTests", JSONArray()
                .put(manual("externalDisplay", "Conecta el monitor USB-C/HDMI y comprueba que aparece en la lista de pantallas."))
                .put(manual("desktopMode", "Activa Samsung DeX y vuelve a generar el informe."))
                .put(manual("freeformWindows", "Abre esta app en ventana flotante desde DeX o el selector multitarea."))
                .put(manual("windowControls", "Comprueba si Android/DeX permite mover, redimensionar, minimizar y maximizar ventanas."))
                .put(manual("snap", "Prueba las zonas de ajuste de ventanas disponibles en el dispositivo."))
                .put(manual("widgets", "La creación y colocación de un widget real mediante AppWidgetHost queda para la siguiente fase.")))
            .put("assessment", JSONObject().put("status", "PRELIMINARY")
                .put("note", "La presencia de una API o feature no demuestra que el comportamiento deseado funcione. Las pruebas manuales siguen pendientes."))
    }
    private fun manual(id: String, instruction: String) =
        JSONObject().put("id", id).put("instruction", instruction).put("status", "NOT_TESTED")
    private fun displayJson(display: Display): JSONObject {
        val metrics = android.util.DisplayMetrics()
        @Suppress("DEPRECATION")
        display.getRealMetrics(metrics)
        return JSONObject().put("id", display.displayId).put("name", display.name)
            .put("state", display.state).put("flags", display.flags)
            .put("widthPx", metrics.widthPixels).put("heightPx", metrics.heightPixels)
            .put("densityDpi", metrics.densityDpi).put("rotation", display.rotation)
    }
}
