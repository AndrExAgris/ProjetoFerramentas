// Gerado por ferramentas/gerar_compose.py a partir de tokens-resolvidos.json. Não edite à mão.
package io.github.andrexagris.opendesign

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Fontes empacotadas em res/font (cópias estáticas da Geist, Literata e JetBrains Mono, licença OFL). */
object OdFontes {
    val interface_ = FontFamily(
        Font(R.font.geist_regular, FontWeight.Normal),
        Font(R.font.geist_medium, FontWeight.Medium),
        Font(R.font.geist_semibold, FontWeight.SemiBold),
    )
    val editorial = FontFamily(
        Font(R.font.literata_regular, FontWeight.Normal),
        Font(R.font.literata_semibold, FontWeight.SemiBold),
        Font(R.font.literata_italic, FontWeight.Normal, FontStyle.Italic),
    )
    val codigo = FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold),
    )
}

private val alinhamento = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)

/** Estilos de texto do Open Design. Os de leitura usam números tabulares (fontFeatureSettings = "tnum"). */
@Immutable
data class OdTipografia(
    /** Título da página na barra superior. Um por tela. Proposta. */
    val uiTituloPagina: TextStyle = TextStyle(
        fontFamily = OdFontes.interface_,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight(600),
        letterSpacing = (-0.01).em,
        lineHeightStyle = alinhamento,
    ),
    /** Título de seção, painel ou diálogo. Proposta. */
    val uiTitulo: TextStyle = TextStyle(
        fontFamily = OdFontes.interface_,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight(600),
        lineHeightStyle = alinhamento,
    ),
    /** Título de grupo, cabeçalho de card funcional. Proposta. */
    val uiSubtitulo: TextStyle = TextStyle(
        fontFamily = OdFontes.interface_,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight(600),
        lineHeightStyle = alinhamento,
    ),
    /** Corpo da interface: listas, tabelas, descrições curtas. Proposta. */
    val ui: TextStyle = TextStyle(
        fontFamily = OdFontes.interface_,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(400),
        lineHeightStyle = alinhamento,
    ),
    /** Botões, rótulos de campo, itens de navegação. Proposta. */
    val uiMedio: TextStyle = TextStyle(
        fontFamily = OdFontes.interface_,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(500),
        lineHeightStyle = alinhamento,
    ),
    /** Ajuda de campo, metadados, etiquetas, dicas. Proposta. */
    val uiPequeno: TextStyle = TextStyle(
        fontFamily = OdFontes.interface_,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight(400),
        lineHeightStyle = alinhamento,
    ),
    /** Título de abertura de artigo ou página de apresentação. Uma vez. Proposta. */
    val editorialDestaque: TextStyle = TextStyle(
        fontFamily = OdFontes.editorial,
        fontSize = 48.sp,
        lineHeight = 54.sp,
        fontWeight = FontWeight(600),
        letterSpacing = (-0.015).em,
        lineHeightStyle = alinhamento,
    ),
    /** h1 de artigo e documentação. Proposta. */
    val editorialTitulo1: TextStyle = TextStyle(
        fontFamily = OdFontes.editorial,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight(600),
        lineHeightStyle = alinhamento,
    ),
    /** h2 de artigo. Proposta. */
    val editorialTitulo2: TextStyle = TextStyle(
        fontFamily = OdFontes.editorial,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight(600),
        lineHeightStyle = alinhamento,
    ),
    /** h3 de artigo. Proposta. */
    val editorialTitulo3: TextStyle = TextStyle(
        fontFamily = OdFontes.editorial,
        fontSize = 21.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight(600),
        lineHeightStyle = alinhamento,
    ),
    /** Parágrafos de leitura longa, largura de 60 a 75 caracteres. Proposta. */
    val editorialLeitura: TextStyle = TextStyle(
        fontFamily = OdFontes.editorial,
        fontSize = 18.sp,
        lineHeight = 29.sp,
        fontWeight = FontWeight(400),
        lineHeightStyle = alinhamento,
    ),
    /** Valor principal de um instrumento (uma leitura por tela). Números tabulares: os dígitos têm a mesma largura e o valor não pula quando muda. */
    val leituraDestaque: TextStyle = TextStyle(
        fontFamily = OdFontes.interface_,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight(600),
        letterSpacing = (-0.01).em,
        fontFeatureSettings = "tnum",
        lineHeightStyle = alinhamento,
    ),
    /** Valor de card de leitura ao vivo (bateria, rede, sensor). */
    val leitura: TextStyle = TextStyle(
        fontFamily = OdFontes.interface_,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight(600),
        fontFeatureSettings = "tnum",
        lineHeightStyle = alinhamento,
    ),
    /** Valores em listas, tabelas e eixos de gráfico. */
    val leituraPequena: TextStyle = TextStyle(
        fontFamily = OdFontes.interface_,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(500),
        fontFeatureSettings = "tnum",
        lineHeightStyle = alinhamento,
    ),
    /** Blocos de código, terminal. Proposta. */
    val codigo: TextStyle = TextStyle(
        fontFamily = OdFontes.codigo,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(400),
        lineHeightStyle = alinhamento,
    ),
)
