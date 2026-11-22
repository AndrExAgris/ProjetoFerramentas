package io.github.andrexagris.ferramentas.ui.ferramentas.aparelho

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.VibrationEffect
import android.os.VibratorManager
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.andrexagris.ferramentas.base.CapturarTeclas
import io.github.andrexagris.ferramentas.base.Serie
import io.github.andrexagris.ferramentas.base.estadoBateriaAgora
import io.github.andrexagris.ferramentas.base.formatarBytes
import io.github.andrexagris.ferramentas.base.formatarDataHora
import io.github.andrexagris.ferramentas.base.formatarDuracao
import io.github.andrexagris.ferramentas.base.fluxoSensor
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.rememberSensor
import io.github.andrexagris.ferramentas.base.rotulo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBarra
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotoes
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtDeslizante
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdGraficoTempoReal
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdLinhaValor
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.odSerieResultante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sqrt

fun telasAparelho(): Map<String, TelaDeFerramenta> = mapOf(
    "aparelho.hardware" com { InfoHardware() },
    "aparelho.sensores" com { ListaSensores() },
    "aparelho.tela" com { TesteTela() },
    "aparelho.toque" com { TesteToque() },
    "aparelho.vibracao" com { TesteVibracao() },
    "aparelho.botoes" com { TesteBotoes() },
    "aparelho.temperaturas" com { Temperaturas() },
    "aparelho.ram" com { MonitorRam() },
    "aparelho.tempo-de-tela" com { TempoDeTela() },
    "aparelho.armazenamento" com { Armazenamento() },
    "aparelho.apps" com { GerenciadorApps() },
)

private fun lerArquivo(caminho: String): String? = runCatching { File(caminho).readText().trim() }.getOrNull()

@Composable
private fun InfoHardware() {
    val c = LocalContext.current
    val am = c.getSystemService(ActivityManager::class.java)
    val mem = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
    val dm = c.resources.displayMetrics
    val display = c.display
    val polegadas = sqrt((dm.widthPixels / dm.xdpi).let { it * it } + (dm.heightPixels / dm.ydpi).let { it * it })
    val nucleos = Runtime.getRuntime().availableProcessors()
    val freqs = (0 until nucleos).mapNotNull { lerArquivo("/sys/devices/system/cpu/cpu$it/cpufreq/cpuinfo_max_freq")?.toLongOrNull() }
    val dados = StatFs(Environment.getDataDirectory().path)
    val cameras = remember {
        runCatching {
            val cm = c.getSystemService(CameraManager::class.java)
            cm.cameraIdList.map { id ->
                val ch = cm.getCameraCharacteristics(id)
                val px = ch.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val lado = if (ch.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) "Frontal" else "Traseira"
                "$lado ${px?.let { num(it.width * it.height / 1e6, 0) } ?: "?"} MP"
            }
        }.getOrDefault(emptyList())
    }
    FtColuna {
        FtInfos("Aparelho", listOf(
            "Modelo" to "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}", "Código" to Build.DEVICE,
            "Android" to "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", "Atualização de segurança" to Build.VERSION.SECURITY_PATCH,
            "Kernel" to System.getProperty("os.version"), "Compilação" to Build.DISPLAY,
        ))
        FtInfos("Processador", listOf(
            "Chip" to "${Build.SOC_MANUFACTURER} ${Build.SOC_MODEL}".trim().ifBlank { Build.HARDWARE }, "Núcleos" to nucleos.toString(),
            "Frequências máximas" to freqs.distinct().sortedDescending().joinToString { "${num(it / 1e6, 2)} GHz" }.ifBlank { null },
            "Arquiteturas" to Build.SUPPORTED_ABIS.joinToString(), "OpenGL ES" to am.deviceConfigurationInfo.glEsVersion,
        ))
        FtInfos("Memória e armazenamento", listOf("RAM" to formatarBytes(mem.totalMem), "Armazenamento interno" to formatarBytes(dados.totalBytes), "Livre" to formatarBytes(dados.availableBytes)))
        FtInfos("Tela", listOf(
            "Resolução" to "${dm.widthPixels} × ${dm.heightPixels}", "Tamanho" to "${num(polegadas, 1)}\"", "Densidade" to "${dm.densityDpi} dpi (${num(dm.xdpi, 0)} ppi reais)",
            "Taxas de atualização" to display?.supportedModes?.map { it.refreshRate.toInt() }?.distinct()?.sorted()?.joinToString { "$it Hz" },
            "HDR" to display?.hdrCapabilities?.supportedHdrTypes?.isNotEmpty()?.let { if (it) "Sim" else "Não" },
        ))
        FtInfos("Câmeras", cameras.mapIndexed { i, s -> "Câmera ${i + 1}" to s })
        FtInfos("Recursos", listOf(
            "NFC" to sim(c, PackageManager.FEATURE_NFC), "Infravermelho" to sim(c, PackageManager.FEATURE_CONSUMER_IR), "UWB" to sim(c, "android.hardware.uwb"),
            "Wi-Fi RTT" to sim(c, PackageManager.FEATURE_WIFI_RTT), "Wi-Fi Aware" to sim(c, PackageManager.FEATURE_WIFI_AWARE), "Bluetooth LE" to sim(c, PackageManager.FEATURE_BLUETOOTH_LE),
            "Leitor de digitais" to sim(c, PackageManager.FEATURE_FINGERPRINT), "Barômetro" to sim(c, PackageManager.FEATURE_SENSOR_BAROMETER),
        ))
    }
}

