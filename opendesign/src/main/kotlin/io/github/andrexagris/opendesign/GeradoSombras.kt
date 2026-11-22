// Gerado por ferramentas/gerar_compose.py a partir de tokens-resolvidos.json. Não edite à mão.
package io.github.andrexagris.opendesign

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Uma camada de sombra, igual a uma parte do box-shadow do CSS. interna = inset (innerShadow). */
@Immutable
data class OdCamadaSombra(val x: Dp, val y: Dp, val desfoque: Dp, val espalhamento: Dp, val cor: Color, val interna: Boolean)

@Immutable
data class OdSombras(
    val relevo: List<OdCamadaSombra>,
    val afundado: List<OdCamadaSombra>,
    val painel: List<OdCamadaSombra>,
    val menu: List<OdCamadaSombra>,
    val dialogo: List<OdCamadaSombra>,
)

val odSombrasEscuro = OdSombras(
    relevo = listOf(OdCamadaSombra(0.dp, 1.dp, 2.dp, 0.dp, Color(0x73000000), false), OdCamadaSombra(0.dp, 3.dp, 8.dp, 0.dp, Color(0x47000000), false)),
    afundado = listOf(OdCamadaSombra(0.dp, 1.dp, 2.dp, 0.dp, Color(0x80000000), true)),
    painel = listOf(OdCamadaSombra(0.dp, 1.dp, 2.dp, 0.dp, Color(0x59000000), false)),
    menu = listOf(OdCamadaSombra(0.dp, 1.dp, 0.dp, 0.dp, Color(0x0DFFFFFF), true), OdCamadaSombra(0.dp, 8.dp, 20.dp, 0.dp, Color(0x73000000), false)),
    dialogo = listOf(OdCamadaSombra(0.dp, 1.dp, 0.dp, 0.dp, Color(0x12FFFFFF), true), OdCamadaSombra(0.dp, 20.dp, 48.dp, 0.dp, Color(0x8C000000), false)),
)

val odSombrasClaro = OdSombras(
    relevo = listOf(OdCamadaSombra(0.dp, 1.dp, 2.dp, 0.dp, Color(0x2610181F), false), OdCamadaSombra(0.dp, 3.dp, 8.dp, 0.dp, Color(0x1710181F), false)),
    afundado = listOf(OdCamadaSombra(0.dp, 1.dp, 2.dp, 0.dp, Color(0x1F10181F), true)),
    painel = listOf(OdCamadaSombra(0.dp, 1.dp, 2.dp, 0.dp, Color(0x0F10181F), false)),
    menu = listOf(OdCamadaSombra(0.dp, 6.dp, 16.dp, 0.dp, Color(0x1A10181F), false), OdCamadaSombra(0.dp, 1.dp, 3.dp, 0.dp, Color(0x1410181F), false)),
    dialogo = listOf(OdCamadaSombra(0.dp, 16.dp, 40.dp, 0.dp, Color(0x2910181F), false), OdCamadaSombra(0.dp, 2.dp, 6.dp, 0.dp, Color(0x1410181F), false)),
)
