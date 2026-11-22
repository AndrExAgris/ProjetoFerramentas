package io.github.andrexagris.ferramentas.base

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Estado do NFC no aparelho. */
enum class EstadoNfc { SemNfc, Desligado, Ligado }

@Composable
fun estadoNfc(): EstadoNfc {
    val a = NfcAdapter.getDefaultAdapter(LocalContext.current)
    return when { a == null -> EstadoNfc.SemNfc; !a.isEnabled -> EstadoNfc.Desligado; else -> EstadoNfc.Ligado }
}

/**
 * Liga o modo leitor do NFC enquanto a tela estiver visível: toda tag aproximada chega em aoLer (numa thread
 * do NFC, fora da principal; pode fazer E/S direto). O sistema não abre outros apps nem toca o som padrão.
 */
@Composable
fun LeitorNfc(aoLer: (Tag) -> Unit) {
    val activity = LocalActivity.current ?: return
    val atual = rememberUpdatedState(aoLer)
    LifecycleResumeEffect(activity) {
        val adaptador = NfcAdapter.getDefaultAdapter(activity)
        val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or NfcAdapter.FLAG_READER_NFC_BARCODE
        adaptador?.enableReaderMode(activity, { tag -> atual.value(tag) }, flags, Bundle().apply { putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250) })
        onPauseOrDispose { runCatching { adaptador?.disableReaderMode(activity) } }
    }
}

fun ByteArray.hex(sep: String = ":"): String = joinToString(sep) { "%02X".format(it) }

fun String.hexParaBytes(): ByteArray {
    val limpo = filter { it.isLetterOrDigit() }
    require(limpo.length % 2 == 0) { "Número ímpar de dígitos hexadecimais." }
    return ByteArray(limpo.length / 2) { limpo.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
