package io.github.andrexagris.opendesign

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/*
 * Escalas de instrumentos (régua, nível, bússola, afinador, velocímetro). Regras de cor:
 * - ponteiro neutro (escalaPonteiro = texto), sempre visível sobre qualquer faixa;
 * - a faixa embaixo diz o que é aceitável: escalaFaixaOk, escalaFaixaAtencao, escalaFaixaPerigo (tintas leves);
 * - dentro da faixa OK, ponteiro e valor viram escalaDentro (sucesso.texto), junto com texto ou ícone;
 * - perigo só quando passar do limite tem consequência real; fora do OK de um afinador é só "acima/abaixo";
 * - referência (norte, nota-alvo, zero, cursor da régua) em escalaReferencia (primaria.texto);
 * - marcas maiores e números em escalaMarca, subdivisões em escalaMarcaMenor, trilho em escalaTrilho.
 */

enum class OdTipoFaixa { Ok, Atencao, Perigo }

@Immutable
data class OdFaixa(val inicio: Float, val fim: Float, val tipo: OdTipoFaixa)

@Composable
private fun corFaixa(t: OdTipoFaixa): Color = when (t) {
    OdTipoFaixa.Ok -> OdTema.cores.escalaFaixaOk
    OdTipoFaixa.Atencao -> OdTema.cores.escalaFaixaAtencao
    OdTipoFaixa.Perigo -> OdTema.cores.escalaFaixaPerigo
}

/** Leitura está em alguma faixa OK? */
fun List<OdFaixa>.contemOk(valor: Float) = any { it.tipo == OdTipoFaixa.Ok && valor >= it.inicio && valor <= it.fim }

/** Cor do ponteiro com a transição de entrada na faixa OK e vibração curta opcional ao entrar. */
@Composable
private fun corPonteiro(dentro: Boolean, vibrar: Boolean): Color {
    val view = LocalView.current
    LaunchedEffect(dentro) {
        if (dentro && vibrar) view.performHapticFeedback(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)
    }
    val alvo = if (dentro) OdTema.cores.escalaDentro else OdTema.cores.escalaPonteiro
    return animateColorAsState(alvo, tween(OdTema.duracao(OdMovimento.duracao)), label = "ponteiro").value
}

@Composable
private fun estiloEscala(): TextStyle = OdTema.tipografia.uiPequeno.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum", color = OdTema.cores.escalaMarca)

private fun DrawScope.texto(m: TextMeasurer, s: String, estilo: TextStyle, centro: Offset) {
    val r = m.measure(s, estilo)
    drawText(r, topLeft = Offset(centro.x - r.size.width / 2f, centro.y - r.size.height / 2f))
}

/**
 * Escala linear (afinador, nível de uma direção, medidor horizontal). Ex. afinador:
 * OdEscalaLinear(cents, -50f, 50f, faixas = listOf(OdFaixa(-15f, -5f, Atencao), OdFaixa(-5f, 5f, Ok), OdFaixa(5f, 15f, Atencao)), referencia = 0f)
 */
