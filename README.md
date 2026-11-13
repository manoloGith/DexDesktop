# DexDesktop

Proyecto Android para validar y desarrollar un entorno de escritorio sobre Android, con especial atención a Samsung DeX y monitores externos.

## Objetivo inmediato

La primera entrega será una APK de diagnóstico. Antes de construir el launcher definitivo, comprobará en el dispositivo real qué capacidades están disponibles y documentará qué funciona, qué requiere integración del fabricante y qué no puede hacerse con una aplicación Android normal.

## Enfoque técnico

- Kotlin y Android.
- Jetpack Compose y Material 3 para la interfaz.
- Android AppWidgetHost para evaluar widgets de terceros.
- Room o DataStore para persistencia cuando sea necesario.
- APIs públicas de Android como primera opción.
- Integración específica de Samsung/DeX solo cuando esté documentada y sea viable.

## Principio de validación

No se considerará una función compatible solo porque exista una API. Se distinguirán las comprobaciones automáticas de las pruebas manuales y se registrarán las evidencias en informes JSON y HTML.

## Dispositivo de pruebas

El modelo exacto, versión de Android, One UI y pantallas conectadas se detectarán en tiempo de ejecución. No se fijará el modelo en el código.

## Estado

Repositorio inicializado. La APK de diagnóstico y su flujo de compilación se incorporarán en la siguiente fase.
