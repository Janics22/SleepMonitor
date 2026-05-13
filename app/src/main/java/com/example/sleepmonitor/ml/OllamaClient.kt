package com.example.sleepmonitor.ml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object OllamaClient {
    private const val BASE_URL = "http://10.0.2.2:11434"
    private const val USE_MOCK = false

    //See if provided "modelToSearch" name is on the Ollama client
    //Indirectly checks if it has connection
    suspend fun checkModel(modelToSearch: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (USE_MOCK) {
            return@withContext Result.success(true)
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL/api/tags")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("Error HTTP: ${connection.responseCode}"))
            }

            val response = connection.inputStream
                .bufferedReader()
                .use(BufferedReader::readText)

            val json = JSONObject(response)
            val modelsArray = json.optJSONArray("models") ?: JSONArray()

            var found = false
            for (i in 0 until modelsArray.length()) {
                val obj = modelsArray.getJSONObject(i)
                val modelName = obj.optString("name", "")
                if (modelName == modelToSearch) {
                    found = true
                    break
                }
            }

            Result.success(found)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    //Provide modelName, if possible check before with is checkModel() that exists
    //And provide the prompt, can only be a String
    //Maybe later on i try to mix this two funcionts in one alone, for now i pref to leave it like this
    const val CONNECT_TIMEOUT = 15000
    const val READ_TIMEOUT = 60000

    suspend fun askModel(model: String, prompt: String): String = withContext(Dispatchers.IO) {
        if (USE_MOCK) {
            return@withContext "[MOCK][$model] Respuesta simulada para: \"$prompt\""
        }

        try {
            val url = URL("$BASE_URL/api/generate")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            val body = JSONObject().apply {
                put("model", model)
                put("prompt", prompt)
                put("stream", false)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream
                .bufferedReader()
                .use(BufferedReader::readText)

            val json = JSONObject(response)
            json.optString("response", "No response from model")
        } catch (e: Exception) {
            "Error connecting to Ollama: ${e.message}"
        }
    }
}