private fun sim(c: Context, recurso: String) = if (c.packageManager.hasSystemFeature(recurso)) "Sim" else "Não"

private fun nomeTipoSensor(t: Int): String = when (t) {
    Sensor.TYPE_ACCELEROMETER -> "Acelerômetro"; Sensor.TYPE_GYROSCOPE -> "Giroscópio"; Sensor.TYPE_MAGNETIC_FIELD -> "Magnetômetro"; Sensor.TYPE_LIGHT -> "Luz"
    Sensor.TYPE_PROXIMITY -> "Proximidade"; Sensor.TYPE_PRESSURE -> "Barômetro"; Sensor.TYPE_GRAVITY -> "Gravidade"; Sensor.TYPE_LINEAR_ACCELERATION -> "Aceleração linear"
    Sensor.TYPE_ROTATION_VECTOR -> "Vetor de rotação"; Sensor.TYPE_GAME_ROTATION_VECTOR -> "Rotação (jogos)"; Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> "Rotação geomagnética"
    Sensor.TYPE_STEP_COUNTER -> "Contador de passos"; Sensor.TYPE_STEP_DETECTOR -> "Detector de passos"; Sensor.TYPE_SIGNIFICANT_MOTION -> "Movimento significativo"
    Sensor.TYPE_AMBIENT_TEMPERATURE -> "Temperatura"; Sensor.TYPE_RELATIVE_HUMIDITY -> "Umidade"; Sensor.TYPE_HEART_RATE -> "Frequência cardíaca"
    Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "Acelerômetro (bruto)"; Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> "Giroscópio (bruto)"; Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> "Magnetômetro (bruto)"
    Sensor.TYPE_HINGE_ANGLE -> "Ângulo da dobradiça"; Sensor.TYPE_STATIONARY_DETECT -> "Parado"; Sensor.TYPE_MOTION_DETECT -> "Em movimento"
    else -> "Outro (${t})"
}

