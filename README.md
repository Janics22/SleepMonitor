# Sleep Monitor

Sleep Monitor es una aplicacion Android full-stack para monitorizar sesiones de sueno, capturar senales del dispositivo, ejecutar un modelo de Machine Learning con TensorFlow Lite, acumular tiempos por fase de sueno, consultar un sistema RAG/Ollama para generar consejos personalizados, mostrar resultados en una interfaz final con tema oscuro y sincronizar datos con Firebase como backend principal.

Este README esta pensado para ser la guia completa del proyecto. Incluye todo: requisitos, estructura, arquitectura, ejecucion desde cero, Firebase, Firestore, Storage, backend local, H2 Console, TensorFlow Lite, RAG/Ollama, pruebas, comprobaciones, solucion de errores, flujo end-to-end y criterios de entrega.

---

# Indice

1. Descripcion general
2. Que incluye la app
3. Arquitectura general
4. Tecnologias usadas
5. Requisitos previos
6. Estructura del proyecto
7. Modos de ejecucion
8. Preparar Firebase
9. Activar Authentication
10. Activar Firestore
11. Reglas de Firestore
12. Storage
13. Compilar la app
14. Ejecutar en Android Studio
15. Emulador recomendado
16. Backend local Spring/H2
17. H2 Console
18. Funcionamiento de la app
19. UI y dark mode
20. Perfil editable
21. Cierre de sesion
22. Eliminacion de cuenta
23. Sesiones de sueno
24. Captura de sensores
25. TensorFlow Lite
26. Acumulacion de fases
27. RAG/Ollama
28. Instalacion de Ollama
29. Arranque del servidor RAG
30. Pruebas del RAG sin app
31. Pruebas del RAG desde app
32. Comprobaciones completas
33. Troubleshooting
34. Criterios del enunciado cubiertos
35. Resumen final

---

# 1. Descripcion general

Sleep Monitor permite al usuario registrar una noche de sueno desde el movil. Durante la sesion, la app captura senales del acelerometro y del microfono, genera muestras locales, analiza esas muestras con un modelo TensorFlow Lite, estima fases de sueno, calcula una puntuacion, genera recomendaciones y sincroniza el resultado.

El sistema esta disenado como una app local-first:

```text
La app escribe primero en Room
Despues sincroniza con Firebase o backend REST si estan disponibles
La UI lee el estado local
El sistema sigue funcionando aunque la red falle
```

El flujo principal es:

```text
Usuario
-> App Android
-> Firebase Auth
-> Room local
-> Foreground Service
-> Sensores
-> TensorFlow Lite
-> Acumulador de fases
-> RAG/Ollama
-> Recomendaciones
-> Firestore
-> UI final
```

---

# 2. Que incluye la app

La app incluye:

- Registro de usuario.
- Login.
- Recuperacion de contrasena.
- Cierre de sesion.
- Eliminacion de cuenta.
- Perfil editable.
- Dark mode por defecto.
- Navegacion inferior.
- Pantalla de inicio.
- Pantalla de dormir.
- Pantalla de recomendaciones.
- Pantalla de perfil.
- Pantalla de eliminacion de cuenta.
- Persistencia local con Room.
- Firebase Authentication.
- Cloud Firestore.
- Firebase Storage preparado para futuro.
- Backend local Spring Boot opcional.
- H2 persistente.
- Swagger UI.
- OpenAPI JSON.
- Captura de acelerometro.
- Captura de nivel de ruido.
- Servicio en primer plano.
- Notificacion de monitorizacion.
- Despertador inteligente.
- Pruebas automatizadas de noche.
- Modelo TensorFlow Lite.
- Clasificador de fases de sueno.
- Fallback por reglas si falla la IA.
- Acumulacion de tiempos por fase.
- Integracion con RAG/Ollama.
- Consejos personalizados generados por IA.
- Informe final de sueno.
- Feedback del usuario.
- Sincronizacion remota.
- README completo.
- Reglas de seguridad Firebase.

