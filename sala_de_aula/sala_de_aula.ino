#include <SPI.h>
#include <Keypad.h>
#include <Adafruit_NeoPixel.h>
#include <ESP32Servo.h>
#include <Adafruit_GFX.h>
#include <Adafruit_ILI9341.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>

// --- PINOS DO HARDWARE ---
#define LED_PIN 15          // Fita NeoPixel
#define NUMPIXELS 8         // Quantidade de LEDs NeoPixel
#define SERVO_PIN 13        // Servo motor da cortina
#define AC_LED_PIN 27       // LED indicador do Ar Condicionado

#define TFT_CS 14
#define TFT_DC 21
#define TFT_RST 22
#define TFT_SCK 18
#define TFT_MOSI 23
// NOTA: O display ILI9341 é apenas escrita, então MISO não é conectado ao SPI,
// liberando o GPIO 19 para a Linha 1 do teclado (teclas 1, 2, 3, A) sem conflitos!

// --- CONFIGURAÇÃO WI-FI E NUVEM IOT (TagoIO) ---
const char* WIFI_SSID = "Wokwi-GUEST";
const char* WIFI_PASS = "";

// Token do Dispositivo na TagoIO:
const char* TAGO_DEVICE_TOKEN = "e00da194-f6c1-435c-bd87-3e209c9a18e6";
const char* TAGO_API_URL = "https://api.tago.io/data";

// --- INSTÂNCIAS DOS COMPONENTES ---
Adafruit_NeoPixel strip(NUMPIXELS, LED_PIN, NEO_GRB + NEO_KHZ800);
Servo cortina;
Adafruit_ILI9341 tft = Adafruit_ILI9341(TFT_CS, TFT_DC, TFT_RST);

// --- CONFIGURAÇÃO DO TECLADO MATRICIAL (KEYPAD) ---
const byte ROWS = 4;
const byte COLS = 4;
char keys[ROWS][COLS] = {
    {'1', '2', '3', 'A'},
    {'4', '5', '6', 'B'},
    {'7', '8', '9', 'C'},
    {'*', '0', '#', 'D'}};
byte rowPins[ROWS] = {19, 18, 5, 17};
byte colPins[COLS] = {16, 4, 25, 2};
Keypad keypad = Keypad(makeKeymap(keys), rowPins, colPins, ROWS, COLS);

// --- ESTRUTURA DE DADOS DE USUÁRIO ---
struct Usuario {
  String senha;
  String nome;
  String nomeCor;
  uint32_t corHex;
};

// --- CADASTRO DOS 4 USUÁRIOS ---
Usuario usuarios[] = {
    {"1234", "Joao", "Azul", strip.Color(0, 0, 255)},
    {"A1B2", "Maria", "Verde", strip.Color(0, 255, 0)},
    {"9876", "Carlos", "Amarelo", strip.Color(255, 255, 0)},
    {"C0D3", "Ana", "Roxo", strip.Color(255, 0, 255)}};
const int totalUsuarios = 4;

// --- ESTADO DO SISTEMA ---
String senhaDigitada = "";
bool usuarioLogado = false;
bool arCondicionadoLigado = false;
float temperaturaAtual = 26.5;
int tentativasErradas = 0;
String usuarioAtual = "Nenhum";
String corAtual = "Apagado";

// Protótipos das funções
void prepararTeclado();
void prepararTela();
void conectarWiFi();
void enviarTelemetriaNuvem(const char* alerta = "Normal");
void exibirMensagemPadrao();
void atualizarDisplayDigitos();
void verificarSenha();
void sucessoAcesso(Usuario user);
void erroAcesso();
void fazerLogoff();

void prepararTela() {
  SPI.end();
  // Passamos -1 no MISO para o SPI nunca roubar o GPIO 19 da Linha 1 do teclado!
  SPI.begin(TFT_SCK, -1, TFT_MOSI, -1);
  pinMode(TFT_CS, OUTPUT);
  digitalWrite(TFT_CS, HIGH);
  pinMode(TFT_DC, OUTPUT);
  pinMode(TFT_RST, OUTPUT);
  digitalWrite(TFT_RST, HIGH);
}

void prepararTeclado() {
  SPI.end();
  // Restaura os pinos compartilhados para modo GPIO puro
  gpio_reset_pin((gpio_num_t)19); // R1 (teclas 1, 2, 3, A)
  gpio_reset_pin((gpio_num_t)18); // R2 (teclas 4, 5, 6, B)
  gpio_reset_pin((gpio_num_t)25); // C3 (teclas 3, 6, 9, #)

  for (byte i = 0; i < ROWS; i++) {
    pinMode(rowPins[i], OUTPUT);
    digitalWrite(rowPins[i], HIGH);
  }
  for (byte i = 0; i < COLS; i++) {
    pinMode(colPins[i], INPUT_PULLUP);
  }
  delayMicroseconds(50);
}

void conectarWiFi() {
  Serial.print("Conectando ao Wi-Fi: ");
  Serial.println(WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASS);

  int timeout = 0;
  while (WiFi.status() != WL_CONNECTED && timeout < 20) {
    delay(500);
    Serial.print(".");
    timeout++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n[Wi-Fi] Conectado com sucesso!");
    Serial.print("[Wi-Fi] IP: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\n[Wi-Fi] Modo offline");
  }
}

