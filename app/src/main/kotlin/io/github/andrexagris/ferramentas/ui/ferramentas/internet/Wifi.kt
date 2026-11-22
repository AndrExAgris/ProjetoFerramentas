package io.github.andrexagris.ferramentas.ui.ferramentas.internet

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.andrexagris.ferramentas.base.Serie
import io.github.andrexagris.ferramentas.base.bipe
import io.github.andrexagris.ferramentas.base.compartilharImagem
import io.github.andrexagris.ferramentas.base.gerarCodigo
import io.github.andrexagris.ferramentas.base.infoRede
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.textoQrWifi
import io.github.andrexagris.ferramentas.catalogo.Permissao
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEscalaArco
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdFaixa
import io.github.andrexagris.opendesign.OdGraficoTempoReal
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdInstrumento
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdLinhaInterruptor
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.OdTipoFaixa
import io.github.andrexagris.opendesign.odFormatar
import io.github.andrexagris.opendesign.odSerieResultante
import kotlinx.coroutines.delay
import kotlin.math.log10
import kotlin.math.pow

fun telasWifi(): Map<String, TelaDeFerramenta> = mapOf(
    "internet.wifi-info" com { InfoWifi() },
    "internet.redes-proximas" com { RedesProximas() },
    "internet.melhor-canal" com { MelhorCanal() },
    "internet.ponto-cego".com(listOf(Permissao.Localizacao)) { PontoCego() },
    "internet.distancia-roteador".com(listOf(Permissao.Localizacao)) { DistanciaRoteador() },
    "internet.wifi-qr" com { WifiQr() },
    "internet.wifi-rtt" com { WifiRtt() },
)

fun canalDe(freqMhz: Int): Int = when {
    freqMhz == 2484 -> 14
    freqMhz in 2412..2472 -> (freqMhz - 2407) / 5
    freqMhz in 5000..5900 -> (freqMhz - 5000) / 5
    freqMhz in 5925..7125 -> (freqMhz - 5950) / 5
    else -> 0
}

fun bandaDe(freqMhz: Int): String = when { freqMhz < 3000 -> "2,4 GHz"; freqMhz < 5925 -> "5 GHz"; else -> "6 GHz" }

private fun padraoWifi(p: Int): String? = when (p) {
    ScanResult.WIFI_STANDARD_LEGACY -> "Wi-Fi 1–3 (a/b/g)"
    ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4 (n)"
    ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5 (ac)"
    ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6 (ax)"
    ScanResult.WIFI_STANDARD_11BE -> "Wi-Fi 7 (be)"
    else -> null
}

private fun seguranca(caps: String): String = when {
    "WPA3" in caps || "SAE" in caps -> "WPA3"
    "WPA2" in caps || "RSN" in caps -> "WPA2"
    "WPA" in caps -> "WPA"
    "WEP" in caps -> "WEP"
    else -> "Aberta"
}

/** Distância estimada pela perda no espaço livre (FSPL): d = 10^((27,55 − 20·log10(f) + |RSSI| − margem) / 20). */
fun distanciaRssi(rssi: Int, freqMhz: Int, potenciaRoteadorDbm: Double = 20.0): Double =
    10.0.pow((27.55 - 20 * log10(freqMhz.toDouble()) + (potenciaRoteadorDbm - rssi) - 20.0) / 20.0)

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
@Composable
fun rememberWifiInfo(intervaloMs: Long = 1000): WifiInfo? {
    val c = LocalContext.current
    val wm = remember { c.applicationContext.getSystemService(WifiManager::class.java) }
    val info by produceState<WifiInfo?>(null) { while (true) { value = wm.connectionInfo; delay(intervaloMs) } }
    return info?.takeIf { it.networkId != -1 || it.rssi > -127 && it.bssid != null && it.bssid != "02:00:00:00:00:00" }
}

