package io.github.andrexagris.ferramentas.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.andrexagris.ferramentas.FerramentasApp
import io.github.andrexagris.ferramentas.catalogo.Disponibilidade
import io.github.andrexagris.ferramentas.catalogo.Ferramenta
import io.github.andrexagris.ferramentas.catalogo.Grupo
import io.github.andrexagris.ferramentas.catalogo.disponivel
import io.github.andrexagris.opendesign.OdIcones
import kotlinx.coroutines.launch

/** Estado que as telas compartilham: disponibilidade no aparelho, favoritos e o filtro "só o que funciona aqui". */
@Stable
class EstadoApp(
    private val disponibilidades: Map<String, Disponibilidade>,
    val favoritos: Set<String>,
    val soDisponiveis: Boolean,
    val alternarFavorito: (String) -> Unit,
    val definirSoDisponiveis: (Boolean) -> Unit,
) {
    fun disponibilidade(f: Ferramenta): Disponibilidade = disponibilidades[f.id] ?: Disponibilidade.Disponivel

    fun favorita(f: Ferramenta): Boolean = f.id in favoritos

    /** Aplica o filtro: com "só o que funciona aqui" ligado, tira as indisponíveis. */
    fun visiveis(lista: List<Ferramenta>): List<Ferramenta> = if (soDisponiveis) lista.filter { disponibilidade(it).disponivel } else lista

    fun indisponiveis(lista: List<Ferramenta>): Int = lista.count { !disponibilidade(it).disponivel }
}

@Composable
fun rememberEstadoApp(): EstadoApp {
    val app = LocalContext.current.applicationContext as FerramentasApp
    val escopo = rememberCoroutineScope()
    val favoritos by app.preferencias.favoritos.collectAsStateWithLifecycle(emptySet())
    val soDisponiveis by app.preferencias.soDisponiveis.collectAsStateWithLifecycle(false)
    return remember(favoritos, soDisponiveis) {
        EstadoApp(
            app.disponibilidade, favoritos, soDisponiveis,
            alternarFavorito = { id -> escopo.launch { app.preferencias.alternarFavorito(id) } },
            definirSoDisponiveis = { v -> escopo.launch { app.preferencias.definirSoDisponiveis(v) } },
        )
    }
}

/** Ícone de cada grupo na grade do Início. */
val Grupo.icone: ImageVector
    get() = when (this) {
        Grupo.Bateria -> OdIcones.Bateria
        Grupo.Internet -> FtIcones.World
        Grupo.Sinais -> FtIcones.AntennaBars5
        Grupo.Nfc -> FtIcones.Nfc
        Grupo.Localizacao -> FtIcones.MapPin
        Grupo.Medicao -> FtIcones.RulerMeasure
        Grupo.Movimento -> FtIcones.ArrowsMove
        Grupo.Ambiente -> FtIcones.SunWind
        Grupo.Som -> FtIcones.Waveform
        Grupo.Camera -> FtIcones.Camera
        Grupo.Saude -> FtIcones.Heart
        Grupo.Aparelho -> FtIcones.DeviceMobile
        Grupo.Utilidades -> FtIcones.Tools
    }
