package com.example.salaautomatizada.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.salaautomatizada.AppViewModel

/** Tela principal: lista de salas + botão Liga/Desliga fixo na parte inferior. */
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

    // Avisos comuns viram snackbar.
    LaunchedEffect(message) {
        val m = message
        if (m != null && !m.blocking) {
            snackbar.showSnackbar(m.text)
            vm.consumeMessage(m)
        }
    }

    // Aviso de sala ocupada vira diálogo, para não passar despercebido.
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
        bottomBar = { PowerBar(vm) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNew,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Nova configuração") }
            )
        }
    ) { padding ->
        HomeTab(
            vm = vm,
            padding = padding,
            email = email,
            onEdit = onEdit,
            onLogout = onLogout
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
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(56.dp)
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
