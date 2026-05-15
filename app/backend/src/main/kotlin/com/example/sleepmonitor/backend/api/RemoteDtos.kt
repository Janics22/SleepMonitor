package com.example.sleepmonitor.backend.api

import com.example.sleepmonitor.backend.persistence.PhaseRecord
import com.example.sleepmonitor.backend.persistence.RecommendationRecord
import com.example.sleepmonitor.backend.persistence.SensorSummaryRecord
import com.example.sleepmonitor.backend.persistence.SleepSessionRecord
import com.example.sleepmonitor.backend.persistence.UserRecord

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
    val sessionId: String? = null,
    val title: String,
    val description: String,
    val createdAt: Long,
    val applied: Boolean
)

fun RemoteUserDto.toRecord(userIdOverride: String = userId) = UserRecord(
    userId = userIdOverride,
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

fun UserRecord.toDto() = RemoteUserDto(
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

fun RemoteSleepSessionDto.toRecord(sessionIdOverride: String = sessionId) = SleepSessionRecord(
    sessionId = sessionIdOverride,
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

fun SleepSessionRecord.toDto() = RemoteSleepSessionDto(
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

fun RemoteSensorSummaryDto.toRecord(sessionIdOverride: String = sessionId) = SensorSummaryRecord(
    sessionId = sessionIdOverride,
    avgMovement = avgMovement,
    maxMovement = maxMovement,
    avgNoiseDb = avgNoiseDb,
    maxNoiseDb = maxNoiseDb,
    noiseEvents = noiseEvents,
    movementEvents = movementEvents,
    estimatedSleepMinutes = estimatedSleepMinutes,
    totalSamples = totalSamples
)

fun SensorSummaryRecord.toDto() = RemoteSensorSummaryDto(
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

fun RemotePhaseDto.toRecord(sessionIdOverride: String = sessionId) = PhaseRecord(
    phaseId = phaseId,
    sessionId = sessionIdOverride,
    type = type,
    start = start,
    end = end,
    durationSeconds = durationSeconds
)

fun PhaseRecord.toDto() = RemotePhaseDto(
    phaseId = phaseId,
    sessionId = sessionId,
    type = type,
    start = start,
    end = end,
    durationSeconds = durationSeconds
)

fun RemoteRecommendationDto.toRecord(sessionIdOverride: String? = sessionId) = RecommendationRecord(
    recId = recId,
    userId = userId,
    sessionId = sessionIdOverride,
    title = title,
    description = description,
    createdAt = createdAt,
    applied = applied
)

fun RecommendationRecord.toDto() = RemoteRecommendationDto(
    recId = recId,
    userId = userId,
    sessionId = sessionId,
    title = title,
    description = description,
    createdAt = createdAt,
    applied = applied
)
