package io.github.andrexagris.ferramentas.ui.ferramentas.camera

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as TelaAndroid
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.googlecode.tesseract.android.TessBaseAPI
import io.github.andrexagris.ferramentas.base.ControleCamera
import io.github.andrexagris.ferramentas.base.Lanterna
import io.github.andrexagris.ferramentas.base.VistaCamera
import io.github.andrexagris.ferramentas.base.bipe
import io.github.andrexagris.ferramentas.base.compartilharArquivo
import io.github.andrexagris.ferramentas.base.compartilharImagem
import io.github.andrexagris.ferramentas.base.compartilharTexto
import io.github.andrexagris.ferramentas.base.copiarTexto
import io.github.andrexagris.ferramentas.base.corCentral
import io.github.andrexagris.ferramentas.base.gerarCodigo
import io.github.andrexagris.ferramentas.base.lerCodigo
import io.github.andrexagris.ferramentas.base.nomeFormato
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotaoMedir
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
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEspacos
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaAcao
import io.github.andrexagris.opendesign.OdLinhaInterruptor
import io.github.andrexagris.opendesign.OdRaios
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.OdTipoToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

fun telasCamera(): Map<String, TelaDeFerramenta> = mapOf(
    "camera.leitor" com { LeitorCodigos() },
    "camera.gerador" com { GeradorCodigos() },
    "camera.cores" com { SeletorCores() },
    "camera.lupa" com { Lupa() },
    "camera.termica" com { OdEstadoRecurso(OdTipoEstado.Indisponivel, "Sem câmera térmica", "Não há API padrão do Android para câmeras térmicas.") },
    "camera.documentos" com { ScannerDocumentos() },
    "camera.ocr" com { Ocr() },
    "camera.estroboscopio" com { Estroboscopio() },
    "camera.lanterna" com { LanternaAvancada() },
)

private fun vibrarCurto(c: Context) {
    runCatching { c.getSystemService(VibratorManager::class.java).defaultVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)) }
}

@Composable
private fun LeitorCodigos() {
    val c = LocalContext.current
    val controle = remember { ControleCamera() }
    var lanterna by remember { mutableStateOf(false) }
    val lidos = remember { mutableStateListOf<Pair<String, String>>() }
    val toasts = LocalToasts.current
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
            VistaCamera(Modifier.fillMaxSize(), controle = controle) { img ->
                lerCodigo(img)?.let { r ->
                    if (lidos.firstOrNull()?.second != r.text) {
                        lidos.add(0, nomeFormato(r.barcodeFormat) to r.text)
                        bipe(1500.0, 60); vibrarCurto(c)
                    }
                }
            }
            val cor = OdTema.cores.escalaReferencia
            Canvas(Modifier.fillMaxSize()) {
                val lado = size.minDimension * 0.65f
                drawRoundRect(cor, Offset((size.width - lado) / 2, (size.height - lado) / 2), androidx.compose.ui.geometry.Size(lado, lado), androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()), style = Stroke(3.dp.toPx()))
            }
        }
        FtColuna {
            OdGrupoConfig(null) { OdLinhaInterruptor("Lanterna", lanterna, { lanterna = it; controle.lanterna(it) }) }
            lidos.firstOrNull()?.let { (formato, texto) ->
                FtPainel(titulo = formato) {
                    Text(texto, style = OdTema.tipografia.uiSubtitulo, color = OdTema.cores.texto)
                    FtBotoes {
                        OdBotao("Copiar", { copiarTexto(c, texto); toasts.mostrar("Copiado.") }, Modifier.weight(1f))
                        if (texto.startsWith("http://") || texto.startsWith("https://")) OdBotao("Abrir link", { c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(texto))) }, Modifier.weight(1f), tipo = OdTipoBotao.Primario)
                        else OdBotao("Compartilhar", { compartilharTexto(c, texto) }, Modifier.weight(1f))
                    }
                }
            }
            if (lidos.size > 1) OdGrupoConfig("Lidos antes") { lidos.drop(1).take(10).forEach { (f, t) -> OdLinhaAcao(t, { copiarTexto(c, t); toasts.mostrar("Copiado.") }, descricao = f) } }
            if (lidos.isEmpty()) FtNota("Aponte para um QR Code ou código de barras. Lê QR, EAN, Code 128, Data Matrix, PDF417 e outros.")
        }
    }
}

