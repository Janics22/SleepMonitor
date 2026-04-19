package com.example.sleepmonitor.domain.sleep

import android.content.Context
import com.example.sleepmonitor.data.local.entities.SensorSampleEntity

object HybridSleepInsightsEngine {
    const val version = RuleBasedSleepInsightsEngine.version

    fun analyze(
        context: Context,
        sessionId: String,
        samples: List<SensorSampleEntity>,
        startedAt: Long,
        endedAt: Long
    ): RuleBasedSleepInsightsEngine.AnalysisResult {
        // The TensorFlow Lite JNI runtime bundled with the app was only 4 KB ELF aligned.
        // To keep the release artifact 16 KB compatible, we publish the rule-based engine.
        return RuleBasedSleepInsightsEngine.analyze(sessionId, samples, startedAt, endedAt)
    }
}
