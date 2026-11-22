package io.github.andrexagris.ferramentas

import android.app.Application
import io.github.andrexagris.ferramentas.base.Canais
import io.github.andrexagris.ferramentas.base.Lanterna
import io.github.andrexagris.ferramentas.catalogo.Disponibilidade
import io.github.andrexagris.ferramentas.catalogo.VerificadorDisponibilidade
import io.github.andrexagris.ferramentas.dados.Ajustes
import io.github.andrexagris.ferramentas.dados.PreferenciasApp
import io.github.andrexagris.ferramentas.servicos.ChavesPeriodicas
import io.github.andrexagris.ferramentas.servicos.TrabalhoPeriodico
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Guarda o que o app inteiro compartilha. Sem biblioteca de injeção: o app é pequeno o bastante. */
class FerramentasApp : Application() {
    val preferencias by lazy { PreferenciasApp(this) }

    /** Disponibilidade de cada ferramenta neste aparelho, por id. */
    val disponibilidade: Map<String, Disponibilidade> by lazy { VerificadorDisponibilidade(this).verificarTodas() }

    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Canais.criar(this)
        Lanterna.observar(this)
        // A tarefa periódica só é agendada se alguma ferramenta que depende dela estiver ligada.
        escopo.launch {
            val algumaLigada = listOf(ChavesPeriodicas.HISTORICO_BATERIA, ChavesPeriodicas.ALARME_CHEIA, ChavesPeriodicas.HISTORICO_PRESSAO, ChavesPeriodicas.APP_NOVO, ChavesPeriodicas.HOSTS)
                .any { Ajustes.bool(this@FerramentasApp, it, false) } ||
                listOf(ChavesPeriodicas.ALARME_NIVEL, ChavesPeriodicas.ALARME_TEMPERATURA, ChavesPeriodicas.FRANQUIA_GB).any { Ajustes.float(this@FerramentasApp, it, 0f) > 0f }
            if (algumaLigada) TrabalhoPeriodico.agendar(this@FerramentasApp)
        }
    }
}
