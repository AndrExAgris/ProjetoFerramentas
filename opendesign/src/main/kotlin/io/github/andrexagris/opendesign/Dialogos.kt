package io.github.andrexagris.opendesign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Diálogo modal para decisões importantes. Título em forma de pergunta que nomeia o objeto, uma frase com a
 * consequência e as ações. destrutivo = a confirmação vira DestrutivoForte (só para o que não se desfaz).
 * Em telas com menos de 400 dp, as ações empilham em largura total, a principal em cima.
 * Toque fora e Voltar fecham (aoFechar), a menos que fixo = true.
 */
@Composable
fun OdDialogo(
    titulo: String,
    texto: String?,
    confirmar: String,
    aoConfirmar: () -> Unit,
    aoFechar: () -> Unit,
    cancelar: String? = "Cancelar",
    destrutivo: Boolean = false,
    icone: ImageVector? = null,
    fixo: Boolean = false,
    conteudo: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val c = OdTema.cores
    val forma = RoundedCornerShape(OdRaios.flutuante)
    Dialog(onDismissRequest = aoFechar, properties = DialogProperties(dismissOnBackPress = !fixo, dismissOnClickOutside = !fixo, usePlatformDefaultWidth = false)) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(OdEspacos.e4), contentAlignment = Alignment.Center) {
            val estreito = maxWidth < 400.dp
            Column(
                Modifier.widthIn(max = 440.dp).fillMaxWidth()
                    .odSombra(OdTema.sombras.dialogo, forma)
                    .background(c.superficieElevada, forma).border(OdMedidas.bordaFina, c.borda, forma)
                    .padding(if (estreito) OdEspacos.e5 - 4.dp else OdEspacos.e5)
                    .semantics { paneTitle = titulo },
            ) {
                if (icone != null) {
                    Box(Modifier.size(40.dp).background(if (destrutivo) c.erro.fundo else c.primaria.fundo, RoundedCornerShape(OdRaios.controle)), contentAlignment = Alignment.Center) {
                        Icon(icone, null, Modifier.size(22.dp), tint = if (destrutivo) c.erro.texto else c.primaria.texto)
                    }
                    Box(Modifier.size(OdEspacos.e3))
                }
                Text(titulo, style = OdTema.tipografia.uiTitulo, color = c.texto, modifier = Modifier.semantics { heading() })
                if (texto != null) Text(texto, style = OdTema.tipografia.ui, color = c.texto2, modifier = Modifier.padding(top = OdEspacos.e2))
                if (conteudo != null) Column(Modifier.padding(top = OdEspacos.e3), content = conteudo)
                val tipo = if (destrutivo) OdTipoBotao.DestrutivoForte else OdTipoBotao.Primario
                val iconeConfirmar = if (destrutivo) OdIcones.Lixeira else null
                if (estreito) {
                    Column(Modifier.fillMaxWidth().padding(top = OdEspacos.e5), verticalArrangement = Arrangement.spacedBy(OdEspacos.e1)) {
                        OdBotao(confirmar, aoConfirmar, Modifier.fillMaxWidth(), tipo, iconeConfirmar)
                        if (cancelar != null) OdBotao(cancelar, aoFechar, Modifier.fillMaxWidth(), OdTipoBotao.Terciario)
                    }
                } else {
                    Row(Modifier.fillMaxWidth().padding(top = OdEspacos.e5), horizontalArrangement = Arrangement.spacedBy(OdEspacos.e2, Alignment.End)) {
                        if (cancelar != null) OdBotao(cancelar, aoFechar, tipo = OdTipoBotao.Terciario)
                        OdBotao(confirmar, aoConfirmar, tipo = tipo, icone = iconeConfirmar)
                    }
                }
            }
        }
    }
}

/**
 * Folha inferior (bottom sheet): escolhas e ações que vêm de baixo. Fecha arrastando, tocando fora ou com Voltar.
 *
 *     if (aberta) OdFolhaInferior(aoFechar = { aberta = false }, titulo = "Exportar leituras") {
 *         OdItemFolha("Planilha (CSV)", OdIcones.Arquivo) { … }
 *     }
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdFolhaInferior(aoFechar: () -> Unit, titulo: String? = null, descricao: String? = null, conteudo: @Composable ColumnScope.() -> Unit) {
    val c = OdTema.cores
    val forma = RoundedCornerShape(topStart = OdRaios.flutuante, topEnd = OdRaios.flutuante)
    ModalBottomSheet(
        onDismissRequest = aoFechar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = forma,
        containerColor = c.superficieElevada,
        contentColor = c.texto,
        scrimColor = c.sobreposicaoModal,
        tonalElevation = 0.dp,
        dragHandle = { Box(Modifier.padding(vertical = 12.dp).size(36.dp, 4.dp).background(c.bordaControle, CircleShape)) },
        modifier = Modifier.widthIn(max = 640.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = OdEspacos.e4).padding(bottom = OdEspacos.e4).navigationBarsPadding()) {
            if (titulo != null) Text(titulo, style = OdTema.tipografia.uiTitulo, modifier = Modifier.semantics { heading() })
            if (descricao != null) Text(descricao, style = OdTema.tipografia.ui, color = c.texto2, modifier = Modifier.padding(top = OdEspacos.e1))
            if (titulo != null || descricao != null) Box(Modifier.size(OdEspacos.e3))
            conteudo()
        }
    }
}

/** Item de ação dentro de uma folha inferior (48 dp, texto 15). */
@Composable
fun OdItemFolha(texto: String, icone: ImageVector? = null, aoClicar: () -> Unit) {
    val c = OdTema.cores
    Row(
        Modifier.fillMaxWidth().heightIn(min = OdMedidas.alvoToque).clickable(role = Role.Button, onClick = aoClicar).padding(horizontal = OdEspacos.e3),
        horizontalArrangement = Arrangement.spacedBy(OdEspacos.e3), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icone != null) Icon(icone, null, Modifier.size(20.dp), tint = c.texto2)
        Text(texto, style = OdTema.tipografia.ui.copy(fontSize = OdTema.tipografia.uiSubtitulo.fontSize), color = c.texto)
    }
}
