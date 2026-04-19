package com.example.sleepmonitor.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val BirthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf<LocalDate?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val state by viewModel.state.observeAsState(RegisterState.Idle)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state) {
        if (state is RegisterState.Success) {
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
            onRegisterSuccess()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaNacimiento.toDatePickerMillisOrNull(),
            yearRange = 1900..LocalDate.now().year
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        fechaNacimiento = datePickerState.selectedDateMillis.toLocalDateOrNull()
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Fecha de nacimiento",
                        modifier = Modifier.padding(start = 24.dp, top = 20.dp, end = 24.dp)
                    )
                },
                headline = {
                    Text(
                        text = fechaNacimiento?.format(BirthDateFormatter) ?: "Selecciona una fecha",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                    )
                },
                showModeToggle = true
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Crear cuenta", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Hemos simplificado el registro para que completes lo importante primero y añadas el resto sin esfuerzo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            RegisterHeroCard()

            RegisterSectionCard(
                title = "Acceso",
                subtitle = "Estos datos son obligatorios para entrar en la app."
            ) {
                IconTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                IconTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Usuario",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null
                        )
                    }
                )

                PasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contrasena",
                    visible = showPassword,
                    onToggleVisibility = { showPassword = !showPassword }
                )

                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Repite la contrasena",
                    visible = showConfirmPassword,
                    onToggleVisibility = { showConfirmPassword = !showConfirmPassword }
                )
            }

            RegisterSectionCard(
                title = "Perfil opcional",
                subtitle = "Completar estos campos ayuda a personalizar reportes y futuras recomendaciones."
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconTextField(
                        value = peso,
                        onValueChange = { peso = it },
                        label = "Peso (kg)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    IconTextField(
                        value = altura,
                        onValueChange = { altura = it },
                        label = "Altura (cm)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                IconTextField(
                    value = sexo,
                    onValueChange = { sexo = it },
                    label = "Sexo"
                )

                IconTextField(
                    value = pais,
                    onValueChange = { pais = it },
                    label = "Pais"
                )

                BirthDateField(
                    value = fechaNacimiento?.format(BirthDateFormatter).orEmpty(),
                    onOpenPicker = {
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                        showDatePicker = true
                    }
                )
            }

            Button(
                onClick = {
                    viewModel.register(
                        email = email,
                        username = username,
                        password = password,
                        confirmPassword = confirmPassword,
                        peso = peso.toIntOrNull(),
                        altura = altura.toIntOrNull(),
                        sexo = sexo.takeIf { it.isNotBlank() },
                        pais = pais.takeIf { it.isNotBlank() },
                        fechaNacimiento = fechaNacimiento.toEpochMillisOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is RegisterState.Loading
            ) {
                Text("Completar registro")
            }

            when (val current = state) {
                RegisterState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is RegisterState.Error -> {
                    Text(current.message, color = MaterialTheme.colorScheme.error)
                }

                else -> Unit
            }

            TextButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                    onBackToLogin()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Volver al login")
            }
        }
    }
}

@Composable
private fun RegisterHeroCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Registro mas claro, menos friccion", style = MaterialTheme.typography.titleLarge)
            Text(
                "Primero crea tu acceso. Luego, si quieres, completa tu perfil con un par de datos utiles para que Sleep Monitor entienda mejor tu contexto.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "La fecha de nacimiento ahora se selecciona con calendario para evitar errores al escribirla.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun RegisterSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun IconTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    icon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
        leadingIcon = icon
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ocultar contrasena" else "Mostrar contrasena"
                )
            }
        }
    )
}

@Composable
private fun BirthDateField(
    value: String,
    onOpenPicker: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPicker),
        label = { Text("Fecha de nacimiento") },
        placeholder = { Text("Selecciona una fecha") },
        supportingText = { Text("Pulsa el icono del calendario para elegirla mas rapido.") },
        trailingIcon = {
            IconButton(onClick = onOpenPicker) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Abrir selector de fecha",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    )
}

private fun LocalDate?.toEpochMillisOrNull(): Long? {
    return this?.atStartOfDay(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
}

private fun LocalDate?.toDatePickerMillisOrNull(): Long? {
    return this?.atStartOfDay(ZoneOffset.UTC)
        ?.toInstant()
        ?.toEpochMilli()
}

private fun Long?.toLocalDateOrNull(): LocalDate? {
    return this?.let {
        Instant.ofEpochMilli(it)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
    }
}