@Composable
private fun InfoWifi() {
    val c = LocalContext.current
    val info = rememberWifiInfo()
    val serie = remember { Serie(120) }
    LaunchedEffect(info?.rssi) { info?.rssi?.let { serie.add(it.toFloat()) } }
    val wm = remember { c.applicationContext.getSystemService(WifiManager::class.java) }
    if (!wm.isWifiEnabled) { OdEstadoRecurso(OdTipoEstado.Indisponivel, "Wi-Fi desligado", "Ligue o Wi-Fi e conecte a uma rede."); return }
    if (info == null) { OdEstadoRecurso(OdTipoEstado.Indisponivel, "Sem conexão Wi-Fi", "Conecte a uma rede para ver os detalhes."); return }
    val rede = c.infoRede()
    FtColuna {
        FtPainel(titulo = info.ssid.trim('"').takeIf { it != WifiManager.UNKNOWN_SSID } ?: "Rede Wi-Fi") {
            OdLeitura(info.rssi.toString(), unidade = "dBm", tamanho = OdTamanhoLeitura.Destaque)
            OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "RSSI")), -100f, -20f, janela = 120, unidade = "dBm", casas = 0, rotuloTempo = "−2 min" to "agora")
        }
        FtInfos("Conexão", listOf(
            "BSSID" to info.bssid,
            "Frequência" to "${info.frequency} MHz (${bandaDe(info.frequency)})",
            "Canal" to canalDe(info.frequency).toString(),
            "Padrão" to padraoWifi(info.wifiStandard),
            "Velocidade do link" to "↓ ${info.rxLinkSpeedMbps} / ↑ ${info.txLinkSpeedMbps} Mb/s",
            "Segurança" to (if (Build.VERSION.SDK_INT >= 31) segurancaAtual(info.currentSecurityType) else null),
        ))
        FtInfos("Rede", listOf(
            "IP" to rede?.ipv4?.hostAddress?.let { "$it/${rede.prefixo}" },
            "Gateway" to rede?.gateway?.hostAddress,
            "DNS" to rede?.dns?.joinToString { it.hostAddress ?: "" }?.ifBlank { null },
            "IPv6" to rede?.ipv6?.firstOrNull()?.hostAddress,
            "MTU" to rede?.mtu?.takeIf { it > 0 }?.toString(),
        ))
    }
}

private fun segurancaAtual(t: Int): String = when (t) {
    WifiInfo.SECURITY_TYPE_OPEN -> "Aberta"; WifiInfo.SECURITY_TYPE_WEP -> "WEP"; WifiInfo.SECURITY_TYPE_PSK -> "WPA2"
    WifiInfo.SECURITY_TYPE_SAE -> "WPA3"; WifiInfo.SECURITY_TYPE_EAP -> "Empresarial (EAP)"; WifiInfo.SECURITY_TYPE_OWE -> "Aberta melhorada (OWE)"
    else -> "Outra"
}

/** Varredura de redes Wi-Fi (o Android limita a 4 a cada 2 minutos). */
@SuppressLint("MissingPermission")
@Composable
fun rememberVarreduraWifi(): Pair<List<ScanResult>, () -> Boolean> {
    val c = LocalContext.current
    val wm = remember { c.applicationContext.getSystemService(WifiManager::class.java) }
    var resultados by remember { mutableStateOf(runCatching { wm.scanResults }.getOrDefault(emptyList())) }
    DisposableEffect(Unit) {
        val r = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, i: Intent) { resultados = runCatching { wm.scanResults }.getOrDefault(emptyList()) }
        }
        c.registerReceiver(r, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        @Suppress("DEPRECATION") wm.startScan()
        onDispose { c.unregisterReceiver(r) }
    }
    @Suppress("DEPRECATION")
    return resultados to { wm.startScan() }
}

