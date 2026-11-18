# Sala Automatizada - IoT & Monitoramento Mobile

Aplicativo Android (Kotlin + Jetpack Compose + Room) integrado à plataforma IoT em nuvem (TagoIO) e ao projeto de microcontrolador ESP32.

---

## 🔗 Links do Projeto
- 🌐 **Simulador Wokwi (ESP32):** [Proj Sistemas Embarcados 2.0 - Wokwi](https://wokwi.com/projects/480979869852553217)
- 📊 **Dashboard na Nuvem (TagoIO):** [Painel de Monitoramento TagoIO](https://admin.us-e1.tago.io/dashboards?page=1&amount=20)

---

## Como abrir e executar
1. No Android Studio (Koala 2024.1 ou mais novo): **File > Open** e selecione a pasta `SalaAutomatizada` (a que contém `settings.gradle.kts`).
2. Aguarde o *Gradle Sync* (use JDK 17, já padrão do Android Studio; compileSdk 34, minSdk 26).
3. Escolha um emulador ou celular físico conectado via USB / Wi-Fi e clique em **Run**.

---

## Funcionalidades do Aplicativo Mobile

O aplicativo possui navegação inferior dividida em 3 abas principais:

### 1. Aba "Salas" (Configuração e Controle)
- **Seleção e Ativação de Salas:** Lista de salas configuradas (prática, laboratório, teórica...). Toque em uma para torná-la a ativa.
- **Botão Liga/Desliga Inferior:** Envia o comando de ativação/desativação para a nuvem IoT e para o ESP32.
- **Bloqueio por Presença:** Impede alterações enquanto a sala estiver ocupada.
- **Personalização de Sala:** Controle de iluminação (brilho, cor NeoPixel), cortinas e ar condicionado com temperatura ideal (16–30 °C).

### 2. Aba "Monitoramento" (Dashboard IoT em Tempo Real)
- **Status da Conexão Nuvem:** Indicador de sincronização com a TagoIO e botão de atualização rápida.
- **Card do Ar Condicionado:** Exibe o estado em tempo real (Ligado/Desligado), indicador do LED azul (GPIO 27) e medidor de temperatura ambiente / climatizada (°C).
- **Card de Ocupação:** Indica se a sala está Livre ou Ocupada, mostrando o nome do usuário identificado pelo teclado físico do ESP32.
- **Card de Atuadores:** Estado da cortina e cor da fita NeoPixel.
- **Card de Segurança & Alertas:** Exibe o status de segurança e indica se avisos de SMS e E-mail foram disparados por tentativas inválidas de acesso.
- **Configuração do Token:** Botão no topo para inserir o Device Token da TagoIO.

### 3. Aba "Relatórios" (Métricas e Histórico)
- **Cards de Métricas (KPIs):** Total de acessos registrados, ciclos de ar condicionado ativo e temperatura média.
- **Linha do Tempo de Eventos:** Histórico detalhado de entradas, saídas, acionamentos de ar condicionado e alertas de tentativas de invasão, com data e hora.
- **Exportar Relatório:** Botão para compartilhar o relatório consolidado em texto via WhatsApp, E-mail, Google Drive ou Bloco de Notas.
- **Limpeza de Logs:** Opção para resetar os registros locais.

---

## Integração com a Plataforma IoT (TagoIO)
- O aplicativo comunica-se diretamente com a API REST da TagoIO (`https://api.tago.io/data`) utilizando HTTPS.
- Para conectar o app à sua conta na nuvem:
  1. Vá na aba **Monitoramento**;
  2. Toque no ícone de **Chave** no canto superior direito;
  3. Cole o seu **Device Token** gerado no TagoIO e clique em **Salvar**;
  4. O app passa a sincronizar os dados do ESP32 em tempo real!
- Caso nenhum token seja inserido, o aplicativo funciona em **Modo Demonstração Offline**, permitindo testar todas as telas e relatórios mesmo sem conexão.
