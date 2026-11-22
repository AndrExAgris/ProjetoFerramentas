package io.github.andrexagris.ferramentas.ui.ferramentas.som

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.andrexagris.ferramentas.base.SaidaAudio
import io.github.andrexagris.ferramentas.base.Serie
import io.github.andrexagris.ferramentas.base.TAXA_AUDIO
import io.github.andrexagris.ferramentas.base.dbfs
import io.github.andrexagris.ferramentas.base.dbfsPonderadoA
import io.github.andrexagris.ferramentas.base.espectro
import io.github.andrexagris.ferramentas.base.fluxoMicrofone
import io.github.andrexagris.ferramentas.base.formatarCronometro
import io.github.andrexagris.ferramentas.base.notaDe
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.picoEspectro
import io.github.andrexagris.ferramentas.base.rms
import io.github.andrexagris.ferramentas.base.yin
import io.github.andrexagris.ferramentas.dados.rememberAjusteFloat
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotaoMedir
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotoes
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtDeslizante
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEscalaArco
import io.github.andrexagris.opendesign.OdEscalaLinear
import io.github.andrexagris.opendesign.OdFaixa
import io.github.andrexagris.opendesign.OdGraficoTempoReal
import io.github.andrexagris.opendesign.OdInstrumento
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdSinal
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoFaixa
import io.github.andrexagris.opendesign.odFormatar
import io.github.andrexagris.opendesign.odSerieResultante
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

fun telasSom(): Map<String, TelaDeFerramenta> = mapOf(
    "som.decibelimetro" com { Decibelimetro() },
    "som.espectro" com { Espectro() },
    "som.cronometro-acustico" com { CronometroAcustico() },
    "som.afinador" com { Afinador() },
    "som.metronomo" com { Metronomo() },
    "som.bpm" com { DetectorBpm() },
    "som.tom" com { GeradorTom() },
    "som.ruido" com { GeradorRuido() },
    "som.alto-falantes" com { TesteAltoFalantes() },
    "som.sonar" com { Sonar() },
)

/** Offset que leva dBFS a dB SPL aproximado. Celulares típicos: cerca de 94 dB SPL = −30 dBFS. Ajustável. */
private const val OFFSET_PADRAO = 120f

@Composable
private fun Decibelimetro() {
    var offset by rememberAjusteFloat("som.decibelimetro.offset", OFFSET_PADRAO)
    var ponderacao by rememberSaveable { mutableStateOf("A") }
    var nivel by remember { mutableStateOf(0.0) }
    var max by remember { mutableStateOf(0.0) }
    var energia by remember { mutableStateOf(0.0) }
    var n by remember { mutableIntStateOf(0) }
    val serie = remember { Serie(150) }
    io.github.andrexagris.ferramentas.base.ManterTelaLigada()
    LaunchedEffect(ponderacao) {
        fluxoMicrofone(4096).collect { b ->
            val db = (if (ponderacao == "A") dbfsPonderadoA(espectro(b)) else dbfs(rms(b))) + offset
            nivel = nivel * 0.6 + db * 0.4
            if (nivel > max) max = nivel
            energia += 10.0.pow(db / 10); n++
            serie.add(nivel.toFloat())
        }
    }
    val leq = if (n > 0) 10 * log10(energia / n) else 0.0
    val estado = when { nivel < 40 -> "Silencioso"; nivel < 60 -> "Conversa"; nivel < 80 -> "Barulhento"; nivel < 90 -> "Muito barulhento"; else -> "Prejudicial com o tempo" }
    FtColuna {
        OdInstrumento(estado, nivel < 80, leitura = { OdLeitura(odFormatar(nivel, 0), unidade = "dB($ponderacao)", largura = 4, tamanho = OdTamanhoLeitura.Destaque) }) {
            OdEscalaArco(nivel.toFloat().coerceIn(20f, 120f), 20f, 120f, faixas = listOf(OdFaixa(20f, 80f, OdTipoFaixa.Ok), OdFaixa(80f, 90f, OdTipoFaixa.Atencao), OdFaixa(90f, 120f, OdTipoFaixa.Perigo)))
        }
        OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "dB")), 20f, 120f, janela = 150, unidade = "dB", casas = 0, rotuloTempo = "−14 s" to "agora")
        FtInfos(null, listOf("Máximo" to "${num(max, 0)} dB", "Média (Leq)" to "${num(leq, 0)} dB"))
        OdSegmentado(listOf("A" to "dB(A): como o ouvido", "Z" to "dB(Z): sem filtro"), ponderacao, { ponderacao = it })
        FtDeslizante("Calibração", offset, { offset = it }, 90f..150f, odFormatar((offset - OFFSET_PADRAO).toDouble(), 1, OdSinal.Sempre).trim() + " dB")
        OdBotao("Zerar máximo e média", { max = 0.0; energia = 0.0; n = 0 }, Modifier.fillMaxWidth())
        FtNota("Não é um instrumento certificado. Calibre comparando com um decibelímetro de verdade. Acima de 85 dB por horas seguidas, a audição corre risco.")
    }
}

