# DexDesktop — Roadmap rediseñado

> Este documento define el producto final y el orden de implementación. La APK del módulo `app` es exclusivamente de diagnóstico; la aplicación de escritorio real se desarrolla en `desktop`.

## Visión y decisiones de producto

DexDesktop será un entorno de escritorio para Android, diseñado para pantallas externas y uso con ratón y teclado. Samsung DeX es la primera plataforma de referencia, pero se evitarán dependencias innecesarias de Samsung para facilitar la adaptación a otros entornos Android desktop.

- **No habrá interfaz móvil de producto.** El modo móvil solo se utilizará para instalar, configurar o diagnosticar técnicamente.
- La experiencia principal incluirá escritorio, barra superior, Dock, menú de aplicaciones, ventanas propias, organización de espacios y accesos directos.
- El Dock tendrá auto-ocultación inteligente como comportamiento básico: se ocultará ante aplicaciones externas maximizadas o a pantalla completa y reaparecerá cuando el puntero se acerque al borde inferior.
- DexDesktop controlará completamente sus propias ventanas. Las ventanas de otras aplicaciones seguirán bajo el control de Android/DeX salvo que exista una integración soportada y probada.
- Root y Shizuku no serán requisitos para instalar, iniciar ni utilizar las funciones normales.
- Los widgets Android reales quedan aplazados: no bloquearán el desarrollo del escritorio ni del Dock.

## Modelo de capacidades y privilegios

Se distinguirán tres niveles para no confundir una función técnicamente posible con una función que pueda utilizar cualquier usuario.

| Nivel | Requisitos | Uso previsto |
|---|---|---|
| Estándar | APIs públicas y permisos normales concedidos por el usuario | Escritorio, aplicaciones, ventanas propias, preferencias, Dock propio y auto-ocultación cuando la detección disponible sea suficiente |
| Integración opcional | Servicio de accesibilidad, permiso de superposición y, en funciones experimentales, Shizuku autorizado por ADB | Detección de cambios de ventana, Dock flotante persistente, zona de activación por ratón y pruebas de integración avanzada |
| Experimental privilegiado | Root o permisos de sistema/fabricante | Solo investigar operaciones que Android o DeX no expongan a una aplicación normal; nunca se prometerán sin prueba en el dispositivo |

### Qué necesita realmente cada función

| Función | Permiso/API | ¿Shizuku? | ¿Root? |
|---|---|---:|---:|
| Escritorio, barra superior y menú de apps | APIs Android normales | No | No |
| Lanzar aplicaciones instaladas | Intents y visibilidad de paquetes permitida | No | No |
| Ventanas internas de DexDesktop | Compose y motor propio | No | No |
| Dock integrado en el escritorio de DexDesktop | UI propia | No | No |
| Dock flotante sobre otras aplicaciones | `SYSTEM_ALERT_WINDOW` concedido por el usuario | No | No |
| Detectar cambios de ventana y actividad | Servicio de accesibilidad activado expresamente por el usuario | No | No |
| Detectar maximización/pantalla completa de apps externas | Eventos de accesibilidad y, cuando sea posible, información de ventanas/pantalla | No en el diseño base | No en el diseño base |
| Activar el Dock al acercar el ratón al borde | Ventana de superposición pequeña y eventos hover del ratón; requiere validación en DeX | No | No |
| Ocultar/mostrar el Dock y animarlo | Servicio propio + superposición | No | No |
| Mover, redimensionar o minimizar ventanas propias | Motor de ventanas interno | No | No |
| Mover o redimensionar arbitrariamente apps externas | No existe API pública universal para imponerlo | Shizuku puede permitir investigar operaciones concretas, no garantizarlo | Puede ampliar posibilidades, sin garantía universal |
| Snap de ventanas externas | Funciones nativas de DeX o integración específica validada | Opcional para investigación | Solo si una operación concreta lo exige y se demuestra |
| Cambiar ajustes protegidos del sistema | Depende del ajuste y de la versión Android | A veces | A veces |
| Escritorios virtuales internos | Estado y ventanas propios de DexDesktop | No | No |
| Escritorios virtuales globales del sistema | API oficial si el fabricante la ofrece | No garantizado | No garantizado |