@Composable
private fun GeradorCodigos() {
    val c = LocalContext.current
    var texto by rememberSaveable { mutableStateOf("") }
    var formato by rememberSaveable { mutableStateOf(BarcodeFormat.QR_CODE) }
    val bmp = remember(texto, formato) {
        if (texto.isBlank()) null else runCatching { if (formato == BarcodeFormat.QR_CODE) gerarCodigo(texto, formato) else gerarCodigo(texto, formato, 900, 300) }.getOrNull()
    }
    FtColuna {
        FtCampo(texto, { texto = it }, "Texto ou endereço", linhas = 2)
        OdSegmentado(listOf(BarcodeFormat.QR_CODE to "QR", BarcodeFormat.CODE_128 to "Code 128", BarcodeFormat.EAN_13 to "EAN-13", BarcodeFormat.DATA_MATRIX to "Data Matrix"), formato, { formato = it })
        when {
            texto.isBlank() -> FtNota("Digite o conteúdo do código.")
            bmp == null -> FtNota(if (formato == BarcodeFormat.EAN_13) "EAN-13 aceita só 12 ou 13 dígitos." else "Este conteúdo não cabe nesse formato.")
            else -> {
                FtPainel { Image(bmp.asImageBitmap(), "Código gerado", Modifier.fillMaxWidth().aspectRatio(bmp.width.toFloat() / bmp.height).background(Color.White)) }
                OdBotao("Compartilhar imagem", { compartilharImagem(c, "codigo.png", bmp) }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario)
            }
        }
    }
}

private val NOMES_CORES = listOf(
    "Preto" to 0x000000, "Branco" to 0xFFFFFF, "Cinza" to 0x808080, "Prata" to 0xC0C0C0, "Vermelho" to 0xE53935, "Vinho" to 0x7B1F2E, "Rosa" to 0xF48FB1,
    "Pink" to 0xE91E63, "Laranja" to 0xFB8C00, "Pêssego" to 0xFFCBA4, "Amarelo" to 0xFDD835, "Mostarda" to 0xC9A227, "Bege" to 0xE8D8B8, "Marrom" to 0x6D4C41,
    "Caramelo" to 0xAF6E2F, "Verde" to 0x43A047, "Verde-limão" to 0xC0CA33, "Verde-oliva" to 0x6B6B2F, "Verde-escuro" to 0x1B5E20, "Menta" to 0x9FE2BF,
    "Turquesa" to 0x26C6DA, "Azul-petróleo" to 0x00695C, "Azul-claro" to 0x81D4FA, "Azul" to 0x1E88E5, "Azul-marinho" to 0x1A237E, "Roxo" to 0x8E24AA,
    "Lilás" to 0xC8A2C8, "Lavanda" to 0xB39DDB, "Grafite" to 0x3C3F44, "Dourado" to 0xD4AF37, "Creme" to 0xFFF8DC, "Coral" to 0xFF7F50, "Salmão" to 0xFA8072,
)

fun nomeDaCor(r: Int, g: Int, b: Int): String = NOMES_CORES.minBy { (_, v) ->
    val dr = r - (v shr 16 and 255); val dg = g - (v shr 8 and 255); val db = b - (v and 255)
    2 * dr * dr + 4 * dg * dg + 3 * db * db
}.first