@Composable
private fun Espectro() {
    val tamanho = 4096
    var esp by remember { mutableStateOf(DoubleArray(tamanho / 2)) }
    var pico by remember { mutableStateOf(0.0 to 0.0) }
    val largura = 256
    val altura = 160
    val bitmap = remember { Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888) }
    var versao by remember { mutableIntStateOf(0) }
    val c = OdTema.cores
    val corFundo = c.superficie.toArgb()
    val corForte = c.secundaria.cor
    LaunchedEffect(Unit) {
        bitmap.eraseColor(corFundo)
        fluxoMicrofone(tamanho).collect { b ->
            val e = espectro(b)
            esp = e
            pico = picoEspectro(e, TAXA_AUDIO, 30.0, 16000.0)
            // Espectrograma: rola para cima e desenha a linha nova embaixo (escala log de frequência).
            val px = IntArray(largura * altura)
            bitmap.getPixels(px, 0, largura, 0, 0, largura, altura)
            System.arraycopy(px, largura, px, 0, largura * (altura - 1))
            for (x in 0 until largura) {
                val f = 30.0 * (16000.0 / 30).pow(x / largura.toDouble())
                val k = (f * tamanho / TAXA_AUDIO).toInt().coerceIn(1, e.size - 1)
                val v = ((20 * log10(e[k].coerceAtLeast(1e-9)) + 100) / 80).coerceIn(0.0, 1.0).toFloat()
                px[(altura - 1) * largura + x] = androidx.compose.ui.graphics.lerp(androidx.compose.ui.graphics.Color(corFundo), corForte, v).toArgb()
            }
            bitmap.setPixels(px, 0, largura, 0, 0, largura, altura)
            versao++
        }
    }
    FtColuna {
        FtPainel(titulo = "Frequência mais forte") {
            OdLeitura(num(pico.first, 0), unidade = "Hz", tamanho = OdTamanhoLeitura.Destaque)
            OdLeitura(notaDe(pico.first).let { "${it.nome}${it.oitava}" }, unidade = "nota mais próxima")
        }
        FtPainel(titulo = "Espectro (30 Hz a 16 kHz)") {
            Canvas(Modifier.fillMaxWidth().height(140.dp).semantics { contentDescription = "Espectro com pico em ${pico.first.toInt()} Hz" }) {
                val p = Path()
                for (x in 0..size.width.toInt() step 2) {
                    val f = 30.0 * (16000.0 / 30).pow(x / size.width.toDouble())
                    val k = (f * tamanho / TAXA_AUDIO).toInt().coerceIn(1, esp.size - 1)
                    val y = size.height * (1 - ((20 * log10(esp[k].coerceAtLeast(1e-9)) + 100) / 80).coerceIn(0.0, 1.0)).toFloat()
                    if (x == 0) p.moveTo(x.toFloat(), y) else p.lineTo(x.toFloat(), y)
                }
                drawPath(p, corForte, style = Stroke(1.5.dp.toPx()))
                listOf(100.0, 1000.0, 10000.0).forEach { f -> val x = (log10(f / 30) / log10(16000.0 / 30) * size.width).toFloat(); drawLine(c.graficoGrade, Offset(x, 0f), Offset(x, size.height), 1f) }
            }
        }
        FtPainel(titulo = "Espectrograma") {
            versao.let { Image(bitmap.asImageBitmap(), "Espectrograma", Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.FillBounds, filterQuality = FilterQuality.None) }
        }
        FtNota("Linhas verticais em 100 Hz, 1 kHz e 10 kHz. O espectrograma mostra o tempo de cima (passado) para baixo (agora).")
    }
}

