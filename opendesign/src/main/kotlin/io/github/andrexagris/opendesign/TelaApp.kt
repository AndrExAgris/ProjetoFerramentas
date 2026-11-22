package io.github.andrexagris.opendesign

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Estrutura de uma tela de app: barra superior, conteúdo e barra inferior fixa, com áreas seguras
 * (edge-to-edge). Os toasts aparecem acima da barra inferior.
 * Na Activity, chame enableEdgeToEdge() antes de setContent.
 *
 *     OdTelaApp(titulo = "Início", toasts = toasts, barraInferior = { OdBarraInferior(…) }) { padding -> … }
 */
@Composable
fun OdTelaApp(
    titulo: String,
    modifier: Modifier = Modifier,
    aoVoltar: (() -> Unit)? = null,
    toasts: OdToasts? = null,
    acoes: @Composable RowScope.() -> Unit = {},
    barraInferior: @Composable () -> Unit = {},
    conteudo: @Composable (PaddingValues) -> Unit,
) {
    val c = OdTema.cores
    Scaffold(
        modifier = modifier,
        topBar = { OdBarraSuperior(titulo, aoVoltar, acoes) },
        bottomBar = barraInferior,
        snackbarHost = { if (toasts != null) OdRegiaoToasts(toasts) },
        containerColor = c.fundo,
        contentColor = c.texto,
    ) { padding ->
        Box(Modifier.fillMaxSize()) { conteudo(padding) }
    }
}

/** Barra superior do app: voltar (subtelas), título e até duas ações em ícone à direita. */
@Composable
fun OdBarraSuperior(titulo: String, aoVoltar: (() -> Unit)? = null, acoes: @Composable RowScope.() -> Unit = {}) {
    val c = OdTema.cores
    Column(Modifier.fillMaxWidth().background(c.superficie).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            Modifier.fillMaxWidth().height(OdTema.densidade.alturaBarraSuperior)
                .padding(start = if (aoVoltar != null) 4.dp else OdMedidas.gradeMargemCompacta, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (aoVoltar != null) {
                OdBotaoIcone(OdIcones.Voltar, "Voltar", aoVoltar)
                Spacer(Modifier.width(4.dp))
            }
            Text(
                titulo, style = OdTema.tipografia.uiTitulo, color = c.texto, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            acoes()
        }
        HorizontalDivider(thickness = OdMedidas.bordaFina, color = c.borda)
    }
}

