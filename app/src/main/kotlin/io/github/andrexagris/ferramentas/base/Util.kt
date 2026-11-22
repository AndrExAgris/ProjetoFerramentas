package io.github.andrexagris.ferramentas.base

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

val localeBr: Locale = Locale.forLanguageTag("pt-BR")

fun copiarTexto(contexto: Context, texto: String, rotulo: String = "Ferramentas") {
    contexto.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(rotulo, texto))
}

/** Salva um arquivo no cache e abre o "Compartilhar" do Android (Salvar no Drive, Arquivos, e-mail…). */
fun compartilharArquivo(contexto: Context, nome: String, mime: String, escrever: (File) -> Unit) {
    val pasta = File(contexto.cacheDir, "exportar").apply { mkdirs() }
    val arquivo = File(pasta, nome)
    escrever(arquivo)
    val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.arquivos", arquivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    contexto.startActivity(Intent.createChooser(intent, "Exportar $nome").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Exporta medições em CSV (separador vírgula, decimal com ponto, UTF-8), o formato que planilhas abrem direto. */
fun exportarCsv(contexto: Context, nomeBase: String, cabecalho: List<String>, linhas: List<List<Any?>>) {
    val nome = "$nomeBase-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())}.csv"
    compartilharArquivo(contexto, nome, "text/csv") { f ->
        f.bufferedWriter().use { w ->
            w.appendLine(cabecalho.joinToString(",") { csv(it) })
            linhas.forEach { l -> w.appendLine(l.joinToString(",") { csv(it) }) }
        }
    }
}

private fun csv(v: Any?): String {
    val s = when (v) {
        null -> ""
        is Float -> if (v.isNaN()) "" else v.toString()
        is Double -> if (v.isNaN()) "" else v.toString()
        else -> v.toString()
    }
    return if (s.any { it == ',' || it == '"' || it == '\n' }) "\"" + s.replace("\"", "\"\"") + "\"" else s
}

fun compartilharImagem(contexto: Context, nome: String, bitmap: Bitmap) =
    compartilharArquivo(contexto, nome, "image/png") { f -> f.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }

fun compartilharTexto(contexto: Context, texto: String) {
    contexto.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, texto), null))
}

/** Mantém a tela ligada enquanto o composable estiver na tela (medições ao vivo). */
@Composable
fun ManterTelaLigada() {
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

/** "1,2 MB", "834 kB", "3,40 GB" (base 1000, como o Android mostra). */
fun formatarBytes(bytes: Long): String {
    if (bytes < 1000) return "$bytes B"
    val exp = (ln(bytes.toDouble()) / ln(1000.0)).toInt().coerceAtMost(5)
    val v = bytes / 1000.0.pow(exp)
    val unidade = "kMGTP"[exp - 1] + "B"
    return String.format(localeBr, if (v >= 100) "%.0f %s" else if (v >= 10) "%.1f %s" else "%.2f %s", v, unidade)
}

/** "123 kb/s", "4,5 Mb/s" a partir de bytes por segundo. */
fun formatarVelocidade(bytesPorSegundo: Double): String {
    val bits = bytesPorSegundo * 8
    return when {
        bits >= 1e9 -> String.format(localeBr, "%.2f Gb/s", bits / 1e9)
        bits >= 1e6 -> String.format(localeBr, "%.1f Mb/s", bits / 1e6)
        bits >= 1e3 -> String.format(localeBr, "%.0f kb/s", bits / 1e3)
        else -> String.format(localeBr, "%.0f b/s", bits)
    }
}

/** "1 h 05 min", "12 min 30 s", "45 s". */
fun formatarDuracao(ms: Long): String {
    val s = abs(ms) / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    val seg = s % 60
    return when {
        h > 0 -> String.format(localeBr, "%d h %02d min", h, m)
        m > 0 -> String.format(localeBr, "%d min %02d s", m, seg)
        else -> "$seg s"
    }
}

/** Cronômetro "mm:ss,d" ou "h:mm:ss". */
fun formatarCronometro(ms: Long, decimos: Boolean = true): String {
    val total = ms.coerceAtLeast(0)
    val h = total / 3_600_000
    val m = (total / 60_000) % 60
    val s = (total / 1000) % 60
    val d = (total / 100) % 10
    return if (h > 0) String.format(localeBr, "%d:%02d:%02d", h, m, s)
    else if (decimos) String.format(localeBr, "%02d:%02d,%d", m, s, d) else String.format(localeBr, "%02d:%02d", m, s)
}

fun formatarDataHora(ms: Long): String = SimpleDateFormat("dd/MM HH:mm", localeBr).format(Date(ms))
fun formatarHora(ms: Long): String = SimpleDateFormat("HH:mm:ss", localeBr).format(Date(ms))

/** Número com vírgula decimal e casas fixas, sem os espaços de alinhamento do odFormatar. */
fun num(v: Double, casas: Int = 1): String = if (v.isNaN() || v.isInfinite()) "—" else String.format(localeBr, "%.${casas}f", v)
fun num(v: Float, casas: Int = 1): String = num(v.toDouble(), casas)
