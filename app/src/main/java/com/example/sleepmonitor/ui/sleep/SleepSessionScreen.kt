package com.example.sleepmonitor.ui.sleep

import android.Manifest
import android.content.pm.ApplicationInfo
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sleepmonitor.data.local.entities.PhaseEntity
import com.example.sleepmonitor.data.repository.SleepSessionReport
import com.example.sleepmonitor.service.AccelerometerTestProfile
import com.example.sleepmonitor.service.SleepMonitorService
import com.example.sleepmonitor.ui.utils.TimeUtils

private data class PendingStart(
    val start: String,
    val end: String,
    val sampleIntervalMs: Long,
    val testAutomationProfile: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepSessionScreen(
    viewModel: SleepSessionViewModel,
    onGoBackHome: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.observeAsState(SleepSessionState.Initializing)
    val showTestAutomation = remember(context) {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    val startPickerState = rememberTimePickerState(initialHour = 7, initialMinute = 0, is24Hour = true)
    val endPickerState = rememberTimePickerState(initialHour = 7, initialMinute = 30, is24Hour = true)
    var sampleIntervalSeconds by remember { mutableFloatStateOf(10f) }
    var feedback by remember { mutableFloatStateOf(70f) }
    var pendingStart by remember { mutableStateOf<PendingStart?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            pendingStart?.let {
                viewModel.startSession(
                    context = context,
                    alarmWindowStart = it.start,
                    alarmWindowEnd = it.end,
                    sampleIntervalMs = it.sampleIntervalMs,
                    testAutomationProfile = it.testAutomationProfile
                )
                pendingStart = null
            }
        } else {
            Toast.makeText(
                context,
                "Necesitas permiso de microfono y notificaciones para una sesion completa.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(state) {
        when (val current = state) {
            SleepSessionState.FeedbackSaved -> onGoBackHome()
            is SleepSessionState.Error -> Toast.makeText(context, current.message, Toast.LENGTH_LONG).show()
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Sesion nocturna", style = MaterialTheme.typography.headlineLarge)

            when (val current = state) {
                SleepSessionState.Initializing -> {
                    Text("Recuperando estado de la sesion...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                is SleepSessionState.Ready -> {
                    if (current.showOnboarding) {
                        FeatureCard(
                            title = "Antes de empezar",
                            body = "Coloca el movil cargando, deja el microfono activo y ten en cuenta que las fases son una estimacion basada en movimiento y ruido."
                        )
                        TextButton(onClick = { viewModel.dismissOnboarding() }) {
                            Text("Entendido")
                        }
                    }

                    FeatureCard(
                        title = "Configura el despertar inteligente",
                        body = "La sesion se registrara con un Foreground Service y el reporte final quedara preparado incluso si la app se cierra."
                    )

                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Inicio de ventana", style = MaterialTheme.typography.titleMedium)
                            TimeInput(state = startPickerState)
                            Text("Fin de ventana", style = MaterialTheme.typography.titleMedium)
                            TimeInput(state = endPickerState)
                            Text(
                                "Intervalo de muestreo: ${sampleIntervalSeconds.toInt()} s",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Slider(
                                value = sampleIntervalSeconds,
                                onValueChange = {
                                    sampleIntervalSeconds = listOf(5f, 10f, 15f)
                                        .minBy { option -> kotlin.math.abs(option - it) }
                                },
                                valueRange = 5f..15f,
                                steps = 1
                            )
                            Button(
                                onClick = {
                                    requestSessionStart(
                                        permissionLauncher = permissionLauncher,
                                        onPendingStart = { pendingStart = it },
                                        start = TimeUtils.hhmm(startPickerState.hour, startPickerState.minute),
                                        end = TimeUtils.hhmm(endPickerState.hour, endPickerState.minute),
                                        sampleIntervalMs = sampleIntervalSeconds.toLong() * 1000L
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Empezar sesion de sueno")
                            }
                        }
                    }

                    if (showTestAutomation) {
                        Card(
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Automatizacion del acelerometro", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "Estas pruebas generan muestras sinteticas para validar el flujo completo sin depender del sensor fisico ni del microfono.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TestAutomationButton(
                                        modifier = Modifier.weight(1f),
                                        label = "Tranquila",
                                        onClick = {
                                            requestSessionStart(
                                                permissionLauncher = permissionLauncher,
                                                onPendingStart = { pendingStart = it },
                                                start = TimeUtils.hhmm(startPickerState.hour, startPickerState.minute),
                                                end = TimeUtils.hhmm(endPickerState.hour, endPickerState.minute),
                                                sampleIntervalMs = 1_000L,
                                                testAutomationProfile = AccelerometerTestProfile.CalmNight.wireValue
                                            )
                                        }
                                    )
                                    TestAutomationButton(
                                        modifier = Modifier.weight(1f),
                                        label = "Inquieta",
                                        onClick = {
                                            requestSessionStart(
                                                permissionLauncher = permissionLauncher,
                                                onPendingStart = { pendingStart = it },
                                                start = TimeUtils.hhmm(startPickerState.hour, startPickerState.minute),
                                                end = TimeUtils.hhmm(endPickerState.hour, endPickerState.minute),
                                                sampleIntervalMs = 1_000L,
                                                testAutomationProfile = AccelerometerTestProfile.RestlessNight.wireValue
                                            )
                                        }
                                    )
                                }
                                TestAutomationButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    label = "Despertar suave",
                                    onClick = {
                                        requestSessionStart(
                                            permissionLauncher = permissionLauncher,
                                            onPendingStart = { pendingStart = it },
                                            start = TimeUtils.hhmm(startPickerState.hour, startPickerState.minute),
                                            end = TimeUtils.hhmm(endPickerState.hour, endPickerState.minute),
                                            sampleIntervalMs = 1_000L,
                                            testAutomationProfile = AccelerometerTestProfile.SmartWake.wireValue
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                is SleepSessionState.Monitoring -> {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Sesion en marcha", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "Ventana objetivo: ${current.alarmWindowStart} - ${current.alarmWindowEnd}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "La captura sigue activa con el movil bloqueado para mantener la sesion toda la noche.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { viewModel.stopSession(context) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Detener y generar reporte")
                            }
                        }
                    }
                }

                is SleepSessionState.Report -> {
                    SleepReportContent(
                        report = current.report,
                        feedback = feedback,
                        onFeedbackChange = { feedback = it },
                        onSubmitFeedback = { viewModel.submitUserFeedback(feedback.toInt()) },
                        onSkip = {
                            viewModel.skipFeedback()
                            onGoBackHome()
                        }
                    )
                }

                SleepSessionState.FeedbackSaved -> {
                    Text("Guardando feedback...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                is SleepSessionState.Error -> {
                    OutlinedButton(
                        onClick = {
                            viewModel.resetToReady()
                            onGoBackHome()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver al inicio")
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepReportContent(
    report: SleepSessionReport,
    feedback: Float,
    onFeedbackChange: (Float) -> Unit,
    onSubmitFeedback: () -> Unit,
    onSkip: () -> Unit
) {
    val wakeMethod = when (report.session.wakeMethod) {
        "SMART_ALARM" -> "Despertador inteligente"
        "WINDOW_END" -> "Fin de ventana"
        "MANUAL_STOP" -> "Fin manual"
        SleepMonitorService.STOP_REASON_TEST_AUTOMATION -> "Prueba automatizada"
        else -> "Sesion finalizada"
    }

    ScoreHeroCard(
        score = report.session.aiScore ?: 0,
        headline = report.qualityHeadline,
        description = report.qualityDescription,
        wakeMethod = wakeMethod,
        durationMinutes = report.summary?.estimatedSleepMinutes,
        noiseEvents = report.summary?.noiseEvents,
        movementEvents = report.summary?.movementEvents,
        precisionNotice = report.precisionNotice
    )

    if (report.phases.isNotEmpty()) {
        PhaseBreakdownCard(phases = report.phases)
    }

    if (report.recommendations.isNotEmpty()) {
        report.recommendations.forEach { recommendation ->
            FeatureCard(
                title = recommendation.title,
                body = recommendation.description
            )
        }
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Tu valoracion", style = MaterialTheme.typography.titleLarge)
            Text("Como te has levantado hoy: ${feedback.toInt()}/100")
            Slider(
                value = feedback,
                onValueChange = onFeedbackChange,
                valueRange = 0f..100f
            )
            Button(onClick = onSubmitFeedback, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar feedback")
            }
            TextButton(onClick = onSkip) {
                Text("Omitir por ahora")
            }
        }
    }
}

@Composable
private fun ScoreHeroCard(
    score: Int,
    headline: String,
    description: String,
    wakeMethod: String,
    durationMinutes: Int?,
    noiseEvents: Int?,
    movementEvents: Int?,
    precisionNotice: String
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreGauge(
                    score = score,
                    modifier = Modifier.size(156.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SourcePill(label = scoreSourceLabel(score))
                    Text(headline, style = MaterialTheme.typography.headlineSmall)
                    Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Cierre de sesion: $wakeMethod",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                durationMinutes?.let {
                    MetricChip(
                        label = "Duracion",
                        value = TimeUtils.formatDurationMinutes(it),
                        modifier = Modifier.weight(1f)
                    )
                }
                noiseEvents?.let {
                    MetricChip(
                        label = "Ruido",
                        value = "$it eventos",
                        modifier = Modifier.weight(1f)
                    )
                }
                movementEvents?.let {
                    MetricChip(
                        label = "Movimiento",
                        value = "$it eventos",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                precisionNotice,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val safeScore = score.coerceIn(0, 100)
    val progress = safeScore / 100f
    val accent = when {
        safeScore >= 85 -> Color(0xFF8BE8D2)
        safeScore >= 70 -> Color(0xFFA7D8FF)
        safeScore >= 55 -> Color(0xFFF1C77A)
        else -> MaterialTheme.colorScheme.tertiary
    }
    val track = MaterialTheme.colorScheme.surfaceContainerLow

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = track,
                startAngle = 155f,
                sweepAngle = 230f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = accent,
                startAngle = 155f,
                sweepAngle = 230f * progress,
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                safeScore.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "/100",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SourcePill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF1DCD1))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = Color(0xFF231916),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun PhaseBreakdownCard(phases: List<PhaseEntity>) {
    val totals = phases
        .groupBy { it.type }
        .mapValues { entry -> entry.value.sumOf { it.durationSeconds } }
        .filterValues { it > 0 }
    val totalSeconds = totals.values.sum().coerceAtLeast(1)

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Fases detectadas", style = MaterialTheme.typography.titleLarge)
            listOf("REM", "LIGHT", "DEEP", "AWAKE").forEach { type ->
                val seconds = totals[type] ?: 0
                if (seconds > 0) {
                    PhaseBar(
                        label = TimeUtils.phaseLabel(type),
                        minutes = seconds / 60,
                        fraction = seconds.toFloat() / totalSeconds,
                        color = phaseColor(type)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhaseBar(
    label: String,
    minutes: Int,
    fraction: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(
                "$minutes min",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.04f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun phaseColor(type: String): Color = when (type) {
    "REM" -> Color(0xFF8BE8D2)
    "LIGHT" -> Color(0xFFA7D8FF)
    "DEEP" -> Color(0xFFC7A8FF)
    "AWAKE" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

private fun scoreSourceLabel(score: Int): String = when {
    score <= 0 -> "SIN DATOS"
    else -> "ANALISIS LOCAL"
}

@Composable
private fun FeatureCard(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TestAutomationButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(label)
    }
}

private fun requiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayOf(Manifest.permission.RECORD_AUDIO)
    }
}

private fun requestSessionStart(
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onPendingStart: (PendingStart) -> Unit,
    start: String,
    end: String,
    sampleIntervalMs: Long,
    testAutomationProfile: String? = null
) {
    onPendingStart(
        PendingStart(
            start = start,
            end = end,
            sampleIntervalMs = sampleIntervalMs,
            testAutomationProfile = testAutomationProfile
        )
    )
    permissionLauncher.launch(requiredPermissions())
}