**Importante sobre el Dock y la accesibilidad:** el permiso de superposición y el servicio de accesibilidad son permisos especiales que el usuario activa en Ajustes. No requieren root ni Shizuku. La accesibilidad permitirá observar eventos de ventanas, pero Android no garantiza que todas las aplicaciones o versiones de DeX expongan información suficiente para distinguir siempre maximizada, pantalla completa e inmersiva. Se implementará detección por evidencias disponibles, estados de confianza y un modo alternativo configurable.

**Importante sobre Shizuku:** Shizuku no equivale a root. Permite que una aplicación autorizada solicite determinadas operaciones a través de un servicio iniciado por ADB o root, dentro de los permisos que el sistema conceda. No elimina las restricciones de Android ni convierte automáticamente a DexDesktop en administrador de ventanas. Solo se añadirá cuando una prueba identifique una operación concreta que aporte una mejora real.

**Importante sobre root:** no se diseñará el producto alrededor de root. Si una función solo funciona con root, se etiquetará como opcional/experimental y la experiencia estándar conservará una alternativa o indicará claramente la limitación.

## Fases de desarrollo rediseñadas

### Fase 0 — Diagnóstico de plataforma y viabilidad

**Objetivo:** disponer de evidencias del entorno real antes de depender de capacidades específicas de Android o DeX.

**Trabajo**
- Mantener la APK de diagnóstico separada del producto.
- Probar en pantalla externa/DeX las capacidades de ventanas nativas: mover, redimensionar, minimizar, maximizar, restaurar y Snap.
- Comprobar pantallas, resolución, densidad, teclado, ratón y eventos disponibles.
- Registrar resultados agregados y anónimos; no subir al repositorio JSON originales ni datos que identifiquen el dispositivo.
- Completar la matriz de viabilidad por función y distinguir control nativo de DeX frente a control desde DexDesktop.
- Aplazar las pruebas de widgets; no forman parte del camino crítico.

**Entregables:** APK de diagnóstico, matriz de viabilidad y documentación de límites.

**Salida:** pruebas reproducibles y límites conocidos para el dispositivo de referencia.

**Estado:** las pruebas registradas han validado en DeX las operaciones nativas de ventanas y Snap. Esto demuestra que DeX las realiza, no que DexDesktop pueda controlar directamente ventanas externas.

### Fase 1 — Arquitectura base y permisos opcionales

**Objetivo:** preparar una arquitectura que permita escritorio, servicio persistente y futuras integraciones sin mezclar el diagnóstico con el producto.

**Trabajo**
- Mantener `:desktop` como aplicación real y `:app` como diagnóstico.
- Separar módulos/capas: escritorio Compose, modelo de ventanas, catálogo de aplicaciones, preferencias, servicio de Dock y adaptadores de plataforma.
- Configurar persistencia con DataStore y utilizar Room solo cuando los datos y relaciones lo justifiquen.
- Definir estados de permisos: superposición, accesibilidad y, en el futuro, conexión opcional con Shizuku.
- Crear una pantalla de configuración de permisos que explique para qué se solicita cada uno y permita continuar sin conceder permisos opcionales.
- Preparar pruebas automatizadas, registro de errores y CI.

**Entregables:** arquitectura modular, gestión de preferencias, flujo de permisos y compilación reproducible.

**Salida:** el producto compila y funciona sin Shizuku ni root; los permisos especiales se solicitan solo al activar la función que los necesita.

### Fase 2 — Superficie de escritorio y experiencia principal

**Objetivo:** convertir la primera superficie visual existente en un escritorio utilizable.

**Trabajo**
- Completar fondo, área de trabajo y barra superior.
- Completar Dock integrado, menú de aplicaciones, favoritos y accesos directos.
- Sustituir iconos provisionales por iconos reales de las aplicaciones.
- Añadir selección, organización y persistencia de iconos y accesos.
- Adaptar distribución, escala y márgenes a monitores externos y distintas densidades.
- Diseñar navegación priorizando ratón y teclado.

**Entregables:** escritorio funcional, barra superior, Dock integrado y personalización básica.

