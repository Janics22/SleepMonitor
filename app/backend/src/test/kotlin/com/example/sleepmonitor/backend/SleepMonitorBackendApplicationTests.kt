package com.example.sleepmonitor.backend

import com.example.sleepmonitor.backend.api.RemoteSessionBundleDto
import com.example.sleepmonitor.backend.api.RemoteSleepSessionDto
import com.example.sleepmonitor.backend.api.RemoteUserDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SleepMonitorBackendApplicationTests(
    @Autowired private val restTemplate: TestRestTemplate,
    @LocalServerPort private val port: Int
) {

    @Test
    fun `user snapshot includes persisted session bundle`() {
        val userId = "user-123"
        val sessionId = "session-456"

        val user = RemoteUserDto(
            userId = userId,
            email = "demo@example.com",
            username = "demo",
            passwordHash = "hashed-password",
            createdAt = 1710000000000,
            pais = "ES"
        )
        val session = RemoteSessionBundleDto(
            session = RemoteSleepSessionDto(
                sessionId = sessionId,
                userId = userId,
                startTime = 1710000000000,
                endTime = 1710003600000,
                alarmWindowStart = "06:30",
                alarmWindowEnd = "07:00",
                sampleIntervalMs = 5000,
                feedbackStatus = "PENDING",
                calibrated = false,
                sensorFailure = false,
                batteryWarning = false,
                processedByAi = true,
                status = "COMPLETED"
            )
        )

        val userResponse = restTemplate.exchange(
            "http://localhost:$port/api/users/$userId",
            HttpMethod.PUT,
            HttpEntity(user),
            Void::class.java
        )
        val sessionResponse = restTemplate.exchange(
            "http://localhost:$port/api/sessions/$sessionId",
            HttpMethod.PUT,
            HttpEntity(session),
            Void::class.java
        )
        val snapshot = restTemplate.getForObject(
            "http://localhost:$port/api/users/$userId/snapshot",
            Map::class.java
        )

        assertThat(userResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(sessionResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(snapshot?.get("user")).isNotNull
        assertThat(snapshot?.get("sessions").toString()).contains(sessionId)
    }
}
