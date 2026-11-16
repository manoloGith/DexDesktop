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

## Variantes de instalación

La compilación genera dos variantes independientes:

- **standard**: escritorio básico. No declara ni incluye el servicio de accesibilidad; es la variante recomendada para las primeras pruebas de instalación.
- **accessibility**: incluye el prototipo de servicio de accesibilidad que muestra la barra superior y el Dock sobre aplicaciones externas. Android requiere que el usuario lo active expresamente. Esta variante puede recibir advertencias de Play Protect por la sensibilidad inherente al permiso de accesibilidad.

La variante estándar no es una forma de eludir Play Protect: simplemente no incluye la función de accesibilidad. La variante completa conserva esa función para pruebas controladas y futura distribución revisada.

## APK de diagnóstico

El módulo `:app` conserva la herramienta de diagnóstico como aplicación independiente. Sirve para pruebas técnicas y no forma parte de la interfaz del producto final.

## Compilar

Requisitos: JDK 17 y Gradle 8.9.

```bash
gradle --no-daemon :app:assembleDebug :desktop:assembleStandardDebug :desktop:assembleAccessibilityDebug
```

Rutas de salida:
- DexDesktop estándar: `desktop/build/outputs/apk/standard/debug/desktop-standard-debug.apk`
- DexDesktop con accesibilidad: `desktop/build/outputs/apk/accessibility/debug/desktop-accessibility-debug.apk`
- APK diagnóstico: `app/build/outputs/apk/debug/app-debug.apk`
