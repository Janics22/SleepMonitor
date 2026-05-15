package com.example.sleepmonitor.backend.service

import com.example.sleepmonitor.backend.api.RemoteSessionBundleDto
import com.example.sleepmonitor.backend.api.RemoteUserDto
import com.example.sleepmonitor.backend.api.RemoteUserSnapshotDto
import com.example.sleepmonitor.backend.api.BadRequestException
import com.example.sleepmonitor.backend.api.NotFoundException
import com.example.sleepmonitor.backend.api.toDto
import com.example.sleepmonitor.backend.api.toRecord
import com.example.sleepmonitor.backend.persistence.PhaseRepository
import com.example.sleepmonitor.backend.persistence.RecommendationRepository
import com.example.sleepmonitor.backend.persistence.SensorSummaryRepository
import com.example.sleepmonitor.backend.persistence.SleepSessionRepository
import com.example.sleepmonitor.backend.persistence.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SleepMonitorSyncService(
    private val userRepository: UserRepository,
    private val sleepSessionRepository: SleepSessionRepository,
    private val sensorSummaryRepository: SensorSummaryRepository,
    private val phaseRepository: PhaseRepository,
    private val recommendationRepository: RecommendationRepository
) {

    @Transactional(readOnly = true)
    fun getUser(userId: String): RemoteUserDto {
        validateId(userId, "userId")
        return userRepository.findByIdOrNull(userId)?.toDto()
            ?: throw NotFoundException("Usuario no encontrado")
    }

    @Transactional(readOnly = true)
    fun getUserSnapshot(userId: String): RemoteUserSnapshotDto {
        validateId(userId, "userId")
        val user = userRepository.findByIdOrNull(userId)?.toDto()
        val sessions = sleepSessionRepository.findAllByUserIdOrderByStartTimeDesc(userId).map { sessionRecord ->
            buildSessionBundle(sessionRecord.sessionId, sessionRecord.toDto())
        }

        return RemoteUserSnapshotDto(
            user = user,
            sessions = sessions
        )
    }

    @Transactional(readOnly = true)
    fun getUserSessions(userId: String): List<RemoteSessionBundleDto> {
        validateId(userId, "userId")
        return sleepSessionRepository.findAllByUserIdOrderByStartTimeDesc(userId)
            .map { buildSessionBundle(it.sessionId, it.toDto()) }
    }

    @Transactional(readOnly = true)
    fun getSession(sessionId: String): RemoteSessionBundleDto {
        validateId(sessionId, "sessionId")
        val session = sleepSessionRepository.findByIdOrNull(sessionId)?.toDto()
            ?: throw NotFoundException("Sesion no encontrada")
        return buildSessionBundle(sessionId, session)
    }

    @Transactional(readOnly = true)
    fun getUserRecommendations(userId: String) =
        recommendationRepository.findAllByUserIdOrderByCreatedAtAsc(userId).map { it.toDto() }

    @Transactional(readOnly = true)
    fun getSessionRecommendations(sessionId: String) =
        recommendationRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId).map { it.toDto() }

    @Transactional
    fun upsertUser(userId: String, user: RemoteUserDto) {
        validateUser(userId, user)
        userRepository.save(user.toRecord(userIdOverride = userId))
    }

    @Transactional
    fun deleteUser(userId: String) {
        validateId(userId, "userId")
        val sessionIds = sleepSessionRepository.findAllByUserIdOrderByStartTimeDesc(userId).map { it.sessionId }
        sessionIds.forEach(::deleteSession)
        recommendationRepository.deleteAllByUserId(userId)
        userRepository.findByIdOrNull(userId)?.let(userRepository::delete)
    }

    @Transactional
    fun upsertSession(sessionId: String, bundle: RemoteSessionBundleDto) {
        validateSessionBundle(sessionId, bundle)
        sleepSessionRepository.save(bundle.session.toRecord(sessionIdOverride = sessionId))

        val summary = bundle.summary
        if (summary == null) {
            sensorSummaryRepository.findByIdOrNull(sessionId)?.let(sensorSummaryRepository::delete)
        } else {
            sensorSummaryRepository.save(summary.toRecord(sessionIdOverride = sessionId))
        }

        phaseRepository.deleteAllBySessionId(sessionId)
        if (bundle.phases.isNotEmpty()) {
            phaseRepository.saveAll(bundle.phases.map { it.toRecord(sessionIdOverride = sessionId) })
        }

        recommendationRepository.deleteAllBySessionId(sessionId)
        if (bundle.recommendations.isNotEmpty()) {
            recommendationRepository.saveAll(
                bundle.recommendations.map { it.toRecord(sessionIdOverride = sessionId) }
            )
        }
    }

    @Transactional
    fun deleteSession(sessionId: String) {
        validateId(sessionId, "sessionId")
        phaseRepository.deleteAllBySessionId(sessionId)
        recommendationRepository.deleteAllBySessionId(sessionId)
        sensorSummaryRepository.findByIdOrNull(sessionId)?.let(sensorSummaryRepository::delete)
        sleepSessionRepository.findByIdOrNull(sessionId)?.let(sleepSessionRepository::delete)
    }

    private fun buildSessionBundle(sessionId: String, session: com.example.sleepmonitor.backend.api.RemoteSleepSessionDto): RemoteSessionBundleDto =
        RemoteSessionBundleDto(
            session = session,
            summary = sensorSummaryRepository.findByIdOrNull(sessionId)?.toDto(),
            phases = phaseRepository.findAllBySessionIdOrderByStartAsc(sessionId).map { it.toDto() },
            recommendations = recommendationRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId)
                .map { it.toDto() }
        )

    private fun validateUser(userId: String, user: RemoteUserDto) {
        validateId(userId, "userId")
        if (user.userId.isBlank() || user.userId != userId) {
            throw BadRequestException("El userId de la ruta y del cuerpo deben coincidir")
        }
        if (!user.email.contains("@") || user.email.length < 5) {
            throw BadRequestException("Email no valido")
        }
        if (user.username.length < 3) {
            throw BadRequestException("El username debe tener al menos 3 caracteres")
        }
        if (user.passwordHash.isBlank()) {
            throw BadRequestException("passwordHash es obligatorio")
        }
    }

    private fun validateSessionBundle(sessionId: String, bundle: RemoteSessionBundleDto) {
        validateId(sessionId, "sessionId")
        val session = bundle.session
        if (session.sessionId.isBlank() || session.sessionId != sessionId) {
            throw BadRequestException("El sessionId de la ruta y del cuerpo deben coincidir")
        }
        validateId(session.userId, "userId")
        if (session.alarmWindowStart.isBlank() || session.alarmWindowEnd.isBlank()) {
            throw BadRequestException("La ventana de alarma es obligatoria")
        }
        if (session.sampleIntervalMs <= 0) {
            throw BadRequestException("sampleIntervalMs debe ser mayor que cero")
        }
    }

    private fun validateId(value: String, name: String) {
        if (value.isBlank()) {
            throw BadRequestException("$name no puede estar vacio")
        }
    }
}
