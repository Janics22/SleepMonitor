package com.example.mlfruits

import android.content.Context
import android.content.res.AssetFileDescriptor
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SleepClassifier(
    context: Context,
    private val modelName: String = "model.tflite",
    labelsFileName: String = "labelsIO.txt"
){

    /**
     * @param vmSequence: Los 1740 valores de la Magnitud Vectorial.
     * @param stats: Array con [ZCM, Actigrafía, Peso, Edad, Sexo].
     */
    private val interpreter: Interpreter
    private val labels: List<String>
    init {
        /**
         * Al construir la clase:
         * - se carga el modelo desde assets
         * - se cargan las etiquetas
         *
         * Esto se hace una sola vez para no penalizar rendimiento
         * cada vez que clasificamos una imagen.
         */
        interpreter = Interpreter(loadModelFile(context, modelName))
        labels = loadLabels(context, labelsFileName)
    }

    private fun loadLabels(context: Context, fileName: String): List<String> {
        return context.assets.open(fileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readLines()
                .filter { it.isNotBlank() }
        }
    }

    /**
     * Función principal que predice la fase de sueño.
     * Recibe solo los 5 números clave (Opción B).
     */
    fun classify(
        actigrafia: Float,
        zcm: Int,
        vmMean: Float,
        vmStd: Float
    ): SleepResult {

        // 1. Preparamos la entrada: un array de 5 posiciones [1, 5]
        val input = arrayOf(
            floatArrayOf(actigrafia, zcm.toFloat(),vmMean, vmStd)
        )

        // 2. Preparamos la salida: donde la IA escribirá las 4 probabilidades [1, 4]
        val output = Array(1) { FloatArray(4) }

        // 3. ¡Ejecutamos la IA!
        interpreter.run(input, output)

        // 4. Buscamos cuál de los 4 números es el más grande (el que tiene más confianza)
        val probabilidades = output[0]
        val maxIndex = probabilidades.indices.maxByOrNull { probabilidades[it] } ?: 0

        return SleepResult(
            faseNombre = labels[maxIndex],
            confianza = probabilidades[maxIndex]
        )
    }

    /**
     * Función técnica para leer el archivo .tflite sin errores.
     */
    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun close() {
        interpreter.close()
    }
}
/**
 * Clase auxiliar para recibir el resultado de forma limpia
 */
data class SleepResult(
    val faseNombre: String,
    val confianza: Float,
)