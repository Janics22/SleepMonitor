package com.example.sleepmonitor.backend.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserRecord, String>

interface SleepSessionRepository : JpaRepository<SleepSessionRecord, String> {
    fun findAllByUserIdOrderByStartTimeDesc(userId: String): List<SleepSessionRecord>
    fun deleteAllByUserId(userId: String)
}

interface SensorSummaryRepository : JpaRepository<SensorSummaryRecord, String>

interface PhaseRepository : JpaRepository<PhaseRecord, String> {
    fun findAllBySessionIdOrderByStartAsc(sessionId: String): List<PhaseRecord>
    fun deleteAllBySessionId(sessionId: String)
}

interface RecommendationRepository : JpaRepository<RecommendationRecord, String> {
    fun findAllByUserIdOrderByCreatedAtAsc(userId: String): List<RecommendationRecord>
    fun findAllBySessionIdOrderByCreatedAtAsc(sessionId: String): List<RecommendationRecord>
    fun deleteAllBySessionId(sessionId: String)
    fun deleteAllByUserId(userId: String)
}
