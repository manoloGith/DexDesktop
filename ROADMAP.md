# DexDesktop — Roadmap de la aplicación de escritorio

> **Este documento describe el producto final**, no la APK temporal de diagnóstico. La APK de diagnóstico es una herramienta de la Fase 0 para comprobar el dispositivo y reducir riesgos antes de comprometer el diseño definitivo.

## Visión del producto

Construir un entorno de escritorio para Android, optimizado para pantallas externas y Samsung DeX, con una experiencia visual inspirada en escritorios modernos: barra superior, Dock, escritorio configurable, widgets y acceso fluido a aplicaciones.

La prioridad es ofrecer una experiencia real y útil, no una maqueta visual. Las aplicaciones y widgets deben ser los de Android siempre que la plataforma lo permita. Las funciones que Android no permita controlar desde una aplicación normal se identificarán claramente y se resolverán mediante integración del fabricante, alternativas parciales o cambios de alcance.

## Principios y límites técnicos

1. **Sin root como objetivo principal.** La versión estándar debe funcionar como una aplicación Android instalable y, cuando proceda, como launcher predeterminado.
2. **No fingir capacidades del sistema.** Una ventana dibujada dentro de DexDesktop no equivale a controlar la ventana nativa de otra aplicación.
3. **Separar los tipos de ventana:**
   - Ventanas propias: contenido y paneles que DexDesktop controla directamente.
   - Aplicaciones externas: se lanzan mediante Android y el sistema/fabricante decide su gestión de ventanas.
   - Integraciones especiales: solo se incorporan si existe una API soportada y se valida en el hardware real.
4. **Widgets reales.** Evaluar y utilizar `AppWidgetHost` para alojar widgets de proveedores instalados, respetando configuración, permisos y ciclo de vida.
5. **Diseño adaptable.** Diferenciar modo teléfono/tablet y modo escritorio con monitor externo.
6. **Compatibilidad demostrada.** Cada función se marcará como validada, parcialmente validada, experimental o no disponible. La existencia de una API no será suficiente para darla por terminada.

### Clases de viabilidad

| Código | Significado |
|---|---|
| V1 | Implementable con APIs públicas de Android. |
| V2 | Necesita integración específica del fabricante o de Samsung DeX. |
| V3 | Necesita privilegios de sistema, root o modificar el sistema operativo. |
| V4 | Viable parcialmente, con limitaciones funcionales. |
| V5 | No viable en una app Android normal con las APIs disponibles. |

La clasificación puede cambiar tras pruebas en dispositivos y versiones concretas. No se asumirá que una función V2 o V3 está disponible hasta demostrarlo.

---

## Fases de desarrollo

### Fase 0 — Diagnóstico y decisión de viabilidad

**Objetivo:** determinar qué puede hacer realmente el dispositivo de pruebas y qué arquitectura es viable.

**Trabajo**
- Completar la APK de diagnóstico y corregir su compilación.
- Identificar modelo, Android, One UI, pantallas y características disponibles.
- Inventariar aplicaciones iniciables y proveedores de widgets.
- Probar en el teléfono y con monitor externo: Samsung DeX, teclado, ratón, resolución, densidad y cambios de pantalla.
- Ejecutar pruebas manuales de ventanas, movimiento, redimensionado, minimizar/maximizar y Snap.
- Investigar qué APIs públicas o integraciones oficiales permiten cada comportamiento.
- Clasificar cada requisito V1–V5 y documentar evidencias, limitaciones y alternativas.

**Entregables**
- APK de diagnóstico instalable.
- Informes JSON y HTML del dispositivo, cuando se complete esa función.
- Matriz de viabilidad por requisito.
- Decisión documentada: continuar, continuar con limitaciones, rediseñar o detener una función.

**Criterio de salida**
- Compilación reproducible y probada.
- Pruebas en el dispositivo real, tanto en modo móvil como con DeX/monitor.
- Ninguna función crítica se considera viable sin evidencia suficiente.

### Fase 1 — Base del producto y arquitectura

**Objetivo:** crear la aplicación definitiva con una base mantenible, separada del código de diagnóstico.

