package com.example.salaautomatizada.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.salaautomatizada.AppViewModel
import com.example.salaautomatizada.data.RoomConfig
import kotlin.math.roundToInt

private val colorPresets = listOf(
    Color(0xFFFFE0B2), // branco quente
    Color(0xFFE3F2FD), // branco frio
    Color(0xFFFFEB3B), // amarelo
    Color(0xFF2196F3), // azul
    Color(0xFF4CAF50), // verde
    Color(0xFFF44336), // vermelho
    Color(0xFF9C27B0)  // roxo
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(vm: AppViewModel, configId: Long, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var roomType by remember { mutableStateOf(roomTypes.first()) }
    var lightsOn by remember { mutableStateOf(true) }
    var brightness by remember { mutableFloatStateOf(80f) }
    var color by remember { mutableIntStateOf(colorPresets.first().toArgb()) }
    var hue by remember { mutableFloatStateOf(200f) }
    var curtainsOpen by remember { mutableStateOf(true) }
    var acOn by remember { mutableStateOf(false) }
    var temperature by remember { mutableFloatStateOf(23f) }
    var roomPassword by remember { mutableStateOf("1234") }
    var passwordError by remember { mutableStateOf(false) }

    // Modo edição: carrega a configuração existente.
    LaunchedEffect(configId) {
        if (configId > 0) {
            vm.getConfig(configId)?.let { c ->
                name = c.name
                roomType = c.roomType
                lightsOn = c.lightsOn
                brightness = c.brightness.toFloat()
                color = c.lightColor
                curtainsOpen = c.curtainsOpen
                acOn = c.acOn
                temperature = c.temperature.toFloat()
                roomPassword = c.roomPassword
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (configId > 0) "Editar configuração" else "Nova configuração") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                        } else if (roomPassword.isBlank()) {
                            passwordError = true
                        } else {
                            vm.save(
                                RoomConfig(
                                    id = if (configId > 0) configId else 0,
                                    userEmail = "",
                                    name = name.trim(),
                                    roomType = roomType,
                                    lightsOn = lightsOn,
                                    brightness = brightness.roundToInt(),
                                    lightColor = color,
                                    curtainsOpen = curtainsOpen,
                                    acOn = acOn,
                                    temperature = temperature.roundToInt(),
                                    roomPassword = roomPassword.trim()
                                ),
                                onDone = onBack
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(54.dp)
                ) {
                    Text("Salvar configuração", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identificação
            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("Identificação")
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            if (it.length <= 30) name = it
                            nameError = false
                        },
                        label = { Text("Nome da configuração") },
                        singleLine = true,
                        isError = nameError,
                        shape = MaterialTheme.shapes.medium,
                        supportingText = { if (nameError) Text("Informe um nome para salvar.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = roomPassword,
                        onValueChange = {
                            if (it.length <= 8) {
                                roomPassword = it.uppercase()
                                passwordError = false
                            }
                        },
                        label = { Text("Senha de acesso (Teclado físico)") },
                        placeholder = { Text("Ex: 1234 ou A1B2") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        singleLine = true,
                        isError = passwordError,
                        shape = MaterialTheme.shapes.medium,
                        supportingText = {
                            if (passwordError) Text("Informe a senha de acesso da sala.")
                            else Text("Senha digitada no teclado para abrir a sala")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Tipo de sala",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roomTypes.forEach { type ->
                            FilterChip(
                                selected = roomType == type,
                                onClick = { roomType = type },
                                label = { Text(type) },
                                leadingIcon = {
                                    Icon(roomTypeIcon(type), contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                }
            }

            // Luzes
            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SwitchRow(
                        icon = Icons.Rounded.Lightbulb,
                        title = "Luzes",
                        subtitle = if (lightsOn) "Ligadas" else "Desligadas",
                        checked = lightsOn,
                        onChange = { lightsOn = it }
                    )

                    LabeledValue("Intensidade", "${brightness.roundToInt()}%")
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        valueRange = 0f..100f,
                        enabled = lightsOn
                    )

                    Text(
                        "Coloração",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorPresets.forEach { preset ->
                            val selected = color == preset.toArgb()
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(preset)
                                    .border(
                                        width = if (selected) 3.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable(enabled = lightsOn) { color = preset.toArgb() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = "Selecionada",
                                        modifier = Modifier.size(20.dp),
                                        tint = if (preset.luminance() > 0.5f) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        "Outra cor",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = hue,
                        onValueChange = {
                            hue = it
                            color = Color.hsv(it, 0.85f, 1f).toArgb()
                        },
                        valueRange = 0f..360f,
                        enabled = lightsOn
                    )

                    // Pré-visualização
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2B3040))
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Color(color).copy(
                                        alpha = if (lightsOn) 0.15f + brightness / 100f * 0.85f else 0f
                                    )
                                )
                        )
                        Text(
                            if (lightsOn) "Pré-visualização da iluminação" else "Luzes desligadas",
                            color = if (lightsOn && Color(color).luminance() > 0.5f && brightness > 50f)
                                Color.Black else Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Cortinas
            AppCard {
                Column(Modifier.padding(16.dp)) {
                    SwitchRow(
                        icon = if (curtainsOpen) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay,
                        title = "Cortinas",
                        subtitle = if (curtainsOpen) "Abertas" else "Fechadas",
                        checked = curtainsOpen,
                        onChange = { curtainsOpen = it }
                    )
                }
            }

            // Ar-condicionado
            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SwitchRow(
                        icon = Icons.Rounded.AcUnit,
                        title = "Ar-condicionado",
                        subtitle = if (acOn) "Ligado" else "Desligado",
                        checked = acOn,
                        onChange = { acOn = it }
                    )
                    Text(
                        "${temperature.roundToInt()}°C",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (acOn) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 16f..30f,
                        steps = 13,
                        enabled = acOn
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "16°C",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Temperatura ideal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "30°C",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}
