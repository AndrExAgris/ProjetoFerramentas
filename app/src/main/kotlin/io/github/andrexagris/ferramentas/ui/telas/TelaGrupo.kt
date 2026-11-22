package io.github.andrexagris.ferramentas.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.andrexagris.ferramentas.catalogo.Catalogo
import io.github.andrexagris.ferramentas.catalogo.Ferramenta
import io.github.andrexagris.ferramentas.catalogo.Grupo
import io.github.andrexagris.ferramentas.ui.EstadoApp
import io.github.andrexagris.ferramentas.ui.componentes.FtAviso
import io.github.andrexagris.ferramentas.ui.componentes.FtLinhaFerramenta
import io.github.andrexagris.ferramentas.ui.icone
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdMedidas
import io.github.andrexagris.opendesign.OdTelaApp

/** Ferramentas de um grupo, divididas por seção. */
@Composable
fun TelaGrupo(
    grupo: Grupo,
    estado: EstadoApp,
    aoAbrirFerramenta: (Ferramenta) -> Unit,
    aoVoltar: () -> Unit,
    barraInferior: @Composable () -> Unit,
) {
    OdTelaApp(grupo.nome, aoVoltar = aoVoltar, barraInferior = barraInferior) { padding ->
        val visiveis = estado.visiveis(Catalogo.doGrupo(grupo))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)
                .padding(horizontal = OdMedidas.gradeMargemCompacta, vertical = OdEspacos.e4),
            verticalArrangement = Arrangement.spacedBy(OdEspacos.e5),
        ) {
            if (visiveis.isEmpty()) {
                FtAviso(
                    grupo.icone, "Nenhuma ferramenta funciona aqui",
                    "Todas as ferramentas de ${grupo.nome} precisam de algo que este aparelho não tem. O filtro \"Só o que funciona aqui\" está escondendo todas.",
                    acao = "Mostrar todas", aoAgir = { estado.definirSoDisponiveis(false) },
                )
            }
            grupo.secoes.forEach { secao ->
                val daSecao = visiveis.filter { it.secao == secao }
                if (daSecao.isNotEmpty()) OdGrupoConfig(secao) {
                    daSecao.forEach { f ->
                        FtLinhaFerramenta(
                            f, estado.disponibilidade(f), estado.favorita(f),
                            aoAbrir = { aoAbrirFerramenta(f) }, aoAlternarFavorito = { estado.alternarFavorito(f.id) },
                        )
                    }
                }
            }
        }
    }
}