@Composable
private fun RedesProximas() {
    val (redes, varrer) = rememberVarreduraWifi()
    var banda by rememberSaveable { mutableStateOf("2,4 GHz") }
    val toasts = io.github.andrexagris.ferramentas.ui.ferramentas.LocalToasts.current
    val daBanda = redes.filter { bandaDe(it.frequency) == banda }.sortedByDescending { it.level }
    FtColuna {
        OdSegmentado(listOf("2,4 GHz" to "2,4 GHz", "5 GHz" to "5 GHz", "6 GHz" to "6 GHz"), banda, { banda = it })
        FtPainel(titulo = "Canais") { GraficoCanais(daBanda, banda) }
        OdBotao("Procurar de novo", { if (!varrer()) toasts.mostrar("O Android limita a 4 buscas a cada 2 minutos. Tente daqui a pouco.", io.github.andrexagris.opendesign.OdTipoToast.Aviso) }, Modifier.fillMaxWidth())
        OdGrupoConfig("${daBanda.size} redes") {
            daBanda.forEach { r ->
                OdLinhaConfig(r.wifiSsid?.toString()?.trim('"')?.ifBlank { null } ?: "Rede oculta",
                    descricao = listOfNotNull("canal ${canalDe(r.frequency)}", "${larguraCanal(r)} MHz", seguranca(r.capabilities), padraoWifi(r.wifiStandard), r.BSSID).joinToString(" · ")) {
                    androidx.compose.material3.Text("${r.level} dBm", style = OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum"))
                }
            }
        }
    }
}

fun larguraCanal(r: ScanResult): Int = when (r.channelWidth) {
    ScanResult.CHANNEL_WIDTH_40MHZ -> 40; ScanResult.CHANNEL_WIDTH_80MHZ -> 80; ScanResult.CHANNEL_WIDTH_160MHZ, ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> 160
    ScanResult.CHANNEL_WIDTH_320MHZ -> 320; else -> 20
}

/** Gráfico de canais: cada rede é uma curva centrada no canal, larga como a faixa ocupada, alta como o sinal. */
@Composable
private fun GraficoCanais(redes: List<ScanResult>, banda: String) {
    val c = OdTema.cores
    val medidor = rememberTextMeasurer()
    val estilo = OdTema.tipografia.uiPequeno.copy(fontSize = 10.sp, color = c.graficoRotulo)
    val (fMin, fMax) = when (banda) { "2,4 GHz" -> 2400 to 2490; "5 GHz" -> 5150 to 5850; else -> 5925 to 7125 }
    val cores = listOf(c.secundaria.cor, c.graficoX, c.graficoY, c.graficoZ, c.primaria.cor, c.info.cor)
    Canvas(Modifier.fillMaxWidth().height(180.dp).semantics { contentDescription = "Gráfico de canais com ${redes.size} redes" }) {
        val base = size.height - 16.dp.toPx()
        for (db in listOf(-90, -70, -50, -30)) {
            val y = base - (db + 100) / 70f * base
            drawLine(c.graficoGrade, Offset(0f, y), Offset(size.width, y), 1f)
        }
        val canais = if (banda == "2,4 GHz") (1..13).toList() else (fMin..fMax step 20).map { canalDe(it + 10) }
        canais.forEach { ch ->
            val f = if (banda == "2,4 GHz") 2407 + ch * 5 else if (banda == "5 GHz") 5000 + ch * 5 else 5950 + ch * 5
            val x = (f - fMin).toFloat() / (fMax - fMin) * size.width
            val t = medidor.measure(ch.toString(), estilo)
            if (banda == "2,4 GHz" || ch % 2 == 0) drawText(t, topLeft = Offset(x - t.size.width / 2, size.height - t.size.height))
        }
        redes.forEachIndexed { i, r ->
            val centro = if (r.centerFreq0 > 0 && larguraCanal(r) > 20) r.centerFreq0 else r.frequency
            val meia = larguraCanal(r) / 2f
            val x0 = (centro - meia - fMin) / (fMax - fMin) * size.width
            val x1 = (centro + meia - fMin) / (fMax - fMin) * size.width
            val topo = base - ((r.level + 100) / 70f).coerceIn(0.02f, 1f) * base
            val p = Path().apply { moveTo(x0, base); cubicTo(x0, topo, x1, topo, x1, base) }
            drawPath(p, cores[i % cores.size], style = Stroke(1.5.dp.toPx()))
        }
    }
}

@Composable
private fun MelhorCanal() {
    val (redes, _) = rememberVarreduraWifi()
    // 2,4 GHz: só 1, 6 e 11 não se sobrepõem. Pontuação = soma da potência (mW) das redes que invadem ±20 MHz.
    val pontos24 = listOf(1, 6, 11).associateWith { ch ->
        val f = 2407 + ch * 5
        redes.filter { it.frequency < 3000 }.sumOf { r ->
            val sobrepoe = kotlin.math.abs(r.frequency - f) < 20 + (larguraCanal(r) - 20) / 2
            if (sobrepoe) 10.0.pow(r.level / 10.0) else 0.0
        }
    }
    val melhor24 = pontos24.minByOrNull { it.value }?.key
    val canais5 = listOf(36, 40, 44, 48, 52, 56, 60, 64, 100, 104, 108, 112, 116, 120, 124, 128, 132, 136, 140, 149, 153, 157, 161, 165)
    val ocupacao5 = canais5.associateWith { ch -> redes.count { it.frequency in 5000..5900 && kotlin.math.abs(canalDe(it.frequency) - ch) < larguraCanal(it) / 10 } }
    val melhor5 = ocupacao5.minByOrNull { it.value }?.key
    FtColuna {
        FtPainel(titulo = "Melhor canal em 2,4 GHz") { OdLeitura(melhor24?.toString() ?: "—", unidade = "canal", tamanho = OdTamanhoLeitura.Destaque) }
        FtInfos("Ocupação em 2,4 GHz", pontos24.map { (ch, mw) -> "Canal $ch" to (if (mw == 0.0) "Livre" else "${num(10 * log10(mw), 0)} dBm somados") })
        FtPainel(titulo = "Melhor canal em 5 GHz") { OdLeitura(melhor5?.toString() ?: "—", unidade = "canal", tamanho = OdTamanhoLeitura.Destaque) }
        FtInfos("Redes por canal em 5 GHz", ocupacao5.filter { it.value > 0 }.map { (ch, n) -> "Canal $ch" to "$n" })
        FtNota("Em 2,4 GHz use só 1, 6 ou 11. Em 5 GHz, os canais de 52 a 144 (DFS) podem demorar a ligar no roteador. Troque o canal nas configurações do roteador.")
    }
}

@Composable
private fun PontoCego() {
    val info = rememberWifiInfo(500)
    var som by rememberSaveable { mutableStateOf(false) }
    val serie = remember { Serie(240) }
    LaunchedEffect(info?.rssi) { info?.rssi?.let { serie.add(it.toFloat()) } }
    LaunchedEffect(som) { while (som) { info?.rssi?.let { bipe(400.0 + (it + 100) * 15.0, 40) }; delay(700) } }
    io.github.andrexagris.ferramentas.base.ManterTelaLigada()
    if (info == null) { OdEstadoRecurso(OdTipoEstado.Indisponivel, "Sem conexão Wi-Fi", "Conecte a uma rede e ande pela casa com o celular."); return }
    val rssi = info.rssi.toFloat()
    val estado = when { rssi > -55 -> "Ótimo"; rssi > -67 -> "Bom"; rssi > -75 -> "Fraco"; else -> "Ponto cego" }
    FtColuna {
        OdInstrumento(estado, rssi > -67, leitura = { OdLeitura(odFormatar(rssi.toDouble(), 0), unidade = "dBm", tamanho = OdTamanhoLeitura.Destaque, largura = 4) }) {
            OdEscalaArco(rssi, -95f, -30f, faixas = listOf(OdFaixa(-67f, -30f, OdTipoFaixa.Ok), OdFaixa(-80f, -67f, OdTipoFaixa.Atencao)))
        }
        OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "RSSI")), -100f, -20f, janela = 240, unidade = "dBm", casas = 0, rotuloTempo = "−2 min" to "agora")
        OdGrupoConfig(null) { OdLinhaInterruptor("Som do sinal", som, { som = it }, "Mais agudo com sinal mais forte") }
        FtInfos(null, listOf("Mínimo" to serie.pontos.minOrNull()?.let { "${it.toInt()} dBm" }, "Máximo" to serie.pontos.maxOrNull()?.let { "${it.toInt()} dBm" }))
        FtNota("Ande devagar pela casa olhando o número. Abaixo de −75 dBm, chamadas de vídeo e jogos começam a falhar.")
    }
}

