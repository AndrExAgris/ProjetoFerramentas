package io.github.andrexagris.ferramentas.ui.ferramentas.medicao

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.andrexagris.ferramentas.base.VistaCamera
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.rememberSensor
import io.github.andrexagris.ferramentas.dados.rememberAjusteFloat
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotoes
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtDeslizante
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.LocalToasts
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.ferramentas.ui.ferramentas.numero
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdNivelBolha
import io.github.andrexagris.opendesign.OdRaios
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.odFormatar
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.tan

fun telasMedicao(): Map<String, TelaDeFerramenta> = mapOf(
    "medicao.regua" com { Regua() },
    "medicao.paquimetro" com { Paquimetro() },
    "medicao.transferidor" com { Transferidor() },
    "medicao.nivel" com { Nivel() },
    "medicao.inclinometro" com { InclinometroCamera() },
    "medicao.altura" com { AlturaTrigonometria() },
    "medicao.distancia-camera" com { DistanciaCamera() },
    "medicao.trena-ar" com { TrenaAr() },
    "medicao.trena-profundidade" com { TrenaProfundidade() },
    "medicao.scanner-3d" com { Scanner3d() },
)

private const val CHAVE_CALIBRACAO = "medicao.regua.fator"

/** Pixels por milímetro reais, com a calibração feita na régua (cartão de 85,6 mm). */
@Composable
fun rememberPxPorMm(): Float {
    val fator by rememberAjusteFloat(CHAVE_CALIBRACAO, 1f)
    val dm = LocalContext.current.resources.displayMetrics
    return dm.ydpi / 25.4f * fator
}

@Composable
private fun Regua() {
    var calibrando by rememberSaveable { mutableStateOf(false) }
    var fator by rememberAjusteFloat(CHAVE_CALIBRACAO, 1f)
    var polegadas by rememberSaveable { mutableStateOf(false) }
    val pxMm = rememberPxPorMm()
    val c = OdTema.cores
    val medidor = rememberTextMeasurer()
    val estilo = OdTema.tipografia.uiPequeno.copy(fontSize = 11.sp, color = c.escalaMarca)
    var cursor by remember { mutableFloatStateOf(300f) }
    if (calibrando) {
        val densidade = LocalDensity.current
        FtColuna {
            FtNota("Encoste um cartão de banco ou documento (85,6 mm) no retângulo e ajuste até a largura bater.")
            Box(Modifier.width(with(densidade) { (85.6f * pxMm).toDp() }).height(with(densidade) { (53.98f * pxMm).toDp() })
                .background(c.primaria.fundo, RoundedCornerShape(OdRaios.painel)))
            FtDeslizante("Ajuste", fator, { fator = it }, 0.85f..1.15f, "${num((fator - 1) * 100, 1)}%")
            OdBotao("Concluir calibração", { calibrando = false }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario)
        }
        return
    }
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { ch, _ -> cursor = ch.position.y } }
            .semantics { contentDescription = "Régua. Cursor em ${num(cursor / pxMm, 1)} milímetros" }) {
            val passo = if (polegadas) pxMm * 25.4f / 16 else pxMm
            var i = 0
            while (i * passo < size.height) {
                val y = i * passo
                val (comp, rotulo) = if (polegadas) when { i % 16 == 0 -> 34.dp.toPx() to (i / 16).toString(); i % 8 == 0 -> 24.dp.toPx() to null; i % 4 == 0 -> 16.dp.toPx() to null; else -> 9.dp.toPx() to null }
                else when { i % 10 == 0 -> 34.dp.toPx() to (i / 10).toString(); i % 5 == 0 -> 22.dp.toPx() to null; else -> 12.dp.toPx() to null }
                drawLine(if (rotulo != null) c.escalaMarca else c.escalaMarcaMenor, Offset(0f, y), Offset(comp, y), (if (rotulo != null) 1.5f else 1f).dp.toPx())
                if (rotulo != null && i > 0) { val t = medidor.measure(rotulo, estilo); drawText(t, topLeft = Offset(comp + 4.dp.toPx(), y - t.size.height / 2)) }
                i++
            }
            drawLine(c.escalaReferencia, Offset(0f, cursor), Offset(size.width, cursor), 2.dp.toPx())
        }
        Column(Modifier.align(Alignment.CenterEnd).padding(OdEspacos.e4), horizontalAlignment = Alignment.End) {
            OdLeitura(if (polegadas) num(cursor / pxMm / 25.4, 2) else num(cursor / pxMm / 10, 1), unidade = if (polegadas) "pol" else "cm", tamanho = OdTamanhoLeitura.Destaque)
            OdSegmentado(listOf(false to "cm", true to "pol"), polegadas, { polegadas = it }, Modifier.width(140.dp))
            OdBotao("Calibrar", { calibrando = true }, Modifier.padding(top = OdEspacos.e2))
        }
    }
}