**Salida:** escritorio usable en pantalla externa, con preferencias conservadas tras reiniciar.

### Fase 3 — Catálogo y lanzamiento de aplicaciones

**Objetivo:** integrar las aplicaciones Android instaladas de forma fiable.

**Trabajo**
- Consultar actividades lanzables respetando las reglas de visibilidad de paquetes.
- Mostrar iconos y nombres reales.
- Añadir búsqueda, favoritos, ordenación y agrupación.
- Lanzar aplicaciones mediante intents.
- Gestionar instalaciones/desinstalaciones y errores.
- Comprobar el comportamiento de lanzamiento en DeX y en otros entornos compatibles.

**Entregables:** menú de aplicaciones completo y lanzamiento fiable.

**Salida:** aplicaciones visibles iniciables y catálogo actualizado ante cambios.

### Fase 4 — Servicio de escritorio, Dock flotante y auto-ocultación

**Objetivo:** hacer que el Dock acompañe al usuario también cuando trabaja dentro de aplicaciones externas.

**Trabajo**
- Crear un `AccessibilityService` opcional para observar cambios de ventana, foco y actividad.
- Crear el servicio/controlador del Dock flotante mediante `WindowManager` y permiso `SYSTEM_ALERT_WINDOW`.
- Implementar los modos: siempre visible, auto-ocultar y siempre oculto.
- En auto-ocultar, esconder el Dock cuando una aplicación externa ocupe el estado maximizado/pantalla completa detectado.
- Crear una zona de activación transparente y estrecha en el borde inferior que responda al hover del ratón.
- Al entrar el puntero en la zona, mostrar el Dock; al salir, ocultarlo tras un retardo configurable.
- Evitar que el Dock desaparezca mientras se interactúa con él, se arrastra un elemento o hay un menú abierto.
- Añadir histéresis/retardo para evitar parpadeos al mover el ratón cerca del borde.
- No activar el auto-ocultado por ventanas internas de DexDesktop ni mientras el propio escritorio sea la superficie activa.
- Probar que la zona de activación no bloquee clics ni gestos de las aplicaciones que quedan debajo.
- Incorporar fallback manual si una aplicación o versión de DeX no proporciona datos fiables sobre maximización o pantalla completa.

**Entregables:** servicio de accesibilidad, Dock flotante, zona hover, modos de visibilidad y ajustes.

**Salida:** en el dispositivo de referencia, el Dock se oculta y reaparece de forma fiable en los escenarios acordados, sin interceptar la interacción normal. La compatibilidad con otros dispositivos se marcará por separado.

**Privilegios:** requiere que el usuario active Accesibilidad y Superposición. No requiere Shizuku ni root.

### Fase 5 — Motor completo de ventanas propias

**Objetivo:** gestionar varias ventanas de DexDesktop como en un escritorio de PC.

**Trabajo**
- Crear modelo de ventana con identificador, posición, tamaño, estado, foco y orden Z.
- Implementar barra de título, icono, menú y controles.
- Permitir mover, redimensionar, minimizar, maximizar, restaurar y cerrar.
- Añadir Snap de ventanas propias, límites de pantalla y prevención de ventanas fuera del área visible.
- Añadir atajos de teclado y comportamiento de ratón.
- Persistir posiciones, tamaños y estados cuando proceda.

**Entregables:** gestor de ventanas internas y conjunto de pruebas de interacción.

**Salida:** varias ventanas propias conviven y se gestionan sin errores.

**Límite:** estas ventanas pertenecen a DexDesktop; no son ventanas nativas de otras aplicaciones.

### Fase 6 — Integración con ventanas externas y Samsung DeX

**Objetivo:** mejorar la convivencia con aplicaciones externas sin atribuirse controles que pertenecen al sistema.

**Trabajo**
- Probar lanzamiento, foco, maximización, pantalla completa y restauración de aplicaciones externas.
- Medir qué información expone el servicio de accesibilidad en diferentes estados.
- Evaluar APIs oficiales de Samsung/Android y documentar su disponibilidad.
- Investigar mediante un adaptador aislado si Shizuku permite operaciones concretas útiles en el dispositivo de referencia.
- No incorporar comandos privilegiados hasta demostrar que son estables, reversibles y compatibles.
- Mantener el control nativo de DeX como mecanismo principal para mover/redimensionar apps externas.
- Diseñar mensajes y controles que indiquen cuándo una acción debe realizarse desde DeX.

