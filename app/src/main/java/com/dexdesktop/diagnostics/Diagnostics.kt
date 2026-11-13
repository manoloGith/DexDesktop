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
    fun collect(context: Context, activity: Activity, previousReport: JSONObject? = null): JSONObject {
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

        val manualTests = JSONArray()
        val definitions = listOf(
            "externalDisplay" to "Conecta el monitor USB-C/HDMI y comprueba que aparece como una pantalla adicional.",
            "desktopMode" to "Activa Samsung DeX y comprueba que el escritorio aparece en el monitor externo.",
            "freeformWindows" to "Abre esta aplicación en una ventana flotante desde DeX o desde la multitarea.",
            "windowMove" to "En DeX, arrastra la barra de título de esta aplicación y comprueba si puedes moverla.",
            "windowResize" to "Arrastra un borde o una esquina de esta ventana y comprueba si puedes cambiar su tamaño.",
            "windowMinimizeRestore" to "Minimiza esta ventana desde los controles de DeX y vuelve a abrirla desde la barra de tareas.",
            "windowMaximizeRestore" to "Maximiza esta ventana y después restáurala a su tamaño anterior.",
            "windowSnap" to "Prueba las zonas o mecanismos de ajuste de ventanas que ofrece DeX.",
            "externalAppWindows" to "Abre otra aplicación Android y comprueba qué controles de ventana ofrece DeX. Evalúa el sistema, no el control directo de DexDesktop.",
            "widgets" to "La detección de proveedores está disponible; añadir y mostrar un widget real mediante AppWidgetHost queda para una prueba posterior."
        )
        definitions.forEach { (id, instruction) ->
            val oldStatus = findTestStatus(previousReport, id)
            manualTests.put(manual(id, instruction, oldStatus))
        }

        return JSONObject()
            .put("schemaVersion", 2)
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
            .put("manualTests", manualTests)
            .put("assessment", JSONObject().put("status", "PRELIMINARY")
                .put("note", "Las pruebas manuales se registran por separado. La disponibilidad de una API o feature no demuestra por sí sola el comportamiento deseado."))
    }

    private fun findTestStatus(report: JSONObject?, id: String): String {
        val tests = report?.optJSONArray("manualTests") ?: return "NOT_TESTED"
        for (index in 0 until tests.length()) {
            val item = tests.optJSONObject(index) ?: continue
            if (item.optString("id") == id) return item.optString("status", "NOT_TESTED")
        }
        return "NOT_TESTED"
    }

    private fun manual(id: String, instruction: String, status: String) =
        JSONObject().put("id", id).put("instruction", instruction).put("status", status)

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