**Trabajo**
- Definir módulos y arquitectura por capas.
- Configurar Kotlin, Jetpack Compose y Material 3.
- Crear gestión de configuración y persistencia con DataStore; usar Room si el volumen y las relaciones de datos lo justifican.
- Gestionar ciclo de vida, cambios de configuración, orientación, tamaños y pantallas.
- Preparar registro de errores, pruebas automatizadas y compilaciones CI.
- Definir perfiles de interfaz: móvil y escritorio.

**Entregables**
- Estructura modular de la aplicación.
- Navegación y tema base.
- Persistencia de preferencias.
- Pipeline de compilación y pruebas.

**Criterio de salida**
- La app compila en CI, se instala y conserva configuración tras reinicios.
- Cambiar entre pantalla móvil y externa no provoca pérdida de estado ni bloqueos.

### Fase 2 — Escritorio visual y launcher

**Objetivo:** implementar la superficie principal del escritorio.

**Trabajo**
- Implementar el escritorio con fondo configurable.
- Crear barra superior con hora, fecha, estado e indicadores disponibles.
- Crear Dock inferior o lateral configurable, sin reproducir una barra de tareas de Windows.
- Añadir accesos directos, carpetas y menú de aplicaciones.
- Permitir seleccionar DexDesktop como launcher predeterminado cuando el dispositivo lo admita.
- Adaptar distribución, escala y márgenes a pantalla táctil y monitor externo.

**Entregables**
- Escritorio funcional.
- Barra superior.
- Dock configurable.
- Lanzador y organización básica de aplicaciones.

**Criterio de salida**
- Se puede navegar por el escritorio, abrir aplicaciones y personalizar los elementos básicos.
- La interfaz se adapta correctamente a las resoluciones probadas.

### Fase 3 — Catálogo y lanzamiento de aplicaciones Android

**Objetivo:** integrar las aplicaciones instaladas sin crear copias ni simulaciones.

**Trabajo**
- Consultar actividades lanzables respetando las restricciones de visibilidad de paquetes de Android.
- Mostrar icono, nombre y accesos directos disponibles.
- Buscar, ordenar y agrupar aplicaciones.
- Lanzar aplicaciones mediante intents.
- Gestionar aplicaciones no disponibles, permisos y errores de lanzamiento.
- Detectar y mostrar las capacidades de ventana que el sistema ofrece, sin prometer control sobre ellas.

**Entregables**
- Menú de aplicaciones.
- Búsqueda y favoritos.
- Apertura de aplicaciones Android reales.
- Gestión de errores y estados.

**Criterio de salida**
- Las aplicaciones visibles se pueden iniciar de forma fiable y la lista se actualiza al instalar o desinstalar apps.

### Fase 4 — Motor de ventanas propias

**Objetivo:** crear ventanas gestionadas por DexDesktop para sus propios paneles y herramientas.

**Trabajo**
- Modelo de ventana con identificador, posición, tamaño, estado y orden Z.
- Barra de título propia con icono, título y controles.
- Mover, redimensionar, enfocar, minimizar, maximizar, restaurar y cerrar ventanas propias.
- Gestión de ventanas superpuestas y límites de pantalla.
- Atajos de teclado y soporte de ratón.
- Persistencia opcional de posición y tamaño.

**Entregables**
- Gestor de ventanas internas.
- Controles y comportamiento coherente.
- Pruebas de interacción táctil, ratón y teclado.

**Criterio de salida**
- Varias ventanas propias pueden convivir, moverse, cambiar de tamaño y restaurarse sin errores.

**Límite importante:** este motor controla ventanas dibujadas dentro de DexDesktop. No convierte automáticamente aplicaciones externas en ventanas hijas ni permite inyectarles una barra de título.

### Fase 5 — Integración de aplicaciones externas y Samsung DeX

**Objetivo:** conseguir la mejor convivencia posible entre DexDesktop y las ventanas nativas de otras aplicaciones.

**Trabajo**
- Validar el lanzamiento de apps externas en modo escritorio y su comportamiento real en DeX.
- Comprobar qué opciones de ventana ofrece One UI/DeX: mover, redimensionar, maximizar, minimizar, pantalla dividida y restauración.
- Evaluar APIs oficiales y mecanismos soportados por Samsung.
- Investigar límites de launcher, modo multi-ventana, actividad redimensionable y pantallas externas.
- Diseñar una experiencia coherente cuando el control pertenezca a DeX y no a DexDesktop.
- Documentar qué controles se pueden ofrecer directamente y cuáles debe realizar el usuario con el sistema.

