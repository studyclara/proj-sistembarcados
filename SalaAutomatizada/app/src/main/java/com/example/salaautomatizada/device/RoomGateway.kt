package com.example.salaautomatizada.device

import android.content.Context
import android.util.Log
import com.example.salaautomatizada.data.RoomConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Ponte entre o app e o microcontrolador da sala.
 *
 * Hoje o app usa [SimulatedRoomGateway] (nada é enviado de verdade). Para integrar o
 * hardware real, crie outra classe que implemente esta interface e troque a linha
 * `private val gateway: RoomGateway = ...` em AppViewModel.kt. Exemplos de transporte:
 *  - HTTP para um ESP32 na rede local (ex.: POST http://<ip>/config com [toDeviceJson]);
 *  - MQTT (publicar em um tópico e assinar o tópico do sensor de presença);
 *  - Bluetooth Low Energy.
 * Se usar rede, lembre de adicionar a permissão INTERNET no AndroidManifest.xml.
 */
interface RoomGateway {
    /** `true` quando o sensor de presença indica que há alguém na sala. */
    val occupied: StateFlow<Boolean>

    /** Liga a automação da sala e aplica os ajustes da configuração. */
    suspend fun apply(config: RoomConfig): Result<Unit>

    /** Desliga a automação da sala. */
    suspend fun shutdown(): Result<Unit>
}

/** Formato JSON sugerido para enviar os ajustes ao microcontrolador. */
fun RoomConfig.toDeviceJson(): String = JSONObject()
    .put("power", true)
    .put("room_type", roomType)
    .put(
        "lights",
        JSONObject()
            .put("on", lightsOn)
            .put("brightness", brightness)
            .put("color", "#%06X".format(lightColor and 0xFFFFFF))
    )
    .put("curtains", if (curtainsOpen) "open" else "closed")
    .put("ac", JSONObject().put("on", acOn).put("temperature", temperature))
    .toString()

/**
 * Versão de demonstração: espera um pouco (como se falasse com o hardware), registra o
 * JSON no Logcat (tag "RoomGateway") e deixa a presença ser alterada pela tela Liga/Desliga.
 */
class SimulatedRoomGateway(context: Context) : RoomGateway {

    private val prefs = context.applicationContext
        .getSharedPreferences("sala_gateway", Context.MODE_PRIVATE)

    private val _occupied = MutableStateFlow(prefs.getBoolean("occupied", false))
    override val occupied: StateFlow<Boolean> = _occupied.asStateFlow()

    /** Só existe na simulação: no hardware real quem decide é o sensor. */
    fun setOccupied(value: Boolean) {
        prefs.edit().putBoolean("occupied", value).apply()
        _occupied.value = value
    }

    override suspend fun apply(config: RoomConfig): Result<Unit> {
        delay(800)
        Log.d(TAG, "apply -> ${config.toDeviceJson()}")
        return Result.success(Unit)
    }

    override suspend fun shutdown(): Result<Unit> {
        delay(600)
        Log.d(TAG, "shutdown -> {\"power\":false}")
        return Result.success(Unit)
    }

    private companion object {
        const val TAG = "RoomGateway"
    }
}