**Entregables:** matriz por versión de Android/One UI, adaptador opcional si aporta una función probada y documentación de límites.

**Salida:** ninguna operación externa se declara soportada sin pruebas reales y repetibles.

**Privilegios:** la observación básica usa Accesibilidad. Shizuku podría ser opcional para pruebas de operaciones concretas. Root solo se consideraría para experimentos aislados; no se promete que resuelva las restricciones de DeX.

### Fase 7 — Snap y organización del espacio de trabajo

**Objetivo:** organizar ventanas de forma rápida y coherente.

**Trabajo**
- Crear diseños de mitad, tercios, cuadrantes y plantillas personalizadas para ventanas propias.
- Añadir zonas de acoplamiento, previsualización y atajos.
- Integrar las opciones nativas de DeX para ventanas externas cuando sea posible.
- Evaluar automatización opcional solo si existe una API o mecanismo probado.
- Proporcionar alternativas manuales para aplicaciones externas no controlables.

**Entregables:** Snap completo para ventanas propias y guía/integración validada para las externas.

**Salida:** distribuciones internas fiables; compatibilidad externa documentada por dispositivo.

### Fase 8 — Escritorios virtuales internos

**Objetivo:** separar proyectos y grupos de ventanas dentro de DexDesktop.

**Trabajo**
- Crear varios espacios internos.
- Asociar ventanas propias, accesos y configuración a cada espacio.
- Cambiar mediante Dock, ratón y atajos.
- Guardar y restaurar cada espacio.
- Investigar integración con escritorios globales solo si existe API oficial.

**Entregables:** escritorios internos persistentes y cambio rápido.

**Salida:** cada espacio restaura su contenido sin confundirlo con los escritorios globales del sistema.

### Fase 9 — Pantallas, periféricos y compatibilidad

**Objetivo:** robustecer la experiencia en diferentes monitores y entornos Android desktop.

**Trabajo**
- Detectar conexión y desconexión de pantallas.
- Adaptar resolución, densidad, orientación, escala y área útil.
- Gestionar ratón, hover, rueda, botones y foco.
- Añadir atajos configurables.
- Restaurar distribución tras conectar/desconectar monitor.
- Validar Samsung DeX primero y después otros entornos Android desktop.
- Comprobar específicamente la zona hover del Dock y las superposiciones en cada entorno.

**Entregables:** perfiles de pantalla, compatibilidad de periféricos y matriz de pruebas.

**Salida:** cambios de pantalla sin bloqueos ni pérdida de configuración en escenarios validados.

### Fase 10 — Personalización, rendimiento y estabilidad

**Objetivo:** preparar el uso diario.

**Trabajo**
- Temas, fondos, iconos, tamaños, transparencias y animaciones.
- Ajustes completos de Dock, barra superior, accesos y ventanas.
- Opción para reducir animaciones y mejorar accesibilidad.
- Optimización de CPU, memoria y batería.
- Recuperación ante fallos, migración de preferencias y pruebas prolongadas.
- Revisar permisos, privacidad y comportamiento en segundo plano.

**Entregables:** ajustes completos, pruebas de rendimiento y documentación de usuario.

**Salida:** sin fallos críticos abiertos en los dispositivos de referencia.

### Fase 11 — Widgets Android reales (aplazada)

**Objetivo:** incorporar widgets de proveedores Android cuando el escritorio principal esté estabilizado.

**Trabajo**
- Diagnosticar el fallo previo de visualización tras seleccionar proveedor.
- Implementar y validar el ciclo de vida de `AppWidgetHost`.
- Añadir, configurar, mover, redimensionar y eliminar widgets.
- Persistir ubicación y tamaño.
- Probar widgets interactivos y restauración tras reinicio.

**Entregables:** selector y alojamiento de widgets reales.

**Salida:** widgets de varios proveedores se muestran y funcionan de forma estable.

