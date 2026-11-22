package io.github.andrexagris.ferramentas.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.andrexagris.ferramentas.catalogo.Disponibilidade
import io.github.andrexagris.ferramentas.catalogo.Ferramenta
import io.github.andrexagris.ferramentas.catalogo.SeusAparelhos
import io.github.andrexagris.ferramentas.ui.EstadoApp
import io.github.andrexagris.ferramentas.ui.FtIcones
import io.github.andrexagris.ferramentas.ui.componentes.FtAviso
import io.github.andrexagris.ferramentas.ui.componentes.FtBotaoFavorito
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdMedidas
import io.github.andrexagris.opendesign.OdTelaApp
import io.github.andrexagris.opendesign.OdTipoEstado

/**
 * Tela de uma ferramenta. Indisponível: mostra o motivo em vez de abrir e falhar.
 * Disponível: por enquanto todas abrem "Em construção"; cada ferramenta pronta troca este conteúdo pela própria tela.
 */
@Composable
fun TelaFerramenta(ferramenta: Ferramenta, estado: EstadoApp, aoVoltar: () -> Unit) {
    val f = ferramenta
    OdTelaApp(
        f.nome, aoVoltar = aoVoltar,
        acoes = { FtBotaoFavorito(estado.favorita(f), { estado.alternarFavorito(f.id) }, f.nome) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)
                .padding(horizontal = OdMedidas.gradeMargemCompacta, vertical = OdEspacos.e4),
            verticalArrangement = Arrangement.spacedBy(OdEspacos.e5),
        ) {
            when (val d = estado.disponibilidade(f)) {
                is Disponibilidade.Indisponivel -> OdEstadoRecurso(OdTipoEstado.Indisponivel, "Indisponível neste aparelho", d.motivo)
                Disponibilidade.Disponivel -> FtAviso(
                    FtIcones.Tools, "Em construção",
                    "Esta ferramenta ainda não foi feita. Quando ficar pronta, ela abre aqui.",
                )
            }
            if (f.usaInternet) OdGrupoConfig("Rede") {
                OdLinhaConfig("Usa a internet", descricao = "Esta ferramenta se conecta à internet para funcionar.", icone = FtIcones.World)
            }
            OdGrupoConfig("Sobre a ferramenta") {
                if (f.descricao != null) OdLinhaConfig("O que mostra", descricao = f.descricao)
                OdLinhaConfig("Grupo", descricao = "${f.grupo.nome} · ${f.secao}")
                OdLinhaConfig("Nos aparelhos-alvo", descricao = textoAparelhos(f.aparelhos))
                OdLinhaConfig("Dificuldade", descricao = f.dificuldade.rotulo)
                OdLinhaConfig("API ou sensor", descricao = f.api)
                if (f.observacao != null) OdLinhaConfig("Observação", descricao = f.observacao, icone = OdIcones.Info)
            }
            if (f.permissoes.isNotEmpty()) OdGrupoConfig("Permissões") {
                f.permissoes.forEach { p -> OdLinhaConfig(p.nome, descricao = p.como.rotulo, icone = OdIcones.Cadeado) }
            }
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