@Composable
private fun Paquimetro() {
    val pxMm = rememberPxPorMm()
    var a by remember { mutableFloatStateOf(200f) }
    var b by remember { mutableFloatStateOf(500f) }
    val c = OdTema.cores
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize().pointerInput(Unit) {
            detectDragGestures { ch, _ -> val y = ch.position.y; if (abs(y - a) < abs(y - b)) a = y else b = y }
        }.semantics { contentDescription = "Paquímetro: ${num(abs(b - a) / pxMm, 1)} milímetros" }) {
            drawRect(c.primaria.fundo, Offset(0f, minOf(a, b)), Size(size.width, abs(b - a)))
            listOf(a, b).forEach { y -> drawLine(c.escalaReferencia, Offset(0f, y), Offset(size.width, y), 2.dp.toPx()) }
        }
        Column(Modifier.align(Alignment.Center).background(c.superficie, RoundedCornerShape(OdRaios.painel)).padding(OdEspacos.e4), horizontalAlignment = Alignment.CenterHorizontally) {
            OdLeitura(num(abs(b - a) / pxMm, 1), unidade = "mm", tamanho = OdTamanhoLeitura.Destaque)
            OdLeitura(num(abs(b - a) / pxMm / 25.4, 3), unidade = "pol")
        }
        FtNota("Coloque o objeto sobre a tela e arraste as duas linhas até as bordas dele. Calibre na Régua para mais precisão.", Modifier.align(Alignment.BottomCenter).padding(OdEspacos.e4))
    }
}

@Composable
private fun Transferidor() {
    var angulo by remember { mutableFloatStateOf(45f) }
    val c = OdTema.cores
    val medidor = rememberTextMeasurer()
    val estilo = OdTema.tipografia.uiPequeno.copy(fontSize = 10.sp, color = c.escalaMarca)
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize().pointerInput(Unit) {
            detectDragGestures { ch, _ ->
                val centro = Offset(0f, size.height / 2f)
                val d = ch.position - centro
                angulo = Math.toDegrees(atan2(-d.y.toDouble(), d.x.toDouble())).toFloat().coerceIn(-90f, 90f) + 90f
            }
        }.semantics { contentDescription = "Transferidor: ${angulo.toInt()} graus" }) {
            val centro = Offset(0f, size.height / 2)
            val raio = minOf(size.width * 0.95f, size.height / 2 * 0.95f)
            drawArc(c.escalaTrilho, -90f, 180f, false, Offset(-raio, centro.y - raio), Size(raio * 2, raio * 2), style = Stroke(1.dp.toPx()))
            for (g in 0..180) {
                val a = Math.toRadians(g - 90.0)
                val comp = when { g % 10 == 0 -> 22.dp.toPx(); g % 5 == 0 -> 14.dp.toPx(); else -> 7.dp.toPx() }
                val p1 = Offset(centro.x + raio * kotlin.math.cos(a).toFloat(), centro.y - raio * kotlin.math.sin(a).toFloat())
                val p2 = Offset(centro.x + (raio - comp) * kotlin.math.cos(a).toFloat(), centro.y - (raio - comp) * kotlin.math.sin(a).toFloat())
                drawLine(if (g % 10 == 0) c.escalaMarca else c.escalaMarcaMenor, p1, p2, 1.dp.toPx())
                if (g % 30 == 0) {
                    val t = medidor.measure(g.toString(), estilo)
                    val p3 = Offset(centro.x + (raio - 36.dp.toPx()) * kotlin.math.cos(a).toFloat(), centro.y - (raio - 36.dp.toPx()) * kotlin.math.sin(a).toFloat())
                    drawText(t, topLeft = Offset(p3.x - t.size.width / 2, p3.y - t.size.height / 2))
                }
            }
            val a = Math.toRadians(angulo - 90.0)
            drawLine(c.escalaReferencia, centro, Offset(centro.x + raio * kotlin.math.cos(a).toFloat(), centro.y - raio * kotlin.math.sin(a).toFloat()), 2.dp.toPx())
            drawLine(c.escalaPonteiro, Offset(0f, centro.y - raio), Offset(0f, centro.y + raio), 2.dp.toPx())
        }
        Column(Modifier.align(Alignment.CenterEnd).padding(OdEspacos.e4)) { OdLeitura(odFormatar(angulo.toDouble(), 0), unidade = "°", tamanho = OdTamanhoLeitura.Destaque, largura = 4) }
    }
}

