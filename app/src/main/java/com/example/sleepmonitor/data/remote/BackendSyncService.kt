package com.example.sleepmonitor.data.remote

import android.util.Log
import androidx.room.withTransaction
import com.example.sleepmonitor.data.firebase.FirebaseBackendGateway
import com.example.sleepmonitor.data.firebase.FirebaseIdentity
import com.example.sleepmonitor.data.local.SleepDatabase
import com.example.sleepmonitor.data.local.entities.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class BackendAuthResult {
    data object Unavailable : BackendAuthResult()
    data class Success(val identity: FirebaseIdentity) : BackendAuthResult()
    data class Failure(val message: String) : BackendAuthResult()
}

class BackendSyncService(
    private val db: SleepDatabase,
    private val api: BackendApi?,
    private val firebaseGateway: FirebaseBackendGateway? = null
) {

    val isConfigured: Boolean
        get() = firebaseGateway != null || api != null

    val isFirebaseConfigured: Boolean
        get() = firebaseGateway != null

    suspend fun registerIdentity(email: String, password: String): BackendAuthResult =
        firebaseAuthCall("registerIdentity") { gateway ->
            gateway.register(email, password)
        }

    suspend fun signInIdentity(email: String, password: String): BackendAuthResult =
        firebaseAuthCall("signInIdentity") { gateway ->
            gateway.signIn(email, password)
        }

    suspend fun sendPasswordReset(email: String): BackendAuthResult =
        firebaseAuthCall("sendPasswordReset") { gateway ->
            gateway.sendPasswordReset(email)
            FirebaseIdentity(userId = "", sessionToken = null)
        }

    suspend fun deleteIdentity(userId: String, email: String, password: String): BackendAuthResult =
        firebaseAuthCall("deleteIdentity") { gateway ->
            gateway.deleteAccount(userId, email, password)
            FirebaseIdentity(userId = "", sessionToken = null)
        }

    fun signOutIdentity() {
        firebaseGateway?.signOut()
    }

    suspend fun syncUser(user: UserEntity) {
        val remoteUser = user.toRemoteDto()
        firebaseGateway?.let { gateway ->
            safeNetworkCall("syncUser(firebase)") {
                gateway.upsertUser(remoteUser)
            }
            return
        }
        api?.let { restApi ->
            safeNetworkCall("syncUser(rest)") {
                restApi.upsertUser(user.userId, remoteUser)
            }
        }
    }

    suspend fun deleteUser(userId: String) {
        firebaseGateway?.let { gateway ->
            safeNetworkCall("deleteUser(firebase)") {
                gateway.deleteUser(userId)
            }
            return
        }
        api?.let { restApi ->
            safeNetworkCall("deleteUser(rest)") {
                restApi.deleteUser(userId)
            }
        }
    }

    suspend fun syncSession(sessionId: String) {
        val bundle = buildSessionBundle(sessionId) ?: return
        firebaseGateway?.let { gateway ->
            safeNetworkCall("syncSession(firebase)") {
                gateway.upsertSession(bundle)
            }
            return
        }
        api?.let { restApi ->
            safeNetworkCall("syncSession(rest)") {
                restApi.upsertSession(sessionId, bundle)
            }
        }
    }

    suspend fun deleteSession(sessionId: String) {
        val userId = db.sleepSessionDao().getSessionById(sessionId)?.userId
        if (userId != null) {
            firebaseGateway?.let { gateway ->
                safeNetworkCall("deleteSession(firebase)") {
                    gateway.deleteSession(userId, sessionId)
                }
                return
            }
        }
        api?.let { restApi ->
            safeNetworkCall("deleteSession(rest)") {
                restApi.deleteSession(sessionId)
            }
        }
    }

    suspend fun pullUserSnapshot(userId: String) {
        firebaseGateway?.let { gateway ->
            safeNetworkResult("pullUserSnapshot(firebase)") {
                gateway.getUserSnapshot(userId)
            }?.let { snapshot ->
                mergeSnapshot(snapshot)
            }
            return
        }

        val snapshot = api?.let { restApi ->
            safeNetworkResult("pullUserSnapshot(rest)") {
                restApi.getUserSnapshot(userId)
            }
        } ?: return

        mergeSnapshot(snapshot)
    }

    private suspend fun buildSessionBundle(sessionId: String): RemoteSessionBundleDto? {
        val session = db.sleepSessionDao().getSessionById(sessionId) ?: return null
        val summary = db.sensorSummaryDao().getSummaryForSession(sessionId)
        val phases = db.phaseDao().getPhasesForSessionOnce(sessionId)
        val recommendations = db.recommendationDao().getRecommendationsForSession(sessionId)

        return RemoteSessionBundleDto(
            session = session.toRemoteDto(),
            summary = summary?.toRemoteDto(),
            phases = phases.map { it.toRemoteDto() },
            recommendations = recommendations.map { it.toRemoteDto() }
        )
    }

    private suspend fun mergeSnapshot(snapshot: RemoteUserSnapshotDto) {
        db.withTransaction {
            snapshot.user?.let { db.userDao().upsertUser(it.toEntity()) }
            snapshot.sessions.forEach { remoteBundle ->
                val session = remoteBundle.session.toEntity()
                db.sleepSessionDao().insertSession(session)

                remoteBundle.summary?.let { summary ->
                    db.sensorSummaryDao().insertSummary(summary.toEntity())
                }

                db.phaseDao().deleteForSession(session.sessionId)
                if (remoteBundle.phases.isNotEmpty()) {
                    db.phaseDao().insertPhases(remoteBundle.phases.map(RemotePhaseDto::toEntity))
                }

                db.recommendationDao().deleteForSession(session.sessionId)
                if (remoteBundle.recommendations.isNotEmpty()) {
                    db.recommendationDao().insertRecommendations(
                        remoteBundle.recommendations.map(RemoteRecommendationDto::toEntity)
                    )
                }
            }
        }
    }

    private suspend inline fun firebaseAuthCall(
        operation: String,
        crossinline block: suspend (FirebaseBackendGateway) -> FirebaseIdentity
    ): BackendAuthResult {
        val gateway = firebaseGateway ?: return BackendAuthResult.Unavailable

        return runCatching {
            withContext(Dispatchers.IO) {
                BackendAuthResult.Success(block(gateway))
            }
        }.getOrElse { error ->
            Log.w("BackendSyncService", "Firebase auth failed during $operation", error)
            BackendAuthResult.Failure(error.toUserMessage())
        }
    }

    private suspend inline fun safeNetworkCall(
        operation: String,
        crossinline block: suspend () -> Unit
    ) {
        runCatching {
            withContext(Dispatchers.IO) {
                block()
            }
        }.onFailure { error ->
            Log.w("BackendSyncService", "Backend sync failed during $operation", error)
        }
    }

    private suspend inline fun <T> safeNetworkResult(
        operation: String,
        crossinline block: suspend () -> T
    ): T? {
        return runCatching {
            withContext(Dispatchers.IO) {
                block()
            }
        }.getOrElse { error ->
            Log.w("BackendSyncService", "Backend sync failed during $operation", error)
            null
        }
    }

    private fun Throwable.toUserMessage(): String =
        localizedMessage?.takeIf { it.isNotBlank() }
            ?: "No se pudo completar la operacion remota."
}
