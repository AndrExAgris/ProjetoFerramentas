package io.github.andrexagris.ferramentas.ui.ferramentas.movimento

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.andrexagris.ferramentas.base.MinMax
import io.github.andrexagris.ferramentas.base.Serie
import io.github.andrexagris.ferramentas.base.espectro
import io.github.andrexagris.ferramentas.base.exportarCsv
import io.github.andrexagris.ferramentas.base.fluxoSensor
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.rememberSensor
import io.github.andrexagris.ferramentas.base.rememberSeriesXyz
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotaoMedir
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotoes
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.ferramentas.ui.ferramentas.numero
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdGraficoTempoReal
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.odFormatar
import io.github.andrexagris.opendesign.odSerieResultante
import io.github.andrexagris.opendesign.odSerieX
import io.github.andrexagris.opendesign.odSerieY
import io.github.andrexagris.opendesign.odSerieZ
import kotlinx.coroutines.flow.conflate
import kotlin.math.PI
import kotlin.math.sqrt

fun telasMovimento(): Map<String, TelaDeFerramenta> = mapOf(
    "movimento.acelerometro" com { Acelerometro() },
    "movimento.vibrometro" com { Vibrometro() },
    "movimento.velocidade" com { VelocidadeAcelerometro() },
    "movimento.giroscopio" com { Giroscopio() },
    "movimento.andares" com { ContadorAndares() },
    "movimento.pendulo" com { Pendulo() },
)

private fun mod(v: FloatArray) = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])

