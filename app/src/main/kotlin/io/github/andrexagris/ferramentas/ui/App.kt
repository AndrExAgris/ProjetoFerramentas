package io.github.andrexagris.ferramentas.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import io.github.andrexagris.ferramentas.BuildConfig
import io.github.andrexagris.ferramentas.catalogo.Catalogo
import io.github.andrexagris.ferramentas.catalogo.Grupo
import io.github.andrexagris.ferramentas.ui.telas.TelaBuscar
import io.github.andrexagris.ferramentas.ui.telas.TelaFavoritos
import io.github.andrexagris.ferramentas.ui.telas.TelaFerramenta
import io.github.andrexagris.ferramentas.ui.telas.TelaGrupo
import io.github.andrexagris.ferramentas.ui.telas.TelaInicio
import io.github.andrexagris.ferramentas.ui.telas.TelaLicencas
import io.github.andrexagris.ferramentas.ui.telas.TelaTextoLicenca
import io.github.andrexagris.ferramentas.ui.telas.licencaDoApp
import io.github.andrexagris.ferramentas.ui.telas.licencasDeTerceiros
import io.github.andrexagris.opendesign.OdBarraInferior
import io.github.andrexagris.opendesign.OdDestino
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLinhaInterruptor
import io.github.andrexagris.opendesign.OdMovimento
import io.github.andrexagris.opendesign.OdPreferencias
import io.github.andrexagris.opendesign.OdTelaConfiguracoes
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.rememberOdToasts

/**
 * As quatro abas da barra inferior. Cada aba é um grafo próprio, então cada uma guarda a sua pilha
 * (Início → grupo → ferramenta) quando você troca de aba, e o Voltar do Android funciona em cada passo.
 */
private enum class Aba(val rota: String, val raiz: String, val destino: OdDestino) {
    Inicio("aba-inicio", "inicio", OdDestino("inicio", "Início", OdIcones.Inicio, OdIcones.InicioCheio)),
    Favoritos("aba-favoritos", "favoritos", OdDestino("favoritos", "Favoritos", FtIcones.Star, FtIcones.StarFilled)),
    Buscar("aba-buscar", "buscar", OdDestino("buscar", "Buscar", FtIcones.Search)),
    Mais("aba-mais", "mais", OdDestino("mais", "Mais", OdIcones.Mais, OdIcones.MaisCheio)),
}

private fun rotaGrupo(aba: Aba, id: String = "{id}") = "${aba.raiz}/grupo/$id"
private fun rotaFerramenta(aba: Aba, id: String = "{id}") = "${aba.raiz}/ferramenta/$id"
private const val ROTA_LICENCAS = "mais/licencas"
private fun rotaTextoLicenca(arquivo: String = "{arquivo}") = "mais/licencas/$arquivo"

