package com.dexdesktop

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private data class LaunchableApp(val packageName: String, val label: String)

private class DesktopWindow(
    val id: Long,
    val title: String,
    val kind: String,
    x: Float,
    y: Float
) {
    var x by mutableFloatStateOf(x)
    var y by mutableFloatStateOf(y)
}

class DesktopActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF101827)) {
                    DesktopWorkspace(
                        apps = loadLaunchableApps(),
                        onLaunchApp = ::launchApp
                    )
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun loadLaunchableApps(): List<LaunchableApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { resolve ->
                val info = resolve.activityInfo ?: return@mapNotNull null
                LaunchableApp(info.packageName, resolve.loadLabel(packageManager)?.toString() ?: info.packageName)
            }
            .distinctBy { it.packageName }
            .filterNot { it.packageName == packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(app: LaunchableApp) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent == null) {
                Toast.makeText(this, "No se puede iniciar ${app.label}", Toast.LENGTH_SHORT).show()
                return
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (error: Exception) {
            Toast.makeText(this, "Error al abrir ${app.label}: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
private fun DesktopWorkspace(apps: List<LaunchableApp>, onLaunchApp: (LaunchableApp) -> Unit) {
    val windows = remember { mutableStateListOf<DesktopWindow>() }
    var drawerOpen by remember { mutableStateOf(false) }
    var clock by remember { mutableStateOf(LocalDateTime.now()) }
    var nextWindowId by remember { mutableStateOf(1L) }

    LaunchedEffect(Unit) {
        while (true) {
            clock = LocalDateTime.now()
            delay(15_000)
        }
    }

    fun openInternal(title: String, kind: String) {
        val index = windows.size
        windows.add(DesktopWindow(nextWindowId++, title, kind, 110f + (index % 5) * 34f, 82f + (index % 5) * 28f))
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF182B45), Color(0xFF243B55), Color(0xFF111827)))
        )
    ) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                clock = clock,
                onOpenApps = { drawerOpen = !drawerOpen },
                onOpenSettings = { openInternal("Ajustes de DexDesktop", "settings") }
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    Modifier.align(Alignment.TopStart).padding(start = 24.dp, top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    DesktopShortcut("Este equipo", "▣") { openInternal("Este equipo", "computer") }
                    DesktopShortcut("Aplicaciones", "▦") { drawerOpen = true }
                    DesktopShortcut("Ajustes", "⚙") { openInternal("Ajustes de DexDesktop", "settings") }
                }

                windows.forEach { window ->
                    InternalWindow(
                        window = window,
                        onFocus = {
                            windows.remove(window)
                            windows.add(window)
                        },
                        onClose = { windows.remove(window) }
                    )
                }

                if (drawerOpen) {
                    AppDrawer(
                        apps = apps,
                        onDismiss = { drawerOpen = false },
                        onLaunch = {
                            drawerOpen = false
                            onLaunchApp(it)
                        }
                    )
                }
            }
            Dock(
                onApps = { drawerOpen = !drawerOpen },
                onHome = { windows.clear(); drawerOpen = false },
                onFiles = { openInternal("Este equipo", "computer") },
                onSettings = { openInternal("Ajustes de DexDesktop", "settings") }
            )
        }
    }
}

@Composable
private fun TopBar(clock: LocalDateTime, onOpenApps: () -> Unit, onOpenSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(48.dp).background(Color(0xE6101726)).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("DEX", color = Color(0xFF8CC8FF), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(Modifier.width(18.dp))
        Text("DexDesktop", color = Color.White, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(24.dp))
        Text("Escritorio", color = Color(0xFFB8C5D6), fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(clock.format(DateTimeFormatter.ofPattern("EEE d MMM · HH:mm")), color = Color.White, fontSize = 13.sp)
        Spacer(Modifier.width(18.dp))
        Text("Ajustes", color = Color(0xFFDCE8F5), fontSize = 13.sp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onOpenSettings).padding(8.dp))
        Text("Aplicaciones", color = Color(0xFFDCE8F5), fontSize = 13.sp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onOpenApps).padding(8.dp))
    }
}