@Composable
private fun Nivel() {
    val g by rememberSensor(Sensor.TYPE_GRAVITY, SensorManager.SENSOR_DELAY_GAME)
    val acc by rememberSensor(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME)
    val v = g ?: acc
    var zeroX by rememberAjusteFloat("medicao.nivel.zero-x", 0f)
    var zeroY by rememberAjusteFloat("medicao.nivel.zero-y", 0f)
    val x = v?.let { Math.toDegrees(atan2(it[0].toDouble(), it[2].toDouble())).toFloat() } ?: 0f
    val y = v?.let { Math.toDegrees(atan2(it[1].toDouble(), it[2].toDouble())).toFloat() } ?: 0f
    val deitado = v?.let { abs(it[2]) > 6f } ?: true
    // Em pé (na borda): ângulo em relação à vertical/horizontal.
    val borda = v?.let { Math.toDegrees(atan2(it[0].toDouble(), it[1].toDouble())).toFloat() } ?: 0f
    FtColuna {
        if (deitado) {
            OdNivelBolha(x - zeroX, -(y - zeroY), Modifier.fillMaxWidth(), vibrarAoEntrar = true)
            FtInfos(null, listOf("Inclinação lateral" to "${num(x - zeroX, 1)}°", "Inclinação frontal" to "${num(y - zeroY, 1)}°"))
        } else {
            FtPainel(titulo = "Na borda") { OdLeitura(odFormatar(borda.toDouble(), 1), unidade = "°", tamanho = OdTamanhoLeitura.Destaque) }
        }
        FtBotoes {
            OdBotao("Zerar aqui", { zeroX = x; zeroY = y }, Modifier.weight(1f))
            OdBotao("Zero de fábrica", { zeroX = 0f; zeroY = 0f }, Modifier.weight(1f))
        }
        FtNota("Deitado, mede as duas direções. Em pé sobre a borda, mede o ângulo da superfície. Para calibrar, apoie numa superfície plana, toque em \"Zerar aqui\", gire 180° e confira.")
    }
}

/** Elevação da direção da câmera traseira (graus, positivo acima do horizonte) a partir do acelerômetro. */
fun elevacaoCamera(a: FloatArray): Float {
    val n = sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]).coerceAtLeast(0.1f)
    return Math.toDegrees(asin((-a[2] / n).toDouble().coerceIn(-1.0, 1.0))).toFloat()
}

