package io.github.andrexagris.ferramentas.servicos

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import io.github.andrexagris.ferramentas.R
import io.github.andrexagris.ferramentas.base.Canais
import io.github.andrexagris.ferramentas.base.IdsNotificacao
import io.github.andrexagris.ferramentas.base.cargaMah
import io.github.andrexagris.ferramentas.base.correnteMa
import io.github.andrexagris.ferramentas.base.estadoBateriaAgora
import io.github.andrexagris.ferramentas.base.formatarDuracao
import io.github.andrexagris.ferramentas.base.intentAbrirApp
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.dados.BancoDados
import io.github.andrexagris.ferramentas.dados.ItemSalvo
import io.github.andrexagris.ferramentas.dados.Tipos
import io.github.andrexagris.ferramentas.ui.ferramentas.sinais.CelulaLida
import io.github.andrexagris.ferramentas.ui.ferramentas.sinais.lerCelulas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Ponto de um trajeto ou do log de sinal. */
data class PontoTrajeto(val tempo: Long, val lat: Double, val lon: Double, val alt: Double?, val precisao: Float, val velocidade: Float?)

data class RegistroCelular(val tempo: Long, val lat: Double?, val lon: Double?, val celula: CelulaLida?)

/** Resultado do teste de consumo em standby. */
data class ResultadoStandby(val duracaoMs: Long, val nivelInicio: Float, val nivelFim: Float, val mahGastos: Float?, val correnteMediaMa: Float?) {
    val porcentoHora: Float get() = (nivelInicio - nivelFim) / (duracaoMs / 3_600_000f)
    val autonomiaH: Float? get() = porcentoHora.takeIf { it > 0.01f }?.let { 100f / it }
}

/** O que o serviço está fazendo agora, observado pelas telas. */
sealed interface TarefaMedicao {
    data object Nenhuma : TarefaMedicao
    data class Standby(val inicio: Long, val duracaoMs: Long, val amostras: Int, val resultado: ResultadoStandby? = null) : TarefaMedicao
    data class Trajeto(val inicio: Long, val pontos: List<PontoTrajeto>, val distanciaM: Double) : TarefaMedicao
    data class LogCelular(val inicio: Long, val registros: List<RegistroCelular>) : TarefaMedicao
}

/**
 * Serviço em primeiro plano para medições longas, que precisam continuar com a tela apagada
 * (o Doze espaçaria as leituras de um app comum). Uma tarefa por vez.
 */
