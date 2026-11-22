package io.github.andrexagris.ferramentas.ui.ferramentas.ambiente

import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Range
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.andrexagris.ferramentas.base.AjustesCamera2
import io.github.andrexagris.ferramentas.base.MinMax
import io.github.andrexagris.ferramentas.base.Serie
import io.github.andrexagris.ferramentas.base.VistaCamera
import io.github.andrexagris.ferramentas.base.bipe
import io.github.andrexagris.ferramentas.base.espectro
import io.github.andrexagris.ferramentas.base.exportarCsv
import io.github.andrexagris.ferramentas.base.inclinacaoObturador
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.rememberSensor
import io.github.andrexagris.ferramentas.dados.Amostra
import io.github.andrexagris.ferramentas.dados.BancoDados
import io.github.andrexagris.ferramentas.dados.Series
import io.github.andrexagris.ferramentas.dados.rememberAjusteBool
import io.github.andrexagris.ferramentas.dados.rememberAjusteFloat
import io.github.andrexagris.ferramentas.servicos.ChavesPeriodicas
import io.github.andrexagris.ferramentas.servicos.TrabalhoPeriodico
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotoes
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtDeslizante
import io.github.andrexagris.ferramentas.ui.ferramentas.FtGraficoHistorico
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.ferramentas.ui.ferramentas.movimento.TelaTresEixos
import io.github.andrexagris.ferramentas.ui.ferramentas.numero
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEscalaArco
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdFaixa
import io.github.andrexagris.opendesign.OdGraficoTempoReal
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdInstrumento
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaAcao
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdLinhaInterruptor
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.OdTipoFaixa
import io.github.andrexagris.opendesign.odFormatar
import io.github.andrexagris.opendesign.odSerieResultante
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sqrt

fun telasAmbiente(): Map<String, TelaDeFerramenta> = mapOf(
    "ambiente.metais" com { DetectorMetais() },
    "ambiente.magnetometro" com { TelaTresEixos(Sensor.TYPE_MAGNETIC_FIELD, "µT", 100f, 1) },
    "ambiente.luximetro" com { Luximetro() },
    "ambiente.flicker" com { DetectorFlicker() },
    "ambiente.fotometro" com { Fotometro() },
    "ambiente.barometro" com { Barometro() },
    "ambiente.tendencia" com { TendenciaTempo() },
    "ambiente.termometro" com { SensorSimples(Sensor.TYPE_AMBIENT_TEMPERATURE, "Temperatura", "°C", -10f, 50f, 1) },
    "ambiente.higrometro" com { Higrometro() },
)

@Composable
private fun DetectorMetais() {
    val l by rememberSensor(Sensor.TYPE_MAGNETIC_FIELD, SensorManager.SENSOR_DELAY_GAME)
    var base by rememberAjusteFloat("ambiente.metais.base", 0f)
    var som by rememberSaveable { mutableStateOf(true) }
    val serie = remember { Serie(200) }
    val total = l?.let { sqrt(it[0] * it[0] + it[1] * it[1] + it[2] * it[2]) } ?: 0f
    LaunchedEffect(total) { if (base == 0f && total > 0) base = total; serie.add(total - base) }
    val dif = abs(total - base)
    LaunchedEffect(som) { while (som) { if (dif > 8) bipe(500.0 + dif * 8, 50); delay((600 - dif * 8).toLong().coerceIn(80, 600)) } }
    FtColuna {
        OdInstrumento(if (dif > 25) "Metal muito perto" else if (dif > 8) "Metal detectado" else "Nada por perto", dif > 8, leitura = {
            OdLeitura(odFormatar(dif.toDouble(), 0), unidade = "µT de diferença", largura = 5, tamanho = OdTamanhoLeitura.Destaque)
        }) { OdEscalaArco(dif.coerceAtMost(100f), 0f, 100f, faixas = listOf(OdFaixa(8f, 100f, OdTipoFaixa.Ok))) }
        OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "µT")), -60f, 60f, janela = 200, unidade = "µT", casas = 0, rotuloTempo = null)
        FtInfos(null, listOf("Campo total" to "${num(total, 1)} µT", "Referência" to "${num(base, 1)} µT"))
        FtBotoes {
            OdBotao("Zerar aqui", { base = total }, Modifier.weight(1f))
            OdBotao(if (som) "Sem som" else "Com som", { som = !som }, Modifier.weight(1f))
        }
        FtNota("Zere longe de metais e passe o topo do celular (onde fica o magnetômetro) perto da parede. Só acha metais ferromagnéticos: ferro e aço, como pregos e canos.")
    }
}