@Composable
private fun SeletorCores() {
    val c = LocalContext.current
    var rgb by remember { mutableStateOf(intArrayOf(0, 0, 0)) }
    var congelado by remember { mutableStateOf(false) }
    val toasts = LocalToasts.current
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
            VistaCamera(Modifier.fillMaxSize()) { img -> if (!congelado) rgb = corCentral(img) }
            val cor = OdTema.cores.escalaReferencia
            Canvas(Modifier.fillMaxSize()) { drawCircle(cor, 18.dp.toPx(), Offset(size.width / 2, size.height / 2), style = Stroke(3.dp.toPx())) }
        }
        val hex = "#%02X%02X%02X".format(rgb[0], rgb[1], rgb[2])
        val hsv = FloatArray(3).also { android.graphics.Color.RGBToHSV(rgb[0], rgb[1], rgb[2], it) }
        FtColuna {
            FtPainel {
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                    // A amostra mostra a cor medida: é dado, não decoração da interface.
                    Box(Modifier.size(56.dp).background(Color(rgb[0], rgb[1], rgb[2]), RoundedCornerShape(OdRaios.controle)))
                    Column(Modifier.padding(start = OdEspacos.e4)) {
                        OdLeitura(hex, tamanho = OdTamanhoLeitura.Padrao)
                        Text(nomeDaCor(rgb[0], rgb[1], rgb[2]), style = OdTema.tipografia.uiMedio, color = OdTema.cores.texto2)
                    }
                }
            }
            FtInfos(null, listOf("RGB" to "${rgb[0]}, ${rgb[1]}, ${rgb[2]}", "HSV" to "${hsv[0].toInt()}°, ${(hsv[1] * 100).toInt()}%, ${(hsv[2] * 100).toInt()}%"))
            FtBotoes {
                OdBotao(if (congelado) "Continuar" else "Congelar", { congelado = !congelado }, Modifier.weight(1f), tipo = OdTipoBotao.Primario)
                OdBotao("Copiar HEX", { copiarTexto(c, hex); toasts.mostrar("Copiado.") }, Modifier.weight(1f))
            }
            FtNota("A cor depende da luz do ambiente e do balanço de branco da câmera. Use luz natural e boa iluminação.")
        }
    }
}

@Composable
private fun Lupa() {
    val controle = remember { ControleCamera() }
    var zoom by rememberSaveable { mutableFloatStateOf(0.5f) }
    var luz by remember { mutableStateOf(false) }
    LaunchedEffect(zoom) { delay(300); controle.zoom(zoom) }
    Box(Modifier.fillMaxSize()) {
        VistaCamera(Modifier.fillMaxSize(), controle = controle)
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(OdTema.cores.superficie).padding(OdEspacos.e4)) {
            FtDeslizante("Aumento", zoom, { zoom = it; controle.zoom(it) }, 0f..1f, "${(zoom * 100).toInt()}%")
            OdBotao(if (luz) "Desligar luz" else "Ligar luz", { luz = !luz; controle.lanterna(luz) }, Modifier.fillMaxWidth())
        }
    }
}

/** Foto da câmera como bitmap já girado. */
private fun capturar(c: Context, controle: ControleCamera, aoTerminar: (Bitmap?) -> Unit) {
    val cap = controle.captura ?: return aoTerminar(null)
    cap.takePicture(ContextCompat.getMainExecutor(c), object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(img: ImageProxy) {
            val bmp = img.toBitmap()
            val m = Matrix().apply { postRotate(img.imageInfo.rotationDegrees.toFloat()) }
            val girado = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
            img.close()
            aoTerminar(girado)
        }
        override fun onError(e: ImageCaptureException) = aoTerminar(null)
    })
}

/** Realce para documentos: tons de cinza com mais contraste. */
private fun realcar(b: Bitmap): Bitmap {
    val escala = 1600f / maxOf(b.width, b.height)
    val menor = if (escala < 1) Bitmap.createScaledBitmap(b, (b.width * escala).toInt(), (b.height * escala).toInt(), true) else b
    val saida = Bitmap.createBitmap(menor.width, menor.height, Bitmap.Config.ARGB_8888)
    val cm = ColorMatrix().apply { setSaturation(0f) }
    val contraste = 1.6f; val brilho = -0.3f * 255
    cm.postConcat(ColorMatrix(floatArrayOf(contraste, 0f, 0f, 0f, brilho, 0f, contraste, 0f, 0f, brilho, 0f, 0f, contraste, 0f, brilho, 0f, 0f, 0f, 1f, 0f)))
    TelaAndroid(saida).drawBitmap(menor, 0f, 0f, Paint().apply { colorFilter = ColorMatrixColorFilter(cm) })
    return saida
}

