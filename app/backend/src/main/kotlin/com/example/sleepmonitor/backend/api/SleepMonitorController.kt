package com.example.sleepmonitor.backend.api

import com.example.sleepmonitor.backend.service.SleepMonitorSyncService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
class UserController(
    private val syncService: SleepMonitorSyncService
) {

    @GetMapping("/{userId}")
    @Operation(summary = "Obtiene un usuario por id")
    fun getUser(@PathVariable userId: String): RemoteUserDto =
        syncService.getUser(userId)

    @GetMapping("/{userId}/snapshot")
    @Operation(summary = "Obtiene el snapshot remoto de un usuario")
    fun getUserSnapshot(@PathVariable userId: String): RemoteUserSnapshotDto =
        syncService.getUserSnapshot(userId)

    @GetMapping("/{userId}/sessions")
    @Operation(summary = "Lista las sesiones de un usuario")
    fun getUserSessions(@PathVariable userId: String): List<RemoteSessionBundleDto> =
        syncService.getUserSessions(userId)

    @GetMapping("/{userId}/recommendations")
    @Operation(summary = "Lista recomendaciones de un usuario")
    fun getUserRecommendations(@PathVariable userId: String): List<RemoteRecommendationDto> =
        syncService.getUserRecommendations(userId)

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Crea o actualiza un usuario")
    fun upsertUser(
        @PathVariable userId: String,
        @RequestBody user: RemoteUserDto
    ) {
        syncService.upsertUser(userId, user)
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un usuario y sus sesiones asociadas")
    fun deleteUser(@PathVariable userId: String) {
        syncService.deleteUser(userId)
    }
}

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions")
class SessionController(
    private val syncService: SleepMonitorSyncService
) {

    @GetMapping("/{sessionId}")
    @Operation(summary = "Obtiene una sesion completa")
    fun getSession(@PathVariable sessionId: String): RemoteSessionBundleDto =
        syncService.getSession(sessionId)

    @GetMapping("/{sessionId}/recommendations")
    @Operation(summary = "Lista recomendaciones de una sesion")
    fun getSessionRecommendations(@PathVariable sessionId: String): List<RemoteRecommendationDto> =
        syncService.getSessionRecommendations(sessionId)

    @PutMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Crea o actualiza una sesion completa")
    fun upsertSession(
        @PathVariable sessionId: String,
        @RequestBody session: RemoteSessionBundleDto
    ) {
        syncService.upsertSession(sessionId, session)
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una sesion y sus datos dependientes")
    fun deleteSession(@PathVariable sessionId: String) {
        syncService.deleteSession(sessionId)
    }
}

@RestController
@RequestMapping("/api")
class HealthController {
    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf("status" to "ok")
}