@Composable
fun OdEscalaLinear(
    valor: Float,
    minimo: Float,
    maximo: Float,
    modifier: Modifier = Modifier,
    faixas: List<OdFaixa> = emptyList(),
    passoMenor: Float = (maximo - minimo) / 20f,
    passoMaior: Float = (maximo - minimo) / 4f,
    rotulo: (Float) -> String = { odFormatar(it.toDouble(), 0, OdSinal.Sempre).trim() },
    referencia: Float? = null,
    vibrarAoEntrar: Boolean = false,
    descricao: String? = null,
) {
    val c = OdTema.cores
    val dentro = faixas.contemOk(valor)
    val ponteiro = corPonteiro(dentro, vibrarAoEntrar)
    val medidor = rememberTextMeasurer()
    val estilo = estiloEscala()
    val faixasCores = faixas.map { it to corFaixa(it.tipo) }
    Canvas(modifier.fillMaxWidth().height(66.dp).semantics { contentDescription = descricao ?: rotulo(valor) }) {
        val m = 20.dp.toPx()
        val w = size.width - 2 * m
        fun x(v: Float) = m + w * ((v.coerceIn(minimo, maximo) - minimo) / (maximo - minimo))
        val y0 = 18.dp.toPx(); val y1 = 44.dp.toPx()
        faixasCores.forEach { (f, cor) -> drawRect(cor, Offset(x(f.inicio), y0), Size(x(f.fim) - x(f.inicio), y1 - y0)) }
        var v = minimo
        while (v <= maximo + 0.0001f) {
            val maior = kotlin.math.abs(((v - minimo) / passoMaior) - kotlin.math.round((v - minimo) / passoMaior)) < 0.001f
            drawLine(if (maior) c.escalaMarca else c.escalaMarcaMenor, Offset(x(v), if (maior) 32.dp.toPx() else 37.dp.toPx()), Offset(x(v), y1), (if (maior) 1.5f else 1f).dp.toPx())
            if (maior) texto(medidor, rotulo(v), estilo, Offset(x(v), 57.dp.toPx()))
            v += passoMenor
        }
        drawLine(c.escalaTrilho, Offset(m, y1), Offset(m + w, y1), 1.dp.toPx())
        if (referencia != null) {
            val rx = x(referencia)
            drawPath(Path().apply { moveTo(rx - 5.dp.toPx(), 4.dp.toPx()); lineTo(rx + 5.dp.toPx(), 4.dp.toPx()); lineTo(rx, 11.dp.toPx()); close() }, c.escalaReferencia)
        }
        val px = x(valor)
        drawRoundRect(ponteiro, Offset(px - 1.5.dp.toPx(), 13.dp.toPx()), Size(3.dp.toPx(), 34.dp.toPx()), CornerRadius(1.5.dp.toPx()))
        drawCircle(ponteiro, 4.5.dp.toPx(), Offset(px, 13.dp.toPx()))
    }
}

/** Escala em arco de 240° (velocímetro, manômetro). Perigo só a partir do limite que a pessoa definir. */
@Composable
fun OdEscalaArco(
    valor: Float,
    minimo: Float,
    maximo: Float,
    modifier: Modifier = Modifier,
    faixas: List<OdFaixa> = emptyList(),
    passoMenor: Float = (maximo - minimo) / 12f,
    passoMaior: Float = (maximo - minimo) / 6f,
    rotulo: (Float) -> String = { odFormatar(it.toDouble(), 0, OdSinal.Nenhum) },
    vibrarAoEntrar: Boolean = false,
    descricao: String? = null,
) {
    val c = OdTema.cores
    val ponteiro = corPonteiro(faixas.contemOk(valor), vibrarAoEntrar)
    val medidor = rememberTextMeasurer()
    val estilo = estiloEscala()
    val faixasCores = faixas.map { it to corFaixa(it.tipo) }
    Canvas(modifier.fillMaxWidth().aspectRatio(240f / 176f).semantics { contentDescription = descricao ?: rotulo(valor) }) {
        val esc = size.width / 240f
        val centro = Offset(120f * esc, 116f * esc)
        val r = 92f * esc
        val traco = 12f * esc
        fun ang(v: Float) = 210f - (v.coerceIn(minimo, maximo) - minimo) / (maximo - minimo) * 240f
        fun ponto(raio: Float, graus: Float) = Offset(centro.x + raio * cos(Math.toRadians(graus.toDouble())).toFloat(), centro.y - raio * sin(Math.toRadians(graus.toDouble())).toFloat())
        val caixa = Size(2 * r, 2 * r); val canto = Offset(centro.x - r, centro.y - r)
        // drawArc usa graus no sentido horário a partir das 3 h; convertemos do sentido matemático.
        fun arco(cor: Color, a0: Float, a1: Float) = drawArc(cor, -a0, a0 - a1, false, canto, caixa, style = Stroke(traco))
        arco(c.escalaTrilho, 210f, -30f)
        faixasCores.forEach { (f, cor) -> arco(cor, ang(f.inicio), ang(f.fim)) }
        var v = minimo
        while (v <= maximo + 0.0001f) {
            val maior = kotlin.math.abs(((v - minimo) / passoMaior) - kotlin.math.round((v - minimo) / passoMaior)) < 0.001f
            drawLine(if (maior) c.escalaMarca else c.escalaMarcaMenor, ponto(r - 10f * esc, ang(v)), ponto(r - (if (maior) 22f else 16f) * esc, ang(v)), (if (maior) 1.5f else 1f).dp.toPx(), StrokeCap.Round)
            if (maior) texto(medidor, rotulo(v), estilo, ponto(r - 36f * esc, ang(v)))
            v += passoMenor
        }
        rotate(-120f + (valor.coerceIn(minimo, maximo) - minimo) / (maximo - minimo) * 240f, centro) {
            drawPath(Path().apply { moveTo(centro.x - 2.5f * esc, centro.y); lineTo(centro.x, centro.y - r + 14f * esc); lineTo(centro.x + 2.5f * esc, centro.y); close() }, ponteiro)
        }
        drawCircle(ponteiro, 7f * esc, centro)
    }
}

