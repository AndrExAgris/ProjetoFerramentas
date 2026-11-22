package io.github.andrexagris.ferramentas.ui.ferramentas.saude

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.andrexagris.ferramentas.base.ControleCamera
import io.github.andrexagris.ferramentas.base.Serie
import io.github.andrexagris.ferramentas.base.VistaCamera
import io.github.andrexagris.ferramentas.base.bipe
import io.github.andrexagris.ferramentas.base.brilhoCentral
import io.github.andrexagris.ferramentas.base.fluxoSensor
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.rememberSensor
import io.github.andrexagris.ferramentas.dados.rememberAjusteFloat
import io.github.andrexagris.ferramentas.dados.rememberAjusteTexto
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotaoMedir
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotoes
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtDeslizante
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.ferramentas.ui.ferramentas.numero
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdGraficoTempoReal
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.odSerieResultante
import kotlinx.coroutines.flow.conflate
import java.time.LocalDate
import kotlin.math.sqrt

fun telasSaude(): Map<String, TelaDeFerramenta> = mapOf(
    "saude.frequencia-camera" com { FrequenciaCamera() },
    "saude.frequencia-sensor" com { FrequenciaSensor() },
    "saude.pedometro" com { Pedometro() },
    "saude.repeticoes" com { ContadorRepeticoes() },
)

@Composable
private fun FrequenciaCamera() {
    val controle = remember { ControleCamera() }
    var medindo by remember { mutableStateOf(false) }
    val onda = remember { Serie(150) }
    var bpm by remember { mutableStateOf<Double?>(null) }
    var dedo by remember { mutableStateOf(false) }
    val analise = remember {
        object {
            val brutos = ArrayDeque<Pair<Long, Double>>()
            val batidas = ArrayDeque<Long>()
            var ultimo = 0.0; var subindo = false
        }
    }
    io.github.andrexagris.ferramentas.base.ManterTelaLigada()
    FtColuna {
        if (medindo) {
            VistaCamera(Modifier.size(1.dp), controle = controle, mostrarPreview = false) { img ->
                controle.lanterna(true)
                val t = System.currentTimeMillis()
                val v = brilhoCentral(img, 0.5f)
                dedo = v > 40 && v < 250
                analise.brutos.addLast(t to v)
                while (analise.brutos.isNotEmpty() && t - analise.brutos.first().first > 1500) analise.brutos.removeFirst()
                // Passa-alta: tira a média de 1,5 s; o pulso aparece como queda de brilho a cada batida.
                val filtrado = analise.brutos.map { it.second }.average() - v
                onda.add(filtrado.toFloat())
                if (filtrado > analise.ultimo) analise.subindo = true
                else if (analise.subindo && filtrado < analise.ultimo) {
                    analise.subindo = false
                    if (analise.batidas.isEmpty() || t - analise.batidas.last() > 330) {
                        analise.batidas.addLast(t)
                        while (analise.batidas.size > 12) analise.batidas.removeFirst()
                        if (analise.batidas.size >= 5) {
                            val intervalos = analise.batidas.zipWithNext { a, b -> (b - a).toDouble() }.sorted()
                            bpm = 60000 / intervalos[intervalos.size / 2]
                        }
                    }
                }
                analise.ultimo = filtrado
            }
            FtPainel(titulo = if (dedo) "Medindo" else "Cubra a câmera e o flash com o dedo") {
                OdLeitura(bpm?.takeIf { dedo }?.let { num(it, 0) } ?: "—", unidade = "bpm", tamanho = OdTamanhoLeitura.Destaque)
                OdGraficoTempoReal(listOf(odSerieResultante(onda.pontos.toList(), "pulso")), -3f, 3f, janela = 150, legenda = false, rotuloTempo = null)
            }
        }
        FtBotaoMedir(medindo, { medindo = it; bpm = null; analise.batidas.clear(); if (!it) controle.lanterna(false) })
        FtNota("Cubra de leve a câmera traseira e o flash com a ponta do dedo e fique parado por 20 segundos. Não é aparelho médico.")
    }
}

