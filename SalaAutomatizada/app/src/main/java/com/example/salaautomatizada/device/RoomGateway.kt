package com.example.salaautomatizada.device

import android.content.Context
import android.util.Log
import com.example.salaautomatizada.data.RoomConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Modelo de dados de telemetria recebido em tempo real da plataforma IoT (TagoIO / ESP32).
 */
data class RoomTelemetry(
    val occupied: Boolean = false,
    val currentUser: String = "Nenhum",
    val curtains: String = "Fechada",
    val acOn: Boolean = false,
    val temperature: Float = 24.5f,
    val lightColor: String = "Desligado",
    val lastAlert: String = "Sistema Pronto",
    val timestamp: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false
)

/**
 * Ponte entre o aplicativo mobile e o microcontrolador ESP32 através da Plataforma IoT na Nuvem.
 */
interface RoomGateway {
    /** `true` quando há alguém presente na sala. */
    val occupied: StateFlow<Boolean>

    /** Telemetria completa em tempo real da sala na nuvem. */
    val telemetry: StateFlow<RoomTelemetry>

    /** Liga a automação da sala e envia a configuração para a nuvem/ESP32. */
    suspend fun apply(config: RoomConfig): Result<Unit>

    /** Desliga a automação da sala. */
    suspend fun shutdown(): Result<Unit>

    /** Sincroniza os dados mais recentes diretamente da plataforma IoT. */
    suspend fun syncFromCloud(): Result<RoomTelemetry>

    /** Obtém o token configurado da nuvem. */
    fun getCloudToken(): String

    /** Atualiza o token do dispositivo na nuvem IoT. */
    fun setCloudToken(token: String)
}

/** Formato JSON para envio aos tópicos/variáveis da nuvem. */
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
 * Implementação do Gateway IoT conectado à nuvem TagoIO através de API REST / HTTPS.
 * Sincroniza em tempo real com o ESP32 (físico ou simulado no Wokwi).
 */
class TagoIoRoomGateway(context: Context) : RoomGateway {

    private val prefs = context.applicationContext
        .getSharedPreferences("sala_tago_prefs", Context.MODE_PRIVATE)

    private val _telemetry = MutableStateFlow(
        RoomTelemetry(
            occupied = prefs.getBoolean("occupied", false),
            currentUser = prefs.getString("currentUser", "Nenhum") ?: "Nenhum",
            curtains = prefs.getString("curtains", "Fechada") ?: "Fechada",
            acOn = prefs.getBoolean("acOn", false),
            temperature = prefs.getFloat("temperature", 24.5f),
            lightColor = prefs.getString("lightColor", "Desligado") ?: "Desligado",
            lastAlert = prefs.getString("lastAlert", "Sistema Pronto") ?: "Sistema Pronto",
            isOnline = false
        )
    )
    override val telemetry: StateFlow<RoomTelemetry> = _telemetry.asStateFlow()

    private val _occupied = MutableStateFlow(prefs.getBoolean("occupied", false))
    override val occupied: StateFlow<Boolean> = _occupied.asStateFlow()

    override fun getCloudToken(): String {
        return prefs.getString("tago_token", "") ?: ""
    }

    override fun setCloudToken(token: String) {
        prefs.edit().putString("tago_token", token.trim()).apply()
    }

    /** Permite simular presença local caso esteja sem internet ou demonstrando sem hardware. */
    fun setSimulatedPresence(value: Boolean) {
        _occupied.value = value
        val updated = _telemetry.value.copy(
            occupied = value,
            currentUser = if (value) "Aluno (Simulado)" else "Nenhum",
            acOn = value,
            curtains = if (value) "Aberta" else "Fechada",
            temperature = if (value) 22.0f else 26.5f,
            lastAlert = if (value) "Presença detectada (Simulado)" else "Sala desocupada"
        )
        saveLocalTelemetry(updated)
    }

    private fun saveLocalTelemetry(t: RoomTelemetry) {
        prefs.edit()
            .putBoolean("occupied", t.occupied)
            .putString("currentUser", t.currentUser)
            .putString("curtains", t.curtains)
            .putBoolean("acOn", t.acOn)
            .putFloat("temperature", t.temperature)
            .putString("lightColor", t.lightColor)
            .putString("lastAlert", t.lastAlert)
            .apply()
        _telemetry.value = t
        _occupied.value = t.occupied
    }

