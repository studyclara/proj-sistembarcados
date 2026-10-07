#include <SPI.h>
#include <Keypad.h>
#include <Adafruit_NeoPixel.h>
#include <ESP32Servo.h>
#include <Adafruit_GFX.h>
#include <Adafruit_ILI9341.h>


#define LED_PIN 15
#define NUMPIXELS 8
#define SERVO_PIN 13
#define TFT_CS 14
#define TFT_DC 21
#define TFT_RST 22
#define TFT_SCK 18
#define TFT_MISO 19
#define TFT_MOSI 23

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
byte colPins[COLS] = {16, 4, 0, 2};
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

String senhaDigitada = "";
bool usuarioLogado = false;

void prepararTeclado() {
  for (byte i = 0; i < ROWS; i++) {
    pinMode(rowPins[i], OUTPUT);
    digitalWrite(rowPins[i], HIGH);
  }
  for (byte i = 0; i < COLS; i++) {
    pinMode(colPins[i], INPUT_PULLUP);
  }
}

void prepararTela() {
  SPI.end();
  SPI.begin(TFT_SCK, TFT_MISO, TFT_MOSI, -1);
  pinMode(TFT_CS, OUTPUT);
  digitalWrite(TFT_CS, HIGH);
  pinMode(TFT_DC, OUTPUT);
  pinMode(TFT_RST, OUTPUT);
  digitalWrite(TFT_RST, HIGH);
}

void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\n--- SISTEMA DE ACESSO INICIALIZADO ---");

  strip.begin();
  strip.show();

  cortina.attach(SERVO_PIN);
  cortina.write(0);

  prepararTela();
  tft.begin();
  tft.setRotation(1);
  tft.setTextWrap(false);
  exibirMensagemPadrao();
}

void loop() {
  prepararTeclado();
  char key = keypad.getKey();

  if (key) {
    if (usuarioLogado) {
      if (key == '*') {
        fazerLogoff();
      }
    } else {
      if (key == '#') {
        verificarSenha();
        senhaDigitada = "";
      } else if (key == '*') {
        senhaDigitada = "";
        exibirMensagemPadrao();
      } else {
        senhaDigitada += key;
        atualizarDisplayDigitos();
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

  Serial.println("----------------------------------------");
  Serial.print("Bem-vindo, ");
  Serial.print(user.nome);
  Serial.println("!");
  Serial.println("STATUS DA SALA:");
  Serial.println("- Cortina: ABERTA (90 deg)");
  Serial.print("- Cor do LED: ");
  Serial.println(user.nomeCor);
  Serial.println("----------------------------------------");

  prepararTela();
  tft.fillScreen(ILI9341_BLACK);
  tft.setTextColor(ILI9341_GREEN, ILI9341_BLACK);
  tft.setTextSize(2);
  tft.setCursor(10, 20);
  tft.println("BEM-VINDO(A)!");
  tft.setCursor(10, 60);
  tft.println(user.nome);

  tft.setTextColor(ILI9341_WHITE, ILI9341_BLACK);
  tft.setTextSize(2);
  tft.setCursor(10, 120);
  tft.print("Para sair,");
  tft.setCursor(10, 148);
  tft.print("pressione *");

  for (int i = 0; i < NUMPIXELS; i++) {
    strip.setPixelColor(i, user.corHex);
  }
  strip.show();

  cortina.write(90);
}

void erroAcesso() {
  Serial.println("[ALERTA] Tentativa de acesso negada! Senha errada.");

  prepararTela();
  tft.fillScreen(ILI9341_BLACK);
  tft.setTextColor(ILI9341_RED, ILI9341_BLACK);
  tft.setTextSize(2);
  tft.setCursor(10, 50);
  tft.println("Senha errada");

  delay(2000);
  exibirMensagemPadrao();
}

void fazerLogoff() {
  usuarioLogado = false;

  Serial.println("----------------------------------------");
  Serial.println("Usuario saiu da sala.");
  Serial.println("STATUS DA SALA:");
  Serial.println("- Cortina: FECHADA (0 deg)");
  Serial.println("- LED: DESLIGADO");
  Serial.println("----------------------------------------");

  cortina.write(0);
  strip.clear();
  strip.show();

  senhaDigitada = "";
  exibirMensagemPadrao();
}
