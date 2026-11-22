# Projeto Ferramentas: briefing para o Claude Code

Nov 21, 2026 · @André

## Visão geral

O Projeto Ferramentas é um app Android nativo que reúne num só lugar mais de 120 ferramentas que tiram proveito dos sensores, rádios e APIs do próprio celular: bateria, internet, NFC, medição, som, câmera e outras. Tudo funciona só com o aparelho, sem acessórios e sem servidor próprio.

- **Aparelhos-alvo:** Galaxy S20 FE (Android 13, o último que ele recebeu) e Galaxy A24 (Android 15 ou mais novo). Os dois têm que funcionar.
- **Ferramentas para aparelhos futuros:** entram no app mesmo as que pedem hardware que os dois não têm (barômetro, UWB, infravermelho, sensor de profundidade). Nesses aparelhos elas aparecem em cinza, com o motivo.
- **Inspiração:** GlassWire (só a parte de ver quanto cada app gasta de internet, sem firewall), Network Analyzer, NFC Tools, phyphox e apps de medição.
- **Licença:** GPL-3.0.
- **Design:** Open Design, o design system do André, que já tem a versão para Android.
- **Fonte da lista de ferramentas:** a planilha [Ferramentas do celular: catálogo de ideias](https://claude.ai/artifact/HbCYz9WVuTd3XTSbask3Kg). A coluna "Quero?" ainda não foi preenchida; até lá, este documento traz a lista completa.

## Estrutura de pastas

O Claude Code roda dentro de `Projeto Ferramentas/`, que vira a raiz do repositório. O Open Design fica um nível acima, em `code/`, e é lido de lá por caminho relativo (`../open-design/`).

```
code/
├── open-design.zip              pacote baixado (não usar)
├── open-design/                 pacote descompactado (referência, somente leitura)
│   ├── README.md                regras gerais do design system
│   ├── usar-com-claude-code.md
│   ├── android/
│   │   ├── README.md            guia do Compose (ler primeiro)
│   │   ├── tokens-resolvidos.json
│   │   ├── ferramentas/         geradores Python (tokens, ícones)
│   │   ├── opendesign/          módulo de biblioteca Compose
│   │   └── exemplo/MainActivity.kt
│   └── components/<Nome>/README.md
└── Projeto Ferramentas/         repositório do app (onde o Claude Code roda)
    ├── CLAUDE.md                este briefing, resumido
    ├── LICENSE                  GPL-3.0
    ├── settings.gradle.kts
    ├── opendesign/              cópia do módulo do Open Design
    └── app/
```

- **Os nomes acima são os do pacote.** Se a pasta descompactada tiver outro nome (ex.: `OpenDesign/`), o Claude Code deve achar o caminho certo antes de começar e usar esse.
- **O módulo `opendesign/` é copiado para dentro do repositório**, como o próprio Open Design pede. Assim o projeto compila sozinho em qualquer máquina, inclusive no GitHub. Anotar no `CLAUDE.md` de qual versão do pacote veio a cópia.
- **Nada dentro de `code/open-design/` é editado.** Quando o design system mudar, a cópia em `opendesign/` é atualizada a partir dele.
- **Espaço no nome da pasta:** o Gradle aceita `Projeto Ferramentas`, mas se alguma ferramenta reclamar do caminho, renomear para `ProjetoFerramentas` resolve.

## Decisões técnicas

O app é nativo em Kotlin com Jetpack Compose, com `minSdk` 33, porque quase todas as ferramentas dependem de APIs que não existem para web ou para apps híbridos.

| Item | Decisão |
| --- | --- |
| Linguagem e UI | Kotlin + Jetpack Compose (Compose 1.9 ou mais novo, exigência do Open Design), Material 3 por baixo do tema |
| Versões do Android | `minSdk` 33 (Android 13, o do S20 FE); `targetSdk` e `compileSdk` na versão estável mais recente. APIs do Android 14+ (ciclos e saúde da bateria) atrás de checagem de versão |
| Módulos Gradle | `:app` e `:opendesign` (cópia do Open Design) |
| Navegação | Uma Activity, Navigation Compose |
| Dados | DataStore para preferências, Room para históricos (bateria, sinal, medições), exportação em CSV |
| Tarefas em segundo plano | Foreground service para medições longas (teste de standby, logs); WorkManager para tarefas periódicas (mínimo de 15 min) |
| Concorrência | Coroutines e Flow; cada sensor exposto como `Flow` |
| Idioma | Interface em português do Brasil |
| Licença | GPL-3.0, arquivo `LICENSE` na raiz e tela Sobre com as licenças de terceiros (fontes OFL, ícones Tabler MIT, código reaproveitado) |
| Rede | Sem análise de uso nem rastreadores. Internet só nas ferramentas que precisam dela (whois, IP externo, teste de velocidade), e isso aparece na tela da ferramenta |

**Modelo de ferramenta.** Cada ferramenta é registrada num catálogo central com: id, grupo, seção, nome, ícone, requisitos e tela. Os requisitos são recursos de hardware (`PackageManager.hasSystemFeature`), sensores (`SensorManager.getDefaultSensor`), versão mínima do Android e permissões. O catálogo alimenta a grade de grupos, a busca, os favoritos e o estado de disponibilidade, então adicionar uma ferramenta é criar a tela e uma entrada no catálogo.

**Permissões.** Cada uma é pedida só quando a ferramenta que precisa dela abre, com uma explicação antes (componente `OdEstadoRecurso`). Nada é pedido na abertura do app.

| Permissão | Usada por | Como se obtém |
| --- | --- | --- |
| `PACKAGE_USAGE_STATS` | Uso de dados por app, tempo de tela | Usuário libera em Configurações > Acesso ao uso |
| `ACCESS_FINE_LOCATION`, `NEARBY_WIFI_DEVICES` | Nome do Wi-Fi, varredura de redes, rede celular, GPS | Pedido normal |
| `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` | Ferramentas de Bluetooth | Pedido normal |
| `NFC` | Ferramentas de NFC | Automática |
| `CAMERA` | Ferramentas de câmera (a lanterna não precisa) | Pedido normal |
| `RECORD_AUDIO` | Ferramentas de som | Pedido normal |
| `ACTIVITY_RECOGNITION` | Pedômetro | Pedido normal |
| `HIGH_SAMPLING_RATE_SENSORS` | Vibrômetro e sensores acima de 200 Hz | Automática |
| `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE` e tipos | Alarmes, medições longas, velocidade na barra | Pedido normal / declaração |
| `BIND_VPN_SERVICE` | Conexões por app em tempo real | Usuário aceita a VPN local |
| `QUERY_ALL_PACKAGES` | Nome e ícone dos apps nas listas | Declaração (restrita na Play Store) |
| `MANAGE_EXTERNAL_STORAGE` | Analisador de armazenamento | Usuário libera em Configurações (restrita na Play Store) |
| `BATTERY_STATS` | Saúde % oficial da bateria (opcional) | ADB ou Shizuku, uma vez |
| `TRANSMIT_IR`, `UWB_RANGING` | Infravermelho, UWB (aparelhos futuros) | Automática / pedido normal |

A distribuição ainda não foi decidida. Fora da Play Store (APK no GitHub Releases ou F-Droid) as duas permissões restritas não são problema.

## Organização do app

O app é organizado em 13 grupos de ferramentas: tocar em "Bateria" mostra as ferramentas de bateria, tocar em "Internet" mostra as de internet, e assim por diante. Dentro de cada grupo, as ferramentas ficam divididas em seções.

**Navegação** (barra inferior do Open Design, `OdBarraInferior`):

1. **Início:** grade com os 13 grupos (`OdGrade`), cada um com ícone, nome e quantidade de ferramentas.
2. **Favoritos:** as ferramentas fixadas pelo usuário.
3. **Buscar:** busca pelo nome da ferramenta, em todos os grupos.
4. **Mais:** Configurações (Aparência sempre como primeiro item, depois as opções do app), Sobre e licenças.

Caminho de uma ferramenta: Início → grupo → lista por seção → tela da ferramenta, com o botão Voltar do Android funcionando em cada passo.

| Grupo | Seções | Ferramentas |
| --- | --- | --- |
| Bateria | Ao vivo, Saúde, Testes, Histórico e alertas | 12 |
| Internet | Uso de dados, Wi-Fi, Diagnóstico | 25 |
| Sinais | Rede celular, Bluetooth, Outros rádios | 7 |
| NFC | Ler, Gravar, Proteger, Avançado, Automação | 13 |
| Localização | Posição, Direção, Trajeto | 9 |
| Medição | Tela, Inclinação, Distância e altura | 10 |
| Movimento | Sensores, Experimentos | 6 |
| Ambiente | Magnetismo, Luz, Clima | 9 |
| Som | Medir, Música, Gerar, Testes, Experimentos | 10 |
| Câmera | Códigos, Imagem, Documentos, Flash | 9 |
| Saúde | Coração, Atividade | 4 |
| Aparelho | Informações, Testes, Desempenho, Uso e armazenamento | 11 |
| Utilidades | Geral | 3 |

**Recursos gerais do app** (não são ferramentas):

- **Busca e favoritos**, essenciais com mais de 120 ferramentas.
- **Ferramenta indisponível em cinza:** quando falta hardware, versão do Android ou suporte, a ferramenta aparece em cinza e, ao tocar, mostra o motivo (ex.: "Este aparelho não tem barômetro") em vez de abrir e falhar.
- **Filtro "só o que funciona aqui":** esconde as indisponíveis. Padrão: mostrar tudo, com as indisponíveis em cinza.
- **Atalhos e widgets:** blocos no painel rápido (`TileService`) e widgets para lanterna, bateria e velocidade de rede.
- **Exportar medições:** qualquer leitura com histórico pode virar CSV.

## Catálogo de ferramentas

São 128 ferramentas. A coluna "Seus aparelhos" diz se roda no S20 FE e no A24: **Os dois**, **Os dois (limitado)**, **Só A24**, **Verificar** (testar no aparelho) ou **Aparelho futuro** (falta hardware; fica em cinza). Dificuldade: F = fácil, M = média, D = difícil.

### Bateria

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Painel ao vivo: nível, carregando ou descarregando, fonte, temperatura, voltagem | Ao vivo | Os dois | F | `ACTION_BATTERY_CHANGED` |  |
| Corrente e potência instantâneas (mA e W) | Ao vivo | Os dois | F | `BATTERY_PROPERTY_CURRENT_NOW` | Sinal e unidade (µA ou mA) variam por fabricante: autodetectar |
| Tempo restante até encher ou esvaziar | Ao vivo | Os dois | F | `computeChargeTimeRemaining` + cálculo próprio | Oscila com o uso |
| Tecnologia e saúde básica | Saúde | Os dois | F | `EXTRA_TECHNOLOGY`, `EXTRA_HEALTH` | O sistema só diz "Li-ion"; Li-Po e capacidade de fábrica vêm de uma tabela interna por modelo |
| Capacidade estimada (mAh) contra a de fábrica | Saúde | Os dois | M | `BATTERY_PROPERTY_CHARGE_COUNTER` + nível | Confiável só numa carga longa (ex.: 20% a 100%) |
| Ciclos de carga | Saúde | Só A24 | F | `EXTRA_CYCLE_COUNT` (Android 14+) | Samsung pode não preencher: testar |
| Saúde % oficial (SoH) | Saúde | Só A24 | M | `BATTERY_PROPERTY_STATE_OF_HEALTH` (Android 14+) | Exige `BATTERY_STATS` via ADB ou Shizuku |
| Teste de consumo em standby | Testes | Os dois | M | Foreground service + `CHARGE_COUNTER` e `CURRENT_NOW` | Usuário escolhe a duração (ex.: 10 min) e apaga a tela; resultado: mA médio, %/hora, autonomia. Mede consumo, não saúde |
| Teste de carregador e cabo | Testes | Os dois | F | `CURRENT_NOW` + voltagem | Comparar na mesma faixa de nível |
| Histórico e gráficos de nível, temperatura e corrente | Histórico e alertas | Os dois | M | Serviço + Room | Intervalos que não gastem bateria |
| Alarmes (X%, carga completa, temperatura alta) | Histórico e alertas | Os dois | F | `BroadcastReceiver` |  |
| Consumo de bateria por app | Histórico e alertas | Os dois (limitado) | D | `UsageStatsManager` | Sem API pública; aproximar por tempo em primeiro plano |

### Internet

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Uso de dados por app (Wi-Fi e móvel, dia/semana/mês) | Uso de dados | Os dois | M | `NetworkStatsManager` + `PACKAGE_USAGE_STATS` | Números iguais aos do sistema, em blocos de tempo. A ferramenta principal no estilo GlassWire |
| Velocidade ao vivo na barra | Uso de dados | Os dois | F | `TrafficStats` |  |
| Franquia e alertas | Uso de dados | Os dois | F | `NetworkStatsManager` | Dia de reset do plano, aviso em X GB |
| Conexões por app em tempo real | Uso de dados | Os dois | D | `VpnService` local, sem servidor | Não roda junto com outra VPN. Base: PCAPdroid |
| Aviso de app novo usando rede | Uso de dados | Os dois | M | `NetworkStatsManager` periódico | Com atraso; na hora só com a VPN local |
| Info da conexão atual (SSID, canal, RSSI, IP, gateway, DNS) | Wi-Fi | Os dois | F | `WifiManager`, `ConnectivityManager` | SSID exige localização |
| Redes próximas e gráfico de canais | Wi-Fi | Os dois | M | `WifiManager.startScan` | Limite de 4 varreduras a cada 2 min |
| Melhor canal | Wi-Fi | Os dois | F | Dados da varredura |  |
| Caça ao ponto cego (sinal enquanto anda) | Wi-Fi | Os dois | F | RSSI |  |
| Distância até o roteador pelo sinal | Wi-Fi | Os dois (limitado) | F | RSSI | Bem impreciso |
| Compartilhar Wi-Fi por QR | Wi-Fi | Os dois | F | ZXing | Usuário digita a senha |
| Distância precisa por Wi-Fi RTT | Wi-Fi | Verificar | M | `WifiRttManager` | Precisa de suporte no celular e no roteador |
| Scanner de dispositivos na LAN | Diagnóstico | Os dois (limitado) | M | Ping sweep, mDNS, NetBIOS, SSDP | Sem tabela ARP desde o Android 10: MAC raramente aparece |
| Ping (latência, perda, jitter) | Diagnóstico | Os dois | F | `ping` do sistema / `InetAddress` |  |
| Traceroute | Diagnóstico | Os dois | M | Ping com TTL crescente |  |
| Scanner de portas | Diagnóstico | Os dois | F | Sockets TCP | Só em redes próprias |
| Consulta DNS | Diagnóstico | Os dois | F | dnsjava |  |
| Whois | Diagnóstico | Os dois | F | Socket na porta 43 | Precisa de internet |
| IP externo e provedor | Diagnóstico | Os dois | F | Base GeoIP offline ou API | Preferir base offline |
| Teste de velocidade | Diagnóstico | Os dois | M | Servidores LibreSpeed |  |
| Wake-on-LAN | Diagnóstico | Os dois | F | UDP broadcast |  |
| Descoberta de serviços (impressoras, TVs) | Diagnóstico | Os dois | M | `NsdManager`, SSDP |  |
| Calculadora de sub-rede | Diagnóstico | Os dois | F | Cálculo puro |  |
| Checagem de site e certificado | Diagnóstico | Os dois | F | `HttpsURLConnection` |  |
| Monitor de disponibilidade de hosts | Diagnóstico | Os dois | M | WorkManager | Mínimo de 15 min |

### Sinais

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Info da rede celular (4G/5G, banda, RSRP, RSRQ, SINR, torre) | Rede celular | Os dois | M | `TelephonyManager.getAllCellInfo` | Detalhe varia por chip |
| Log de sinal celular com GPS e CSV | Rede celular | Os dois | M | `CellInfo` + GPS |  |
| Scanner Bluetooth LE | Bluetooth | Os dois | M | `BluetoothLeScanner` |  |
| Achar dispositivo perdido (quente/frio) | Bluetooth | Os dois | F | RSSI do BLE |  |
| Info do Bluetooth (pareados, codec, bateria de fones) | Bluetooth | Os dois (limitado) | M | `BluetoothAdapter` | Bateria só se o fone informar |
| Controle remoto infravermelho | Outros rádios | Aparelho futuro | M | `ConsumerIrManager` | Precisa de base de códigos de controle |
| Localizador UWB | Outros rádios | Aparelho futuro | D | `UwbManager` | API restrita em muitos aparelhos |

### NFC

No A24 o NFC depende do mercado, por isso tudo aqui está como "Verificar". MIFARE Classic não funciona em muitos Galaxy por causa do controlador NFC: testar nos dois.

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Ler tag (UID, chip, tecnologias, ATQA/SAK, memória) | Ler | Verificar | F | `NfcAdapter`, `Tag` |  |
| Ler conteúdo NDEF | Ler | Verificar | F | `Ndef` |  |
| Leitor de cartão por aproximação (dados públicos) | Ler | Verificar | M | `IsoDep` (EMV) | Não guardar nada |
| Escrever NDEF (link, texto, contato, Wi-Fi, abrir app) | Gravar | Verificar | F | `Ndef.writeNdefMessage` |  |
| Apagar ou formatar tag | Gravar | Verificar | F | `Ndef`, `NdefFormatable` |  |
| Copiar tag (conteúdo, não o UID) | Gravar | Verificar | F | `Ndef` |  |
| Travar como somente leitura | Proteger | Verificar | F | `Ndef.makeReadOnly` | Irreversível: confirmação dupla |
| Senha em NTAG21x | Proteger | Verificar | M | `NfcA.transceive` (PWD\_AUTH) |  |
| Dump de memória em hex | Avançado | Verificar | M | `NfcA`, `MifareUltralight` |  |
| MIFARE Classic com chaves | Avançado | Verificar | M | `MifareClassic` | Base: MIFARE Classic Tool |
| Clonar UID para tag "mágica" | Avançado | Verificar | D | `MifareClassic` |  |
| Comandos brutos (APDU) | Avançado | Verificar | M | `IsoDep`, `NfcA.transceive` |  |
| Tag como gatilho de ação | Automação | Verificar | M | Intent filter NDEF | Ações de sistema limitadas sem root |

### Localização

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Status do GNSS (satélites, sinal, mapa do céu, precisão) | Posição | Os dois | M | `GnssStatus`, `GnssMeasurement` | Base: GPSTest |
| Coordenadas e altitude GPS | Posição | Os dois | F | `LocationManager` | Altitude erra 10 m ou mais |
| Altímetro barométrico | Posição | Aparelho futuro | F | `TYPE_PRESSURE` | Sem barômetro nos dois |
| GNSS de dupla frequência (L1/L5) | Posição | Verificar | M | `GnssMeasurement.getCarrierFrequencyHz` | Depende do chip |
| Bússola (norte magnético e verdadeiro) | Direção | Os dois | F | Rotation vector + `GeomagneticField` | Calibrar em 8 |
| Marcar e voltar ao ponto | Direção | Os dois | F | GPS + bússola |  |
| Velocímetro GPS | Trajeto | Os dois | F | `Location.getSpeed` | Céu aberto |
| Medidor de área | Trajeto | Os dois | M | `LocationManager` | Erro pesa em áreas pequenas |
| Odômetro e trajeto em GPX | Trajeto | Os dois | M | `LocationManager` |  |

### Medição

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Régua na tela (mm e polegadas) | Tela | Os dois | F | `DisplayMetrics` + calibração | Calibrar com cartão de 85,6 mm; componente `OdRegua` |
| Paquímetro de tela | Tela | Os dois | F | DPI calibrado |  |
| Transferidor | Tela | Os dois | F | Tela, câmera |  |
| Nível de bolha | Inclinação | Os dois | F | Sensor de gravidade | Calibrar o zero; componente `OdNivelBolha` |
| Inclinômetro pela câmera | Inclinação | Os dois | F | Rotation vector + câmera |  |
| Altura por trigonometria | Distância e altura | Os dois | M | Rotation vector | Precisa da distância ou da altura do aparelho |
| Distância pela câmera (objeto de tamanho conhecido) | Distância e altura | Os dois | M | CameraX + campo de visão |  |
| Trena em realidade aumentada | Distância e altura | Verificar | D | ARCore | Confirmar os dois na lista do ARCore |
| Trena por sensor de profundidade | Distância e altura | Aparelho futuro | M | Camera2 `DEPTH16` | Sem ToF nos dois |
| Scanner 3D | Distância e altura | Aparelho futuro | D | Profundidade ou ARCore Depth |  |

### Movimento

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Acelerômetro e medidor de G | Sensores | Os dois | F | `TYPE_ACCELEROMETER` | Gráfico `OdGraficoTempoReal` com eixos X, Y, Z |
| Vibrômetro / sismógrafo | Sensores | Os dois | M | Acelerômetro + FFT | Taxa do sensor limita as frequências |
| Velocidade por acelerômetro | Sensores | Os dois (limitado) | M | Acelerômetro | Erro acumula em segundos |
| Giroscópio / rotação (RPM) | Sensores | Os dois | F | `TYPE_GYROSCOPE` |  |
| Contador de andares | Sensores | Aparelho futuro | M | `TYPE_PRESSURE` |  |
| Pêndulo (medir a gravidade) | Experimentos | Os dois | F | Acelerômetro |  |

### Ambiente

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Detector de metais | Magnetismo | Os dois (limitado) | F | Magnetômetro (µT) | Só metais ferromagnéticos |
| Magnetômetro 3 eixos | Magnetismo | Os dois | F | `TYPE_MAGNETIC_FIELD` |  |
| Luxímetro | Luz | Os dois | F | `TYPE_LIGHT` | Calibrável |
| Detector de cintilação (flicker) | Luz | Os dois (limitado) | M | Câmera (rolling shutter) |  |
| Fotômetro (abertura, velocidade, ISO) | Luz | Os dois | F | Sensor de luz ou câmera |  |
| Barômetro | Clima | Aparelho futuro | F | `TYPE_PRESSURE` |  |
| Tendência do tempo pela pressão | Clima | Aparelho futuro | M | `TYPE_PRESSURE` + histórico |  |
| Termômetro ambiente | Clima | Aparelho futuro | F | `TYPE_AMBIENT_TEMPERATURE` | Raro hoje |
| Higrômetro e ponto de orvalho | Clima | Aparelho futuro | F | `TYPE_RELATIVE_HUMIDITY` | Raro hoje |

### Som

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Decibelímetro dB(A)/dB(Z) | Medir | Os dois | M | `AudioRecord` (fonte `UNPROCESSED`) | Calibrável; não é instrumento certificado |
| Analisador de espectro e espectrograma | Medir | Os dois | M | `AudioRecord` + FFT |  |
| Cronômetro acústico | Medir | Os dois | F | `AudioRecord` |  |
| Afinador (nota e cents) | Música | Os dois | M | Pitch YIN |  |
| Metrônomo | Música | Os dois | F | `AudioTrack` |  |
| Detector de BPM | Música | Os dois | M | `AudioRecord` |  |
| Gerador de tom e varredura | Gerar | Os dois | F | `AudioTrack` |  |
| Gerador de ruído (branco, rosa, marrom) | Gerar | Os dois | F | `AudioTrack` |  |
| Teste de alto-falantes | Testes | Os dois (limitado) | F | `AudioTrack` | A24 é mono |
| Sonar por eco | Experimentos | Os dois (limitado) | D | Alto-falante + microfone | Experimental |

### Câmera

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Leitor de código de barras e QR | Códigos | Os dois | F | ZXing-C++ | Base: Binary Eye |
| Gerador de códigos | Códigos | Os dois | F | ZXing |  |
| Seletor de cores (HEX, RGB, nome) | Imagem | Os dois | F | CameraX |  |
| Lupa | Imagem | Os dois | F | CameraX |  |
| Câmera térmica | Imagem | Aparelho futuro | D | SDK do fabricante | Sem API padrão |
| Scanner de documentos (PDF) | Documentos | Os dois | M | OpenCV |  |
| OCR | Documentos | Os dois | M | Tesseract |  |
| Estroboscópio / tacômetro | Flash | Os dois (limitado) | M | `CameraManager.setTorchMode` |  |
| Lanterna avançada (intensidade, SOS, Morse) | Flash | Os dois | F | `setTorchStrengthLevel` |  |

### Saúde

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Frequência cardíaca pela câmera (PPG) | Coração | Os dois | M | Câmera + flash | Não é aparelho médico; base: Lens HRV |
| Frequência cardíaca por sensor dedicado | Coração | Aparelho futuro | F | `TYPE_HEART_RATE` |  |
| Pedômetro | Atividade | Os dois | F | `TYPE_STEP_COUNTER` |  |
| Contador de repetições | Atividade | Os dois | M | Acelerômetro |  |

### Aparelho

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Informações do hardware | Informações | Os dois | F | `Build`, `ActivityManager` | Base: CPU Info |
| Lista e teste de sensores | Informações | Os dois | F | `SensorManager` |  |
| Teste de tela (pixels, cores, burn-in) | Testes | Os dois | F | Tela cheia |  |
| Teste de toque multitoque | Testes | Os dois | F | `MotionEvent` |  |
| Teste de vibração | Testes | Os dois | F | `VibratorManager` |  |
| Teste de botões e proximidade | Testes | Os dois | F | `KeyEvent`, `TYPE_PROXIMITY` | Proximidade pode ser virtual |
| Temperaturas | Desempenho | Os dois (limitado) | F | Bateria, `getThermalHeadroom` | CPU não é exposta |
| Monitor de RAM | Desempenho | Os dois (limitado) | F | `ActivityManager` |  |
| Tempo de tela por app | Uso e armazenamento | Os dois | F | `UsageStatsManager` |  |
| Analisador de armazenamento | Uso e armazenamento | Os dois | M | `MANAGE_EXTERNAL_STORAGE` |  |
| Gerenciador de apps | Uso e armazenamento | Os dois | M | `PackageManager` |  |

### Utilidades

| Ferramenta | Seção | Seus aparelhos | Dif. | API / sensor | Observação |
| --- | --- | --- | --- | --- | --- |
| Contador (toque ou volume) | Geral | Os dois | F |  |  |
| Cronômetro e timer | Geral | Os dois | F |  |  |
| Conversor de unidades | Geral | Os dois | F |  |  |

## Hardware dos aparelhos

Os dois têm acelerômetro, giroscópio, magnetômetro e sensor de luz, e nenhum tem barômetro, infravermelho, UWB ou sensor de profundidade. A maior diferença para o app é a versão do Android.

| Item | Galaxy S20 FE | Galaxy A24 | O que muda no app |
| --- | --- | --- | --- |
| Chip | Exynos 990 ou Snapdragon 865, conforme a versão | MediaTek Helio G99 | Detalhes da rede celular variam |
| Último Android | 13 (One UI 5.1) | 15 ou mais novo | Ciclos e saúde oficial da bateria só no A24 |
| Tela | 6,5", 1080x2400, 120 Hz, \~407 ppi | 6,5", 1080x2340, 90 Hz | Régua usa o DPI real + calibração |
| Bateria | 4500 mAh Li-Po (mínima 4370) | 5000 mAh Li-Po | Base da capacidade estimada |
| Acelerômetro, giroscópio, magnetômetro, luz | Sim | Sim |  |
| Proximidade | Sim (pode ser virtual) | Virtual |  |
| Barômetro | Não | Não | Ferramentas de pressão ficam em cinza |
| NFC | Sim | Depende do mercado: verificar | Grupo NFC inteiro |
| Infravermelho, UWB, ToF | Não | Não | Ferramentas para aparelhos futuros |
| GNSS | GPS, GLONASS, BeiDou, Galileo | GPS, GLONASS, Galileo, BeiDou, QZSS |  |
| Wi-Fi | Wi-Fi 6, dual band | Wi-Fi 5, dual band |  |
| Bluetooth | 5.0 | 5.3 |  |
| Alto-falante | Estéreo | Mono | Teste de estéreo só faz sentido no S20 FE |
| USB | USB-C 3.2 | USB-C 2.0 |  |

**Limitações do Android que valem para os dois:**

- Varredura de Wi-Fi limitada a 4 a cada 2 minutos em primeiro plano.
- Tabela ARP bloqueada desde o Android 10: o scanner de rede quase nunca vê MAC e fabricante.
- Doze espaça leituras com a tela apagada; medições longas usam foreground service.
- A VPN local (conexões em tempo real) ocupa a vaga de VPN do sistema.
- Corrente da bateria tem sinal e unidade diferentes por fabricante; o app precisa autodetectar.

O que testar primeiro nos aparelhos reais: NFC no A24, MIFARE Classic nos dois, ciclos de carga no A24, Wi-Fi RTT, GNSS de dupla frequência e ARCore.

## Design: Open Design no Compose

O visual vem inteiro do módulo `opendesign` do Open Design: nenhuma cor, tamanho ou fonte é escrita à mão no app. As regras completas estão em `code/open-design/README.md` e `code/open-design/android/README.md`, que o Claude Code deve ler antes de começar.

**Como ligar o tema:**

1. Copiar `code/open-design/android/opendesign/` para `Projeto Ferramentas/opendesign/`.
2. `include(":opendesign")` no `settings.gradle.kts` e `implementation(project(":opendesign"))` no `app/build.gradle.kts`, com os plugins `com.android.library`, `org.jetbrains.kotlin.android` e `org.jetbrains.kotlin.plugin.compose` na raiz.
3. Compilar o módulo e corrigir o que o compilador apontar **sem mudar o visual**. O código dele foi escrito para Compose 1.9 e conferido com Kotlin 2.2, mas ainda não foi compilado contra as bibliotecas do Android.
4. Na Activity: `enableEdgeToEdge()` e o app dentro de `OpenDesignTema(prefs)`, com `rememberOdPreferencias()`.
5. Cores, tipografia, densidade e sombras sempre por `OdTema.cores`, `OdTema.tipografia`, `OdTema.densidade` e `OdTema.sombras`.

**Componentes do Open Design que o app usa:**

| Necessidade do app | Componente |
| --- | --- |
| Barra inferior (Início, Favoritos, Buscar, Mais) | `OdBarraInferior`, `OdDestino` |
| Estrutura de cada tela (título, Voltar, ações, toasts) | `OdTelaApp` |
| Grade de grupos e painéis de leituras | `OdGrade` |
| Número ao vivo com unidade, mínimo e máximo | `OdCartaoLeitura`, `OdLeitura`, `odFormatar` |
| Indisponível neste aparelho, precisa de permissão, medindo | `OdEstadoRecurso`, `OdChipEstado`, `OdPontoAoVivo` |
| Gráficos de sensores (eixos X, Y, Z) | `OdGraficoTempoReal` com `odSerieX`, `odSerieY`, `odSerieZ` |
| Régua, nível, bússola, mostradores | `OdRegua`, `OdNivelBolha`, `OdBussola`, `OdEscalaLinear`, `OdEscalaArco`, `OdInstrumento` |
| Configurações (Aparência primeiro, depois as do app, Sobre no fim) | `OdTelaConfiguracoes`, `OdGrupoConfig` e linhas |
| Confirmações destrutivas (ex.: travar tag NFC) | `OdDialogo(destrutivo = true)` |
| Opções de uma ferramenta | `OdFolhaInferior`, `OdSegmentado`, `OdInterruptor` |
| Botões e avisos rápidos | `OdBotao`, `OdBotaoIcone`, `rememberOdToasts()` |

**Regras que o Claude Code deve seguir:**

- Componente que não existe: criar no app com prefixo próprio (ex.: `Ft`), usando só os tokens do `OdTema`.
- Ícones novos: `python3 ../open-design/android/ferramentas/gerar_icones.py Nome=nome-tabler`, sempre do Tabler, traço por padrão.
- Cores dinâmicas do Android desligadas; "Sistema" fica disponível só como opção de cor principal nas configurações.
- Tema claro, escuro ou sistema só se troca em Configurações > Aparência.
- Estado nunca só por cor: sempre ícone e texto.
- Área de toque de pelo menos 48 dp.
- Escrita: botões dizem o que acontece ("Iniciar teste", "Gravar na tag"), maiúscula só no início da frase, toasts curtos no passado ("Medição salva."), erros dizem como resolver, sem emoji.
- Testar em modo claro e escuro, com fonte grande e com TalkBack.

Exemplo de tudo junto: `code/open-design/android/exemplo/MainActivity.kt`, que já é um app de ferramentas de exemplo.

## Referências open source

Como o app é GPL-3.0, dá para reaproveitar código de todos os projetos abertos abaixo (GPL-3.0, Apache-2.0 e MIT são compatíveis com GPL-3.0), mantendo os avisos de copyright e listando-os na tela Sobre. Antes de copiar código de um projeto marcado "ver repositório", conferir a licença.

| Projeto | Usar como base para | Licença |
| --- | --- | --- |
| [PCAPdroid](https://github.com/emanuele-f/PCAPdroid) | Conexões por app em tempo real (VPN local) | GPL-3.0+ |
| [Traffic Light](https://f-droid.org/packages/com.leekleak.trafficlight/) | Uso de dados por app | GPL-3.0 |
| [Data Monitor](https://f-droid.org/packages/com.drnoob.datamonitor/) | Uso de dados, franquia, velocidade ao vivo | Ver repositório |
| [WiFiAnalyzer](https://github.com/VREMSoftwareDevelopment/WifiAnalyzer) | Redes próximas, canais, melhor canal | GPL-3.0 |
| [Vernet](https://github.com/git-elliot/vernet) | Scanner de LAN e portas (alternativa aberta ao Network Analyzer) | Ver repositório |
| [Android Network Tools](https://github.com/stealthcopter/AndroidNetworkTools) | Ping, portas, descoberta, Wake-on-LAN (biblioteca) | Apache-2.0 |
| [Network Survey](https://github.com/christianrowlands/android-network-survey) | Rede celular, Bluetooth, GNSS, logs | Apache-2.0 |
| [GPSTest](https://github.com/barbeau/gpstest) | Status do GNSS | Apache-2.0 |
| [Spectre](https://f-droid.org/packages/dev.thomasbuilds.spectre/) | Wi-Fi, BLE, GNSS e Wi-Fi RTT num app só | GPL-3.0 |
| [MIFARE Classic Tool](https://github.com/ikarus23/MifareClassicTool) | NFC: MIFARE Classic, dump, clonagem | GPL-3.0 |
| [phyphox](https://phyphox.org) | Sensores em geral, sonar, estroboscópio, exportação | GPL-3.0 |
| [Nonius](https://f-droid.org/packages/dev.kaleve.nonius/) | Régua calibrada, nível, bússola, decibelímetro, afinador, luxímetro | GPL-3.0+ |
| [Audio Spectrum Analyzer](https://github.com/woheller69/audio-analyzer-for-android) | FFT e espectrograma | Apache-2.0 |
| [Binary Eye](https://github.com/markusfisch/BinaryEye) | Leitor e gerador de códigos | MIT |
| [Lens HRV](https://f-droid.org/packages/com.lenshrv.foss/) | Frequência cardíaca pela câmera | GPL-3.0 |
| [CPU Info](https://github.com/kamgurgul/cpu-info) | Hardware, sensores, apps | Apache-2.0 |
| [Battery Monitor](https://f-droid.org/packages/codes.swistak.batterymonitor/) | Bateria ao vivo, dados via Shizuku | GPL-3.0+ |
| [Beam](https://github.com/montafra/beam) | Corrente e potência, correção de unidade | MIT |

Network Analyzer (Techet), NFC Tools e GlassWire são fechados: servem só como lista de recursos para imitar, sem copiar código.

## Como começar no Claude Code

Exporte este documento como Markdown, salve como `BRIEFING.md` dentro de `Projeto Ferramentas/`, abra o terminal nessa pasta, rode `claude` e cole o pedido abaixo.

```markdown
Vamos iniciar o repositório do Projeto Ferramentas, um app Android nativo de ferramentas.
Tudo o que foi decidido está em BRIEFING.md: leia inteiro antes de qualquer coisa.

O design system Open Design está um nível acima, em ../open-design/ (confirme o nome
exato da pasta descompactada em ../ antes de seguir). Leia ../open-design/README.md e
../open-design/android/README.md. Não edite nada dentro de ../open-design/.

Faça nesta ordem, e me mostre o plano antes de escrever código:
1. git init, .gitignore de Android, LICENSE com o texto da GPL-3.0 e um README curto.
2. CLAUDE.md com o resumo do BRIEFING.md e as regras de design e de código.
3. Projeto Gradle com os módulos :app e :opendesign (cópia de
   ../open-design/android/opendesign/), minSdk 33, Compose 1.9+. Compile e corrija o
   módulo opendesign sem mudar o visual.
4. Esqueleto: OpenDesignTema, barra inferior (Início, Favoritos, Buscar, Mais), grade
   dos 13 grupos, tela de grupo por seção, busca, favoritos e Configurações com
   Aparência primeiro.
5. Catálogo central com todas as ferramentas do BRIEFING.md, cada uma com grupo,
   seção, ícone e requisitos (hardware, sensor, versão do Android, permissões).
   Ferramentas ainda não feitas abrem uma tela "Em construção"; as indisponíveis no
   aparelho aparecem em cinza com o motivo.

Pare aí e me diga como testar no celular. Pergunte só quando uma decisão não estiver
no BRIEFING.md.
```

**Ordem de construção depois do esqueleto:**

1. **Primeiras ferramentas**, que validam as APIs mais arriscadas nos dois aparelhos: painel ao vivo da bateria, corrente e potência, teste de consumo em standby, uso de dados por app e lista de sensores.
2. **Testes de hardware:** NFC no A24, MIFARE Classic, ciclos de carga, Wi-Fi RTT, dupla frequência e ARCore. O resultado atualiza a coluna "Seus aparelhos" do catálogo.
3. **Grupo por grupo**, na ordem das ferramentas marcadas como "Sim" na planilha.

**Em aberto:**

- Quais ferramentas entram: preencher a coluna "Quero?" da [planilha](https://claude.ai/artifact/HbCYz9WVuTd3XTSbask3Kg). Enquanto não estiver preenchida, o catálogo traz todas.
- Distribuição: GitHub Releases, F-Droid ou Play Store (esta última restringe `QUERY_ALL_PACKAGES` e `MANAGE_EXTERNAL_STORAGE`).
- `applicationId` do app, por exemplo `io.github.andrexagris.ferramentas`.