@Composable
private fun ListaSensores() {
    val c = LocalContext.current
    val sm = remember { c.getSystemService(SensorManager::class.java) }
    val sensores = remember { sm.getSensorList(Sensor.TYPE_ALL).sortedBy { it.type } }
    var aberto by rememberSaveable { mutableStateOf<Int?>(null) }
    FtColuna {
        val s = aberto?.let { i -> sensores.getOrNull(i) }
        if (s != null) {
            val l by produceState<FloatArray?>(null, s) {
                kotlinx.coroutines.flow.callbackFlow {
                    val o = object : android.hardware.SensorEventListener {
                        override fun onSensorChanged(e: android.hardware.SensorEvent) { trySend(e.values.copyOf()) }
                        override fun onAccuracyChanged(x: Sensor?, a: Int) {}
                    }
                    sm.registerListener(o, s, SensorManager.SENSOR_DELAY_UI)
                    awaitClose { sm.unregisterListener(o) }
                }.collect { value = it }
            }
            OdBotao("Voltar para a lista", { aberto = null }, Modifier.fillMaxWidth(), icone = OdIcones.Voltar)
            FtPainel(titulo = s.name) {
                l?.forEachIndexed { i, v -> OdLeitura(num(v, 3), unidade = "valor ${i + 1}") } ?: Text("Esperando leitura… (alguns sensores só mudam com movimento)", style = OdTema.tipografia.uiPequeno, color = OdTema.cores.texto2)
            }
            FtInfos(null, listOf("Tipo" to nomeTipoSensor(s.type), "Fabricante" to s.vendor, "Versão" to s.version.toString(), "Alcance" to num(s.maximumRange, 2), "Resolução" to num(s.resolution, 5),
                "Consumo" to "${num(s.power, 2)} mA", "Taxa máxima" to if (s.minDelay > 0) "${1_000_000 / s.minDelay} Hz" else "Só quando muda", "Virtual" to if (s.isDynamicSensor) "Dinâmico" else null))
        } else {
            FtNota("${sensores.size} sensores. Toque em um para ver os valores ao vivo.")
            OdGrupoConfig(null) {
                sensores.forEachIndexed { i, se -> OdLinhaValor(nomeTipoSensor(se.type), null, { aberto = i }, descricao = "${se.name} · ${se.vendor}") }
            }
        }
    }
}

@Composable
private fun TesteTela() {
    var aberto by remember { mutableStateOf(false) }
    var etapa by remember { mutableIntStateOf(0) }
    val cores = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black, Color.Gray)
    FtColuna {
        FtNota("Em tela cheia, toque para trocar: vermelho, verde, azul, branco, preto, cinza, degradê e grade. Procure pixels apagados, manchas e marcas de burn-in. Voltar sai.")
        OdBotao("Começar teste", { etapa = 0; aberto = true }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario)
    }
    if (aberto) Dialog({ aberto = false }, DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().clickable { if (etapa >= cores.size + 1) aberto = false else etapa++ }) {
            // As cores puras são o próprio teste da tela, não decoração da interface.
            when {
                etapa < cores.size -> Box(Modifier.fillMaxSize().background(cores[etapa]))
                etapa == cores.size -> Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black, Color.White))))
                else -> Canvas(Modifier.fillMaxSize().background(Color.Black)) {
                    val passo = 2.dp.toPx() * 4
                    var x = 0f; while (x < size.width) { drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), 1f); x += passo }
                    var y = 0f; while (y < size.height) { drawLine(Color.White, Offset(0f, y), Offset(size.width, y), 1f); y += passo }
                }
            }
        }
    }
}

