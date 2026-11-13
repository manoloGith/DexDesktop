package com.dexdesktop.diagnostics

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import org.json.JSONObject

private data class ManualTest(
    val id: String,
    val title: String,
    val instruction: String
)

private val manualTests = listOf(
    ManualTest("externalDisplay", "Pantalla externa", "Conecta el monitor USB-C/HDMI y comprueba que aparece como una pantalla adicional."),
    ManualTest("desktopMode", "Samsung DeX", "Activa Samsung DeX y comprueba que el escritorio aparece en el monitor externo."),
    ManualTest("freeformWindows", "Ventana flotante", "Abre esta aplicación en una ventana flotante desde DeX o desde la multitarea."),
    ManualTest("windowMove", "Mover ventana", "En DeX, arrastra la barra de título de esta aplicación y comprueba que puedes moverla por el monitor."),
    ManualTest("windowResize", "Redimensionar ventana", "Arrastra un borde o una esquina de esta ventana y comprueba si puedes cambiar su tamaño."),
    ManualTest("windowMinimizeRestore", "Minimizar y restaurar", "Minimiza esta ventana desde los controles de DeX y vuelve a abrirla desde la barra de tareas."),
    ManualTest("windowMaximizeRestore", "Maximizar y restaurar", "Maximiza esta ventana y después restáurala a su tamaño anterior."),
    ManualTest("windowSnap", "Snap / ajuste", "Prueba las zonas o mecanismos de ajuste de ventanas que ofrece DeX."),
    ManualTest("externalAppWindows", "Ventanas de otras aplicaciones", "Abre otra aplicación Android (por ejemplo, Ajustes) y comprueba qué controles de ventana ofrece DeX. Esta prueba evalúa el sistema, no un control directo de DexDesktop."),
    ManualTest("widgets", "Widgets reales", "Añade un widget real con el selector del sistema, comprueba que se muestra y que puedes interactuar con él.")
)

class MainActivity : ComponentActivity() {
    private var report by mutableStateOf<JSONObject?>(null)
    private var hostedWidget by mutableStateOf<AppWidgetHostView?>(null)
    private var activeWidgetId: Int? = null
    private var pendingWidgetId: Int? = null
    private val widgetHost by lazy { AppWidgetHost(this, APP_WIDGET_HOST_ID) }
    private val widgetManager by lazy { AppWidgetManager.getInstance(this) }

