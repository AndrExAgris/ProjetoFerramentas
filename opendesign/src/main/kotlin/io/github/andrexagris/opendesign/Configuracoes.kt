package io.github.andrexagris.opendesign

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Liga e desliga com efeito imediato. Trilho primaria quando ligado. Usa o Switch do Material 3 (52 × 32). */
@Composable
fun OdInterruptor(ligado: Boolean, aoMudar: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    val c = OdTema.cores
    Switch(
        checked = ligado, onCheckedChange = aoMudar, modifier = modifier, enabled = habilitado,
        colors = SwitchDefaults.colors(
            checkedThumbColor = c.primaria.sobre, checkedTrackColor = c.primaria.cor,
            checkedBorderColor = if (c.claro) c.primaria.texto else Color.Transparent, checkedIconColor = c.primaria.cor,
            uncheckedThumbColor = c.texto2, uncheckedTrackColor = c.superficieSutil, uncheckedBorderColor = c.bordaControle,
            disabledCheckedThumbColor = c.textoDesabilitado, disabledCheckedTrackColor = c.superficieSutil, disabledCheckedBorderColor = c.borda,
            disabledUncheckedThumbColor = c.textoDesabilitado, disabledUncheckedTrackColor = c.superficieSutil, disabledUncheckedBorderColor = c.borda,
        ),
    )
}

/** Grupo de configurações: título curto e as linhas numa lista com divisórias. */
@Composable
fun OdGrupoConfig(titulo: String?, modifier: Modifier = Modifier, linhas: @Composable ColumnScope.() -> Unit) {
    val c = OdTema.cores
    val forma = RoundedCornerShape(OdRaios.painel)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(OdEspacos.e2)) {
        if (titulo != null) Text(
            titulo, style = OdTema.tipografia.uiPequeno.copy(fontWeight = FontWeight.SemiBold), color = c.texto2,
            modifier = Modifier.padding(horizontal = OdEspacos.e1).semantics { heading() },
        )
        // A cor da borda aparece entre as linhas (espaço de 1 dp): divisórias sem desenhar nada a mais.
        Column(
            Modifier.fillMaxWidth().clip(forma).border(OdMedidas.bordaFina, c.borda, forma).background(c.borda),
            verticalArrangement = Arrangement.spacedBy(OdMedidas.bordaFina), content = linhas,
        )
    }
}

/** Linha base: ícone, título, descrição e o que vai à direita. */
@Composable
fun OdLinhaConfig(
    titulo: String,
    modifier: Modifier = Modifier,
    descricao: String? = null,
    icone: ImageVector? = null,
    habilitado: Boolean = true,
    perigo: Boolean = false,
    final: @Composable RowScope.() -> Unit = {},
) {
    val c = OdTema.cores
    val t = OdTema.tipografia
    val corTexto = when { !habilitado -> c.textoDesabilitado; perigo -> c.erro.texto; else -> c.texto }
    Row(
        modifier.fillMaxWidth().background(c.superficie)
            .heightIn(min = maxOf(OdMedidas.alvoToque, 56.dp * OdTema.densidade.fator))
            .padding(horizontal = OdEspacos.e4, vertical = OdEspacos.e2 * OdTema.densidade.fator),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OdEspacos.e3),
    ) {
        if (icone != null) Icon(icone, null, Modifier.size(20.dp), tint = when { !habilitado -> c.textoDesabilitado; perigo -> c.erro.texto; else -> c.texto2 })
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, style = t.uiMedio, color = corTexto)
            if (descricao != null) Text(descricao, style = t.uiPequeno, color = if (habilitado) c.texto2 else c.textoDesabilitado)
        }
        CompositionLocalProvider(LocalContentColor provides c.texto2) { final() }
    }
}

/** Linha com interruptor: a linha inteira liga e desliga. */
@Composable
fun OdLinhaInterruptor(titulo: String, ligado: Boolean, aoMudar: (Boolean) -> Unit, descricao: String? = null, icone: ImageVector? = null, habilitado: Boolean = true) {
    OdLinhaConfig(
        titulo, Modifier.toggleable(value = ligado, enabled = habilitado, role = Role.Switch, onValueChange = aoMudar),
        descricao, icone, habilitado,
    ) { OdInterruptor(ligado, null, habilitado = habilitado) }
}

