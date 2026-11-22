package io.github.andrexagris.ferramentas.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.andrexagris.ferramentas.ui.FtIcones
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdLinhaValor
import io.github.andrexagris.opendesign.OdMedidas
import io.github.andrexagris.opendesign.OdTelaApp
import io.github.andrexagris.opendesign.OdTema

/** Um componente de terceiros e o arquivo de licença dele em assets/licencas/. */
data class Licenca(val nome: String, val detalhe: String, val arquivo: String)

val licencaDoApp = Licenca("Projeto Ferramentas", "GNU GPL 3.0. Você pode usar, estudar, modificar e redistribuir.", "GPL-3.0.txt")

val licencasDeTerceiros = listOf(
    Licenca("Geist", "Fonte da interface. SIL Open Font License 1.1", "Geist-OFL.txt"),
    Licenca("Literata", "Fonte editorial. SIL Open Font License 1.1", "Literata-OFL.txt"),
    Licenca("JetBrains Mono", "Fonte de código e leituras. SIL Open Font License 1.1", "JetBrainsMono-OFL.txt"),
    Licenca("Tabler Icons 3.50.0", "Ícones. Licença MIT", "TablerIcons-MIT.txt"),
    Licenca("Android Jetpack e Kotlin", "Compose, Navigation, DataStore, Room, WorkManager, CameraX, Glance e Kotlin. Apache 2.0", "Apache-2.0.txt"),
    Licenca("ZXing", "Leitura e geração de códigos. Apache 2.0", "Apache-2.0.txt"),
    Licenca("Tesseract e Tesseract4Android", "Reconhecimento de texto (OCR). Apache 2.0", "Apache-2.0.txt"),
    Licenca("Modelo tessdata_fast (português)", "Dados de treino do Tesseract. Apache 2.0", "Tessdata-Apache-2.0.txt"),
    Licenca("dnsjava", "Consultas DNS. Licença BSD de 2 cláusulas", "dnsjava-BSD.txt"),
)

/** Lista das licenças: a do app e as de terceiros (fontes OFL, ícones MIT, bibliotecas Apache). */
@Composable
fun TelaLicencas(aoAbrir: (Licenca) -> Unit, aoVoltar: () -> Unit, barraInferior: @Composable () -> Unit) {
    OdTelaApp("Licenças de código aberto", aoVoltar = aoVoltar, barraInferior = barraInferior) { padding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)
                .padding(horizontal = OdMedidas.gradeMargemCompacta, vertical = OdEspacos.e4),
            verticalArrangement = Arrangement.spacedBy(OdEspacos.e5),
        ) {
            OdGrupoConfig("Este app") {
                OdLinhaValor(licencaDoApp.nome, null, { aoAbrir(licencaDoApp) }, licencaDoApp.detalhe, FtIcones.License)
            }
            OdGrupoConfig("Terceiros") {
                licencasDeTerceiros.forEach { l -> OdLinhaValor(l.nome, null, { aoAbrir(l) }, l.detalhe) }
            }
            Text(
                "O visual vem do Open Design, o design system do autor, copiado para o módulo opendesign do código-fonte.",
                style = OdTema.tipografia.uiPequeno, color = OdTema.cores.texto2, modifier = Modifier.padding(horizontal = OdEspacos.e1),
            )
        }
    }
}

/** Texto completo de uma licença, em fonte de código. */
@Composable
fun TelaTextoLicenca(licenca: Licenca, aoVoltar: () -> Unit) {
    val contexto = LocalContext.current
    val texto = remember(licenca.arquivo) {
        runCatching { contexto.assets.open("licencas/${licenca.arquivo}").bufferedReader().use { it.readText() } }
            .getOrElse { "Não foi possível abrir o texto da licença. Ele também está no código-fonte, em app/src/main/assets/licencas/${licenca.arquivo}." }
    }
    OdTelaApp(licenca.nome, aoVoltar = aoVoltar) { padding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)
                .background(OdTema.cores.superficieCodigo),
        ) {
            Text(
                texto, style = OdTema.tipografia.codigo, color = OdTema.cores.texto,
                modifier = Modifier.fillMaxWidth().padding(OdEspacos.e4),
            )
        }
    }
}
