package com.example.sleepmonitor.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.Closeable
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class SleepAiPrediction(
    val label: String,
    val confidence: Float
)

class SleepAiClassifier(
    context: Context,
    private val modelName: String = "model.tflite",
    labelsFileName: String = "labelsIO.txt"
) : Closeable {

    private val interpreter = Interpreter(loadModelFile(context, modelName))
    private val labels = loadLabels(context, labelsFileName)

    fun classify(
        actigraphy: Float,
        zcm: Int,
        vmMean: Float,
        vmStd: Float
    ): SleepAiPrediction {
        val input = arrayOf(floatArrayOf(actigraphy, zcm.toFloat(), vmMean, vmStd))
        val output = Array(1) { FloatArray(labels.size.coerceAtLeast(4)) }
        interpreter.run(input, output)

        val probabilities = output[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        return SleepAiPrediction(
            label = labels.getOrElse(maxIndex) { "Ligero" },
            confidence = probabilities.getOrElse(maxIndex) { 0f }
        )
    }

    private fun loadLabels(context: Context, fileName: String): List<String> {
        return context.assets.open(fileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readLines()
                .map(String::trim)
                .filter(String::isNotEmpty)
        }
    }

    private fun loadModelFile(context: Context, fileName: String): MappedByteBuffer {
        val descriptor: AssetFileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(descriptor.fileDescriptor)
        val channel = inputStream.channel
        return channel.map(
            FileChannel.MapMode.READ_ONLY,
            descriptor.startOffset,
            descriptor.declaredLength
        )
    }

    override fun close() {
        interpreter.close()
    }
}
