package com.example.sleepmonitor.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.observeAsState(ProfileState.Loading)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(20.dp)
    ) {
        when (val current = state) {
            ProfileState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is ProfileState.Ready -> {
                ProfileContent(
                    state = current,
                    onSave = viewModel::saveProfile,
                    onDeleteAccount = onDeleteAccount,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileState.Ready,
    onSave: (String, Int?, Int?, String?, String?, Long?) -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    val user = state.user
    var username by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user?.userId) {
        username = user?.username.orEmpty()
        peso = user?.peso?.toString().orEmpty()
        altura = user?.altura?.toString().orEmpty()
        sexo = user?.sexo.orEmpty()
        pais = user?.pais.orEmpty()
        fechaNacimiento = user?.fechaNacimiento.toBirthDateText()
        localError = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Perfil y seguridad", style = MaterialTheme.typography.headlineLarge)

        ProfileCard {
            Text("Cuenta", style = MaterialTheme.typography.titleLarge)
            Text(
                user?.email ?: "Sin correo disponible",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "El correo se gestiona desde Firebase Authentication cuando Firebase esta configurado.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ProfileCard {
            Text("Datos personales", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it.filter(Char::isDigit) },
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = altura,
                    onValueChange = { altura = it.filter(Char::isDigit) },
                    label = { Text("Altura (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = sexo,
                onValueChange = { sexo = it },
                label = { Text("Sexo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pais,
                onValueChange = { pais = it },
                label = { Text("Pais") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha nacimiento (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val feedback = localError ?: state.error ?: state.message
            if (!feedback.isNullOrBlank()) {
                Text(
                    text = feedback,
                    color = if (localError != null || state.error != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            }

            Button(
                onClick = {
                    val birthDate = parseBirthDate(fechaNacimiento)
                    if (birthDate == null && fechaNacimiento.isNotBlank()) {
                        localError = "Usa el formato YYYY-MM-DD para la fecha"
                        return@Button
                    }
                    localError = null
                    onSave(
                        username,
                        peso.toIntOrNull(),
                        altura.toIntOrNull(),
                        sexo.takeIf { it.isNotBlank() },
                        pais.takeIf { it.isNotBlank() },
                        birthDate
                    )
                },
                enabled = !state.isSaving && user != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Guardar cambios")
                }
            }
        }

        ProfileCard {
            Text("Privacidad y precision", style = MaterialTheme.typography.titleLarge)
            Text("El audio no sale del dispositivo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Las sesiones se sincronizan con Firebase si esta configurado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Las fases de sueno son una estimacion basada en ruido, movimiento y modelo ML local.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        ProfileCard {
            Text("Acciones de cuenta", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(
                onClick = onDeleteAccount,
                enabled = user != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar cuenta")
            }
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesion")
            }
        }
    }
}

@Composable
private fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

private fun Long?.toBirthDateText(): String {
    if (this == null) return ""
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}

private fun parseBirthDate(value: String): Long? {
    if (value.isBlank()) return null
    return try {
        LocalDate.parse(value.trim())
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
}
