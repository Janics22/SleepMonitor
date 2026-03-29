package com.example.sleepmonitor.domain.sleep

import android.content.Context
import com.example.sleepmonitor.data.local.entities.PhaseEntity
import com.example.sleepmonitor.data.local.entities.SensorSampleEntity
import com.example.sleepmonitor.data.local.entities.SensorSummaryEntity
import com.example.sleepmonitor.ml.SleepAiClassifier
import com.example.sleepmonitor.ui.utils.IdUtils
import com.example.sleepmonitor.ui.utils.TimeUtils
import kotlin.math.max

object HybridSleepInsightsEngine {
    const val version = "hybrid-tflite-v1"

    fun analyze(
        context: Context,
        sessionId: String,
        samples: List<SensorSampleEntity>,
        startedAt: Long,
        endedAt: Long
    ): RuleBasedSleepInsightsEngine.AnalysisResult {
        if (samples.size < 4) {
            return RuleBasedSleepInsightsEngine.analyze(sessionId, samples, startedAt, endedAt)
        }

        val windows = SleepSignalFeatures.buildWindows(samples)
        if (windows.isEmpty()) {
            return RuleBasedSleepInsightsEngine.analyze(sessionId, samples, startedAt, endedAt)
        }

        return runCatching {
            val predictions = mutableListOf<PredictedWindow>()
            SleepAiClassifier(context).use { classifier ->
                windows.forEach { window ->
                    val prediction = classifier.classify(
                        actigraphy = window.actigraphy,
                        zcm = window.zcm,
                        vmMean = window.vmMean,
                        vmStd = window.vmStd
                    )
                    predictions += PredictedWindow(
                        start = window.start,
                        end = window.end,
                        phase = SleepSignalFeatures.normalizePhaseLabel(prediction.label),
                        confidence = prediction.confidence
                    )
                }
            }

            val phases = mergePhases(sessionId, predictions, endedAt)
            val summary = buildSummary(sessionId, samples, startedAt, endedAt)
            val aiScore = computeScore(phases, summary, predictions.map { it.confidence }.average().toFloat())
            val dominantPhase = predictions.groupingBy { it.phase }.eachCount().maxByOrNull { it.value }?.key

            RuleBasedSleepInsightsEngine.AnalysisResult(
                phases = phases,
                summary = summary,
                aiScore = aiScore,
                recommendations = buildRecommendations(summary, aiScore, dominantPhase),
                sensorFailure = false,
                engineVersion = version,
                usedAi = true
            )
        }.getOrElse {
            RuleBasedSleepInsightsEngine.analyze(sessionId, samples, startedAt, endedAt)
        }
    }

    private fun buildSummary(
        sessionId: String,
        samples: List<SensorSampleEntity>,
        startedAt: Long,
        endedAt: Long
    ): SensorSummaryEntity {
        val movementEvents = samples.count { it.movementMagnitude > 0.10f }
        val noiseEvents = samples.count { it.noiseDecibels > 44f }
        return SensorSummaryEntity(
            sessionId = sessionId,
            avgMovement = samples.map { it.movementMagnitude }.average().toFloat(),
            maxMovement = samples.maxOf { it.movementMagnitude },
            avgNoiseDb = samples.map { it.noiseDecibels }.average().toFloat(),
            maxNoiseDb = samples.maxOf { it.noiseDecibels },
            noiseEvents = noiseEvents,
            movementEvents = movementEvents,
            estimatedSleepMinutes = TimeUtils.minutesBetween(startedAt, endedAt),
            totalSamples = samples.size
        )
    }

    private fun mergePhases(
        sessionId: String,
        predictions: List<PredictedWindow>,
        endedAt: Long
    ): List<PhaseEntity> {
        if (predictions.isEmpty()) return emptyList()

        val phases = mutableListOf<PhaseEntity>()
        var currentPhase = predictions.first().phase
        var phaseStart = predictions.first().start
        var phaseEnd = predictions.first().end

        predictions.drop(1).forEach { prediction ->
            if (prediction.phase == currentPhase) {
                phaseEnd = prediction.end
            } else {
                phases += phaseEntity(sessionId, currentPhase, phaseStart, phaseEnd)
                currentPhase = prediction.phase
                phaseStart = prediction.start
                phaseEnd = prediction.end
            }
        }

        phases += phaseEntity(sessionId, currentPhase, phaseStart, max(phaseEnd, endedAt))
        return phases
    }

