package io.github.andrexagris.ferramentas.ui.ferramentas.localizacao

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.andrexagris.ferramentas.base.compartilharArquivo
import io.github.andrexagris.ferramentas.base.copiarTexto
import io.github.andrexagris.ferramentas.base.formatarDuracao
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.orientacao
import io.github.andrexagris.ferramentas.base.rememberComPermissao
import io.github.andrexagris.ferramentas.base.rememberSensor
import io.github.andrexagris.ferramentas.base.temPermissao
import io.github.andrexagris.ferramentas.catalogo.Permissao
import io.github.andrexagris.ferramentas.dados.BancoDados
import io.github.andrexagris.ferramentas.dados.ItemSalvo
import io.github.andrexagris.ferramentas.dados.Tipos
import io.github.andrexagris.ferramentas.dados.rememberAjusteFloat
import io.github.andrexagris.ferramentas.servicos.ServicoMedicao
import io.github.andrexagris.ferramentas.servicos.TarefaMedicao
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
import io.github.andrexagris.opendesign.OdBotaoIcone
import io.github.andrexagris.opendesign.OdBussola
import io.github.andrexagris.opendesign.OdEscalaArco
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.odFormatar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.cos

fun telasLocalizacao(): Map<String, TelaDeFerramenta> = mapOf(
    "localizacao.gnss" com { StatusGnss() },
    "localizacao.coordenadas" com { Coordenadas() },
    "localizacao.altimetro" com { AltimetroBarometrico() },
    "localizacao.dupla-frequencia" com { DuplaFrequencia() },
    "localizacao.bussola" com { Bussola() },
    "localizacao.voltar-ponto" com { VoltarAoPonto() },
    "localizacao.velocimetro" com { Velocimetro() },
    "localizacao.area" com { MedidorArea() },
    "localizacao.trajeto" com { Trajeto() },
)

/** Posição do GPS ao vivo (null até o primeiro fix). Exige a permissão de localização. */
@SuppressLint("MissingPermission")
@Composable
fun rememberLocalizacao(intervaloMs: Long = 1000): State<Location?> {
    val c = LocalContext.current
    val estado = remember { mutableStateOf<Location?>(null) }
    DisposableEffect(intervaloMs) {
        val lm = c.getSystemService(LocationManager::class.java)
        val o = LocationListener { estado.value = it }
        if (c.temPermissao(Permissao.Localizacao)) {
            estado.value = runCatching { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
            runCatching { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, intervaloMs, 0f, o, Looper.getMainLooper()) }
        }
        onDispose { lm.removeUpdates(o) }
    }
    return estado
}

private fun nomeConstelacao(t: Int) = when (t) {
    GnssStatus.CONSTELLATION_GPS -> "GPS"; GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"; GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
    GnssStatus.CONSTELLATION_BEIDOU -> "BeiDou"; GnssStatus.CONSTELLATION_QZSS -> "QZSS"; GnssStatus.CONSTELLATION_SBAS -> "SBAS"; GnssStatus.CONSTELLATION_IRNSS -> "NavIC"
    else -> "Outro"
}

data class Satelite(val constelacao: Int, val id: Int, val cn0: Float, val elevacao: Float, val azimute: Float, val usado: Boolean, val freqMhz: Float?)

@SuppressLint("MissingPermission")
@Composable
fun rememberGnss(): List<Satelite> {
    val c = LocalContext.current
    var lista by remember { mutableStateOf(emptyList<Satelite>()) }
    DisposableEffect(Unit) {
        val lm = c.getSystemService(LocationManager::class.java)
        val cb = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(s: GnssStatus) {
                lista = (0 until s.satelliteCount).map { i ->
                    Satelite(s.getConstellationType(i), s.getSvid(i), s.getCn0DbHz(i), s.getElevationDegrees(i), s.getAzimuthDegrees(i), s.usedInFix(i),
                        if (s.hasCarrierFrequencyHz(i)) s.getCarrierFrequencyHz(i) / 1e6f else null)
                }
            }
        }
        val o = LocationListener { }
        runCatching { lm.registerGnssStatusCallback(c.mainExecutor, cb); lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, o, Looper.getMainLooper()) }
        onDispose { lm.unregisterGnssStatusCallback(cb); lm.removeUpdates(o) }
    }
    return lista
}