**Entregables**
- Matriz de compatibilidad por versión de Android/One UI.
- Flujo de apertura de aplicaciones externas.
- Integración Samsung únicamente cuando esté respaldada y probada.
- Alternativas de interfaz para capacidades no disponibles.

**Criterio de salida**
- Aplicaciones externas probadas en el dispositivo real.
- No se presenta como función propia ningún control que realmente gestione el sistema operativo.

**Riesgo crítico:** una app Android convencional no dispone de una API pública universal para imponer barras de título, mover, redimensionar o minimizar arbitrariamente ventanas de otras aplicaciones. Si DeX no expone una integración soportada, esta parte tendrá que limitarse a las funciones nativas del sistema.

### Fase 6 — Snap Layouts y organización de ventanas

**Objetivo:** ofrecer distribución rápida de ventanas.

**Trabajo**
- Diseñar plantillas de distribución: mitad, tercios, cuadrantes y diseños configurables.
- Implementar Snap completo para ventanas propias.
- Evaluar si las ventanas externas pueden organizarse mediante funciones nativas de DeX o integraciones oficiales.
- Añadir zonas de acoplamiento y previsualización donde sea técnicamente posible.
- Proporcionar alternativas cuando una ventana externa no pueda ser controlada.

**Entregables**
- Snap para ventanas propias.
- Integración o guía de uso de las opciones nativas disponibles para apps externas.
- Matriz de compatibilidad y limitaciones.

**Criterio de salida**
- Las distribuciones propias funcionan con ratón y táctil.
- Las externas solo se consideran soportadas si la prueba real demuestra que se pueden organizar.

### Fase 7 — Widgets Android reales

**Objetivo:** colocar widgets de aplicaciones instaladas en el escritorio.

**Trabajo**
- Implementar el ciclo de vida de `AppWidgetHost` y `AppWidgetManager`.
- Solicitar y gestionar identificadores de widget.
- Mostrar el selector de proveedores disponibles.
- Ejecutar el flujo de configuración de cada widget.
- Alojar, redimensionar y eliminar widgets.
- Persistir su ubicación y tamaño.
- Gestionar actualización, permisos, restauración y proveedores que fallen.
- Probar widgets interactivos y su compatibilidad en pantallas externas.

**Entregables**
- Selector de widgets.
- Escritorio con widgets reales.
- Gestión de tamaño, posición y configuración.
- Persistencia del diseño.

**Criterio de salida**
- Varios widgets de proveedores distintos se pueden añadir, configurar, utilizar, mover y eliminar.
- Se valida su comportamiento tras cerrar y reiniciar la app.

### Fase 8 — Escritorios virtuales y espacios de trabajo

**Objetivo:** organizar ventanas, accesos y widgets en distintos espacios.

**Trabajo**
- Crear espacios de trabajo internos de DexDesktop.
- Asociar a cada espacio ventanas propias, widgets y accesos.
- Añadir cambio de escritorio mediante Dock, gestos y atajos.
- Guardar y restaurar el estado de cada espacio.
- Evaluar si existe integración oficial con escritorios virtuales del sistema o de DeX.

**Entregables**
- Varios escritorios virtuales internos.
- Cambio rápido y persistencia.
- Reglas de organización por espacio.

**Criterio de salida**
- Los espacios propios mantienen su contenido y estado al cambiar entre ellos.

**Límite:** los escritorios virtuales internos de DexDesktop no equivalen a escritorios virtuales globales de Android ni cambian por sí mismos el espacio de trabajo de las aplicaciones externas.

### Fase 9 — Monitores, teclado, ratón y perfiles

**Objetivo:** ofrecer una experiencia robusta en distintos escenarios de uso.

**Trabajo**
- Detectar conexión/desconexión de pantallas.
- Adaptar la interfaz a resolución, densidad, orientación y área útil.
- Gestionar ratón, rueda, botones, foco y hover.
- Añadir atajos de teclado configurables.
- Crear perfiles móvil, monitor externo y Samsung DeX.
- Restaurar ventanas y distribución tras conectar o desconectar el monitor.
- Probar adaptadores USB-C/HDMI, hubs y periféricos disponibles.

