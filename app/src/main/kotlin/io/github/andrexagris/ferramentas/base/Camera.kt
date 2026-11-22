package io.github.andrexagris.ferramentas.base

import android.annotation.SuppressLint
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.util.Range
import android.util.Size
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

/** Controle da câmera aberta: lanterna, zoom e captura de foto. */
class ControleCamera {
    var camera: Camera? = null
        internal set
    var captura: ImageCapture? = null
        internal set

    fun lanterna(ligada: Boolean) { camera?.cameraControl?.enableTorch(ligada) }
    fun zoom(fracao: Float) { camera?.cameraControl?.setLinearZoom(fracao.coerceIn(0f, 1f)) }
    val temFlash: Boolean get() = camera?.cameraInfo?.hasFlashUnit() == true
}

/** Ajustes manuais do Camera2 para quem precisa (exposição curta no detector de cintilação, por exemplo). */
class AjustesCamera2(
    val exposicaoNs: Long? = null,
    val iso: Int? = null,
    val fps: Range<Int>? = null,
    val aoResultado: ((TotalCaptureResult) -> Unit)? = null,
)

/**
 * Pré-visualização da câmera com análise opcional de quadros. A análise recebe cada quadro (YUV) numa
 * thread própria; feche nada: o ImageProxy é fechado aqui depois da chamada.
 * Exige CAMERA (passe pela PortaPermissoes antes).
 */
@SuppressLint("UnsafeOptInUsageError")
@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
@Composable
fun VistaCamera(
    modifier: Modifier = Modifier,
    controle: ControleCamera = remember { ControleCamera() },
    traseira: Boolean = true,
    comCaptura: Boolean = false,
    mostrarPreview: Boolean = true,
    resolucaoAnalise: Size = Size(640, 480),
    ajustes: AjustesCamera2? = null,
    analisar: ((ImageProxy) -> Unit)? = null,
) {
    val contexto = LocalContext.current
    val dono = LocalLifecycleOwner.current
    val vista = remember {
        // COMPATIBLE (TextureView): respeita o recorte do Compose; o SurfaceView vazaria por cima de outros elementos.
        PreviewView(contexto).apply { scaleType = PreviewView.ScaleType.FILL_CENTER; implementationMode = PreviewView.ImplementationMode.COMPATIBLE }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(traseira, comCaptura, analisar != null, ajustes) {
        val futuro = ProcessCameraProvider.getInstance(contexto)
        var provedor: ProcessCameraProvider? = null
        futuro.addListener({
            val p = futuro.get().also { provedor = it }
            val casos = mutableListOf<androidx.camera.core.UseCase>()
            if (mostrarPreview) casos += Preview.Builder().build().also { it.surfaceProvider = vista.surfaceProvider }
            if (analisar != null) {
                val construtor = ImageAnalysis.Builder()
                    .setResolutionSelector(ResolutionSelector.Builder().setResolutionStrategy(ResolutionStrategy(resolucaoAnalise, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)).build())
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                if (ajustes != null) {
                    val ext = Camera2Interop.Extender(construtor)
                    if (ajustes.exposicaoNs != null || ajustes.iso != null) {
                        ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                        ajustes.exposicaoNs?.let { ext.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, it) }
                        ajustes.iso?.let { ext.setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, it) }
                    }
                    ajustes.fps?.let { ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, it) }
                    ajustes.aoResultado?.let { cb ->
                        ext.setSessionCaptureCallback(object : CameraCaptureSession.CaptureCallback() {
                            override fun onCaptureCompleted(s: CameraCaptureSession, r: CaptureRequest, resultado: TotalCaptureResult) = cb(resultado)
                        })
                    }
                }
                casos += construtor.build().also { a ->
                    a.setAnalyzer(executor) { img -> try { analisar(img) } finally { img.close() } }
                }
            }
            if (comCaptura) casos += ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build().also { controle.captura = it }
            val seletor = if (traseira) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
            runCatching {
                p.unbindAll()
                controle.camera = p.bindToLifecycle(dono, seletor, *casos.toTypedArray())
            }
        }, ContextCompat.getMainExecutor(contexto))
        onDispose {
            controle.camera?.cameraControl?.enableTorch(false)
            provedor?.unbindAll()
            controle.camera = null
        }
    }
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }
    if (mostrarPreview) AndroidView({ vista }, modifier.clipToBounds())
}

/** Brilho médio (0 a 255) do plano Y numa região central (fração do lado). */
fun brilhoCentral(img: ImageProxy, fracao: Float = 0.2f): Double {
    val plano = img.planes[0]
    val buf = plano.buffer
    val w = img.width; val h = img.height
    val lado = (minOf(w, h) * fracao).toInt().coerceAtLeast(4)
    val x0 = (w - lado) / 2; val y0 = (h - lado) / 2
    var soma = 0L; var n = 0
    var y = y0
    while (y < y0 + lado) {
        var x = x0
        while (x < x0 + lado) { soma += buf.get(y * plano.rowStride + x * plano.pixelStride).toInt() and 0xFF; n++; x += 2 }
        y += 2
    }
    return soma.toDouble() / n
}

/** Cor média (R, G, B de 0 a 255) de uma região central, convertendo de YUV_420_888. */
fun corCentral(img: ImageProxy, fracao: Float = 0.06f): IntArray {
    val (yP, uP, vP) = img.planes
    val w = img.width; val h = img.height
    val lado = (minOf(w, h) * fracao).toInt().coerceAtLeast(2)
    val x0 = (w - lado) / 2; val y0 = (h - lado) / 2
    var r = 0L; var g = 0L; var b = 0L; var n = 0
    for (y in y0 until y0 + lado) for (x in x0 until x0 + lado) {
        val yy = yP.buffer.get(y * yP.rowStride + x * yP.pixelStride).toInt() and 0xFF
        val uv = (y / 2) * uP.rowStride + (x / 2) * uP.pixelStride
        val u = (uP.buffer.get(uv).toInt() and 0xFF) - 128
        val v = (vP.buffer.get((y / 2) * vP.rowStride + (x / 2) * vP.pixelStride).toInt() and 0xFF) - 128
        r += (yy + 1.402 * v).toInt().coerceIn(0, 255)
        g += (yy - 0.344 * u - 0.714 * v).toInt().coerceIn(0, 255)
        b += (yy + 1.772 * u).toInt().coerceIn(0, 255)
        n++
    }
    return intArrayOf((r / n).toInt(), (g / n).toInt(), (b / n).toInt())
}

/** Inclinação do rolling shutter em ns, se a câmera informar (usado pelo detector de cintilação). */
fun TotalCaptureResult.inclinacaoObturador(): Long? = get(CaptureResult.SENSOR_ROLLING_SHUTTER_SKEW)
