package com.example.sleepmonitor.data.remote

import android.util.Log
import androidx.room.withTransaction
import com.example.sleepmonitor.data.local.SleepDatabase
import com.example.sleepmonitor.data.local.entities.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackendSyncService(
    private val db: SleepDatabase,
    private val api: BackendApi?
) {

    val isConfigured: Boolean
        get() = api != null

    suspend fun syncUser(user: UserEntity) {
        safeNetworkCall("syncUser") {
            api?.upsertUser(user.userId, user.toRemoteDto())
        }
    }

    suspend fun deleteUser(userId: String) {
        safeNetworkCall("deleteUser") {
            api?.deleteUser(userId)
        }
    }

    suspend fun syncSession(sessionId: String) {
        safeNetworkCall("syncSession") {
            val bundle = buildSessionBundle(sessionId) ?: return@safeNetworkCall
            api?.upsertSession(sessionId, bundle)
        }
    }

    suspend fun deleteSession(sessionId: String) {
        safeNetworkCall("deleteSession") {
            api?.deleteSession(sessionId)
        }
    }

    suspend fun pullUserSnapshot(userId: String) {
        safeNetworkCall("pullUserSnapshot") {
            val snapshot = api?.getUserSnapshot(userId) ?: return@safeNetworkCall
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

    private suspend inline fun safeNetworkCall(
        operation: String,
        crossinline block: suspend () -> Unit
    ) {
        if (api == null) return

        runCatching {
            withContext(Dispatchers.IO) {
                block()
            }
        }.onFailure { error ->
            Log.w("BackendSyncService", "Backend sync failed during $operation", error)
        }
    }
}
