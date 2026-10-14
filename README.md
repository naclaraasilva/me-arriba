# Me arriba — Detecção de Quedas e Assistência de Emergência

Aplicativo Android que detecta possíveis quedas/acidentes usando os sensores do
smartphone e conduz o usuário a solicitar ajuda. Interface em português (tema
escuro Me arriba), implementada em **Java + XML** seguindo o padrão do projeto
`tresSensores-main` (Activities + `SensorManager` + `findViewById`).

## Telas (MVP — 5 telas essenciais)

1. **Início / monitoramento** (`MainActivity`) — status do monitoramento,
   localização, modo de transporte, clima, iniciar/parar e botão de pânico.
2. **Possível queda detectada** (`ui/FallDetectedActivity`) — contador circular
   de 30 s, “Não, estou bem” e “Preciso de ajuda”.
3. **Status do alerta** (`ui/AlertStatusActivity`) — estado do envio e status
   individual de API, contatos e localização.
4. **Configurações** (`ui/SettingsActivity`) — contatos de emergência,
   monitoramento, transporte, check-in, permissões e modo demonstração.
5. **Histórico de eventos** (`ui/HistoryActivity`) — filtros Todos/Quedas/Alertas,
   persistido com **Room**, com estado vazio e detalhes em diálogo.

Check-in de segurança é um **modal** sobre a tela inicial (sem tela própria).
Clima é um cartão na tela inicial. Não há tela separada de mapa.

## Arquitetura / código

```
com.reserva.sensor_queda
├── MainActivity                     (Tela 1)
├── ui/
│   ├── FallDetectedActivity         (Tela 2)
│   ├── AlertStatusActivity          (Tela 3)
│   ├── SettingsActivity             (Tela 4)
│   ├── HistoryActivity + EventAdapter + EventUtils (Tela 5)
├── data/
│   ├── EventEntity / EventDao / AppDatabase (Room)
│   ├── EventRepository              (I/O fora da UI thread)
│   ├── EventType
│   ├── EmergencyContact / SettingsRepository (SharedPreferences + JSON)
├── sensor/FallDetector              (acelerômetro + giroscópio + temperatura/pressão)
├── alert/AlertService, AlertSender, MockAlertSender, LocationProvider, AlertResult
├── telemetry/TelemetrySender, DeviceInfo (API Socorro)
└── widget/RingProgressView          (anel circular customizado)
```

UI, detecção de sensores e envio de alertas estão **separados**: a detecção fica
em `sensor/`, o envio em `alert/` e a interface em `ui/` + `MainActivity`.

## Como executar

1. Abra a pasta `sensor-queda` no Android Studio.
2. Aguarde a sincronização do Gradle (usa o SDK em `local.properties`).
3. Conecte um dispositivo ou inicie um emulador com API 29+ (usa `compileSdk 36`).
4. Execute o módulo `app`.

Linha de comando:

```powershell
.\gradlew.bat :app:assembleDebug
# APK: app\build\outputs\apk\debug\app-debug.apk
```

Observação: o acelerômetro/giroscópio **não** funcionam no emulador; para testar
a detecção use o botão **“Simular queda (teste)”** na tela inicial.

## Como testar os fluxos

- **Queda simulada:** Início → “Simular queda (teste)” → Tela 2 → escolha
  “Não, estou bem” (registra falso positivo) ou “Preciso de ajuda”/espera 30 s
  (vai para a Tela 3).
- **Botão de pânico:** clique (abre confirmação) ou **pressione e segure**.
- **Check-in:** em Configurações, ative o check-in e defina o intervalo.
- **Histórico:** todos os eventos acima são persistidos e sobrevivem ao reinício.

## Integração com a API de telemetria (Socorro)

O app já integra a API real em `https://ideaberta.com.br/socorro/`.

- **Endpoint padrão** e **modo demonstração desligado** por padrão; ambos
  ajustáveis em Configurações. Também há o campo **Nome do usuário** (enviado
  como `usuario`).
- **Envio de alerta (queda/pânico):** POST `application/x-www-form-urlencoded`
  feito por `telemetry/TelemetrySender` a partir de `AlertService`.
- **Telemetria periódica (tipo Normal):** enviada a cada 15 s enquanto o
  monitoramento está ativo (`MainActivity`), com localização, velocidade,
  bateria e sensores ambientais quando disponíveis.
- **Campos enviados:** `usuario`, `modelo`, `device_id`, `bateria`, `latitude`,
  `longitude`, `tipo` (1=Normal, 2=Queda), `queda` (impacto em G),
  `velocidade` (km/h), `temperatura` (°C) e `pressao` (hPa).
- **Sucesso** = HTTP 2xx com `"status":"sucesso"` no JSON de resposta. A Tela 3
  mostra “Enviado à API”, sem afirmar entrega aos contatos.

## O que ainda depende de integração real

- **Envio efetivo de SMS/WhatsApp aos contatos** — requer canal/API de
  mensageria realmente configurado. Por isso a Tela 3 mostra “Sem confirmação”
  para contatos: sucesso HTTP da API **não** prova entrega ao contato.
- **Integração de clima** (ex.: OpenWeatherMap) — cartão permanece
  “Clima indisponível” até a integração existir.
- **Sincronização na nuvem** do histórico — expansão futura; hoje é local (Room).
- **Monitoramento em segundo plano** — o MVP registra sensores enquanto a tela
  está ativa (`onResume`/`onPause`). Um serviço em primeiro plano com notificação
  é necessário para rodar com o app fechado.

## Aviso

A detecção por smartphone é heurística, **não é infalível** e **não substitui**
serviços de emergência nem garante socorro. O algoritmo combina queda livre +
impacto e/ou rotação e precisa de calibração e testes em campo.
