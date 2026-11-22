package io.github.andrexagris.opendesign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class OdTamanhoLeitura { Destaque, Padrao, Pequena }

/** Como mostrar o sinal: só nos negativos (espaço do mesmo tamanho nos positivos) ou sempre (+ e −). */
enum class OdSinal { Nenhum, Negativo, Sempre }

private const val MENOS = '−'
private const val ESPACO_DIGITO = ' '

/**
 * Formata uma leitura no padrão do Open Design: vírgula decimal, ponto nos milhares, casas fixas,
 * sinal de menos de verdade (U+2212) e espaço de dígito no lugar do sinal nos positivos.
 * largura: completa à esquerda com espaços de dígito até esse número de caracteres.
 * Ex.: odFormatar(-3.14159, 2) = "−3,14"; odFormatar(3.1, 2) = " 3,10".
 */
fun odFormatar(valor: Double, casas: Int = 1, sinal: OdSinal = OdSinal.Negativo, largura: Int? = null): String {
    val simbolos = DecimalFormatSymbols(Locale("pt", "BR"))
    val padrao = "#,##0" + if (casas > 0) "." + "0".repeat(casas) else ""
    val numero = DecimalFormat(padrao, simbolos).format(kotlin.math.abs(valor))
    val zero = numero.all { it == '0' || it == ',' || it == '.' }
    val prefixo = when {
        sinal == OdSinal.Nenhum -> ""
        valor < 0 && !zero -> MENOS.toString()
        sinal == OdSinal.Sempre && !zero -> "+"
        else -> ESPACO_DIGITO.toString()
    }
    val s = prefixo + numero
    return if (largura != null && s.length < largura) ESPACO_DIGITO.toString().repeat(largura - s.length) + s else s
}

/**
 * Valor ao vivo com números tabulares e largura fixa: o valor muda o tempo todo e nada ao redor se mexe.
 * largura: quantos caracteres reservar (sinal + dígitos + separador). A largura é medida com a fonte real.
 */
@Composable
fun OdLeitura(
    valor: String,
    modifier: Modifier = Modifier,
    unidade: String? = null,
    largura: Int = valor.length,
    tamanho: OdTamanhoLeitura = OdTamanhoLeitura.Padrao,
    cor: Color = Color.Unspecified,
    desabilitado: Boolean = false,
    descricao: String? = null,
) {
    val c = OdTema.cores
    val t = OdTema.tipografia
    val estilo: TextStyle = when (tamanho) {
        OdTamanhoLeitura.Destaque -> t.leituraDestaque
        OdTamanhoLeitura.Padrao -> t.leitura
        OdTamanhoLeitura.Pequena -> t.leituraPequena
    }
    val medidor = rememberTextMeasurer()
    val densidade = LocalDensity.current
    val larguraDp = remember(largura, estilo, densidade) {
        with(densidade) { medidor.measure("0".repeat(largura.coerceAtLeast(1)), estilo).size.width.toDp() }
    }
    val corValor = when { desabilitado -> c.textoDesabilitado; cor != Color.Unspecified -> cor; else -> c.texto }
    Row(
        modifier.clearAndSetSemantics { contentDescription = descricao ?: listOfNotNull(valor.trim().replace(MENOS, '-'), unidade).joinToString(" ") },
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(valor, style = estilo, color = corValor, textAlign = TextAlign.End, maxLines = 1, softWrap = false, modifier = Modifier.width(larguraDp).alignByBaseline())
        if (unidade != null) Text(
            unidade, maxLines = 1, color = c.texto2, modifier = Modifier.alignByBaseline(),
            style = estilo.copy(fontSize = if (tamanho == OdTamanhoLeitura.Pequena) t.uiPequeno.fontSize else estilo.fontSize / 2, fontWeight = FontWeight.Medium, fontFeatureSettings = null),
        )
    }
}

/** Card de leitura: rótulo com ícone (e um chip de estado à direita), valor e detalhe. */
@Composable
fun OdCartaoLeitura(
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    unidade: String? = null,
    largura: Int = valor.length,
    icone: ImageVector? = null,
    detalhe: String? = null,
    desabilitado: Boolean = false,
    chip: (@Composable () -> Unit)? = null,
) {
    val c = OdTema.cores
    val forma = RoundedCornerShape(OdRaios.painel)
    Column(
        modifier.background(c.superficie, forma).border(OdMedidas.bordaFina, c.borda, forma).padding(OdEspacos.e4 * OdTema.densidade.fator),
        verticalArrangement = Arrangement.spacedBy(OdEspacos.e1),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icone != null) Icon(icone, null, Modifier.size(16.dp), tint = c.texto2)
            Text(rotulo, style = OdTema.tipografia.uiPequeno.copy(fontWeight = FontWeight.Medium), color = c.texto2, maxLines = 1, modifier = Modifier.weight(1f))
            if (chip != null) { Spacer(Modifier.width(4.dp)); chip() }
        }
        OdLeitura(valor, unidade = unidade, largura = largura, desabilitado = desabilitado)
        if (detalhe != null) Text(detalhe, style = OdTema.tipografia.uiPequeno.copy(fontFeatureSettings = "tnum"), color = c.texto2)
    }
}
