package io.github.andrexagris.ferramentas.base

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.currentCoroutineContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

const val TAXA_AUDIO = 44100

/**
 * Blocos de áudio do microfone, em amostras de −1 a 1. Usa a fonte UNPROCESSED quando o aparelho tem
 * (sem controle automático de ganho, o melhor para medir), senão VOICE_RECOGNITION.
 * Exige RECORD_AUDIO (a ferramenta passa pela PortaPermissoes antes).
 */
@SuppressLint("MissingPermission")
fun fluxoMicrofone(tamanhoBloco: Int = 4096, taxa: Int = TAXA_AUDIO): Flow<FloatArray> = flow {
    val minimo = AudioRecord.getMinBufferSize(taxa, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
    val fonte = MediaRecorder.AudioSource.UNPROCESSED
    val gravador = runCatching {
        AudioRecord(fonte, taxa, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, maxOf(minimo, tamanhoBloco * 4 * 2))
            .takeIf { it.state == AudioRecord.STATE_INITIALIZED }
    }.getOrNull() ?: AudioRecord(
        MediaRecorder.AudioSource.VOICE_RECOGNITION, taxa, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, maxOf(minimo, tamanhoBloco * 4 * 2),
    )
    try {
        gravador.startRecording()
        while (currentCoroutineContext().isActive) {
            val bloco = FloatArray(tamanhoBloco)
            var lidos = 0
            while (lidos < tamanhoBloco) {
                val n = gravador.read(bloco, lidos, tamanhoBloco - lidos, AudioRecord.READ_BLOCKING)
                if (n <= 0) break
                lidos += n
            }
            emit(bloco)
        }
    } finally {
        runCatching { gravador.stop() }
        gravador.release()
    }
}.flowOn(Dispatchers.IO)

fun rms(x: FloatArray): Double {
    var s = 0.0
    for (v in x) s += v * v
    return sqrt(s / x.size)
}

/** dB em relação ao fundo de escala (0 dBFS = máximo do microfone). */
fun dbfs(rms: Double): Double = 20 * log10(rms.coerceAtLeast(1e-9))

/** FFT radix-2 no lugar (re e im com tamanho potência de 2). */
fun fft(re: DoubleArray, im: DoubleArray) {
    val n = re.size
    var j = 0
    for (i in 1 until n) {
        var bit = n shr 1
        while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
        j = j xor bit
        if (i < j) {
            var t = re[i]; re[i] = re[j]; re[j] = t
            t = im[i]; im[i] = im[j]; im[j] = t
        }
    }
    var len = 2
    while (len <= n) {
        val ang = -2 * PI / len
        val wr = cos(ang); val wi = sin(ang)
        var i = 0
        while (i < n) {
            var cr = 1.0; var ci = 0.0
            for (k in 0 until len / 2) {
                val ar = re[i + k + len / 2] * cr - im[i + k + len / 2] * ci
                val ai = re[i + k + len / 2] * ci + im[i + k + len / 2] * cr
                re[i + k + len / 2] = re[i + k] - ar; im[i + k + len / 2] = im[i + k] - ai
                re[i + k] += ar; im[i + k] += ai
                val ncr = cr * wr - ci * wi
                ci = cr * wi + ci * wr; cr = ncr
            }
            i += len
        }
        len = len shl 1
    }
}

/** Espectro de magnitude (metade útil) com janela de Hann. Tamanho da entrada: potência de 2. */
fun espectro(x: FloatArray): DoubleArray {
    val n = x.size
    val re = DoubleArray(n) { x[it] * (0.5 - 0.5 * cos(2 * PI * it / (n - 1))) }
    val im = DoubleArray(n)
    fft(re, im)
    return DoubleArray(n / 2) { sqrt(re[it] * re[it] + im[it] * im[it]) / (n / 4.0) }
}

/** Ganho da ponderação A (IEC 61672) em dB, numa frequência. */
fun ponderacaoA(f: Double): Double {
    if (f <= 0) return -200.0
    val f2 = f * f
    val ra = (12194.0.pow(2) * f2 * f2) / ((f2 + 20.6.pow(2)) * sqrt((f2 + 107.7.pow(2)) * (f2 + 737.9.pow(2))) * (f2 + 12194.0.pow(2)))
    return 20 * log10(ra) + 2.0
}

/** Nível em dBFS com ponderação A, a partir do espectro de um bloco. */
fun dbfsPonderadoA(espectro: DoubleArray, taxa: Int = TAXA_AUDIO): Double {
    val n = espectro.size * 2
    var energia = 0.0
    for (k in 1 until espectro.size) {
        val f = k.toDouble() * taxa / n
        val g = 10.0.pow(ponderacaoA(f) / 10)
        energia += espectro[k] * espectro[k] * g / 2
    }
    return 10 * log10(energia.coerceAtLeast(1e-18))
}

/** Frequência fundamental pelo algoritmo YIN (de Cheveigné e Kawahara). null quando não há nota clara. */
fun yin(x: FloatArray, taxa: Int = TAXA_AUDIO, limiar: Double = 0.12): Double? {
    val metade = x.size / 2
    val d = DoubleArray(metade)
    for (tau in 1 until metade) {
        var s = 0.0
        for (i in 0 until metade) { val dif = x[i] - x[i + tau]; s += dif * dif }
        d[tau] = s
    }
    val cmnd = DoubleArray(metade)
    cmnd[0] = 1.0
    var soma = 0.0
    for (tau in 1 until metade) {
        soma += d[tau]
        cmnd[tau] = if (soma == 0.0) 1.0 else d[tau] * tau / soma
    }
    val minTau = taxa / 2000
    var tau = minTau.coerceAtLeast(2)
    while (tau < metade) {
        if (cmnd[tau] < limiar) {
            while (tau + 1 < metade && cmnd[tau + 1] < cmnd[tau]) tau++
            val t0 = tau - 1; val t2 = (tau + 1).coerceAtMost(metade - 1)
            val s0 = cmnd[t0]; val s1 = cmnd[tau]; val s2 = cmnd[t2]
            val denom = 2 * (2 * s1 - s2 - s0)
            val melhor = if (denom != 0.0) tau + (s2 - s0) / denom else tau.toDouble()
            return taxa / melhor
        }
        tau++
    }
    return null
}

private val NOMES_NOTAS = listOf("Dó", "Dó♯", "Ré", "Ré♯", "Mi", "Fá", "Fá♯", "Sol", "Sol♯", "Lá", "Lá♯", "Si")

/** Nota mais próxima: nome, oitava, frequência ideal e desvio em cents (lá4 = referência). */
data class Nota(val nome: String, val oitava: Int, val alvo: Double, val cents: Double)

fun notaDe(freq: Double, la4: Double = 440.0): Nota {
    val semitons = 12 * log2(freq / la4)
    val n = semitons.roundToInt()
    val alvo = la4 * 2.0.pow(n / 12.0)
    val indice = ((n + 9) % 12 + 12) % 12
    val oitava = 4 + Math.floorDiv(n + 9, 12)
    return Nota(NOMES_NOTAS[indice], oitava, alvo, 1200 * log2(freq / alvo))
}

/**
 * Saída de áudio contínua: chama gerar(buffer, taxa) para preencher cada bloco de amostras estéreo
 * intercaladas (esquerda, direita). Pare com parar().
 */
class SaidaAudio(private val taxa: Int = TAXA_AUDIO, private val gerar: (FloatArray) -> Unit) {
    @Volatile private var rodando = false
    private var thread: Thread? = null

    fun iniciar() {
        if (rodando) return
        rodando = true
        thread = Thread {
            val tamanho = AudioTrack.getMinBufferSize(taxa, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT).coerceAtLeast(4096)
            val faixa = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(taxa).setEncoding(AudioFormat.ENCODING_PCM_FLOAT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
                .setBufferSizeInBytes(tamanho * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            val bloco = FloatArray(1024 * 2)
            faixa.play()
            while (rodando) {
                gerar(bloco)
                faixa.write(bloco, 0, bloco.size, AudioTrack.WRITE_BLOCKING)
            }
            faixa.stop(); faixa.release()
        }.apply { priority = Thread.MAX_PRIORITY; start() }
    }

    fun parar() {
        rodando = false
        thread?.join(500)
        thread = null
    }
}

/** Toca um som curto (bipe) de forma síncrona numa thread própria. */
fun bipe(freq: Double = 1000.0, duracaoMs: Int = 80, volume: Float = 0.6f) {
    Thread {
        val n = TAXA_AUDIO * duracaoMs / 1000
        val dados = FloatArray(n) { i ->
            val env = minOf(1f, i / 200f, (n - i) / 200f)
            (sin(2 * PI * freq * i / TAXA_AUDIO) * volume * env).toFloat()
        }
        val t = AudioTrack.Builder()
            .setAudioFormat(AudioFormat.Builder().setSampleRate(TAXA_AUDIO).setEncoding(AudioFormat.ENCODING_PCM_FLOAT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(n * 4).setTransferMode(AudioTrack.MODE_STATIC).build()
        t.write(dados, 0, n, AudioTrack.WRITE_BLOCKING)
        t.play()
        Thread.sleep(duracaoMs + 50L)
        t.release()
    }.start()
}

/** Pico (índice e magnitude) de um espectro entre duas frequências. */
fun picoEspectro(esp: DoubleArray, taxa: Int, fMin: Double, fMax: Double): Pair<Double, Double> {
    val n = esp.size * 2
    val i0 = (fMin * n / taxa).toInt().coerceAtLeast(1)
    val i1 = (fMax * n / taxa).toInt().coerceAtMost(esp.size - 1)
    var melhor = i0
    for (i in i0..i1) if (esp[i] > esp[melhor]) melhor = i
    return melhor.toDouble() * taxa / n to esp[melhor]
}

