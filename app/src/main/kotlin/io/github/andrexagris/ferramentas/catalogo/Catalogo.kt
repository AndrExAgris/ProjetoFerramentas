package io.github.andrexagris.ferramentas.catalogo

import androidx.compose.ui.graphics.vector.ImageVector
import io.github.andrexagris.ferramentas.catalogo.Dificuldade.Dificil
import io.github.andrexagris.ferramentas.catalogo.Dificuldade.Facil
import io.github.andrexagris.ferramentas.catalogo.Dificuldade.Media
import io.github.andrexagris.ferramentas.catalogo.SeusAparelhos.AparelhoFuturo
import io.github.andrexagris.ferramentas.catalogo.SeusAparelhos.OsDois
import io.github.andrexagris.ferramentas.catalogo.SeusAparelhos.OsDoisLimitado
import io.github.andrexagris.ferramentas.catalogo.SeusAparelhos.SoA24
import io.github.andrexagris.ferramentas.catalogo.SeusAparelhos.Verificar
import io.github.andrexagris.ferramentas.ui.FtIcones
import io.github.andrexagris.opendesign.OdIcones

/**
 * Catálogo central: as 128 ferramentas do README, na mesma ordem. Alimenta a grade de grupos, as listas por
 * seção, a busca, os favoritos e a disponibilidade. Quando a coluna "Quero?" da planilha for preenchida,
 * as que ficarem de fora saem daqui.
 */