@Composable
private fun CronometroAcustico() {
    var limiar by rememberSaveable { mutableFloatStateOf(-20f) }
    var armado by remember { mutableStateOf(false) }
    var inicio by remember { mutableStateOf<Long?>(null) }
    var resultado by remember { mutableStateOf<Long?>(null) }
    var nivel by remember { mutableStateOf(-90.0) }
    LaunchedEffect(armado) {
        if (!armado) return@LaunchedEffect
        var amostras = 0L
        var ultimoPico = -1L
        fluxoMicrofone(512).collect { b ->
            val db = dbfs(rms(b)); nivel = db
            val t = amostras * 1000 / TAXA_AUDIO
            if (db > limiar && t - ultimoPico > 150) {
                ultimoPico = t
                val inicioAtual = inicio
                if (inicioAtual == null) inicio = t else { resultado = t - inicioAtual; armado = false }
            }
            amostras += b.size
        }
    }
    FtColuna {
        FtPainel(titulo = when { armado && inicio == null -> "Esperando o primeiro som"; armado -> "Esperando o segundo som"; else -> "Tempo entre os sons" }) {
            OdLeitura(resultado?.let { formatarCronometro(it).replace(",", ",") + "" } ?: "—", tamanho = OdTamanhoLeitura.Destaque)
            if (resultado != null) OdLeitura(num(resultado!!.toDouble(), 0), unidade = "ms")
        }
        OdEscalaLinear(nivel.toFloat().coerceIn(-60f, 0f), -60f, 0f, referencia = limiar)
        FtDeslizante("Sensibilidade (limiar)", limiar, { limiar = it }, -50f..-5f, "${limiar.toInt()} dBFS")
        FtBotaoMedir(armado, { armado = it; if (it) { inicio = null; resultado = null } }, iniciar = "Armar cronômetro", parar = "Cancelar")
        FtNota("O cronômetro começa no primeiro som forte (palma, apito) e para no segundo. Precisão de cerca de 12 ms.")
    }
}

@Composable
private fun Afinador() {
    var la4 by rememberAjusteFloat("som.afinador.la4", 440f)
    var freq by remember { mutableStateOf<Double?>(null) }
    var suave by remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(la4) {
        fluxoMicrofone(4096).collect { b ->
            val f = if (rms(b) > 0.005) yin(b) else null
            freq = f
            suave = if (f == null) null else suave?.let { s -> if (kotlin.math.abs(1200 * kotlin.math.ln(f / s) / kotlin.math.ln(2.0)) < 50) s * 0.6 + f * 0.4 else f } ?: f
        }
    }
    val nota = suave?.let { notaDe(it, la4.toDouble()) }
    val cents = nota?.cents?.toFloat() ?: 0f
    val dentro = nota != null && kotlin.math.abs(cents) <= 5f
    FtColuna {
        OdInstrumento(
            when { nota == null -> "Toque uma nota"; dentro -> "Afinado"; cents < 0 -> "Abaixo: aperte a corda"; else -> "Acima: afrouxe a corda" }, dentro,
            leitura = {
                OdLeitura(nota?.let { "${it.nome}${it.oitava}" } ?: "—", tamanho = OdTamanhoLeitura.Destaque, cor = if (dentro) OdTema.cores.escalaDentro else OdTema.cores.texto)
                OdLeitura(odFormatar(cents.toDouble(), 0, OdSinal.Sempre), unidade = "cents", largura = 3)
            },
        ) {
            OdEscalaLinear(cents.coerceIn(-50f, 50f), -50f, 50f, referencia = 0f, passoMenor = 5f, passoMaior = 25f, vibrarAoEntrar = true,
                faixas = if (nota == null) emptyList() else listOf(OdFaixa(-15f, -5f, OdTipoFaixa.Atencao), OdFaixa(-5f, 5f, OdTipoFaixa.Ok), OdFaixa(5f, 15f, OdTipoFaixa.Atencao)))
        }
        FtInfos(null, listOf("Frequência" to suave?.let { "${num(it, 1)} Hz" }, "Frequência da nota" to nota?.let { "${num(it.alvo, 1)} Hz" }))
        FtDeslizante("Lá de referência", la4, { la4 = it.toInt().toFloat() }, 430f..450f, "${la4.toInt()} Hz", passos = 19)
        FtNota("Violão em afinação padrão: Mi2, Lá2, Ré3, Sol3, Si3, Mi4 (da corda mais grossa para a mais fina).")
    }
}