@Composable
private fun ScannerDocumentos() {
    val c = LocalContext.current
    val controle = remember { ControleCamera() }
    val paginas = remember { mutableStateListOf<Bitmap>() }
    var processando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().aspectRatio(3f / 4f)) { VistaCamera(Modifier.fillMaxSize(), controle = controle, comCaptura = true) }
        FtColuna {
            FtBotoes {
                OdBotao("Fotografar página", {
                    processando = true
                    capturar(c, controle) { b -> escopo.launch { b?.let { paginas += withContext(Dispatchers.Default) { realcar(it) } }; processando = false } }
                }, Modifier.weight(1f), tipo = OdTipoBotao.Primario, carregando = processando)
                OdBotao("Gerar PDF (${paginas.size})", {
                    compartilharArquivo(c, "documento-${System.currentTimeMillis()}.pdf", "application/pdf") { f ->
                        val pdf = PdfDocument()
                        paginas.forEachIndexed { i, p ->
                            val pg = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, i + 1).create())
                            val e = minOf(595f / p.width, 842f / p.height)
                            pg.canvas.drawBitmap(p, Matrix().apply { postScale(e, e); postTranslate((595 - p.width * e) / 2, (842 - p.height * e) / 2) }, Paint(Paint.FILTER_BITMAP_FLAG))
                            pdf.finishPage(pg)
                        }
                        f.outputStream().use { pdf.writeTo(it) }
                        pdf.close()
                    }
                }, Modifier.weight(1f), habilitado = paginas.isNotEmpty())
            }
            if (paginas.isNotEmpty()) {
                androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OdEspacos.e2)) {
                    paginas.takeLast(4).forEach { Image(it.asImageBitmap(), "Página", Modifier.height(96.dp).aspectRatio(it.width.toFloat() / it.height)) }
                }
                OdBotao("Descartar páginas", { paginas.clear() }, Modifier.fillMaxWidth())
            }
            FtNota("Fotografe o documento de cima, sobre um fundo escuro e com boa luz. O PDF sai em tamanho A4, uma foto por página.")
        }
    }
}

/** Copia o modelo do Tesseract dos assets na primeira vez. */
private fun prepararTesseract(c: Context): File {
    val pasta = File(c.filesDir, "tesseract/tessdata").apply { mkdirs() }
    val arq = File(pasta, "por.traineddata")
    if (!arq.exists()) c.assets.open("tessdata/por.traineddata").use { i -> arq.outputStream().use { i.copyTo(it) } }
    return pasta.parentFile!!
}

@Composable
private fun Ocr() {
    val c = LocalContext.current
    val controle = remember { ControleCamera() }
    var texto by remember { mutableStateOf<String?>(null) }
    var processando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()
    val toasts = LocalToasts.current
    Column(Modifier.fillMaxSize()) {
        if (texto == null) Box(Modifier.fillMaxWidth().aspectRatio(3f / 4f)) { VistaCamera(Modifier.fillMaxSize(), controle = controle, comCaptura = true) }
        FtColuna {
            if (texto == null) OdBotao("Fotografar e reconhecer", {
                processando = true
                capturar(c, controle) { b ->
                    escopo.launch {
                        texto = withContext(Dispatchers.Default) {
                            runCatching {
                                val base = prepararTesseract(c)
                                val api = TessBaseAPI()
                                try {
                                    api.init(base.absolutePath, "por")
                                    api.setImage(realcar(b ?: error("sem foto")))
                                    api.getUTF8Text().trim()
                                } finally { api.recycle() }
                            }.getOrElse { "" }
                        }
                        processando = false
                        if (texto.isNullOrBlank()) { toasts.mostrar("Nenhum texto reconhecido. Aproxime e use mais luz.", OdTipoToast.Aviso); texto = null }
                    }
                }
            }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario, carregando = processando)
            texto?.let { t ->
                FtPainel { Text(t, style = OdTema.tipografia.ui, color = OdTema.cores.texto) }
                FtBotoes {
                    OdBotao("Copiar", { copiarTexto(c, t); toasts.mostrar("Texto copiado.") }, Modifier.weight(1f), tipo = OdTipoBotao.Primario)
                    OdBotao("Nova foto", { texto = null }, Modifier.weight(1f))
                }
            }
            FtNota("Reconhece texto impresso em português, tudo no aparelho (Tesseract). Fotografe reto, com o texto ocupando a tela e boa luz.")
        }
    }
}

