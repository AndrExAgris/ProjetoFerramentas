package io.github.andrexagris.ferramentas.servicos

import android.content.Context
import android.hardware.Sensor
import android.net.NetworkCapabilities
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.andrexagris.ferramentas.base.IdsNotificacao
import io.github.andrexagris.ferramentas.base.correnteMa
import io.github.andrexagris.ferramentas.base.estadoBateriaAgora
import io.github.andrexagris.ferramentas.base.fluxoSensor
import io.github.andrexagris.ferramentas.base.formatarBytes
import io.github.andrexagris.ferramentas.base.nomeDoUid
import io.github.andrexagris.ferramentas.base.notificar
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.bytesAparelho
import io.github.andrexagris.ferramentas.base.bytesPorUid
import io.github.andrexagris.ferramentas.base.tcpAberta
import io.github.andrexagris.ferramentas.base.temPermissao
import io.github.andrexagris.ferramentas.base.temSensor
import io.github.andrexagris.ferramentas.catalogo.Permissao
import io.github.andrexagris.ferramentas.dados.Ajustes
import io.github.andrexagris.ferramentas.dados.Amostra
import io.github.andrexagris.ferramentas.dados.BancoDados
import io.github.andrexagris.ferramentas.dados.ItemSalvo
import io.github.andrexagris.ferramentas.dados.Series
import io.github.andrexagris.ferramentas.dados.Tipos
import io.github.andrexagris.ferramentas.dados.dataStore
import io.github.andrexagris.ferramentas.widgets.WidgetBateria
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import androidx.datastore.preferences.core.edit
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Chaves dos ajustes das tarefas periódicas (as telas das ferramentas gravam, o worker lê). */
object ChavesPeriodicas {
    const val HISTORICO_BATERIA = "bateria.historico.ativo"
    const val ALARME_NIVEL = "bateria.alarmes.nivel"          // % (0 = desligado)
    const val ALARME_CHEIA = "bateria.alarmes.cheia"
    const val ALARME_TEMPERATURA = "bateria.alarmes.temperatura" // °C (0 = desligado)
    const val HISTORICO_PRESSAO = "ambiente.tendencia.ativo"
    const val FRANQUIA_GB = "internet.franquia.limite"         // GB (0 = desligado)
    const val FRANQUIA_DIA = "internet.franquia.dia"           // dia do mês em que o plano renova
    const val APP_NOVO = "internet.app-novo.ativo"
    const val HOSTS = "internet.monitor-hosts.ativo"
    const val ESTADO_AVISOS = "periodico.avisos"               // conjunto de avisos já dados (para não repetir)
    const val APPS_CONHECIDOS = "internet.app-novo.conhecidos"
}

/**
 * Uma única tarefa periódica (a cada 15 minutos, o mínimo do WorkManager) faz tudo que precisa rodar com o
 * app fechado. Cada parte só roda se a pessoa ligou na ferramenta correspondente.
 */
class TrabalhoPeriodico(contexto: Context, params: WorkerParameters) : CoroutineWorker(contexto, params) {
    private val c = applicationContext
    private val banco = BancoDados.de(c)

    override suspend fun doWork(): Result {
        val avisos = c.dataStore.data.first()[stringSetPreferencesKey(ChavesPeriodicas.ESTADO_AVISOS)] ?: emptySet()
        val novos = avisos.toMutableSet()
        runCatching { bateria(novos) }
        runCatching { pressao() }
        runCatching { franquia(novos) }
        runCatching { appsNovos() }
        runCatching { hosts() }
        runCatching { WidgetBateria().updateAll(c) }
        if (novos != avisos) c.dataStore.edit { it[stringSetPreferencesKey(ChavesPeriodicas.ESTADO_AVISOS)] = novos }
        banco.amostras().apagarAntigas(System.currentTimeMillis() - 90L * 24 * 3600 * 1000)
        return Result.success()
    }

    private suspend fun bateria(avisos: MutableSet<String>) {
        val e = c.estadoBateriaAgora() ?: return
        if (Ajustes.bool(c, ChavesPeriodicas.HISTORICO_BATERIA, false)) {
            banco.amostras().inserir(Amostra(serie = Series.BATERIA, tempo = System.currentTimeMillis(), v1 = e.nivel.toDouble(), v2 = e.temperatura.toDouble(),
                v3 = c.correnteMa(e)?.toDouble() ?: Double.NaN, v4 = e.voltagem.toDouble()))
        }
        val limite = Ajustes.float(c, ChavesPeriodicas.ALARME_NIVEL, 0f)
        if (limite > 0 && !e.carregando && e.nivel <= limite) {
            if (avisos.add("nivel")) c.notificar(IdsNotificacao.BATERIA_NIVEL, "Bateria em ${e.nivel.toInt()}%", "Chegou ao limite de ${limite.toInt()}% que você definiu.")
        } else if (e.carregando) avisos.remove("nivel")
        if (Ajustes.bool(c, ChavesPeriodicas.ALARME_CHEIA, false) && e.carregando && e.nivel >= 100f) {
            if (avisos.add("cheia")) c.notificar(IdsNotificacao.BATERIA_CHEIA, "Bateria carregada", "Já pode tirar da tomada.")
        } else if (!e.conectado) avisos.remove("cheia")
        val temp = Ajustes.float(c, ChavesPeriodicas.ALARME_TEMPERATURA, 0f)
        if (temp > 0 && e.temperatura >= temp) {
            if (avisos.add("temperatura")) c.notificar(IdsNotificacao.BATERIA_TEMPERATURA, "Bateria quente: ${num(e.temperatura, 1)} °C", "Deixe o celular esfriar, longe do sol e sem carregar.")
        } else if (e.temperatura < temp - 3) avisos.remove("temperatura")
    }

