# Sleep Monitor

Aplicacion Android para monitorizar sesiones de sueno usando acelerometro y microfono del dispositivo, generar un reporte local y dejar preparada la integracion futura de un motor de IA on-device.

## Estructura del repositorio

```text
/root
├── app/                  # Proyecto Android Studio con Compose y Gradle
├── ml/                   # Espacio reservado para entrenamiento, modelos y experimentos ML
└── README.md             # Descripcion general del proyecto
```

## Arquitectura general

- `app/src/main/java/com/example/sleepmonitor/ui`
  Contiene navegacion, pantallas Compose y ViewModels.
- `app/src/main/java/com/example/sleepmonitor/data`
  Contiene Room, entidades, DAOs y repositorios.
- `app/src/main/java/com/example/sleepmonitor/service`
  Contiene el `Foreground Service` que captura sensores durante la noche y cierra la sesion.
- `app/src/main/java/com/example/sleepmonitor/domain/sleep`
  Contiene el motor local desacoplado que hoy genera reportes y recomendaciones basadas en reglas y manana puede sustituirse por TFLite.
- `ml/`
  Mantiene la estructura de datos, scripts y documentacion para la parte de Machine Learning.

## Modulos principales de la app

- Autenticacion local con registro, login, recuperacion de contrasena preparada para backend y eliminacion de cuenta.
- Persistencia local con Room para usuarios, sesiones, fases, muestras de sensores, resumenes y recomendaciones.
- Sesion nocturna con `Foreground Service`, captura de acelerometro y microfono, cierre manual o por ventana inteligente y recuperacion de estado.
- Reporte final con puntuacion, fases detectadas, resumen de ruido/movimiento, recomendaciones y feedback del usuario.
- Perfil con aviso de privacidad, precision limitada y datos utiles para la futura capa IA.

## Integracion futura de IA

La app no incluye IA real en esta entrega. En su lugar, el flujo queda preparado para integrar un modelo on-device:

- Los datos crudos y agregados ya se almacenan de forma estructurada.
- El motor actual vive en `domain/sleep/RuleBasedSleepInsightsEngine.kt`, pensado para poder ser reemplazado por un motor TFLite.
- El feedback del usuario ya queda persistido para futuras calibraciones.

## Requisitos funcionales cubiertos

- Inicio de sesion, registro, recuperacion de contrasena, cierre de sesion y eliminacion de cuenta.
- Inicio y parada de una sesion de sueno.
- Captura local de acelerometro y nivel de ruido.
- Despertar inteligente basado en ventana y fase ligera estimada.
- Reporte final con puntuacion, fases, hora de fin y recomendaciones.
- Feedback opcional del usuario y almacenamiento de discrepancia frente a la nota calculada.
- Historial de recomendaciones con fallback a consejos generales.

## Ejecucion en Android Studio

1. Abre la carpeta raiz del repositorio en Android Studio.
2. Espera a que Gradle sincronice el proyecto.
3. Selecciona un emulador o dispositivo Android 8.0+.
4. Ejecuta la configuracion `app`.
5. Concede permisos de microfono y notificaciones cuando la app los solicite.

## Build por linea de comandos

Desde la raiz del proyecto:

```powershell
gradlew.bat assembleDebug
```

## Notas importantes

- Todo el procesamiento de la sesion se hace localmente.
- La estimacion de fases usa movimiento y ruido, por lo que no tiene la precision de un wearable con pulsometro.
- La carpeta `ml/ML_EXPERIMENTS.md` se mantiene como punto de documentacion para los experimentos de Machine Learning cuando se desarrollen.

## Ajuste de organizacion del repositorio

Para alinearse mejor con la organizacion pedida para la entrega, la carpeta `ml/` mantiene ya la siguiente estructura de trabajo:

```text
ml/
|-- data/
|-- experiments/
|-- models/
|-- reference/
|-- scripts/
`-- ML_EXPERIMENTS.md
```

El detalle actualizado del estado del material de Machine Learning, los artefactos disponibles y las carencias de reproducibilidad queda documentado en `ml/ML_EXPERIMENTS.md`.

## Backend

La aplicacion incluye una integracion de backend REST configurable desde Gradle mediante la propiedad `BACKEND_BASE_URL`.

Backend actual en la app:

- cliente HTTP con Retrofit + Moshi
- sincronizacion remota encapsulada en `app/src/main/java/com/example/sleepmonitor/data/remote/`
- persistencia local con Room como fuente de datos offline
- sincronizacion segura y no bloqueante: si el backend no esta configurado o falla, la app sigue funcionando en local

Operaciones de backend cubiertas:

- `create` y `update` de usuario
- `read` de snapshot remoto del usuario
- `delete` de usuario
- `create` y `update` de sesiones de sueno
- `read` de sesiones y recomendaciones a traves del snapshot remoto
- persistencia local de usuarios, sesiones, fases, resumenes y recomendaciones

## Flujo de datos del sistema

1. La app escribe primero en Room para garantizar funcionamiento offline.
2. Tras cada alta, actualizacion o borrado relevante, los repositorios lanzan una sincronizacion basica hacia el backend REST.
3. Al iniciar sesion o recuperar una sesion ya abierta, la app solicita un snapshot remoto del usuario y lo fusiona con la base local.
4. La UI consume siempre el estado local a traves de ViewModels, evitando depender directamente de la red.

En la practica:

- `AuthRepository` sincroniza altas, borrado de cuenta y recuperacion de snapshot al hacer login.
- `AuthRepository` sincroniza altas, cambios de credenciales y borrado de cuenta.
- `SleepRepository` sincroniza inicio de sesion, cierre de sesion, feedback y estados relevantes.
- `AppRootViewModel` lanza la recuperacion basica del snapshot remoto cuando existe una sesion activa.
- `BackendSyncService` centraliza el CRUD remoto y la fusion basica entre backend y Room.

## Configuracion del backend

Para activar la sincronizacion remota, define la URL base al compilar:

```powershell
gradlew.bat assembleDebug -PBACKEND_BASE_URL=https://tu-backend.example/
```

Si no se define `BACKEND_BASE_URL`, la aplicacion mantiene el modo local con persistencia completa en Room.