/** Linha com o valor atual e seta: abre uma subtela ou uma folha inferior com as opções. */
@Composable
fun OdLinhaValor(titulo: String, valor: String?, aoClicar: () -> Unit, descricao: String? = null, icone: ImageVector? = null, habilitado: Boolean = true) {
    OdLinhaConfig(titulo, Modifier.clickable(enabled = habilitado, role = Role.Button, onClick = aoClicar), descricao, icone, habilitado) {
        if (valor != null) Text(valor, style = OdTema.tipografia.ui, color = OdTema.cores.texto2, maxLines = 1)
        Icon(OdIcones.Seta, null, Modifier.size(18.dp))
    }
}

/** Linha de ação simples (Restaurar padrões, Apagar histórico). perigo = texto erro.texto; confirme com OdDialogo. */
@Composable
fun OdLinhaAcao(titulo: String, aoClicar: () -> Unit, icone: ImageVector? = null, perigo: Boolean = false, descricao: String? = null) {
    OdLinhaConfig(titulo, Modifier.clickable(role = Role.Button, onClick = aoClicar), descricao, icone, perigo = perigo)
}

/** Opção de uma lista de escolha única (subtela ou folha): marca em primaria.texto na escolhida. */
@Composable
fun OdLinhaOpcao(titulo: String, selecionada: Boolean, aoClicar: () -> Unit, descricao: String? = null) {
    OdLinhaConfig(titulo, Modifier.selectable(selected = selecionada, role = Role.RadioButton, onClick = aoClicar), descricao) {
        Icon(OdIcones.Marca, null, Modifier.size(20.dp), tint = if (selecionada) OdTema.cores.primaria.texto else Color.Transparent)
    }
}

/** Linha com o controle embaixo do título (segmentado, amostras de cor). */
@Composable
fun OdLinhaEmpilhada(titulo: String, descricao: String? = null, icone: ImageVector? = null, controle: @Composable () -> Unit) {
    val c = OdTema.cores
    Column(Modifier.fillMaxWidth().background(c.superficie).padding(horizontal = OdEspacos.e4, vertical = OdEspacos.e3 * OdTema.densidade.fator)) {
        OdLinhaConfigCabecalho(titulo, descricao, icone)
        Box(Modifier.padding(start = if (icone != null) 32.dp else 0.dp, top = OdEspacos.e2)) { controle() }
    }
}

@Composable
private fun OdLinhaConfigCabecalho(titulo: String, descricao: String?, icone: ImageVector?) {
    val c = OdTema.cores
    Row(horizontalArrangement = Arrangement.spacedBy(OdEspacos.e3), verticalAlignment = Alignment.CenterVertically) {
        if (icone != null) Icon(icone, null, Modifier.size(20.dp), tint = c.texto2)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, style = OdTema.tipografia.uiMedio, color = c.texto)
            if (descricao != null) Text(descricao, style = OdTema.tipografia.uiPequeno, color = c.texto2)
        }
    }
}

