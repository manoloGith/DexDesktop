# DexDesktop

> 📌 **[ABRIR ROADMAP DEL PROYECTO](ROADMAP.md)** — fases, estado actual, próximos pasos y limitaciones.

Proyecto Android para desarrollar un entorno de escritorio sobre Android, con especial atención a Samsung DeX y monitores externos.

## Documentación del proyecto

- **[Roadmap de la aplicación de escritorio](ROADMAP.md):** visión del producto final, fases, entregables, prioridades, criterios de salida y limitaciones técnicas.
- [Alcance de la APK de diagnóstico](docs/VALIDATION_SCOPE.md): pruebas necesarias antes de cerrar la arquitectura.

## Aplicación real: primera base de escritorio

El módulo `:desktop` contiene la aplicación DexDesktop independiente. Está diseñada para ejecutarse como aplicación de escritorio en Samsung DeX y otros entornos Android desktop; no implementa una interfaz móvil.

Esta primera iteración incorpora:
- Superficie de escritorio con barra superior y Dock.
- Menú de aplicaciones basado en actividades Android instaladas y visibles.
- Lanzamiento de aplicaciones reales mediante intents del sistema.
- Accesos directos y paneles internos iniciales.
- Ventanas internas que se pueden mover y enfocar.

Todavía no incluye persistencia de distribución, redimensionado completo de ventanas internas, escritorios virtuales, widgets ni integración especial con Samsung. Las aplicaciones externas siguen siendo ventanas gestionadas por Android/DeX, no por DexDesktop.

## APK de diagnóstico

El módulo `:app` conserva la herramienta de diagnóstico como aplicación independiente. Sirve para pruebas técnicas y no forma parte de la interfaz del producto final.

## Tecnologías previstas

- Kotlin
- Jetpack Compose / Material 3
- Android DisplayManager
- Android AppWidgetHost para widgets reales
- GitHub Actions

## Compilar

Requisitos: JDK 17 y Gradle 8.9.

```bash
gradle --no-daemon :desktop:assembleDebug :app:assembleDebug
```

APK DexDesktop: `desktop/build/outputs/apk/debug/desktop-debug.apk`.\nAPK diagnóstico: `app/build/outputs/apk/debug/app-debug.apk`.