/** Bússola: o mostrador gira e a linha de fé no topo fica parada. N em escalaReferencia; cardeais N, L, S, O. */
@Composable
fun OdBussola(rumo: Float, modifier: Modifier = Modifier) {
    val c = OdTema.cores
    val medidor = rememberTextMeasurer()
    val estilo = estiloEscala()
    val cardeal = estilo.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.texto)
    Canvas(modifier.fillMaxWidth().aspectRatio(220f / 226f).semantics { contentDescription = "Rumo ${rumo.toInt()} graus" }) {
        val esc = size.width / 220f
        val centro = Offset(110f * esc, 116f * esc)
        val r = 98f * esc
        rotate(-rumo, centro) {
            drawCircle(c.escalaTrilho, r, centro, style = Stroke(1.5f.dp.toPx()))
            for (g in 0 until 360 step 5) {
                val maior = g % 30 == 0
                val cor = if (g == 0) c.escalaReferencia else if (maior) c.escalaMarca else c.escalaMarcaMenor
                rotate(g.toFloat(), centro) {
                    drawLine(cor, Offset(centro.x, centro.y - r + 4f * esc), Offset(centro.x, centro.y - r + (if (maior) 16f else 10f) * esc), (if (g == 0) 2f else if (maior) 1.5f else 1f).dp.toPx(), StrokeCap.Round)
                }
            }
            listOf(0 to "N", 90 to "L", 180 to "S", 270 to "O").forEach { (g, s) ->
                rotate(g.toFloat(), centro) { texto(medidor, s, if (g == 0) cardeal.copy(color = c.escalaReferencia) else cardeal, Offset(centro.x, centro.y - r + 32f * esc)) }
            }
            for (g in 30 until 360 step 30) if (g % 90 != 0) rotate(g.toFloat(), centro) { texto(medidor, g.toString(), estilo, Offset(centro.x, centro.y - r + 28f * esc)) }
            drawPath(Path().apply { moveTo(centro.x, centro.y - r + 40f * esc); lineTo(centro.x + 7f * esc, centro.y - r + 56f * esc); lineTo(centro.x - 7f * esc, centro.y - r + 56f * esc); close() }, c.escalaReferencia)
        }
        drawPath(Path().apply { moveTo(centro.x - 7f * esc, 0f); lineTo(centro.x + 7f * esc, 0f); lineTo(centro.x, 12f * esc); close() }, c.escalaPonteiro)
        drawLine(c.escalaPonteiro, Offset(centro.x, 12f * esc), Offset(centro.x, 34f * esc), 3.dp.toPx(), StrokeCap.Round)
    }
}

/**
 * Nível de bolha: inclinações em graus. Dentro de limiteOk (padrão 1°) a bolha vira escalaDentro.
 * alcance: inclinação que leva a bolha até a borda.
 */