@Composable
private fun Luximetro() {
    val c = LocalContext.current
    val l by rememberSensor(Sensor.TYPE_LIGHT)
    var fator by rememberAjusteFloat("ambiente.luximetro.fator", 1f)
    val mm = remember { MinMax() }
    val serie = remember { Serie(120) }
    val lux = (l?.get(0) ?: 0f) * fator
    LaunchedEffect(l) { l?.let { mm.add(lux); serie.add(lux) } }
    val referencia = when { lux < 50 -> "Penumbra"; lux < 200 -> "Ambiente doméstico"; lux < 500 -> "Bom para ler"; lux < 1000 -> "Escritório bem iluminado"; lux < 10000 -> "Dia nublado ou sombra"; else -> "Sol direto" }
    FtColuna {
        FtPainel(titulo = referencia) { OdLeitura(odFormatar(lux.toDouble(), 0), unidade = "lux", tamanho = OdTamanhoLeitura.Destaque) }
        OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "lux")), 0f, (serie.pontos.maxOrNull() ?: 100f).coerceAtLeast(100f) * 1.2f, janela = 120, unidade = "lux", casas = 0, rotuloTempo = null)
        FtInfos(null, listOf("Mínimo" to mm.min.value.takeIf { !it.isNaN() }?.let { "${num(it, 0)} lux" }, "Máximo" to mm.max.value.takeIf { !it.isNaN() }?.let { "${num(it, 0)} lux" }))
        FtDeslizante("Calibração", fator, { fator = it }, 0.5f..2f, "× ${num(fator, 2)}")
        FtBotoes {
            OdBotao("Zerar mín./máx.", { mm.zerar() }, Modifier.weight(1f))
            OdBotao("Exportar CSV", { exportarCsv(c, "lux", listOf("indice", "lux"), serie.pontos.mapIndexed { i, v -> listOf(i, v) }) }, Modifier.weight(1f))
        }
        FtNota("O sensor fica perto da câmera frontal. Compare com um luxímetro de verdade e ajuste a calibração. Referências: 300 lux para trabalhar, 500 para ler.")
    }
}

@Composable
private fun DetectorFlicker() {
    var freq by remember { mutableStateOf<Double?>(null) }
    var indice by remember { mutableStateOf<Double?>(null) }
    var inclinacao by remember { mutableStateOf<Long?>(null) }
    val ajustes = remember { AjustesCamera2(exposicaoNs = 250_000L, iso = 800, fps = Range(30, 30), aoResultado = { r -> r.inclinacaoObturador()?.let { inclinacao = it } }) }
    FtColuna(rolar = false) {
        Box(Modifier.fillMaxWidth().aspectRatio(3f / 4f)) {
            VistaCamera(Modifier.fillMaxWidth().aspectRatio(3f / 4f), ajustes = ajustes) { img ->
                // Média de cada linha: faixas claras e escuras aparecem quando a luz pisca durante a leitura do sensor.
                val p = img.planes[0]; val buf = p.buffer
                val n = 512.coerceAtMost(img.height)
                val linhas = FloatArray(n) { r ->
                    val y = r * img.height / n
                    var s = 0L
                    var x = 0
                    while (x < img.width) { s += buf.get(y * p.rowStride + x * p.pixelStride).toInt() and 0xFF; x += 8 }
                    s.toFloat() / (img.width / 8)
                }
                val media = linhas.average().toFloat()
                if (media < 8) return@VistaCamera
                val sinal = FloatArray(n) { linhas[it] - media }
                val esp = espectro(sinal)
                val pico = (3 until esp.size / 2).maxByOrNull { esp[it] }!!
                indice = esp[pico] / media * 100
                val skew = inclinacao
                freq = if (skew != null && esp[pico] / media > 0.01) {
                    val nsPorLinha = skew.toDouble() / img.height
                    val periodoLinhas = n.toDouble() / pico * (img.height.toDouble() / n)
                    1e9 / (periodoLinhas * nsPorLinha)
                } else null
            }
        }
        FtPainel {
            OdLeitura(indice?.let { num(it, 1) } ?: "—", unidade = "% de cintilação", tamanho = OdTamanhoLeitura.Destaque)
            OdLeitura(freq?.let { num(it, 0) } ?: "—", unidade = "Hz")
        }
        FtNota("Aponte para a lâmpada ou para uma parede iluminada por ela. Acima de uns 5% a cintilação pode cansar a vista. A frequência só aparece se a câmera informar o tempo de leitura do sensor.")
    }
}

