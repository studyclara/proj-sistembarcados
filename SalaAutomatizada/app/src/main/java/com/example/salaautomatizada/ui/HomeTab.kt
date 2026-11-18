package com.example.salaautomatizada.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.salaautomatizada.AppViewModel
import com.example.salaautomatizada.data.RoomConfig
import com.example.salaautomatizada.ui.theme.status

@Composable
fun HomeTab(
    vm: AppViewModel,
    padding: PaddingValues,
    email: String?,
    onEdit: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val configs by vm.configs.collectAsState()
    val activeId by vm.activeId.collectAsState()
    val activeConfig by vm.activeConfig.collectAsState()
    val powered by vm.powered.collectAsState()
    val occupied by vm.occupied.collectAsState()
    var toDelete by remember { mutableStateOf<RoomConfig?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = padding.calculateTopPadding() + 16.dp,
            bottom = padding.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(bottom = 4.dp)) {
                    Text(
                        "Minhas salas",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Toque em uma sala para torná-la a ativa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AccountMenu(email = email, onLogout = onLogout)
            }
        }

        if (occupied) {
            item { OccupiedBanner() }
        }

        item {
            SystemStatusCard(
                powered = powered,
                occupied = occupied,
                activeName = activeConfig?.name,
                onTogglePresence = { vm.setSimulatedPresence(!occupied) }
            )
        }

        if (configs.isEmpty()) {
            item { EmptyState() }
        } else {
            items(configs, key = { it.id }) { config ->
                ConfigCard(
                    config = config,
                    selected = config.id == activeId,
                    powered = powered,
                    onSelect = { vm.selectConfig(config.id) },
                    onEdit = { onEdit(config.id) },
                    onDelete = { toDelete = config }
                )
            }
        }
    }

    toDelete?.let { config ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Excluir configuração") },
            text = { Text("Deseja realmente excluir \"${config.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = { vm.delete(config); toDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

/** Ícone da conta no cabeçalho: mostra o e-mail e a opção de sair. */
@Composable
private fun AccountMenu(email: String?, onLogout: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Rounded.AccountCircle,
                contentDescription = "Conta",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Text(
                email ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            DropdownMenuItem(
                text = { Text("Sair da conta") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                onClick = {
                    expanded = false
                    onLogout()
                }
            )
        }
    }
}

/** Aviso fixo no topo da Home enquanto houver alguém na sala. */
@Composable
private fun OccupiedBanner() {
    val status = MaterialTheme.status
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(status.warningContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Rounded.Person, contentDescription = null, tint = status.onWarningContainer)
        Column(Modifier.weight(1f)) {
            Text(
                "Sala ocupada",
                style = MaterialTheme.typography.titleMedium,
                color = status.onWarningContainer
            )
            Text(
                "Há outro usuário na sala. Ligar, desligar e trocar a sala ativa ficam bloqueados até ela ficar vazia.",
                style = MaterialTheme.typography.bodySmall,
                color = status.onWarningContainer
            )
        }
    }
}

@Composable
private fun SystemStatusCard(
    powered: Boolean,
    occupied: Boolean,
    activeName: String?,
    onTogglePresence: () -> Unit
) {
    val status = MaterialTheme.status
    // Toque longo no selo alterna a presença simulada (só tem efeito na simulação).
    val pillModifier = Modifier.pointerInput(occupied) {
        detectTapGestures(onLongPress = { onTogglePresence() })
    }
    AppCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (powered) status.successContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.PowerSettingsNew,
                    contentDescription = null,
                    tint = if (powered) status.success else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (powered) "Sistema ligado" else "Sistema desligado",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    when {
                        activeName == null -> "Nenhuma sala selecionada"
                        powered -> "$activeName em operação"
                        else -> "Selecionada: $activeName"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            if (occupied) {
                StatusPill(
                    text = "Ocupada",
                    icon = Icons.Rounded.Person,
                    container = status.warningContainer,
                    content = status.onWarningContainer,
                    modifier = pillModifier
                )
            } else {
                StatusPill(
                    text = "Vazia",
                    icon = Icons.Rounded.MeetingRoom,
                    container = status.successContainer,
                    content = status.onSuccessContainer,
                    modifier = pillModifier
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.MeetingRoom,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("Nenhuma configuração ainda", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Toque em \"Nova configuração\" para criar a primeira sala.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConfigCard(
    config: RoomConfig,
    selected: Boolean,
    powered: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val status = MaterialTheme.status
    val live = selected && powered

    val borderColor = when {
        live -> status.success
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    AppCard(
        onClick = onSelect,
        borderColor = borderColor,
        borderWidth = if (selected) 2.dp else 1.dp
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (live) status.successContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        roomTypeIcon(config.roomType),
                        contentDescription = null,
                        tint = if (live) status.onSuccessContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        config.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${config.roomType} • Senha: ${config.roomPassword}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                when {
                    live -> StatusPill(
                        text = "Ativa",
                        icon = Icons.Rounded.FiberManualRecord,
                        container = status.successContainer,
                        content = status.onSuccessContainer
                    )
                    selected -> StatusPill(
                        text = "Selecionada",
                        icon = Icons.Rounded.CheckCircle,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        content = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            ConfigSummary(config)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Editar")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Excluir")
                }
            }
        }
    }
}
