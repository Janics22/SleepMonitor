package com.example.sleepmonitor.backend.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserRecord(
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: String = "",
    @Column(nullable = false, unique = true)
    var email: String = "",
    @Column(nullable = false, unique = true)
    var username: String = "",
    @Column(nullable = false, length = 512)
    var passwordHash: String = "",
    @Column(nullable = false)
    var createdAt: Long = 0,
    var peso: Int? = null,
    var altura: Int? = null,
    var sexo: String? = null,
    var pais: String? = null,
    var fechaNacimiento: Long? = null,
    var sleepProfile: String? = null,
    @Column(nullable = false)
    var aiCalibrationScore: Float = 0f
)

@Entity
@Table(name = "sleep_sessions")
class SleepSessionRecord(
    @Id
    @Column(name = "session_id", nullable = false)
    var sessionId: String = "",
    @Column(name = "user_id", nullable = false)
    var userId: String = "",
    @Column(nullable = false)
    var startTime: Long = 0,
    var endTime: Long? = null,
    @Column(nullable = false)
    var alarmWindowStart: String = "",
    @Column(nullable = false)
    var alarmWindowEnd: String = "",
    @Column(nullable = false)
    var sampleIntervalMs: Long = 0,
    var realWakeUpTime: Long? = null,
    var aiScore: Int? = null,
    var userScore: Int? = null,
    var discrepancyScore: Int? = null,
    @Column(nullable = false)
    var feedbackStatus: String = "",
    @Column(nullable = false)
    var calibrated: Boolean = false,
    @Column(nullable = false)
    var sensorFailure: Boolean = false,
    @Column(nullable = false)
    var batteryWarning: Boolean = false,
    @Column(nullable = false)
    var processedByAi: Boolean = false,
    @Column(nullable = false)
    var status: String = "",
    var wakeMethod: String? = null,
    var aiEngineVersion: String? = null
)

@Entity
@Table(name = "sensor_summaries")
class SensorSummaryRecord(
    @Id
    @Column(name = "session_id", nullable = false)
    var sessionId: String = "",
    @Column(nullable = false)
    var avgMovement: Float = 0f,
    @Column(nullable = false)
    var maxMovement: Float = 0f,
    @Column(nullable = false)
    var avgNoiseDb: Float = 0f,
    @Column(nullable = false)
    var maxNoiseDb: Float = 0f,
    @Column(nullable = false)
    var noiseEvents: Int = 0,
    @Column(nullable = false)
    var movementEvents: Int = 0,
    @Column(nullable = false)
    var estimatedSleepMinutes: Int = 0,
    @Column(nullable = false)
    var totalSamples: Int = 0
)

@Entity
@Table(name = "phases")
class PhaseRecord(
    @Id
    @Column(name = "phase_id", nullable = false)
    var phaseId: String = "",
    @Column(name = "session_id", nullable = false)
    var sessionId: String = "",
    @Column(nullable = false)
    var type: String = "",
    @Column(name = "phase_start", nullable = false)
    var start: Long = 0,
    @Column(name = "phase_end", nullable = false)
    var end: Long = 0,
    @Column(nullable = false)
    var durationSeconds: Int = 0
)

@Entity
@Table(name = "recommendations")
class RecommendationRecord(
    @Id
    @Column(name = "rec_id", nullable = false)
    var recId: String = "",
    @Column(name = "user_id", nullable = false)
    var userId: String = "",
    @Column(name = "session_id")
    var sessionId: String? = null,
    @Column(nullable = false)
    var title: String = "",
    @Column(nullable = false, length = 2048)
    var description: String = "",
    @Column(nullable = false)
    var createdAt: Long = 0,
    @Column(nullable = false)
    var applied: Boolean = false
)
