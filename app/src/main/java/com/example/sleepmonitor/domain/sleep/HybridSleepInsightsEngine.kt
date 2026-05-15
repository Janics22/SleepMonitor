package com.example.sleepmonitor.domain.sleep

import android.content.Context
import com.example.sleepmonitor.data.local.entities.PhaseEntity
import com.example.sleepmonitor.data.local.entities.SensorSampleEntity
import com.example.sleepmonitor.data.local.entities.SensorSummaryEntity
import com.example.sleepmonitor.ml.SleepAiClassifier
import com.example.sleepmonitor.ml.pedirConsejoAlServidor
import com.example.sleepmonitor.ui.utils.IdUtils
import com.example.sleepmonitor.ui.utils.TimeUtils
import kotlin.math.max

object HybridSleepInsightsEngine {
    const val version = "hybrid-tflite-v1"
    const val precisionNotice =
        "La clasificacion principal se realiza en el dispositivo con TensorFlow Lite a partir de ventanas de movimiento y ruido. Si la inferencia falla, la app usa un respaldo local por reglas."

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
            val phaseDurations = PhaseDurationAccumulator()
            SleepAiClassifier(context).use { classifier ->
                windows.forEach { window ->
                    val prediction = classifier.classify(
                        actigraphy = window.actigraphy,
                        zcm = window.zcm,
                        vmMean = window.vmMean,
                        vmStd = window.vmStd
                    )
                    val phase = SleepSignalFeatures.normalizePhaseLabel(prediction.label)
                    phaseDurations.add(phase, window.durationSeconds())
                    predictions += PredictedWindow(
                        start = window.start,
                        end = window.end,
                        phase = phase,
                        confidence = prediction.confidence.coerceIn(0f, 1f)
                    )
                }
            }

            val phases = mergePhases(sessionId, predictions, endedAt)
            val summary = buildSummary(sessionId, samples, startedAt, endedAt)
            val meanConfidence = predictions.map { it.confidence }.average().toFloat()
            val aiScore = computeScore(phases, summary, meanConfidence)
            val dominantPhase = predictions.groupingBy { it.phase }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key

            RuleBasedSleepInsightsEngine.AnalysisResult(
                phases = phases,
                summary = summary,
                aiScore = aiScore,
                recommendations = buildRecommendations(
                    summary = summary,
                    aiScore = aiScore,
                    dominantPhase = dominantPhase,
                    phaseDurations = phaseDurations.toTotals()
                ),
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
        dominantPhase: String?,
        phaseDurations: PhaseDurationTotals
    ): List<RuleBasedSleepInsightsEngine.RecommendationDraft> {
        val items = mutableListOf<RuleBasedSleepInsightsEngine.RecommendationDraft>()
        buildRagRecommendation(phaseDurations)?.let(items::add)

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

        return items.take(4)
    }

    private fun buildRagRecommendation(
        phaseDurations: PhaseDurationTotals
    ): RuleBasedSleepInsightsEngine.RecommendationDraft? {
        if (phaseDurations.totalSeconds <= 0) return null

        val advice = runCatching {
            pedirConsejoAlServidor(
                total = phaseDurations.totalHours,
                rem = phaseDurations.remHours,
                profundo = phaseDurations.deepHours,
                ligero = phaseDurations.lightHours
            )
        }.getOrNull()?.trim().orEmpty()

        if (advice.isBlank()) return null

        val isServerError = advice.startsWith("No se pudo", ignoreCase = true) ||
            advice.startsWith("Error", ignoreCase = true)

        return RuleBasedSleepInsightsEngine.RecommendationDraft(
            title = if (isServerError) "Consejo RAG no disponible" else "Consejo personalizado IA",
            description = if (isServerError) {
                "$advice. Arranca el servidor RAG/Ollama para recibir el consejo generativo con los tiempos por fase."
            } else {
                advice
            }
        )
    }

    private fun SleepWindowFeatures.durationSeconds(): Int {
        return max(1, ((end - start) / 1000L).toInt())
    }

    private data class PredictedWindow(
        val start: Long,
        val end: Long,
        val phase: String,
        val confidence: Float
    )

    private class PhaseDurationAccumulator {
        private var remSeconds: Int = 0
        private var lightSeconds: Int = 0
        private var deepSeconds: Int = 0

        fun add(phase: String, durationSeconds: Int) {
            val safeDuration = durationSeconds.coerceAtLeast(1)
            when (phase) {
                "REM" -> remSeconds += safeDuration
                "LIGHT" -> lightSeconds += safeDuration
                "DEEP" -> deepSeconds += safeDuration
            }
        }

        fun toTotals(): PhaseDurationTotals = PhaseDurationTotals(
            remSeconds = remSeconds,
            lightSeconds = lightSeconds,
            deepSeconds = deepSeconds
        )
    }

    private data class PhaseDurationTotals(
        val remSeconds: Int,
        val lightSeconds: Int,
        val deepSeconds: Int
    ) {
        val totalSeconds: Int
            get() = remSeconds + lightSeconds + deepSeconds

        val totalHours: Double
            get() = totalSeconds.toHours()

        val remHours: Double
            get() = remSeconds.toHours()

        val lightHours: Double
            get() = lightSeconds.toHours()

        val deepHours: Double
            get() = deepSeconds.toHours()

        private fun Int.toHours(): Double = this / 3600.0
    }
}