@Composable
private fun MiraCamera(texto: String, conteudo: @Composable () -> Unit = {}) {
    val c = OdTema.cores
    Box(Modifier.fillMaxSize()) {
        VistaCamera(Modifier.fillMaxSize())
        Canvas(Modifier.fillMaxSize()) {
            drawLine(c.escalaReferencia, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 2.dp.toPx())
            drawCircle(c.escalaReferencia, 14.dp.toPx(), Offset(size.width / 2, size.height / 2), style = Stroke(2.dp.toPx()))
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(c.superficie).padding(OdEspacos.e4)) {
            OdLeitura(texto, tamanho = OdTamanhoLeitura.Destaque)
            conteudo()
        }
    }
}

@Composable
private fun InclinometroCamera() {
    val a by rememberSensor(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME)
    val elev = a?.let { elevacaoCamera(it.valores) } ?: 0f
    val rolagem = a?.let { Math.toDegrees(atan2(it[0].toDouble(), it[1].toDouble())).toFloat() } ?: 0f
    MiraCamera("${odFormatar(elev.toDouble(), 1)}°") {
        FtInfos(null, listOf("Inclinação (para cima/baixo)" to "${num(elev, 1)}°", "Giro lateral" to "${num(rolagem, 1)}°"))
    }
}

@Composable
private fun AlturaTrigonometria() {
    val a by rememberSensor(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME)
    var distancia by rememberSaveable { mutableStateOf("10") }
    var base by rememberSaveable { mutableStateOf<Float?>(null) }
    var topo by rememberSaveable { mutableStateOf<Float?>(null) }
    val elev = a?.let { elevacaoCamera(it.valores) } ?: 0f
    val d = distancia.numero()
    val altura = if (d != null && base != null && topo != null) d * (tan(Math.toRadians(topo!!.toDouble())) - tan(Math.toRadians(base!!.toDouble()))) else null
    MiraCamera(altura?.let { "${num(it, 2)} m" } ?: "${odFormatar(elev.toDouble(), 1)}°") {
        FtCampo(distancia, { distancia = it }, "Distância até o objeto", numerico = true, sufixo = "m")
        FtBotoes {
            OdBotao(base?.let { "Base: ${num(it, 1)}°" } ?: "Mirar na base", { base = elev }, Modifier.weight(1f))
            OdBotao(topo?.let { "Topo: ${num(it, 1)}°" } ?: "Mirar no topo", { topo = elev }, Modifier.weight(1f), tipo = OdTipoBotao.Primario)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DistanciaCamera() {
    val ctx = LocalContext.current
    val vfov = remember {
        runCatching {
            val cm = ctx.getSystemService(CameraManager::class.java)
            val id = cm.cameraIdList.first { cm.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK }
            val ch = cm.getCameraCharacteristics(id)
            val tam = ch.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)!!
            val f = ch.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)!!.first()
            2 * atan(maxOf(tam.width, tam.height) / (2 * f.toDouble()))
        }.getOrDefault(Math.toRadians(65.0))
    }
    var tamanho by rememberSaveable { mutableStateOf("1.7") }
    var cima by remember { mutableFloatStateOf(0.35f) }
    var baixo by remember { mutableFloatStateOf(0.65f) }
    val c = OdTema.cores
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            VistaCamera(Modifier.fillMaxSize())
            Canvas(Modifier.fillMaxSize().pointerInput(Unit) {
                detectDragGestures { ch, _ -> val f = ch.position.y / size.height; if (abs(f - cima) < abs(f - baixo)) cima = f else baixo = f }
            }) {
                listOf(cima, baixo).forEach { f -> drawLine(c.escalaReferencia, Offset(0f, f * size.height), Offset(size.width, f * size.height), 2.dp.toPx()) }
            }
        }
        val fracao = abs(baixo - cima).coerceAtLeast(0.01f)
        val h = tamanho.numero()
        val dist = h?.let { it / (2 * fracao * tan(vfov / 2)) }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(c.superficie).padding(OdEspacos.e4)) {
            OdLeitura(dist?.let { "${num(it, 1)} m" } ?: "—", tamanho = OdTamanhoLeitura.Destaque)
            FtCampo(tamanho, { tamanho = it }, "Altura real do objeto", numerico = true, sufixo = "m")
            FtNota("Arraste as linhas até o topo e a base do objeto (uma pessoa tem cerca de 1,7 m; uma porta, 2,1 m).")
        }
    }
}

