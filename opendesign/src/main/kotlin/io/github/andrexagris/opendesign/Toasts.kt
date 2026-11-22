package io.github.andrexagris.opendesign

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

enum class OdTipoToast { Sucesso, Info, Aviso, Erro }

class OdToast internal constructor(
    val id: Long, val texto: String, val tipo: OdTipoToast, val acao: String?, val aoAgir: (() -> Unit)?, val duracaoMs: Long?,
)

/**
 * Fila de toasts. Some sozinho em duracao-toast (5 s); com ação, fica até a pessoa agir ou fechar.
 * Pausa enquanto o dedo está sobre ele. No máximo três (o mais antigo sai). Arrastar para o lado dispensa.
 * Com o TalkBack ligado, o tempo segue o recomendado pelo sistema.
 */
@Stable
class OdToasts internal constructor() {
    internal val itens = mutableStateListOf<OdToast>()
    private var proximo = 0L

    fun mostrar(texto: String, tipo: OdTipoToast = OdTipoToast.Sucesso, acao: String? = null, aoAgir: (() -> Unit)? = null, duracaoMs: Long? = null) {
        val duracao = duracaoMs ?: if (acao != null) null else OdMovimento.duracaoToast.toLong()
        if (itens.size >= 3) itens.removeAt(0)
        itens.add(OdToast(proximo++, texto, tipo, acao, aoAgir, duracao))
    }

    fun fechar(t: OdToast) { itens.remove(t) }
}

@Composable
fun rememberOdToasts(): OdToasts = remember { OdToasts() }

/** Região dos toasts. O OdTelaApp já a coloca acima da barra inferior; use direto só fora dele. */
@Composable
fun OdRegiaoToasts(estado: OdToasts, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = OdEspacos.e4, vertical = OdEspacos.e3),
        verticalArrangement = Arrangement.spacedBy(OdEspacos.e2), horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        estado.itens.forEach { t -> key(t.id) { ItemToast(t, estado) } }
    }
}

@Composable
private fun ItemToast(t: OdToast, estado: OdToasts) {
    val c = OdTema.cores
    val forma = RoundedCornerShape(OdRaios.flutuante)
    val interacao = remember { MutableInteractionSource() }
    val hover by interacao.collectIsHoveredAsState()
    var tocando by remember { mutableStateOf(false) }
    val pausado by rememberUpdatedState(hover || tocando)
    val acessibilidade = LocalAccessibilityManager.current
    LaunchedEffect(t.id) {
        val base = t.duracaoMs ?: return@LaunchedEffect
        var resta = acessibilidade?.calculateRecommendedTimeoutMillis(base, containsIcons = true, containsText = true, containsControls = t.acao != null) ?: base
        while (resta > 0) { delay(100); if (!pausado) resta -= 100 }
        estado.fechar(t)
    }
    val deslocamento = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()
    val limite = with(LocalDensity.current) { 80.dp.toPx() }
    val entrada = remember { MutableTransitionState(false).apply { targetState = true } }
    val (icone, cor) = when (t.tipo) {
        OdTipoToast.Sucesso -> OdIcones.Sucesso to c.sucesso.texto
        OdTipoToast.Info -> OdIcones.Info to c.info.texto
        OdTipoToast.Aviso -> OdIcones.Aviso to c.aviso.texto
        OdTipoToast.Erro -> OdIcones.Erro to c.erro.texto
    }
    AnimatedVisibility(entrada, enter = fadeIn(tween(OdTema.duracao(OdMovimento.duracao))) + slideInVertically(tween(OdTema.duracao(OdMovimento.duracao))) { it / 3 }) {
        Row(
            Modifier.widthIn(max = 420.dp).fillMaxWidth()
                .offset { IntOffset(deslocamento.value.roundToInt(), 0) }
                .graphicsLayer { alpha = 1f - (abs(deslocamento.value) / (limite * 3)).coerceIn(0f, 0.8f) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { d -> escopo.launch { deslocamento.snapTo(deslocamento.value + d) } },
                    onDragStopped = {
                        if (abs(deslocamento.value) > limite) { deslocamento.animateTo(sign(deslocamento.value) * limite * 6); estado.fechar(t) }
                        else deslocamento.animateTo(0f)
                    },
                )
                .hoverable(interacao)
                .pointerInput(Unit) { awaitPointerEventScope { while (true) { val e = awaitPointerEvent(PointerEventPass.Initial); tocando = e.changes.any { it.pressed } } } }
                .odSombra(OdTema.sombras.menu, forma)
                .background(c.superficieElevada, forma).border(OdMedidas.bordaFina, c.borda, forma)
                .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                .semantics { liveRegion = if (t.tipo == OdTipoToast.Erro) LiveRegionMode.Assertive else LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OdEspacos.e2),
        ) {
            Icon(icone, null, Modifier.size(18.dp), tint = cor)
            Text(t.texto, style = OdTema.tipografia.ui, color = c.texto, modifier = Modifier.weight(1f).padding(vertical = 10.dp))
            if (t.acao != null) OdBotao(t.acao, { t.aoAgir?.invoke(); estado.fechar(t) }, tipo = OdTipoBotao.Terciario)
            OdBotaoIcone(OdIcones.Fechar, "Fechar", { estado.fechar(t) })
        }
    }
}
