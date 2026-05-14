package com.example.sleepmonitor

import com.example.sleepmonitor.service.AccelerometerTestAutomation
import com.example.sleepmonitor.service.AccelerometerTestProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccelerometerTestAutomationTest {

    @Test
    fun calmNightCompletesAfterExpectedSamples() {
        val automation = AccelerometerTestAutomation(AccelerometerTestProfile.CalmNight)

        repeat(AccelerometerTestProfile.CalmNight.sampleCount) {
            automation.nextReading()
        }

        assertTrue(automation.isCompleted())
    }

    @Test
    fun restlessNightProducesHigherNoiseAndMovement() {
        val automation = AccelerometerTestAutomation(AccelerometerTestProfile.RestlessNight)

        val firstReading = automation.nextReading()

        assertTrue(firstReading.noiseDecibels >= 30f)
        assertTrue(firstReading.x > 0.4f)
        assertFalse(automation.isCompleted())
    }

    @Test
    fun parsesWireValueToProfile() {
        assertEquals(
            AccelerometerTestProfile.SmartWake,
            AccelerometerTestProfile.fromWireValue("smart_wake")
        )
        assertEquals(null, AccelerometerTestProfile.fromWireValue("unknown"))
    }
}