@Composable
private fun Fotometro() {
    val l by rememberSensor(Sensor.TYPE_LIGHT)
    var iso by rememberSaveable { mutableStateOf(100) }
    val lux = l?.get(0) ?: 0f
    val ev100 = if (lux > 0) log2(lux / 2.5) else 0.0
    val ev = ev100 + log2(iso / 100.0)
    val velocidades = listOf(1.0 / 8000, 1.0 / 4000, 1.0 / 2000, 1.0 / 1000, 1.0 / 500, 1.0 / 250, 1.0 / 125, 1.0 / 60, 1.0 / 30, 1.0 / 15, 1.0 / 8, 1.0 / 4, 0.5, 1.0, 2.0, 4.0, 8.0, 15.0, 30.0)
    fun texto(t: Double) = if (t >= 1) "${t.toInt()} s" else "1/${(1 / t).toInt()}"
    FtColuna {
        FtPainel(titulo = "Exposição") {
            OdLeitura(num(ev, 1), unidade = "EV (ISO $iso)", tamanho = OdTamanhoLeitura.Destaque)
            OdLeitura(odFormatar(lux.toDouble(), 0), unidade = "lux")
        }
        OdSegmentado(listOf(100 to "100", 200 to "200", 400 to "400", 800 to "800", 1600 to "1600"), iso, { iso = it })
        OdGrupoConfig("Abertura e velocidade") {
            listOf(1.4, 2.0, 2.8, 4.0, 5.6, 8.0, 11.0, 16.0).forEach { n ->
                val t = n * n / 2.0.pow(ev)
                val perto = velocidades.minBy { abs(ln(it) - ln(t)) }
                OdLinhaConfig("f/${num(n, 1)}") { androidx.compose.material3.Text(texto(perto), style = io.github.andrexagris.opendesign.OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum")) }
            }
        }
        FtNota("Mede a luz que chega ao celular (luz incidente): aponte o sensor, perto da câmera frontal, do assunto para a câmera.")
    }
}

@Composable
private fun Barometro() {
    val p by rememberSensor(Sensor.TYPE_PRESSURE)
    val serie = remember { Serie(120) }
    var altitude by rememberSaveable { mutableStateOf("0") }
    LaunchedEffect(p) { p?.let { serie.add(it[0]) } }
    val hpa = p?.get(0)
    val nivelMar = hpa?.let { h -> altitude.numero()?.let { a -> h / (1 - a / 44330.0).pow(5.255) } }
    FtColuna {
        FtPainel(titulo = "Pressão") { OdLeitura(hpa?.let { odFormatar(it.toDouble(), 1) } ?: "—", unidade = "hPa", tamanho = OdTamanhoLeitura.Destaque) }
        val m = serie.pontos.minOrNull() ?: 1000f
        OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "hPa")), m - 1f, (serie.pontos.maxOrNull() ?: m) + 1f, janela = 120, unidade = "hPa", casas = 1, rotuloTempo = null)
        FtCampo(altitude, { altitude = it }, "Sua altitude", numerico = true, sufixo = "m")
        FtInfos(null, listOf("Ao nível do mar" to nivelMar?.let { "${num(it, 1)} hPa" }))
    }
}

