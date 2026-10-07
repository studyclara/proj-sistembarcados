# Sistema de acesso da sala

Cada pessoa entra com a própria senha no teclado. Se a senha estiver certa, a cortina abre, a fita de LEDs acende na cor dessa pessoa e o nome aparece na tela. Para sair, aperta `*`: a cortina fecha e as luzes apagam.

A placa é um ESP32. O programa está em `sala_de_aula/sala_de_aula.ino`.

## Usuários

| Senha | Nome | Cor dos LEDs |
| --- | --- | --- |
| 1234 | Joao | Azul |
| A1B2 | Maria | Verde |
| 9876 | Carlos | Amarelo |
| C0D3 | Ana | Roxo |

## Como usar o teclado

1. Digite a senha. Cada tecla aparece em amarelo na faixa do meio da tela, e também no Monitor Serial (`115200`).
2. `#` confirma.
3. `*` apaga o que foi digitado e volta para a tela inicial.
4. Senha errada fica 2 segundos na tela e o sistema pede de novo.
5. Com alguém dentro da sala, `*` faz o logoff.

As senhas têm letras. `A1B2` usa a tecla A, o 1, a tecla B e o 2.

## O que aparece na tela ao digitar

A senha continua mascarada: cada tecla vira um `*` amarelo. O Monitor Serial não imprime a senha. Ele só registra entrada, cor do LED, cortina e senha recusada.

Os GPIO 18 e 19 ligam ao mesmo tempo o teclado e o display. Depois que o teclado lê uma tecla, o desenho seguinte não chega na tela. Antes de escrever, o programa devolve esses pinos ao SPI; antes de ler o teclado, devolve eles para a matriz. Os números dos pinos continuam os mesmos.

## Ligações

| Função | GPIO |
| --- | --- |
| Fita NeoPixel, 8 LEDs, DIN | 15 |
| Servo da cortina, sinal | 13 |
| ILI9341 CS | 14 |
| ILI9341 D/C | 21 |
| ILI9341 RST | 22 |
| ILI9341 MOSI | 23 |
| ILI9341 SCK | 18 |
| ILI9341 MISO | 19 |
| Teclado, linhas R1 a R4 | 19, 18, 5, 17 |
| Teclado, colunas C1 a C4 | 16, 4, 0, 2 |

O servo e a fita podem ser alimentados pelo 5 V da placa durante a demonstração, com o GND em comum. Se o ESP32 reiniciar quando a cortina se move ou os LEDs acendem juntos, a alimentação de 5 V desses dois precisa vir de uma fonte separada, sempre com o GND ligado ao GND do ESP32.

O GPIO 0 é a coluna das teclas 3, 6, 9 e `#`. Ele também é o pino de boot. A placa chega a iniciar porque o teclado deixa essa coluna em pull-up, mas um curto nessa coluna no instante do reset entra no modo de gravação.

## O que o programa faz em cada etapa

Na abertura, a cortina fica em 0° e os LEDs apagados. A tela pede a senha.

Senha certa: cortina vai para 90°, os 8 LEDs recebem a cor do usuário e a tela mostra o nome, a cortina aberta e a cor. O Monitor Serial imprime o mesmo estado.

Senha errada: a tela avisa e, dois segundos depois, volta o pedido de senha. O serial registra só o alerta.

Logoff: cortina em 0°, fita apagada, tela inicial.

## Gravar no ESP32

1. Nas preferências da IDE Arduino, em URLs adicionais de placas, use `https://espressif.github.io/arduino-esp32/package_esp32_index.json`. Instale a placa **esp32** by Espressif.
2. Bibliotecas: Adafruit GFX Library, Adafruit BusIO, Adafruit ILI9341, Adafruit NeoPixel, Keypad e ESP32Servo.
3. Abra `sala_de_aula/sala_de_aula.ino`.
4. Placa: **ESP32 Dev Module**. Monitor Serial: **115200**.
5. Envie o programa. O Monitor Serial fica em 115200 e não lista as senhas.

## Simular no Wokwi

A pasta `sala_de_aula` tem o `diagram.json` com esta mesma fiação e o `libraries.txt`. O RST do ILI9341 no simulador não é ligado: o Wokwi ignora esse pino, e o GPIO 22 continua sendo o reset no hardware real.

Os pinos do sketch são os da montagem: tela em CS 14, D/C 21 e RST 22, fita no 15, servo no 13, linhas do teclado em 19, 18, 5 e 17, colunas em 16, 4, 0 e 2.