@Composable
fun OdNivelBolha(inclinacaoX: Float, inclinacaoY: Float, modifier: Modifier = Modifier, limiteOk: Float = 1f, alcance: Float = 6f, vibrarAoEntrar: Boolean = false) {
    val c = OdTema.cores
    val dentro = hypot(inclinacaoX, inclinacaoY) <= limiteOk
    val bolha = corPonteiro(dentro, vibrarAoEntrar)
    Canvas(modifier.fillMaxWidth().aspectRatio(1f).semantics { contentDescription = if (dentro) "Nivelado" else "Inclinação ${odFormatar(hypot(inclinacaoX, inclinacaoY).toDouble(), 1).trim()} graus" }) {
        val centro = center; val r = size.minDimension / 2f * 0.94f
        val rOk = r * (limiteOk / alcance).coerceAtLeast(0.12f)
        drawCircle(c.escalaTrilho, r, centro, style = Stroke(1.5f.dp.toPx()))
        drawCircle(c.escalaMarcaMenor, r * 0.64f, centro, style = Stroke(1.dp.toPx()))
        drawCircle(c.escalaFaixaOk, rOk, centro)
        drawLine(c.escalaMarcaMenor, Offset(centro.x, centro.y - r), Offset(centro.x, centro.y + r), 1.dp.toPx())
        drawLine(c.escalaMarcaMenor, Offset(centro.x - r, centro.y), Offset(centro.x + r, centro.y), 1.dp.toPx())
        drawCircle(c.escalaReferencia, rOk, centro, style = Stroke(1.5f.dp.toPx()))
        val limite = r - 14.dp.toPx()
        var dx = inclinacaoX / alcance * r; var dy = inclinacaoY / alcance * r
        val d = hypot(dx, dy)
        if (d > limite) { dx *= limite / d; dy *= limite / d }
        val p = Offset(centro.x + dx, centro.y + dy)
        drawCircle(bolha.copy(alpha = 0.22f), 13.dp.toPx(), p)
        drawCircle(bolha, 13.dp.toPx(), p, style = Stroke(2.5f.dp.toPx()))
    }
}

/** Régua na escala real da tela (xdpi do aparelho). medidaMm: posição do cursor de medida, se houver. */
@Composable
fun OdRegua(modifier: Modifier = Modifier, medidaMm: Float? = null) {
    val c = OdTema.cores
    val medidor = rememberTextMeasurer()
    val estilo = estiloEscala()
    val pxPorMm = LocalContext.current.resources.displayMetrics.xdpi / 25.4f
    Canvas(modifier.fillMaxWidth().height(64.dp).semantics { contentDescription = medidaMm?.let { "Medida ${it.toInt()} milímetros" } ?: "Régua" }) {
        drawLine(c.escalaTrilho, Offset(0f, 0.5f), Offset(size.width, 0.5f), 1.dp.toPx())
        var mm = 0
        while (mm * pxPorMm <= size.width) {
            val x = mm * pxPorMm
            val (alt, cor) = when { mm % 10 == 0 -> 18.dp.toPx() to c.escalaMarca; mm % 5 == 0 -> 12.dp.toPx() to c.escalaMarcaMenor; else -> 7.dp.toPx() to c.escalaMarcaMenor }
            drawLine(cor, Offset(x, 0f), Offset(x, alt), (if (mm % 10 == 0) 1.5f else 1f).dp.toPx())
            if (mm % 10 == 0) texto(medidor, (mm / 10).toString(), estilo, Offset(x + if (mm == 0) 4.dp.toPx() else 0f, 30.dp.toPx()))
            mm++
        }
        if (medidaMm != null) {
            val x = medidaMm * pxPorMm
            drawRoundRect(c.escalaReferencia.copy(alpha = 0.35f), Offset(0f, 40.dp.toPx()), Size(x, 6.dp.toPx()), CornerRadius(3.dp.toPx()))
            drawLine(c.escalaReferencia, Offset(x, 0f), Offset(x, 52.dp.toPx()), 2.dp.toPx())
            drawCircle(c.escalaReferencia, 5.dp.toPx(), Offset(x, 56.dp.toPx()))
        }
    }
}

/**
 * Moldura de instrumento: leitura, escala e estado em texto. dentro = leitura na faixa OK: o número e o
 * estado viram escalaDentro e o estado ganha a marca de confirmação.
 */
@Composable
fun OdInstrumento(estado: String?, dentro: Boolean, modifier: Modifier = Modifier, leitura: @Composable () -> Unit, escala: @Composable () -> Unit) {
    val c = OdTema.cores
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(OdEspacos.e2)) {
        leitura()
        escala()
        if (estado != null) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (dentro) Icon(OdIcones.Marca, null, Modifier.size(16.dp), tint = c.escalaDentro)
            Text(estado, style = OdTema.tipografia.uiMedio, color = if (dentro) c.escalaDentro else c.texto2)
        }
    }
}
