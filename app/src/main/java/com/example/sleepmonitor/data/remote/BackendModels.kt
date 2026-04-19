package com.example.sleepmonitor.data.remote

import com.example.sleepmonitor.data.local.entities.PhaseEntity
import com.example.sleepmonitor.data.local.entities.RecommendationEntity
import com.example.sleepmonitor.data.local.entities.SensorSummaryEntity
import com.example.sleepmonitor.data.local.entities.SleepSessionEntity
import com.example.sleepmonitor.data.local.entities.UserEntity

data class RemoteUserSnapshotDto(
    val user: RemoteUserDto? = null,
    val sessions: List<RemoteSessionBundleDto> = emptyList()
)

data class RemoteUserDto(
    val userId: String,
    val email: String,
    val username: String,
    val passwordHash: String,
    val createdAt: Long,
    val peso: Int? = null,
    val altura: Int? = null,
    val sexo: String? = null,
    val pais: String? = null,
    val fechaNacimiento: Long? = null,
    val sleepProfile: String? = null,
    val aiCalibrationScore: Float = 0f
)

data class RemoteSessionBundleDto(
    val session: RemoteSleepSessionDto,
    val summary: RemoteSensorSummaryDto? = null,
    val phases: List<RemotePhaseDto> = emptyList(),
    val recommendations: List<RemoteRecommendationDto> = emptyList()
)

data class RemoteSleepSessionDto(
    val sessionId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val alarmWindowStart: String,
    val alarmWindowEnd: String,
    val sampleIntervalMs: Long,
    val realWakeUpTime: Long? = null,
    val aiScore: Int? = null,
    val userScore: Int? = null,
    val discrepancyScore: Int? = null,
    val feedbackStatus: String,
    val calibrated: Boolean,
    val sensorFailure: Boolean,
    val batteryWarning: Boolean,
    val processedByAi: Boolean,
    val status: String,
    val wakeMethod: String? = null,
    val aiEngineVersion: String? = null
)

data class RemoteSensorSummaryDto(
    val sessionId: String,
    val avgMovement: Float,
    val maxMovement: Float,
    val avgNoiseDb: Float,
    val maxNoiseDb: Float,
    val noiseEvents: Int,
    val movementEvents: Int,
    val estimatedSleepMinutes: Int,
    val totalSamples: Int
)

data class RemotePhaseDto(
    val phaseId: String,
    val sessionId: String,
    val type: String,
    val start: Long,
    val end: Long,
    val durationSeconds: Int
)

data class RemoteRecommendationDto(
    val recId: String,
    val userId: String,
    val sessionId: String?,
    val title: String,
    val description: String,
    val createdAt: Long,
    val applied: Boolean
)

fun UserEntity.toRemoteDto() = RemoteUserDto(
    userId = userId,
    email = email,
    username = username,
    passwordHash = passwordHash,
    createdAt = createdAt,
    peso = peso,
    altura = altura,
    sexo = sexo,
    pais = pais,
    fechaNacimiento = fechaNacimiento,
    sleepProfile = sleepProfile,
    aiCalibrationScore = aiCalibrationScore
)

fun RemoteUserDto.toEntity() = UserEntity(
    userId = userId,
    email = email,
    username = username,
    passwordHash = passwordHash,
    createdAt = createdAt,
    peso = peso,
    altura = altura,
    sexo = sexo,
    pais = pais,
    fechaNacimiento = fechaNacimiento,
    sleepProfile = sleepProfile,
    aiCalibrationScore = aiCalibrationScore
)

fun SleepSessionEntity.toRemoteDto() = RemoteSleepSessionDto(
    sessionId = sessionId,
    userId = userId,
    startTime = startTime,
    endTime = endTime,
    alarmWindowStart = alarmWindowStart,
    alarmWindowEnd = alarmWindowEnd,
    sampleIntervalMs = sampleIntervalMs,
    realWakeUpTime = realWakeUpTime,
    aiScore = aiScore,
    userScore = userScore,
    discrepancyScore = discrepancyScore,
    feedbackStatus = feedbackStatus,
    calibrated = calibrated,
    sensorFailure = sensorFailure,
    batteryWarning = batteryWarning,
    processedByAi = processedByAi,
    status = status,
    wakeMethod = wakeMethod,
    aiEngineVersion = aiEngineVersion
)

fun RemoteSleepSessionDto.toEntity() = SleepSessionEntity(
    sessionId = sessionId,
    userId = userId,
    startTime = startTime,
    endTime = endTime,
    alarmWindowStart = alarmWindowStart,
    alarmWindowEnd = alarmWindowEnd,
    sampleIntervalMs = sampleIntervalMs,
    realWakeUpTime = realWakeUpTime,
    aiScore = aiScore,
    userScore = userScore,
    discrepancyScore = discrepancyScore,
    feedbackStatus = feedbackStatus,
    calibrated = calibrated,
    sensorFailure = sensorFailure,
    batteryWarning = batteryWarning,
    processedByAi = processedByAi,
    status = status,
    wakeMethod = wakeMethod,
    aiEngineVersion = aiEngineVersion
)

fun SensorSummaryEntity.toRemoteDto() = RemoteSensorSummaryDto(
    sessionId = sessionId,
    avgMovement = avgMovement,
    maxMovement = maxMovement,
    avgNoiseDb = avgNoiseDb,
    maxNoiseDb = maxNoiseDb,
    noiseEvents = noiseEvents,
    movementEvents = movementEvents,
    estimatedSleepMinutes = estimatedSleepMinutes,
    totalSamples = totalSamples
)

fun RemoteSensorSummaryDto.toEntity() = SensorSummaryEntity(
    sessionId = sessionId,
    avgMovement = avgMovement,
    maxMovement = maxMovement,
    avgNoiseDb = avgNoiseDb,
    maxNoiseDb = maxNoiseDb,
    noiseEvents = noiseEvents,
    movementEvents = movementEvents,
    estimatedSleepMinutes = estimatedSleepMinutes,
    totalSamples = totalSamples
)

fun PhaseEntity.toRemoteDto() = RemotePhaseDto(
    phaseId = phaseId,
    sessionId = sessionId,
    type = type,
    start = start,
    end = end,
    durationSeconds = durationSeconds
)

fun RemotePhaseDto.toEntity() = PhaseEntity(
    phaseId = phaseId,
    sessionId = sessionId,
    type = type,
    start = start,
    end = end,
    durationSeconds = durationSeconds
)

fun RecommendationEntity.toRemoteDto() = RemoteRecommendationDto(
    recId = recId,
    userId = userId,
    sessionId = sessionId,
    title = title,
    description = description,
    createdAt = createdAt,
    applied = applied
)

fun RemoteRecommendationDto.toEntity() = RecommendationEntity(
    recId = recId,
    userId = userId,
    sessionId = sessionId,
    title = title,
    description = description,
    createdAt = createdAt,
    applied = applied
)