@Composable
fun App(prefs: OdPreferencias, ferramentaPedida: String? = null, aoAbrirPedida: () -> Unit = {}, aoMudarPrefs: (OdPreferencias) -> Unit) {
    val nav = rememberNavController()
    val estado = rememberEstadoApp()
    val toasts = rememberOdToasts()
    val entrada by nav.currentBackStackEntryAsState()
    val abaAtual = Aba.entries.firstOrNull { aba -> entrada?.destination?.hierarchy?.any { it.route == aba.rota } == true } ?: Aba.Inicio

    val barra: @Composable () -> Unit = {
        OdBarraInferior(Aba.entries.map { it.destino }, abaAtual.destino.id, { id -> nav.irParaAba(Aba.entries.first { it.destino.id == id }, abaAtual) })
    }

    LaunchedEffect(ferramentaPedida) {
        val id = ferramentaPedida ?: return@LaunchedEffect
        if (Catalogo.porId(id) != null) nav.navigate(rotaFerramenta(Aba.Inicio, id)) { popUpTo(Aba.Inicio.raiz) }
        aoAbrirPedida()
    }

    val duracao = OdTema.duracao(OdMovimento.duracao)
    NavHost(
        nav, startDestination = Aba.Inicio.rota,
        modifier = Modifier.background(OdTema.cores.fundo),
        enterTransition = { if (duracao == 0) EnterTransition.None else fadeIn(tween(duracao)) },
        exitTransition = { if (duracao == 0) ExitTransition.None else fadeOut(tween(duracao)) },
    ) {
        navigation(startDestination = Aba.Inicio.raiz, route = Aba.Inicio.rota) {
            composable(Aba.Inicio.raiz) {
                TelaInicio(estado, aoAbrirGrupo = { nav.navigate(rotaGrupo(Aba.Inicio, it.id)) }, barraInferior = barra)
            }
            composable(rotaGrupo(Aba.Inicio)) { e ->
                val grupo = Grupo.deId(e.argumento("id")) ?: return@composable VoltarSeInvalido(nav)
                TelaGrupo(grupo, estado, aoAbrirFerramenta = { nav.navigate(rotaFerramenta(Aba.Inicio, it.id)) }, aoVoltar = { nav.navigateUp() }, barraInferior = barra)
            }
            telaFerramenta(Aba.Inicio, nav, estado)
        }
        navigation(startDestination = Aba.Favoritos.raiz, route = Aba.Favoritos.rota) {
            composable(Aba.Favoritos.raiz) {
                TelaFavoritos(estado, aoAbrirFerramenta = { nav.navigate(rotaFerramenta(Aba.Favoritos, it.id)) }, barraInferior = barra)
            }
            telaFerramenta(Aba.Favoritos, nav, estado)
        }
        navigation(startDestination = Aba.Buscar.raiz, route = Aba.Buscar.rota) {
            composable(Aba.Buscar.raiz) {
                TelaBuscar(estado, aoAbrirFerramenta = { nav.navigate(rotaFerramenta(Aba.Buscar, it.id)) }, barraInferior = barra)
            }
            telaFerramenta(Aba.Buscar, nav, estado)
        }
        navigation(startDestination = Aba.Mais.raiz, route = Aba.Mais.rota) {
            composable(Aba.Mais.raiz) {
                OdTelaConfiguracoes(
                    prefs, aoMudarPrefs, versao = BuildConfig.VERSION_NAME, toasts = toasts,
                    aoAbrirLicencas = { nav.navigate(ROTA_LICENCAS) }, barraInferior = barra,
                ) {
                    OdGrupoConfig("Ferramentas") {
                        OdLinhaInterruptor(
                            "Só o que funciona aqui", estado.soDisponiveis, estado.definirSoDisponiveis,
                            "Esconde as ferramentas indisponíveis neste aparelho", FtIcones.Filter,
                        )
                    }
                }
            }
            composable(ROTA_LICENCAS) {
                TelaLicencas(aoAbrir = { nav.navigate(rotaTextoLicenca(it.arquivo)) }, aoVoltar = { nav.navigateUp() }, barraInferior = barra)
            }
            composable(rotaTextoLicenca()) { e ->
                val arquivo = e.argumento("arquivo")
                val licenca = (listOf(licencaDoApp) + licencasDeTerceiros).firstOrNull { it.arquivo == arquivo } ?: return@composable VoltarSeInvalido(nav)
                TelaTextoLicenca(licenca, aoVoltar = { nav.navigateUp() })
            }
        }
    }
}

/** A tela da ferramenta existe em cada aba que leva a ela, para não misturar as pilhas. Sem barra inferior: a ferramenta usa a tela toda. */
private fun NavGraphBuilder.telaFerramenta(aba: Aba, nav: NavHostController, estado: EstadoApp) {
    composable(rotaFerramenta(aba)) { e ->
        val ferramenta = Catalogo.porId(e.argumento("id")) ?: return@composable VoltarSeInvalido(nav)
        TelaFerramenta(ferramenta, estado, aoVoltar = { nav.navigateUp() })
    }
}

/**
 * Troca de aba guardando a pilha da anterior. Tocar na aba atual volta para a raiz dela.
 */
private fun NavHostController.irParaAba(aba: Aba, atual: Aba) {
    if (aba == atual) {
        popBackStack(aba.raiz, inclusive = false)
        return
    }
    navigate(aba.rota) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavBackStackEntry.argumento(nome: String): String? = arguments?.getString(nome)

/** Rota com id que não existe (link antigo, por exemplo): volta em vez de mostrar tela vazia. */
@Composable
private fun VoltarSeInvalido(nav: NavHostController) {
    LaunchedEffect(Unit) { nav.popBackStack() }
}
