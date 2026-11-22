package io.github.andrexagris.opendesign

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Um destino da barra inferior. iconeAtivo: versão preenchida para o item atual (quando a Tabler tem).
 * contador: null = sem marca; 0 = ponto; N = número (acima de 99 vira "99+").
 */
data class OdDestino(
    val id: String,
    val rotulo: String,
    val icone: ImageVector,
    val iconeAtivo: ImageVector? = null,
    val contador: Int? = null,
)

/**
 * Barra de navegação inferior: 3 a 5 destinos, o último pode ser "Mais" (abre a tela de configurações e
 * áreas secundárias). Item atual com ícone preenchido em primaria.texto sobre a marca primaria.fundo.
 * Altura altura-nav-inferior (64 dp) mais a área segura de baixo.
 */
@Composable
fun OdBarraInferior(destinos: List<OdDestino>, atual: String, aoEscolher: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = OdTema.cores
    Column(modifier.fillMaxWidth().background(c.superficie)) {
        HorizontalDivider(thickness = OdMedidas.bordaFina, color = c.borda)
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars)
                .height(OdMedidas.alturaNavInferior).padding(horizontal = 8.dp).selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinos.forEach { d -> ItemBarra(d, d.id == atual) { aoEscolher(d.id) } }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ItemBarra(d: OdDestino, selecionado: Boolean, aoClicar: () -> Unit) {
    val c = OdTema.cores
    val t = OdTema.tipografia
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val marca by animateColorAsState(
        when {
            selecionado -> c.primaria.fundo
            pressionado -> c.sobreposicaoPressionado
            else -> Color.Transparent
        },
        tween(OdTema.duracao(OdMovimento.duracao), easing = OdMovimento.curva), label = "marca",
    )
    Column(
        Modifier.weight(1f).fillMaxHeight()
            .selectable(selected = selecionado, onClick = aoClicar, role = Role.Tab, interactionSource = interacao, indication = null)
            .semantics { if (d.contador != null && d.contador > 0) stateDescription = "${d.contador} novos" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.size(width = 56.dp, height = 30.dp).background(marca, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
            BadgedBox(badge = { if (d.contador != null) OdContador(d.contador) }) {
                Icon(
                    if (selecionado) d.iconeAtivo ?: d.icone else d.icone, null,
                    Modifier.size(22.dp), tint = if (selecionado) c.primaria.texto else c.texto2,
                )
            }
        }
        Text(
            d.rotulo, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = t.uiPequeno.copy(fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Medium),
            color = if (selecionado) c.texto else c.texto2,
        )
    }
}

/** Contador de novidades (erro com texto erro.sobre). 0 mostra só um ponto. */
@Composable
fun OdContador(n: Int) {
    val c = OdTema.cores
    if (n <= 0) Badge(containerColor = c.erro.cor, contentColor = c.erro.sobre)
    else Badge(containerColor = c.erro.cor, contentColor = c.erro.sobre) {
        Text(if (n > 99) "99+" else n.toString(), style = OdTema.tipografia.uiPequeno.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"))
    }
}