@Composable
private fun DesktopShortcut(title: String, symbol: String, onClick: () -> Unit) {
    Column(
        Modifier.width(92.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(Modifier.size(54.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xCCDFEAF8)),
            contentAlignment = Alignment.Center) {
            Text(symbol, color = Color(0xFF20334A), fontSize = 27.sp)
        }
        Text(title, color = Color.White, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Dock(onApps: () -> Unit, onHome: () -> Unit, onFiles: () -> Unit, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp, top = 6.dp), horizontalArrangement = Arrangement.Center) {
        Row(
            Modifier.clip(RoundedCornerShape(22.dp)).background(Color(0xD91A2638))
                .border(1.dp, Color(0x446F8AA8), RoundedCornerShape(22.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem("▦", "Aplicaciones", onApps)
            DockItem("⌂", "Escritorio", onHome)
            DockItem("▣", "Archivos", onFiles)
            DockItem("⚙", "Ajustes", onSettings)
        }
    }
}

@Composable
private fun DockItem(symbol: String, label: String, onClick: () -> Unit) {
    Column(
        Modifier.width(66.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(symbol, color = Color.White, fontSize = 23.sp)
        Text(label, color = Color(0xFFB9C8D9), fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun AppDrawer(apps: List<LaunchableApp>, onDismiss: () -> Unit, onLaunch: (LaunchableApp) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Card(
            Modifier.width(390.dp).fillMaxHeight().padding(start = 8.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF21B293B))
        ) {
            Column(Modifier.fillMaxSize().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Aplicaciones", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("Cerrar", color = Color(0xFF9BCBFA),
                        modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
                }
                Text("${apps.size} aplicaciones disponibles", color = Color(0xFF9AAFC5), fontSize = 12.sp)
                Spacer(Modifier.height(14.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(104.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        Column(
                            Modifier.clip(RoundedCornerShape(12.dp)).clickable { onLaunch(app) }.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF334A64)),
                                contentAlignment = Alignment.Center) {
                                Text(app.label.take(1).uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(app.label, color = Color.White, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InternalWindow(window: DesktopWindow, onFocus: () -> Unit, onClose: () -> Unit) {
    Card(
        Modifier.offset { IntOffset(window.x.roundToInt(), window.y.roundToInt()) }
            .width(500.dp).height(320.dp)
            .pointerInput(window.id) {
                detectDragGestures(onDragStart = { onFocus() }) { change, drag ->
                    change.consume()
                    window.x += drag.x
                    window.y += drag.y
                }
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F7FB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(42.dp).background(Color(0xFF24364D)).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(window.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                Text("×", color = Color.White, fontSize = 18.sp,
                    modifier = Modifier.clickable(onClick = onClose).padding(horizontal = 6.dp))
            }
            when (window.kind) {
                "computer" -> ComputerPanel()
                "settings" -> SettingsPanel()
                else -> ComputerPanel()
            }
        }
    }
}

@Composable
private fun ComputerPanel() {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Espacio de trabajo", color = Color(0xFF1C2D43), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Aquí aparecerán tus accesos, carpetas y herramientas del escritorio.", color = Color(0xFF52657A))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PanelTile("Documentos", "Carpeta")
            PanelTile("Descargas", "Carpeta")
            PanelTile("Unidades", "Almacenamiento")
        }
    }
}

@Composable
private fun PanelTile(title: String, subtitle: String) {
    Column(Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFFE5EDF6)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("▣", color = Color(0xFF426A92), fontSize = 22.sp)
        Text(title, color = Color(0xFF20334A), fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = Color(0xFF718399), fontSize = 11.sp)
    }
}

@Composable
private fun SettingsPanel() {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ajustes de DexDesktop", color = Color(0xFF1C2D43), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("La configuración persistente se incorporará en la siguiente iteración.", color = Color(0xFF52657A))
        Divider(color = Color(0xFFD7E0EA))
        Text("• Apariencia del escritorio", color = Color(0xFF344A62))
        Text("• Barra superior y Dock", color = Color(0xFF344A62))
        Text("• Distribución de iconos", color = Color(0xFF344A62))
        Text("• Atajos de teclado y ratón", color = Color(0xFF344A62))
    }
}
