package com.example.salaautomatizada.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.salaautomatizada.AppViewModel

/** Tela principal com abas: Salas, Monitoramento IoT e Relatórios. */
@Composable
fun MainScreen(
    vm: AppViewModel,
    onNew: () -> Unit,
    onEdit: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val message by vm.message.collectAsState()
    val email by vm.email.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) } // 0: Salas, 1: Monitoramento, 2: Relatórios
    var showCloudConfigDialog by remember { mutableStateOf(false) }

    // Avisos comuns viram snackbar.
    LaunchedEffect(message) {
        val m = message
        if (m != null && !m.blocking) {
            snackbar.showSnackbar(m.text)
            vm.consumeMessage(m)
        }
    }

    // Aviso de sala ocupada vira diálogo.
    message?.takeIf { it.blocking }?.let { m ->
        AlertDialog(
            onDismissRequest = { vm.consumeMessage(m) },
            icon = { Icon(Icons.Rounded.Person, contentDescription = null) },
            title = { Text("Sala ocupada") },
            text = { Text(m.text) },
            confirmButton = {
                TextButton(onClick = { vm.consumeMessage(m) }) { Text("Entendi") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Column {
                if (currentTab == 0) {
                    PowerBar(vm)
                }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Rounded.MeetingRoom, contentDescription = "Salas") },
                        label = { Text("Salas") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Rounded.Sensors, contentDescription = "Monitoramento") },
                        label = { Text("Monitoramento") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(Icons.Rounded.Assessment, contentDescription = "Relatórios") },
                        label = { Text("Relatórios") }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = onNew,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Nova configuração") }
                )
            }
        }
    ) { padding ->
        when (currentTab) {
            0 -> HomeTab(
                vm = vm,
                padding = padding,
                email = email,
                onEdit = onEdit,
                onLogout = onLogout
            )
            1 -> MonitoringTab(
                vm = vm,
                padding = padding,
                onOpenCloudConfig = { showCloudConfigDialog = true }
            )
            2 -> ReportsTab(
                vm = vm,
                padding = padding
            )
        }
    }

    if (showCloudConfigDialog) {
        var tokenInput by remember { mutableStateOf(vm.getCloudToken()) }

        AlertDialog(
            onDismissRequest = { showCloudConfigDialog = false },
            title = { Text("Configurar Nuvem TagoIO") },
            text = {
                Column {
                    Text(
                        "Insira o Device Token gerado no seu dispositivo na plataforma TagoIO (ou deixe vazio para demonstração local):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Device Token (TagoIO)") },
                        singleLine = true,
                        placeholder = { Text("Ex: 12345678-abcd-ef01-2345-6789abcdef01") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "O token conecta o app diretamente aos dados do ESP32 na nuvem TagoIO.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.setCloudToken(tokenInput)
                        showCloudConfigDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudConfigDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/** Barra inferior com o botão de ligar/desligar a sala. */
@Composable
private fun PowerBar(vm: AppViewModel) {
    val powered by vm.powered.collectAsState()
    val busy by vm.busy.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = { vm.togglePower() },
            shape = MaterialTheme.shapes.medium,
            colors = if (powered) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            } else {
                ButtonDefaults.buttonColors()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .height(52.dp)
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = if (powered) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    Icons.Rounded.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (powered) "Desligar sala" else "Ligar sala",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