@Composable
private fun FrequenciaSensor() {
    val l by rememberSensor(Sensor.TYPE_HEART_RATE)
    FtColuna {
        FtPainel(titulo = "Frequência cardíaca") { OdLeitura(l?.get(0)?.takeIf { it > 0 }?.let { num(it, 0) } ?: "—", unidade = "bpm", tamanho = OdTamanhoLeitura.Destaque) }
        FtNota("Encoste o dedo no sensor e espere alguns segundos. Não é aparelho médico.")
    }
}

@Composable
private fun Pedometro() {
    val l by rememberSensor(Sensor.TYPE_STEP_COUNTER)
    var dia by rememberAjusteTexto("saude.pedometro.dia", "")
    var base by rememberAjusteFloat("saude.pedometro.base", -1f)
    var passo by rememberAjusteFloat("saude.pedometro.passo", 0.75f)
    val total = l?.get(0)
    LaunchedEffect(total) {
        total ?: return@LaunchedEffect
        val hoje = LocalDate.now().toString()
        // O contador do Android soma desde que o aparelho ligou; guarda o valor do começo do dia.
        if (dia != hoje || base < 0 || total < base) { dia = hoje; base = total }
    }
    val hoje = if (total != null && base >= 0) (total - base).coerceAtLeast(0f) else null
    FtColuna {
        FtPainel(titulo = "Passos hoje") { OdLeitura(hoje?.let { num(it, 0) } ?: "—", tamanho = OdTamanhoLeitura.Destaque) }
        FtInfos(null, listOf(
            "Distância" to hoje?.let { "${num(it * passo / 1000, 2)} km" },
            "Calorias (estimativa)" to hoje?.let { "${num(it * 0.04, 0)} kcal" },
            "Desde que o aparelho ligou" to total?.let { num(it, 0) },
        ))
        FtDeslizante("Tamanho do passo", passo, { passo = it }, 0.4f..1.1f, "${num(passo * 100, 0)} cm")
        FtNota("A contagem começa quando você abre esta ferramenta pela primeira vez no dia. O sensor conta mesmo com o app fechado.")
    }
}

@Composable
private fun ContadorRepeticoes() {
    val c = LocalContext.current
    var contando by remember { mutableStateOf(false) }
    var reps by remember { mutableIntStateOf(0) }
    var sensibilidade by rememberSaveable { mutableFloatStateOf(2.5f) }
    val serie = remember { Serie(200) }
    LaunchedEffect(contando, sensibilidade) {
        if (!contando) return@LaunchedEffect
        var media = SensorManager.GRAVITY_EARTH
        var acima = false
        var ultima = 0L
        c.fluxoSensor(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME).conflate().collect { l ->
            val m = sqrt(l[0] * l[0] + l[1] * l[1] + l[2] * l[2])
            media = media * 0.98f + m * 0.02f
            val d = m - media
            serie.add(d)
            // Histerese: conta ao subir acima do limiar e só arma de novo ao voltar abaixo de zero.
            if (!acima && d > sensibilidade && System.currentTimeMillis() - ultima > 400) { acima = true; reps++; ultima = System.currentTimeMillis(); bipe(1200.0, 40) }
            if (acima && d < 0) acima = false
        }
    }
    FtColuna {
        FtPainel(titulo = "Repetições") { OdLeitura(reps.toString(), tamanho = OdTamanhoLeitura.Destaque) }
        OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "a")), -8f, 8f, janela = 200, unidade = "m/s²", rotuloTempo = null)
        FtDeslizante("Limiar", sensibilidade, { sensibilidade = it }, 1f..6f, "${num(sensibilidade, 1)} m/s²")
        FtBotoes {
            OdBotao(if (contando) "Pausar" else "Começar", { contando = !contando }, Modifier.weight(1f), tipo = io.github.andrexagris.opendesign.OdTipoBotao.Primario)
            OdBotao("Zerar", { reps = 0 }, Modifier.weight(1f))
        }
        FtNota("Prenda o celular no braço ou segure junto ao corpo. Cada movimento forte conta uma repetição; ajuste o limiar se contar demais ou de menos.")
    }
}