---

# 3. Arquitectura general

La app sigue una arquitectura por capas.

```text
UI Compose
   |
ViewModels
   |
Repositories
   |
Room local database
   |
BackendSyncService
   |
Firebase / REST local
```

El flujo de analisis de sueno es:

```text
SleepMonitorService
   |
SensorSampleEntity
   |
SleepRepository.finalizeSession()
   |
HybridSleepInsightsEngine
   |
SleepSignalFeatures
   |
SleepAiClassifier
   |
model.tflite
   |
PhaseDurationAccumulator
   |
RAG/Ollama
   |
RecommendationEntity
   |
SleepReportContent
```

---

# 4. Tecnologias usadas

## Android

- Kotlin.
- Jetpack Compose.
- Material 3.
- Navigation Compose.
- ViewModel.
- LiveData.
- Room.
- KSP.
- Foreground Service.
- SensorManager.
- MediaRecorder.
- TensorFlow Lite.
- Retrofit.
- Moshi.
- OkHttp.

## Firebase

- Firebase Authentication.
- Cloud Firestore.
- Firebase Storage preparado.
- Firebase Security Rules.
- Firebase CLI.

## Backend local

- Spring Boot 3.4.5.
- Kotlin JVM.
- Spring Web.
- Spring Data JPA.
- H2 Database.
- SpringDoc OpenAPI.
- Swagger UI.

## Machine Learning

- TensorFlow Lite.
- Modelo `model.tflite`.
- Labels `labelsIO.txt`.
- Scripts Python de entrenamiento.
- Experimentos ML.
- Referencia Android.

## RAG/Ollama

- FastAPI.
- Uvicorn.
- ChromaDB.
- Ollama.
- Modelo `llama3.1`.
- Base vectorial local.
- Endpoint `/obtener_consejo`.

---

# 5. Requisitos previos

Necesitas:

- Windows.
- PowerShell.
- Android Studio.
- Java 21.
- Android SDK.
- Un emulador Android o dispositivo fisico.
- Python 3.10 o superior.
- Firebase CLI.
- Cuenta de Firebase.
- Ollama instalado si quieres probar RAG completo.
- Conexion a internet para descargar dependencias.

Comprobar Java:

```powershell
java -version
```

Resultado esperado:

```text
version 21
```

Comprobar Python:

```powershell
python --version
```

Resultado esperado:

```text
Python 3.x
```

Comprobar Firebase CLI:

```powershell
firebase --version
```

Comprobar Ollama:

```powershell
ollama --version
```

Si Ollama no existe, se explica mas abajo como instalarlo.

---

# 6. Estructura del proyecto

```text
SleepMonitor-0.2.b/
|-- README.md
|-- FIREBASE_SETUP.md
|-- firebase.json
|-- firestore.rules
|-- firestore.indexes.json
|-- storage.rules
|-- build.gradle.kts
|-- settings.gradle.kts
|-- gradle.properties
|-- gradlew
|-- gradlew.bat
|-- app/
|   |-- build.gradle.kts
|   |-- google-services.example.json
|   |-- proguard-rules.pro
|   |-- src/
|   |   |-- main/
|   |   |   |-- AndroidManifest.xml
|   |   |   |-- assets/
|   |   |   |   |-- model.tflite
|   |   |   |   |-- labelsIO.txt
|   |   |   |-- java/com/example/sleepmonitor/
|   |   |       |-- MainActivity.kt
|   |   |       |-- SleepMonitorApp.kt
|   |   |       |-- data/
|   |   |       |-- domain/
|   |   |       |-- ml/
|   |   |       |-- navigation/
|   |   |       |-- service/
|   |   |       |-- ui/
|   |-- backend/
|       |-- build.gradle.kts
|       |-- src/main/kotlin/
|       |-- src/main/resources/
|-- ml/
|   |-- RAG/
|   |-- mi_base_de_datos/
|   |-- data/
|   |-- experiments/
|   |-- models/
|   |-- reference/
|   |-- scripts/
|   |-- ML_EXPERIMENTS.md
```

