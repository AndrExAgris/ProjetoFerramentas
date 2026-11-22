package io.github.andrexagris.ferramentas.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.andrexagris.ferramentas.base.PortaPermissoes
import io.github.andrexagris.ferramentas.catalogo.Disponibilidade
import io.github.andrexagris.ferramentas.catalogo.Ferramenta
import io.github.andrexagris.ferramentas.catalogo.SeusAparelhos
import io.github.andrexagris.ferramentas.ui.EstadoApp
import io.github.andrexagris.ferramentas.ui.FtIcones
import io.github.andrexagris.ferramentas.ui.componentes.FtAviso
import io.github.andrexagris.ferramentas.ui.componentes.FtBotaoFavorito
import io.github.andrexagris.ferramentas.ui.ferramentas.LocalToasts
import io.github.andrexagris.ferramentas.ui.ferramentas.RegistroTelas
import io.github.andrexagris.opendesign.OdBotaoIcone
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdFolhaInferior
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdMedidas
import io.github.andrexagris.opendesign.OdTelaApp
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.rememberOdToasts

/**
 * Tela de uma ferramenta. Indisponível: mostra o motivo em vez de abrir e falhar. Disponível: passa pela porta
 * de permissões e abre a tela registrada em RegistroTelas (ou "Em construção" se ainda não houver).
 * O botão de informação abre os detalhes do catálogo numa folha inferior.
 */
@Composable
fun TelaFerramenta(ferramenta: Ferramenta, estado: EstadoApp, aoVoltar: () -> Unit) {
    val f = ferramenta
    val toasts = rememberOdToasts()
    var detalhes by rememberSaveable { mutableStateOf(false) }
    val tela = RegistroTelas.de(f.id)
    OdTelaApp(
        f.nome, aoVoltar = aoVoltar, toasts = toasts,
        acoes = {
            OdBotaoIcone(OdIcones.Info, "Sobre a ferramenta", { detalhes = true })
            FtBotaoFavorito(estado.favorita(f), { estado.alternarFavorito(f.id) }, f.nome)
        },
    ) { padding ->
        CompositionLocalProvider(LocalToasts provides toasts) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (val d = estado.disponibilidade(f)) {
                    is Disponibilidade.Indisponivel -> Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = OdMedidas.gradeMargemCompacta)) {
                        OdEstadoRecurso(OdTipoEstado.Indisponivel, "Indisponível neste aparelho", d.motivo)
                        Detalhes(f)
                    }
                    Disponibilidade.Disponivel -> if (tela == null) {
                        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = OdMedidas.gradeMargemCompacta)) {
                            FtAviso(FtIcones.Tools, "Em construção", "Esta ferramenta ainda não foi feita. Quando ficar pronta, ela abre aqui.")
                            Detalhes(f)
                        }
                    } else {
                        val porta = tela.permissoes ?: f.permissoes
                        if (porta.isEmpty()) tela.conteudo()
                        else Column(Modifier.fillMaxSize()) {
                            PortaPermissoes(porta, tela.motivo) { tela.conteudo() }
                        }
                    }
                }
            }
        }
    }
    if (detalhes) OdFolhaInferior(aoFechar = { detalhes = false }, titulo = f.nome, descricao = f.descricao) {
        Column(Modifier.verticalScroll(rememberScrollState())) { Detalhes(f) }
    }
}

@Composable
private fun Detalhes(f: Ferramenta) {
    Column(Modifier.padding(vertical = OdEspacos.e4), verticalArrangement = Arrangement.spacedBy(OdEspacos.e5)) {
        if (f.usaInternet) OdGrupoConfig("Rede") {
            OdLinhaConfig("Usa a internet", descricao = "Esta ferramenta se conecta à internet para funcionar.", icone = FtIcones.World)
        }
        OdGrupoConfig("Sobre a ferramenta") {
            OdLinhaConfig("Grupo", descricao = "${f.grupo.nome} · ${f.secao}")
            OdLinhaConfig("Nos aparelhos-alvo", descricao = textoAparelhos(f.aparelhos))
            OdLinhaConfig("API ou sensor", descricao = f.api)
            if (f.observacao != null) OdLinhaConfig("Observação", descricao = f.observacao, icone = OdIcones.Info)
        }
        if (f.permissoes.isNotEmpty()) OdGrupoConfig("Permissões") {
            f.permissoes.forEach { p -> OdLinhaConfig(p.nome, descricao = p.como.rotulo, icone = OdIcones.Cadeado) }
        }
    }
}

private fun textoAparelhos(a: SeusAparelhos): String = when (a) {
    SeusAparelhos.OsDois -> "Roda no Galaxy S20 FE e no Galaxy A24."
    SeusAparelhos.OsDoisLimitado -> "Roda nos dois, com limitações."
    SeusAparelhos.SoA24 -> "Só no Galaxy A24 (precisa do Android 14 ou mais novo)."
    SeusAparelhos.Verificar -> "Precisa ser testada nos aparelhos."
    SeusAparelhos.AparelhoFuturo -> "Nenhum dos dois tem o hardware. Fica para aparelhos futuros."
}
