package com.example.sleepmonitor

import com.example.sleepmonitor.data.local.entities.SensorSampleEntity
import com.example.sleepmonitor.domain.sleep.SleepSignalFeatures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepSignalFeaturesTest {

    @Test
    fun buildsFeatureWindowsFromSamples() {
        val samples = List(6) { index ->
            SensorSampleEntity(
                sessionId = "session",
                timestamp = 1_000L * index,
                accelerometerX = 0f,
                accelerometerY = 0f,
                accelerometerZ = 0f,
                movementMagnitude = if (index % 2 == 0) 0.4f else -0.2f,
                noiseDecibels = 20f
            )
        }

        val windows = SleepSignalFeatures.buildWindows(samples)

        assertEquals(1, windows.size)
        assertTrue(windows.first().actigraphy > 0f)
        assertTrue(windows.first().zcm > 0)
    }

    @Test
    fun normalizesSpanishLabels() {
        assertEquals("AWAKE", SleepSignalFeatures.normalizePhaseLabel("Despierto"))
        assertEquals("LIGHT", SleepSignalFeatures.normalizePhaseLabel("Ligero"))
        assertEquals("DEEP", SleepSignalFeatures.normalizePhaseLabel("Profundo"))
        assertEquals("REM", SleepSignalFeatures.normalizePhaseLabel("REM"))
    }
}
