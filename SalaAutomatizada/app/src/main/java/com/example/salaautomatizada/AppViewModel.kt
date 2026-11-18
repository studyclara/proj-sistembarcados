package com.example.salaautomatizada

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.salaautomatizada.data.AppDatabase
import com.example.salaautomatizada.data.RoomConfig
import com.example.salaautomatizada.data.RoomEventLog
import com.example.salaautomatizada.data.User
import com.example.salaautomatizada.device.RoomGateway
import com.example.salaautomatizada.device.RoomTelemetry
import com.example.salaautomatizada.device.TagoIoRoomGateway
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest

/** Mensagem para a interface. [blocking] = aviso importante, mostrado em diálogo. */
data class UiMessage(val text: String, val blocking: Boolean = false)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val prefs = app.getSharedPreferences("sala_prefs", Context.MODE_PRIVATE)

    // Gateway IoT conectado à nuvem TagoIO
    val gateway: RoomGateway = TagoIoRoomGateway(app)

    val occupied: StateFlow<Boolean> = gateway.occupied
    val telemetry: StateFlow<RoomTelemetry> = gateway.telemetry

    val eventLogs: StateFlow<List<RoomEventLog>> = db.eventLogDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _email = MutableStateFlow(prefs.getString("email", null))
    val email: StateFlow<String?> = _email.asStateFlow()

    /** Sala escolhida pelo usuário (ativa quando o sistema está ligado). */
    private val _activeId = MutableStateFlow(prefs.getLong("active_id", -1L))
    val activeId: StateFlow<Long> = _activeId.asStateFlow()

    private val _powered = MutableStateFlow(prefs.getBoolean("powered", false))
    val powered: StateFlow<Boolean> = _powered.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<UiMessage?>(null)
    val message: StateFlow<UiMessage?> = _message.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val configs: StateFlow<List<RoomConfig>> = _email
        .flatMapLatest { e ->
            if (e == null) flowOf(emptyList()) else db.configDao().observe(e)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeConfig: StateFlow<RoomConfig?> = combine(configs, _activeId) { list, id ->
        list.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        // Tenta sincronizar com a nuvem na inicialização
        refreshTelemetry(silent = true)
    }

    // ---------------------------------------------------------------- mensagens

    fun say(text: String, blocking: Boolean = false) {
        _message.value = UiMessage(text, blocking)
    }

    fun consumeMessage(message: UiMessage) {
        if (_message.value == message) _message.value = null
    }

    private fun blockedByPresence() = say(
        "A sala não pode ser configurada agora porque há outro usuário presente. " +
            "Tente novamente quando ela estiver vazia.",
        blocking = true
    )

    // ---------------------------------------------------------------- conta

    private fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(("sala::$password").toByteArray())
            .joinToString("") { "%02x".format(it) }

    /** Retorna null em caso de sucesso, ou a mensagem de erro. */
    suspend fun login(email: String, password: String): String? {
        val e = email.trim().lowercase()
        val user = db.userDao().find(e) ?: return "Usuário não encontrado. Crie uma conta."
        if (user.passwordHash != hash(password)) return "Senha incorreta."
        startSession(e)
        return null
    }

    /** Retorna null em caso de sucesso, ou a mensagem de erro. */
    suspend fun register(email: String, password: String): String? {
        val e = email.trim().lowercase()
        if (db.userDao().find(e) != null) return "Este e-mail já está cadastrado."
        db.userDao().insert(User(e, hash(password)))
        startSession(e)
        return null
    }

    private fun startSession(email: String) {
        val previous = prefs.getString("last_email", null)
        val editor = prefs.edit().putString("email", email).putString("last_email", email)
        if (previous != email) {
            // Outro usuário assumiu o app: zera a sala ativa e o estado ligado.
            editor.putLong("active_id", -1L).putBoolean("powered", false)
            _activeId.value = -1L
            _powered.value = false
            if (previous != null) viewModelScope.launch { gateway.shutdown() }
        }
        editor.apply()
        _email.value = email
    }

    fun logout() {
        prefs.edit().remove("email").apply()
        _email.value = null
    }

    // ---------------------------------------------------------------- sala

    private fun setActive(id: Long) {
        prefs.edit().putLong("active_id", id).apply()
        _activeId.value = id
    }

    private fun setPowered(value: Boolean) {
        prefs.edit().putBoolean("powered", value).apply()
        _powered.value = value
    }

    private suspend fun <T> runBusy(block: suspend () -> T): T {
        _busy.value = true
        try {
            return block()
        } finally {
            _busy.value = false
        }
    }

    /** Permite simular a presença para testes sem hardware. */
    fun setSimulatedPresence(value: Boolean) {
        (gateway as? TagoIoRoomGateway)?.setSimulatedPresence(value)
        viewModelScope.launch {
            db.eventLogDao().insert(
                RoomEventLog(
                    eventType = if (value) "ENTRADA" else "SAIDA",
                    userName = if (value) "Aluno Simulado" else "Nenhum",
                    description = if (value) "Presença simulada ativada na sala." else "Sala desocupada (simulado).",
                    temperature = if (value) 22.0f else 26.5f,
                    acState = value
                )
            )
        }
    }

    /** Botão Liga/Desliga. Bloqueado se a sala estiver ocupada. */
    fun togglePower() {
        if (_busy.value) return
        viewModelScope.launch {
            if (gateway.occupied.value) {
                blockedByPresence()
                return@launch
            }
            if (_powered.value) {
                runBusy { gateway.shutdown() }
                    .onSuccess {
                        setPowered(false)
                        say("Sala desligada.")
                        db.eventLogDao().insert(
                            RoomEventLog(
                                eventType = "SAIDA",
                                userName = _email.value ?: "Usuário",
                                description = "Sala desligada pelo aplicativo. Ar condicionado desligado.",
                                temperature = 26.5f,
                                acState = false
                            )
                        )
                    }
                    .onFailure { say("Não foi possível desligar a sala. Tente novamente.") }
            } else {
                val config = activeConfig.value
                if (config == null) {
                    say("Selecione uma configuração na Home antes de ligar a sala.")
                    return@launch
                }
                runBusy { gateway.apply(config) }
                    .onSuccess {
                        setPowered(true)
                        say("Sala ligada com “${config.name}”.")
                        db.eventLogDao().insert(
                            RoomEventLog(
                                eventType = "POWER",
                                userName = _email.value ?: "Usuário",
                                description = "Sala ligada com “${config.name}”. Ar Condicionado: ${if (config.acOn) "LIGADO" else "DESLIGADO"} (${config.temperature}°C).",
                                temperature = config.temperature.toFloat(),
                                acState = config.acOn
                            )
                        )
                    }
                    .onFailure { say("Não foi possível ligar a sala. Tente novamente.") }
            }
        }
    }

    /** Sincroniza dados com a nuvem TagoIO. */
    fun refreshTelemetry(silent: Boolean = false) {
        viewModelScope.launch {
            runBusy {
                gateway.syncFromCloud()
                    .onSuccess { t ->
                        if (!silent) {
                            if (t.isOnline) say("Dados da nuvem IoT atualizados!")
                            else say("Nuvem IoT offline. Exibindo telemetria local.")
                        }
                    }
                    .onFailure {
                        if (!silent) say("Não foi possível sincronizar com a TagoIO. Verifique a conexão.")
                    }
            }
        }
    }

    fun getCloudToken(): String = gateway.getCloudToken()

    fun setCloudToken(token: String) {
        gateway.setCloudToken(token)
        say("Token salvo! Conectando à nuvem...")
        refreshTelemetry(silent = false)
    }

    fun clearEventLogs() {
        viewModelScope.launch {
            db.eventLogDao().clearAll()
            say("Relatórios limpos com sucesso.")
        }
    }

    fun selectConfig(id: Long) {
        if (id == _activeId.value || _busy.value) return
        viewModelScope.launch {
            val config = configs.value.firstOrNull { it.id == id } ?: return@launch
            if (_powered.value) {
                if (gateway.occupied.value) {
                    blockedByPresence()
                    return@launch
                }
                runBusy { gateway.apply(config) }
                    .onSuccess {
                        setActive(id)
                        say("“${config.name}” agora é a sala ativa.")
                    }
                    .onFailure { say("Não foi possível aplicar “${config.name}” na sala.") }
            } else {
                setActive(id)
                say("“${config.name}” selecionada. Ligue o sistema para ativá-la.")
            }
        }
    }

    suspend fun getConfig(id: Long): RoomConfig? = db.configDao().get(id)

    fun save(config: RoomConfig, onDone: () -> Unit) {
        viewModelScope.launch {
            val owner = _email.value ?: return@launch
            val rowId = db.configDao().upsert(config.copy(userEmail = owner))
            val saved = config.copy(id = if (config.id > 0) config.id else rowId, userEmail = owner)

            if (_activeId.value == -1L) setActive(saved.id)

            when {
                saved.id == _activeId.value && _powered.value -> {
                    if (gateway.occupied.value) {
                        say(
                            "Configuração salva, mas a sala está ocupada. Desligue e ligue a sala " +
                                "quando ela estiver vazia para aplicar as mudanças."
                        )
                    } else {
                        gateway.apply(saved)
                            .onSuccess { say("Configuração salva e aplicada na sala.") }
                            .onFailure { say("Configuração salva, mas não foi possível aplicá-la na sala.") }
                    }
                }
                else -> say("Configuração salva.")
            }
            onDone()
        }
    }

    fun delete(config: RoomConfig) {
        viewModelScope.launch {
            if (config.id == _activeId.value && _powered.value) {
                if (gateway.occupied.value) {
                    blockedByPresence()
                    return@launch
                }
                val result = runBusy { gateway.shutdown() }
                if (result.isFailure) {
                    say("Não foi possível desligar a sala. A configuração não foi excluída.")
                    return@launch
                }
                setPowered(false)
            }
            db.configDao().delete(config)
            if (_activeId.value == config.id) setActive(-1L)
            say("“${config.name}” excluída.")
        }
    }
}