**Prioridad:** aplazada; no bloquea las fases 1–10.

### Fase 12 — Beta, publicación y mantenimiento

**Objetivo:** distribuir una versión estable y mantenerla.

**Trabajo**
- Builds debug y release, firma y versionado.
- Pruebas de instalación, actualización y migración.
- Publicar APK beta y canal de incidencias.
- Mantener matriz de compatibilidad Android/One UI y otros entornos.
- Documentar claramente qué funciones necesitan Accesibilidad, Superposición o integraciones opcionales.

**Entregables:** APK release, guía de instalación, notas de versión y matriz de dispositivos probados.

**Salida:** instalación y actualización reproducibles y funciones documentadas con su nivel real de compatibilidad.

## Orden de prioridades

| Prioridad | Trabajo |
|---|---|
| P0 | Diagnóstico y viabilidad del entorno |
| P1 | Arquitectura, escritorio, catálogo y lanzamiento |
| P1 | Dock flotante, auto-ocultación y activación por ratón |
| P1 | Ventanas propias completas |
| P2 | Integración externa con DeX y Snap |
| P2 | Escritorios virtuales internos |
| P2 | Compatibilidad con monitores y periféricos |
| P3 | Personalización, rendimiento y publicación |
| Aplazada | Widgets Android reales |
| Experimental | Operaciones avanzadas mediante Shizuku/root |

## Reglas de desarrollo y validación

- Cada fase debe terminar con código integrado, compilación CI correcta, pruebas y documentación.
- No se marca una función como terminada por escribir el código: debe compilar y superar pruebas.
- Las pruebas de hardware deben registrar entorno, versión Android/One UI, modo de pantalla y resultado, sin subir identificadores personales del dispositivo.
- Los informes de diagnóstico originales permanecen fuera del repositorio; solo se publican resultados agregados y anónimos.
- Las funciones que requieran permisos especiales deben explicar su finalidad y seguir siendo opcionales cuando sea posible.
- Shizuku y root se implementarán como adaptadores aislados, nunca como dependencias del núcleo.
- Si una función no es posible sin privilegios, se documentará la limitación y se ofrecerá una alternativa estándar.
- No se afirmará que DexDesktop controla ventanas externas cuando la operación la realiza Android/DeX.
- Los cambios de alcance y decisiones arquitectónicas se anotarán en este documento o en ADRs.

## Estado actual del proyecto

- [x] Repositorio y rama `develop`.
- [x] Módulo de diagnóstico `:app` separado del producto.
- [x] Módulo de producto `:desktop` creado con Kotlin y Compose.
- [x] Pipeline CI configurado para compilar ambos módulos.
- [x] Primera superficie de escritorio con barra superior, Dock, menú de aplicaciones y lanzamiento de apps.
- [x] Diagnóstico de DeX: pruebas registradas de mover, redimensionar, minimizar/restaurar, maximizar/restaurar y Snap mediante controles nativos.
- [ ] Verificar CI de la versión actual del módulo `:desktop` y descargar/probar su APK.
- [ ] Completar Fase 1: arquitectura, persistencia y flujo de permisos.
- [ ] Completar Fase 2: iconos reales, organización persistente y adaptación visual.
- [ ] Implementar Fase 4: servicio de Dock, Accesibilidad, superposición y zona hover.
- [ ] Implementar ventanas propias completas.
- [ ] Validar la detección de maximizada/pantalla completa en el dispositivo DeX.

## Decisiones de privacidad y alcance

No se subirán al repositorio modelo del teléfono, números de serie, identificadores, números de compilación ni informes JSON originales que puedan identificar el dispositivo. Los resultados publicados serán agregados y anónimos.

La aplicación real vive en `:desktop`; `:app` sigue siendo una herramienta de diagnóstico. No se diseñará una interfaz móvil alternativa. Las pruebas sin pantalla externa solo se usarán para diagnóstico técnico.

La detección del estado maximizado de aplicaciones externas se considerará una capacidad dependiente de la versión y del fabricante hasta demostrar que los eventos y datos disponibles son fiables. El comportamiento del Dock tendrá un modo manual de respaldo para evitar que un falso positivo deje el Dock inaccesible.
