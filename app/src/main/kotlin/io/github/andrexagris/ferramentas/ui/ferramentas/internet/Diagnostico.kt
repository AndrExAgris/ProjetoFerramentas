package io.github.andrexagris.ferramentas.ui.ferramentas.internet

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.andrexagris.ferramentas.base.PORTAS_COMUNS
import io.github.andrexagris.ferramentas.base.Serie
import io.github.andrexagris.ferramentas.base.copiarTexto
import io.github.andrexagris.ferramentas.base.formatarDataHora
import io.github.andrexagris.ferramentas.base.formatarVelocidade
import io.github.andrexagris.ferramentas.base.infoRede
import io.github.andrexagris.ferramentas.base.ipv4ParaInt
import io.github.andrexagris.ferramentas.base.mascaraDe
import io.github.andrexagris.ferramentas.base.nomeReverso
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.paraInt
import io.github.andrexagris.ferramentas.base.paraIpv4
import io.github.andrexagris.ferramentas.base.pingUmaVez
import io.github.andrexagris.ferramentas.base.rememberComPermissao
import io.github.andrexagris.ferramentas.base.resolver
import io.github.andrexagris.ferramentas.base.tcpAberta
import io.github.andrexagris.ferramentas.catalogo.Permissao
import io.github.andrexagris.ferramentas.dados.BancoDados
import io.github.andrexagris.ferramentas.dados.ItemSalvo
import io.github.andrexagris.ferramentas.dados.Tipos
import io.github.andrexagris.ferramentas.dados.rememberAjusteBool
import io.github.andrexagris.ferramentas.servicos.ChavesPeriodicas
import io.github.andrexagris.ferramentas.servicos.TrabalhoPeriodico
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotaoMedir
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotoes
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.LocalToasts
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.ferramentas.ui.ferramentas.numero
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdGraficoTempoReal
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaAcao
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdLinhaInterruptor
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.OdTipoToast
import io.github.andrexagris.opendesign.odSerieResultante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.xbill.DNS.Lookup
import org.xbill.DNS.SimpleResolver
import org.xbill.DNS.Type
import java.io.InputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.Socket
import java.net.URL
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.HttpsURLConnection
import kotlin.math.abs
import kotlin.random.Random

fun telasDiagnostico(): Map<String, TelaDeFerramenta> = mapOf(
    "internet.lan" com { ScannerLan() },
    "internet.ping" com { PingTela() },
    "internet.traceroute" com { Traceroute() },
    "internet.portas" com { ScannerPortas() },
    "internet.dns" com { ConsultaDns() },
    "internet.whois" com { Whois() },
    "internet.ip-externo" com { IpExterno() },
    "internet.velocidade" com { TesteVelocidade() },
    "internet.wake-on-lan" com { WakeOnLan() },
    "internet.servicos" com { DescobertaServicos() },
    "internet.sub-rede" com { CalculadoraSubRede() },
    "internet.site" com { ChecagemSite() },
    "internet.monitor-hosts".com(emptyList()) { MonitorHosts() },
)

private data class Dispositivo(val ip: String, val nome: String?, val ms: Long?, val portas: List<Int>, val gateway: Boolean, val proprio: Boolean)