@Composable
private fun TendenciaTempo() {
    val c = LocalContext.current
    var ativo by rememberAjusteBool(ChavesPeriodicas.HISTORICO_PRESSAO, false)
    val amostras by produceState(emptyList<Amostra>()) { BancoDados.de(c).amostras().observar(Series.PRESSAO, System.currentTimeMillis() - 48L * 3600 * 1000).collect { value = it } }
    val ultima = amostras.lastOrNull()
    val ha3h = amostras.lastOrNull { it.tempo <= System.currentTimeMillis() - 3 * 3600 * 1000 }
    val delta = if (ultima != null && ha3h != null) ultima.v1 - ha3h.v1 else null
    val previsao = when { delta == null -> "Precisa de 3 horas de histórico"; delta > 1.6 -> "Tempo melhorando"; delta < -1.6 -> "Tempo piorando: chance de chuva"; else -> "Tempo estável" }
    FtColuna {
        OdGrupoConfig(null) { OdLinhaInterruptor("Registrar a pressão", ativo, { ativo = it; TrabalhoPeriodico.agendar(c) }, "Uma leitura a cada 15 minutos") }
        FtPainel(titulo = previsao) { OdLeitura(delta?.let { odFormatar(it, 1, io.github.andrexagris.opendesign.OdSinal.Sempre) } ?: "—", unidade = "hPa em 3 h", tamanho = OdTamanhoLeitura.Destaque) }
        FtGraficoHistorico(amostras.map { it.tempo to it.v1.toFloat() }, descricao = "Pressão nas últimas 48 horas")
        FtNota("Pressão caindo mais de 1,6 hPa em 3 horas costuma indicar frente fria ou chuva; subindo, tempo firme.")
    }
}

@Composable
private fun SensorSimples(tipo: Int, titulo: String, unidade: String, min: Float, max: Float, casas: Int) {
    val l by rememberSensor(tipo)
    val serie = remember { Serie(120) }
    LaunchedEffect(l) { l?.let { serie.add(it[0]) } }
    FtColuna {
        FtPainel(titulo = titulo) { OdLeitura(l?.let { odFormatar(it[0].toDouble(), casas) } ?: "—", unidade = unidade, tamanho = OdTamanhoLeitura.Destaque) }
        OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), unidade)), min, max, janela = 120, unidade = unidade, casas = casas, rotuloTempo = null)
    }
}

@Composable
private fun Higrometro() {
    val u by rememberSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
    val t by rememberSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    var tempTexto by rememberSaveable { mutableStateOf("25") }
    val ur = u?.get(0)
    val temp = t?.get(0)?.toDouble() ?: tempTexto.numero()
    // Ponto de orvalho pela fórmula de Magnus.
    val orvalho = if (ur != null && temp != null && ur > 0) { val g = ln(ur / 100.0) + 17.62 * temp / (243.12 + temp); 243.12 * g / (17.62 - g) } else null
    FtColuna {
        FtPainel(titulo = "Umidade relativa") { OdLeitura(ur?.let { odFormatar(it.toDouble(), 0) } ?: "—", unidade = "%", tamanho = OdTamanhoLeitura.Destaque) }
        if (t == null) FtCampo(tempTexto, { tempTexto = it }, "Temperatura do ar", numerico = true, sufixo = "°C")
        FtInfos(null, listOf("Ponto de orvalho" to orvalho?.let { "${num(it, 1)} °C" }, "Conforto" to ur?.let { when { it < 30 -> "Seco"; it > 70 -> "Úmido"; else -> "Confortável" } }))
    }
}

