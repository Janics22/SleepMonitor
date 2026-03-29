package com.example.sleepmonitor.domain.sleep

import com.example.sleepmonitor.data.local.entities.SensorSampleEntity
import kotlin.math.pow
import kotlin.math.sqrt

data class SleepWindowFeatures(
    val start: Long,
    val end: Long,
    val actigraphy: Float,
    val zcm: Int,
    val vmMean: Float,
    val vmStd: Float
)

object SleepSignalFeatures {
    private const val minWindowSize = 4

    fun buildWindows(
        samples: List<SensorSampleEntity>,
        preferredWindowSize: Int = 6
    ): List<SleepWindowFeatures> {
        if (samples.size < minWindowSize) return emptyList()
        val windowSize = preferredWindowSize.coerceAtLeast(minWindowSize)
        return samples.chunked(windowSize)
            .filter { it.size >= minWindowSize }
            .map { chunk ->
                val values = chunk.map { it.movementMagnitude }
                val mean = values.average().toFloat()
                val variance = values.map { (it - mean).pow(2) }.average().toFloat()
                SleepWindowFeatures(
                    start = chunk.first().timestamp,
                    end = chunk.last().timestamp,
                    actigraphy = values.sumOf { kotlin.math.abs(it).toDouble() }.toFloat(),
                    zcm = zeroCrossings(values),
                    vmMean = mean,
                    vmStd = sqrt(variance)
                )
            }
    }

    fun normalizePhaseLabel(raw: String): String = when (raw.trim().lowercase()) {
        "despierto", "awake" -> "AWAKE"
        "ligero", "light" -> "LIGHT"
        "profundo", "deep" -> "DEEP"
        "rem" -> "REM"
        else -> "LIGHT"
    }

    private fun zeroCrossings(values: List<Float>): Int {
        if (values.size < 2) return 0
        val mean = values.average().toFloat()
        val centered = values.map { it - mean }
        return centered.zipWithNext().count { (previous, next) ->
            (previous <= 0f && next > 0f) || (previous >= 0f && next < 0f)
        }
    }
}