@Composable
private fun ScannerLan() {
    val c = LocalContext.current
    val achados = remember { mutableStateMapOf<String, Dispositivo>() }
    var progresso by remember { mutableStateOf(0f) }
    var rodando by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    val escopo = rememberCoroutineScope()
    val rede = remember { c.infoRede() }
    if (rede?.ipv4 == null || !rede.wifi) { OdEstadoRecurso(OdTipoEstado.Indisponivel, "Sem rede local", "Conecte a uma rede Wi-Fi para procurar aparelhos."); return }
    val prefixo = rede.prefixo.coerceAtLeast(24)
    val base = rede.ipv4.paraInt() and mascaraDe(prefixo)
    val total = (1 shl (32 - prefixo)) - 2
    fun iniciar() {
        achados.clear(); rodando = true; progresso = 0f
        job = escopo.launch(Dispatchers.IO) {
            val sem = Semaphore(48)
            var feitos = 0
            val portasTeste = listOf(80, 443, 22, 445, 139, 62078, 8080, 554, 9100, 8008, 5353, 1883)
            coroutineScope {
                (1..total).map { i ->
                    async {
                        sem.withPermit {
                            val ip = (base + i).paraIpv4()
                            val t0 = System.nanoTime()
                            val vivo = runCatching { InetAddress.getByName(ip).isReachable(400) }.getOrDefault(false)
                            val abertas = mutableListOf<Int>()
                            if (!vivo) for (p in portasTeste.take(4)) if (tcpAberta(ip, p, 250) != null) { abertas += p; break }
                            if (vivo || abertas.isNotEmpty()) {
                                val ms = (System.nanoTime() - t0) / 1_000_000
                                achados[ip] = Dispositivo(ip, null, ms, abertas, ip == rede.gateway?.hostAddress, ip == rede.ipv4.hostAddress)
                                val nome = nomeReverso(ip)
                                val portas = portasTeste.filter { tcpAberta(ip, it, 300) != null }
                                achados[ip] = achados[ip]!!.copy(nome = nome, portas = portas)
                            }
                            feitos++
                            progresso = feitos.toFloat() / total
                        }
                    }
                }.awaitAll()
            }
            rodando = false
        }
    }
    FtColuna {
        FtInfos("Rede", listOf("Faixa" to "${base.paraIpv4()}/$prefixo", "Este aparelho" to rede.ipv4.hostAddress, "Roteador" to rede.gateway?.hostAddress))
        FtBotaoMedir(rodando, { if (it) iniciar() else { job?.cancel(); rodando = false } }, iniciar = "Procurar aparelhos")
        if (rodando) FtNota("Procurando… ${(progresso * 100).toInt()}%")
        OdGrupoConfig("${achados.size} aparelhos") {
            achados.values.sortedBy { ipv4ParaInt(it.ip)?.toLong()?.and(0xFFFFFFFFL) }.forEach { d ->
                val papel = when { d.proprio -> "este aparelho"; d.gateway -> "roteador"; else -> null }
                OdLinhaConfig(d.nome ?: d.ip, descricao = listOfNotNull(if (d.nome != null) d.ip else null, papel,
                    d.portas.takeIf { it.isNotEmpty() }?.joinToString { "$it ${PORTAS_COMUNS[it] ?: ""}".trim() }).joinToString(" · ")) {
                    Text(d.ms?.let { "$it ms" } ?: "", style = OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum"))
                }
            }
        }
        FtNota("Desde o Android 10 os apps não leem a tabela ARP, então o MAC e o fabricante quase nunca aparecem. Aparelhos que não respondem a ping nem têm portas abertas ficam de fora.")
    }
}

@Composable
private fun PingTela() {
    var host by rememberSaveable { mutableStateOf("8.8.8.8") }
    var rodando by remember { mutableStateOf(false) }
    val tempos = remember { mutableStateListOf<Double?>() }
    val serie = remember { Serie(60) }
    val escopo = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    val ok = tempos.filterNotNull()
    val jitter = if (ok.size > 1) ok.zipWithNext { a, b -> abs(b - a) }.average() else null
    FtColuna {
        FtCampo(host, { host = it }, "Endereço ou IP")
        FtBotaoMedir(rodando, { liga ->
            rodando = liga
            if (liga) { tempos.clear(); serie.limpar(); job = escopo.launch { while (isActive) { val p = pingUmaVez(host); tempos += p.ms; serie.add((p.ms ?: 0.0).toFloat()); delay(1000) } } }
            else job?.cancel()
        }, iniciar = "Iniciar ping")
        if (tempos.isNotEmpty()) {
            FtPainel(titulo = "Latência") {
                OdLeitura(tempos.last()?.let { num(it, 1) } ?: "sem resposta", unidade = if (tempos.last() != null) "ms" else null, tamanho = OdTamanhoLeitura.Destaque)
                OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "ms")), 0f, (serie.pontos.maxOrNull() ?: 100f).coerceAtLeast(50f) * 1.2f, janela = 60, unidade = "ms", casas = 0, rotuloTempo = "−60 s" to "agora")
            }
            FtInfos("Estatísticas", listOf(
                "Enviados" to tempos.size.toString(),
                "Perda" to "${num((tempos.size - ok.size) * 100.0 / tempos.size, 0)}%",
                "Mínimo" to ok.minOrNull()?.let { "${num(it, 1)} ms" },
                "Média" to ok.takeIf { it.isNotEmpty() }?.average()?.let { "${num(it, 1)} ms" },
                "Máximo" to ok.maxOrNull()?.let { "${num(it, 1)} ms" },
                "Jitter" to jitter?.let { "${num(it, 1)} ms" },
            ))
        }
    }
}