    /**
     * Consulta a API do TagoIO (GET /data?qty=40)
     * e atualiza o estado de todas as variáveis da sala no app.
     */
    override suspend fun syncFromCloud(): Result<RoomTelemetry> = withContext(Dispatchers.IO) {
        val token = getCloudToken()
        if (token.isBlank()) {
            Log.d(TAG, "Token TagoIO não configurado. Utilizando telemetria em cache.")
            return@withContext Result.success(_telemetry.value)
        }

        // Tenta os endpoints regionais suportados pelo TagoIO
        val endpoints = listOf(
            "https://api.us-e1.tago.io/data?qty=40",
            "https://api.tago.io/data?qty=40"
        )

        for (endpoint in endpoints) {
            try {
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Device-Token", token)
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 6000
                conn.readTimeout = 6000

                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val jsonResponse = JSONObject(response)
                    val resultArray = jsonResponse.optJSONArray("result") ?: JSONArray()

                    if (resultArray.length() == 0) {
                        val current = _telemetry.value.copy(isOnline = true)
                        _telemetry.value = current
                        return@withContext Result.success(current)
                    }

                    var occ = _telemetry.value.occupied
                    var user = _telemetry.value.currentUser
                    var curt = _telemetry.value.curtains
                    var ac = _telemetry.value.acOn
                    var temp = _telemetry.value.temperature
                    var light = _telemetry.value.lightColor
                    var alert = _telemetry.value.lastAlert

                    // Conjunto para capturar apenas a ocorrência mais recente (índice menor = mais novo)
                    val seenVariables = mutableSetOf<String>()

                    for (i in 0 until resultArray.length()) {
                        val item = resultArray.getJSONObject(i)
                        val variable = item.optString("variable")
                        val value = item.opt("value")?.toString() ?: ""

                        if (seenVariables.add(variable)) {
                            when (variable) {
                                "presenca" -> occ = (value == "1" || value.equals("true", ignoreCase = true))
                                "usuario" -> user = value
                                "cortina" -> curt = value
                                "ar_condicionado" -> ac = (value == "1" || value.equals("true", ignoreCase = true))
                                "temperatura" -> temp = value.toFloatOrNull() ?: temp
                                "luzes" -> light = value
                                "alerta" -> alert = value
                            }
                        }
                    }

                    val newTelemetry = RoomTelemetry(
                        occupied = occ,
                        currentUser = user,
                        curtains = curt,
                        acOn = ac,
                        temperature = temp,
                        lightColor = light,
                        lastAlert = alert,
                        timestamp = System.currentTimeMillis(),
                        isOnline = true
                    )

                    saveLocalTelemetry(newTelemetry)
                    Log.d(TAG, "Sincronização TagoIO bem sucedida via $endpoint: AC=$ac, Temp=$temp, User=$user")
                    return@withContext Result.success(newTelemetry)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Tentativa falhou para $endpoint: ${e.message}")
            }
        }

        val offlineTelemetry = _telemetry.value.copy(isOnline = false)
        _telemetry.value = offlineTelemetry
        Result.failure(Exception("Falha ao comunicar com os endpoints da TagoIO"))
    }

    /**
     * Envia comandos de ativação de sala para a nuvem TagoIO.
     */
    override suspend fun apply(config: RoomConfig): Result<Unit> = withContext(Dispatchers.IO) {
        val token = getCloudToken()
        val jsonPayload = JSONArray().apply {
            put(JSONObject().put("variable", "comando_power").put("value", 1))
            put(JSONObject().put("variable", "ar_condicionado").put("value", if (config.acOn) 1 else 0))
            put(JSONObject().put("variable", "temperatura").put("value", config.temperature))
            put(JSONObject().put("variable", "cortina").put("value", if (config.curtainsOpen) "Aberta" else "Fechada"))
            put(JSONObject().put("variable", "luzes").put("value", "#%06X".format(config.lightColor and 0xFFFFFF)))
            put(JSONObject().put("variable", "alerta").put("value", "Configuração aplicada via App: ${config.name}"))
        }.toString()

        if (token.isNotBlank()) {
            val endpoints = listOf("https://api.us-e1.tago.io/data", "https://api.tago.io/data")
            for (endpoint in endpoints) {
                try {
                    val url = URL(endpoint)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.setRequestProperty("Device-Token", token)
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.connectTimeout = 6000

                    val writer = OutputStreamWriter(conn.outputStream)
                    writer.write(jsonPayload)
                    writer.flush()
                    writer.close()

                    val code = conn.responseCode
                    if (code in 200..299) {
                        Log.d(TAG, "Comando enviado para TagoIO (HTTP $code) via $endpoint")
                        break
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao enviar comando para $endpoint", e)
                }
            }
        }

        val updated = _telemetry.value.copy(
            acOn = config.acOn,
            temperature = config.temperature.toFloat(),
            curtains = if (config.curtainsOpen) "Aberta" else "Fechada",
            lastAlert = "Sala ligada: ${config.name}"
        )
        saveLocalTelemetry(updated)
        Result.success(Unit)
    }

    /**
     * Envia comando de desligamento para a nuvem TagoIO.
     */
    override suspend fun shutdown(): Result<Unit> = withContext(Dispatchers.IO) {
        val token = getCloudToken()
        val jsonPayload = JSONArray().apply {
            put(JSONObject().put("variable", "comando_power").put("value", 0))
            put(JSONObject().put("variable", "ar_condicionado").put("value", 0))
            put(JSONObject().put("variable", "cortina").put("value", "Fechada"))
            put(JSONObject().put("variable", "alerta").put("value", "Sala desligada via App"))
        }.toString()

        if (token.isNotBlank()) {
            val endpoints = listOf("https://api.us-e1.tago.io/data", "https://api.tago.io/data")
            for (endpoint in endpoints) {
                try {
                    val url = URL(endpoint)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.setRequestProperty("Device-Token", token)
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.connectTimeout = 6000

                    val writer = OutputStreamWriter(conn.outputStream)
                    writer.write(jsonPayload)
                    writer.flush()
                    writer.close()

                    val code = conn.responseCode
                    if (code in 200..299) break
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao enviar shutdown", e)
                }
            }
        }

        val updated = _telemetry.value.copy(
            acOn = false,
            curtains = "Fechada",
            lastAlert = "Sala Desligada"
        )
        saveLocalTelemetry(updated)
        Result.success(Unit)
    }

    companion object {
        private const val TAG = "TagoIoRoomGateway"
    }
}
