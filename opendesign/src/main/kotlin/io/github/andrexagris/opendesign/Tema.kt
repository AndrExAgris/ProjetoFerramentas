package io.github.andrexagris.opendesign

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Modo de cor. Sistema segue o claro ou escuro do aparelho. */
enum class OdModo(val id: String) {
    Sistema("sistema"), Escuro("escuro"), Claro("claro");

    companion object {
        fun deId(id: String?): OdModo? = entries.firstOrNull { it.id == id }
    }
}

/**
 * As escolhas de aparência que a pessoa faz nas configurações (as mesmas da web).
 * corDoSistema: no Android 12 ou mais novo, usa a cor de destaque do papel de parede (Material You) só como
 * cor principal. Desligado por padrão; neutros, estados, gráficos e escalas nunca mudam.
 */
@Immutable
data class OdPreferencias(
    val modo: OdModo = OdModo.Sistema,
    val primaria: OdCor = OdCor.Laranja,
    val secundaria: OdCor = OdCor.Teal,
    val densidade: OdDensidade = OdDensidade.Equilibrada,
    val reduzirAnimacoes: Boolean = false,
    val corDoSistema: Boolean = false,
)

val LocalOdCores = staticCompositionLocalOf { odCores(claro = false) }
val LocalOdTipografia = staticCompositionLocalOf { OdTipografia() }
val LocalOdSombras = staticCompositionLocalOf { odSombrasEscuro }
val LocalOdDensidade = staticCompositionLocalOf { OdDensidade.Equilibrada }
val LocalOdMovimentoReduzido = staticCompositionLocalOf { false }

/** Acesso ao tema atual: OdTema.cores.primaria.cor, OdTema.tipografia.ui, OdTema.densidade.fator… */
object OdTema {
    val cores: OdCores @Composable @ReadOnlyComposable get() = LocalOdCores.current
    val tipografia: OdTipografia @Composable @ReadOnlyComposable get() = LocalOdTipografia.current
    val sombras: OdSombras @Composable @ReadOnlyComposable get() = LocalOdSombras.current
    val densidade: OdDensidade @Composable @ReadOnlyComposable get() = LocalOdDensidade.current

    /** Verdadeiro quando a pessoa pediu menos animação no app ou desligou as animações do Android. */
    val movimentoReduzido: Boolean @Composable @ReadOnlyComposable get() = LocalOdMovimentoReduzido.current

    /** Duração em ms já ajustada para reduzir animações (0 quando reduzido). */
    @Composable @ReadOnlyComposable
    fun duracao(ms: Int): Int = if (LocalOdMovimentoReduzido.current) 0 else ms
}

/** Cor principal disponível a partir do sistema (Material You)? Só Android 12 (API 31) ou mais novo. */
val corDoSistemaDisponivel: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Raiz do tema. Envolva o conteúdo da Activity:
 *
 *     setContent { val prefs by rememberOdPreferencias(); OpenDesignTema(prefs) { App() } }
 *
 * Também configura o MaterialTheme por baixo, então componentes do Material 3 que você usar
 * direto (TextField, Slider, Checkbox…) já saem com as cores e fontes do Open Design.
 */
@Composable
fun OpenDesignTema(preferencias: OdPreferencias = OdPreferencias(), content: @Composable () -> Unit) {
    val contexto = LocalContext.current
    val claro = when (preferencias.modo) {
        OdModo.Sistema -> !isSystemInDarkTheme()
        OdModo.Escuro -> false
        OdModo.Claro -> true
    }
    val cores = remember(claro, preferencias.primaria, preferencias.secundaria, preferencias.corDoSistema) {
        val doSistema = if (preferencias.corDoSistema) familiaDoSistema(contexto, claro) else null
        odCores(claro, preferencias.primaria, preferencias.secundaria, primariaPersonalizada = doSistema)
    }
    val tipografia = remember { OdTipografia() }
    val reduzido = preferencias.reduzirAnimacoes || animacoesDoSistemaDesligadas(contexto)
    CompositionLocalProvider(
        LocalOdCores provides cores,
        LocalOdTipografia provides tipografia,
        LocalOdSombras provides if (claro) odSombrasClaro else odSombrasEscuro,
        LocalOdDensidade provides preferencias.densidade,
        LocalOdMovimentoReduzido provides reduzido,
    ) {
        MaterialTheme(
            colorScheme = remember(cores) { cores.paraMaterial() },
            typography = remember(tipografia) { tipografia.paraMaterial() },
            shapes = Shapes(
                extraSmall = RoundedCornerShape(OdRaios.indicador),
                small = RoundedCornerShape(OdRaios.controle),
                medium = RoundedCornerShape(OdRaios.painel),
                large = RoundedCornerShape(OdRaios.flutuante),
                extraLarge = RoundedCornerShape(OdRaios.flutuante),
            ),
        ) {
            CompositionLocalProvider(LocalContentColor provides cores.texto, content = content)
        }
    }
}

private fun animacoesDoSistemaDesligadas(contexto: Context): Boolean = try {
    Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
} catch (e: Exception) {
    false
}

/** Mapeia os papéis do Open Design para o ColorScheme do Material 3. */
fun OdCores.paraMaterial(): ColorScheme = (if (claro) lightColorScheme() else darkColorScheme()).copy(
    primary = primaria.cor,
    onPrimary = primaria.sobre,
    primaryContainer = primaria.fundo,
    onPrimaryContainer = texto,
    inversePrimary = primaria.texto,
    secondary = secundaria.cor,
    onSecondary = secundaria.sobre,
    secondaryContainer = primaria.fundo, // indicador da NavigationBar = item selecionado
    onSecondaryContainer = texto,
    tertiary = info.cor,
    onTertiary = info.sobre,
    tertiaryContainer = info.fundo,
    onTertiaryContainer = texto,
    background = fundo,
    onBackground = texto,
    surface = superficie,
    onSurface = texto,
    surfaceVariant = superficieSutil,
    onSurfaceVariant = texto2,
    surfaceTint = Color.Transparent,
    inverseSurface = texto,
    inverseOnSurface = fundo,
    error = erro.cor,
    onError = erro.sobre,
    errorContainer = erro.fundo,
    onErrorContainer = texto,
    outline = bordaControle,
    outlineVariant = borda,
    scrim = Color.Black,
    surfaceBright = superficieElevada,
    surfaceContainer = superficie,
    surfaceContainerHigh = superficieElevada,
    surfaceContainerHighest = superficieElevada,
    surfaceContainerLow = superficie,
    surfaceContainerLowest = fundo,
    surfaceDim = fundo,
)

/** Mapeia os estilos do Open Design para a Typography do Material 3. */
fun OdTipografia.paraMaterial() = Typography(
    displayLarge = editorialDestaque,
    displayMedium = editorialTitulo1,
    displaySmall = editorialTitulo2,
    headlineLarge = editorialTitulo1,
    headlineMedium = editorialTitulo2,
    headlineSmall = uiTituloPagina,
    titleLarge = uiTitulo,
    titleMedium = uiSubtitulo,
    titleSmall = uiMedio,
    bodyLarge = ui.copy(fontSize = uiSubtitulo.fontSize, lineHeight = uiSubtitulo.lineHeight),
    bodyMedium = ui,
    bodySmall = uiPequeno,
    labelLarge = uiMedio,
    labelMedium = uiPequeno.copy(fontWeight = uiMedio.fontWeight),
    labelSmall = uiPequeno,
)