void enviarTelemetriaNuvem(const char* alerta) {
  if (WiFi.status() != WL_CONNECTED) {
    WiFi.begin(WIFI_SSID, WIFI_PASS);
    return;
  }

  if (String(TAGO_DEVICE_TOKEN).indexOf("INSIRA_SEU") >= 0) {
    Serial.println("[IoT] Configure seu TAGO_DEVICE_TOKEN no codigo!");
    return;
  }

  Serial.println("[IoT] Enviando dados para a Plataforma IoT (TagoIO)...");

  WiFiClientSecure client;
  client.setInsecure();

  HTTPClient https;
  if (https.begin(client, TAGO_API_URL)) {
    https.addHeader("Content-Type", "application/json");
    https.addHeader("Device-Token", TAGO_DEVICE_TOKEN);

    StaticJsonDocument<512> doc;
    JsonArray array = doc.to<JsonArray>();

    JsonObject p = array.createNestedObject();
    p["variable"] = "presenca";
    p["value"] = usuarioLogado ? 1 : 0;

    JsonObject u = array.createNestedObject();
    u["variable"] = "usuario";
    u["value"] = usuarioAtual;

    JsonObject c = array.createNestedObject();
    c["variable"] = "cortina";
    c["value"] = usuarioLogado ? "Aberta" : "Fechada";

    JsonObject ac = array.createNestedObject();
    ac["variable"] = "ar_condicionado";
    ac["value"] = arCondicionadoLigado ? 1 : 0;

    JsonObject t = array.createNestedObject();
    t["variable"] = "temperatura";
    t["value"] = temperaturaAtual;

    JsonObject l = array.createNestedObject();
    l["variable"] = "luzes";
    l["value"] = corAtual;

    JsonObject a = array.createNestedObject();
    a["variable"] = "alerta";
    a["value"] = alerta;

    JsonObject err = array.createNestedObject();
    err["variable"] = "erros_senha";
    err["value"] = tentativasErradas;

    String jsonString;
    serializeJson(doc, jsonString);

    int httpCode = https.POST(jsonString);
    if (httpCode > 0) {
      Serial.print("[IoT] Resposta Nuvem: ");
      Serial.println(httpCode);
    } else {
      Serial.print("[IoT] Falha HTTP: ");
      Serial.println(https.errorToString(httpCode));
    }
    https.end();
  }
}

void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\n==========================================");
  Serial.println("  SISTEMA DE ACESSO & IOT INICIALIZADO   ");
  Serial.println("==========================================");

  pinMode(AC_LED_PIN, OUTPUT);
  digitalWrite(AC_LED_PIN, LOW);

  strip.begin();
  strip.show();

  cortina.attach(SERVO_PIN);
  cortina.write(0);

  prepararTela();
  tft.begin();
  tft.setRotation(1);
  tft.setTextWrap(false);
  exibirMensagemPadrao();

  prepararTeclado();
  keypad.setDebounceTime(20);

  conectarWiFi();
  enviarTelemetriaNuvem("Sistema Inicializado");
}

void loop() {
  char key = keypad.getKey();

  if (key) {
    Serial.print("[Teclado] Pressionado: ");
    Serial.println(key);

    if (usuarioLogado) {
      if (key == '*') {
        fazerLogoff();
      }
    } else {
      if (key == '#') {
        if (senhaDigitada.length() > 0) {
          verificarSenha();
          senhaDigitada = "";
        }
      } else if (key == '*') {
        senhaDigitada = "";
        Serial.println("[Teclado] Senha limpa!");
        exibirMensagemPadrao();
      } else {
        if (senhaDigitada.length() < 4) {
          senhaDigitada += key;
          atualizarDisplayDigitos();
        } else {
          Serial.println("[Teclado] Senha com 4 digitos. Pressione '#' para confirmar ou '*' para apagar.");
        }
      }
    }
  }
}

void exibirMensagemPadrao() {
  prepararTela();
  tft.fillScreen(ILI9341_BLACK);
  tft.setTextColor(ILI9341_WHITE, ILI9341_BLACK);
  tft.setTextSize(2);
  tft.setCursor(10, 20);
  tft.println("SISTEMA DE ACESSO");
  tft.setCursor(10, 60);
  tft.println("Digite a senha e");
  tft.println("pressione '#':");

  tft.setTextSize(1);
  tft.setTextColor(ILI9341_CYAN, ILI9341_BLACK);
  tft.setCursor(10, 200);
  tft.print("Ar Cond: ");
  tft.print(arCondicionadoLigado ? "LIGADO" : "DESLIGADO");
  tft.print(" | ");
  tft.print(temperaturaAtual, 1);
  tft.println(" C");

  tft.setTextColor(ILI9341_LIGHTGREY, ILI9341_BLACK);
  tft.setCursor(10, 220);
  tft.print("Wi-Fi: ");
  tft.print(WiFi.status() == WL_CONNECTED ? "Online" : "Desconectado");

  // Devolve imediatamente os pinos para o teclado
  prepararTeclado();
}