@Composable
private fun DistanciaRoteador() {
    val info = rememberWifiInfo()
    var suave by remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(info?.rssi) { info?.rssi?.let { r -> suave = suave?.let { it * 0.8 + r * 0.2 } ?: r.toDouble() } }
    if (info == null) { OdEstadoRecurso(OdTipoEstado.Indisponivel, "Sem conexão Wi-Fi", "Conecte a uma rede para estimar a distância."); return }
    val d = distanciaRssi(suave?.toInt() ?: info.rssi, info.frequency)
    FtColuna {
        FtPainel(titulo = "Distância estimada") { OdLeitura(num(d, 1), unidade = "m", tamanho = OdTamanhoLeitura.Destaque) }
        FtInfos(null, listOf("Sinal (média)" to suave?.let { "${num(it, 0)} dBm" }, "Frequência" to "${info.frequency} MHz"))
        FtNota("Bem impreciso: paredes, pessoas e a antena do roteador mudam muito o sinal. Serve para saber se está chegando mais perto ou se afastando.")
    }
}

@Composable
private fun WifiQr() {
    val c = LocalContext.current
    val info = rememberWifiInfo(5000)
    var ssid by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(info?.ssid) { if (ssid.isEmpty()) info?.ssid?.trim('"')?.takeIf { it != WifiManager.UNKNOWN_SSID.trim('"', '<', '>') && !it.startsWith("<") }?.let { ssid = it } }
    var senha by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf("WPA") }
    var oculta by rememberSaveable { mutableStateOf(false) }
    val texto = textoQrWifi(ssid, senha, tipo, oculta)
    val bmp = remember(texto) { runCatching { gerarCodigo(texto) }.getOrNull() }
    FtColuna {
        FtCampo(ssid, { ssid = it }, "Nome da rede (SSID)")
        OdSegmentado(listOf("WPA" to "WPA/WPA3", "WEP" to "WEP", "nopass" to "Aberta"), tipo, { tipo = it })
        if (tipo != "nopass") FtCampo(senha, { senha = it }, "Senha")
        OdGrupoConfig(null) { OdLinhaInterruptor("Rede oculta", oculta, { oculta = it }) }
        if (bmp != null && ssid.isNotBlank()) {
            FtPainel { Image(bmp.asImageBitmap(), "QR Code da rede $ssid", Modifier.fillMaxWidth().aspectRatio(1f)) }
            OdBotao("Compartilhar imagem", { compartilharImagem(c, "wifi-$ssid.png", bmp) }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario)
        }
        FtNota("Aponte a câmera de outro celular para o código para conectar sem digitar. O Android não deixa apps lerem a senha salva, por isso ela é digitada aqui.")
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun WifiRtt() {
    val c = LocalContext.current
    val rtt = remember { c.getSystemService(WifiRttManager::class.java) }
    val (redes, _) = rememberVarreduraWifi()
    val compativeis = redes.filter { it.is80211mcResponder }
    var resultados by remember { mutableStateOf<List<RangingResult>>(emptyList()) }
    LaunchedEffect(compativeis.map { it.BSSID }) {
        while (compativeis.isNotEmpty() && rtt?.isAvailable == true) {
            val pedido = RangingRequest.Builder().addAccessPoints(compativeis.take(RangingRequest.getMaxPeers())).build()
            runCatching {
                rtt.startRanging(pedido, c.mainExecutor, object : RangingResultCallback() {
                    override fun onRangingResults(r: MutableList<RangingResult>) { resultados = r }
                    override fun onRangingFailure(code: Int) {}
                })
            }
            delay(1000)
        }
    }
    FtColuna {
        if (rtt?.isAvailable != true) OdEstadoRecurso(OdTipoEstado.Indisponivel, "Wi-Fi RTT desligado", "Ligue o Wi-Fi e a localização do aparelho.")
        else if (compativeis.isEmpty()) OdEstadoRecurso(OdTipoEstado.Indisponivel, "Nenhum roteador compatível", "O roteador precisa ter 802.11mc (FTM). A maioria dos roteadores domésticos ainda não tem.")
        else OdGrupoConfig("Roteadores compatíveis") {
            compativeis.forEach { ap ->
                val r = resultados.firstOrNull { it.macAddress?.toString().equals(ap.BSSID, true) && it.status == RangingResult.STATUS_SUCCESS }
                OdLinhaConfig(ap.wifiSsid?.toString()?.trim('"') ?: ap.BSSID, descricao = r?.let { "± ${num(it.distanceStdDevMm / 1000.0, 2)} m" } ?: "Medindo…") {
                    androidx.compose.material3.Text(r?.let { "${num(it.distanceMm / 1000.0, 2)} m" } ?: "—", style = OdTema.tipografia.uiSubtitulo)
                }
            }
        }
        FtNota("O Wi-Fi RTT mede o tempo de ida e volta do sinal: com celular e roteador compatíveis, a precisão chega a 1 ou 2 metros.")
    }
}