@Composable
private fun TesteToque() {
    val toques = remember { mutableStateMapOf<Long, Offset>() }
    var maximo by remember { mutableIntStateOf(0) }
    val c = OdTema.cores
    val medidor = rememberTextMeasurer()
    val estiloNumero = OdTema.tipografia.uiTitulo.copy(color = c.texto)
    val cores = listOf(c.graficoX, c.graficoY, c.graficoZ, c.secundaria.cor, c.primaria.cor, c.info.cor, c.aviso.cor, c.sucesso.cor, c.erro.cor, c.texto)
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize().pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val e = awaitPointerEvent()
                    e.changes.forEach { ch -> if (ch.pressed) toques[ch.id.value] = ch.position else toques.remove(ch.id.value); ch.consume() }
                    maximo = maxOf(maximo, toques.size)
                }
            }
        }) {
            toques.entries.forEachIndexed { i, (_, p) ->
                val cor = cores[i % cores.size]
                drawCircle(cor, 48.dp.toPx(), p, style = Stroke(3.dp.toPx()))
                drawLine(cor.copy(alpha = 0.4f), Offset(0f, p.y), Offset(size.width, p.y), 1f)
                drawLine(cor.copy(alpha = 0.4f), Offset(p.x, 0f), Offset(p.x, size.height), 1f)
                val t = medidor.measure("${i + 1}", estiloNumero)
                drawText(t, topLeft = Offset(p.x - t.size.width / 2, p.y - 80.dp.toPx()))
            }
        }
        FtPainel(Modifier.align(Alignment.TopCenter)) {
            OdLeitura("${toques.size}", unidade = "dedos agora", tamanho = OdTamanhoLeitura.Padrao)
            OdLeitura("$maximo", unidade = "máximo")
        }
    }
}

@Composable
private fun TesteVibracao() {
    val c = LocalContext.current
    val vib = remember { c.getSystemService(VibratorManager::class.java).defaultVibrator }
    var duracao by rememberSaveable { mutableFloatStateOf(300f) }
    var forca by rememberSaveable { mutableFloatStateOf(255f) }
    FtColuna {
        OdGrupoConfig("Efeitos do sistema") {
            listOf(VibrationEffect.EFFECT_CLICK to "Clique", VibrationEffect.EFFECT_TICK to "Toque leve", VibrationEffect.EFFECT_HEAVY_CLICK to "Clique forte", VibrationEffect.EFFECT_DOUBLE_CLICK to "Clique duplo").forEach { (ef, nome) ->
                io.github.andrexagris.opendesign.OdLinhaAcao(nome, { vib.vibrate(VibrationEffect.createPredefined(ef)) })
            }
        }
        FtPainel(titulo = "Vibração livre") {
            FtDeslizante("Duração", duracao, { duracao = it }, 20f..2000f, "${duracao.toInt()} ms")
            if (vib.hasAmplitudeControl()) FtDeslizante("Força", forca, { forca = it }, 1f..255f, "${(forca / 2.55f).toInt()}%")
            FtBotoes {
                OdBotao("Vibrar", { vib.vibrate(VibrationEffect.createOneShot(duracao.toLong(), if (vib.hasAmplitudeControl()) forca.toInt() else VibrationEffect.DEFAULT_AMPLITUDE)) }, Modifier.weight(1f), tipo = OdTipoBotao.Primario)
                OdBotao("Padrão SOS", { vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150, 100, 150, 300, 450, 100, 450, 100, 450, 300, 150, 100, 150, 100, 150), -1)) }, Modifier.weight(1f))
            }
        }
        FtInfos(null, listOf("Controle de força" to if (vib.hasAmplitudeControl()) "Sim" else "Não"))
    }
}

private fun nomeTecla(k: Int): String = when (k) {
    KeyEvent.KEYCODE_VOLUME_UP -> "Volume +"; KeyEvent.KEYCODE_VOLUME_DOWN -> "Volume −"; KeyEvent.KEYCODE_POWER -> "Liga/desliga"; KeyEvent.KEYCODE_HEADSETHOOK -> "Botão do fone"
    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "Play/pause"; KeyEvent.KEYCODE_MEDIA_NEXT -> "Próxima"; KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "Anterior"; KeyEvent.KEYCODE_CAMERA -> "Câmera"
    else -> KeyEvent.keyCodeToString(k).removePrefix("KEYCODE_")
}

