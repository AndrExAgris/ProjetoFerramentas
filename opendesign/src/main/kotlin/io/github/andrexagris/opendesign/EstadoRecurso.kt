package io.github.andrexagris.opendesign

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Indisponivel: o aparelho não tem o recurso. Permissao: o Android exige autorização. Medindo: leitura em andamento. */
enum class OdTipoEstado { Indisponivel, Permissao, Medindo }

/** Versões curtas, para cards e barras. */
enum class OdTipoChip { Medindo, Pausado, Permissao, Indisponivel }

/**
 * Estado de um recurso do aparelho, em tela cheia ou no lugar de um card.
 * - Indisponivel: sem ação; diga o que falta ("Este celular não tem barômetro").
 * - Permissao: explique o porquê; acao = "Permitir microfone" (ou "Abrir configurações" se a pessoa negou de vez).
 * - Medindo: ponto pulsante, tempo decorrido no texto e acao = "Parar".
 */
@Composable
fun OdEstadoRecurso(
    tipo: OdTipoEstado,
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier,
    icone: ImageVector = when (tipo) { OdTipoEstado.Indisponivel -> OdIcones.Indisponivel; OdTipoEstado.Permissao -> OdIcones.Cadeado; OdTipoEstado.Medindo -> OdIcones.Medidor },
    acao: String? = null,
    aoAgir: () -> Unit = {},
) {
    val c = OdTema.cores
    val (fundoIcone, corIcone) = when (tipo) {
        OdTipoEstado.Indisponivel -> c.superficieSutil to c.texto2
        OdTipoEstado.Permissao -> c.info.fundo to c.info.texto
        OdTipoEstado.Medindo -> c.primaria.fundo to c.primaria.texto
    }
    Column(
        modifier.fillMaxWidth().padding(horizontal = OdEspacos.e4, vertical = OdEspacos.e6),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(OdEspacos.e2),
    ) {
        Box(Modifier.size(56.dp).background(fundoIcone, RoundedCornerShape(OdRaios.painel)), contentAlignment = Alignment.Center) {
            Icon(icone, null, Modifier.size(28.dp), tint = corIcone)
        }
        Row(Modifier.padding(top = OdEspacos.e2), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (tipo == OdTipoEstado.Medindo) OdPontoAoVivo()
            Text(titulo, style = OdTema.tipografia.uiSubtitulo, color = if (tipo == OdTipoEstado.Indisponivel) c.texto2 else c.texto, modifier = Modifier.semantics { heading() })
        }
        Text(texto, style = OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum"), color = c.texto2, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 320.dp))
        if (acao != null) OdBotao(
            acao, aoAgir, Modifier.padding(top = OdEspacos.e3),
            tipo = if (tipo == OdTipoEstado.Medindo) OdTipoBotao.Secundario else OdTipoBotao.Primario,
            icone = if (tipo == OdTipoEstado.Medindo) OdIcones.Parar else null,
        )
    }
}

/** Etiqueta curta de estado: Medindo (com ponto pulsante), Pausado, Sem permissão, Indisponível. */
@Composable
fun OdChipEstado(tipo: OdTipoChip, texto: String = when (tipo) { OdTipoChip.Medindo -> "Medindo"; OdTipoChip.Pausado -> "Pausado"; OdTipoChip.Permissao -> "Sem permissão"; OdTipoChip.Indisponivel -> "Indisponível" }) {
    val c = OdTema.cores
    val fundo = when (tipo) { OdTipoChip.Medindo -> c.primaria.fundo; OdTipoChip.Permissao -> c.info.fundo; else -> c.superficieSutil }
    Row(
        Modifier.height(22.dp).background(fundo, RoundedCornerShape(OdRaios.indicador)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when (tipo) {
            OdTipoChip.Medindo -> OdPontoAoVivo()
            OdTipoChip.Pausado -> Icon(OdIcones.Pausar, null, Modifier.size(14.dp), tint = c.texto2)
            OdTipoChip.Permissao -> Icon(OdIcones.Cadeado, null, Modifier.size(14.dp), tint = c.info.texto)
            OdTipoChip.Indisponivel -> Icon(OdIcones.Indisponivel, null, Modifier.size(14.dp), tint = c.texto2)
        }
        Text(texto, style = OdTema.tipografia.uiPequeno.copy(fontWeight = FontWeight.Medium), color = if (tipo == OdTipoChip.Medindo || tipo == OdTipoChip.Permissao) c.texto else c.texto2)
    }
}

/** Ponto de medição em andamento: pulsa em duracao-pulso; parado com reduzir animações. */
@Composable
fun OdPontoAoVivo(modifier: Modifier = Modifier) {
    val cor = OdTema.cores.primaria.texto
    val reduzido = OdTema.movimentoReduzido
    val progresso = if (reduzido) 1f else {
        val t = rememberInfiniteTransition(label = "pulso")
        val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(OdMovimento.duracaoPulso, easing = LinearEasing), RepeatMode.Restart), label = "pulso")
        p
    }
    Box(
        modifier.size(8.dp).drawBehind {
            val fase = (progresso / 0.7f).coerceAtMost(1f)
            if (fase < 1f) drawCircle(cor.copy(alpha = 0.6f * (1f - fase)), radius = size.minDimension / 2 + 7.dp.toPx() * fase)
            drawCircle(cor)
        },
    )
}
