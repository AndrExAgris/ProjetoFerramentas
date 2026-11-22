package io.github.andrexagris.ferramentas.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.andrexagris.ferramentas.MainActivity
import io.github.andrexagris.ferramentas.R

object Canais {
    const val ALERTAS = "alertas"
    const val MEDICOES = "medicoes"
    const val VELOCIDADE = "velocidade"

    fun criar(c: Context) {
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(ALERTAS, "Alertas", NotificationManager.IMPORTANCE_HIGH).apply { description = "Bateria, franquia de dados, hosts fora do ar e apps novos na rede" },
                NotificationChannel(MEDICOES, "Medições em andamento", NotificationManager.IMPORTANCE_LOW).apply { description = "Testes e registros longos" },
                NotificationChannel(VELOCIDADE, "Velocidade na barra", NotificationManager.IMPORTANCE_LOW).apply { description = "Velocidade da internet ao vivo"; setShowBadge(false) },
            ),
        )
    }
}

fun Context.podeNotificar(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

fun Context.intentAbrirApp(): PendingIntent =
    PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE)

/** Mostra um alerta. Sem permissão de notificação, não faz nada. */
fun Context.notificar(id: Int, titulo: String, texto: String, canal: String = Canais.ALERTAS) {
    if (!podeNotificar()) return
    val n = NotificationCompat.Builder(this, canal)
        .setSmallIcon(R.drawable.ic_notificacao)
        .setContentTitle(titulo)
        .setContentText(texto)
        .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
        .setContentIntent(intentAbrirApp())
        .setAutoCancel(true)
        .build()
    getSystemService(NotificationManager::class.java).notify(id, n)
}

object IdsNotificacao {
    const val BATERIA_NIVEL = 100
    const val BATERIA_CHEIA = 101
    const val BATERIA_TEMPERATURA = 102
    const val FRANQUIA = 110
    const val APP_NOVO = 120
    const val HOST = 130
    const val SERVICO_MEDICAO = 200
    const val SERVICO_VELOCIDADE = 201
    const val SERVICO_VPN = 202
    const val TIMER = 210
}

/** Tipo "uso especial" do serviço em primeiro plano: existe a partir do Android 14; no 13 o serviço fica sem tipo. */
val TIPO_USO_ESPECIAL: Int get() = if (android.os.Build.VERSION.SDK_INT >= 34) android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