object Catalogo {
    val ferramentas: List<Ferramenta> = buildList {
        grupo(Grupo.Bateria) {
            f("bateria.painel", "Painel ao vivo", "Nível, carregando ou descarregando, fonte, temperatura e voltagem", "Ao vivo", FtIcones.BatteryCharging, OsDois, Facil, "ACTION_BATTERY_CHANGED")
            f("bateria.corrente", "Corrente e potência", "Corrente e potência instantâneas, em mA e W", "Ao vivo", FtIcones.Bolt, OsDois, Facil, "BATTERY_PROPERTY_CURRENT_NOW",
                "Sinal e unidade (µA ou mA) variam por fabricante: autodetectar")
            f("bateria.tempo-restante", "Tempo restante", "Até encher ou esvaziar", "Ao vivo", FtIcones.Hourglass, OsDois, Facil, "computeChargeTimeRemaining + cálculo próprio",
                "Oscila com o uso")
            f("bateria.tecnologia", "Tecnologia e saúde básica", null, "Saúde", FtIcones.Battery2, OsDois, Facil, "EXTRA_TECHNOLOGY, EXTRA_HEALTH",
                "O sistema só diz \"Li-ion\"; Li-Po e capacidade de fábrica vêm de uma tabela interna por modelo")
            f("bateria.capacidade", "Capacidade estimada", "mAh atuais contra os de fábrica", "Saúde", OdIcones.Medidor, OsDois, Media, "BATTERY_PROPERTY_CHARGE_COUNTER + nível",
                "Confiável só numa carga longa (ex.: 20% a 100%)")
            f("bateria.ciclos", "Ciclos de carga", null, "Saúde", FtIcones.Refresh, SoA24, Facil, "EXTRA_CYCLE_COUNT (Android 14+)",
                "Samsung pode não preencher: testar", requisitos = listOf(Req.Android14))
            f("bateria.saude-oficial", "Saúde oficial (SoH)", "Percentual oficial de saúde da bateria", "Saúde", FtIcones.HeartRateMonitor, SoA24, Media, "BATTERY_PROPERTY_STATE_OF_HEALTH (Android 14+)",
                "Exige BATTERY_STATS via ADB ou Shizuku", requisitos = listOf(Req.Android14), permissoes = listOf(Permissao.EstatisticasBateria))
            f("bateria.standby", "Teste de consumo em standby", "mA médio, %/hora e autonomia com a tela apagada", "Testes", FtIcones.Moon, OsDois, Media, "Foreground service + CHARGE_COUNTER e CURRENT_NOW",
                "Você escolhe a duração (ex.: 10 min) e apaga a tela. Mede consumo, não saúde", permissoes = listOf(Permissao.Notificacoes))
            f("bateria.carregador", "Teste de carregador e cabo", null, "Testes", FtIcones.PlugConnected, OsDois, Facil, "CURRENT_NOW + voltagem",
                "Comparar na mesma faixa de nível")
            f("bateria.historico", "Histórico e gráficos", "Nível, temperatura e corrente ao longo do tempo", "Histórico e alertas", FtIcones.ChartLine, OsDois, Media, "Serviço + Room",
                "Intervalos que não gastem bateria")
            f("bateria.alarmes", "Alarmes", "Nível escolhido, carga completa e temperatura alta", "Histórico e alertas", FtIcones.Alarm, OsDois, Facil, "BroadcastReceiver",
                permissoes = listOf(Permissao.Notificacoes))
            f("bateria.por-app", "Consumo por app", null, "Histórico e alertas", FtIcones.Apps, OsDoisLimitado, Dificil, "UsageStatsManager",
                "Sem API pública; aproximado pelo tempo em primeiro plano", permissoes = listOf(Permissao.UsoDoApp))
        }

        grupo(Grupo.Internet) {
            val wifiScan = listOf(Permissao.Localizacao, Permissao.WifiProximo)
            f("internet.uso-por-app", "Uso de dados por app", "Wi-Fi e móvel, por dia, semana e mês", "Uso de dados", FtIcones.ChartPie, OsDois, Media, "NetworkStatsManager + PACKAGE_USAGE_STATS",
                "Números iguais aos do sistema, em blocos de tempo", permissoes = listOf(Permissao.UsoDoApp))
            f("internet.velocidade-barra", "Velocidade ao vivo na barra", null, "Uso de dados", FtIcones.ArrowsDownUp, OsDois, Facil, "TrafficStats",
                permissoes = listOf(Permissao.Notificacoes))
            f("internet.franquia", "Franquia e alertas", "Dia de reset do plano e aviso ao chegar no limite", "Uso de dados", FtIcones.ChartBar, OsDois, Facil, "NetworkStatsManager",
                permissoes = listOf(Permissao.UsoDoApp, Permissao.Notificacoes))
            f("internet.conexoes", "Conexões por app em tempo real", null, "Uso de dados", FtIcones.Network, OsDois, Dificil, "VpnService local, sem servidor",
                "Não roda junto com outra VPN. Base: PCAPdroid", permissoes = listOf(Permissao.Vpn))
            f("internet.app-novo", "Aviso de app novo usando rede", null, "Uso de dados", OdIcones.Notificacoes, OsDois, Media, "NetworkStatsManager periódico",
                "Com atraso; na hora só com a VPN local", permissoes = listOf(Permissao.UsoDoApp, Permissao.Notificacoes))
            f("internet.wifi-info", "Info da conexão Wi-Fi", "SSID, canal, RSSI, IP, gateway e DNS", "Wi-Fi", OdIcones.Wifi, OsDois, Facil, "WifiManager, ConnectivityManager",
                "O nome da rede (SSID) exige localização", listOf(Req.Wifi), listOf(Permissao.Localizacao))
            f("internet.redes-proximas", "Redes próximas e gráfico de canais", null, "Wi-Fi", FtIcones.Radar, OsDois, Media, "WifiManager.startScan",
                "Limite de 4 varreduras a cada 2 min", listOf(Req.Wifi), wifiScan)
            f("internet.melhor-canal", "Melhor canal", null, "Wi-Fi", FtIcones.TargetArrow, OsDois, Facil, "Dados da varredura", requisitos = listOf(Req.Wifi), permissoes = wifiScan)
            f("internet.ponto-cego", "Caça ao ponto cego", "Sinal do Wi-Fi enquanto você anda", "Wi-Fi", FtIcones.MapPinSearch, OsDois, Facil, "RSSI", requisitos = listOf(Req.Wifi))
            f("internet.distancia-roteador", "Distância até o roteador pelo sinal", null, "Wi-Fi", FtIcones.Router, OsDoisLimitado, Facil, "RSSI", "Bem impreciso", listOf(Req.Wifi))
            f("internet.wifi-qr", "Compartilhar Wi-Fi por QR", null, "Wi-Fi", FtIcones.Qrcode, OsDois, Facil, "ZXing", "Você digita a senha")
            f("internet.wifi-rtt", "Distância precisa por Wi-Fi RTT", null, "Wi-Fi", FtIcones.Ruler2, Verificar, Media, "WifiRttManager",
                "Precisa de suporte no celular e no roteador", listOf(Req.WifiRtt), wifiScan)
            f("internet.lan", "Scanner de dispositivos na LAN", null, "Diagnóstico", FtIcones.DevicesSearch, OsDoisLimitado, Media, "Ping sweep, mDNS, NetBIOS, SSDP",
                "Sem tabela ARP desde o Android 10: o MAC raramente aparece")
            f("internet.ping", "Ping", "Latência, perda e jitter", "Diagnóstico", FtIcones.ActivityHeartbeat, OsDois, Facil, "ping do sistema / InetAddress")
            f("internet.traceroute", "Traceroute", null, "Diagnóstico", FtIcones.Route2, OsDois, Media, "Ping com TTL crescente")
            f("internet.portas", "Scanner de portas", null, "Diagnóstico", FtIcones.LockOpen, OsDois, Facil, "Sockets TCP", "Só em redes próprias")
            f("internet.dns", "Consulta DNS", null, "Diagnóstico", FtIcones.WorldSearch, OsDois, Facil, "dnsjava", internet = true)
            f("internet.whois", "Whois", null, "Diagnóstico", FtIcones.IdBadge, OsDois, Facil, "Socket na porta 43", internet = true)
            f("internet.ip-externo", "IP externo e provedor", null, "Diagnóstico", FtIcones.World, OsDois, Facil, "Base GeoIP offline ou API", "Preferir base offline", internet = true)
            f("internet.velocidade", "Teste de velocidade", null, "Diagnóstico", FtIcones.BrandSpeedtest, OsDois, Media, "Servidores LibreSpeed", internet = true)
            f("internet.wake-on-lan", "Wake-on-LAN", null, "Diagnóstico", FtIcones.Power, OsDois, Facil, "UDP broadcast")
            f("internet.servicos", "Descoberta de serviços", "Impressoras, TVs e outros aparelhos da rede", "Diagnóstico", FtIcones.Printer, OsDois, Media, "NsdManager, SSDP")
            f("internet.sub-rede", "Calculadora de sub-rede", null, "Diagnóstico", FtIcones.Calculator, OsDois, Facil, "Cálculo puro")
            f("internet.site", "Checagem de site e certificado", null, "Diagnóstico", FtIcones.Certificate, OsDois, Facil, "HttpsURLConnection", internet = true)
            f("internet.monitor-hosts", "Monitor de disponibilidade de hosts", null, "Diagnóstico", FtIcones.ServerCog, OsDois, Media, "WorkManager",
                "Verifica no mínimo a cada 15 min", permissoes = listOf(Permissao.Notificacoes))
        }

        grupo(Grupo.Sinais) {
            f("sinais.celular", "Info da rede celular", "4G/5G, banda, RSRP, RSRQ, SINR e torre", "Rede celular", FtIcones.AntennaBars5, OsDois, Media, "TelephonyManager.getAllCellInfo",
                "Detalhe varia por chip", listOf(Req.Telefonia), listOf(Permissao.Localizacao))
            f("sinais.log-celular", "Log de sinal celular", "Com posição do GPS e exportação em CSV", "Rede celular", FtIcones.FileSpreadsheet, OsDois, Media, "CellInfo + GPS",
                requisitos = listOf(Req.Telefonia, Req.Gps), permissoes = listOf(Permissao.Localizacao))
            f("sinais.ble", "Scanner Bluetooth LE", null, "Bluetooth", FtIcones.Bluetooth, OsDois, Media, "BluetoothLeScanner",
                requisitos = listOf(Req.BluetoothLe), permissoes = listOf(Permissao.Bluetooth))
            f("sinais.achar", "Achar dispositivo perdido", "Quente ou frio pelo sinal do Bluetooth", "Bluetooth", FtIcones.BluetoothConnected, OsDois, Facil, "RSSI do BLE",
                requisitos = listOf(Req.BluetoothLe), permissoes = listOf(Permissao.Bluetooth))
            f("sinais.bluetooth-info", "Info do Bluetooth", "Pareados, codec e bateria de fones", "Bluetooth", FtIcones.DeviceAirpods, OsDoisLimitado, Media, "BluetoothAdapter",
                "Bateria só se o fone informar", listOf(Req.Bluetooth), listOf(Permissao.Bluetooth))
            f("sinais.infravermelho", "Controle remoto infravermelho", null, "Outros rádios", FtIcones.DeviceRemote, AparelhoFuturo, Media, "ConsumerIrManager",
                "Precisa de base de códigos de controle", listOf(Req.Infravermelho), listOf(Permissao.Infravermelho))
            f("sinais.uwb", "Localizador UWB", null, "Outros rádios", FtIcones.Ripple, AparelhoFuturo, Dificil, "UwbManager",
                "API restrita em muitos aparelhos", listOf(Req.Uwb), listOf(Permissao.Uwb))
        }

        grupo(Grupo.Nfc) {
            val nfc = listOf(Req.Nfc)
            val mifare = listOf(Req.Nfc, Req.MifareClassic)
            val obsA24 = "No A24 o NFC depende do mercado"
            f("nfc.ler-tag", "Ler tag", "UID, chip, tecnologias, ATQA/SAK e memória", "Ler", FtIcones.Nfc, Verificar, Facil, "NfcAdapter, Tag", obsA24, nfc)
            f("nfc.ndef", "Ler conteúdo NDEF", null, "Ler", FtIcones.FileText, Verificar, Facil, "Ndef", obsA24, nfc)
            f("nfc.cartao", "Leitor de cartão por aproximação", "Só os dados públicos", "Ler", FtIcones.CreditCard, Verificar, Media, "IsoDep (EMV)", "Não guarda nada", nfc)
            f("nfc.escrever", "Escrever NDEF", "Link, texto, contato, Wi-Fi ou abrir app", "Gravar", FtIcones.Writing, Verificar, Facil, "Ndef.writeNdefMessage", obsA24, nfc)
            f("nfc.apagar", "Apagar ou formatar tag", null, "Gravar", FtIcones.Eraser, Verificar, Facil, "Ndef, NdefFormatable", obsA24, nfc)
            f("nfc.copiar", "Copiar tag", "Copia o conteúdo, não o UID", "Gravar", FtIcones.Copy, Verificar, Facil, "Ndef", obsA24, nfc)
            f("nfc.travar", "Travar como somente leitura", null, "Proteger", OdIcones.Cadeado, Verificar, Facil, "Ndef.makeReadOnly", "Irreversível: confirmação dupla", nfc)
            f("nfc.senha", "Senha em NTAG21x", null, "Proteger", FtIcones.Key, Verificar, Media, "NfcA.transceive (PWD_AUTH)", obsA24, nfc)
            f("nfc.dump", "Dump de memória em hex", null, "Avançado", FtIcones.Binary, Verificar, Media, "NfcA, MifareUltralight", obsA24, nfc)
            f("nfc.mifare", "MIFARE Classic com chaves", null, "Avançado", FtIcones.Cards, Verificar, Media, "MifareClassic",
                "Não funciona em muitos Galaxy por causa do controlador NFC. Base: MIFARE Classic Tool", mifare)
            f("nfc.clonar-uid", "Clonar UID para tag \"mágica\"", null, "Avançado", FtIcones.Fingerprint, Verificar, Dificil, "MifareClassic", null, mifare)
            f("nfc.apdu", "Comandos brutos (APDU)", null, "Avançado", FtIcones.Terminal2, Verificar, Media, "IsoDep, NfcA.transceive", obsA24, nfc)
            f("nfc.gatilho", "Tag como gatilho de ação", null, "Automação", FtIcones.HandClick, Verificar, Media, "Intent filter NDEF", "Ações de sistema limitadas sem root", nfc)
        }

        grupo(Grupo.Localizacao) {
            val local = listOf(Permissao.Localizacao)
            f("localizacao.gnss", "Status do GNSS", "Satélites, sinal, mapa do céu e precisão", "Posição", FtIcones.Satellite, OsDois, Media, "GnssStatus, GnssMeasurement",
                "Base: GPSTest", listOf(Req.Gps), local)
            f("localizacao.coordenadas", "Coordenadas e altitude", null, "Posição", FtIcones.MapPin, OsDois, Facil, "LocationManager", "A altitude erra 10 m ou mais", listOf(Req.Gps), local)
            f("localizacao.altimetro", "Altímetro barométrico", null, "Posição", FtIcones.Mountain, AparelhoFuturo, Facil, "TYPE_PRESSURE", requisitos = listOf(Req.Pressao))
            f("localizacao.dupla-frequencia", "GNSS de dupla frequência (L1/L5)", null, "Posição", FtIcones.Antenna, Verificar, Media, "GnssMeasurement.getCarrierFrequencyHz",
                "Depende do chip", listOf(Req.Gps), local)
            f("localizacao.bussola", "Bússola", "Norte magnético e verdadeiro", "Direção", FtIcones.Compass, OsDois, Facil, "Rotation vector + GeomagneticField",
                "Calibrar movendo em 8", listOf(Req.Magnetometro, Req.Acelerometro))
            f("localizacao.voltar-ponto", "Marcar e voltar ao ponto", null, "Direção", FtIcones.Flag, OsDois, Facil, "GPS + bússola",
                requisitos = listOf(Req.Gps, Req.Magnetometro), permissoes = local)
            f("localizacao.velocimetro", "Velocímetro GPS", null, "Trajeto", FtIcones.Gauge, OsDois, Facil, "Location.getSpeed", "Funciona melhor a céu aberto", listOf(Req.Gps), local)
            f("localizacao.area", "Medidor de área", null, "Trajeto", FtIcones.Polygon, OsDois, Media, "LocationManager", "O erro pesa em áreas pequenas", listOf(Req.Gps), local)
            f("localizacao.trajeto", "Odômetro e trajeto em GPX", null, "Trajeto", FtIcones.Route, OsDois, Media, "LocationManager", requisitos = listOf(Req.Gps), permissoes = local)
        }

        grupo(Grupo.Medicao) {
            val cam = listOf(Permissao.Camera)
            f("medicao.regua", "Régua na tela", "Milímetros e polegadas", "Tela", FtIcones.Ruler, OsDois, Facil, "DisplayMetrics + calibração", "Calibrar com um cartão de 85,6 mm")
            f("medicao.paquimetro", "Paquímetro de tela", null, "Tela", FtIcones.Ruler3, OsDois, Facil, "DPI calibrado")
            f("medicao.transferidor", "Transferidor", null, "Tela", FtIcones.Angle, OsDois, Facil, "Tela, câmera")
            f("medicao.nivel", "Nível de bolha", null, "Inclinação", FtIcones.Bubble, OsDois, Facil, "Sensor de gravidade", "Calibrar o zero", listOf(Req.Acelerometro))
            f("medicao.inclinometro", "Inclinômetro pela câmera", null, "Inclinação", FtIcones.CameraRotate, OsDois, Facil, "Rotation vector + câmera",
                requisitos = listOf(Req.Acelerometro, Req.Camera), permissoes = cam)
            f("medicao.altura", "Altura por trigonometria", null, "Distância e altura", FtIcones.BuildingSkyscraper, OsDois, Media, "Rotation vector",
                "Precisa da distância ou da altura do aparelho", listOf(Req.Acelerometro))
            f("medicao.distancia-camera", "Distância pela câmera", "Usando um objeto de tamanho conhecido", "Distância e altura", FtIcones.RulerMeasure2, OsDois, Media, "CameraX + campo de visão",
                requisitos = listOf(Req.Camera), permissoes = cam)
            f("medicao.trena-ar", "Trena em realidade aumentada", null, "Distância e altura", FtIcones.AugmentedReality, Verificar, Dificil, "ARCore",
                "Confirmar os dois aparelhos na lista do ARCore", listOf(Req.Camera, Req.ArCore), cam)
            f("medicao.trena-profundidade", "Trena por sensor de profundidade", null, "Distância e altura", FtIcones.Scan, AparelhoFuturo, Media, "Camera2 DEPTH16",
                requisitos = listOf(Requisito.CameraProfundidade), permissoes = cam)
            f("medicao.scanner-3d", "Scanner 3D", null, "Distância e altura", FtIcones.Cube3dSphere, AparelhoFuturo, Dificil, "Profundidade ou ARCore Depth",
                requisitos = listOf(Requisito.CameraProfundidade), permissoes = cam)
        }

        grupo(Grupo.Movimento) {
            f("movimento.acelerometro", "Acelerômetro e medidor de G", null, "Sensores", FtIcones.ArrowsMove, OsDois, Facil, "TYPE_ACCELEROMETER",
                "Gráfico com eixos X, Y e Z", listOf(Req.Acelerometro))
            f("movimento.vibrometro", "Vibrômetro e sismógrafo", null, "Sensores", FtIcones.WaveSine, OsDois, Media, "Acelerômetro + FFT",
                "A taxa do sensor limita as frequências", listOf(Req.Acelerometro), listOf(Permissao.AltaTaxa))
            f("movimento.velocidade", "Velocidade por acelerômetro", null, "Sensores", FtIcones.Run, OsDoisLimitado, Media, "Acelerômetro", "O erro acumula em segundos", listOf(Req.Acelerometro))
            f("movimento.giroscopio", "Giroscópio e rotação (RPM)", null, "Sensores", FtIcones.RotateClockwise2, OsDois, Facil, "TYPE_GYROSCOPE", requisitos = listOf(Req.Giroscopio))
            f("movimento.andares", "Contador de andares", null, "Sensores", FtIcones.StairsUp, AparelhoFuturo, Media, "TYPE_PRESSURE", requisitos = listOf(Req.Pressao))
            f("movimento.pendulo", "Pêndulo", "Medir a gravidade", "Experimentos", FtIcones.ClockPlay, OsDois, Facil, "Acelerômetro", requisitos = listOf(Req.Acelerometro))
        }

        grupo(Grupo.Ambiente) {
            f("ambiente.metais", "Detector de metais", null, "Magnetismo", FtIcones.Magnet, OsDoisLimitado, Facil, "Magnetômetro (µT)", "Só metais ferromagnéticos", listOf(Req.Magnetometro))
            f("ambiente.magnetometro", "Magnetômetro 3 eixos", null, "Magnetismo", FtIcones.CircleDashed, OsDois, Facil, "TYPE_MAGNETIC_FIELD", requisitos = listOf(Req.Magnetometro))
            f("ambiente.luximetro", "Luxímetro", null, "Luz", FtIcones.SunHigh, OsDois, Facil, "TYPE_LIGHT", "Calibrável", listOf(Req.Luz))
            f("ambiente.flicker", "Detector de cintilação (flicker)", null, "Luz", FtIcones.Haze, OsDoisLimitado, Media, "Câmera (rolling shutter)",
                requisitos = listOf(Req.Camera), permissoes = listOf(Permissao.Camera))
            f("ambiente.fotometro", "Fotômetro", "Abertura, velocidade e ISO", "Luz", FtIcones.Aperture, OsDois, Facil, "Sensor de luz ou câmera", requisitos = listOf(Req.Luz))
            f("ambiente.barometro", "Barômetro", null, "Clima", FtIcones.Gauge, AparelhoFuturo, Facil, "TYPE_PRESSURE", requisitos = listOf(Req.Pressao))
            f("ambiente.tendencia", "Tendência do tempo pela pressão", null, "Clima", FtIcones.CloudRain, AparelhoFuturo, Media, "TYPE_PRESSURE + histórico", requisitos = listOf(Req.Pressao))
            f("ambiente.termometro", "Termômetro ambiente", null, "Clima", FtIcones.Temperature, AparelhoFuturo, Facil, "TYPE_AMBIENT_TEMPERATURE", "Raro hoje", listOf(Req.TemperaturaAmbiente))
            f("ambiente.higrometro", "Higrômetro e ponto de orvalho", null, "Clima", FtIcones.Droplet, AparelhoFuturo, Facil, "TYPE_RELATIVE_HUMIDITY", "Raro hoje", listOf(Req.Umidade))
        }

        grupo(Grupo.Som) {
            val mic = listOf(Req.Microfone)
            val permMic = listOf(Permissao.Microfone)
            f("som.decibelimetro", "Decibelímetro", "dB(A) e dB(Z)", "Medir", FtIcones.Ear, OsDois, Media, "AudioRecord (fonte UNPROCESSED)",
                "Calibrável; não é instrumento certificado", mic, permMic)
            f("som.espectro", "Analisador de espectro e espectrograma", null, "Medir", FtIcones.Waveform, OsDois, Media, "AudioRecord + FFT", requisitos = mic, permissoes = permMic)
            f("som.cronometro-acustico", "Cronômetro acústico", null, "Medir", FtIcones.Stopwatch, OsDois, Facil, "AudioRecord", requisitos = mic, permissoes = permMic)
            f("som.afinador", "Afinador", "Nota e cents", "Música", FtIcones.GuitarPick, OsDois, Media, "Pitch YIN", requisitos = mic, permissoes = permMic)
            f("som.metronomo", "Metrônomo", null, "Música", FtIcones.Metronome, OsDois, Facil, "AudioTrack")
            f("som.bpm", "Detector de BPM", null, "Música", FtIcones.Music, OsDois, Media, "AudioRecord", requisitos = mic, permissoes = permMic)
            f("som.tom", "Gerador de tom e varredura", null, "Gerar", FtIcones.WaveSquare, OsDois, Facil, "AudioTrack")
            f("som.ruido", "Gerador de ruído", "Branco, rosa e marrom", "Gerar", FtIcones.Volume, OsDois, Facil, "AudioTrack")
            f("som.alto-falantes", "Teste de alto-falantes", null, "Testes", FtIcones.Speakerphone, OsDoisLimitado, Facil, "AudioTrack", "O A24 é mono")
            f("som.sonar", "Sonar por eco", null, "Experimentos", FtIcones.Radar2, OsDoisLimitado, Dificil, "Alto-falante + microfone", "Experimental", mic, permMic)
        }

        grupo(Grupo.Camera) {
            val cam = listOf(Req.Camera)
            val permCam = listOf(Permissao.Camera)
            f("camera.leitor", "Leitor de código de barras e QR", null, "Códigos", FtIcones.Barcode, OsDois, Facil, "ZXing-C++", "Base: Binary Eye", cam, permCam)
            f("camera.gerador", "Gerador de códigos", null, "Códigos", FtIcones.Qrcode, OsDois, Facil, "ZXing")
            f("camera.cores", "Seletor de cores", "HEX, RGB e nome da cor", "Imagem", FtIcones.ColorPicker, OsDois, Facil, "CameraX", requisitos = cam, permissoes = permCam)
            f("camera.lupa", "Lupa", null, "Imagem", FtIcones.ZoomIn, OsDois, Facil, "CameraX", requisitos = cam, permissoes = permCam)
            f("camera.termica", "Câmera térmica", null, "Imagem", FtIcones.Flame, AparelhoFuturo, Dificil, "SDK do fabricante", "Sem API padrão no Android",
                listOf(Requisito.SemApiPadrao("Este aparelho não tem câmera térmica.")))
            f("camera.documentos", "Scanner de documentos (PDF)", null, "Documentos", FtIcones.FileTypePdf, OsDois, Media, "OpenCV", requisitos = cam, permissoes = permCam)
            f("camera.ocr", "OCR", "Reconhecer texto em fotos", "Documentos", FtIcones.TextRecognition, OsDois, Media, "Tesseract", requisitos = cam, permissoes = permCam)
            f("camera.estroboscopio", "Estroboscópio e tacômetro", null, "Flash", FtIcones.Bolt, OsDoisLimitado, Media, "CameraManager.setTorchMode", requisitos = listOf(Req.Flash))
            f("camera.lanterna", "Lanterna avançada", "Intensidade, SOS e Morse", "Flash", FtIcones.Bulb, OsDois, Facil, "setTorchStrengthLevel", requisitos = listOf(Req.Flash))
        }

        grupo(Grupo.Saude) {
            f("saude.frequencia-camera", "Frequência cardíaca pela câmera", "Dedo sobre a câmera e o flash (PPG)", "Coração", FtIcones.Heartbeat, OsDois, Media, "Câmera + flash",
                "Não é aparelho médico. Base: Lens HRV", listOf(Req.Camera, Req.Flash), listOf(Permissao.Camera))
            f("saude.frequencia-sensor", "Frequência cardíaca por sensor dedicado", null, "Coração", FtIcones.Heart, AparelhoFuturo, Facil, "TYPE_HEART_RATE",
                requisitos = listOf(Req.FrequenciaCardiaca), permissoes = listOf(Permissao.SensoresCorporais))
            f("saude.pedometro", "Pedômetro", null, "Atividade", FtIcones.Walk, OsDois, Facil, "TYPE_STEP_COUNTER",
                requisitos = listOf(Req.Passos), permissoes = listOf(Permissao.Atividade))
            f("saude.repeticoes", "Contador de repetições", null, "Atividade", FtIcones.Barbell, OsDois, Media, "Acelerômetro", requisitos = listOf(Req.Acelerometro))
        }

        grupo(Grupo.Aparelho) {
            f("aparelho.hardware", "Informações do hardware", null, "Informações", FtIcones.Cpu, OsDois, Facil, "Build, ActivityManager", "Base: CPU Info")
            f("aparelho.sensores", "Lista e teste de sensores", null, "Informações", FtIcones.ListDetails, OsDois, Facil, "SensorManager")
            f("aparelho.tela", "Teste de tela", "Pixels, cores e burn-in", "Testes", FtIcones.DeviceMobile, OsDois, Facil, "Tela cheia")
            f("aparelho.toque", "Teste de toque multitoque", null, "Testes", FtIcones.HandFinger, OsDois, Facil, "MotionEvent")
            f("aparelho.vibracao", "Teste de vibração", null, "Testes", FtIcones.DeviceMobileVibration, OsDois, Facil, "VibratorManager", requisitos = listOf(Requisito.Vibrador))
            f("aparelho.botoes", "Teste de botões e proximidade", null, "Testes", FtIcones.HandClick, OsDois, Facil, "KeyEvent, TYPE_PROXIMITY", "A proximidade pode ser virtual")
            f("aparelho.temperaturas", "Temperaturas", null, "Desempenho", FtIcones.Thermometer, OsDoisLimitado, Facil, "Bateria, getThermalHeadroom", "A CPU não é exposta")
            f("aparelho.ram", "Monitor de RAM", null, "Desempenho", FtIcones.Database, OsDoisLimitado, Facil, "ActivityManager")
            f("aparelho.tempo-de-tela", "Tempo de tela por app", null, "Uso e armazenamento", FtIcones.ClockHour3, OsDois, Facil, "UsageStatsManager",
                permissoes = listOf(Permissao.UsoDoApp))
            f("aparelho.armazenamento", "Analisador de armazenamento", null, "Uso e armazenamento", FtIcones.Folders, OsDois, Media, "MANAGE_EXTERNAL_STORAGE",
                permissoes = listOf(Permissao.Armazenamento))
            f("aparelho.apps", "Gerenciador de apps", null, "Uso e armazenamento", FtIcones.Package, OsDois, Media, "PackageManager",
                permissoes = listOf(Permissao.ListarApps))
        }

        grupo(Grupo.Utilidades) {
            f("utilidades.contador", "Contador", "Por toque ou pelos botões de volume", "Geral", FtIcones.Tallymarks, OsDois, Facil, "Toque e KeyEvent")
            f("utilidades.cronometro", "Cronômetro e timer", null, "Geral", FtIcones.Stopwatch, OsDois, Facil, "SystemClock")
            f("utilidades.conversor", "Conversor de unidades", null, "Geral", FtIcones.ArrowsExchange, OsDois, Facil, "Cálculo puro")
        }
    }