@Composable
private fun TrenaAr() {
    val ctx = LocalContext.current
    val instalado = remember { runCatching { ctx.packageManager.getPackageInfo("com.google.ar.core", 0); true }.getOrDefault(false) }
    FtColuna {
        OdEstadoRecurso(
            if (instalado) OdTipoEstado.Medindo else OdTipoEstado.Indisponivel,
            if (instalado) "ARCore instalado" else "ARCore não instalado",
            if (instalado) "Este aparelho tem os Serviços do Google Play para RA. A trena em realidade aumentada chega numa versão futura; por enquanto use a Distância pela câmera."
            else "Instale \"Serviços do Google Play para RA\" na Play Store, se o aparelho estiver na lista de compatíveis.",
        )
    }
}

/** Lê a distância no centro de uma câmera de profundidade (DEPTH16) em mm. */
@SuppressLint("MissingPermission")
@Composable
private fun TrenaProfundidade() {
    val ctx = LocalContext.current
    var mm by remember { mutableStateOf<Int?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) {
        val cm = ctx.getSystemService(CameraManager::class.java)
        val thread = HandlerThread("profundidade").apply { start() }
        val h = Handler(thread.looper)
        var dispositivo: CameraDevice? = null
        runCatching {
            val id = cm.cameraIdList.first { cm.getCameraCharacteristics(it).get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT) == true }
            val tam = cm.getCameraCharacteristics(id).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.DEPTH16).first()
            val leitor = ImageReader.newInstance(tam.width, tam.height, ImageFormat.DEPTH16, 2)
            leitor.setOnImageAvailableListener({ r ->
                r.acquireLatestImage()?.use { img ->
                    val p = img.planes[0]; val buf = p.buffer.asShortBuffer()
                    val v = buf.get((img.height / 2) * (p.rowStride / 2) + img.width / 2).toInt()
                    mm = (v and 0x1FFF).takeIf { it > 0 }
                }
            }, h)
            cm.openCamera(id, object : CameraDevice.StateCallback() {
                override fun onOpened(d: CameraDevice) {
                    dispositivo = d
                    @Suppress("DEPRECATION")
                    d.createCaptureSession(listOf(leitor.surface), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(s: CameraCaptureSession) {
                            val req = d.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply { addTarget(leitor.surface) }.build()
                            s.setRepeatingRequest(req, null, h)
                        }
                        override fun onConfigureFailed(s: CameraCaptureSession) { erro = "A câmera de profundidade não iniciou." }
                    }, h)
                }
                override fun onDisconnected(d: CameraDevice) { d.close() }
                override fun onError(d: CameraDevice, e: Int) { erro = "Erro $e na câmera de profundidade."; d.close() }
            }, h)
        }.onFailure { erro = "Não foi possível abrir a câmera de profundidade." }
        onDispose { dispositivo?.close(); thread.quitSafely() }
    }
    FtColuna {
        erro?.let { OdEstadoRecurso(OdTipoEstado.Indisponivel, "Falhou", it) }
        FtPainel(titulo = "Distância no centro") { OdLeitura(mm?.let { num(it / 1000.0, 2) } ?: "—", unidade = "m", tamanho = OdTamanhoLeitura.Destaque) }
        FtNota("Aponte a câmera para a superfície. O sensor de profundidade (ToF) mede até uns 4 a 5 metros.")
    }
}

@Composable
private fun Scanner3d() {
    FtColuna {
        OdEstadoRecurso(OdTipoEstado.Medindo, "Sensor de profundidade encontrado", "O scanner 3D (nuvem de pontos e exportação em PLY) chega numa versão futura. Por enquanto, use a Trena por sensor de profundidade.")
    }
}

