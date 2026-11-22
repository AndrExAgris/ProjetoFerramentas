package io.github.andrexagris.ferramentas.ui.ferramentas.utilidades

import android.media.RingtoneManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.andrexagris.ferramentas.base.CapturarTeclas
import io.github.andrexagris.ferramentas.base.formatarCronometro
import io.github.andrexagris.ferramentas.base.localeBr
import io.github.andrexagris.ferramentas.dados.rememberAjusteFloat
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotaoMedir
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotoes
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.ferramentas.ui.ferramentas.numero
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdLinhaOpcao
import io.github.andrexagris.opendesign.OdRaios
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.MathContext

fun telasUtilidades(): Map<String, TelaDeFerramenta> = mapOf(
    "utilidades.contador" com { Contador() },
    "utilidades.cronometro" com { CronometroTimer() },
    "utilidades.conversor" com { Conversor() },
)

@Composable
private fun Contador() {
    var valor by rememberAjusteFloat("utilidades.contador.valor", 0f)
    var passo by rememberSaveable { mutableStateOf(1) }
    val c = LocalContext.current
    val vib = remember { c.getSystemService(VibratorManager::class.java).defaultVibrator }
    fun mudar(d: Int) { valor += d; runCatching { vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)) } }
    CapturarTeclas { e ->
        when (e.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> { if (e.action == KeyEvent.ACTION_DOWN) mudar(passo); true }
            KeyEvent.KEYCODE_VOLUME_DOWN -> { if (e.action == KeyEvent.ACTION_DOWN) mudar(-passo); true }
            else -> false
        }
    }
    FtColuna {
        Box(
            Modifier.fillMaxWidth().height(260.dp).background(OdTema.cores.primaria.fundo, RoundedCornerShape(OdRaios.painel)).clickable { mudar(passo) }
                .semantics { contentDescription = "Contador: ${valor.toInt()}. Toque para somar $passo." },
            contentAlignment = Alignment.Center,
        ) { OdLeitura(valor.toInt().toString(), tamanho = OdTamanhoLeitura.Destaque) }
        FtBotoes {
            OdBotao("−$passo", { mudar(-passo) }, Modifier.weight(1f))
            OdBotao("Zerar", { valor = 0f }, Modifier.weight(1f))
        }
        OdSegmentado(listOf(1 to "Passo 1", 5 to "5", 10 to "10"), passo, { passo = it })
        FtNota("Toque na área grande ou use os botões de volume (+ soma, − subtrai), sem olhar para a tela.")
    }
}

@Composable
private fun CronometroTimer() {
    var modo by rememberSaveable { mutableStateOf("cronometro") }
    FtColuna {
        OdSegmentado(listOf("cronometro" to "Cronômetro", "timer" to "Timer"), modo, { modo = it })
        if (modo == "cronometro") Cronometro() else Timer()
    }
}