@Composable
private fun Traceroute() {
    var host by rememberSaveable { mutableStateOf("google.com") }
    val saltos = remember { mutableStateListOf<Triple<Int, String?, Double?>>() }
    var rodando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    FtColuna {
        FtCampo(host, { host = it }, "Endereço ou IP")
        FtBotaoMedir(rodando, { liga ->
            rodando = liga
            if (liga) {
                saltos.clear()
                job = escopo.launch {
                    val destino = resolver(host)?.hostAddress
                    for (ttl in 1..30) {
                        val p = pingUmaVez(host, 2, ttl)
                        val ip = p.de
                        val rtt = ip?.let { pingUmaVez(it, 1).ms }
                        saltos += Triple(ttl, ip, rtt)
                        if (ip != null && ip == destino) break
                    }
                    rodando = false
                }
            } else job?.cancel()
        }, iniciar = "Iniciar traceroute")
        if (saltos.isNotEmpty()) OdGrupoConfig("Saltos") {
            saltos.forEach { (n, ip, ms) ->
                OdLinhaConfig("$n. ${ip ?: "* sem resposta"}") { Text(ms?.let { "${num(it, 1)} ms" } ?: "—", style = OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum")) }
            }
        }
        FtNota("Cada salto é um roteador no caminho até o destino. \"Sem resposta\" é comum: muitos roteadores não respondem a ping.")
    }
}

@Composable
private fun ScannerPortas() {
    val c = LocalContext.current
    var host by rememberSaveable { mutableStateOf(c.infoRede()?.gateway?.hostAddress ?: "192.168.0.1") }
    var faixa by rememberSaveable { mutableStateOf("comuns") }
    var inicio by rememberSaveable { mutableStateOf("1") }
    var fim by rememberSaveable { mutableStateOf("1024") }
    val abertas = remember { mutableStateListOf<Pair<Int, Long>>() }
    var progresso by remember { mutableStateOf(0f) }
    var rodando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    FtColuna {
        FtCampo(host, { host = it }, "Endereço ou IP")
        OdSegmentado(listOf("comuns" to "Comuns", "1024" to "1–1024", "faixa" to "Faixa"), faixa, { faixa = it })
        if (faixa == "faixa") FtBotoes {
            FtCampo(inicio, { inicio = it }, "De", Modifier.weight(1f), numerico = true)
            FtCampo(fim, { fim = it }, "Até", Modifier.weight(1f), numerico = true)
        }
        FtBotaoMedir(rodando, { liga ->
            rodando = liga
            if (liga) {
                abertas.clear(); progresso = 0f
                val portas = when (faixa) { "comuns" -> PORTAS_COMUNS.keys.toList(); "1024" -> (1..1024).toList(); else -> ((inicio.toIntOrNull() ?: 1)..(fim.toIntOrNull() ?: 1024).coerceAtMost(65535)).toList() }
                job = escopo.launch(Dispatchers.IO) {
                    val sem = Semaphore(100); var feitos = 0
                    coroutineScope {
                        portas.map { p -> async { sem.withPermit { tcpAberta(host, p, 600)?.let { abertas += p to it }; feitos++; progresso = feitos.toFloat() / portas.size } } }.awaitAll()
                    }
                    rodando = false
                }
            } else job?.cancel()
        }, iniciar = "Procurar portas abertas")
        if (rodando) FtNota("Verificando… ${(progresso * 100).toInt()}%")
        OdGrupoConfig("${abertas.size} portas abertas") {
            abertas.sortedBy { it.first }.forEach { (p, ms) -> OdLinhaConfig("$p", descricao = PORTAS_COMUNS[p]) { Text("$ms ms", style = OdTema.tipografia.ui) } }
        }
        FtNota("Use só em redes e aparelhos seus ou com autorização.")
    }
}

@Composable
private fun ConsultaDns() {
    val c = LocalContext.current
    var nome by rememberSaveable { mutableStateOf("google.com") }
    var tipo by rememberSaveable { mutableStateOf("A") }
    var servidor by rememberSaveable { mutableStateOf("sistema") }
    var resposta by remember { mutableStateOf<List<String>?>(null) }
    var tempo by remember { mutableStateOf<Long?>(null) }
    val escopo = rememberCoroutineScope()
    val toasts = LocalToasts.current
    FtColuna {
        FtCampo(nome, { nome = it }, "Domínio")
        OdSegmentado(listOf("A" to "A", "AAAA" to "AAAA", "MX" to "MX", "TXT" to "TXT", "NS" to "NS"), tipo, { tipo = it })
        OdSegmentado(listOf("sistema" to "Sistema", "1.1.1.1" to "Cloudflare", "8.8.8.8" to "Google", "9.9.9.9" to "Quad9"), servidor, { servidor = it })
        OdBotao("Consultar", {
            escopo.launch {
                val t0 = System.currentTimeMillis()
                resposta = withContext(Dispatchers.IO) {
                    runCatching {
                        val ip = if (servidor == "sistema") c.infoRede()?.dns?.firstOrNull()?.hostAddress ?: "1.1.1.1" else servidor
                        val l = Lookup(nome.trim(), Type.value(tipo))
                        l.setResolver(SimpleResolver(ip).apply { timeout = Duration.ofSeconds(5) })
                        l.setCache(null)
                        val r = l.run()
                        if (l.result != Lookup.SUCCESSFUL) listOf("Sem resposta: ${l.errorString}") else r.map { "${it.rdataToString()}  (TTL ${it.ttl} s)" }
                    }.getOrElse { listOf("Erro: ${it.message}") }
                }
                tempo = System.currentTimeMillis() - t0
            }
        }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario)
        resposta?.let { r ->
            OdGrupoConfig("Registros $tipo${tempo?.let { " · $it ms" } ?: ""}") {
                r.forEach { linha -> OdLinhaAcao(linha, { copiarTexto(c, linha.substringBefore("  (TTL")); toasts.mostrar("Copiado.") }) }
            }
        }
    }
}

private suspend fun consultaWhois(servidor: String, pergunta: String): String = withContext(Dispatchers.IO) {
    Socket().use { s ->
        s.connect(InetSocketAddress(servidor, 43), 8000)
        s.soTimeout = 10000
        s.getOutputStream().write("$pergunta\r\n".toByteArray())
        s.getInputStream().bufferedReader(Charsets.UTF_8).readText()
    }
}

@Composable
private fun Whois() {
    var alvo by rememberSaveable { mutableStateOf("wikipedia.org") }
    var texto by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()
    FtColuna {
        FtCampo(alvo, { alvo = it }, "Domínio ou IP")
        OdBotao("Consultar", {
            escopo.launch {
                carregando = true
                texto = runCatching {
                    val iana = consultaWhois("whois.iana.org", alvo.trim())
                    val ref = Regex("(?m)^(?:refer|whois):\\s*(\\S+)").find(iana)?.groupValues?.get(1)
                    if (ref != null) consultaWhois(ref, alvo.trim()) else iana
                }.getOrElse { "Não foi possível consultar: ${it.message}. Confira a internet e o endereço." }
                carregando = false
            }
        }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario, carregando = carregando)
        texto?.let { FtPainel { Text(it.trim(), style = OdTema.tipografia.codigo, color = OdTema.cores.texto) } }
    }
}

@Composable
private fun IpExterno() {
    var dados by remember { mutableStateOf<JSONObject?>(null) }
    var v6 by remember { mutableStateOf<String?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var tentativa by remember { mutableStateOf(0) }
    androidx.compose.runtime.LaunchedEffect(tentativa) {
        withContext(Dispatchers.IO) {
            runCatching { dados = JSONObject(URL("https://ipinfo.io/json").readText()) }.onFailure { erro = "Sem resposta do serviço. Confira a internet." }
            v6 = runCatching { URL("https://api64.ipify.org").readText().trim().takeIf { ':' in it } }.getOrNull()
        }
    }
    FtColuna {
        val d = dados
        if (d == null) { if (erro != null) OdEstadoRecurso(OdTipoEstado.Indisponivel, "Não foi possível consultar", erro!!, acao = "Tentar de novo", aoAgir = { erro = null; tentativa++ }) else FtNota("Consultando…"); return@FtColuna }
        FtPainel(titulo = "IP externo") { OdLeitura(d.optString("ip"), tamanho = OdTamanhoLeitura.Padrao) }
        FtInfos(null, listOf(
            "IPv6" to v6,
            "Provedor" to d.optString("org").ifBlank { null },
            "Nome reverso" to d.optString("hostname").ifBlank { null },
            "Cidade" to listOf(d.optString("city"), d.optString("region"), d.optString("country")).filter { it.isNotBlank() }.joinToString(", ").ifBlank { null },
            "Fuso" to d.optString("timezone").ifBlank { null },
        ))
        FtNota("Consulta o serviço ipinfo.io, que vê o seu IP como qualquer site vê. A localização é aproximada (do provedor).")
    }
}

@Composable
private fun TesteVelocidade() {
    var fase by remember { mutableStateOf("") }
    var latencia by remember { mutableStateOf<Double?>(null) }
    var descida by remember { mutableStateOf<Double?>(null) }
    var subida by remember { mutableStateOf<Double?>(null) }
    var rodando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    FtColuna {
        FtBotaoMedir(rodando, { liga ->
            rodando = liga
            if (!liga) { job?.cancel(); return@FtBotaoMedir }
            latencia = null; descida = null; subida = null
            job = escopo.launch(Dispatchers.IO) {
                runCatching {
                    fase = "Latência"
                    latencia = (1..8).mapNotNull {
                        val t0 = System.nanoTime()
                        runCatching { (URL("https://speed.cloudflare.com/__down?bytes=0").openConnection() as HttpURLConnection).apply { connectTimeout = 5000 }.inputStream.use { it.readBytes() } }.getOrNull()?.let { (System.nanoTime() - t0) / 1e6 }
                    }.sorted().drop(1).take(4).average()
                    fase = "Descida"
                    descida = medirTransferencia(10_000) { contador ->
                        val con = URL("https://speed.cloudflare.com/__down?bytes=50000000").openConnection() as HttpURLConnection
                        con.inputStream.use { ler(it, contador) }
                    }.also { descida = it }
                    fase = "Subida"
                    subida = medirTransferencia(10_000) { contador ->
                        val con = URL("https://speed.cloudflare.com/__up").openConnection() as HttpURLConnection
                        con.requestMethod = "POST"; con.doOutput = true; con.setFixedLengthStreamingMode(20_000_000)
                        val bloco = Random.nextBytes(64 * 1024)
                        con.outputStream.use { o -> var enviado = 0; while (enviado < 20_000_000 && isActive) { o.write(bloco); enviado += bloco.size; contador(bloco.size.toLong()) } }
                        con.responseCode
                    }
                }.onFailure { fase = "Falhou: confira a internet." }
                if (!fase.startsWith("Falhou")) fase = "Concluído"
                rodando = false
            }
        }, iniciar = "Iniciar teste")
        if (fase.isNotEmpty()) FtNota(fase)
        FtPainel(titulo = "Descida") { OdLeitura(descida?.let { formatarVelocidade(it) } ?: "—", tamanho = OdTamanhoLeitura.Destaque) }
        FtPainel(titulo = "Subida") { OdLeitura(subida?.let { formatarVelocidade(it) } ?: "—", tamanho = OdTamanhoLeitura.Destaque) }
        FtInfos(null, listOf("Latência" to latencia?.let { "${num(it, 0)} ms" }))
        FtNota("Usa os servidores de teste da Cloudflare, o mais próximo de você. Gasta cerca de 100 a 300 MB numa conexão rápida: cuidado com dados móveis.")
    }
}

private fun ler(i: InputStream, contador: (Long) -> Unit) {
    val b = ByteArray(64 * 1024)
    while (true) { val n = i.read(b); if (n < 0) break; contador(n.toLong()) }
}

/** Roda 4 transferências paralelas por um tempo e devolve bytes por segundo. */
private suspend fun medirTransferencia(duracaoMs: Long, transferir: suspend (contar: (Long) -> Unit) -> Unit): Double = coroutineScope {
    val bytes = java.util.concurrent.atomic.AtomicLong(0)
    val t0 = System.currentTimeMillis()
    val jobs = (1..4).map { launch(Dispatchers.IO) { while (isActive && System.currentTimeMillis() - t0 < duracaoMs) runCatching { transferir { bytes.addAndGet(it) } } } }
    delay(1500); val b0 = bytes.get(); val t1 = System.currentTimeMillis() // descarta o início (aceleração do TCP)
    delay(duracaoMs - 1500)
    val b1 = bytes.get(); val t2 = System.currentTimeMillis()
    jobs.forEach { it.cancel() }
    (b1 - b0) * 1000.0 / (t2 - t1).coerceAtLeast(1)
}

@Composable
private fun WakeOnLan() {
    val c = LocalContext.current
    val banco = remember { BancoDados.de(c).itens() }
    val salvos by produceState(emptyList<ItemSalvo>()) { banco.observar(Tipos.WOL).collect { value = it } }
    var nome by rememberSaveable { mutableStateOf("") }
    var mac by rememberSaveable { mutableStateOf("") }
    val escopo = rememberCoroutineScope()
    val toasts = LocalToasts.current
    fun enviar(m: String) = escopo.launch(Dispatchers.IO) {
        runCatching {
            val bytes = m.split(':', '-', '.').filter { it.isNotBlank() }.map { it.toInt(16).toByte() }.toByteArray().also { require(it.size == 6) }
            val pacote = ByteArray(6) { 0xFF.toByte() } + (1..16).flatMap { bytes.toList() }
            val rede = c.infoRede()
            val bcast = rede?.ipv4?.let { (it.paraInt() or mascaraDe(rede.prefixo).inv()).paraIpv4() } ?: "255.255.255.255"
            DatagramSocket().use { s ->
                s.broadcast = true
                listOf(bcast, "255.255.255.255").distinct().forEach { ip -> listOf(9, 7).forEach { p -> s.send(DatagramPacket(pacote, pacote.size, InetAddress.getByName(ip), p)) } }
            }
            toasts.mostrar("Pacote enviado.")
        }.onFailure { toasts.mostrar("MAC inválido. Use o formato AA:BB:CC:DD:EE:FF.", OdTipoToast.Erro) }
    }
    FtColuna {
        FtCampo(nome, { nome = it }, "Nome (opcional)")
        FtCampo(mac, { mac = it }, "Endereço MAC")
        FtBotoes {
            OdBotao("Acordar", { enviar(mac) }, Modifier.weight(1f), tipo = OdTipoBotao.Primario)
            OdBotao("Salvar", { escopo.launch { banco.salvar(ItemSalvo.de(Tipos.WOL, nome.ifBlank { mac }, mapOf("mac" to mac))); toasts.mostrar("Aparelho salvo.") } }, Modifier.weight(1f))
        }
        if (salvos.isNotEmpty()) OdGrupoConfig("Salvos") {
            salvos.forEach { s ->
                OdLinhaConfig(s.nome, descricao = s.campos()["mac"]) {
                    io.github.andrexagris.opendesign.OdBotaoIcone(OdIcones.Iniciar, "Acordar ${s.nome}", { enviar(s.campos()["mac"] ?: "") })
                    io.github.andrexagris.opendesign.OdBotaoIcone(OdIcones.Lixeira, "Apagar ${s.nome}", { escopo.launch { banco.apagar(s.id) } })
                }
            }
        }
        FtNota("O computador precisa estar na mesma rede, ligado na tomada e com o Wake-on-LAN ativado na BIOS e na placa de rede.")
    }
}

private data class Servico(val nome: String, val tipo: String, val endereco: String?, val detalhe: String?)

private val TIPOS_NSD = linkedMapOf(
    "_ipp._tcp" to "Impressora (IPP)", "_printer._tcp" to "Impressora", "_pdl-datastream._tcp" to "Impressora", "_googlecast._tcp" to "Chromecast / Google TV",
    "_airplay._tcp" to "AirPlay", "_raop._tcp" to "Alto-falante AirPlay", "_spotify-connect._tcp" to "Spotify Connect", "_smb._tcp" to "Pasta compartilhada (SMB)",
    "_http._tcp" to "Página web", "_ssh._tcp" to "SSH", "_hap._tcp" to "HomeKit", "_matter._tcp" to "Matter", "_amzn-wplay._tcp" to "Fire TV", "_androidtvremote2._tcp" to "Android TV",
)

@Suppress("DEPRECATION")
@Composable
private fun DescobertaServicos() {
    val c = LocalContext.current
    val servicos = remember { mutableStateMapOf<String, Servico>() }
    DisposableEffect(Unit) {
        val nsd = c.getSystemService(NsdManager::class.java)
        val wifi = c.applicationContext.getSystemService(WifiManager::class.java)
        val trava = wifi.createMulticastLock("ferramentas-descoberta").apply { setReferenceCounted(false); acquire() }
        val ouvintes = TIPOS_NSD.keys.map { tipo ->
            object : NsdManager.DiscoveryListener {
                override fun onServiceFound(s: NsdServiceInfo) {
                    servicos["${s.serviceName}|$tipo"] = Servico(s.serviceName, TIPOS_NSD[tipo] ?: tipo, null, null)
                    runCatching {
                        nsd.resolveService(s, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(si: NsdServiceInfo, e: Int) {}
                            override fun onServiceResolved(si: NsdServiceInfo) {
                                servicos["${s.serviceName}|$tipo"] = Servico(si.serviceName, TIPOS_NSD[tipo] ?: tipo, "${si.host?.hostAddress}:${si.port}", null)
                            }
                        })
                    }
                }
                override fun onServiceLost(s: NsdServiceInfo) {}
                override fun onDiscoveryStarted(t: String) {}
                override fun onDiscoveryStopped(t: String) {}
                override fun onStartDiscoveryFailed(t: String, e: Int) {}
                override fun onStopDiscoveryFailed(t: String, e: Int) {}
            }.also { runCatching { nsd.discoverServices(tipo, NsdManager.PROTOCOL_DNS_SD, it) } }
        }
        val ssdp = Thread {
            runCatching {
                MulticastSocket().use { s ->
                    s.soTimeout = 3000
                    val msg = "M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nMAN: \"ssdp:discover\"\r\nMX: 2\r\nST: ssdp:all\r\n\r\n".toByteArray()
                    repeat(2) { s.send(DatagramPacket(msg, msg.size, InetAddress.getByName("239.255.255.250"), 1900)) }
                    val buf = ByteArray(2048)
                    val fim = System.currentTimeMillis() + 6000
                    while (System.currentTimeMillis() < fim && !Thread.currentThread().isInterrupted) {
                        val p = DatagramPacket(buf, buf.size)
                        runCatching { s.receive(p) }.getOrNull() ?: continue
                        val txt = String(p.data, 0, p.length)
                        val servidor = Regex("(?im)^server:\\s*(.+)$").find(txt)?.groupValues?.get(1)?.trim()
                        val st = Regex("(?im)^st:\\s*(.+)$").find(txt)?.groupValues?.get(1)?.trim()
                        val ip = p.address.hostAddress ?: continue
                        val chave = "ssdp|$ip"
                        val anterior = servicos[chave]
                        servicos[chave] = Servico(servidor ?: "Aparelho UPnP", "UPnP / DLNA", ip, listOfNotNull(anterior?.detalhe, st?.substringAfterLast(':')).distinct().take(4).joinToString(", "))
                    }
                }
            }
        }.apply { start() }
        onDispose {
            ouvintes.forEach { runCatching { nsd.stopServiceDiscovery(it) } }
            ssdp.interrupt()
            trava.release()
        }
    }
    FtColuna {
        FtNota("Procurando impressoras, TVs, alto-falantes e outros aparelhos que se anunciam na rede (mDNS e UPnP).")
        OdGrupoConfig("${servicos.size} encontrados") {
            servicos.values.sortedBy { it.tipo }.forEach { s -> OdLinhaConfig(s.nome, descricao = listOfNotNull(s.tipo, s.endereco, s.detalhe?.ifBlank { null }).joinToString(" · ")) }
        }
    }
}

@Composable
private fun CalculadoraSubRede() {
    val c = LocalContext.current
    val rede = remember { c.infoRede() }
    var ip by rememberSaveable { mutableStateOf(rede?.ipv4?.hostAddress ?: "192.168.0.10") }
    var prefixo by rememberSaveable { mutableStateOf((rede?.prefixo ?: 24).toString()) }
    val n = ipv4ParaInt(ip)
    val p = prefixo.toIntOrNull()?.takeIf { it in 0..32 }
    FtColuna {
        FtCampo(ip, { ip = it }, "Endereço IPv4", erro = if (n == null) "Use o formato 192.168.0.10." else null)
        FtCampo(prefixo, { prefixo = it }, "Prefixo (CIDR)", numerico = true, sufixo = "bits", erro = if (p == null) "De 0 a 32." else null)
        if (n != null && p != null) {
            val m = mascaraDe(p)
            val rede0 = n and m
            val bcast = rede0 or m.inv()
            val hosts = if (p >= 31) (1L shl (32 - p)) else (1L shl (32 - p)) - 2
            val classe = when ((n ushr 24) and 255) { in 1..126 -> "A"; in 128..191 -> "B"; in 192..223 -> "C"; in 224..239 -> "D (multicast)"; else -> "E" }
            val privada = (n ushr 24 == 10) || (n ushr 20 == 0xAC1) || (n ushr 16 == 0xC0A8)
            FtInfos(null, listOf(
                "Rede" to "${rede0.paraIpv4()}/$p", "Máscara" to m.paraIpv4(), "Curinga" to m.inv().paraIpv4(), "Broadcast" to bcast.paraIpv4(),
                "Primeiro host" to (if (p >= 31) rede0 else rede0 + 1).paraIpv4(), "Último host" to (if (p >= 31) bcast else bcast - 1).paraIpv4(),
                "Hosts utilizáveis" to "%,d".format(io.github.andrexagris.ferramentas.base.localeBr, hosts), "Classe" to classe, "Tipo" to if (privada) "Privada" else "Pública",
            ))
        }
    }
}

@Composable
private fun ChecagemSite() {
    var url by rememberSaveable { mutableStateOf("https://") }
    var resultado by remember { mutableStateOf<List<Pair<String, String?>>?>(null) }
    var certs by remember { mutableStateOf<List<List<Pair<String, String?>>>>(emptyList()) }
    var carregando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()
    FtColuna {
        FtCampo(url, { url = it }, "Endereço (URL)")
        OdBotao("Verificar", {
            escopo.launch(Dispatchers.IO) {
                carregando = true
                runCatching {
                    var alvo = url.trim().let { if ("://" !in it) "https://$it" else it }
                    val redirecionamentos = mutableListOf<String>()
                    var con: HttpURLConnection
                    var t0: Long
                    while (true) {
                        con = URL(alvo).openConnection() as HttpURLConnection
                        con.instanceFollowRedirects = false; con.connectTimeout = 8000; con.readTimeout = 8000
                        con.setRequestProperty("User-Agent", "Ferramentas (Android)")
                        t0 = System.currentTimeMillis()
                        val codigo = con.responseCode
                        if (codigo in 300..399 && redirecionamentos.size < 8) {
                            val prox = con.getHeaderField("Location") ?: break
                            alvo = URL(URL(alvo), prox).toString(); redirecionamentos += alvo; con.disconnect()
                        } else break
                    }
                    val ms = System.currentTimeMillis() - t0
                    val https = con as? HttpsURLConnection
                    resultado = listOf(
                        "Status" to "${con.responseCode} ${con.responseMessage ?: ""}".trim(), "Tempo de resposta" to "$ms ms",
                        "Endereço final" to alvo, "Redirecionamentos" to redirecionamentos.size.toString(), "Servidor" to con.getHeaderField("Server"),
                        "Tipo de conteúdo" to con.contentType, "Cifra TLS" to https?.cipherSuite, "HSTS" to (con.getHeaderField("Strict-Transport-Security")?.let { "Sim" } ?: "Não"),
                    )
                    certs = https?.serverCertificates?.filterIsInstance<X509Certificate>()?.map { x ->
                        val dias = (x.notAfter.time - System.currentTimeMillis()) / 86_400_000
                        listOf(
                            "Para" to x.subjectX500Principal.name.substringAfter("CN=").substringBefore(','), "Emitido por" to x.issuerX500Principal.name.substringAfter("CN=").substringBefore(','),
                            "Válido de" to formatarDataHora(x.notBefore.time), "Válido até" to "${formatarDataHora(x.notAfter.time)} (${if (dias >= 0) "faltam $dias dias" else "VENCIDO"})",
                            "Nomes" to x.subjectAlternativeNames?.mapNotNull { it.getOrNull(1)?.toString() }?.take(6)?.joinToString(", "),
                        )
                    }.orEmpty()
                    con.disconnect()
                }.onFailure { resultado = listOf("Erro" to "${it.javaClass.simpleName}: ${it.message}"); certs = emptyList() }
                carregando = false
            }
        }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario, carregando = carregando)
        resultado?.let { FtInfos("Resposta", it) }
        certs.forEachIndexed { i, cert -> FtInfos(if (i == 0) "Certificado do site" else "Certificado intermediário $i", cert) }
    }
}

@Composable
private fun MonitorHosts() {
    val c = LocalContext.current
    val banco = remember { BancoDados.de(c).itens() }
    val hosts by produceState(emptyList<ItemSalvo>()) { banco.observar(Tipos.HOST).collect { value = it } }
    var ativo by rememberAjusteBool(ChavesPeriodicas.HOSTS, false)
    var host by rememberSaveable { mutableStateOf("") }
    var porta by rememberSaveable { mutableStateOf("443") }
    val escopo = rememberCoroutineScope()
    val comNotificacao = rememberComPermissao(Permissao.Notificacoes)
    val toasts = LocalToasts.current
    FtColuna {
        OdGrupoConfig(null) { OdLinhaInterruptor("Monitorar a cada 15 minutos", ativo, { v -> comNotificacao { ativo = v; TrabalhoPeriodico.agendar(c) } }, "Avisa quando um host cair ou voltar") }
        FtBotoes {
            FtCampo(host, { host = it }, "Host", Modifier.weight(2f))
            FtCampo(porta, { porta = it }, "Porta", Modifier.weight(1f), numerico = true)
        }
        OdBotao("Adicionar host", {
            if (host.isNotBlank()) escopo.launch { banco.salvar(ItemSalvo.de(Tipos.HOST, host.trim(), mapOf("porta" to (porta.toIntOrNull() ?: 443)))); host = ""; toasts.mostrar("Host adicionado.") }
        }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario)
        OdBotao("Verificar todos agora", {
            escopo.launch {
                hosts.forEach { h ->
                    val f = h.campos(); val p = f["porta"]?.toIntOrNull() ?: 443
                    val ms = tcpAberta(h.nome, p, 3000)
                    banco.salvar(ItemSalvo.de(Tipos.HOST, h.nome, f + ("ok" to (ms != null).toString()) + ("ms" to (ms ?: "")) + ("verificado" to System.currentTimeMillis()), h.id))
                }
                toasts.mostrar("Verificação concluída.")
            }
        }, Modifier.fillMaxWidth())
        if (hosts.isNotEmpty()) OdGrupoConfig("Hosts") {
            hosts.forEach { h ->
                val f = h.campos()
                val estado = when (f["ok"]) { "true" -> "No ar"; "false" -> "Fora do ar"; else -> "Ainda não verificado" }
                OdLinhaConfig("${h.nome}:${f["porta"]}", descricao = "$estado${f["ms"]?.takeIf { it.isNotBlank() }?.let { " · $it ms" } ?: ""}${f["verificado"]?.toLongOrNull()?.let { " · ${formatarDataHora(it)}" } ?: ""}",
                    icone = if (f["ok"] == "false") OdIcones.Erro else if (f["ok"] == "true") OdIcones.Sucesso else OdIcones.Info) {
                    io.github.andrexagris.opendesign.OdBotaoIcone(OdIcones.Lixeira, "Remover ${h.nome}", { escopo.launch { banco.apagar(h.id) } })
                }
            }
        }
        FtNota("O teste abre uma conexão TCP na porta escolhida (443 para sites). O Android roda a verificação a cada 15 minutos no mínimo.")
    }
}

