package io.github.andrexagris.ferramentas.ui.ferramentas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdMedidas
import io.github.andrexagris.opendesign.OdRaios
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdToasts
import io.github.andrexagris.opendesign.rememberOdToasts

/** Toasts da tela atual, para as ferramentas mostrarem "Medição salva." sem passar parâmetro. */
val LocalToasts = compositionLocalOf<OdToasts> { error("Sem toasts") }

/** Coluna padrão de uma ferramenta: rola, tem as margens da grade e espaço entre os blocos. */
@Composable
fun FtColuna(modifier: Modifier = Modifier, rolar: Boolean = true, conteudo: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxSize().then(if (rolar) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(horizontal = OdMedidas.gradeMargemCompacta, vertical = OdEspacos.e4),
        verticalArrangement = Arrangement.spacedBy(OdEspacos.e4),
        content = conteudo,
    )
}

/** Painel com borda, para agrupar uma leitura grande, um gráfico ou um instrumento. */
@Composable
fun FtPainel(modifier: Modifier = Modifier, titulo: String? = null, conteudo: @Composable ColumnScope.() -> Unit) {
    val c = OdTema.cores
    val forma = RoundedCornerShape(OdRaios.painel)
    Column(
        modifier.fillMaxWidth().background(c.superficie, forma).border(OdMedidas.bordaFina, c.borda, forma).padding(OdEspacos.e4),
        verticalArrangement = Arrangement.spacedBy(OdEspacos.e3),
    ) {
        if (titulo != null) Text(titulo, style = OdTema.tipografia.uiPequeno.copy(fontWeight = FontWeight.SemiBold), color = c.texto2)
        conteudo()
    }
}

