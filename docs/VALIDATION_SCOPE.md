# Alcance de la APK de diagnóstico

## Datos automáticos
- Fabricante, modelo real, dispositivo, producto y versión Android/API.
- Pantallas disponibles y métricas básicas.
- Aplicaciones iniciables y proveedores de widgets declarados.
- Feature declarada de gestión de ventanas freeform.
- Estado multi-ventana de la propia actividad al generar el informe.

## Pruebas manuales necesarias
- Conexión y detección de monitor externo.
- Activación y comportamiento de Samsung DeX.
- Movimiento y redimensionamiento de ventanas.
- Minimizar, maximizar y restaurar.
- Snap de ventanas.
- Comportamiento de widgets en el entorno de escritorio, incluida selección, configuración, creación y visualización mediante AppWidgetHost.

La aplicación no puede inferir ni controlar de forma general las ventanas de otras apps por ser una aplicación Android normal. La presencia de freeform tampoco implica una API pública para imponer barras de título, reubicar ventanas ajenas o gestionar escritorios virtuales del sistema.

## Clasificación futura
- V1: viable con API pública (requiere prueba funcional).
- V2: requiere integración del fabricante.
- V3: requiere privilegios/root o modificación del sistema.
- V4: parcialmente viable, con limitaciones.
- V5: no viable con el alcance/API disponible.

La compilación de diagnóstico actual registra inventario y permite marcar pruebas manuales. El último ensayo en DeX obtuvo PASS en 9 pruebas de pantalla/ventanas y FAIL en widgets: se detectaron 34 proveedores y el selector permitió elegir un widget, pero no se mostró. No emite un veredicto de compatibilidad definitivo.

## Limitaciones de esta compilación
- El alojamiento de widgets está en fase experimental: el flujo de selección se ha probado, pero la visualización real ha fallado en el dispositivo ensayado y requiere instrumentación adicional.
- No prueba automáticamente Snap ni los controles de ventanas de apps ajenas.
- No detecta de forma universal el estado interno de One UI/DeX mediante APIs públicas.
- No incluye todavía exportación HTML ni evaluación automática final.


## Privacidad de los informes

El JSON exportado por la APK contiene datos del dispositivo y del entorno, incluidos modelo, identificadores de producto y compilación, pantallas y paquetes instalados. Debe mantenerse local y no subirse al repositorio ni adjuntarse a incidencias públicas sin eliminar antes esos datos. En GitHub solo se publican resultados agregados y no identificativos.
