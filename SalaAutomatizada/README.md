# Sala Automatizada

Aplicativo Android (Kotlin + Jetpack Compose + Room) para configurar e ligar a automação de salas.

## Como abrir
1. Extraia o .zip.
2. No Android Studio (Koala 2024.1 ou mais novo): **File > Open** e selecione a pasta `SalaAutomatizada` (a que contém `settings.gradle.kts`).
3. Aguarde o *Gradle Sync* (precisa de internet; use JDK 17, que já vem com o Android Studio). SDK 34 instalado.
4. Escolha um emulador ou celular (Android 8.0+ / API 26+) e clique em **Run**.

> Se você já tinha a versão anterior instalada no emulador, desinstale antes (ou use *Wipe Data*) para o ícone novo aparecer.

## Funcionalidades
- **Splash:** tela de abertura animada (e splash nativa do Android 12+).
- **Login/cadastro:** e-mail e senha, armazenados localmente (senha com hash SHA-256).
- **Home:** botão **Ligar sala / Desligar sala** fixo na parte inferior, menu da conta (ícone no topo, com opção de sair) e lista de salas configuradas (prática, laboratório, teórica...). Toque em uma para torná-la a **ativa**; a anterior é desativada automaticamente. A sala ativa recebe o selo verde **Ativa** (quando o sistema está ligado) ou **Selecionada** (quando está desligado). Cada card tem Editar e Excluir.
- **Liga/Desliga:** o botão inferior ativa/desativa o microcontrolador com a sala selecionada.
- **Aviso de presença:** enquanto houver alguém na sala, a Home mostra o aviso "Sala ocupada" no topo.
- **Bloqueio por presença:** se houver alguém na sala, ligar, desligar, trocar a sala ativa ou excluir a sala ativa mostra o aviso "Sala ocupada" e nada é alterado.
- **Configuração da sala:** luzes (liga/desliga, intensidade, cor), cortinas, ar-condicionado e temperatura ideal (16–30 °C). Salva com o nome que o usuário escolher.
- **Ícone do app:** ícone adaptativo (com versão monocromática para Android 13+).
- Tema claro e escuro automáticos.

## Presença e microcontrolador (importante)
Hoje o microcontrolador e o sensor de presença são **simulados** (`device/RoomGateway.kt`):
- Para testar o bloqueio sem o hardware, **segure o dedo no selo "Vazia/Ocupada"** do cartão de status na Home: ele alterna a presença simulada.
- Os comandos que seriam enviados aparecem no **Logcat** (filtre por `RoomGateway`) em formato JSON:
  `{"power":true,"room_type":"Sala prática","lights":{"on":true,"brightness":80,"color":"#FFE0B2"},"curtains":"open","ac":{"on":true,"temperature":23}}`

Para ligar ao hardware real, crie uma classe que implemente `RoomGateway` (HTTP para um ESP32, MQTT ou BLE), com `occupied` vindo do sensor, e troque a linha `private val gateway: RoomGateway = SimulatedRoomGateway(app)` em `AppViewModel.kt`. Se usar rede, adicione a permissão `INTERNET` no `AndroidManifest.xml`.

## Observações
- Dados (contas e configurações) ficam no banco local (Room) do aparelho; não há servidor.
- minSdk 26 (Android 8.0), compileSdk 34.
