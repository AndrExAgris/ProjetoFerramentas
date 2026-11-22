package io.github.andrexagris.ferramentas.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.andrexagris.ferramentas.catalogo.Catalogo
import io.github.andrexagris.ferramentas.catalogo.Ferramenta
import io.github.andrexagris.ferramentas.ui.EstadoApp
import io.github.andrexagris.ferramentas.ui.FtIcones
import io.github.andrexagris.ferramentas.ui.componentes.FtAviso
import io.github.andrexagris.ferramentas.ui.componentes.FtLinhaFerramenta
import io.github.andrexagris.ferramentas.ui.componentes.quantidadeFerramentas
import io.github.andrexagris.opendesign.OdBotaoIcone
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdMedidas
import io.github.andrexagris.opendesign.OdRaios
import io.github.andrexagris.opendesign.OdTelaApp
import io.github.andrexagris.opendesign.OdTema
import java.text.Normalizer

/** Tira acentos, caixa e hífens: "Bússola" e "bussola", "Wi-Fi" e "wifi" dão no mesmo. */
fun normalizarBusca(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").replace("-", "").lowercase().trim()

/**
 * Busca no catálogo inteiro. Cada palavra digitada precisa aparecer no nome, na descrição, no grupo ou na
 * seção; as que batem no nome vêm primeiro.
 */
fun buscar(consulta: String): List<Ferramenta> {
    val palavras = normalizarBusca(consulta).split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (palavras.isEmpty()) return emptyList()
    return indice
        .filter { item -> palavras.all { it in item.tudo } }
        .sortedBy { item -> if (palavras.all { it in item.nome }) 0 else 1 }
        .map { it.ferramenta }
}

private class ItemIndice(val ferramenta: Ferramenta, val nome: String, val tudo: String)

private val indice: List<ItemIndice> by lazy {
    Catalogo.ferramentas.map { f ->
        ItemIndice(f, normalizarBusca(f.nome), normalizarBusca(listOfNotNull(f.nome, f.descricao, f.grupo.nome, f.secao).joinToString(" ")))
    }
}

@Composable
fun TelaBuscar(estado: EstadoApp, aoAbrirFerramenta: (Ferramenta) -> Unit, barraInferior: @Composable () -> Unit) {
    var consulta by rememberSaveable { mutableStateOf("") }
    val foco = LocalFocusManager.current
    OdTelaApp("Buscar", barraInferior = barraInferior) { padding ->
        val encontradas = remember(consulta) { buscar(consulta) }
        val visiveis = estado.visiveis(encontradas)
        val escondidas = encontradas.size - visiveis.size
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)
                .padding(horizontal = OdMedidas.gradeMargemCompacta, vertical = OdEspacos.e4),
            verticalArrangement = Arrangement.spacedBy(OdEspacos.e4),
        ) {
            OutlinedTextField(
                value = consulta,
                onValueChange = { consulta = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nome da ferramenta") },
                leadingIcon = { Icon(FtIcones.Search, null, Modifier.size(20.dp)) },
                trailingIcon = if (consulta.isNotEmpty()) ({ OdBotaoIcone(OdIcones.Fechar, "Limpar busca", { consulta = "" }) }) else null,
                singleLine = true,
                shape = RoundedCornerShape(OdRaios.controle),
                textStyle = OdTema.tipografia.ui.copy(fontSize = OdTema.tipografia.uiSubtitulo.fontSize),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { foco.clearFocus() }),
            )
            when {
                consulta.isBlank() -> FtAviso(
                    FtIcones.Search, "Buscar ferramentas",
                    "Digite parte do nome para procurar entre as ${Catalogo.ferramentas.size} ferramentas de todos os grupos.",
                )
                encontradas.isEmpty() -> FtAviso(
                    FtIcones.SearchOff, "Nada encontrado para \"${consulta.trim()}\"",
                    "Confira a grafia ou tente uma palavra mais curta, como \"wifi\" ou \"bateria\".",
                )
                visiveis.isEmpty() -> FtAviso(
                    FtIcones.Filter, "Resultados escondidos pelo filtro",
                    "${quantidadeFerramentas(escondidas)} não funcionam neste aparelho e o filtro \"Só o que funciona aqui\" está ligado.",
                    acao = "Mostrar todas", aoAgir = { estado.definirSoDisponiveis(false) },
                )
                else -> {
                    val resumo = quantidadeFerramentas(visiveis.size) + if (escondidas > 0) " ($escondidas escondidas pelo filtro)" else ""
                    OdGrupoConfig(resumo) {
                        visiveis.forEach { f ->
                            FtLinhaFerramenta(
                                f, estado.disponibilidade(f), estado.favorita(f),
                                aoAbrir = { aoAbrirFerramenta(f) }, aoAlternarFavorito = { estado.alternarFavorito(f.id) }, comContexto = true,
                            )
                        }
                    }
                }
            }
        }
    }
}