@Composable
private fun Metronomo() {
    var bpm by rememberAjusteFloat("som.metronomo.bpm", 100f)
    var compasso by rememberSaveable { mutableIntStateOf(4) }
    var tocando by remember { mutableStateOf(false) }
    var batida by remember { mutableIntStateOf(0) }
    val estado = remember { object { @Volatile var bpm = 100f; @Volatile var compasso = 4; var amostra = 0L } }
    estado.bpm = bpm; estado.compasso = compasso
    val saida = remember {
        SaidaAudio { buf ->
            for (i in 0 until buf.size / 2) {
                val porBatida = (TAXA_AUDIO * 60 / estado.bpm).toLong()
                val pos = estado.amostra % porBatida
                val n = estado.amostra / porBatida
                val forte = n % estado.compasso == 0L
                val v = if (pos < 1500) (sin(2 * PI * (if (forte) 1600.0 else 1000.0) * pos / TAXA_AUDIO) * (1 - pos / 1500.0) * 0.8).toFloat() else 0f
                buf[i * 2] = v; buf[i * 2 + 1] = v
                estado.amostra++
            }
        }
    }
    LaunchedEffect(tocando) {
        if (tocando) { estado.amostra = 0; saida.iniciar(); while (true) { batida = ((estado.amostra / (TAXA_AUDIO * 60 / estado.bpm).toLong()) % estado.compasso).toInt(); delay(30) } } else saida.parar()
    }
    DisposableEffect(Unit) { onDispose { saida.parar() } }
    FtColuna {
        FtPainel { OdLeitura(bpm.toInt().toString(), unidade = "BPM", tamanho = OdTamanhoLeitura.Destaque); if (tocando) OdLeitura("${batida + 1} de $compasso", unidade = "tempo") }
        FtDeslizante("Andamento", bpm, { bpm = it.toInt().toFloat() }, 30f..240f, "${bpm.toInt()} BPM")
        FtBotoes {
            OdBotao("−1", { bpm = (bpm - 1).coerceAtLeast(30f) }, Modifier.weight(1f))
            OdBotao("+1", { bpm = (bpm + 1).coerceAtMost(240f) }, Modifier.weight(1f))
        }
        OdSegmentado(listOf(2 to "2/4", 3 to "3/4", 4 to "4/4", 6 to "6/8"), compasso, { compasso = it })
        FtBotaoMedir(tocando, { tocando = it }, iniciar = "Tocar", parar = "Parar")
    }
}