    private suspend fun pressao() {
        if (!Ajustes.bool(c, ChavesPeriodicas.HISTORICO_PRESSAO, false) || !c.temSensor(Sensor.TYPE_PRESSURE)) return
        val l = withTimeoutOrNull(5000) { c.fluxoSensor(Sensor.TYPE_PRESSURE).first() } ?: return
        banco.amostras().inserir(Amostra(serie = Series.PRESSAO, tempo = System.currentTimeMillis(), v1 = l[0].toDouble()))
    }

    private suspend fun franquia(avisos: MutableSet<String>) {
        val gb = Ajustes.float(c, ChavesPeriodicas.FRANQUIA_GB, 0f)
        if (gb <= 0 || !c.temPermissao(Permissao.UsoDoApp)) return
        val inicio = inicioCicloFranquia(Ajustes.float(c, ChavesPeriodicas.FRANQUIA_DIA, 1f).toInt())
        val usado = c.bytesAparelho(true, inicio)
        val chave = "franquia-$inicio"
        if (usado >= gb * 1e9 * 0.9 && avisos.add(chave)) {
            c.notificar(IdsNotificacao.FRANQUIA, "Franquia de dados quase no fim", "Você usou ${formatarBytes(usado)} de ${num(gb, 1)} GB neste ciclo.")
        }
    }

    private suspend fun appsNovos() {
        if (!Ajustes.bool(c, ChavesPeriodicas.APP_NOVO, false) || !c.temPermissao(Permissao.UsoDoApp)) return
        val chave = stringSetPreferencesKey(ChavesPeriodicas.APPS_CONHECIDOS)
        val conhecidos = c.dataStore.data.first()[chave]
        val inicio = System.currentTimeMillis() - 7L * 24 * 3600 * 1000
        val uids = (c.bytesPorUid(NetworkCapabilities.TRANSPORT_WIFI, inicio).keys + c.bytesPorUid(NetworkCapabilities.TRANSPORT_CELLULAR, inicio).keys).map { it.toString() }.toSet()
        if (conhecidos == null) { c.dataStore.edit { it[chave] = uids }; return } // primeira vez: só aprende
        val novos = uids - conhecidos
        novos.forEach { uid ->
            val (nome, _) = c.nomeDoUid(uid.toInt())
            c.notificar(IdsNotificacao.APP_NOVO + (uid.toInt() % 1000), "App novo usando a internet", "$nome começou a usar a rede.")
        }
        if (novos.isNotEmpty()) c.dataStore.edit { it[chave] = conhecidos + novos }
    }

    private suspend fun hosts() {
        if (!Ajustes.bool(c, ChavesPeriodicas.HOSTS, false)) return
        banco.itens().listar(Tipos.HOST).forEach { item ->
            val campos = item.campos()
            val porta = campos["porta"]?.toIntOrNull() ?: 443
            val ms = tcpAberta(item.nome, porta, 3000)
            val ok = ms != null
            banco.amostras().inserir(Amostra(serie = Series.HOST, tempo = System.currentTimeMillis(), v1 = if (ok) 1.0 else 0.0, v2 = ms?.toDouble() ?: Double.NaN, texto = item.nome))
            val antes = campos["ok"]
            if (antes != null && antes != ok.toString()) {
                c.notificar(IdsNotificacao.HOST + (item.id % 100).toInt(), if (ok) "${item.nome} voltou" else "${item.nome} fora do ar",
                    if (ok) "Respondeu na porta $porta em $ms ms." else "Não respondeu na porta $porta.")
            }
            banco.itens().salvar(ItemSalvo.de(Tipos.HOST, item.nome, campos + ("ok" to ok.toString()) + ("verificado" to System.currentTimeMillis().toString()), item.id))
        }
    }

    companion object {
        private const val NOME = "periodico"

        /** Agenda a tarefa (idempotente). Chamado ao abrir o app e quando um ajuste periódico muda. */
        fun agendar(c: Context) {
            val pedido = PeriodicWorkRequestBuilder<TrabalhoPeriodico>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(c).enqueueUniquePeriodicWork(NOME, ExistingPeriodicWorkPolicy.KEEP, pedido)
        }

        fun inicioCicloFranquia(dia: Int): Long {
            val cal = Calendar.getInstance()
            val hoje = cal.get(Calendar.DAY_OF_MONTH)
            if (hoje < dia) cal.add(Calendar.MONTH, -1)
            cal.set(Calendar.DAY_OF_MONTH, dia.coerceAtMost(cal.getActualMaximum(Calendar.DAY_OF_MONTH)))
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
