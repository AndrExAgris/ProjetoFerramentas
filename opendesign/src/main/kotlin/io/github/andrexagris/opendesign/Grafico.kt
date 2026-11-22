package io.github.andrexagris.opendesign

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Uma série do gráfico. Para os eixos, use odSerieX/Y/Z (cores grafico-x, -y, -z) e odSerieResultante. */
@Immutable
data class OdSerie(val rotulo: String, val cor: Color, val valores: List<Float>, val grossa: Boolean = false)

@Composable fun odSerieX(valores: List<Float>) = OdSerie("X", OdTema.cores.graficoX, valores)
@Composable fun odSerieY(valores: List<Float>) = OdSerie("Y", OdTema.cores.graficoY, valores)
@Composable fun odSerieZ(valores: List<Float>) = OdSerie("Z", OdTema.cores.graficoZ, valores)
@Composable fun odSerieResultante(valores: List<Float>, rotulo: String = "|v|") = OdSerie(rotulo, OdTema.cores.graficoResultante, valores, grossa = true)

/**
 * Gráfico de linhas em tempo real: a amostra mais nova fica à direita e a linha corre para a esquerda.
 * Escala Y fixa (minimo..maximo): não reescale a cada amostra. janela = quantas amostras cabem na largura.
 * Linhas de 2 dp, grade de 1 dp, rótulo direto no fim de cada linha e legenda com o valor atual:
 * a identidade nunca depende só da cor.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OdGraficoTempoReal(
    series: List<OdSerie>,
    minimo: Float,
    maximo: Float,
    modifier: Modifier = Modifier,
    janela: Int = 100,
    altura: Dp = 150.dp,
    divisoes: Int = 4,
    unidade: String? = null,
    casas: Int = 2,
    rotuloTempo: Pair<String, String>? = "−10 s" to "agora",
    legenda: Boolean = true,
) {
    val c = OdTema.cores
    val t = OdTema.tipografia
    val medidor = rememberTextMeasurer()
    val estiloEixo = t.uiPequeno.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum", color = c.graficoRotulo)
    val estiloRotulo = estiloEixo.copy(fontWeight = FontWeight.SemiBold, color = c.texto)
    Column(modifier.semantics {
        contentDescription = series.joinToString("; ") { s -> "${s.rotulo} ${s.valores.lastOrNull()?.let { odFormatar(it.toDouble(), casas).trim() } ?: "sem dados"}" } + (unidade?.let { " $it" } ?: "")
    }) {
        Canvas(Modifier.fillMaxWidth().height(altura)) {
            val esq = 30.dp.toPx(); val dir = 22.dp.toPx(); val topo = 8.dp.toPx(); val base = if (rotuloTempo != null) 18.dp.toPx() else 4.dp.toPx()
            val w = size.width - esq - dir; val h = size.height - topo - base
            fun y(v: Float) = topo + h * (1f - (v.coerceIn(minimo, maximo) - minimo) / (maximo - minimo))
            for (i in 0..divisoes) {
                val v = maximo - (maximo - minimo) * i / divisoes
                val yy = topo + h * i / divisoes
                val zero = v == 0f
                drawLine(if (zero) c.graficoEixo else c.graficoGrade, Offset(esq, yy), Offset(esq + w, yy), 1.dp.toPx())
                val txt = odFormatar(v.toDouble(), if (v % 1f == 0f) 0 else 1, OdSinal.Negativo).trim()
                val m = medidor.measure(txt, estiloEixo)
                drawText(m, topLeft = Offset(esq - 6.dp.toPx() - m.size.width, yy - m.size.height / 2f))
            }
            if (rotuloTempo != null) {
                val a = medidor.measure(rotuloTempo.first, estiloEixo); val b = medidor.measure(rotuloTempo.second, estiloEixo)
                drawText(a, topLeft = Offset(esq, size.height - a.size.height))
                drawText(b, topLeft = Offset(esq + w - b.size.width, size.height - b.size.height))
            }
            val passo = w / (janela - 1).coerceAtLeast(1)
            val finais = mutableListOf<Pair<OdSerie, Float>>()
            series.forEach { s ->
                val n = s.valores.size
                if (n == 0) return@forEach
                val inicio = (n - janela).coerceAtLeast(0)
                val caminho = Path()
                for (i in inicio until n) {
                    val x = esq + w - (n - 1 - i) * passo
                    if (i == inicio) caminho.moveTo(x, y(s.valores[i])) else caminho.lineTo(x, y(s.valores[i]))
                }
                drawPath(caminho, s.cor, style = Stroke((if (s.grossa) 2.5f else 2f).dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                finais += s to y(s.valores.last())
            }
            // Rótulos diretos no fim das linhas, afastados para não se sobreporem.
            val minimoEntre = 12.dp.toPx()
            var anterior = Float.NEGATIVE_INFINITY
            finais.sortedBy { it.second }.forEach { (s, yy) ->
                val pos = maxOf(yy, anterior + minimoEntre)
                anterior = pos
                val m = medidor.measure(s.rotulo, estiloRotulo)
                drawText(m, topLeft = Offset(esq + w + 5.dp.toPx(), pos - m.size.height / 2f))
            }
        }
        if (legenda) FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OdEspacos.e4), verticalArrangement = Arrangement.spacedBy(OdEspacos.e1)) {
            series.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(14.dp, 2.dp).background(s.cor))
                    Text(s.rotulo, style = t.uiPequeno.copy(fontWeight = FontWeight.Medium), color = c.texto2)
                    Text(s.valores.lastOrNull()?.let { odFormatar(it.toDouble(), casas) } ?: "", style = t.uiPequeno.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"), color = c.texto)
                }
            }
            if (unidade != null) Text(unidade, style = t.uiPequeno, color = c.texto2)
        }
    }
}
