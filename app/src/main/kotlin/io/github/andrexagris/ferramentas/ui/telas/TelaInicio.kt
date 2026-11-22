package io.github.andrexagris.ferramentas.ui.telas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.andrexagris.ferramentas.catalogo.Catalogo
import io.github.andrexagris.ferramentas.catalogo.Grupo
import io.github.andrexagris.ferramentas.ui.EstadoApp
import io.github.andrexagris.ferramentas.ui.componentes.FtCartaoGrupo
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdGrade
import io.github.andrexagris.opendesign.OdTelaApp

/** Início: grade com os 13 grupos, cada um com ícone, nome e quantidade de ferramentas. */
@Composable
fun TelaInicio(estado: EstadoApp, aoAbrirGrupo: (Grupo) -> Unit, barraInferior: @Composable () -> Unit) {
    OdTelaApp("Ferramentas", barraInferior = barraInferior) { padding ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(vertical = OdEspacos.e4)) {
            OdGrade {
                Grupo.entries.forEach { g ->
                    val todas = Catalogo.doGrupo(g)
                    val visiveis = estado.visiveis(todas)
                    celula(compacta = 2, media = 2, expandida = 3) {
                        FtCartaoGrupo(
                            grupo = g,
                            total = visiveis.size,
                            indisponiveis = if (estado.soDisponiveis) 0 else estado.indisponiveis(todas),
                            aoClicar = { aoAbrirGrupo(g) },
                        )
                    }
                }
            }
        }
    }
}