void atualizarDisplayDigitos() {
  prepararTela();
  tft.fillRect(10, 108, 300, 36, ILI9341_BLACK);
  tft.setTextSize(2);
  tft.setTextColor(ILI9341_YELLOW, ILI9341_BLACK);
  tft.setCursor(10, 112);

  for (unsigned int i = 0; i < senhaDigitada.length(); i++) {
    tft.print('*');
  }

  // Devolve imediatamente os pinos para o teclado
  prepararTeclado();
}

void verificarSenha() {
  bool acessoConcedido = false;

  for (int i = 0; i < totalUsuarios; i++) {
    if (senhaDigitada == usuarios[i].senha) {
      acessoConcedido = true;
      sucessoAcesso(usuarios[i]);
      break;
    }
  }

  if (!acessoConcedido) {
    erroAcesso();
  }
}

void sucessoAcesso(Usuario user) {
  usuarioLogado = true;
  tentativasErradas = 0;
  usuarioAtual = user.nome;
  corAtual = user.nomeCor;
  
  // Liga o Ar Condicionado (LED AC pino 27)
  arCondicionadoLigado = true;
  digitalWrite(AC_LED_PIN, HIGH);
  temperaturaAtual = 22.0;

  Serial.println("----------------------------------------");
  Serial.print("Bem-vindo, ");
  Serial.print(user.nome);
  Serial.println("!");
  Serial.println("STATUS DA SALA:");
  Serial.println("- Cortina: ABERTA (90 deg)");
  Serial.print("- Cor do LED: ");
  Serial.println(user.nomeCor);
  Serial.println("- Ar Condicionado: LIGADO (LED Azul ON)");
  Serial.println("- Temperatura: 22.0 C");
  Serial.println("----------------------------------------");

  prepararTela();
  tft.fillScreen(ILI9341_BLACK);
  tft.setTextColor(ILI9341_GREEN, ILI9341_BLACK);
  tft.setTextSize(2);
  tft.setCursor(10, 20);
  tft.println("BEM-VINDO(A)!");
  tft.setCursor(10, 50);
  tft.println(user.nome);

  tft.setTextColor(ILI9341_CYAN, ILI9341_BLACK);
  tft.setTextSize(2);
  tft.setCursor(10, 90);
  tft.println("Ar Cond: LIGADO");
  tft.setTextSize(1);
  tft.setCursor(10, 115);
  tft.print("Climatizando para 22 C");

  tft.setTextColor(ILI9341_WHITE, ILI9341_BLACK);
  tft.setTextSize(2);
  tft.setCursor(10, 150);
  tft.print("Para sair,");
  tft.setCursor(10, 175);
  tft.print("pressione *");

  for (int i = 0; i < NUMPIXELS; i++) {
    strip.setPixelColor(i, user.corHex);
  }
  strip.show();

  cortina.write(90);

  enviarTelemetriaNuvem("Acesso Autorizado");

  prepararTeclado();
}

void erroAcesso() {
  tentativasErradas++;
  Serial.println("[ALERTA] Tentativa de acesso negada! Senha errada.");
  Serial.print("[ALERTA] Erros consecutivos: ");
  Serial.println(tentativasErradas);

  prepararTela();
  tft.fillScreen(ILI9341_BLACK);
  tft.setTextColor(ILI9341_RED, ILI9341_BLACK);
  tft.setTextSize(2);
  tft.setCursor(10, 40);
  tft.println("Senha errada!");
  tft.setTextSize(1);
  tft.setCursor(10, 80);
  tft.print("Tentativa incorreta: ");
  tft.println(tentativasErradas);

  if (tentativasErradas >= 3) {
    tft.setTextColor(ILI9341_YELLOW, ILI9341_BLACK);
    tft.setTextSize(2);
    tft.setCursor(10, 120);
    tft.println("ALERTA ENVIADO!");
    tft.setTextSize(1);
    tft.setCursor(10, 150);
    tft.println("SMS e E-mail disparados na nuvem");
    enviarTelemetriaNuvem("Tentativa de Invasao: 3 Senhas Incorretas");
  } else {
    enviarTelemetriaNuvem("Senha Incorreta");
  }

  delay(2000);
  exibirMensagemPadrao();
}

void fazerLogoff() {
  usuarioLogado = false;
  usuarioAtual = "Nenhum";
  corAtual = "Apagado";

  arCondicionadoLigado = false;
  digitalWrite(AC_LED_PIN, LOW);
  temperaturaAtual = 27.0;

  Serial.println("----------------------------------------");
  Serial.println("Usuario saiu da sala.");
  Serial.println("STATUS DA SALA:");
  Serial.println("- Cortina: FECHADA (0 deg)");
  Serial.println("- LED: DESLIGADO");
  Serial.println("- Ar Condicionado: DESLIGADO");
  Serial.println("----------------------------------------");

  cortina.write(0);
  strip.clear();
  strip.show();

  senhaDigitada = "";
  exibirMensagemPadrao();

  enviarTelemetriaNuvem("Sala Desocupada");
}
