package com.dexdesktop.diagnostics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private var report by mutableStateOf<JSONObject?>(null)
    private val saveReport = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val data = report ?: return@registerForActivityResult
        if (uri != null) contentResolver.openOutputStream(uri)?.use {
            it.write(data.toString(2).toByteArray(Charsets.UTF_8))
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        report = Diagnostics.collect(this, this)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("DexDesktop · Diagnóstico", style = MaterialTheme.typography.headlineMedium)
                        Text("Inventario automático del dispositivo y preparación de pruebas reales en Samsung DeX.")
                        val device = report?.optJSONObject("device")
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Dispositivo", style = MaterialTheme.typography.titleLarge)
                                Text("${device?.optString("manufacturer")} ${device?.optString("model")}")
                                Text("Android ${device?.optString("androidRelease")} · API ${device?.optInt("sdkInt")}")
                                Text("Pantallas: ${report?.optJSONArray("displays")?.length() ?: 0}")
                                Text("Apps iniciables: ${report?.optJSONArray("launchableApps")?.length() ?: 0}")
                                Text("Proveedores de widgets: ${report?.optJSONArray("widgetProviders")?.length() ?: 0}")
                            }
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Capacidades preliminares", style = MaterialTheme.typography.titleLarge)
                                Text("Freeform declarado: ${report?.optJSONObject("capabilities")?.optBoolean("freeformFeatureDeclared")}")
                                Text("Esta actividad está en multi-ventana: ${report?.optJSONObject("capabilities")?.optBoolean("activityCurrentlyInMultiWindow")}")
                                Text("Esto no demuestra que podamos controlar ventanas de otras aplicaciones.")
                            }
                        }
                        Text("Pruebas manuales pendientes", style = MaterialTheme.typography.titleLarge)
                        Text("1. Conecta el monitor externo y actualiza el informe.\n2. Activa Samsung DeX y exporta otro informe.\n3. Prueba mover y redimensionar esta ventana.\n4. Prueba minimizar, maximizar y Snap.\n5. Anota qué funciones ofrece el sistema.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { report = Diagnostics.collect(this@MainActivity, this@MainActivity) }) {
                                Text("Actualizar")
                            }
                            Button(onClick = {
                                report = Diagnostics.collect(this@MainActivity, this@MainActivity)
                                saveReport.launch("dexdesktop-diagnostic.json")
                            }) { Text("Exportar JSON") }
                        }
                        Text("Las pruebas manuales permanecen en estado NOT_TESTED hasta verificarlas en el teléfono.")
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        report = Diagnostics.collect(this, this)
    }
}
