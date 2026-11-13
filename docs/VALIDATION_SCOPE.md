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
- Comportamiento de widgets en el entorno de escritorio.

La aplicación no puede inferir ni controlar de forma general las ventanas de otras apps por ser una aplicación Android normal. La presencia de freeform tampoco implica una API pública para imponer barras de título, reubicar ventanas ajenas o gestionar escritorios virtuales del sistema.

## Clasificación futura
- V1: viable con API pública (requiere prueba funcional).
- V2: requiere integración del fabricante.
- V3: requiere privilegios/root o modificación del sistema.
- V4: parcialmente viable, con limitaciones.
- V5: no viable con el alcance/API disponible.

La versión inicial solo registra inventario y deja las pruebas manuales en estado NOT_TESTED. No emite un veredicto de compatibilidad definitivo.

## Limitaciones de esta compilación
- No crea todavía widgets: solo inventaría proveedores.
- No prueba automáticamente Snap ni los controles de ventanas de apps ajenas.
- No detecta de forma universal el estado interno de One UI/DeX mediante APIs públicas.
- No incluye todavía exportación HTML ni evaluación automática final.
