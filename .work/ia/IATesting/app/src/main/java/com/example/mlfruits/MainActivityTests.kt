package com.example.mlfruits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SleepTesterScreen()
                }
            }
        }
    }
}

@Composable
fun SleepTesterScreen() {
    val context = LocalContext.current

    // Las 4 variables físicas
    var actigrafia by remember { mutableStateOf("150.0") }
    var zcm by remember { mutableStateOf("10") }
    var vmMean by remember { mutableStateOf("9.8") } // 9.8 suele ser la gravedad normal
    var vmStd by remember { mutableStateOf("0.5") }

    var resultadoTexto by remember { mutableStateOf("Esperando datos...") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "🧪 Test de IA (Sueño)", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = actigrafia,
            onValueChange = { actigrafia = it },
            label = { Text("Actigrafía") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = zcm,
            onValueChange = { zcm = it },
            label = { Text("ZCM (Cruces por cero)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = vmMean,
            onValueChange = { vmMean = it },
            label = { Text("VM Media (Gravedad)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = vmStd,
            onValueChange = { vmStd = it },
            label = { Text("VM Desviación (Estabilidad)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                try {
                    val classifier = SleepClassifier(context)

                    val result = classifier.classify(
                        actigrafia = actigrafia.toFloatOrNull() ?: 0f,
                        zcm = zcm.toIntOrNull() ?: 0,
                        vmMean = vmMean.toFloatOrNull() ?: 0f,
                        vmStd = vmStd.toFloatOrNull() ?: 0f
                    )

                    val porcentaje = (result.confianza * 100).toInt()
                    resultadoTexto = "Fase: ${result.faseNombre}\nSeguridad: $porcentaje%"

                    classifier.close()

                } catch (e: Exception) {
                    resultadoTexto = "❌ Error: ${e.message}"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analizar Sueño")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = resultadoTexto,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}