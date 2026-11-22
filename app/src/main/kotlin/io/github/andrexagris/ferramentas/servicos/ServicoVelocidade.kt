package io.github.andrexagris.ferramentas.servicos

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.TrafficStats
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.graphics.drawable.IconCompat
import io.github.andrexagris.ferramentas.base.Canais
import io.github.andrexagris.ferramentas.base.IdsNotificacao
import io.github.andrexagris.ferramentas.base.formatarVelocidade
import io.github.andrexagris.ferramentas.base.intentAbrirApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Velocidade atual da rede (bytes por segundo), compartilhada com a tela e o widget. */
data class Velocidade(val descida: Double, val subida: Double)

/**
 * Mostra a velocidade da internet na barra de status: o ícone pequeno da notificação vira o número
 * (ex.: "4.2 M"), atualizado a cada segundo por TrafficStats. Não usa internet nem permissão especial.
 */
class ServicoVelocidade : Service() {
    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACAO_PARAR) {
            ativo.value = false
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        ServiceCompat.startForeground(this, IdsNotificacao.SERVICO_VELOCIDADE, notificacao(Velocidade(0.0, 0.0)), io.github.andrexagris.ferramentas.base.TIPO_USO_ESPECIAL)
        ativo.value = true
        escopo.coroutineContext[kotlinx.coroutines.Job]?.children?.forEach { it.cancel() }
        escopo.launch {
            var rx = TrafficStats.getTotalRxBytes(); var tx = TrafficStats.getTotalTxBytes(); var t = SystemClock.elapsedRealtime()
            val nm = getSystemService(NotificationManager::class.java)
            while (isActive) {
                delay(1000)
                val rx2 = TrafficStats.getTotalRxBytes(); val tx2 = TrafficStats.getTotalTxBytes(); val t2 = SystemClock.elapsedRealtime()
                val dt = (t2 - t).coerceAtLeast(1) / 1000.0
                val v = Velocidade(((rx2 - rx) / dt).coerceAtLeast(0.0), ((tx2 - tx) / dt).coerceAtLeast(0.0))
                atual.value = v
                nm.notify(IdsNotificacao.SERVICO_VELOCIDADE, notificacao(v))
                rx = rx2; tx = tx2; t = t2
            }
        }
        return START_STICKY
    }

    private fun notificacao(v: Velocidade): Notification = NotificationCompat.Builder(this, Canais.VELOCIDADE)
        .setSmallIcon(IconCompat.createWithBitmap(iconeTexto(v.descida + v.subida)))
        .setContentTitle("↓ ${formatarVelocidade(v.descida)}   ↑ ${formatarVelocidade(v.subida)}")
        .setContentText("Velocidade da internet agora")
        .setOngoing(true).setOnlyAlertOnce(true).setSilent(true).setShowWhen(false)
        .setContentIntent(intentAbrirApp())
        .addAction(0, "Desligar", PendingIntent.getService(this, 2, Intent(this, ServicoVelocidade::class.java).setAction(ACAO_PARAR), PendingIntent.FLAG_IMMUTABLE))
        .build()

    /** Desenha o número em branco num bitmap quadrado (o sistema usa só o alfa do ícone). */
    private fun iconeTexto(bps: Double): Bitmap {
        val bits = bps * 8
        val (valor, unidade) = when {
            bits >= 1e9 -> String.format("%.1f", bits / 1e9) to "Gb"
            bits >= 1e6 -> (if (bits >= 1e7) "%.0f" else "%.1f").format(bits / 1e6) to "Mb"
            else -> "%.0f".format(bits / 1e3) to "kb"
        }
        val lado = 96
        val bmp = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
        p.textSize = if (valor.length <= 3) 58f else 46f
        c.drawText(valor, lado / 2f, 54f, p)
        p.textSize = 36f; p.typeface = Typeface.DEFAULT
        c.drawText(unidade, lado / 2f, 90f, p)
        return bmp
    }

    override fun onDestroy() {
        ativo.value = false
        escopo.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACAO_PARAR = "parar"
        val ativo = MutableStateFlow(false)
        val atual = MutableStateFlow(Velocidade(0.0, 0.0))

        fun iniciar(c: Context) = c.startForegroundService(Intent(c, ServicoVelocidade::class.java))
        fun parar(c: Context) { c.startService(Intent(c, ServicoVelocidade::class.java).setAction(ACAO_PARAR)) }
    }
}
