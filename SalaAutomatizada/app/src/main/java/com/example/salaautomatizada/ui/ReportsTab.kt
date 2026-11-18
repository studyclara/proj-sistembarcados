package com.example.salaautomatizada.ui

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.salaautomatizada.AppViewModel
import com.example.salaautomatizada.data.RoomEventLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsTab(
    vm: AppViewModel,
    padding: PaddingValues
) {
    val context = LocalContext.current
    val eventLogs by vm.eventLogs.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    // Cálculo das métricas
    val totalEventos = eventLogs.size
    val totalAcessos = eventLogs.count { it.eventType == "ENTRADA" || it.eventType == "POWER" }
    val acionamentosAC = eventLogs.count { it.acState }
    val tempMedia = if (eventLogs.isNotEmpty()) {
        eventLogs.map { it.temperature }.average()
    } else 22.0

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
                        "Relatórios & Histórico",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Métricas de acesso e climatização da sala",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { shareReport(context, eventLogs, totalAcessos, acionamentosAC, tempMedia) },
                        enabled = eventLogs.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Exportar Relatório",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showClearDialog = true },
                        enabled = eventLogs.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Limpar Histórico",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Grade de Resumo Estatístico (KPIs)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Acessos",
                    value = totalAcessos.toString(),
                    subtitle = "Registros",
                    accentColor = Color(0xFF1976D2)
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Ar Condicionado",
                    value = acionamentosAC.toString(),
                    subtitle = "Ciclos Ativos",
                    accentColor = Color(0xFF0097A7)
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Temp. Média",
                    value = String.format(Locale.getDefault(), "%.1f°C", tempMedia),
                    subtitle = "Climatização",
                    accentColor = Color(0xFF388E3C)
                )
            }
        }

        // Botão Exportar Relatório em Destaque
        item {
            Button(
                onClick = { shareReport(context, eventLogs, totalAcessos, acionamentosAC, tempMedia) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Assessment, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar Relatório Completo")
            }
        }

        // Cabeçalho da Lista de Eventos
        item {
            Text(
                "Linha do Tempo de Eventos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (eventLogs.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Nenhum evento registrado ainda.",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Os acessos pelo teclado do ESP32 e comandos do app aparecerão aqui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(eventLogs, key = { it.id }) { log ->
                EventLogItem(log = log)
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpar Histórico") },
            text = { Text("Deseja realmente apagar todos os registros de relatório da sala?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.clearEventLogs()
                        showClearDialog = false
                    }
                ) {
                    Text("Limpar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EventLogItem(log: RoomEventLog) {
    val (icon, color) = when (log.eventType) {
        "ENTRADA" -> Icons.Rounded.Login to Color(0xFF2E7D32)
        "SAIDA" -> Icons.Rounded.ExitToApp to Color(0xFFC62828)
        "ALERTA" -> Icons.Rounded.Warning to Color(0xFFD84315)
        "POWER" -> Icons.Rounded.PowerSettingsNew to Color(0xFF0277BD)
        else -> Icons.Rounded.AcUnit to Color(0xFF00838F)
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(log.timestamp))

    AppCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.userName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (log.acState) Color(0xFFE0F7FA) else Color(0xFFEEEEEE)
                    ) {
                        Text(
                            text = if (log.acState) "AC Ligado" else "AC Desligado",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (log.acState) Color(0xFF006064) else Color(0xFF616161)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f °C", log.temperature),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }
        }
    }
}

private fun shareReport(
    context: Context,
    logs: List<RoomEventLog>,
    totalAcessos: Int,
    acionamentosAC: Int,
    tempMedia: Double
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val sb = StringBuilder()
    sb.append("=== RELATÓRIO DE MONITORAMENTO IOT - SALA AUTOMATIZADA ===\n")
    sb.append("Gerado em: ${dateFormat.format(Date())}\n\n")
    sb.append("--- RESUMO ESTATÍSTICO ---\n")
    sb.append("Total de Eventos Registrados: ${logs.size}\n")
    sb.append("Total de Acessos: $totalAcessos\n")
    sb.append("Ciclos de Ar Condicionado: $acionamentosAC\n")
    sb.append(String.format(Locale.getDefault(), "Temperatura Média: %.1f °C\n\n", tempMedia))

    sb.append("--- HISTÓRICO DETALHADO ---\n")
    if (logs.isEmpty()) {
        sb.append("Nenhum evento registrado.\n")
    } else {
        logs.forEach { log ->
            sb.append("[${dateFormat.format(Date(log.timestamp))}] ")
            sb.append("${log.userName}: ${log.description} | ")
            sb.append("AC: ${if (log.acState) "LIGADO" else "DESLIGADO"} | ")
            sb.append(String.format(Locale.getDefault(), "Temp: %.1f °C\n", log.temperature))
        }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Relatório de Monitoramento IoT - Sala Automatizada")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
}