class ServicoMedicao : Service() {
    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var trabalho: Job? = null
    private var ouvinteLocal: LocationListener? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACAO_STANDBY -> iniciarStandby(intent.getLongExtra(EXTRA_DURACAO, 10 * 60_000L))
            ACAO_TRAJETO -> iniciarTrajeto()
            ACAO_LOG_CELULAR -> iniciarLogCelular()
            ACAO_PARAR -> parar()
        }
        return START_NOT_STICKY
    }

    /** Uma tarefa por vez: iniciar outra encerra a anterior. */
    private fun encerrarTarefaAtual() {
        trabalho?.cancel(); trabalho = null
        ouvinteLocal?.let { getSystemService(LocationManager::class.java).removeUpdates(it) }
        ouvinteLocal = null
    }

    private fun notificacao(texto: String): Notification = NotificationCompat.Builder(this, Canais.MEDICOES)
        .setSmallIcon(R.drawable.ic_notificacao).setContentTitle("Medição em andamento").setContentText(texto)
        .setOngoing(true).setOnlyAlertOnce(true).setContentIntent(intentAbrirApp())
        .addAction(0, "Parar", android.app.PendingIntent.getService(this, 1, Intent(this, ServicoMedicao::class.java).setAction(ACAO_PARAR), android.app.PendingIntent.FLAG_IMMUTABLE))
        .build()

    private fun primeiroPlano(texto: String, tipo: Int) {
        ServiceCompat.startForeground(this, IdsNotificacao.SERVICO_MEDICAO, notificacao(texto), tipo)
    }

    private fun atualizarNotificacao(texto: String) {
        getSystemService(android.app.NotificationManager::class.java).notify(IdsNotificacao.SERVICO_MEDICAO, notificacao(texto))
    }

    private fun iniciarStandby(duracaoMs: Long) {
        encerrarTarefaAtual()
        primeiroPlano("Teste de standby: apague a tela.", io.github.andrexagris.ferramentas.base.TIPO_USO_ESPECIAL)
        val inicio = System.currentTimeMillis()
        val inicioRelogio = SystemClock.elapsedRealtime()
        val nivel0 = estadoBateriaAgora()?.nivel ?: 0f
        val carga0 = cargaMah()
        estado.value = TarefaMedicao.Standby(inicio, duracaoMs, 0)
        trabalho = escopo.launch {
            val correntes = mutableListOf<Float>()
            while (isActive && SystemClock.elapsedRealtime() - inicioRelogio < duracaoMs) {
                delay(15_000)
                correnteMa()?.let { correntes += it }
                estado.value = TarefaMedicao.Standby(inicio, duracaoMs, correntes.size)
                atualizarNotificacao("Teste de standby: faltam ${formatarDuracao(duracaoMs - (SystemClock.elapsedRealtime() - inicioRelogio))}.")
            }
            val dur = SystemClock.elapsedRealtime() - inicioRelogio
            val carga1 = cargaMah()
            val r = ResultadoStandby(
                dur, nivel0, estadoBateriaAgora()?.nivel ?: nivel0,
                if (carga0 != null && carga1 != null) carga0 - carga1 else null,
                correntes.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            )
            BancoDados.de(this@ServicoMedicao).itens().salvar(
                ItemSalvo.de(Tipos.STANDBY, "Standby de ${formatarDuracao(dur)}", mapOf(
                    "duracao" to dur, "nivelInicio" to nivel0, "nivelFim" to r.nivelFim, "mah" to r.mahGastos, "corrente" to r.correnteMediaMa,
                )),
            )
            estado.value = TarefaMedicao.Standby(inicio, duracaoMs, correntes.size, r)
            getSystemService(android.app.NotificationManager::class.java).notify(
                IdsNotificacao.SERVICO_MEDICAO + 1,
                NotificationCompat.Builder(this@ServicoMedicao, Canais.ALERTAS).setSmallIcon(R.drawable.ic_notificacao)
                    .setContentTitle("Teste de standby concluído").setContentText("${num(r.porcentoHora, 2)} %/hora").setContentIntent(intentAbrirApp()).setAutoCancel(true).build(),
            )
            pararServico()
        }
    }

    @SuppressLint("MissingPermission")
    private fun iniciarTrajeto() {
        encerrarTarefaAtual()
        primeiroPlano("Gravando trajeto.", ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        val inicio = System.currentTimeMillis()
        estado.value = TarefaMedicao.Trajeto(inicio, emptyList(), 0.0)
        val lm = getSystemService(LocationManager::class.java)
        val o = LocationListener { l: Location ->
            val atual = estado.value as? TarefaMedicao.Trajeto ?: return@LocationListener
            if (l.accuracy > 30) return@LocationListener
            val p = PontoTrajeto(l.time, l.latitude, l.longitude, if (l.hasAltitude()) l.altitude else null, l.accuracy, if (l.hasSpeed()) l.speed else null)
            val anterior = atual.pontos.lastOrNull()
            val d = anterior?.let { a -> FloatArray(1).also { Location.distanceBetween(a.lat, a.lon, p.lat, p.lon, it) }[0].toDouble() } ?: 0.0
            val soma = if (anterior == null || d >= 2.0) atual.distanciaM + d else atual.distanciaM
            estado.value = atual.copy(pontos = if (anterior == null || d >= 2.0) atual.pontos + p else atual.pontos, distanciaM = soma)
            atualizarNotificacao("Trajeto: ${num(soma / 1000, 2)} km")
        }
        ouvinteLocal = o
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, o, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    private fun iniciarLogCelular() {
        encerrarTarefaAtual()
        primeiroPlano("Registrando sinal celular.", ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        val inicio = System.currentTimeMillis()
        estado.value = TarefaMedicao.LogCelular(inicio, emptyList())
        val lm = getSystemService(LocationManager::class.java)
        var ultima: Location? = null
        val o = LocationListener { ultima = it }
        ouvinteLocal = o
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 0f, o, Looper.getMainLooper())
        val tm = getSystemService(TelephonyManager::class.java)
        trabalho = escopo.launch {
            while (isActive) {
                val cel = runCatching { lerCelulas(tm).firstOrNull { it.registrada } }.getOrNull()
                val atual = estado.value as? TarefaMedicao.LogCelular ?: break
                estado.value = atual.copy(registros = atual.registros + RegistroCelular(System.currentTimeMillis(), ultima?.latitude, ultima?.longitude, cel))
                atualizarNotificacao("Sinal celular: ${atual.registros.size + 1} registros")
                delay(5000)
            }
        }
    }

    private fun parar() {
        val e = estado.value
        if (e is TarefaMedicao.Standby && e.resultado == null) estado.value = TarefaMedicao.Nenhuma
        pararServico()
    }

    private fun pararServico() {
        encerrarTarefaAtual()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        ouvinteLocal?.let { getSystemService(LocationManager::class.java).removeUpdates(it) }
        escopo.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACAO_STANDBY = "standby"
        const val ACAO_TRAJETO = "trajeto"
        const val ACAO_LOG_CELULAR = "log-celular"
        const val ACAO_PARAR = "parar"
        const val EXTRA_DURACAO = "duracao"

        val estado = MutableStateFlow<TarefaMedicao>(TarefaMedicao.Nenhuma)
        val estadoAtual: StateFlow<TarefaMedicao> get() = estado

        fun iniciar(c: Context, acao: String, duracaoMs: Long? = null) {
            c.startForegroundService(Intent(c, ServicoMedicao::class.java).setAction(acao).apply { duracaoMs?.let { putExtra(EXTRA_DURACAO, it) } })
        }

        fun parar(c: Context) { c.startService(Intent(c, ServicoMedicao::class.java).setAction(ACAO_PARAR)) }

        /** Limpa o resultado mostrado (depois de a pessoa ver). */
        fun limpar() { estado.value = TarefaMedicao.Nenhuma }
    }
}
