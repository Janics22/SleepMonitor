# Firebase setup

Firebase es el backend remoto principal de la app. El backend Spring/H2 queda como fallback local de desarrollo cuando Firebase no esta configurado.

## 1. Crear el proyecto Firebase

1. Entra en Firebase Console y crea un proyecto.
2. Anade una app Android con package name `com.example.sleepmonitor`.
3. Descarga el archivo real `google-services.json`.
4. Copialo en `app/google-services.json`.

Importante: `app/google-services.json` contiene claves del proyecto y esta ignorado por Git. No uses `google-services.example.json` como configuracion real; solo sirve como referencia de estructura.

## 2. Activar servicios

Activa estos servicios en Firebase Console:

- Authentication: proveedor Email/Password.
- Cloud Firestore: base de datos en modo production.
- Cloud Storage: opcional, reservado para futuros informes o artefactos exportados.

## 3. Desplegar reglas

Desde la raiz del proyecto, con Firebase CLI autenticado:

```powershell
firebase deploy --only firestore:rules,storage
```

El modelo de datos usado por la app es:

```text
users/{uid}
users/{uid}/sessions/{sessionId}
```

Cada usuario autenticado solo puede leer y escribir su propio documento y sus propias sesiones.

## 4. Ejecutar la app con Firebase

Cuando `app/google-services.json` existe, Gradle aplica automaticamente el plugin `com.google.gms.google-services` y la app inicializa:

- Firebase Authentication para registro, login, reset y borrado de cuenta.
- Firestore para perfil, sesiones, resumenes, fases y recomendaciones.

Comando recomendado:

```powershell
.\gradlew.bat :app:assembleDebug
```

Despues ejecuta la app en el emulador o dispositivo desde Android Studio.

## 5. Fallback local

Si falta `app/google-services.json`, la app sigue funcionando con Room y con el backend Spring local si `BACKEND_BASE_URL` esta configurado.

En debug, el proyecto conserva `http://10.0.2.2:8080/` como URL local para el emulador. Aun asi, si Firebase esta configurado, Firebase se usa como backend remoto principal.