@Composable
private fun TesteBotoes() {
    val testados = remember { mutableStateMapOf<String, Int>() }
    var ultima by remember { mutableStateOf<String?>(null) }
    val prox by rememberSensor(Sensor.TYPE_PROXIMITY)
    val c = LocalContext.current
    val maxProx = remember { c.getSystemService(SensorManager::class.java).getDefaultSensor(Sensor.TYPE_PROXIMITY)?.maximumRange }
    CapturarTeclas { e ->
        if (e.action == KeyEvent.ACTION_DOWN && e.repeatCount == 0) { val n = nomeTecla(e.keyCode); ultima = n; testados[n] = (testados[n] ?: 0) + 1 }
        e.keyCode == KeyEvent.KEYCODE_VOLUME_UP || e.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
    }
    FtColuna {
        FtPainel(titulo = "Último botão") { OdLeitura(ultima ?: "Aperte um botão", tamanho = OdTamanhoLeitura.Destaque) }
        FtInfos("Botões testados", testados.map { (k, v) -> k to "$v×" })
        FtPainel(titulo = "Sensor de proximidade") {
            val v = prox?.get(0)
            OdLeitura(when { v == null -> "Sem leitura"; maxProx != null && v < maxProx -> "Perto"; else -> "Longe" }, tamanho = OdTamanhoLeitura.Padrao)
            Text("Valor: ${v?.let { num(it, 1) } ?: "—"} (máximo ${maxProx?.let { num(it, 1) } ?: "—"})", style = OdTema.tipografia.uiPequeno, color = OdTema.cores.texto2)
        }
        FtNota("Os botões de volume ficam presos nesta tela (não mudam o volume). Passe a mão na frente da parte de cima da tela para testar a proximidade; em alguns aparelhos ela é virtual e usa a tela.")
    }
}

@Composable
private fun Temperaturas() {
    val c = LocalContext.current
    val pm = remember { c.getSystemService(PowerManager::class.java) }
    val dados by produceState(Triple<Float?, Int, Float?>(null, 0, null)) {
        while (true) { value = Triple(c.estadoBateriaAgora()?.temperatura, pm.currentThermalStatus, runCatching { pm.getThermalHeadroom(10) }.getOrNull()?.takeIf { !it.isNaN() }); delay(2000) }
    }
    val zonas = remember {
        (0 until 40).mapNotNull { i ->
            val t = lerArquivo("/sys/class/thermal/thermal_zone$i/temp")?.toLongOrNull() ?: return@mapNotNull null
            val nome = lerArquivo("/sys/class/thermal/thermal_zone$i/type") ?: "zona $i"
            nome to (if (t > 1000) t / 1000.0 else t.toDouble())
        }.filter { it.second in 1.0..150.0 }
    }
    val estado = when (dados.second) { PowerManager.THERMAL_STATUS_NONE -> "Normal"; PowerManager.THERMAL_STATUS_LIGHT -> "Um pouco quente"; PowerManager.THERMAL_STATUS_MODERATE -> "Quente: desempenho pode cair"
        PowerManager.THERMAL_STATUS_SEVERE -> "Muito quente: desempenho reduzido"; PowerManager.THERMAL_STATUS_CRITICAL -> "Crítico"; else -> "Emergência" }
    FtColuna {
        FtPainel(titulo = "Bateria") { OdLeitura(dados.first?.let { num(it, 1) } ?: "—", unidade = "°C", tamanho = OdTamanhoLeitura.Destaque) }
        FtInfos("Estado térmico do sistema", listOf("Situação" to estado, "Margem até limitar" to dados.third?.let { "${num((1 - it) * 100, 0)}%" }))
        if (zonas.isNotEmpty()) FtInfos("Sensores internos", zonas.map { (n, t) -> n to "${num(t, 1)} °C" })
        else FtNota("Este aparelho não deixa apps lerem a temperatura da CPU. A margem térmica do Android indica quanto falta para o sistema reduzir o desempenho.")
    }
}