/** Controle segmentado: 2 a 5 opções exclusivas e curtas. */
@Composable
fun <T> OdSegmentado(opcoes: List<Pair<T, String>>, selecionada: T, aoEscolher: (T) -> Unit, modifier: Modifier = Modifier) {
    val c = OdTema.cores
    val forma = RoundedCornerShape(OdRaios.controle)
    val formaItem = RoundedCornerShape(OdRaios.controle - 2.dp)
    Row(
        modifier.fillMaxWidth().clip(forma).background(c.superficieSutil).border(OdMedidas.bordaFina, c.borda, forma).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        opcoes.forEach { (valor, rotulo) ->
            val sel = valor == selecionada
            Box(
                Modifier.weight(1f)
                    .then(if (sel) Modifier.odSombra(OdTema.sombras.relevo, formaItem) else Modifier)
                    .clip(formaItem).background(if (sel) c.superficieElevada else Color.Transparent)
                    .selectable(selected = sel, role = Role.RadioButton) { aoEscolher(valor) }
                    .heightIn(min = maxOf(OdTema.densidade.alturaControle - 8.dp, 40.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(rotulo, style = OdTema.tipografia.uiMedio, color = if (sel) c.texto else c.texto2, maxLines = 1)
            }
        }
    }
}

/** Amostras das 10 cores (mais "Sistema" no Android 12+, se incluirSistema). */
@Composable
fun OdAmostrasCor(selecionada: OdCor?, aoEscolher: (OdCor) -> Unit, incluirSistema: Boolean = false, sistemaSelecionado: Boolean = false, aoEscolherSistema: () -> Unit = {}) {
    val c = OdTema.cores
    val contexto = LocalContext.current
    val itens: List<Pair<OdCor?, OdFamilia>> = OdCor.entries.map { it to c.familias[it.ordinal] } +
        if (incluirSistema && corDoSistemaDisponivel) listOfNotNull(familiaDoSistema(contexto, c.claro)?.let { null to it }) else emptyList()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        itens.chunked(5).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                linha.forEach { (cor, fam) ->
                    val sel = if (cor == null) sistemaSelecionado else (!sistemaSelecionado && cor == selecionada)
                    val forma = RoundedCornerShape(OdRaios.controle)
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).heightIn(min = 40.dp).clip(forma).background(fam.cor)
                            .border(2.dp, if (sel) c.texto else Color.Transparent, forma)
                            .selectable(selected = sel, role = Role.RadioButton) { if (cor == null) aoEscolherSistema() else aoEscolher(cor) }
                            .semantics { contentDescription = cor?.nome ?: "Cor do sistema" },
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            sel -> Icon(OdIcones.Marca, null, Modifier.size(16.dp), tint = fam.sobre)
                            cor == null -> Icon(OdIcones.Paleta, null, Modifier.size(16.dp), tint = fam.sobre)
                        }
                    }
                }
                repeat(5 - linha.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Resumo da aparência para a linha "Aparência": "Sistema, Laranja, Equilibrada". */
fun OdPreferencias.resumoAparencia(): String = listOf(
    when (modo) { OdModo.Sistema -> "Sistema"; OdModo.Escuro -> "Escuro"; OdModo.Claro -> "Claro" },
    if (corDoSistema && corDoSistemaDisponivel) "Cor do sistema" else primaria.nome,
    densidade.name,
).joinToString(", ")

/** Linha "Aparência", sempre a primeira da tela de configurações. */
@Composable
fun OdLinhaAparencia(preferencias: OdPreferencias, aoClicar: () -> Unit) {
    OdLinhaConfig(
        "Aparência", Modifier.clickable(role = Role.Button, onClick = aoClicar), preferencias.resumoAparencia(), OdIcones.Paleta,
    ) { Icon(OdIcones.Seta, null, Modifier.size(18.dp)) }
}

/**
 * Conteúdo da tela Aparência: o mesmo em todo app do Open Design, na mesma ordem.
 * Tema, cor principal (com "Sistema" no Android 12+), cor secundária, densidade, reduzir animações
 * e, no fim, restaurar o padrão (com Desfazer no toast, se houver toasts).
 * Use dentro de uma tela própria; o OdTelaConfiguracoes já faz isso.
 */
@Composable
fun OdConteudoAparencia(preferencias: OdPreferencias, aoMudar: (OdPreferencias) -> Unit, toasts: OdToasts? = null) {
    val p = preferencias
    Column(verticalArrangement = Arrangement.spacedBy(OdEspacos.e5)) {
        OdGrupoConfig(null) {
            OdLinhaEmpilhada("Tema", "Sistema segue o modo claro ou escuro do aparelho", OdIcones.Tema) {
                OdSegmentado(listOf(OdModo.Sistema to "Sistema", OdModo.Claro to "Claro", OdModo.Escuro to "Escuro"), p.modo, { aoMudar(p.copy(modo = it)) })
            }
            OdLinhaEmpilhada("Cor principal", "Botões principais, seleção e destaques", OdIcones.Paleta) {
                OdAmostrasCor(p.primaria, { aoMudar(p.copy(primaria = it, corDoSistema = false)) }, incluirSistema = true,
                    sistemaSelecionado = p.corDoSistema && corDoSistemaDisponivel, aoEscolherSistema = { aoMudar(p.copy(corDoSistema = true)) })
            }
            OdLinhaEmpilhada("Cor secundária", "Gráficos de categoria e destaques auxiliares", OdIcones.Paleta) {
                OdAmostrasCor(p.secundaria, { aoMudar(p.copy(secundaria = it)) })
            }
            OdLinhaEmpilhada("Densidade", "Espaço entre os elementos", OdIcones.Densidade) {
                OdSegmentado(OdDensidade.entries.map { it to it.name }, p.densidade, { aoMudar(p.copy(densidade = it)) })
            }
            OdLinhaInterruptor("Reduzir animações", p.reduzirAnimacoes, { aoMudar(p.copy(reduzirAnimacoes = it)) }, "Sem transições nem pulsos", OdIcones.Animacoes)
        }
        OdGrupoConfig(null) {
            OdLinhaAcao("Restaurar aparência padrão", {
                val antes = p
                aoMudar(OdPreferencias())
                toasts?.mostrar("Aparência restaurada.", acao = "Desfazer", aoAgir = { aoMudar(antes) }) // [Projeto Ferramentas] aoAgir nomeado
            }, OdIcones.Restaurar)
        }
    }
}

/**
 * Tela de configurações pronta, aberta pelo item "Mais". O primeiro item é sempre "Aparência" (igual em todo
 * app); depois vêm os grupos do projeto (opcoesDoApp) e, por fim, "Sobre". O botão Voltar do Android sai das subtelas.
 *
 *     OdTelaConfiguracoes(prefs, { prefs = it }, versao = "1.0.0", toasts = toasts) {
 *         OdGrupoConfig("Medição") { OdLinhaInterruptor("Manter tela ligada", …) }
 *     }
 */
@Composable
fun OdTelaConfiguracoes(
    preferencias: OdPreferencias,
    aoMudar: (OdPreferencias) -> Unit,
    modifier: Modifier = Modifier,
    versao: String? = null,
    toasts: OdToasts? = null,
    aoVoltar: (() -> Unit)? = null,
    aoAbrirLicencas: (() -> Unit)? = null,
    barraInferior: @Composable () -> Unit = {},
    opcoesDoApp: @Composable ColumnScope.() -> Unit = {},
) {
    var subtela by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(enabled = subtela != null) { subtela = null }
    val titulo = when (subtela) { "aparencia" -> "Aparência"; "sobre" -> "Sobre o app"; else -> "Configurações" }
    OdTelaApp(titulo, modifier, aoVoltar = if (subtela != null) ({ subtela = null }) else aoVoltar, toasts = toasts, barraInferior = barraInferior) { padding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)
                .padding(PaddingValues(horizontal = OdMedidas.gradeMargemCompacta, vertical = OdEspacos.e4)),
            verticalArrangement = Arrangement.spacedBy(OdEspacos.e5),
        ) {
            when (subtela) {
                "aparencia" -> OdConteudoAparencia(preferencias, aoMudar, toasts)
                "sobre" -> OdGrupoConfig(null) {
                    if (versao != null) OdLinhaConfig("Versão") { Text(versao, style = OdTema.tipografia.ui) }
                    if (aoAbrirLicencas != null) OdLinhaValor("Licenças de código aberto", null, aoAbrirLicencas)
                }
                else -> {
                    OdGrupoConfig(null) { OdLinhaAparencia(preferencias) { subtela = "aparencia" } }
                    Column(verticalArrangement = Arrangement.spacedBy(OdEspacos.e5), content = opcoesDoApp)
                    if (versao != null || aoAbrirLicencas != null) OdGrupoConfig(null) {
                        OdLinhaValor("Sobre o app", versao, { subtela = "sobre" }, icone = OdIcones.Info)
                    }
                }
            }
            Spacer(Modifier.width(1.dp))
        }
    }
}
