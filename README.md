# DexDesktop

Proyecto Android para validar y desarrollar un entorno de escritorio sobre Android, con especial atención a Samsung DeX y monitores externos.

## Primera entrega: diagnóstico

La APK inicial recoge información del dispositivo, pantallas, aplicaciones iniciables y proveedores de widgets. También genera un informe JSON y prepara pruebas manuales para DeX, ventanas flotantes y Snap.

**Importante:** es un prototipo de diagnóstico, no el launcher definitivo. No certifica que se puedan controlar ventanas de otras aplicaciones.

## Tecnologías
- Kotlin
- Jetpack Compose / Material 3
- Android DisplayManager
- Android AppWidgetManager (inventario de proveedores)
- GitHub Actions

## Compilar
Requisitos: JDK 17 y Gradle 8.9.

```bash
gradle --no-daemon :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Consulta [docs/VALIDATION_SCOPE.md](docs/VALIDATION_SCOPE.md).