**Entregables**
- Perfiles de pantalla.
- Soporte de ratón y teclado.
- Restauración de distribución.
- Pruebas de conexión y desconexión.

**Criterio de salida**
- La interfaz cambia de perfil sin bloqueos ni pérdida de configuración en los escenarios validados.

### Fase 10 — Personalización, estabilidad y rendimiento

**Objetivo:** pulir la experiencia y preparar una versión utilizable a diario.

**Trabajo**
- Temas, fondos, transparencias, iconos y tamaños.
- Configuración de Dock, barra superior, widgets y accesos.
- Animaciones y transiciones con opción de reducirlas.
- Optimización de memoria, CPU y consumo energético.
- Accesibilidad, escalado de texto y navegación por teclado.
- Gestión de fallos, recuperación y migración de configuración.
- Pruebas prolongadas y regresión en distintos tamaños de pantalla.

**Entregables**
- Ajustes completos.
- Pruebas de rendimiento y estabilidad.
- Documentación de usuario y solución de problemas.

**Criterio de salida**
- Sin fallos críticos abiertos; rendimiento y estabilidad aceptables en los dispositivos de referencia.

### Fase 11 — Beta, publicación y mantenimiento

**Objetivo:** distribuir una versión instalable y mantenerla.

**Trabajo**
- Generar builds debug y release firmadas correctamente.
- Configurar versionado y notas de versión.
- Publicar APK de beta y canal de incidencias.
- Probar instalación, actualización y migración.
- Mantener compatibilidad con nuevas versiones de Android y One UI.
- Documentar funciones experimentales y dispositivos validados.

**Entregables**
- APK release.
- Guía de instalación y actualización.
- Registro de cambios.
- Matriz de dispositivos y funciones probadas.

**Criterio de salida**
- Instalación y actualización verificadas en los dispositivos objetivo.
- Procedimiento de mantenimiento y publicación reproducible.

---

## Orden de prioridades del producto

| Prioridad | Funcionalidad | Objetivo |
|---|---|---|
| P0 | Diagnóstico y viabilidad | Evitar construir sobre supuestos incorrectos. |
| P1 | Escritorio, barra superior y Dock | Tener una superficie de escritorio utilizable. |
| P1 | Catálogo y lanzamiento de apps | Acceder a aplicaciones Android reales. |
| P1 | Ventanas propias | Gestionar paneles y herramientas de DexDesktop. |
| P1 | Monitor, teclado y ratón | Usar el escritorio como entorno de trabajo. |
| P2 | Widgets Android reales | Añadir información y controles de otras apps. |
| P2 | Snap de ventanas propias | Organizar el espacio de trabajo. |
| P2 | Escritorios virtuales internos | Separar tareas y espacios. |
| P3 | Integración avanzada con DeX | Mejorar la gestión de aplicaciones externas si la plataforma lo permite. |
| P3 | Personalización y publicación | Consolidar y distribuir el producto. |

## Reglas de gestión del proyecto

- Las fases se desarrollan en orden, pero las tareas independientes pueden avanzar en paralelo si no comprometen la arquitectura.
- Cada fase debe terminar con código integrado, compilación correcta, pruebas y documentación.
- No se marca una tarea como terminada por haber escrito el código: debe compilar y superar sus pruebas.
- Las pruebas de hardware deben indicar dispositivo, versión Android/One UI, modo de pantalla y resultado.
- Los bloqueos por APIs del sistema deben documentarse con evidencia y propuesta alternativa.
- No se eliminarán limitaciones conocidas para aparentar compatibilidad.
- Los cambios de alcance y decisiones arquitectónicas se anotarán en este documento o en ADRs.

## Estado actual

- [x] Repositorio creado y rama `develop` disponible.
- [x] Estructura inicial del proyecto Android y pipeline de compilación añadidos.
- [ ] Fase 0: diagnóstico compilado, instalado y validado en el dispositivo real.
- [ ] Fase 0: pruebas en Samsung DeX y monitor externo.
- [ ] Fase 0: matriz de viabilidad aprobada.
- [ ] Fases del producto definitivo: pendientes de implementación.

**Nota:** el código de diagnóstico actual es provisional y no debe confundirse con la aplicación de escritorio final. La decisión de alcance de las fases 5 y 6 dependerá especialmente de las pruebas reales y de las APIs disponibles.