    private val saveReport = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val data = report ?: return@registerForActivityResult
        if (uri != null) contentResolver.openOutputStream(uri)?.use {
            it.write(data.toString(2).toByteArray(Charsets.UTF_8))
        }
    }

    private val pickWidget = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId ?: -1)
            ?: (pendingWidgetId ?: -1)
        if (result.resultCode == RESULT_OK && id >= 0) {
            val info = widgetManager.getAppWidgetInfo(id)
            if (info?.configure != null) {
                val configureIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                    .setComponent(info.configure)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                configureWidget.launch(configureIntent)
            } else {
                showHostedWidget(id)
            }
        } else {
            if (id >= 0) widgetHost.deleteAppWidgetId(id)
            pendingWidgetId = null
        }
    }

    private val configureWidget = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val id = pendingWidgetId
        if (result.resultCode == RESULT_OK && id != null) {
            showHostedWidget(id)
        } else {
            id?.let { widgetHost.deleteAppWidgetId(it) }
            pendingWidgetId = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        report = Diagnostics.collect(this, this)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("DexDesktop · Diagnóstico", style = MaterialTheme.typography.headlineMedium)
                        Text("Inventario automático y pruebas funcionales de pantallas, ventanas y widgets.")

                        val device = report?.optJSONObject("device")
                        val capabilities = report?.optJSONObject("capabilities")
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Dispositivo", style = MaterialTheme.typography.titleLarge)
                                Text("${device?.optString("manufacturer")} ${device?.optString("model")}")
                                Text("Android ${device?.optString("androidRelease")} · API ${device?.optInt("sdkInt")}")
                                Text("Pantallas detectadas: ${report?.optJSONArray("displays")?.length() ?: 0}")
                                Text("Apps iniciables: ${report?.optJSONArray("launchableApps")?.length() ?: 0}")
                                Text("Proveedores de widgets: ${report?.optJSONArray("widgetProviders")?.length() ?: 0}")
                            }
                        }

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Capacidades preliminares", style = MaterialTheme.typography.titleLarge)
                                Text("Freeform declarado: ${capabilities?.optBoolean("freeformFeatureDeclared")}")
                                Text("Actividad en multi-ventana: ${capabilities?.optBoolean("activityCurrentlyInMultiWindow")}")
                                Text("Actividad redimensionable: ${capabilities?.optBoolean("resizableActivity")}")
                                Text("Estas señales no demuestran que DexDesktop pueda controlar ventanas de otras aplicaciones.")
                            }
                        }

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Prueba de widget Android real", style = MaterialTheme.typography.titleLarge)
                                Text("Abre el selector de widgets del sistema. Elige uno, completa su configuración si la solicita y comprueba que aparece aquí.")
                                Button(onClick = { beginWidgetSelection() }) { Text("Añadir widget real") }
                                hostedWidget?.let { view ->
                                    Text("Widget añadido. Comprueba que se muestra correctamente y después marca el resultado.")
                                    AndroidView(
                                        factory = { context -> FrameLayout(context) },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 420.dp),
                                        update = { container ->
                                            if (view.parent !== container) {
                                                (view.parent as? ViewGroup)?.removeView(view)
                                                container.removeAllViews()
                                                container.addView(
                                                    view,
                                                    FrameLayout.LayoutParams(
                                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                                    )
                                                )
                                            }
                                        }
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
                                            markTest("widgets", "PASS")
                                        }) { Text("Se muestra y funciona") }
                                        Button(onClick = { removeHostedWidget() }) { Text("Quitar widget") }
                                    }
                                }
                            }
                        }

                        Text("Pruebas funcionales", style = MaterialTheme.typography.titleLarge)
                        Text("Realiza cada acción en el teléfono o en DeX y marca el resultado. Los resultados se conservan al actualizar el inventario y se incluyen en el JSON exportado.")
                        manualTests.forEach { test ->
                            val status = getTestStatus(report, test.id)
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(test.title, style = MaterialTheme.typography.titleMedium)
                                    Text(test.instruction, style = MaterialTheme.typography.bodyMedium)
                                    Text("Resultado: ${statusLabel(status)}", style = MaterialTheme.typography.labelLarge)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(onClick = { markTest(test.id, "PASS") }) { Text("Correcto") }
                                        Button(onClick = { markTest(test.id, "FAIL") }) { Text("Falla") }
                                        Button(onClick = { markTest(test.id, "NOT_TESTED") }) { Text("Pendiente") }
                                    }
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                report = Diagnostics.collect(this@MainActivity, this@MainActivity, report)
                            }) { Text("Actualizar inventario") }
                            Button(onClick = { saveReport.launch("dexdesktop-diagnostic.json") }) {
                                Text("Exportar JSON")
                            }
                        }

                        Text(
                            "Importante: las pruebas de mover, redimensionar, minimizar, maximizar y Snap registran el comportamiento que ofrece Android/DeX a esta ventana. No prueban que DexDesktop pueda manipular directamente ventanas de otras aplicaciones.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    private fun beginWidgetSelection() {
        val id = widgetHost.allocateAppWidgetId()
        pendingWidgetId = id
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        pickWidget.launch(intent)
    }

    private fun showHostedWidget(id: Int) {
        val info = widgetManager.getAppWidgetInfo(id)
        if (info == null) {
            widgetHost.deleteAppWidgetId(id)
            pendingWidgetId = null
            return
        }
        hostedWidget?.let { (it.parent as? ViewGroup)?.removeView(it) }
        hostedWidget = widgetHost.createView(this, id, info).also { it.setAppWidget(id, info) }
        activeWidgetId = id
        pendingWidgetId = null
    }

    private fun removeHostedWidget() {
        hostedWidget?.let { (it.parent as? ViewGroup)?.removeView(it) }
        hostedWidget = null
        activeWidgetId?.let { widgetHost.deleteAppWidgetId(it) }
        activeWidgetId = null
        markTest("widgets", "NOT_TESTED")
    }

    private fun markTest(id: String, status: String) {
        val updated = report?.let { JSONObject(it.toString()) } ?: return
        val tests = updated.optJSONArray("manualTests") ?: JSONArray()
        for (index in 0 until tests.length()) {
            val item = tests.optJSONObject(index) ?: continue
            if (item.optString("id") == id) {
                item.put("status", status)
                break
            }
        }
        updated.put("manualTests", tests)
        report = updated
    }

    override fun onStart() {
        super.onStart()
        widgetHost.startListening()
    }

    override fun onStop() {
        widgetHost.stopListening()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        report = Diagnostics.collect(this, this, report)
    }

    companion object {
        private const val APP_WIDGET_HOST_ID = 0x444558
    }
}

private fun getTestStatus(report: JSONObject?, id: String): String {
    val tests = report?.optJSONArray("manualTests") ?: return "NOT_TESTED"
    for (index in 0 until tests.length()) {
        val item = tests.optJSONObject(index) ?: continue
        if (item.optString("id") == id) return item.optString("status", "NOT_TESTED")
    }
    return "NOT_TESTED"
}

private fun statusLabel(status: String): String = when (status) {
    "PASS" -> "CORRECTO"
    "FAIL" -> "NO FUNCIONA"
    else -> "PENDIENTE"
}