/** Lista de pares rótulo/valor (informações técnicas), no estilo das linhas de configuração. */
@Composable
fun FtInfos(titulo: String?, itens: List<Pair<String, String?>>, icone: ImageVector? = null) {
    val visiveis = itens.filter { it.second != null }
    if (visiveis.isEmpty()) return
    OdGrupoConfig(titulo) {
        visiveis.forEach { (r, v) ->
            OdLinhaConfig(r, icone = icone) {
                Text(v!!, style = OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum"), color = OdTema.cores.texto, textAlign = TextAlign.End, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Texto de ajuda discreto (como usar, limitações). */
@Composable
fun FtNota(texto: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = OdEspacos.e1), horizontalArrangement = Arrangement.spacedBy(OdEspacos.e2)) {
        Icon(OdIcones.Info, null, Modifier.size(16.dp).padding(top = 2.dp), tint = OdTema.cores.texto2)
        Text(texto, style = OdTema.tipografia.uiPequeno, color = OdTema.cores.texto2)
    }
}

/** Campo de texto do Open Design (Material 3 com as cores e fontes do tema). */
@Composable
fun FtCampo(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    numerico: Boolean = false,
    sufixo: String? = null,
    linhas: Int = 1,
    erro: String? = null,
) {
    OutlinedTextField(
        valor, aoMudar, modifier.fillMaxWidth(),
        label = { Text(rotulo) },
        singleLine = linhas == 1, minLines = linhas,
        suffix = sufixo?.let { { Text(it) } },
        isError = erro != null,
        supportingText = erro?.let { { Text(it) } },
        shape = RoundedCornerShape(OdRaios.controle),
        textStyle = OdTema.tipografia.ui.copy(fontSize = OdTema.tipografia.uiSubtitulo.fontSize),
        keyboardOptions = if (numerico) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
    )
}

/** Converte texto digitado ("1,5" ou "1.5") em número. */
fun String.numero(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Botão Iniciar/Parar para medições. */
@Composable
fun FtBotaoMedir(medindo: Boolean, aoMudar: (Boolean) -> Unit, modifier: Modifier = Modifier, iniciar: String = "Iniciar medição", parar: String = "Parar") {
    OdBotao(
        if (medindo) parar else iniciar, { aoMudar(!medindo) }, modifier.fillMaxWidth(),
        tipo = if (medindo) OdTipoBotao.Secundario else OdTipoBotao.Primario,
        icone = if (medindo) OdIcones.Parar else OdIcones.Iniciar,
    )
}

/** Fileira de botões que dividem a largura. */
@Composable
fun FtBotoes(conteudo: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OdEspacos.e2), content = conteudo)
}

/** Deslizante com rótulo e valor à direita. */
@Composable
fun FtDeslizante(rotulo: String, valor: Float, aoMudar: (Float) -> Unit, faixa: ClosedFloatingPointRange<Float>, texto: String, passos: Int = 0) {
    val c = OdTema.cores
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(rotulo, style = OdTema.tipografia.uiMedio, color = c.texto, modifier = Modifier.weight(1f))
            Text(texto, style = OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum"), color = c.texto2)
        }
        Slider(
            valor, aoMudar, valueRange = faixa, steps = passos,
            colors = SliderDefaults.colors(thumbColor = c.primaria.cor, activeTrackColor = c.primaria.cor, inactiveTrackColor = c.superficieSutil),
            modifier = Modifier.semantics { contentDescription = "$rotulo: $texto" },
        )
    }
}

/**
 * Gráfico de linha para históricos (pontos com x em ms). Mesmo visual do OdGraficoTempoReal (grade, eixos e
 * cores de gráfico), mas com eixo de tempo real e escala automática.
 */
@Composable
fun FtGraficoHistorico(
    pontos: List<Pair<Long, Float>>,
    modifier: Modifier = Modifier,
    altura: Dp = 160.dp,
    cor: Color = OdTema.cores.secundaria.cor,
    minimo: Float? = null,
    maximo: Float? = null,
    descricao: String,
) {
    val c = OdTema.cores
    Box(
        modifier.fillMaxWidth().height(altura).background(c.superficie, RoundedCornerShape(OdRaios.painel))
            .border(OdMedidas.bordaFina, c.borda, RoundedCornerShape(OdRaios.painel)).padding(OdEspacos.e3)
            .semantics { contentDescription = descricao },
    ) {
        if (pontos.size < 2) {
            Text("Ainda sem dados suficientes.", style = OdTema.tipografia.uiPequeno, color = c.texto2, modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        val vMin = minimo ?: pontos.minOf { it.second }
        val vMax = (maximo ?: pontos.maxOf { it.second }).let { if (it - vMin < 1e-6f) vMin + 1f else it }
        val t0 = pontos.first().first; val t1 = pontos.last().first.coerceAtLeast(t0 + 1)
        Canvas(Modifier.fillMaxSize()) {
            for (i in 0..4) {
                val y = size.height * i / 4
                drawLine(c.graficoGrade, Offset(0f, y), Offset(size.width, y), 1f)
            }
            val caminho = Path()
            pontos.forEachIndexed { i, (t, v) ->
                val x = (t - t0).toFloat() / (t1 - t0) * size.width
                val y = size.height - (v - vMin) / (vMax - vMin) * size.height
                if (i == 0) caminho.moveTo(x, y) else caminho.lineTo(x, y)
            }
            drawPath(caminho, cor, style = Stroke(2.dp.toPx()))
        }
    }
}

/** Gráfico de barras simples (rótulo, valor), horizontal, para listas como uso por app. */
@Composable
fun FtBarra(rotulo: String, valor: Float, maximo: Float, texto: String, icone: (@Composable () -> Unit)? = null) {
    val c = OdTema.cores
    Column(Modifier.fillMaxWidth().padding(vertical = OdEspacos.e1), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OdEspacos.e2)) {
            icone?.invoke()
            Text(rotulo, style = OdTema.tipografia.uiMedio, color = c.texto, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(texto, style = OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum"), color = c.texto2)
        }
        Box(Modifier.fillMaxWidth().height(6.dp).background(c.superficieSutil, RoundedCornerShape(3.dp))) {
            Box(Modifier.fillMaxWidth((valor / maximo).coerceIn(0f, 1f)).height(6.dp).background(c.secundaria.cor, RoundedCornerShape(3.dp)))
        }
    }
}

/** Para pré-visualizações e testes. */
@Composable
fun ProverToastsDeTeste(conteudo: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(LocalToasts provides rememberOdToasts(), content = conteudo)
}