@Composable
private fun Estroboscopio() {
    val c = LocalContext.current
    var hz by rememberSaveable { mutableFloatStateOf(10f) }
    var ligado by remember { mutableStateOf(false) }
    val e = remember { object { @Volatile var hz = 10f } }
    e.hz = hz
    DisposableEffect(ligado) {
        var rodando = ligado
        val t = if (ligado) Thread {
            while (rodando) {
                val meio = (500 / e.hz).toLong()
                Lanterna.definir(c, true); Thread.sleep(meio.coerceAtLeast(5))
                Lanterna.definir(c, false); Thread.sleep(meio.coerceAtLeast(5))
            }
        }.apply { start() } else null
        onDispose { rodando = false; t?.join(300); Lanterna.definir(c, false) }
    }
    FtColuna {
        FtPainel { OdLeitura(io.github.andrexagris.ferramentas.base.num(hz, 1), unidade = "Hz", tamanho = OdTamanhoLeitura.Destaque); OdLeitura(io.github.andrexagris.ferramentas.base.num(hz * 60, 0), unidade = "RPM") }
        FtDeslizante("Frequência", hz, { hz = it }, 1f..25f, "${io.github.andrexagris.ferramentas.base.num(hz, 1)} Hz")
        FtBotoes {
            OdBotao("−0,1", { hz = (hz - 0.1f).coerceAtLeast(1f) }, Modifier.weight(1f))
            OdBotao("+0,1", { hz = (hz + 0.1f).coerceAtMost(25f) }, Modifier.weight(1f))
        }
        FtBotaoMedir(ligado, { ligado = it }, iniciar = "Ligar estroboscópio", parar = "Desligar")
        FtNota("Ilumine algo que gira (ventilador) e ajuste até ele parecer parado: a frequência vira a rotação em RPM. O flash do celular não pisca de forma exata acima de uns 20 Hz.")
    }
}

private val MORSE = mapOf('A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
    'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-", 'U' to "..-",
    'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--", 'Z' to "--..", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-", '5' to ".....",
    '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.", '0' to "-----")

@Composable
private fun LanternaAvancada() {
    val c = LocalContext.current
    val ligada by Lanterna.ligada.collectAsState()
    val maximo = remember { Lanterna.nivelMaximo(c) }
    var nivel by rememberSaveable { mutableFloatStateOf(maximo.toFloat()) }
    var mensagem by rememberSaveable { mutableStateOf("SOS") }
    var transmitindo by remember { mutableStateOf(false) }
    LaunchedEffect(transmitindo) {
        if (!transmitindo) return@LaunchedEffect
        val ponto = 200L
        while (transmitindo) {
            for (ch in mensagem.uppercase().replace('Ç', 'C')) {
                if (!transmitindo) break
                if (ch == ' ') { delay(ponto * 7); continue }
                val codigo = MORSE[java.text.Normalizer.normalize(ch.toString(), java.text.Normalizer.Form.NFD)[0]] ?: continue
                for (s in codigo) { Lanterna.definir(c, true); delay(if (s == '.') ponto else ponto * 3); Lanterna.definir(c, false); delay(ponto) }
                delay(ponto * 2)
            }
            delay(ponto * 7)
        }
        Lanterna.definir(c, false)
    }
    DisposableEffect(Unit) { onDispose { Lanterna.definir(c, false) } }
    FtColuna {
        OdBotao(if (ligada) "Desligar lanterna" else "Ligar lanterna", { if (ligada) Lanterna.definir(c, false) else Lanterna.intensidade(c, nivel.toInt()) }, Modifier.fillMaxWidth().height(72.dp), tipo = OdTipoBotao.Primario)
        if (maximo > 1) FtDeslizante("Intensidade", nivel, { nivel = it; if (ligada) Lanterna.intensidade(c, it.toInt()) }, 1f..maximo.toFloat(), "${nivel.toInt()} de $maximo", passos = (maximo - 2).coerceAtLeast(0))
        else FtNota("Este aparelho não permite ajustar a intensidade do flash.")
        FtCampo(mensagem, { mensagem = it }, "Mensagem em código Morse")
        FtBotaoMedir(transmitindo, { transmitindo = it }, iniciar = "Transmitir em Morse", parar = "Parar transmissão")
        FtNota("Também há o bloco \"Lanterna\" no painel rápido e um widget de lanterna para a tela inicial.")
    }
}
