package io.github.andrexagris.ferramentas.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.andrexagris.ferramentas.catalogo.Disponibilidade
import io.github.andrexagris.ferramentas.catalogo.Ferramenta
import io.github.andrexagris.ferramentas.catalogo.Grupo
import io.github.andrexagris.ferramentas.ui.FtIcones
import io.github.andrexagris.ferramentas.ui.icone
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdBotaoIcone
import io.github.andrexagris.opendesign.OdChipEstado
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdMedidas
import io.github.andrexagris.opendesign.OdRaios
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoChip

/** "1 ferramenta", "12 ferramentas". */
fun quantidadeFerramentas(n: Int): String = if (n == 1) "1 ferramenta" else "$n ferramentas"

/**
 * Cartão de um grupo na grade do Início: ícone, nome e quantidade de ferramentas.
 * indisponiveis > 0 acrescenta a contagem em texto (estado nunca só por cor).
 */
@Composable
fun FtCartaoGrupo(grupo: Grupo, total: Int, indisponiveis: Int, aoClicar: () -> Unit, modifier: Modifier = Modifier) {
    val c = OdTema.cores
    val t = OdTema.tipografia
    val forma = RoundedCornerShape(OdRaios.painel)
    val detalhe = quantidadeFerramentas(total) + if (indisponiveis > 0) ", $indisponiveis indisponíve${if (indisponiveis == 1) "l" else "is"}" else ""
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = OdMedidas.alvoToque)
            .clip(forma)
            .background(c.superficie)
            .border(OdMedidas.bordaFina, c.borda, forma)
            .clickable(role = Role.Button, onClick = aoClicar)
            .semantics(mergeDescendants = true) { contentDescription = "${grupo.nome}, $detalhe" }
            .padding(OdEspacos.e4 * OdTema.densidade.fator),
        verticalArrangement = Arrangement.spacedBy(OdEspacos.e2),
    ) {
        Box(
            Modifier.size(40.dp).background(c.secundaria.fundo, RoundedCornerShape(OdRaios.controle)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(grupo.icone, null, Modifier.size(22.dp), tint = c.secundaria.texto)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(grupo.nome, style = t.uiSubtitulo, color = c.texto, maxLines = 1)
            Text(detalhe, style = t.uiPequeno, color = c.texto2, minLines = 2, maxLines = 2)
        }
    }
}

/** Estrela de favorito: traço quando não é favorita, preenchida quando é. O leitor de tela fala a ação. */
@Composable
fun FtBotaoFavorito(favorita: Boolean, aoAlternar: () -> Unit, nome: String) {
    OdBotaoIcone(
        if (favorita) FtIcones.StarFilled else FtIcones.Star,
        if (favorita) "Remover $nome dos favoritos" else "Adicionar $nome aos favoritos",
        aoAlternar,
    )
}

/**
 * Linha de ferramenta nas listas (grupo, favoritos, busca). Indisponível: em cinza, com o motivo na descrição
 * e a etiqueta "Indisponível"; continua tocável para mostrar o motivo em vez de abrir e falhar.
 * comContexto: mostra "Grupo · Seção" (busca e favoritos), em vez da descrição da ferramenta.
 */
@Composable
fun FtLinhaFerramenta(
    ferramenta: Ferramenta,
    disponibilidade: Disponibilidade,
    favorita: Boolean,
    aoAbrir: () -> Unit,
    aoAlternarFavorito: () -> Unit,
    comContexto: Boolean = false,
) {
    val indisponivel = disponibilidade as? Disponibilidade.Indisponivel
    val descricao = when {
        indisponivel != null -> indisponivel.motivo
        comContexto -> "${ferramenta.grupo.nome} · ${ferramenta.secao}"
        else -> ferramenta.descricao
    }
    OdLinhaConfig(
        titulo = ferramenta.nome,
        modifier = Modifier
            .clickable(role = Role.Button, onClick = aoAbrir)
            .semantics { if (indisponivel != null) stateDescription = "Indisponível" },
        descricao = descricao,
        icone = ferramenta.icone,
        habilitado = indisponivel == null,
    ) {
        if (indisponivel != null) OdChipEstado(OdTipoChip.Indisponivel)
        FtBotaoFavorito(favorita, aoAlternarFavorito, ferramenta.nome)
    }
}

/**
 * Aviso de tela inteira para listas vazias e telas que ainda não existem: ícone, título, texto e uma ação.
 * Mesmo desenho do OdEstadoRecurso, para o que não é estado de recurso do aparelho.
 */
@Composable
fun FtAviso(
    icone: ImageVector,
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier,
    acao: String? = null,
    aoAgir: () -> Unit = {},
) {
    val c = OdTema.cores
    Column(
        modifier.fillMaxWidth().padding(horizontal = OdEspacos.e4, vertical = OdEspacos.e6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OdEspacos.e2),
    ) {
        Box(Modifier.size(56.dp).background(c.superficieSutil, RoundedCornerShape(OdRaios.painel)), contentAlignment = Alignment.Center) {
            Icon(icone, null, Modifier.size(28.dp), tint = c.texto2)
        }
        Text(
            titulo, style = OdTema.tipografia.uiSubtitulo, color = c.texto, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = OdEspacos.e2).semantics { heading() },
        )
        Text(texto, style = OdTema.tipografia.ui, color = c.texto2, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 320.dp))
        if (acao != null) OdBotao(acao, aoAgir, Modifier.padding(top = OdEspacos.e3), tipo = OdTipoBotao.Secundario)
    }
}