---

# 7. Carpetas importantes

## `app/src/main/java/com/example/sleepmonitor/ui`

Contiene pantallas Compose.

Pantallas principales:

```text
auth/LoginScreen.kt
auth/RegisterScreen.kt
auth/ForgotPasswordScreen.kt
auth/DeleteAccountScreen.kt
home/HomeScreen.kt
sleep/SleepSessionScreen.kt
profile/ProfileScreen.kt
recommendations/RecommendationsScreen.kt
```

## `app/src/main/java/com/example/sleepmonitor/data`

Contiene persistencia y sincronizacion.

```text
SleepDatabase.kt
local/entities/Entities.kt
local/dao/Daos.kt
repository/Repositories.kt
remote/BackendSyncService.kt
remote/BackendApi.kt
firebase/FirebaseBackendGateway.kt
```

## `app/src/main/java/com/example/sleepmonitor/domain/sleep`

Contiene logica de sueno.

```text
SleepSignalFeatures.kt
HybridSleepInsightsEngine.kt
RuleBasedSleepInsightsEngine.kt
```

## `app/src/main/java/com/example/sleepmonitor/ml`

Contiene integracion ML Android.

```text
SleepAiClassifier.kt
RAG-puente.kt
```

## `app/src/main/java/com/example/sleepmonitor/service`

Contiene captura de sensores.

```text
SleepMonitorService.kt
AccelerometerTestAutomation.kt
```

## `ml/RAG`

Contiene servidor RAG.

```text
RAG_server.py
README.md
consejos.json
crearBaseDeDatos.py
loadJSON.py
modelosOllama.py
test_api.py
```

## `ml/mi_base_de_datos`

Contiene base ChromaDB local.

```text
chroma.sqlite3
```

---

# 8. Modos de ejecucion

## Modo Firebase completo

Usa:

```text
Android app + Firebase Auth + Firestore + TensorFlow Lite + RAG opcional
```

Es el modo recomendado.

## Modo backend local

Usa:

```text
Android app + Room + Spring Boot + H2
```

Sirve para depurar y ver datos en navegador.

## Modo offline/local

Usa:

```text
Android app + Room
```

Funciona sin Firebase y sin backend.

## Modo RAG completo

Usa:

```text
Android app + TensorFlow Lite + RAG_server.py + Ollama
```

Genera consejos IA personalizados.

---

# 9. Preparar Firebase

## 9.1 Crear proyecto

1. Entra en Firebase Console.
2. Crea un proyecto.
3. Nombre sugerido: `SleepMonitor`.
4. Analytics es opcional.

## 9.2 Crear app Android

1. En Firebase Console, anade app Android.
2. Package name exacto:

```text
com.example.sleepmonitor
```

3. Descarga:

```text
google-services.json
```

4. Colocalo en:

```text
app/google-services.json
```

No lo pongas en la raiz.

No lo subas a Git.

---

# 10. Activar Authentication

1. Firebase Console.
2. Authentication.
3. Sign-in method.
4. Activar Email/Password.

Esto permite:

- Registro.
- Login.
- Recuperacion.
- Cierre.
- Eliminacion.

---

# 11. Activar Firestore

1. Firebase Console.
2. Firestore Database.
3. Crear base de datos.
4. Elegir `Standard`.
5. Elegir modo production.
6. Elegir region.

La app crea automaticamente:

```text
users/{uid}
users/{uid}/sessions/{sessionId}
```

No tienes que crear manualmente `users`.

---

# 12. Que significa `users/{uid}`

`users` es una coleccion.

`{uid}` es el UID real que Firebase Authentication asigna al usuario.

Ejemplo real:

```text
users/6Qx7Abc92Kf...
```

Dentro de ese documento se guardan datos del usuario.

Cuando haces una sesion, se crea:

```text
users/6Qx7Abc92Kf.../sessions/session123
```

---