@Composable
private fun MonitorRam() {
    val c = LocalContext.current
    val am = remember { c.getSystemService(ActivityManager::class.java) }
    val serie = remember { Serie(120) }
    val info by produceState(ActivityManager.MemoryInfo()) {
        while (true) { val m = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }; value = m; serie.add(((m.totalMem - m.availMem) * 100f / m.totalMem)); delay(1000) }
    }
    val usado = info.totalMem - info.availMem
    FtColuna {
        FtPainel(titulo = "Em uso") {
            OdLeitura(num(usado * 100.0 / info.totalMem.coerceAtLeast(1), 0), unidade = "%", tamanho = OdTamanhoLeitura.Destaque)
            OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "%")), 0f, 100f, janela = 120, unidade = "%", casas = 0, rotuloTempo = "−2 min" to "agora")
        }
        FtInfos(null, listOf("Total" to formatarBytes(info.totalMem), "Em uso" to formatarBytes(usado), "Disponível" to formatarBytes(info.availMem),
            "Limite de memória baixa" to formatarBytes(info.threshold), "Memória baixa agora" to if (info.lowMemory) "Sim" else "Não"))
        FtNota("O Android usa a RAM livre como cache de propósito: memória \"cheia\" não é problema. O que importa é não ficar abaixo do limite de memória baixa.")
    }
}

@Composable
private fun TempoDeTela() {
    val c = LocalContext.current
    var dias by rememberSaveable { mutableIntStateOf(1) }
    val dados by produceState<Pair<List<Pair<String, Long>>, Int>?>(null, dias) {
        value = withContext(Dispatchers.IO) {
            val usm = c.getSystemService(UsageStatsManager::class.java)
            val fim = System.currentTimeMillis()
            val ini = if (dias == 1) io.github.andrexagris.ferramentas.base.inicioDe(io.github.andrexagris.ferramentas.base.Periodo.Hoje) else fim - dias * 86_400_000L
            val lista = usm.queryAndAggregateUsageStats(ini, fim).values.filter { it.totalTimeInForeground > 30_000 }.sortedByDescending { it.totalTimeInForeground }
                .map { c.packageManager.rotulo(it.packageName) to it.totalTimeInForeground }
            var desbloqueios = 0
            val ev = usm.queryEvents(ini, fim); val e = UsageEvents.Event()
            while (ev.hasNextEvent()) { ev.getNextEvent(e); if (e.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) desbloqueios++ }
            lista to desbloqueios
        }
    }
    FtColuna {
        OdSegmentado(listOf(1 to "Hoje", 7 to "7 dias"), dias, { dias = it })
        val d = dados ?: run { FtNota("Calculando…"); return@FtColuna }
        val total = d.first.sumOf { it.second }
        FtPainel(titulo = "Tempo de tela") { OdLeitura(formatarDuracao(total / dias), tamanho = OdTamanhoLeitura.Destaque); if (dias > 1) Text("por dia, em média", style = OdTema.tipografia.uiPequeno, color = OdTema.cores.texto2) }
        FtInfos(null, listOf("Desbloqueios" to d.second.toString()))
        if (d.first.isNotEmpty()) FtPainel { d.first.take(30).forEach { (n, ms) -> FtBarra(n, ms.toFloat(), d.first.first().second.toFloat(), formatarDuracao(ms)) } }
    }
}

private data class ItemPasta(val nome: String, val caminho: String, val bytes: Long, val pasta: Boolean)

private fun tamanho(f: File, limite: IntArray = intArrayOf(200_000)): Long {
    if (limite[0]-- <= 0) return 0
    if (f.isFile) return f.length()
    return f.listFiles()?.sumOf { if (java.nio.file.Files.isSymbolicLink(it.toPath())) 0L else tamanho(it, limite) } ?: 0L
}

