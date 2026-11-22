package io.github.andrexagris.ferramentas.ui.ferramentas

import androidx.compose.runtime.Composable
import io.github.andrexagris.ferramentas.catalogo.Permissao
import io.github.andrexagris.ferramentas.ui.ferramentas.ambiente.telasAmbiente
import io.github.andrexagris.ferramentas.ui.ferramentas.aparelho.telasAparelho
import io.github.andrexagris.ferramentas.ui.ferramentas.bateria.telasBateria
import io.github.andrexagris.ferramentas.ui.ferramentas.camera.telasCamera
import io.github.andrexagris.ferramentas.ui.ferramentas.internet.telasInternet
import io.github.andrexagris.ferramentas.ui.ferramentas.localizacao.telasLocalizacao
import io.github.andrexagris.ferramentas.ui.ferramentas.medicao.telasMedicao
import io.github.andrexagris.ferramentas.ui.ferramentas.movimento.telasMovimento
import io.github.andrexagris.ferramentas.ui.ferramentas.nfc.telasNfc
import io.github.andrexagris.ferramentas.ui.ferramentas.saude.telasSaude
import io.github.andrexagris.ferramentas.ui.ferramentas.sinais.telasSinais
import io.github.andrexagris.ferramentas.ui.ferramentas.som.telasSom
import io.github.andrexagris.ferramentas.ui.ferramentas.utilidades.telasUtilidades

/**
 * A tela de uma ferramenta.
 * permissoes: as que precisam estar concedidas antes de abrir (null = todas as do catálogo). Permissões
 * opcionais (ex.: notificações de um alarme) ficam de fora e a própria tela pede quando precisar.
 */
class TelaDeFerramenta(
    val permissoes: List<Permissao>? = null,
    val motivo: String? = null,
    val conteudo: @Composable () -> Unit,
)

/** Liga o id do catálogo à tela. Ferramenta nova: crie a tela no pacote do grupo e registre no mapa do grupo. */
object RegistroTelas {
    private val telas: Map<String, TelaDeFerramenta> by lazy {
        telasBateria() + telasInternet() + telasSinais() + telasNfc() + telasLocalizacao() + telasMedicao() +
            telasMovimento() + telasAmbiente() + telasSom() + telasCamera() + telasSaude() + telasAparelho() + telasUtilidades()
    }

    fun de(id: String): TelaDeFerramenta? = telas[id]

    val ids: Set<String> get() = telas.keys
}

/** Atalho para registrar: "id" com { Tela() }. */
infix fun String.com(conteudo: @Composable () -> Unit): Pair<String, TelaDeFerramenta> = this to TelaDeFerramenta(conteudo = conteudo)

fun String.com(permissoes: List<Permissao>, conteudo: @Composable () -> Unit): Pair<String, TelaDeFerramenta> =
    this to TelaDeFerramenta(permissoes, conteudo = conteudo)