# 13. Desplegar reglas

Primero inicia sesion:

```powershell
firebase login
```

Selecciona proyecto:

```powershell
firebase use --add
```

Despliega Firestore:

```powershell
firebase deploy --only firestore:rules
```

Si Storage esta activado:

```powershell
firebase deploy --only firestore:rules,storage
```

Si Storage falla porque no esta configurado, usa solo:

```powershell
firebase deploy --only firestore:rules
```

---

# 14. Storage

Storage esta preparado para futuro.

La app actual no necesita Storage para el flujo principal.

Si quieres activarlo:

1. Firebase Console.
2. Storage.
3. Get Started.
4. Modo production.
5. Region.

Luego:

```powershell
firebase deploy --only storage
```

---

# 15. Compilar app

Desde la raiz:

```powershell
.\gradlew.bat --no-daemon :app:assembleDebug
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```

---

# 16. Ejecutar tests

Tests Android:

```powershell
.\gradlew.bat --no-daemon :app:testDebugUnitTest
```

Tests backend:

```powershell
.\gradlew.bat --no-daemon :app:backend:test
```

Build completo:

```powershell
.\gradlew.bat --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:backend:test
```

---

# 17. Emulador recomendado

Usa:

```text
Pixel 7
Android 14 API 34
x86_64
Google APIs o Google Play
```

Evita imagenes:

```text
16 KB Page Size
```

El warning de TensorFlow Lite:

```text
libtensorflowlite_jni.so not aligned at 16 KB
```

no impide probar en emuladores normales.

---

# 18. Ejecutar app

1. Abre Android Studio.
2. Abre carpeta raiz.
3. Espera Gradle Sync.
4. Selecciona emulador.
5. Pulsa Run.
6. Registra usuario.
7. Comprueba Firebase.

---

# 19. Comprobar Firebase Authentication

1. Registra usuario en app.
2. Firebase Console.
3. Authentication.
4. Users.

Resultado esperado:

```text
Aparece el email registrado.
```

---

# 20. Comprobar Firestore

1. Firebase Console.
2. Firestore Database.
3. Datos.
4. Coleccion `users`.

Resultado esperado:

```text
users
  UID_DEL_USUARIO
```

Campos esperados:

```text
userId
email
username
createdAt
peso
altura
sexo
pais
fechaNacimiento
sleepProfile
aiCalibrationScore
```

---

# 21. Probar perfil editable

1. Login.
2. Perfil.
3. Cambiar username.
4. Cambiar peso.
5. Cambiar altura.
6. Cambiar sexo.
7. Cambiar pais.
8. Cambiar fecha.
9. Guardar.

Resultado esperado:

- La app muestra guardado.
- Firestore actualiza documento.
- Al volver a perfil, datos se mantienen.

---

# 22. Cerrar sesion

1. Perfil.
2. Cerrar sesion.

Resultado esperado:

```text
Vuelve a Login.
```

---

# 23. Eliminar cuenta

Usa cuenta de prueba.

1. Perfil.
2. Eliminar cuenta.
3. Introducir contrasena.
4. Confirmar.

Resultado esperado:

- Usuario desaparece de Authentication.
- Documento desaparece de Firestore.
- Sesiones desaparecen.
- App vuelve a Login.

---

# 24. Backend local

Arrancar:

```powershell
.\gradlew.bat :app:backend:bootRun
```

Resultado esperado:

```text
Tomcat started on port 8080
H2 console available at '/h2-console'
```

Si queda en:

```text
85% EXECUTING
```

es normal. El servidor esta corriendo.

---

# 25. Health backend

Abrir:

```text
http://localhost:8080/api/health
```

Resultado:

```json
{"status":"ok"}
```

---

# 26. Swagger

Abrir:

```text
http://localhost:8080/swagger-ui.html
```

Resultado esperado:

- Endpoints de usuarios.
- Endpoints de sesiones.
- Health endpoint.

---

# 27. H2 Console

Abrir:

```text
http://localhost:8080/h2-console
```

Datos:

```text
JDBC URL: jdbc:h2:file:./data/sleepmonitor
User Name: sa
Password:
```

Consultas:

```sql
SELECT * FROM USERS;
SELECT * FROM SLEEP_SESSIONS;
SELECT * FROM SENSOR_SUMMARIES;
SELECT * FROM PHASES;
SELECT * FROM RECOMMENDATIONS;
```

Si Firebase esta activo, H2 puede estar vacio porque Firebase tiene prioridad.

---

# 28. Conexion app a backend local

Para emulador:

```powershell
.\gradlew.bat --no-daemon :app:assembleDebug -PBACKEND_BASE_URL=http://10.0.2.2:8080/
```

Para movil fisico:

```text
http://IP_DE_TU_PC:8080/
```

---

# 29. Permisos Android

La app usa:

```xml
RECORD_AUDIO
INTERNET
POST_NOTIFICATIONS
FOREGROUND_SERVICE
FOREGROUND_SERVICE_MICROPHONE
WAKE_LOCK
VIBRATE
```

Aceptar permisos al iniciar sesion de sueno.

---

# 30. Funcionamiento sesion de sueno

1. Usuario entra en Dormir.
2. Configura ventana.
3. Inicia sesion.
4. Foreground Service arranca.
5. Se capturan muestras.
6. Se guardan en Room.
7. Al finalizar, se analiza.
8. Se genera informe.
9. Se sincroniza.

---

# 31. Pruebas automatizadas

La app tiene:

```text
Noche tranquila
Noche inquieta
Despertar suave
```

Sirven para probar sin dormir una noche real.

Pasos:

1. Login.
2. Dormir.
3. Pulsar Noche tranquila.
4. Esperar informe.

Resultado:

- Score.
- Resumen.
- Fases.
- Recomendaciones.

---

# 32. TensorFlow Lite

Assets:

```text
app/src/main/assets/model.tflite
app/src/main/assets/labelsIO.txt
```

Clase:

```text
app/src/main/java/com/example/sleepmonitor/ml/SleepAiClassifier.kt
```

Dependencia:

```kotlin
implementation("org.tensorflow:tensorflow-lite:2.14.0")
```

---

# 33. Entrada del modelo

El modelo recibe:

```text
actigraphy
zcm
vmMean
vmStd
```

Estas features salen de:

```text
SleepSignalFeatures.kt
```

---

# 34. Salida del modelo

El modelo devuelve una fase:

```text
REM
LIGHT
DEEP
AWAKE
```

La app normaliza:

```text
Ligero -> LIGHT
Profundo -> DEEP
REM -> REM
Despierto -> AWAKE
```

---

# 35. Acumulador de fases

Cada respuesta de la IA suma tiempo.

Contadores:

```text
REM
Ligero
Profundo
```

Ejemplo:

```text
Respuesta IA: REM
Duracion ventana: 30s
REM += 30
```

Ejemplo:

```text
Respuesta IA: LIGHT
Duracion ventana: 30s
Ligero += 30
```

Ejemplo:

```text
Respuesta IA: DEEP
Duracion ventana: 30s
Profundo += 30
```

Al final:

```text
total = REM + Ligero + Profundo
```

---

# 36. RAG/Ollama

El RAG recibe:

```json
{
  "total": 6.5,
  "rem": 0.8,
  "profundo": 1.2,
  "ligero": 4.5
}
```

Devuelve:

```json
{
  "consejo": "Consejo personalizado..."
}
```

La app guarda ese consejo como recomendacion.

---

# 37. Instalar Ollama

Descargar:

```text
https://ollama.com/download/windows
```

Instalar.

Cerrar PowerShell.

Abrir PowerShell.

Comprobar:

```powershell
ollama --version
```

Si sale version, esta correcto.

---

# 38. Descargar modelo Ollama

```powershell
ollama pull llama3.1
```

Comprobar:

```powershell
ollama list
```