@Composable
private fun Armazenamento() {
    val raiz = Environment.getExternalStorageDirectory().absolutePath
    var atual by rememberSaveable { mutableStateOf(raiz) }
    val stat = remember { StatFs(Environment.getDataDirectory().path) }
    val itens by produceState<List<ItemPasta>?>(null, atual) {
        value = null
        value = withContext(Dispatchers.IO) {
            File(atual).listFiles().orEmpty().map { f -> ItemPasta(f.name, f.absolutePath, tamanho(f), f.isDirectory) }.sortedByDescending { it.bytes }
        }
    }
    FtColuna {
        FtPainel(titulo = "Armazenamento interno") {
            FtBarra("Usado", (stat.totalBytes - stat.availableBytes).toFloat(), stat.totalBytes.toFloat(), "${formatarBytes(stat.totalBytes - stat.availableBytes)} de ${formatarBytes(stat.totalBytes)}")
        }
        if (atual != raiz) OdBotao("Subir uma pasta", { atual = File(atual).parent ?: raiz }, Modifier.fillMaxWidth(), icone = OdIcones.Voltar)
        Text(atual.removePrefix(raiz).ifBlank { "/" }, style = OdTema.tipografia.codigo, color = OdTema.cores.texto2)
        val l = itens
        if (l == null) FtNota("Calculando tamanhos…")
        else OdGrupoConfig(null) {
            val max = l.firstOrNull()?.bytes?.coerceAtLeast(1) ?: 1
            l.take(80).forEach { i ->
                if (i.pasta) OdLinhaValor(i.nome, formatarBytes(i.bytes), { atual = i.caminho }, descricao = "${num(i.bytes * 100.0 / max, 0)}% da maior")
                else OdLinhaConfig(i.nome) { Text(formatarBytes(i.bytes), style = OdTema.tipografia.ui) }
            }
        }
        FtNota("Toque numa pasta para entrar. Os arquivos dos apps (pasta Android/data) ficam protegidos pelo sistema e podem não ser contados.")
    }
}

private data class AppInstalado(val nome: String, val pacote: String, val versao: String?, val sistema: Boolean, val instalado: Long, val atualizado: Long, val tamanhoApk: Long, val alvo: Int)

@Composable
private fun GerenciadorApps() {
    val c = LocalContext.current
    var filtro by rememberSaveable { mutableStateOf("usuario") }
    var busca by rememberSaveable { mutableStateOf("") }
    val apps by produceState<List<AppInstalado>?>(null) {
        value = withContext(Dispatchers.IO) {
            c.packageManager.getInstalledPackages(0).mapNotNull { p ->
                val ai = p.applicationInfo ?: return@mapNotNull null
                AppInstalado(c.packageManager.getApplicationLabel(ai).toString(), p.packageName, p.versionName, ai.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                    p.firstInstallTime, p.lastUpdateTime, runCatching { File(ai.sourceDir).length() }.getOrDefault(0), ai.targetSdkVersion)
            }.sortedBy { it.nome.lowercase() }
        }
    }
    FtColuna {
        OdSegmentado(listOf("usuario" to "Instalados", "sistema" to "Do sistema", "todos" to "Todos"), filtro, { filtro = it })
        FtCampo(busca, { busca = it }, "Buscar app")
        val l = apps?.filter { (filtro == "todos" || (filtro == "sistema") == it.sistema) && (busca.isBlank() || it.nome.contains(busca, true) || it.pacote.contains(busca, true)) }
        if (l == null) FtNota("Carregando apps…")
        else OdGrupoConfig("${l.size} apps") {
            l.take(150).forEach { a ->
                OdLinhaValor(a.nome, a.versao?.take(14), {
                    c.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${a.pacote}")))
                }, descricao = "${a.pacote} · APK ${formatarBytes(a.tamanhoApk)} · atualizado ${formatarDataHora(a.atualizado)} · alvo API ${a.alvo}")
            }
        }
        FtNota("Toque num app para abrir os detalhes do Android (armazenamento, permissões, desinstalar, forçar parada).")
    }
}
