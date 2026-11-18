package com.example.salaautomatizada.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeviceThermostat
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.salaautomatizada.AppViewModel
import com.example.salaautomatizada.device.RoomTelemetry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MonitoringTab(
    vm: AppViewModel,
    padding: PaddingValues,
    onOpenCloudConfig: () -> Unit
) {
    val telemetry by vm.telemetry.collectAsState()
    val busy by vm.busy.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = padding.calculateTopPadding() + 16.dp,
            bottom = padding.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Monitoramento IoT",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Telemetria em tempo real da sala",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onOpenCloudConfig) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = "Configurar Token IoT",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { vm.refreshTelemetry() },
                        enabled = !busy
                    ) {
                        if (busy) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Atualizar dados",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Status da Conexão com a Nuvem
        item {
            CloudStatusBanner(telemetry = telemetry, onOpenCloudConfig = onOpenCloudConfig)
        }

        // Card do Ar Condicionado e Temperatura
        item {
            AirConditionerCard(telemetry = telemetry)
        }

        // Card de Presença e Usuário Atual
        item {
            PresenceCard(telemetry = telemetry)
        }

        // Card de Cortinas e Iluminação
        item {
            RoomEnvironmentCard(telemetry = telemetry)
        }

        // Card de Alertas & Segurança
        item {
            SecurityAlertCard(telemetry = telemetry)
        }
    }
}

@Composable
private fun CloudStatusBanner(
    telemetry: RoomTelemetry,
    onOpenCloudConfig: () -> Unit
) {
    val isOnline = telemetry.isOnline
    val containerColor = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
    val contentColor = if (isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isOnline) "Nuvem TagoIO Conectada" else "Modo de Demonstração / Offline",
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = if (isOnline)
                        "Última sincronização: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(telemetry.timestamp))}"
                    else
                        "Configure o Device Token da TagoIO para sincronização real",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }
            if (!isOnline) {
                OutlinedButton(
                    onClick = onOpenCloudConfig,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Configurar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun AirConditionerCard(telemetry: RoomTelemetry) {
    val acOn = telemetry.acOn
    val cardColor = if (acOn) Color(0xFFE1F5FE) else MaterialTheme.colorScheme.surfaceVariant
    val accentColor = if (acOn) Color(0xFF0288D1) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardColor
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (acOn) Color(0xFF03A9F4) else Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AcUnit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Ar Condicionado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (acOn) "LIGADO (LED Azul Pino 27 Ativo)" else "DESLIGADO (LED Apagado)",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (acOn) Color(0xFF0288D1) else Color.Gray
                ) {
                    Text(
                        text = if (acOn) "ATIVO" else "STANDBY",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Medidor de temperatura
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DeviceThermostat,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Temperatura da Sala",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f °C", telemetry.temperature),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (telemetry.temperature > 25) Color(0xFFE65100) else Color(0xFF0277BD)
                        )
                    }
                }

                Text(
                    text = if (acOn) "Conforto Térmico Ideal" else "Temperatura Ambiente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PresenceCard(telemetry: RoomTelemetry) {
    val occupied = telemetry.occupied
    val statusColor = if (occupied) Color(0xFF4CAF50) else Color(0xFF757575)

    AppCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ocupação da Sala",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (occupied) Color(0xFFE8F5E9) else Color(0xFFEEEEEE)
                ) {
                    Text(
                        text = if (occupied) "OCUPADA" else "LIVRE",
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (occupied) "Usuário presente: ${telemetry.currentUser}" else "Nenhum usuário detectado",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Identificado via Teclado Matricial do ESP32",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RoomEnvironmentCard(telemetry: RoomTelemetry) {
    AppCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Atuadores & Ambiente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.MeetingRoom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Cortina", fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = telemetry.curtains,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Iluminação NeoPixel", fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = telemetry.lightColor,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityAlertCard(telemetry: RoomTelemetry) {
    val isAlert = telemetry.lastAlert.contains("Invasao", ignoreCase = true) ||
            telemetry.lastAlert.contains("Incorreta", ignoreCase = true)
    val alertColor = if (isAlert) Color(0xFFD32F2F) else Color(0xFF388E3C)
    val bgAlert = if (isAlert) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgAlert
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = alertColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Sistema de Alertas (SMS e E-mail)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
                Text(
                    text = telemetry.lastAlert,
                    style = MaterialTheme.typography.bodyMedium,
                    color = alertColor.copy(alpha = 0.9f)
                )
                Text(
                    text = "Gatilhos de SMS e E-mail ativos na plataforma IoT",
                    style = MaterialTheme.typography.labelSmall,
                    color = alertColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