    private fun phaseEntity(
        sessionId: String,
        type: String,
        start: Long,
        end: Long
    ): PhaseEntity {
        return PhaseEntity(
            phaseId = IdUtils.newId(),
            sessionId = sessionId,
            type = type,
            start = start,
            end = end,
            durationSeconds = max(1, ((end - start) / 1000L).toInt())
        )
    }

    private fun computeScore(
        phases: List<PhaseEntity>,
        summary: SensorSummaryEntity,
        meanConfidence: Float
    ): Int {
        val totalSeconds = phases.sumOf { it.durationSeconds }.coerceAtLeast(1)
        val deepRatio = phases.filter { it.type == "DEEP" }.sumOf { it.durationSeconds }.toFloat() / totalSeconds
        val remRatio = phases.filter { it.type == "REM" }.sumOf { it.durationSeconds }.toFloat() / totalSeconds
        val awakeRatio = phases.filter { it.type == "AWAKE" }.sumOf { it.durationSeconds }.toFloat() / totalSeconds

        val durationBonus = when {
            summary.estimatedSleepMinutes in 420..540 -> 12
            summary.estimatedSleepMinutes in 360..419 -> 8
            summary.estimatedSleepMinutes in 300..359 -> 4
            else -> 0
        }
        val score = 48 +
            (deepRatio * 28).toInt() +
            (remRatio * 18).toInt() +
            ((1f - awakeRatio) * 16).toInt() +
            (meanConfidence * 10f).toInt() +
            durationBonus -
            (summary.noiseEvents * 2) -
            summary.movementEvents

        return score.coerceIn(0, 100)
    }

    private fun buildRecommendations(
        summary: SensorSummaryEntity,
        aiScore: Int,
        dominantPhase: String?
    ): List<RuleBasedSleepInsightsEngine.RecommendationDraft> {
        val items = mutableListOf<RuleBasedSleepInsightsEngine.RecommendationDraft>()

        if (summary.noiseEvents >= 3 || summary.avgNoiseDb >= 40f) {
            items += RuleBasedSleepInsightsEngine.RecommendationDraft(
                title = "Reduce el ruido nocturno",
                description = "La IA detecto interrupciones compatibles con un entorno ruidoso. Prueba una habitacion mas silenciosa o deja el movil mas lejos de la fuente de sonido."
            )
        }

        if (summary.movementEvents >= 6) {
            items += RuleBasedSleepInsightsEngine.RecommendationDraft(
                title = "Mejora la estabilidad del registro",
                description = "Hubo bastante movimiento. Deja el telefono cargando y fijo para que la clasificacion de fases sea mas consistente."
            )
        }

        if (dominantPhase == "LIGHT" || dominantPhase == "AWAKE") {
            items += RuleBasedSleepInsightsEngine.RecommendationDraft(
                title = "Favorece fases mas profundas",
                description = "Predominaron fases ligeras. Reduce pantallas y cafeina en la ultima hora antes de dormir para facilitar un descanso mas profundo."
            )
        }

        if (items.isEmpty()) {
            items += RuleBasedSleepInsightsEngine.RecommendationDraft(
                title = if (aiScore >= 75) "Mantienes un patron estable" else "Ajusta habitos suaves",
                description = if (aiScore >= 75) {
                    "El modelo detecto una noche bastante estable. Repetir la rutina y mantener horarios regulares ayudara a comparar tendencias."
                } else {
                    "La noche fue util para el modelo, pero aun hay margen de mejora. Repite varias sesiones para obtener recomendaciones mas fiables."
                }
            )
        }

        return items.take(3)
    }

    private data class PredictedWindow(
        val start: Long,
        val end: Long,
        val phase: String,
        val confidence: Float
    )
}