/** Tela padrão de sensor de 3 eixos: valores, gráfico X/Y/Z, módulo, mínimo/máximo e exportação. */
@Composable
fun TelaTresEixos(tipo: Int, unidade: String, limite: Float, casas: Int = 2, extra: @Composable (FloatArray?) -> Unit = {}) {
    val c = LocalContext.current
    val l by rememberSensor(tipo, SensorManager.SENSOR_DELAY_GAME)
    val (x, y, z) = rememberSeriesXyz(l, 150)
    val mm = remember { MinMax() }
    val registro = remember { mutableStateListOf<List<Any?>>() }
    var gravando by remember { mutableStateOf(false) }
    LaunchedEffect(l) {
        val v = l ?: return@LaunchedEffect
        mm.add(mod(v.valores))
        if (gravando && registro.size < 200_000) registro += listOf(System.currentTimeMillis(), v[0], v[1], v[2])
    }
    val v = l?.valores
    FtColuna {
        FtPainel {
            OdLeitura(v?.let { odFormatar(it[0].toDouble(), casas, largura = 7) } ?: "—", unidade = "X $unidade", largura = 7)
            OdLeitura(v?.let { odFormatar(it[1].toDouble(), casas, largura = 7) } ?: "—", unidade = "Y $unidade", largura = 7)
            OdLeitura(v?.let { odFormatar(it[2].toDouble(), casas, largura = 7) } ?: "—", unidade = "Z $unidade", largura = 7)
        }
        OdGraficoTempoReal(listOf(odSerieX(x.pontos.toList()), odSerieY(y.pontos.toList()), odSerieZ(z.pontos.toList())), -limite, limite, janela = 150, unidade = unidade, casas = casas, rotuloTempo = "−3 s" to "agora")
        extra(v)
        FtInfos("Módulo", listOf("Agora" to v?.let { "${num(mod(it), casas)} $unidade" }, "Mínimo" to mm.min.value.takeIf { !it.isNaN() }?.let { "${num(it, casas)} $unidade" }, "Máximo" to mm.max.value.takeIf { !it.isNaN() }?.let { "${num(it, casas)} $unidade" }))
        FtBotaoMedir(gravando, { gravando = it; if (it) registro.clear() }, iniciar = "Gravar leituras", parar = "Parar gravação")
        if (!gravando && registro.isNotEmpty()) OdBotao("Exportar ${registro.size} leituras (CSV)", { exportarCsv(c, "sensor", listOf("tempo_ms", "x", "y", "z"), registro.toList()) }, Modifier.fillMaxWidth())
        OdBotao("Zerar mínimo e máximo", { mm.zerar() }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun Acelerometro() {
    TelaTresEixos(Sensor.TYPE_ACCELEROMETER, "m/s²", 20f) { v ->
        FtPainel(titulo = "Força G") { OdLeitura(v?.let { odFormatar((mod(it) / SensorManager.GRAVITY_EARTH).toDouble(), 2) } ?: "—", unidade = "G", tamanho = OdTamanhoLeitura.Destaque) }
    }
}

@Composable
private fun Giroscopio() {
    TelaTresEixos(Sensor.TYPE_GYROSCOPE, "rad/s", 10f) { v ->
        FtPainel(titulo = "Rotação") { OdLeitura(v?.let { odFormatar((mod(it) * 60 / (2 * PI)), 1) } ?: "—", unidade = "RPM", tamanho = OdTamanhoLeitura.Destaque) }
    }
}

@Composable
private fun Vibrometro() {
    val c = LocalContext.current
    val sismo = remember { Serie(300) }
    var freq by remember { mutableStateOf<Double?>(null) }
    var rms by remember { mutableStateOf(0.0) }
    var taxa by remember { mutableStateOf(0.0) }
    LaunchedEffect(Unit) {
        val buf = ArrayDeque<Pair<Long, Float>>()
        c.fluxoSensor(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_FASTEST).collect { l ->
            buf.addLast(l.tempoNs to mod(l.valores))
            if (buf.size > 512) buf.removeFirst()
            if (buf.size % 8 == 0 && buf.size >= 256) {
                val janela = buf.toList().takeLast(256)
                val media = janela.map { it.second }.average().toFloat()
                val sinal = FloatArray(256) { janela[it].second - media }
                taxa = 255 / ((janela.last().first - janela.first().first) / 1e9)
                rms = sqrt(sinal.map { (it * it).toDouble() }.average())
                val esp = espectro(sinal)
                val pico = (2 until esp.size).maxByOrNull { esp[it] }!!
                freq = if (esp[pico] > 0.02) pico * taxa / 256 else null
                sismo.add(sinal.last())
            } else if (buf.size >= 2) sismo.add(buf.last().second - buf.map { it.second }.average().toFloat())
        }
    }
    FtColuna {
        FtPainel(titulo = "Frequência dominante") { OdLeitura(freq?.let { num(it, 1) } ?: "—", unidade = "Hz", tamanho = OdTamanhoLeitura.Destaque) }
        OdGraficoTempoReal(listOf(odSerieResultante(sismo.pontos.toList(), "m/s²")), -2f, 2f, janela = 300, unidade = "m/s²", casas = 2, rotuloTempo = null)
        FtInfos(null, listOf("Intensidade (RMS)" to "${num(rms, 3)} m/s²", "Taxa do sensor" to "${num(taxa, 0)} Hz", "Maior frequência medível" to "${num(taxa / 2, 0)} Hz"))
        FtNota("Apoie o celular na superfície que vibra (máquina de lavar, motor). O sensor só enxerga vibrações até metade da taxa de amostragem.")
    }
}

@Composable
private fun VelocidadeAcelerometro() {
    val c = LocalContext.current
    var v by remember { mutableStateOf(floatArrayOf(0f, 0f, 0f)) }
    var medindo by remember { mutableStateOf(false) }
    var maxima by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(medindo) {
        if (!medindo) return@LaunchedEffect
        var t0 = 0L
        c.fluxoSensor(Sensor.TYPE_LINEAR_ACCELERATION, SensorManager.SENSOR_DELAY_GAME).collect { l ->
            if (t0 != 0L) {
                val dt = (l.tempoNs - t0) / 1e9f
                v = FloatArray(3) { i -> v[i] + (if (kotlin.math.abs(l[i]) > 0.05f) l[i] else 0f) * dt }
                maxima = maxOf(maxima, mod(v))
            }
            t0 = l.tempoNs
        }
    }
    FtColuna {
        FtPainel(titulo = "Velocidade") { OdLeitura(odFormatar((mod(v) * 3.6).toDouble(), 1), unidade = "km/h", tamanho = OdTamanhoLeitura.Destaque); OdLeitura(odFormatar(mod(v).toDouble(), 2), unidade = "m/s") }
        FtInfos(null, listOf("Máxima" to "${num(maxima * 3.6, 1)} km/h"))
        FtBotoes {
            OdBotao(if (medindo) "Parar" else "Iniciar", { medindo = !medindo }, Modifier.weight(1f), tipo = io.github.andrexagris.opendesign.OdTipoBotao.Primario)
            OdBotao("Zerar", { v = floatArrayOf(0f, 0f, 0f); maxima = 0f }, Modifier.weight(1f))
        }
        FtNota("Integra a aceleração ao longo do tempo. O erro cresce rápido (em segundos), então serve só para movimentos curtos, como um arremesso ou uma arrancada.")
    }
}

@Composable
private fun ContadorAndares() {
    val p by rememberSensor(Sensor.TYPE_PRESSURE)
    var referencia by remember { mutableStateOf<Float?>(null) }
    var alturaAndar by rememberSaveable { mutableStateOf("3") }
    LaunchedEffect(p) { if (referencia == null) p?.let { referencia = it[0] } }
    val h = if (p != null && referencia != null) SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, p!![0]) - SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, referencia!!) else 0f
    val andar = alturaAndar.numero()?.takeIf { it > 0 }?.let { h / it }
    FtColuna {
        FtPainel(titulo = "Andares desde o início") { OdLeitura(andar?.let { odFormatar(it, 1, io.github.andrexagris.opendesign.OdSinal.Sempre) } ?: "—", tamanho = OdTamanhoLeitura.Destaque) }
        FtInfos(null, listOf("Desnível" to "${num(h, 1)} m", "Pressão" to p?.let { "${num(it[0], 2)} hPa" }))
        FtCampo(alturaAndar, { alturaAndar = it }, "Altura de um andar", numerico = true, sufixo = "m")
        OdBotao("Zerar aqui", { referencia = p?.get(0) }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun Pendulo() {
    val c = LocalContext.current
    var comprimento by rememberSaveable { mutableStateOf("1.0") }
    var medindo by remember { mutableStateOf(false) }
    var periodo by remember { mutableStateOf<Double?>(null) }
    val serie = remember { Serie(300) }
    LaunchedEffect(medindo) {
        if (!medindo) return@LaunchedEffect
        val amostras = ArrayList<Pair<Long, FloatArray>>()
        c.fluxoSensor(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME).conflate().collect { l ->
            amostras += l.tempoNs to l.valores
            if (amostras.size > 1500) amostras.removeAt(0)
            if (amostras.size > 200) {
                // Eixo com maior variação, sem a média: cruza o zero duas vezes por período.
                val eixo = (0..2).maxBy { i -> val m = amostras.map { it.second[i] }.average(); amostras.sumOf { (it.second[i] - m) * (it.second[i] - m) } }
                val media = amostras.map { it.second[eixo] }.average().toFloat()
                serie.add(l[eixo] - media)
                val cruzamentos = amostras.zipWithNext().filter { (a, b) -> (a.second[eixo] - media) < 0 && (b.second[eixo] - media) >= 0 }.map { it.second.first }
                if (cruzamentos.size >= 4) periodo = (cruzamentos.last() - cruzamentos.first()) / 1e9 / (cruzamentos.size - 1)
            }
        }
    }
    val l = comprimento.numero()
    val g = if (l != null && periodo != null) 4 * PI * PI * l / (periodo!! * periodo!!) else null
    FtColuna {
        FtPainel(titulo = "Gravidade medida") { OdLeitura(g?.let { num(it, 2) } ?: "—", unidade = "m/s²", tamanho = OdTamanhoLeitura.Destaque) }
        OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "a")), -5f, 5f, janela = 300, unidade = "m/s²", rotuloTempo = null)
        FtInfos(null, listOf("Período" to periodo?.let { "${num(it, 3)} s" }, "Valor de referência" to "9,81 m/s²"))
        FtCampo(comprimento, { comprimento = it }, "Comprimento do fio até o centro do celular", numerico = true, sufixo = "m")
        FtBotaoMedir(medindo, { medindo = it; if (it) { periodo = null; serie.limpar() } })
        FtNota("Pendure o celular por um fio (meia de nylon funciona), solte com pouca amplitude (menos de 15°) e meça. g = 4π²L/T².")
    }
}
