package com.example.sleepmonitor.data.firebase

import android.content.Context
import android.util.Log
import com.example.sleepmonitor.data.remote.RemotePhaseDto
import com.example.sleepmonitor.data.remote.RemoteRecommendationDto
import com.example.sleepmonitor.data.remote.RemoteSensorSummaryDto
import com.example.sleepmonitor.data.remote.RemoteSessionBundleDto
import com.example.sleepmonitor.data.remote.RemoteSleepSessionDto
import com.example.sleepmonitor.data.remote.RemoteUserDto
import com.example.sleepmonitor.data.remote.RemoteUserSnapshotDto
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class FirebaseIdentity(
    val userId: String,
    val sessionToken: String?
)

class FirebaseBackendGateway private constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    companion object {
        fun create(context: Context): FirebaseBackendGateway? {
            val app = runCatching {
                FirebaseApp.initializeApp(context.applicationContext) ?: FirebaseApp.getInstance()
            }.getOrElse { error ->
                Log.i("FirebaseBackend", "Firebase no esta configurado todavia.", error)
                null
            } ?: return null

            return FirebaseBackendGateway(
                auth = FirebaseAuth.getInstance(app),
                firestore = FirebaseFirestore.getInstance(app)
            )
        }
    }

    suspend fun register(email: String, password: String): FirebaseIdentity {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase Auth no devolvio usuario.")
        return FirebaseIdentity(
            userId = user.uid,
            sessionToken = user.getIdToken(false).await().token
        )
    }

    suspend fun signIn(email: String, password: String): FirebaseIdentity {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase Auth no devolvio usuario.")
        return FirebaseIdentity(
            userId = user.uid,
            sessionToken = user.getIdToken(false).await().token
        )
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun deleteAccount(userId: String, email: String, password: String) {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase Auth no devolvio usuario.")
        if (user.uid != userId) {
            error("La identidad autenticada no coincide con el usuario local.")
        }

        deleteUser(userId)
        user.delete().await()
    }

    suspend fun upsertUser(user: RemoteUserDto) {
        users().document(user.userId)
            .set(user.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    suspend fun deleteUser(userId: String) {
        val userRef = users().document(userId)
        val sessions = userRef.collection("sessions").get().await().documents
        sessions.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        userRef.delete().await()
    }

    suspend fun upsertSession(bundle: RemoteSessionBundleDto) {
        val session = bundle.session
        users().document(session.userId)
            .collection("sessions")
            .document(session.sessionId)
            .set(bundle.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    suspend fun deleteSession(userId: String, sessionId: String) {
        users().document(userId)
            .collection("sessions")
            .document(sessionId)
            .delete()
            .await()
    }

    suspend fun getUserSnapshot(userId: String): RemoteUserSnapshotDto {
        val userRef = users().document(userId)
        val user = userRef.get().await().data?.asStringMap()?.toRemoteUserDto(userId)
        val sessions = userRef.collection("sessions")
            .get()
            .await()
            .documents
            .mapNotNull { it.data?.asStringMap()?.toRemoteSessionBundleDto() }
            .sortedByDescending { it.session.startTime }

        return RemoteUserSnapshotDto(
            user = user,
            sessions = sessions
        )
    }

    private fun users() = firestore.collection("users")
}

private fun RemoteUserDto.toFirestoreMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "email" to email,
    "username" to username,
    "passwordHash" to "",
    "createdAt" to createdAt,
    "peso" to peso,
    "altura" to altura,
    "sexo" to sexo,
    "pais" to pais,
    "fechaNacimiento" to fechaNacimiento,
    "sleepProfile" to sleepProfile,
    "aiCalibrationScore" to aiCalibrationScore
)

private fun RemoteSleepSessionDto.toFirestoreMap(): Map<String, Any?> = mapOf(
    "sessionId" to sessionId,
    "userId" to userId,
    "startTime" to startTime,
    "endTime" to endTime,
    "alarmWindowStart" to alarmWindowStart,
    "alarmWindowEnd" to alarmWindowEnd,
    "sampleIntervalMs" to sampleIntervalMs,
    "realWakeUpTime" to realWakeUpTime,
    "aiScore" to aiScore,
    "userScore" to userScore,
    "discrepancyScore" to discrepancyScore,
    "feedbackStatus" to feedbackStatus,
    "calibrated" to calibrated,
    "sensorFailure" to sensorFailure,
    "batteryWarning" to batteryWarning,
    "processedByAi" to processedByAi,
    "status" to status,
    "wakeMethod" to wakeMethod,
    "aiEngineVersion" to aiEngineVersion
)

private fun RemoteSensorSummaryDto.toFirestoreMap(): Map<String, Any?> = mapOf(
    "sessionId" to sessionId,
    "avgMovement" to avgMovement,
    "maxMovement" to maxMovement,
    "avgNoiseDb" to avgNoiseDb,
    "maxNoiseDb" to maxNoiseDb,
    "noiseEvents" to noiseEvents,
    "movementEvents" to movementEvents,
    "estimatedSleepMinutes" to estimatedSleepMinutes,
    "totalSamples" to totalSamples
)

private fun RemotePhaseDto.toFirestoreMap(): Map<String, Any?> = mapOf(
    "phaseId" to phaseId,
    "sessionId" to sessionId,
    "type" to type,
    "start" to start,
    "end" to end,
    "durationSeconds" to durationSeconds
)

private fun RemoteRecommendationDto.toFirestoreMap(): Map<String, Any?> = mapOf(
    "recId" to recId,
    "userId" to userId,
    "sessionId" to sessionId,
    "title" to title,
    "description" to description,
    "createdAt" to createdAt,
    "applied" to applied
)

private fun RemoteSessionBundleDto.toFirestoreMap(): Map<String, Any?> = mapOf(
    "session" to session.toFirestoreMap(),
    "summary" to summary?.toFirestoreMap(),
    "phases" to phases.map { it.toFirestoreMap() },
    "recommendations" to recommendations.map { it.toFirestoreMap() }
)

private fun Map<String, Any?>.toRemoteUserDto(userIdOverride: String? = null): RemoteUserDto = RemoteUserDto(
    userId = userIdOverride ?: requiredString("userId"),
    email = requiredString("email"),
    username = requiredString("username"),
    passwordHash = optionalString("passwordHash").orEmpty(),
    createdAt = requiredLong("createdAt"),
    peso = optionalInt("peso"),
    altura = optionalInt("altura"),
    sexo = optionalString("sexo"),
    pais = optionalString("pais"),
    fechaNacimiento = optionalLong("fechaNacimiento"),
    sleepProfile = optionalString("sleepProfile"),
    aiCalibrationScore = optionalFloat("aiCalibrationScore") ?: 0f
)

private fun Map<String, Any?>.toRemoteSleepSessionDto(): RemoteSleepSessionDto = RemoteSleepSessionDto(
    sessionId = requiredString("sessionId"),
    userId = requiredString("userId"),
    startTime = requiredLong("startTime"),
    endTime = optionalLong("endTime"),
    alarmWindowStart = requiredString("alarmWindowStart"),
    alarmWindowEnd = requiredString("alarmWindowEnd"),
    sampleIntervalMs = requiredLong("sampleIntervalMs"),
    realWakeUpTime = optionalLong("realWakeUpTime"),
    aiScore = optionalInt("aiScore"),
    userScore = optionalInt("userScore"),
    discrepancyScore = optionalInt("discrepancyScore"),
    feedbackStatus = optionalString("feedbackStatus") ?: "PENDING",
    calibrated = optionalBoolean("calibrated") ?: false,
    sensorFailure = optionalBoolean("sensorFailure") ?: false,
    batteryWarning = optionalBoolean("batteryWarning") ?: false,
    processedByAi = optionalBoolean("processedByAi") ?: false,
    status = optionalString("status") ?: "ACTIVE",
    wakeMethod = optionalString("wakeMethod"),
    aiEngineVersion = optionalString("aiEngineVersion")
)

private fun Map<String, Any?>.toRemoteSensorSummaryDto(): RemoteSensorSummaryDto = RemoteSensorSummaryDto(
    sessionId = requiredString("sessionId"),
    avgMovement = requiredFloat("avgMovement"),
    maxMovement = requiredFloat("maxMovement"),
    avgNoiseDb = requiredFloat("avgNoiseDb"),
    maxNoiseDb = requiredFloat("maxNoiseDb"),
    noiseEvents = requiredInt("noiseEvents"),
    movementEvents = requiredInt("movementEvents"),
    estimatedSleepMinutes = requiredInt("estimatedSleepMinutes"),
    totalSamples = requiredInt("totalSamples")
)

private fun Map<String, Any?>.toRemotePhaseDto(): RemotePhaseDto = RemotePhaseDto(
    phaseId = requiredString("phaseId"),
    sessionId = requiredString("sessionId"),
    type = requiredString("type"),
    start = requiredLong("start"),
    end = requiredLong("end"),
    durationSeconds = requiredInt("durationSeconds")
)

private fun Map<String, Any?>.toRemoteRecommendationDto(): RemoteRecommendationDto = RemoteRecommendationDto(
    recId = requiredString("recId"),
    userId = requiredString("userId"),
    sessionId = optionalString("sessionId"),
    title = requiredString("title"),
    description = requiredString("description"),
    createdAt = requiredLong("createdAt"),
    applied = optionalBoolean("applied") ?: false
)

private fun Map<String, Any?>.toRemoteSessionBundleDto(): RemoteSessionBundleDto {
    val session = requiredMap("session").toRemoteSleepSessionDto()
    return RemoteSessionBundleDto(
        session = session,
        summary = optionalMap("summary")?.toRemoteSensorSummaryDto(),
        phases = optionalMapList("phases").map { it.toRemotePhaseDto() },
        recommendations = optionalMapList("recommendations").map { it.toRemoteRecommendationDto() }
    )
}

private fun Any?.asStringMap(): Map<String, Any?>? = (this as? Map<*, *>)?.entries
    ?.mapNotNull { entry ->
        val key = entry.key as? String ?: return@mapNotNull null
        key to entry.value
    }
    ?.toMap()

private fun Map<String, Any?>.requiredMap(key: String): Map<String, Any?> =
    optionalMap(key) ?: error("Campo remoto requerido ausente: $key")

private fun Map<String, Any?>.optionalMap(key: String): Map<String, Any?>? = this[key].asStringMap()

private fun Map<String, Any?>.optionalMapList(key: String): List<Map<String, Any?>> =
    (this[key] as? List<*>)?.mapNotNull { it.asStringMap() }.orEmpty()

private fun Map<String, Any?>.requiredString(key: String): String =
    optionalString(key) ?: error("Campo remoto requerido ausente: $key")

private fun Map<String, Any?>.optionalString(key: String): String? = this[key] as? String

private fun Map<String, Any?>.requiredLong(key: String): Long =
    optionalLong(key) ?: error("Campo remoto requerido ausente: $key")

private fun Map<String, Any?>.optionalLong(key: String): Long? = (this[key] as? Number)?.toLong()

private fun Map<String, Any?>.requiredInt(key: String): Int =
    optionalInt(key) ?: error("Campo remoto requerido ausente: $key")

private fun Map<String, Any?>.optionalInt(key: String): Int? = (this[key] as? Number)?.toInt()

private fun Map<String, Any?>.requiredFloat(key: String): Float =
    optionalFloat(key) ?: error("Campo remoto requerido ausente: $key")

private fun Map<String, Any?>.optionalFloat(key: String): Float? = (this[key] as? Number)?.toFloat()

private fun Map<String, Any?>.optionalBoolean(key: String): Boolean? = this[key] as? Boolean