@Composable
private fun Cronometro() {
    var inicio by rememberSaveable { mutableLongStateOf(0L) }
    var acumulado by rememberSaveable { mutableLongStateOf(0L) }
    var rodando by rememberSaveable { mutableStateOf(false) }
    var agora by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val voltas = remember { mutableStateListOf<Long>() }
    LaunchedEffect(rodando) { while (rodando) { agora = SystemClock.elapsedRealtime(); delay(31) } }
    val tempo = acumulado + if (rodando) agora - inicio else 0
    FtPainel { OdLeitura(formatarCronometro(tempo), tamanho = OdTamanhoLeitura.Destaque) }
    FtBotoes {
        OdBotao(if (rodando) "Pausar" else if (tempo > 0) "Continuar" else "Iniciar", {
            if (rodando) { acumulado += SystemClock.elapsedRealtime() - inicio } else inicio = SystemClock.elapsedRealtime()
            rodando = !rodando
        }, Modifier.weight(1f), tipo = OdTipoBotao.Primario)
        OdBotao(if (rodando) "Volta" else "Zerar", { if (rodando) voltas.add(0, tempo) else { acumulado = 0; voltas.clear() } }, Modifier.weight(1f))
    }
    if (voltas.isNotEmpty()) OdGrupoConfig("Voltas") {
        voltas.forEachIndexed { i, t ->
            val anterior = voltas.getOrNull(i + 1) ?: 0
            OdLinhaConfig("Volta ${voltas.size - i}", descricao = "+${formatarCronometro(t - anterior)}") { Text(formatarCronometro(t), style = OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum")) }
        }
    }
}

@Composable
private fun Timer() {
    val c = LocalContext.current
    var minutos by rememberSaveable { mutableStateOf("5") }
    var segundos by rememberSaveable { mutableStateOf("0") }
    var fim by rememberSaveable { mutableLongStateOf(0L) }
    var agora by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var tocando by remember { mutableStateOf(false) }
    val rodando = fim > 0
    LaunchedEffect(fim) {
        while (fim > 0) {
            agora = SystemClock.elapsedRealtime()
            if (agora >= fim) {
                fim = 0; tocando = true
                val som = RingtoneManager.getRingtone(c, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                runCatching { som?.play() }
                runCatching { c.getSystemService(VibratorManager::class.java).defaultVibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 300, 500, 300, 500), -1)) }
                delay(8000); runCatching { som?.stop() }; tocando = false
            }
            delay(100)
        }
    }
    val restante = (fim - agora).coerceAtLeast(0)
    if (rodando || tocando) FtPainel { OdLeitura(if (tocando) "Tempo esgotado" else formatarCronometro(restante + 999, false), tamanho = OdTamanhoLeitura.Destaque) }
    else FtBotoes {
        FtCampo(minutos, { minutos = it }, "Minutos", Modifier.weight(1f), numerico = true)
        FtCampo(segundos, { segundos = it }, "Segundos", Modifier.weight(1f), numerico = true)
    }
    FtBotaoMedir(rodando, { liga ->
        fim = if (liga) SystemClock.elapsedRealtime() + ((minutos.numero() ?: 0.0) * 60_000 + (segundos.numero() ?: 0.0) * 1000).toLong() else 0
    }, iniciar = "Iniciar timer", parar = "Cancelar")
    FtNota("O alarme toca com o app aberto. Mantenha esta tela aberta até o fim.")
}

/** Unidade com fator para a unidade base da categoria (temperatura tem conversão própria). */
private data class Unidade(val nome: String, val fator: Double)

private val CATEGORIAS: Map<String, List<Unidade>> = linkedMapOf(
    "Comprimento" to listOf(Unidade("Milímetro", 0.001), Unidade("Centímetro", 0.01), Unidade("Metro", 1.0), Unidade("Quilômetro", 1000.0), Unidade("Polegada", 0.0254),
        Unidade("Pé", 0.3048), Unidade("Jarda", 0.9144), Unidade("Milha", 1609.344), Unidade("Milha náutica", 1852.0)),
    "Massa" to listOf(Unidade("Miligrama", 1e-6), Unidade("Grama", 0.001), Unidade("Quilograma", 1.0), Unidade("Tonelada", 1000.0), Unidade("Onça", 0.028349523125), Unidade("Libra", 0.45359237), Unidade("Arroba", 15.0)),
    "Temperatura" to listOf(Unidade("Celsius", 0.0), Unidade("Fahrenheit", 0.0), Unidade("Kelvin", 0.0)),
    "Volume" to listOf(Unidade("Mililitro", 0.001), Unidade("Litro", 1.0), Unidade("Metro cúbico", 1000.0), Unidade("Xícara (240 ml)", 0.24), Unidade("Colher de sopa", 0.015),
        Unidade("Galão americano", 3.785411784), Unidade("Onça fluida", 0.0295735295625)),
    "Área" to listOf(Unidade("Metro quadrado", 1.0), Unidade("Hectare", 10_000.0), Unidade("Quilômetro quadrado", 1e6), Unidade("Alqueire paulista", 24_200.0), Unidade("Acre", 4046.8564224), Unidade("Pé quadrado", 0.09290304)),
    "Velocidade" to listOf(Unidade("m/s", 1.0), Unidade("km/h", 1 / 3.6), Unidade("mph", 0.44704), Unidade("Nó", 0.514444)),
    "Dados" to listOf(Unidade("Bit", 0.125), Unidade("Byte", 1.0), Unidade("Kilobyte (kB)", 1e3), Unidade("Megabyte (MB)", 1e6), Unidade("Gigabyte (GB)", 1e9), Unidade("Terabyte (TB)", 1e12),
        Unidade("Kibibyte (KiB)", 1024.0), Unidade("Mebibyte (MiB)", 1048576.0), Unidade("Gibibyte (GiB)", 1073741824.0)),
    "Pressão" to listOf(Unidade("Pascal", 1.0), Unidade("hPa / mbar", 100.0), Unidade("Bar", 1e5), Unidade("Atmosfera", 101325.0), Unidade("PSI", 6894.757293168), Unidade("mmHg", 133.322387415)),
    "Energia" to listOf(Unidade("Joule", 1.0), Unidade("Quilojoule", 1000.0), Unidade("Caloria", 4.184), Unidade("Quilocaloria", 4184.0), Unidade("Watt-hora", 3600.0), Unidade("Quilowatt-hora", 3.6e6)),
    "Tempo" to listOf(Unidade("Segundo", 1.0), Unidade("Minuto", 60.0), Unidade("Hora", 3600.0), Unidade("Dia", 86400.0), Unidade("Semana", 604800.0), Unidade("Ano (365 dias)", 31_536_000.0)),
)