@Composable
private fun DetectorBpm() {
    var bpm by remember { mutableStateOf<Double?>(null) }
    var ouvindo by remember { mutableStateOf(false) }
    val energias = remember { mutableStateListOf<Float>() }
    LaunchedEffect(ouvindo) {
        if (!ouvindo) return@LaunchedEffect
        energias.clear()
        val bloco = 1024
        val porSeg = TAXA_AUDIO / bloco.toDouble()
        var anterior = 0.0
        fluxoMicrofone(bloco).collect { b ->
            val e = rms(b)
            energias += (e - anterior).coerceAtLeast(0.0).toFloat() // fluxo de energia (ataques)
            anterior = e
            if (energias.size > (porSeg * 10).toInt()) energias.removeAt(0)
            if (energias.size > porSeg * 5) {
                // Autocorrelação do envelope de ataques entre 60 e 200 BPM.
                val x = energias.toFloatArray(); val m = x.average().toFloat()
                var melhor = 0; var melhorV = 0.0
                for (lag in (porSeg * 60 / 200).toInt()..(porSeg * 60 / 60).toInt()) {
                    var s = 0.0
                    for (i in lag until x.size) s += (x[i] - m) * (x[i - lag] - m)
                    if (s > melhorV) { melhorV = s; melhor = lag }
                }
                if (melhor > 0) bpm = 60 * porSeg / melhor
            }
        }
    }
    FtColuna {
        FtPainel(titulo = "Andamento") { OdLeitura(bpm?.let { num(it, 0) } ?: "—", unidade = "BPM", tamanho = OdTamanhoLeitura.Destaque) }
        FtBotaoMedir(ouvindo, { ouvindo = it; if (it) bpm = null }, iniciar = "Ouvir música")
        FtNota("Deixe o celular perto do som por uns 10 segundos. Funciona melhor com batida marcada; às vezes acha a metade ou o dobro do andamento.")
    }
}

@Composable
private fun GeradorTom() {
    var freq by rememberSaveable { mutableFloatStateOf(440f) }
    var onda by rememberSaveable { mutableStateOf("seno") }
    var varrer by rememberSaveable { mutableStateOf(false) }
    var volume by rememberSaveable { mutableFloatStateOf(0.5f) }
    var tocando by remember { mutableStateOf(false) }
    val e = remember { object { @Volatile var f = 440.0; @Volatile var onda = "seno"; @Volatile var vol = 0.5f; @Volatile var varrer = false; var fase = 0.0; var t = 0L } }
    e.f = freq.toDouble(); e.onda = onda; e.vol = volume; e.varrer = varrer
    val saida = remember {
        SaidaAudio { buf ->
            for (i in 0 until buf.size / 2) {
                val f = if (e.varrer) 20.0 * (20000.0 / 20).pow((e.t % (TAXA_AUDIO * 10L)) / (TAXA_AUDIO * 10.0)) else e.f
                e.fase = (e.fase + f / TAXA_AUDIO) % 1.0
                val v = when (e.onda) { "quadrada" -> if (e.fase < 0.5) 1.0 else -1.0; "triangular" -> 4 * kotlin.math.abs(e.fase - 0.5) - 1; "dente" -> 2 * e.fase - 1; else -> sin(2 * PI * e.fase) }
                val s = (v * e.vol * 0.8).toFloat()
                buf[i * 2] = s; buf[i * 2 + 1] = s; e.t++
            }
        }
    }
    LaunchedEffect(tocando) { if (tocando) { e.t = 0; saida.iniciar() } else saida.parar() }
    DisposableEffect(Unit) { onDispose { saida.parar() } }
    FtColuna {
        FtPainel { OdLeitura(if (varrer) "20 Hz → 20 kHz" else num(freq.toDouble(), 0), unidade = if (varrer) "em 10 s" else "Hz", tamanho = OdTamanhoLeitura.Destaque) }
        // Escala logarítmica no deslizante: de 20 Hz a 20 kHz.
        val pos = (log10(freq / 20.0) / 3).toFloat()
        FtDeslizante("Frequência", pos, { freq = (20 * 10.0.pow(it * 3.0)).toFloat() }, 0f..1f, "${num(freq.toDouble(), 0)} Hz")
        OdSegmentado(listOf("seno" to "Seno", "quadrada" to "Quadrada", "triangular" to "Triang.", "dente" to "Serra"), onda, { onda = it })
        OdSegmentado(listOf(false to "Tom fixo", true to "Varredura"), varrer, { varrer = it })
        FtDeslizante("Volume", volume, { volume = it }, 0f..1f, "${(volume * 100).toInt()}%")
        FtBotaoMedir(tocando, { tocando = it }, iniciar = "Tocar", parar = "Parar")
        FtNota("Comece com volume baixo: tons agudos e ondas quadradas podem incomodar e forçar o alto-falante.")
    }
}

