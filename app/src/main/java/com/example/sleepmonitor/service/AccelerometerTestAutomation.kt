package com.example.sleepmonitor.service

import kotlin.math.sin

data class SyntheticSensorReading(
    val x: Float,
    val y: Float,
    val z: Float,
    val noiseDecibels: Float
)

enum class AccelerometerTestProfile(
    val wireValue: String,
    val label: String,
    val sampleCount: Int
) {
    CalmNight("calm_night", "Noche tranquila", 8),
    RestlessNight("restless_night", "Noche inquieta", 10),
    SmartWake("smart_wake", "Despertar suave", 10);

    companion object {
        fun fromWireValue(value: String?): AccelerometerTestProfile? =
            entries.firstOrNull { it.wireValue == value }
    }
}

class AccelerometerTestAutomation(
    private val profile: AccelerometerTestProfile
) {
    private var emittedSamples = 0

    fun nextReading(): SyntheticSensorReading {
        val index = emittedSamples++
        return when (profile) {
            AccelerometerTestProfile.CalmNight -> calmNight(index)
            AccelerometerTestProfile.RestlessNight -> restlessNight(index)
            AccelerometerTestProfile.SmartWake -> smartWake(index)
        }
    }

    fun isCompleted(): Boolean = emittedSamples >= profile.sampleCount

    fun profileLabel(): String = profile.label

    private fun calmNight(index: Int): SyntheticSensorReading {
        val drift = 0.08f * sin(index.toFloat())
        return SyntheticSensorReading(
            x = 0.03f + drift,
            y = -0.04f + drift / 2f,
            z = 9.81f + 0.05f * sin(index / 2f),
            noiseDecibels = 18f + (index % 3)
        )
    }

    private fun restlessNight(index: Int): SyntheticSensorReading {
        val spike = if (index % 3 == 0) 1.6f else 0.45f
        val noise = if (index % 2 == 0) 34f else 46f
        return SyntheticSensorReading(
            x = spike,
            y = spike / 1.5f,
            z = 9.81f + spike / 2f,
            noiseDecibels = noise
        )
    }

    private fun smartWake(index: Int): SyntheticSensorReading {
        val wakeBoost = if (index >= profile.sampleCount - 3) 1.25f else 0.18f
        return SyntheticSensorReading(
            x = wakeBoost,
            y = wakeBoost / 2f,
            z = 9.81f + wakeBoost / 1.8f,
            noiseDecibels = if (index >= profile.sampleCount - 2) 38f else 22f + index
        )
    }
}
