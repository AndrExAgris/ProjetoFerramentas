package io.github.andrexagris.opendesign

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

enum class OdLarguraTela { Compacta, Media, Expandida }

/** Colunas, margem e medianiz para uma largura (os mesmos cortes das classes de janela do Android: 600 e 840 dp). */
@Immutable
data class OdMedidasGrade(val largura: OdLarguraTela, val colunas: Int, val margem: Dp, val medianiz: Dp)

fun odGradePara(larguraDisponivel: Dp): OdMedidasGrade = when {
    larguraDisponivel >= OdMedidas.pontoExpandida -> OdMedidasGrade(OdLarguraTela.Expandida, OdMedidas.gradeColunasExpandida, OdMedidas.gradeMargemExpandida, OdMedidas.gradeMedianizExpandida)
    larguraDisponivel >= OdMedidas.pontoMedia -> OdMedidasGrade(OdLarguraTela.Media, OdMedidas.gradeColunasMedia, OdMedidas.gradeMargemMedia, OdMedidas.gradeMedianizMedia)
    else -> OdMedidasGrade(OdLarguraTela.Compacta, OdMedidas.gradeColunasCompacta, OdMedidas.gradeMargemCompacta, OdMedidas.gradeMedianizCompacta)
}

class OdGradeEscopo internal constructor() {
    internal class Celula(val compacta: Int, val media: Int, val expandida: Int, val conteudo: @Composable () -> Unit)
    internal val celulas = mutableListOf<Celula>()

    /** Uma célula que ocupa N colunas: compacta de 1 a 4, média de 1 a 8, expandida de 1 a 12. Padrão: linha toda. */
    fun celula(compacta: Int = 4, media: Int = compacta * 2, expandida: Int = compacta * 3, conteudo: @Composable () -> Unit) {
        celulas += Celula(compacta, media, expandida, conteudo)
    }
}

/**
 * Grade de 4, 8 ou 12 colunas conforme a largura disponível.
 *
 *     OdGrade {
 *         celula(compacta = 2, expandida = 3) { OdCartaoLeitura("Bateria", "84", unidade = "%") }
 *         celula(compacta = 2, expandida = 3) { OdCartaoLeitura("Download", "42,8", unidade = "Mb/s") }
 *         celula { OdGraficoTempoReal(…) }
 *     }
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OdGrade(modifier: Modifier = Modifier, comMargem: Boolean = true, conteudo: OdGradeEscopo.() -> Unit) {
    val escopo = OdGradeEscopo().apply(conteudo)
    BoxWithConstraints(modifier) {
        val g = odGradePara(maxWidth)
        val util = maxWidth - if (comMargem) g.margem * 2 else Dp(0f)
        val coluna = (util - g.medianiz * (g.colunas - 1)) / g.colunas
        FlowRow(
            Modifier.padding(horizontal = if (comMargem) g.margem else Dp(0f)),
            horizontalArrangement = Arrangement.spacedBy(g.medianiz),
            verticalArrangement = Arrangement.spacedBy(g.medianiz),
            maxItemsInEachRow = g.colunas,
        ) {
            escopo.celulas.forEach { cel ->
                val n = when (g.largura) { OdLarguraTela.Compacta -> cel.compacta; OdLarguraTela.Media -> cel.media; OdLarguraTela.Expandida -> cel.expandida }.coerceIn(1, g.colunas)
                Box(Modifier.width(coluna * n + g.medianiz * (n - 1))) { cel.conteudo() }
            }
        }
    }
}