@Composable
private fun StatusGnss() {
    val sats = rememberGnss()
    val local by rememberLocalizacao()
    val c = OdTema.cores
    val cores = mapOf(GnssStatus.CONSTELLATION_GPS to c.graficoX, GnssStatus.CONSTELLATION_GLONASS to c.graficoY, GnssStatus.CONSTELLATION_GALILEO to c.graficoZ,
        GnssStatus.CONSTELLATION_BEIDOU to c.secundaria.cor, GnssStatus.CONSTELLATION_QZSS to c.primaria.cor)
    val medidor = rememberTextMeasurer()
    val estilo = OdTema.tipografia.uiPequeno.copy(fontSize = 10.sp, color = c.graficoRotulo)
    FtColuna {
        FtInfos(null, listOf(
            "Satélites usados" to "${sats.count { it.usado }} de ${sats.size}",
            "Precisão" to local?.let { "± ${num(it.accuracy, 0)} m" },
            "Sinal médio (usados)" to sats.filter { it.usado }.takeIf { it.isNotEmpty() }?.map { it.cn0 }?.average()?.let { "${num(it, 0)} dB-Hz" },
        ))
        FtPainel(titulo = "Mapa do céu") {
            Canvas(Modifier.fillMaxWidth().aspectRatio(1f).semantics { contentDescription = "Mapa do céu com ${sats.size} satélites" }) {
                val raio = size.minDimension / 2 * 0.9f
                val centro = Offset(size.width / 2, size.height / 2)
                for (el in listOf(0, 30, 60)) drawCircle(c.graficoGrade, raio * (90 - el) / 90f, centro, style = Stroke(1.dp.toPx()))
                drawLine(c.graficoGrade, Offset(centro.x, centro.y - raio), Offset(centro.x, centro.y + raio), 1f)
                drawLine(c.graficoGrade, Offset(centro.x - raio, centro.y), Offset(centro.x + raio, centro.y), 1f)
                listOf("N" to 0f, "L" to 90f, "S" to 180f, "O" to 270f).forEach { (r, az) ->
                    val t = medidor.measure(r, estilo)
                    val a = Math.toRadians(az.toDouble() - 90)
                    drawText(t, topLeft = Offset(centro.x + (raio + 8.dp.toPx()) * cos(a).toFloat() - t.size.width / 2, centro.y + (raio + 8.dp.toPx()) * kotlin.math.sin(a).toFloat() - t.size.height / 2))
                }
                sats.forEach { s ->
                    val r = raio * (90 - s.elevacao) / 90f
                    val a = Math.toRadians(s.azimute.toDouble() - 90)
                    val p = Offset(centro.x + r * cos(a).toFloat(), centro.y + r * kotlin.math.sin(a).toFloat())
                    val cor = cores[s.constelacao] ?: c.texto2
                    if (s.usado) drawCircle(cor, 6.dp.toPx(), p) else drawCircle(cor, 6.dp.toPx(), p, style = Stroke(1.5.dp.toPx()))
                }
            }
            FtNota("Cheio: usado no cálculo da posição. Cores: GPS, GLONASS, Galileo, BeiDou e QZSS.")
        }
        OdGrupoConfig("Satélites") {
            sats.sortedByDescending { it.cn0 }.forEach { s ->
                OdLinhaConfig("${nomeConstelacao(s.constelacao)} ${s.id}", descricao = "elev. ${num(s.elevacao, 0)}° · az. ${num(s.azimute, 0)}°${if (s.usado) " · usado" else ""}${s.freqMhz?.let { " · ${num(it, 2)} MHz" } ?: ""}") {
                    Text("${num(s.cn0, 0)} dB-Hz", style = OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum"))
                }
            }
        }
        if (sats.isEmpty()) FtNota("Procurando satélites. Vá para um lugar aberto, longe de prédios: o primeiro fix pode levar um minuto.")
    }
}

/** "23°32'51,2\" S" a partir de graus decimais. */
fun grausMinutos(v: Double, lat: Boolean): String {
    val a = abs(v); val g = a.toInt(); val m = ((a - g) * 60).toInt(); val s = ((a - g) * 60 - m) * 60
    return "$g°%02d'%s\" %s".format(m, num(s, 1), if (lat) (if (v >= 0) "N" else "S") else (if (v >= 0) "L" else "O"))
}

@Composable
private fun Coordenadas() {
    val ctx = LocalContext.current
    val local by rememberLocalizacao()
    val toasts = LocalToasts.current
    val l = local
    FtColuna {
        if (l == null) { OdEstadoRecurso(OdTipoEstado.Medindo, "Procurando posição", "Vá para um lugar aberto. O primeiro fix pode levar um minuto."); return@FtColuna }
        FtPainel(titulo = "Coordenadas") {
            OdLeitura(num(l.latitude, 6), unidade = "lat")
            OdLeitura(num(l.longitude, 6), unidade = "lon")
        }
        FtInfos(null, listOf(
            "Graus e minutos" to "${grausMinutos(l.latitude, true)}  ${grausMinutos(l.longitude, false)}",
            "Altitude (GPS)" to if (l.hasAltitude()) "${num(l.altitude, 0)} m" else null,
            "Altitude acima do mar" to if (android.os.Build.VERSION.SDK_INT >= 34 && l.hasMslAltitude()) "${num(l.mslAltitudeMeters, 0)} m" else null,
            "Precisão" to "± ${num(l.accuracy, 0)} m",
            "Fonte" to l.provider,
            "Idade do fix" to "${(System.currentTimeMillis() - l.time) / 1000} s",
        ))
        FtBotoes {
            OdBotao("Copiar", { copiarTexto(ctx, "${l.latitude}, ${l.longitude}"); toasts.mostrar("Coordenadas copiadas.") }, Modifier.weight(1f))
            OdBotao("Compartilhar", { io.github.andrexagris.ferramentas.base.compartilharTexto(ctx, "https://www.openstreetmap.org/?mlat=${l.latitude}&mlon=${l.longitude}#map=17/${l.latitude}/${l.longitude}") }, Modifier.weight(1f))
        }
        FtNota("A altitude do GPS costuma errar 10 m ou mais. Para altitude precisa, só com barômetro.")
    }
}

@Composable
private fun AltimetroBarometrico() {
    val p by rememberSensor(Sensor.TYPE_PRESSURE)
    var nivelMar by rememberAjusteFloat("localizacao.altimetro.nivel-mar", SensorManager.PRESSURE_STANDARD_ATMOSPHERE)
    var texto by rememberSaveable { mutableStateOf("") }
    val hpa = p?.get(0)
    FtColuna {
        FtPainel(titulo = "Altitude") {
            OdLeitura(hpa?.let { odFormatar(SensorManager.getAltitude(nivelMar, it).toDouble(), 1) } ?: "—", unidade = "m", tamanho = OdTamanhoLeitura.Destaque)
            OdLeitura(hpa?.let { odFormatar(it.toDouble(), 1) } ?: "—", unidade = "hPa")
        }
        FtCampo(texto, { texto = it }, "Altitude conhecida aqui (para calibrar)", numerico = true, sufixo = "m")
        OdBotao("Calibrar", {
            val alt = texto.numero() ?: return@OdBotao
            if (hpa != null) nivelMar = (hpa / Math.pow(1 - alt / 44330.0, 5.255)).toFloat()
        }, Modifier.fillMaxWidth())
        FtNota("Sem calibrar, usa a pressão padrão ao nível do mar (1013,25 hPa). A pressão muda com o tempo, então recalibre em lugares de altitude conhecida.")
    }
}

@Composable
private fun DuplaFrequencia() {
    val sats = rememberGnss()
    val l5 = sats.filter { it.freqMhz != null && it.freqMhz < 1200 }
    FtColuna {
        FtPainel(titulo = "Segunda frequência (L5/E5a)") {
            OdLeitura(if (sats.isEmpty()) "Procurando…" else if (l5.isNotEmpty()) "Sim" else "Não detectada", tamanho = OdTamanhoLeitura.Destaque)
        }
        FtInfos(null, listOf("Satélites com frequência informada" to sats.count { it.freqMhz != null }.toString(), "Em L5/E5a (≈1176 MHz)" to l5.size.toString(), "Usados no fix" to l5.count { it.usado }.toString()))
        OdGrupoConfig("Frequências vistas") {
            sats.mapNotNull { s -> s.freqMhz?.let { "${nomeConstelacao(s.constelacao)} · ${num(it, 2)} MHz" } }.groupingBy { it }.eachCount().forEach { (f, n) -> OdLinhaConfig(f, descricao = "$n satélites") }
        }
        FtNota("Com duas frequências (L1 + L5) o GPS corrige o erro da ionosfera e fica bem mais preciso em cidades. Depende do chip do aparelho.")
    }
}

/** Declinação magnética no lugar (para o norte verdadeiro), se houver posição. */
@SuppressLint("MissingPermission")
private fun declinacao(c: Context): Float? {
    if (!c.temPermissao(Permissao.Localizacao)) return null
    val lm = c.getSystemService(LocationManager::class.java)
    val l = runCatching { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull() ?: return null
    return GeomagneticField(l.latitude.toFloat(), l.longitude.toFloat(), l.altitude.toFloat(), System.currentTimeMillis()).declination
}

@Composable
fun rememberAzimute(): Pair<Float?, Int> {
    val rv by rememberSensor(Sensor.TYPE_ROTATION_VECTOR, SensorManager.SENSOR_DELAY_GAME)
    var suave by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(rv) {
        val r = rv ?: return@LaunchedEffect
        val az = orientacao(r.valores)[0]
        suave = suave?.let { s -> val d = ((az - s + 540) % 360) - 180; (s + d * 0.2f + 360) % 360 } ?: az
    }
    return suave to (rv?.precisao ?: 0)
}

private fun pontoCardeal(az: Float) = listOf("N", "NE", "L", "SE", "S", "SO", "O", "NO")[(((az + 22.5f) % 360) / 45).toInt()]

@Composable
private fun Bussola() {
    val c = LocalContext.current
    val (az, precisao) = rememberAzimute()
    val decl = remember { declinacao(c) }
    var verdadeiro by rememberSaveable { mutableStateOf(true) }
    val rumo = az?.let { if (verdadeiro && decl != null) (it + decl + 360) % 360 else it }
    FtColuna {
        if (rumo == null) { OdEstadoRecurso(OdTipoEstado.Medindo, "Lendo sensores", "Segure o celular deitado."); return@FtColuna }
        OdBussola(rumo, Modifier.fillMaxWidth())
        FtPainel { OdLeitura("${odFormatar(rumo.toDouble(), 0)}° ${pontoCardeal(rumo)}", tamanho = OdTamanhoLeitura.Destaque) }
        if (decl != null) io.github.andrexagris.opendesign.OdSegmentado(listOf(true to "Norte verdadeiro", false to "Norte magnético"), verdadeiro, { verdadeiro = it })
        FtInfos(null, listOf(
            "Declinação magnética aqui" to decl?.let { "${num(it, 1)}°" },
            "Calibração" to when (precisao) { SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Boa"; SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Média"; else -> "Ruim: mova o celular em forma de 8" },
        ))
        if (decl == null) FtNota("Sem a localização, a bússola mostra o norte magnético. Abra uma ferramenta de GPS uma vez para ter o norte verdadeiro.")
        FtNota("Segure deitado e longe de ímãs, alto-falantes e metal.")
    }
}

/** Seta que aponta um ângulo (0 = para cima), para "voltar ao ponto". */
@Composable
private fun Seta(angulo: Float) {
    val c = OdTema.cores
    Canvas(Modifier.size(180.dp).semantics { contentDescription = "Seta apontando ${angulo.toInt()} graus" }) {
        rotate(angulo) {
            val w = size.width; val h = size.height
            val p = Path().apply { moveTo(w / 2, h * 0.08f); lineTo(w * 0.78f, h * 0.62f); lineTo(w / 2, h * 0.5f); lineTo(w * 0.22f, h * 0.62f); close() }
            drawPath(p, c.escalaReferencia)
            drawCircle(c.escalaTrilho, w * 0.46f, style = Stroke(2.dp.toPx()))
        }
    }
}

@Composable
private fun VoltarAoPonto() {
    val c = LocalContext.current
    val banco = remember { BancoDados.de(c).itens() }
    val pontos by produceState(emptyList<ItemSalvo>()) { banco.observar(Tipos.PONTO).collect { value = it } }
    val local by rememberLocalizacao()
    val (az, _) = rememberAzimute()
    var alvo by rememberSaveable { mutableStateOf<Long?>(null) }
    var nome by rememberSaveable { mutableStateOf("") }
    val escopo = rememberCoroutineScope()
    val toasts = LocalToasts.current
    val p = pontos.firstOrNull { it.id == alvo }
    FtColuna {
        val l = local
        if (p != null && l != null) {
            val f = p.campos()
            val destino = Location("").apply { latitude = f["lat"]!!.toDouble(); longitude = f["lon"]!!.toDouble() }
            val dist = l.distanceTo(destino)
            val rumo = (l.bearingTo(destino) + 360) % 360
            Seta(((rumo - (az ?: 0f)) + 360) % 360)
            FtPainel(titulo = p.nome) { OdLeitura(if (dist >= 1000) num(dist / 1000.0, 2) else num(dist, 0), unidade = if (dist >= 1000) "km" else "m", tamanho = OdTamanhoLeitura.Destaque) }
            FtInfos(null, listOf("Direção" to "${num(rumo, 0)}° ${pontoCardeal(rumo)}", "Precisão do GPS" to "± ${num(l.accuracy, 0)} m"))
            OdBotao("Escolher outro ponto", { alvo = null }, Modifier.fillMaxWidth())
        } else {
            if (l == null) OdEstadoRecurso(OdTipoEstado.Medindo, "Procurando posição", "Espere o GPS achar a posição para marcar um ponto.")
            FtCampo(nome, { nome = it }, "Nome do ponto (ex.: carro)")
            OdBotao("Marcar aqui", {
                l ?: return@OdBotao
                escopo.launch { banco.salvar(ItemSalvo.de(Tipos.PONTO, nome.ifBlank { "Ponto" }, mapOf("lat" to l.latitude, "lon" to l.longitude))); nome = ""; toasts.mostrar("Ponto marcado.") }
            }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario, habilitado = l != null)
            if (pontos.isNotEmpty()) OdGrupoConfig("Voltar para") {
                pontos.forEach { pt ->
                    val f = pt.campos()
                    val dist = l?.let { FloatArray(1).also { r -> Location.distanceBetween(it.latitude, it.longitude, f["lat"]!!.toDouble(), f["lon"]!!.toDouble(), r) }[0] }
                    io.github.andrexagris.opendesign.OdLinhaValor(pt.nome, dist?.let { if (it >= 1000) "${num(it / 1000.0, 1)} km" else "${num(it, 0)} m" }, { alvo = pt.id })
                }
            }
        }
        FtNota("A seta usa a bússola: segure o celular deitado e gire o corpo até a seta apontar para cima.")
    }
}

@Composable
private fun Velocimetro() {
    val local by rememberLocalizacao(500)
    var maxima by remember { mutableStateOf(0f) }
    var distancia by remember { mutableStateOf(0.0) }
    var anterior by remember { mutableStateOf<Location?>(null) }
    val inicio = remember { System.currentTimeMillis() }
    io.github.andrexagris.ferramentas.base.ManterTelaLigada()
    LaunchedEffect(local) {
        val l = local ?: return@LaunchedEffect
        if (l.hasSpeed()) maxima = maxOf(maxima, l.speed * 3.6f)
        anterior?.let { a -> val d = a.distanceTo(l); if (d > 2 && l.accuracy < 25) distancia += d }
        anterior = l
    }
    val kmh = local?.takeIf { it.hasSpeed() }?.speed?.times(3.6f) ?: 0f
    FtColuna {
        OdLeitura(odFormatar(kmh.toDouble(), 0), unidade = "km/h", tamanho = OdTamanhoLeitura.Destaque, largura = 4)
        OdEscalaArco(kmh, 0f, if (maxima > 120) 240f else 120f)
        FtInfos(null, listOf("Máxima" to "${num(maxima, 0)} km/h", "Distância" to "${num(distancia / 1000, 2)} km",
            "Média" to "${num(distancia / ((System.currentTimeMillis() - inicio) / 3_600_000.0) / 1000, 0)} km/h", "Precisão" to local?.let { "± ${num(it.accuracy, 0)} m" }))
        FtNota("Funciona melhor a céu aberto. Em túneis e entre prédios a velocidade some ou pula.")
    }
}

@Composable
private fun MedidorArea() {
    val local by rememberLocalizacao()
    val pontos = remember { mutableStateListOf<Pair<Double, Double>>() }
    // Área pela fórmula do laço (shoelace) numa projeção local em metros.
    val (area, perimetro) = if (pontos.size < 3) 0.0 to 0.0 else {
        val lat0 = Math.toRadians(pontos.map { it.first }.average())
        val xy = pontos.map { (la, lo) -> Math.toRadians(lo) * 6371000 * cos(lat0) to Math.toRadians(la) * 6371000 }
        val a = abs(xy.indices.sumOf { i -> val (x1, y1) = xy[i]; val (x2, y2) = xy[(i + 1) % xy.size]; x1 * y2 - x2 * y1 }) / 2
        val per = xy.indices.sumOf { i -> val (x1, y1) = xy[i]; val (x2, y2) = xy[(i + 1) % xy.size]; Math.hypot(x2 - x1, y2 - y1) }
        a to per
    }
    FtColuna {
        FtPainel(titulo = "Área") {
            OdLeitura(if (area >= 10000) num(area / 10000, 3) else num(area, 0), unidade = if (area >= 10000) "hectares" else "m²", tamanho = OdTamanhoLeitura.Destaque)
            OdLeitura(num(perimetro, 0), unidade = "m de perímetro")
        }
        FtInfos(null, listOf("Pontos" to pontos.size.toString(), "Precisão do GPS agora" to local?.let { "± ${num(it.accuracy, 0)} m" }))
        FtBotoes {
            OdBotao("Marcar ponto", { local?.let { pontos += it.latitude to it.longitude } }, Modifier.weight(1f), tipo = OdTipoBotao.Primario, habilitado = local != null)
            OdBotao("Recomeçar", { pontos.clear() }, Modifier.weight(1f))
        }
        FtNota("Ande pelo contorno e marque cada canto. O erro do GPS (vários metros) pesa muito em áreas pequenas: use para terrenos, não para cômodos.")
    }
}

private fun gpx(pontos: List<io.github.andrexagris.ferramentas.servicos.PontoTrajeto>): String {
    val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    return buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<gpx version="1.1" creator="Ferramentas" xmlns="http://www.topografix.com/GPX/1/1"><trk><name>Trajeto</name><trkseg>""")
        pontos.forEach { p -> appendLine("""<trkpt lat="${p.lat}" lon="${p.lon}">${p.alt?.let { "<ele>$it</ele>" } ?: ""}<time>${f.format(Date(p.tempo))}</time></trkpt>""") }
        appendLine("</trkseg></trk></gpx>")
    }
}

@Composable
private fun Trajeto() {
    val c = LocalContext.current
    val tarefa by ServicoMedicao.estadoAtual.collectAsState()
    val t = tarefa as? TarefaMedicao.Trajeto
    var gravando by rememberSaveable { mutableStateOf(false) }
    val comNotificacao = rememberComPermissao(Permissao.Notificacoes)
    FtColuna {
        FtBotaoMedir(gravando && t != null, { liga ->
            gravando = liga
            if (liga) comNotificacao { ServicoMedicao.iniciar(c, ServicoMedicao.ACAO_TRAJETO) } else ServicoMedicao.parar(c)
        }, iniciar = "Gravar trajeto")
        if (t != null) {
            FtPainel(titulo = "Distância") { OdLeitura(num(t.distanciaM / 1000, 2), unidade = "km", tamanho = OdTamanhoLeitura.Destaque) }
            val dur = (t.pontos.lastOrNull()?.tempo ?: t.inicio) - t.inicio
            FtInfos(null, listOf("Duração" to formatarDuracao(dur), "Pontos" to t.pontos.size.toString(),
                "Velocidade média" to if (dur > 0) "${num(t.distanciaM / (dur / 1000.0) * 3.6, 1)} km/h" else null))
            OdBotao("Exportar GPX", { compartilharArquivo(c, "trajeto-${t.inicio}.gpx", "application/gpx+xml") { it.writeText(gpx(t.pontos)) } }, Modifier.fillMaxWidth(), habilitado = t.pontos.size > 1)
        }
        FtNota("Continua gravando com a tela apagada. O arquivo GPX abre no OsmAnd, Strava, Google Earth e outros.")
    }
}

