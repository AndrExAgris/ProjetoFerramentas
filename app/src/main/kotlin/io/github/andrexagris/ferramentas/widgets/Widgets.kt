package io.github.andrexagris.ferramentas.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.action.actionStartActivity
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider as TipoCor
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.github.andrexagris.ferramentas.MainActivity
import io.github.andrexagris.ferramentas.base.Lanterna
import io.github.andrexagris.ferramentas.base.estadoBateriaAgora
import io.github.andrexagris.ferramentas.base.textoStatus
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.opendesign.OdRaios
import io.github.andrexagris.opendesign.odCores

/** Cores dos widgets: as mesmas do Open Design, claro e escuro conforme o sistema. */
private object CoresWidget {
    private val claro = odCores(claro = true)
    private val escuro = odCores(claro = false)
    val fundo = ColorProvider(claro.superficie, escuro.superficie)
    val texto = ColorProvider(claro.texto, escuro.texto)
    val texto2 = ColorProvider(claro.texto2, escuro.texto2)
    val ativo = ColorProvider(claro.primaria.cor, escuro.primaria.cor)
    val sobreAtivo = ColorProvider(claro.primaria.sobre, escuro.primaria.sobre)
}

@Composable
private fun Cartao(modifier: GlanceModifier, fundo: TipoCor = CoresWidget.fundo, conteudo: @Composable () -> Unit) {
    Column(
        modifier.fillMaxSize().background(fundo).cornerRadius(OdRaios.flutuante).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally,
    ) { conteudo() }
}

/** Widget de bateria: nível e estado. Atualiza a cada 15 min (tarefa periódica) e ao abrir o app. */
class WidgetBateria : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val e = context.estadoBateriaAgora()
        provideContent {
            Cartao(GlanceModifier.clickable(actionStartActivity<MainActivity>())) {
                Text("${e?.nivel?.toInt() ?: "—"}%", style = TextStyle(color = CoresWidget.texto, fontSize = 28.sp, fontWeight = FontWeight.Medium))
                Text(e?.let { "${textoStatus(it.status)} · ${num(it.temperatura, 0)} °C" } ?: "Bateria", style = TextStyle(color = CoresWidget.texto2, fontSize = 12.sp))
            }
        }
    }
}

class ReceptorWidgetBateria : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WidgetBateria()
}

/** Widget de lanterna: toque liga e desliga. */
class WidgetLanterna : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ligada = Lanterna.ligada.value
        provideContent {
            Cartao(GlanceModifier.clickable(actionRunCallback<AlternarLanterna>()), fundo = if (ligada) CoresWidget.ativo else CoresWidget.fundo) {
                Text("Lanterna", style = TextStyle(color = if (ligada) CoresWidget.sobreAtivo else CoresWidget.texto, fontSize = 16.sp, fontWeight = FontWeight.Medium))
                Text(if (ligada) "Ligada" else "Desligada", style = TextStyle(color = if (ligada) CoresWidget.sobreAtivo else CoresWidget.texto2, fontSize = 12.sp))
            }
        }
    }
}

class AlternarLanterna : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Lanterna.alternar(context)
        WidgetLanterna().updateAll(context)
    }
}

class ReceptorWidgetLanterna : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WidgetLanterna()
}
