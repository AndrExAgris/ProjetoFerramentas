package io.github.andrexagris.opendesign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Importância do botão. Risco é outro eixo: Destrutivo (contornado) ou DestrutivoForte (preenchido, só para o que não se desfaz). */
enum class OdTipoBotao { Primario, Secundario, Terciario, Destrutivo, DestrutivoForte }

private data class VisualBotao(val fundo: Color, val conteudo: Color, val borda: Color, val relevo: Boolean)

/**
 * Botão do Open Design: relevo leve em repouso (sombra-relevo), camada de 8% na cor do texto no hover e no
 * pressionado, e a sombra some ao pressionar. Desenho de altura-controle (36 na densidade equilibrada) e área
 * de toque de 48 dp.
 */
@Composable
fun OdBotao(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    tipo: OdTipoBotao = OdTipoBotao.Secundario,
    icone: ImageVector? = null,
    habilitado: Boolean = true,
    carregando: Boolean = false,
) {
    val c = OdTema.cores
    val v = when {
        !habilitado -> VisualBotao(if (tipo == OdTipoBotao.Terciario) Color.Transparent else c.superficieSutil, c.textoDesabilitado, Color.Transparent, false)
        tipo == OdTipoBotao.Primario -> VisualBotao(c.primaria.cor, c.primaria.sobre, Color.Transparent, true)
        tipo == OdTipoBotao.Secundario -> VisualBotao(c.superficieSutil, c.texto, c.bordaControle, true)
        tipo == OdTipoBotao.Terciario -> VisualBotao(Color.Transparent, c.texto, Color.Transparent, false)
        tipo == OdTipoBotao.Destrutivo -> VisualBotao(Color.Transparent, c.erro.texto, c.erro.cor, false)
        else -> VisualBotao(c.erro.cor, c.erro.sobre, Color.Transparent, true)
    }
    val forte = tipo == OdTipoBotao.Primario || tipo == OdTipoBotao.DestrutivoForte
    BaseBotao(aoClicar, modifier, v, habilitado && !carregando, Role.Button, null) {
        Row(
            Modifier.padding(horizontal = OdEspacos.e3 * OdTema.densidade.fator + 2.dp).alpha(if (carregando) 0f else 1f),
            horizontalArrangement = Arrangement.spacedBy(OdEspacos.e2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icone != null) Icon(icone, null, Modifier.size(18.dp))
            Text(texto, style = OdTema.tipografia.uiMedio.copy(fontWeight = if (forte) FontWeight.SemiBold else FontWeight.Medium), maxLines = 1)
        }
        if (carregando) CircularProgressIndicator(Modifier.size(16.dp), color = v.conteudo, strokeWidth = 2.dp)
    }
}

/** Botão só com ícone (terciário). descricao é obrigatória: é o que o leitor de tela fala. */
@Composable
fun OdBotaoIcone(icone: ImageVector, descricao: String, aoClicar: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    val c = OdTema.cores
    val v = VisualBotao(Color.Transparent, if (habilitado) c.texto else c.textoDesabilitado, Color.Transparent, false)
    BaseBotao(aoClicar, modifier, v, habilitado, Role.Button, descricao, quadrado = true) {
        Icon(icone, null, Modifier.size(20.dp))
    }
}

@Composable
private fun BaseBotao(
    aoClicar: () -> Unit,
    modifier: Modifier,
    v: VisualBotao,
    habilitado: Boolean,
    papel: Role,
    descricao: String?,
    quadrado: Boolean = false,
    conteudo: @Composable () -> Unit,
) {
    val forma = RoundedCornerShape(OdRaios.controle)
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val hover by interacao.collectIsHoveredAsState()
    val altura = OdTema.densidade.alturaControle
    Box(
        modifier
            .minimumInteractiveComponentSize()
            .then(if (v.relevo) Modifier.odRelevo(forma, pressionado, habilitado) else Modifier)
            .clip(forma)
            .background(v.fundo)
            .then(if (v.borda != Color.Transparent) Modifier.border(OdMedidas.bordaFina, v.borda, forma) else Modifier)
            .drawWithContent {
                drawContent()
                if (habilitado && (pressionado || hover)) drawRect(v.conteudo.copy(alpha = if (pressionado) OdOpacidades.pressionado else OdOpacidades.hover))
            }
            .clickable(interactionSource = interacao, indication = null, enabled = habilitado, role = papel, onClick = aoClicar)
            .then(if (descricao != null) Modifier.semantics { contentDescription = descricao } else Modifier)
            .heightIn(min = altura)
            .then(if (quadrado) Modifier.size(altura) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides v.conteudo) { conteudo() }
    }
}
