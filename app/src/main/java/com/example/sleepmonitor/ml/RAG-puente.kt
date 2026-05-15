package com.example.sleepmonitor.ml

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


private const val URL_CONSEJO = "http://10.0.2.2:8000/obtener_consejo"

fun pedirConsejoAlServidor(total: Double, rem: Double, profundo: Double, ligero: Double): String {
    val client = OkHttpClient()

    // El JSON que le enviamos al servidor
    val json = """
        {
            "total": $total,
            "rem": $rem,
            "profundo": $profundo,
            "ligero": $ligero
        }
    """.trimIndent()

    val body = json.toRequestBody("application/json".toMediaTypeOrNull())

    // Si pruebas en el mismo PC, usa "localhost".
    // Si pruebas desde un móvil real, usa la IP de tu PC.
    val request = Request.Builder()
        .url(URL_CONSEJO)
        .post(body)
        .build()

    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return "Error del servidor"
            val resBody = response.body?.string() ?: ""
            JSONObject(resBody).getString("consejo")
        }
    } catch (e: Exception) {
        "No se pudo conectar con el servidor de IA: ${e.message}"
    }
}