@Composable
private fun GeradorRuido() {
    var tipo by rememberSaveable { mutableStateOf("branco") }
    var volume by rememberSaveable { mutableFloatStateOf(0.4f) }
    var tocando by remember { mutableStateOf(false) }
    val e = remember { object { @Volatile var tipo = "branco"; @Volatile var vol = 0.4f; val b = DoubleArray(7); var marrom = 0.0 } }
    e.tipo = tipo; e.vol = volume
    val saida = remember {
        SaidaAudio { buf ->
            for (i in 0 until buf.size / 2) {
                val w = Random.nextDouble(-1.0, 1.0)
                val v = when (e.tipo) {
                    "rosa" -> { // filtro de Paul Kellet
                        e.b[0] = 0.99886 * e.b[0] + w * 0.0555179; e.b[1] = 0.99332 * e.b[1] + w * 0.0750759; e.b[2] = 0.96900 * e.b[2] + w * 0.1538520
                        e.b[3] = 0.86650 * e.b[3] + w * 0.3104856; e.b[4] = 0.55000 * e.b[4] + w * 0.5329522; e.b[5] = -0.7616 * e.b[5] - w * 0.0168980
                        val p = (e.b[0] + e.b[1] + e.b[2] + e.b[3] + e.b[4] + e.b[5] + e.b[6] + w * 0.5362) * 0.11; e.b[6] = w * 0.115926; p
                    }
                    "marrom" -> { e.marrom = (e.marrom + 0.02 * w) / 1.02; e.marrom * 3.5 }
                    else -> w
                }
                val s = (v * e.vol).toFloat().coerceIn(-1f, 1f)
                buf[i * 2] = s; buf[i * 2 + 1] = s
            }
        }
    }
    LaunchedEffect(tocando) { if (tocando) saida.iniciar() else saida.parar() }
    DisposableEffect(Unit) { onDispose { saida.parar() } }
    FtColuna {
        OdSegmentado(listOf("branco" to "Branco", "rosa" to "Rosa", "marrom" to "Marrom"), tipo, { tipo = it })
        FtDeslizante("Volume", volume, { volume = it }, 0f..1f, "${(volume * 100).toInt()}%")
        FtBotaoMedir(tocando, { tocando = it }, iniciar = "Tocar", parar = "Parar")
        FtNota("Branco: chiado igual em todas as frequências. Rosa: mais grave, bom para dormir e testar caixas de som. Marrom: grave e suave, como cachoeira.")
    }
}