private fun converterTemperatura(v: Double, de: String, para: String): Double {
    val c = when (de) { "Fahrenheit" -> (v - 32) * 5 / 9; "Kelvin" -> v - 273.15; else -> v }
    return when (para) { "Fahrenheit" -> c * 9 / 5 + 32; "Kelvin" -> c + 273.15; else -> c }
}

private fun formatar(v: Double): String = BigDecimal(v).round(MathContext(10)).stripTrailingZeros().let {
    if (kotlin.math.abs(v) >= 1e12 || (kotlin.math.abs(v) < 1e-6 && v != 0.0)) String.format(localeBr, "%.6e", v) else String.format(localeBr, "%,.${it.scale().coerceIn(0, 10)}f", it)
}

@Composable
private fun Conversor() {
    var categoria by rememberSaveable { mutableStateOf("Comprimento") }
    var valor by rememberSaveable { mutableStateOf("1") }
    var de by rememberSaveable { mutableStateOf("Metro") }
    var escolhendo by remember { mutableStateOf(false) }
    val unidades = CATEGORIAS.getValue(categoria)
    val origem = unidades.firstOrNull { it.nome == de } ?: unidades.first()
    val v = valor.numero()
    FtColuna {
        if (escolhendo) {
            OdGrupoConfig("Categoria") { CATEGORIAS.keys.forEach { k -> OdLinhaOpcao(k, k == categoria, { categoria = k; de = CATEGORIAS.getValue(k)[if (k == "Temperatura") 0 else 2.coerceAtMost(CATEGORIAS.getValue(k).size - 1)].nome; escolhendo = false }) } }
            return@FtColuna
        }
        OdBotao("Categoria: $categoria", { escolhendo = true }, Modifier.fillMaxWidth())
        FtCampo(valor, { valor = it }, "Valor", numerico = true)
        OdGrupoConfig("De") { unidades.forEach { u -> OdLinhaOpcao(u.nome, u.nome == origem.nome, { de = u.nome }) } }
        if (v != null) OdGrupoConfig("Para") {
            unidades.filter { it.nome != origem.nome }.forEach { u ->
                val r = if (categoria == "Temperatura") converterTemperatura(v, origem.nome, u.nome) else v * origem.fator / u.fator
                OdLinhaConfig(u.nome) { Text(formatar(r), style = OdTema.tipografia.uiSubtitulo.copy(fontFeatureSettings = "tnum"), color = OdTema.cores.texto) }
            }
        }
    }
}
