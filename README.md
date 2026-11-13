# DexDesktop

> 📌 **[ABRIR ROADMAP DEL PROYECTO](ROADMAP.md)** — fases, estado actual, próximos pasos y limitaciones.

Proyecto Android para desarrollar un entorno de escritorio sobre Android, con especial atención a Samsung DeX y monitores externos.

## Documentación del proyecto

- **[Roadmap de la aplicación de escritorio](ROADMAP.md):** visión del producto final, fases, entregables, prioridades, criterios de salida y limitaciones técnicas.
- [Alcance de la APK de diagnóstico](docs/VALIDATION_SCOPE.md): pruebas necesarias antes de cerrar la arquitectura.

## Primera entrega: diagnóstico

La APK inicial recoge información del dispositivo, pantallas, aplicaciones iniciables y proveedores de widgets. También prepara pruebas manuales para DeX, ventanas flotantes y Snap.

**Importante:** es un prototipo de diagnóstico, no el launcher definitivo. No certifica que se puedan controlar ventanas de otras aplicaciones.

## Tecnologías previstas

- Kotlin
- Jetpack Compose / Material 3
- Android DisplayManager
- Android AppWidgetHost para widgets reales
- GitHub Actions

## Compilar

Requisitos: JDK 17 y Gradle 8.9.

```bash
gradle --no-daemon :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