@Composable
private fun TesteAltoFalantes() {
    var lado by remember { mutableStateOf<String?>(null) }
    val e = remember { object { @Volatile var lado = ""; var fase = 0.0; var t = 0L } }
    e.lado = lado ?: ""
    val saida = remember {
        SaidaAudio { buf ->
            for (i in 0 until buf.size / 2) {
                val f = if (e.lado == "graves") 60.0 + 100 * ((e.t / TAXA_AUDIO.toDouble()) % 2) else 440.0
                e.fase = (e.fase + f / TAXA_AUDIO) % 1.0
                val pulso = if (e.lado == "graves") 1.0 else if ((e.t / (TAXA_AUDIO / 4)) % 2 == 0L) 1.0 else 0.0
                val v = (sin(2 * PI * e.fase) * 0.6 * pulso).toFloat()
                buf[i * 2] = if (e.lado == "esquerdo" || e.lado == "ambos" || e.lado == "graves") v else 0f
                buf[i * 2 + 1] = if (e.lado == "direito" || e.lado == "ambos" || e.lado == "graves") v else 0f
                e.t++
            }
        }
    }
    LaunchedEffect(lado) { if (lado != null) saida.iniciar() else saida.parar() }
    DisposableEffect(Unit) { onDispose { saida.parar() } }
    FtColuna {
        listOf("esquerdo" to "Tocar só no esquerdo", "direito" to "Tocar só no direito", "ambos" to "Tocar nos dois", "graves" to "Teste de graves (60 a 160 Hz)").forEach { (id, texto) ->
            OdBotao(if (lado == id) "Parar" else texto, { lado = if (lado == id) null else id }, Modifier.fillMaxWidth(), tipo = if (lado == id) OdTipoBotao.Primario else OdTipoBotao.Secundario)
        }
        FtNota("Com o celular deitado, o \"esquerdo\" é o lado de baixo (alto-falante principal) e o \"direito\" o de cima (auricular). O Galaxy A24 tem um alto-falante só (mono): os dois lados soam iguais. Também serve para testar fones.")
    }
}

@Composable
private fun Sonar() {
    var distancia by remember { mutableStateOf<Double?>(null) }
    var medindo by remember { mutableStateOf(false) }
    LaunchedEffect(medindo) {
        if (!medindo) return@LaunchedEffect
        // Pulso curto de 8 kHz a 12 kHz (chirp). Grava, correlaciona e procura o segundo pico (eco) depois do direto.
        val n = 256
        val chirp = FloatArray(n) { i -> val t = i / TAXA_AUDIO.toDouble(); (sin(2 * PI * (8000 * t + 0.5 * (4000 / (n / TAXA_AUDIO.toDouble())) * t * t)) * sin(PI * i / n)).toFloat() }
        while (medindo) {
            val gravacao = mutableListOf<Float>()
            val emissor = Thread {
                val tr = android.media.AudioTrack.Builder().setAudioFormat(android.media.AudioFormat.Builder().setSampleRate(TAXA_AUDIO).setEncoding(android.media.AudioFormat.ENCODING_PCM_FLOAT).setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(n * 4 * 4).setTransferMode(android.media.AudioTrack.MODE_STATIC).build()
                tr.write(chirp + FloatArray(n * 3), 0, n * 4, android.media.AudioTrack.WRITE_BLOCKING); Thread.sleep(150); tr.play(); Thread.sleep(300); tr.release()
            }
            emissor.start()
            fluxoMicrofone(2048).take(12).collect { b -> gravacao.addAll(b.toList()) }
            val g = gravacao.toFloatArray()
            val corr = DoubleArray(g.size - n) { k -> var s = 0.0; for (i in 0 until n step 2) s += g[k + i] * chirp[i]; kotlin.math.abs(s) }
            val direto = corr.indices.maxBy { corr[it] }
            val minAtraso = (TAXA_AUDIO * 2 * 0.3 / 343).toInt() // ignora ecos a menos de 30 cm
            val janela = (direto + minAtraso).coerceAtMost(corr.size - 1) until (direto + TAXA_AUDIO * 2 * 5 / 343).coerceAtMost(corr.size)
            val eco = janela.maxByOrNull { corr[it] }
            distancia = if (eco != null && corr[eco] > corr[direto] * 0.15) (eco - direto) / TAXA_AUDIO.toDouble() * 343 / 2 else null
            delay(500)
        }
    }
    FtColuna {
        FtPainel(titulo = "Distância do eco") { OdLeitura(distancia?.let { num(it, 2) } ?: "—", unidade = "m", tamanho = OdTamanhoLeitura.Destaque) }
        FtBotaoMedir(medindo, { medindo = it }, iniciar = "Medir por eco")
        FtNota("Experimental. Aponte a parte de baixo do celular para uma parede lisa a 0,5 a 4 metros, em silêncio. Com volume alto funciona melhor.")
    }
}
