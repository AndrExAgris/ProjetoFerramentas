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
import io.github.andrexagris.ferramentas.ui.EstadoApp
import io.github.andrexagris.ferramentas.ui.FtIcones
import io.github.andrexagris.ferramentas.ui.componentes.FtAviso
import io.github.andrexagris.ferramentas.ui.componentes.FtLinhaFerramenta
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdMedidas
import io.github.andrexagris.opendesign.OdTelaApp

/** Favoritos: as ferramentas fixadas, na ordem do catálogo. */
@Composable
fun TelaFavoritos(estado: EstadoApp, aoAbrirFerramenta: (Ferramenta) -> Unit, barraInferior: @Composable () -> Unit) {
    OdTelaApp("Favoritos", barraInferior = barraInferior) { padding ->
        val todas = Catalogo.ferramentas.filter { estado.favorita(it) }
        val visiveis = estado.visiveis(todas)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)
                .padding(horizontal = OdMedidas.gradeMargemCompacta, vertical = OdEspacos.e4),
            verticalArrangement = Arrangement.spacedBy(OdEspacos.e5),
        ) {
            when {
                todas.isEmpty() -> FtAviso(
                    FtIcones.Star, "Nenhum favorito ainda",
                    "Toque na estrela ao lado de uma ferramenta para fixá-la aqui.",
                )
                visiveis.isEmpty() -> FtAviso(
                    FtIcones.Filter, "Favoritos escondidos pelo filtro",
                    "Seus favoritos não funcionam neste aparelho e o filtro \"Só o que funciona aqui\" está ligado.",
                    acao = "Mostrar todas", aoAgir = { estado.definirSoDisponiveis(false) },
                )
                else -> OdGrupoConfig(null) {
                    visiveis.forEach { f ->
                        FtLinhaFerramenta(
                            f, estado.disponibilidade(f), favorita = true,
                            aoAbrir = { aoAbrirFerramenta(f) }, aoAlternarFavorito = { estado.alternarFavorito(f.id) }, comContexto = true,
                        )
                    }
                }
            }
        }
    }
}
