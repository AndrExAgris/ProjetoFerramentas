package io.github.andrexagris.opendesign

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset

/*
 * Sombras no Compose
 *
 * O CSS desenha box-shadow com deslocamento, desfoque, espalhamento e cor; o Modifier.shadow() antigo do
 * Compose não: ele simula uma luz de cima a partir de uma "elevação" e o resultado muda de aparelho para
 * aparelho. Desde o Compose 1.9 (BOM 2025.08.00) existem Modifier.dropShadow() e Modifier.innerShadow(),
 * que funcionam como o box-shadow. O Open Design usa esses dois:
 *
 * - sombra-relevo (botões em repouso): as mesmas camadas do CSS, com dropShadow, ANTES do background.
 * - pressionar: a sombra some (alfa 0) em duracao-rapida; o interior não muda. Igual à web.
 * - sombra-afundado (campos): innerShadow DEPOIS do background.
 * - menus, diálogos e folhas: sombra-menu e sombra-dialogo também por dropShadow.
 *
 * No modo escuro as sombras quase não aparecem; quem separa as camadas é a luminosidade das superfícies
 * (fundo < superficie < superficie-elevada), como na web.
 */

/** Desenha as camadas externas de uma sombra do Open Design. Use antes do background. */
fun Modifier.odSombra(camadas: List<OdCamadaSombra>, forma: Shape, alfa: Float = 1f): Modifier =
    camadas.filter { !it.interna }.fold(this) { m, c ->
        m.dropShadow(forma, Shadow(radius = c.desfoque, spread = c.espalhamento, color = c.cor, offset = DpOffset(c.x, c.y), alpha = alfa))
    }

/** Desenha as camadas internas (inset). Use depois do background. */
fun Modifier.odSombraInterna(camadas: List<OdCamadaSombra>, forma: Shape, alfa: Float = 1f): Modifier =
    camadas.filter { it.interna }.fold(this) { m, c ->
        m.innerShadow(forma, Shadow(radius = c.desfoque, spread = c.espalhamento, color = c.cor, offset = DpOffset(c.x, c.y), alpha = alfa))
    }

/** Relevo de botão: aparece em repouso e some ao pressionar (ou quando ativo = false). */
@Composable
fun Modifier.odRelevo(forma: Shape, pressionado: Boolean, ativo: Boolean = true): Modifier {
    val alfa by animateFloatAsState(
        targetValue = if (pressionado || !ativo) 0f else 1f,
        animationSpec = tween(OdTema.duracao(OdMovimento.duracaoRapida), easing = OdMovimento.curva),
        label = "relevo",
    )
    return odSombra(OdTema.sombras.relevo, forma, alfa)
}

/** Afundado: campos preenchíveis. */
@Composable
fun Modifier.odAfundado(forma: Shape): Modifier = odSombraInterna(OdTema.sombras.afundado, forma)