Resultado esperado:

```text
llama3.1
```

Probar:

```powershell
ollama run llama3.1
```

Escribir:

```text
Hola
```

Salir:

```text
/bye
```

---

# 39. Instalar dependencias RAG

Ir a:

```powershell
cd C:\Users\janca\OneDrive\Documentos\Playground\SleepMonitor-fusion-ui-backend-main-ml-20260514\SleepMonitor-0.2.b\ml\RAG
```

Instalar:

```powershell
pip install fastapi uvicorn chromadb ollama pydantic
```

Si falla:

```powershell
python -m pip install fastapi uvicorn chromadb ollama pydantic
```

---

# 40. Arrancar RAG

Desde `ml\RAG`:

```powershell
python RAG_server.py
```

Resultado esperado:

```text
Uvicorn running on http://0.0.0.0:8000
```

Dejar terminal abierta.

---

# 41. Probar RAG sin app

En otra terminal:

```powershell
Invoke-RestMethod -Uri "http://localhost:8000/obtener_consejo" -Method Post -ContentType "application/json" -Body '{"total": 6.5, "rem": 0.8, "profundo": 1.2, "ligero": 4.5}'
```

Resultado esperado:

```text
consejo
-------
Texto personalizado...
```

Si devuelve:

```text
No se ha conseguido conectar con Ollama.
```

entonces FastAPI funciona, pero Ollama no.

---

# 42. Probar FastAPI docs

Abrir:

```text
http://localhost:8000/docs
```

Resultado:

```text
Swagger de FastAPI
POST /obtener_consejo
```

---

# 43. Probar RAG desde app

1. Arrancar Ollama.
2. Arrancar `python RAG_server.py`.
3. Abrir app.
4. Login.
5. Dormir.
6. Ejecutar `Noche tranquila`.
7. Esperar informe.
8. Ver recomendaciones.

Resultado esperado:

```text
Consejo personalizado IA
```

Si no conecta:

```text
Consejo RAG no disponible
```

---

# 44. Importante: localhost en emulador

Desde Windows:

```text
http://localhost:8000
```

funciona.

Desde emulador Android, `localhost` apunta al emulador.

Para conectar con tu PC desde emulador:

```text
http://10.0.2.2:8000
```

Para movil fisico:

```text
http://IP_DE_TU_PC:8000
```

Si PowerShell conecta pero la app no, esta es la causa mas probable.

---

# 45. Comprobar puerto 8000

```powershell
netstat -ano | findstr :8000
```

Resultado esperado:

```text
LISTENING
```

---

# 46. Comprobar firewall

Si usas movil fisico:

1. PC y movil en misma WiFi.
2. Permitir Python en Firewall.
3. Abrir en navegador del movil:

```text
http://IP_DE_TU_PC:8000/docs
```

Resultado esperado:

```text
FastAPI docs
```

---

# 47. Comprobar recomendacion RAG guardada

Tras una sesion:

1. Abrir informe.
2. Ver recomendaciones.
3. Buscar:

```text
Consejo personalizado IA
```

En Firestore:

```text
users/{uid}/sessions/{sessionId}
```

Campo:

```text
recommendations
```

---

# 48. Comprobar informe

Debe mostrar:

- Titular.
- Descripcion.
- Score.
- Metodo de despertar.
- Duracion.
- Movimiento.
- Ruido.
- Fases.
- Recomendaciones.
- Feedback.

---

# 49. Comprobar feedback

1. Finalizar sesion.
2. Poner puntuacion.
3. Guardar.

Debe guardarse:

```text
userScore
discrepancyScore
feedbackStatus
```

---

# 50. Comprobar Firestore tras sesion

En Firestore:

```text
users
  UID
    sessions
      SESSION_ID
```

Dentro:

```text
session
summary
phases
recommendations
```

---

# 51. Comprobar si TFLite se ejecuto

En Firestore buscar:

```text
processedByAi
aiScore
aiEngineVersion
```

Esperado:

```text
processedByAi = true
aiScore = numero
aiEngineVersion = hybrid-tflite-v1
```

En H2:

```sql
SELECT SESSION_ID, AI_SCORE, PROCESSED_BY_AI, AI_ENGINE_VERSION FROM SLEEP_SESSIONS;
```

---

# 52. Comprobacion end-to-end total

1. `ollama --version`
2. `ollama list`
3. `python RAG_server.py`
4. Probar `Invoke-RestMethod`
5. Compilar app
6. Ejecutar app
7. Login
8. Dormir
9. Noche tranquila
10. Informe
11. Consejo RAG
12. Firestore
13. Logout
14. Login otra vez
15. Datos persisten

Resultado esperado:

```text
Usuario -> App -> TFLite -> Acumulador -> RAG/Ollama -> Recomendacion -> Firestore -> UI
```

---

# 53. Errores frecuentes

## `ollama no se reconoce`

Instalar Ollama.

Cerrar y abrir PowerShell.

```powershell
ollama --version
```

## `model llama3.1 not found`

```powershell
ollama pull llama3.1
```

## RAG devuelve `No se ha conseguido conectar con Ollama`

Probar:

```powershell
ollama run llama3.1
```

## PowerShell conecta, app no

Cambiar URL a:

```text
http://10.0.2.2:8000/obtener_consejo
```

## Firebase no crea usuarios

Revisar:

- `app/google-services.json`
- package name
- Email/Password activo
- internet

## Firestore Permission Denied

Revisar:

- reglas desplegadas
- usuario autenticado
- UID correcto

## H2 vacio

Si Firebase esta activo, H2 puede estar vacio.

## Gradle `.lck Acceso denegado`

Cerrar Android Studio y procesos Gradle.

## TensorFlow 16 KB warning

Usar emulador normal 4 KB.

---

# 54. Criterios cubiertos

## Integracion ML

Cubierto con:

```text
SleepAiClassifier.kt
model.tflite
labelsIO.txt
```

## UI de resultados

Cubierto con:

```text
SleepSessionScreen.kt
```

## Flujo app-modelo

Cubierto:

```text
Sensores -> Features -> TFLite -> Fases -> Informe
```

## End-to-end

Cubierto:

```text
Usuario -> App -> Firebase -> ML -> RAG -> UI
```

## Backend

Cubierto:

- Firebase principal.
- Spring/H2 opcional.
- Room local.

## RAG

Cubierto:

- Tiempos por fase.
- Ollama.
- FastAPI.
- Consejo guardado.

---

# 55. Comandos finales rapidos

```powershell
.\gradlew.bat --no-daemon :app:assembleDebug
```

```powershell
.\gradlew.bat --no-daemon :app:testDebugUnitTest
```

```powershell
firebase deploy --only firestore:rules
```

```powershell
ollama pull llama3.1
```

```powershell
cd ml\RAG
python RAG_server.py
```

```powershell
Invoke-RestMethod -Uri "http://localhost:8000/obtener_consejo" -Method Post -ContentType "application/json" -Body '{"total": 6.5, "rem": 0.8, "profundo": 1.2, "ligero": 4.5}'
```

---

# 56. Resumen final

La app queda asi:

```text
Firebase Auth
-> Usuario
-> App Android
-> Room
-> Sensores
-> TensorFlow Lite
-> Acumulador REM/Ligero/Profundo
-> RAG/Ollama
-> Recomendacion personalizada
-> Firestore
-> UI final
```

Para validar la entrega completa:

1. Configura Firebase.
2. Despliega reglas.
3. Instala Ollama.
4. Descarga `llama3.1`.
5. Arranca RAG.
6. Compila app.
7. Ejecuta app.
8. Registra usuario.
9. Ejecuta prueba automatizada.
10. Comprueba informe.
11. Comprueba consejo IA.
12. Comprueba Firestore.
13. Cierra sesion.
14. Vuelve a entrar.
15. Elimina cuenta de prueba.
