package io.github.andrexagris.ferramentas.base

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Gera um código (QR, EAN, Code 128…) como bitmap preto no branco, com margem. */
fun gerarCodigo(texto: String, formato: BarcodeFormat = BarcodeFormat.QR_CODE, largura: Int = 800, altura: Int = largura): Bitmap {
    val dicas = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.MARGIN to 2) +
        if (formato == BarcodeFormat.QR_CODE) mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M) else emptyMap()
    val m = MultiFormatWriter().encode(texto, formato, largura, altura, dicas)
    val px = IntArray(m.width * m.height) { i -> if (m.get(i % m.width, i / m.width)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt() }
    return Bitmap.createBitmap(px, m.width, m.height, Bitmap.Config.ARGB_8888)
}

/** Texto do QR de Wi-Fi que câmeras e o Android entendem. */
fun textoQrWifi(ssid: String, senha: String, seguranca: String, oculta: Boolean): String {
    fun esc(s: String) = s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace(":", "\\:").replace("\"", "\\\"")
    return "WIFI:T:$seguranca;S:${esc(ssid)};" + (if (seguranca != "nopass") "P:${esc(senha)};" else "") + "H:$oculta;;"
}

private val leitor = MultiFormatReader().apply {
    setHints(mapOf(DecodeHintType.TRY_HARDER to true, DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.entries.toList()))
}

/** Procura um código num quadro da câmera (plano Y). null se não achar. */
fun lerCodigo(img: ImageProxy): Result? {
    val p = img.planes[0]
    val buf = p.buffer
    val dados = ByteArray(buf.remaining()).also { buf.get(it) }
    val fonte = PlanarYUVLuminanceSource(dados, p.rowStride, img.height, 0, 0, img.width, img.height, false)
    return synchronized(leitor) {
        runCatching { leitor.decodeWithState(BinaryBitmap(HybridBinarizer(fonte))) }.recoverCatching {
            leitor.decodeWithState(BinaryBitmap(HybridBinarizer(fonte.invert())))
        }.getOrNull().also { leitor.reset() }
    }
}

fun nomeFormato(f: BarcodeFormat): String = when (f) {
    BarcodeFormat.QR_CODE -> "QR Code"; BarcodeFormat.EAN_13 -> "EAN-13"; BarcodeFormat.EAN_8 -> "EAN-8"; BarcodeFormat.CODE_128 -> "Code 128"
    BarcodeFormat.CODE_39 -> "Code 39"; BarcodeFormat.UPC_A -> "UPC-A"; BarcodeFormat.UPC_E -> "UPC-E"; BarcodeFormat.DATA_MATRIX -> "Data Matrix"
    BarcodeFormat.PDF_417 -> "PDF417"; BarcodeFormat.AZTEC -> "Aztec"; BarcodeFormat.ITF -> "ITF"; BarcodeFormat.CODABAR -> "Codabar"
    else -> f.name
}