    private val porId: Map<String, Ferramenta> = ferramentas.associateBy { it.id }

    fun porId(id: String?): Ferramenta? = porId[id]

    fun doGrupo(grupo: Grupo): List<Ferramenta> = ferramentas.filter { it.grupo == grupo }

    init {
        require(porId.size == ferramentas.size) { "Há ids repetidos no catálogo." }
        ferramentas.forEach { require(it.secao in it.grupo.secoes) { "Seção \"${it.secao}\" não existe no grupo ${it.grupo.nome} (${it.id})." } }
    }
}

private class ConstrutorGrupo(private val grupo: Grupo, private val destino: MutableList<Ferramenta>) {
    fun f(
        id: String,
        nome: String,
        descricao: String?,
        secao: String,
        icone: ImageVector,
        aparelhos: SeusAparelhos,
        dificuldade: Dificuldade,
        api: String,
        observacao: String? = null,
        requisitos: List<Requisito> = emptyList(),
        permissoes: List<Permissao> = emptyList(),
        internet: Boolean = false,
    ) {
        destino += Ferramenta(id, grupo, secao, nome, descricao, icone, aparelhos, dificuldade, api, observacao, requisitos, permissoes, internet)
    }
}

private fun MutableList<Ferramenta>.grupo(grupo: Grupo, bloco: ConstrutorGrupo.() -> Unit) {
    ConstrutorGrupo(grupo, this).bloco()
}
