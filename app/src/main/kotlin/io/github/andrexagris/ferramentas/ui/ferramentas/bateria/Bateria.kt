package io.github.andrexagris.ferramentas.ui.ferramentas.bateria

import android.app.usage.UsageStatsManager
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.andrexagris.ferramentas.base.MinMax
import io.github.andrexagris.ferramentas.base.Serie
import io.github.andrexagris.ferramentas.base.cargaMah
import io.github.andrexagris.ferramentas.base.correnteMa
import io.github.andrexagris.ferramentas.base.estadoBateriaAgora
import io.github.andrexagris.ferramentas.base.exportarCsv
import io.github.andrexagris.ferramentas.base.formatarDataHora
import io.github.andrexagris.ferramentas.base.formatarDuracao
import io.github.andrexagris.ferramentas.base.modeloBateria
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.rememberBateria
import io.github.andrexagris.ferramentas.base.rememberComPermissao
import io.github.andrexagris.ferramentas.base.rememberCorrente
import io.github.andrexagris.ferramentas.base.rotulo
import io.github.andrexagris.ferramentas.base.textoFonte
import io.github.andrexagris.ferramentas.base.textoSaude
import io.github.andrexagris.ferramentas.base.textoStatus
import io.github.andrexagris.ferramentas.catalogo.Permissao
import io.github.andrexagris.ferramentas.dados.BancoDados
import io.github.andrexagris.ferramentas.dados.ItemSalvo
import io.github.andrexagris.ferramentas.dados.Series
import io.github.andrexagris.ferramentas.dados.Tipos
import io.github.andrexagris.ferramentas.dados.rememberAjusteBool
import io.github.andrexagris.ferramentas.dados.rememberAjusteFloat
import io.github.andrexagris.ferramentas.servicos.ChavesPeriodicas
import io.github.andrexagris.ferramentas.servicos.ServicoMedicao
import io.github.andrexagris.ferramentas.servicos.TarefaMedicao
import io.github.andrexagris.ferramentas.servicos.TrabalhoPeriodico
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBarra
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotaoMedir
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtDeslizante
import io.github.andrexagris.ferramentas.ui.ferramentas.FtGraficoHistorico
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.LocalToasts
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdCartaoLeitura
import io.github.andrexagris.opendesign.OdChipEstado
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdGrade
import io.github.andrexagris.opendesign.OdGraficoTempoReal
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaAcao
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdLinhaInterruptor
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoChip
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.odFormatar
import io.github.andrexagris.opendesign.odSerieResultante
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

fun telasBateria(): Map<String, TelaDeFerramenta> = mapOf(
    "bateria.painel" com { PainelBateria() },
    "bateria.corrente" com { CorrentePotencia() },
    "bateria.tempo-restante" com { TempoRestante() },
    "bateria.tecnologia" com { TecnologiaSaude() },
    "bateria.capacidade" com { CapacidadeEstimada() },
    "bateria.ciclos" com { Ciclos() },
    "bateria.saude-oficial" com { SaudeOficial() },
    "bateria.standby".com(emptyList()) { TesteStandby() },
    "bateria.carregador" com { TesteCarregador() },
    "bateria.historico" com { HistoricoBateria() },
    "bateria.alarmes".com(emptyList()) { AlarmesBateria() },
    "bateria.por-app" com { ConsumoPorApp() },
)

@Composable
private fun PainelBateria() {
    val e by rememberBateria()
    val corrente by rememberCorrente()
    val b = e ?: return
    FtColuna {
        OdGrade(comMargem = false) {
            celula(compacta = 4) {
                OdCartaoLeitura("Nível", odFormatar(b.nivel.toDouble(), 0), unidade = "%", largura = 4, icone = OdIcones.Bateria, detalhe = textoStatus(b.status),
                    chip = { OdChipEstado(OdTipoChip.Medindo, "Ao vivo") })
            }
            celula(compacta = 2) { OdCartaoLeitura("Temperatura", odFormatar(b.temperatura.toDouble(), 1), unidade = "°C", largura = 5) }
            celula(compacta = 2) { OdCartaoLeitura("Voltagem", odFormatar(b.voltagem.toDouble(), 3), unidade = "V", largura = 6) }
            celula(compacta = 2) { OdCartaoLeitura("Corrente", corrente?.let { odFormatar(it.toDouble(), 0, largura = 5) } ?: "—", unidade = "mA", largura = 5) }
            celula(compacta = 2) {
                OdCartaoLeitura("Potência", corrente?.let { odFormatar((it * b.voltagem / 1000).toDouble(), 2, largura = 6) } ?: "—", unidade = "W", largura = 6)
            }
        }
        FtInfos("Estado", listOf("Fonte" to textoFonte(b.fonte), "Saúde" to textoSaude(b.saude), "Tecnologia" to b.tecnologia))
        FtNota("Corrente positiva entrando na bateria, negativa saindo. O app corrige a unidade e o sinal que cada fabricante usa.")
    }
}

@Composable
private fun CorrentePotencia() {
    val c = LocalContext.current
    val e by rememberBateria()
    val serie = remember { Serie(120) }
    val mm = remember { MinMax() }
    val registro = remember { mutableStateListOf<List<Any?>>() }
    var atual by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            val ma = c.correnteMa()
            atual = ma
            if (ma != null) { serie.add(ma); mm.add(ma); registro += listOf(System.currentTimeMillis(), ma, c.estadoBateriaAgora()?.voltagem) }
            delay(500)
        }
    }
    val v = e?.voltagem ?: 0f
    FtColuna {
        FtPainel {
            OdLeitura(atual?.let { odFormatar(it.toDouble(), 0, largura = 6) } ?: "—", unidade = "mA", largura = 6, tamanho = OdTamanhoLeitura.Destaque)
            OdLeitura(atual?.let { odFormatar((it * v / 1000).toDouble(), 2, largura = 6) } ?: "—", unidade = "W", largura = 6)
        }
        val limite = (serie.pontos.maxOfOrNull { abs(it) } ?: 1000f).coerceAtLeast(500f) * 1.2f
        OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "mA")), -limite, limite, janela = 120, unidade = "mA", casas = 0, rotuloTempo = "−60 s" to "agora")
        FtInfos("Desde que abriu", listOf(
            "Mínima" to (if (mm.min.value.isNaN()) null else "${num(mm.min.value, 0)} mA"),
            "Máxima" to (if (mm.max.value.isNaN()) null else "${num(mm.max.value, 0)} mA"),
            "Média" to serie.pontos.takeIf { it.isNotEmpty() }?.average()?.let { "${num(it, 0)} mA" },
        ))
        OdBotao("Exportar CSV", { exportarCsv(c, "corrente", listOf("tempo_ms", "corrente_ma", "voltagem_v"), registro.toList()) }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun TempoRestante() {
    val c = LocalContext.current
    val e by rememberBateria()
    val medias = remember { Serie(60) }
    LaunchedEffect(Unit) { while (true) { c.correnteMa()?.let { medias.add(it) }; delay(2000) } }
    val b = e ?: return
    val bm = c.getSystemService(BatteryManager::class.java)
    val carga = c.cargaMah()
    val media = medias.pontos.takeIf { it.size >= 3 }?.average()?.toFloat()
    val estimativa: Long? = when {
        b.carregando -> bm.computeChargeTimeRemaining().takeIf { it > 0 } ?: if (media != null && media > 0 && carga != null) {
            val total = carga / (b.nivel / 100f)
            ((total - carga) / media * 3_600_000).toLong()
        } else null
        media != null && media < 0 && carga != null -> (carga / -media * 3_600_000).toLong()
        else -> null
    }
    FtColuna {
        FtPainel(titulo = if (b.carregando) "Até encher" else "Até esvaziar") {
            OdLeitura(estimativa?.let { formatarDuracao(it) } ?: "Calculando…", tamanho = OdTamanhoLeitura.Destaque)
        }
        FtInfos(null, listOf("Nível" to "${num(b.nivel, 0)}%", "Corrente média (últimos 2 min)" to media?.let { "${num(it, 0)} mA" }, "Carga restante" to carga?.let { "${num(it, 0)} mAh" }))
        FtNota("A estimativa usa a corrente média dos últimos minutos e muda com o uso. Carregando, usa a previsão do próprio Android quando ele informa.")
    }
}

@Composable
private fun TecnologiaSaude() {
    val e by rememberBateria()
    val b = e ?: return
    val m = modeloBateria()
    FtColuna {
        FtInfos("Bateria", listOf(
            "Tecnologia informada" to (b.tecnologia ?: "Não informada"),
            "Tipo (tabela do app)" to (m?.tipo ?: "Modelo fora da tabela"),
            "Capacidade de fábrica" to (m?.let { "${it.mah} mAh" } ?: "Modelo fora da tabela"),
            "Saúde informada" to textoSaude(b.saude),
            "Temperatura" to "${num(b.temperatura, 1)} °C",
            "Voltagem" to "${num(b.voltagem, 3)} V",
        ))
        FtInfos("Aparelho", listOf("Modelo" to "${Build.MANUFACTURER} ${Build.MODEL}", "Identificado como" to m?.nome))
        FtNota("O Android só informa \"Li-ion\" e uma saúde genérica (boa ou com defeito). O tipo e a capacidade de fábrica vêm de uma tabela do app.")
    }
}

@Composable
private fun CapacidadeEstimada() {
    val c = LocalContext.current
    val e by rememberBateria()
    val carga by produceState<Float?>(null) { while (true) { value = c.cargaMah(); delay(5000) } }
    val b = e ?: return
    val m = modeloBateria()
    val estimada = carga?.let { it / (b.nivel / 100f) }
    FtColuna {
        FtPainel(titulo = "Capacidade total estimada") {
            OdLeitura(estimada?.let { odFormatar(it.toDouble(), 0) } ?: "—", unidade = "mAh", tamanho = OdTamanhoLeitura.Destaque)
            if (estimada != null && m != null) OdLeitura(odFormatar((estimada / m.mah * 100).toDouble(), 0), unidade = "% da capacidade de fábrica")
        }
        FtInfos(null, listOf("Carga agora" to carga?.let { "${num(it, 0)} mAh" }, "Nível" to "${num(b.nivel, 0)}%", "Capacidade de fábrica" to m?.let { "${it.mah} mAh" }))
        if (carga == null) OdEstadoRecurso(OdTipoEstado.Indisponivel, "Contador de carga indisponível", "Este aparelho não informa a carga em mAh (CHARGE_COUNTER).")
        FtNota("A conta divide a carga atual pelo nível. Fica confiável depois de uma carga longa (de 20% a 100%, por exemplo), quando o medidor da bateria se recalibra.")
    }
}

@Composable
private fun Ciclos() {
    val e by rememberBateria()
    val b = e ?: return
    FtColuna {
        if (b.ciclos != null) FtPainel(titulo = "Ciclos de carga") { OdLeitura(b.ciclos.toString(), unidade = "ciclos", tamanho = OdTamanhoLeitura.Destaque) }
        else OdEstadoRecurso(OdTipoEstado.Indisponivel, "O fabricante não informa", "O Android 14 tem o campo de ciclos, mas este aparelho deixa vazio.")
        FtNota("Um ciclo equivale a usar 100% da bateria, somando cargas parciais. Baterias de lítio costumam manter 80% da capacidade até 500 a 800 ciclos.")
    }
}

@Composable
private fun SaudeOficial() {
    val c = LocalContext.current
    val soh = remember {
        val bm = c.getSystemService(BatteryManager::class.java)
        runCatching { bm.getIntProperty(10 /* BATTERY_PROPERTY_STATE_OF_HEALTH */) }.getOrNull()?.takeIf { it in 1..100 }
    }
    FtColuna {
        if (soh != null) FtPainel(titulo = "Saúde oficial") { OdLeitura(soh.toString(), unidade = "%", tamanho = OdTamanhoLeitura.Destaque) }
        else OdEstadoRecurso(OdTipoEstado.Indisponivel, "Saúde não informada", "A permissão foi dada, mas este aparelho não preenche a saúde oficial da bateria.")
        FtNota("É a capacidade atual em relação à de fábrica, calculada pelo próprio controlador da bateria.")
    }
}

@Composable
private fun TesteStandby() {
    val c = LocalContext.current
    val tarefa by ServicoMedicao.estadoAtual.collectAsState()
    var minutos by rememberSaveable { mutableStateOf(10) }
    val comNotificacao = rememberComPermissao(Permissao.Notificacoes)
    val historico by produceState(emptyList<ItemSalvo>()) { BancoDados.de(c).itens().observar(Tipos.STANDBY).collect { value = it } }
    val t = tarefa as? TarefaMedicao.Standby
    FtColuna {
        when {
            t?.resultado != null -> {
                val r = t.resultado
                FtPainel(titulo = "Resultado") {
                    OdLeitura(num(r.porcentoHora, 2), unidade = "% por hora", tamanho = OdTamanhoLeitura.Destaque)
                }
                FtInfos(null, listOf(
                    "Duração" to formatarDuracao(r.duracaoMs),
                    "Consumo médio" to r.correnteMediaMa?.let { "${num(abs(it), 0)} mA" },
                    "Carga gasta" to r.mahGastos?.let { "${num(it, 0)} mAh" },
                    "Autonomia em standby" to r.autonomiaH?.let { "${num(it, 0)} h" },
                ))
                OdBotao("Fazer outro teste", { ServicoMedicao.limpar() }, Modifier.fillMaxWidth())
            }
            t != null -> {
                OdEstadoRecurso(OdTipoEstado.Medindo, "Medindo", "Apague a tela e deixe o celular parado. ${t.amostras} leituras até agora.", acao = "Parar", aoAgir = { ServicoMedicao.parar(c) })
            }
            else -> {
                OdGrupoConfig("Duração") {
                    Column(Modifier.fillMaxWidth()) {
                        OdSegmentado(listOf(10 to "10 min", 30 to "30 min", 60 to "1 h", 180 to "3 h"), minutos, { minutos = it })
                    }
                }
                OdBotao("Iniciar teste", {
                    comNotificacao { ServicoMedicao.iniciar(c, ServicoMedicao.ACAO_STANDBY, minutos * 60_000L) }
                }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario, icone = OdIcones.Iniciar)
                FtNota("Desconecte o carregador, inicie e apague a tela. O teste mede quanto o aparelho consome parado, não a saúde da bateria.")
            }
        }
        if (historico.isNotEmpty()) OdGrupoConfig("Testes anteriores") {
            historico.take(10).forEach { i ->
                val f = i.campos()
                val dur = f["duracao"]?.toLongOrNull() ?: 1
                val pct = ((f["nivelInicio"]?.toFloatOrNull() ?: 0f) - (f["nivelFim"]?.toFloatOrNull() ?: 0f)) / (dur / 3_600_000f)
                OdLinhaConfig(i.nome, descricao = "${formatarDataHora(i.criado)} · ${num(pct, 2)} %/hora")
            }
        }
    }
}

@Composable
private fun TesteCarregador() {
    val c = LocalContext.current
    val e by rememberBateria()
    val janela = remember { Serie(30) }
    val escopo = rememberCoroutineScope()
    val toasts = LocalToasts.current
    var nome by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) { while (true) { c.correnteMa()?.let { janela.add(it) }; delay(1000) } }
    val historico by produceState(emptyList<ItemSalvo>()) { BancoDados.de(c).itens().observar(Tipos.CARREGADOR).collect { value = it } }
    val b = e ?: return
    val media = janela.pontos.takeIf { it.isNotEmpty() }?.average()?.toFloat()
    FtColuna {
        if (!b.conectado) OdEstadoRecurso(OdTipoEstado.Indisponivel, "Conecte o carregador", "Ligue o carregador e o cabo que quer testar. A medição começa sozinha.")
        else {
            FtPainel(titulo = "Média dos últimos 30 s") {
                OdLeitura(media?.let { odFormatar(it.toDouble(), 0) } ?: "—", unidade = "mA", tamanho = OdTamanhoLeitura.Destaque)
                OdLeitura(media?.let { odFormatar((it * b.voltagem / 1000).toDouble(), 2) } ?: "—", unidade = "W")
            }
            FtInfos(null, listOf("Fonte" to textoFonte(b.fonte), "Nível" to "${num(b.nivel, 0)}%", "Temperatura" to "${num(b.temperatura, 1)} °C"))
            FtCampo(nome, { nome = it }, "Nome do carregador ou cabo")
            OdBotao("Salvar resultado", {
                escopo.launch {
                    BancoDados.de(c).itens().salvar(ItemSalvo.de(Tipos.CARREGADOR, nome.ifBlank { "Carregador" }, mapOf("ma" to media, "w" to media?.let { it * b.voltagem / 1000 }, "nivel" to b.nivel, "fonte" to textoFonte(b.fonte))))
                    toasts.mostrar("Resultado salvo.")
                }
            }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario, habilitado = media != null)
        }
        FtNota("Compare carregadores e cabos na mesma faixa de nível (ex.: entre 20% e 50%) e com a tela apagada: perto de 100% todo carregador fica lento.")
        if (historico.isNotEmpty()) {
            val max = historico.maxOf { it.campos()["ma"]?.toFloatOrNull() ?: 0f }.coerceAtLeast(1f)
            FtPainel(titulo = "Comparação") {
                historico.forEach { i ->
                    val f = i.campos()
                    val ma = f["ma"]?.toFloatOrNull() ?: 0f
                    FtBarra(i.nome, ma, max, "${num(ma, 0)} mA · ${num(f["w"]?.toFloatOrNull() ?: 0f, 1)} W (${f["nivel"]?.toFloatOrNull()?.toInt()}%)")
                }
            }
        }
    }
}

@Composable
private fun HistoricoBateria() {
    val c = LocalContext.current
    var ativo by rememberAjusteBool(ChavesPeriodicas.HISTORICO_BATERIA, false)
    var dias by rememberSaveable { mutableStateOf(1) }
    val desde = System.currentTimeMillis() - dias * 24L * 3600 * 1000
    val amostras by produceState(emptyList<io.github.andrexagris.ferramentas.dados.Amostra>(), dias) {
        BancoDados.de(c).amostras().observar(Series.BATERIA, desde).collect { value = it }
    }
    val escopo = rememberCoroutineScope()
    FtColuna {
        OdGrupoConfig(null) {
            OdLinhaInterruptor("Registrar histórico", ativo, { ativo = it; TrabalhoPeriodico.agendar(c) }, "Uma leitura a cada 15 minutos, mesmo com o app fechado")
        }
        OdSegmentado(listOf(1 to "24 h", 7 to "7 dias", 30 to "30 dias"), dias, { dias = it })
        FtPainel(titulo = "Nível (%)") { FtGraficoHistorico(amostras.map { it.tempo to it.v1.toFloat() }, minimo = 0f, maximo = 100f, descricao = "Histórico de nível") }
        FtPainel(titulo = "Temperatura (°C)") { FtGraficoHistorico(amostras.map { it.tempo to it.v2.toFloat() }, descricao = "Histórico de temperatura") }
        FtPainel(titulo = "Corrente (mA)") { FtGraficoHistorico(amostras.filter { !it.v3.isNaN() }.map { it.tempo to it.v3.toFloat() }, descricao = "Histórico de corrente") }
        OdGrupoConfig(null) {
            OdLinhaAcao("Exportar CSV", { exportarCsv(c, "bateria", listOf("tempo_ms", "nivel", "temperatura_c", "corrente_ma", "voltagem_v"), amostras.map { listOf(it.tempo, it.v1, it.v2, it.v3, it.v4) }) })
            OdLinhaAcao("Apagar histórico", { escopo.launch { BancoDados.de(c).amostras().apagarSerie(Series.BATERIA) } }, OdIcones.Lixeira, perigo = true)
        }
        if (!ativo && amostras.isEmpty()) FtNota("Ligue o registro para começar. O Android executa a leitura a cada 15 minutos no mínimo, às vezes com atraso para poupar bateria.")
    }
}

@Composable
private fun AlarmesBateria() {
    val c = LocalContext.current
    var nivel by rememberAjusteFloat(ChavesPeriodicas.ALARME_NIVEL, 0f)
    var cheia by rememberAjusteBool(ChavesPeriodicas.ALARME_CHEIA, false)
    var temp by rememberAjusteFloat(ChavesPeriodicas.ALARME_TEMPERATURA, 0f)
    val comNotificacao = rememberComPermissao(Permissao.Notificacoes)
    FtColuna {
        FtPainel {
            FtDeslizante("Avisar com bateria em", nivel, { v -> comNotificacao { nivel = v; TrabalhoPeriodico.agendar(c) } }, 0f..50f, if (nivel <= 0) "Desligado" else "${nivel.toInt()}%", passos = 9)
            FtDeslizante("Avisar com temperatura acima de", temp, { v -> comNotificacao { temp = v; TrabalhoPeriodico.agendar(c) } }, 0f..50f, if (temp <= 0) "Desligado" else "${temp.toInt()} °C", passos = 9)
        }
        OdGrupoConfig(null) {
            OdLinhaInterruptor("Avisar com carga completa", cheia, { v -> comNotificacao { cheia = v; TrabalhoPeriodico.agendar(c) } })
        }
        FtNota("Os alarmes conferem a bateria a cada 15 minutos, o intervalo mínimo do Android para apps fechados.")
    }
}

@Composable
private fun ConsumoPorApp() {
    val c = LocalContext.current
    var dias by rememberSaveable { mutableStateOf(1) }
    val lista by produceState(emptyList<Pair<String, Long>>(), dias) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val usm = c.getSystemService(UsageStatsManager::class.java)
            val fim = System.currentTimeMillis()
            val ini = if (dias == 1) io.github.andrexagris.ferramentas.base.inicioDe(io.github.andrexagris.ferramentas.base.Periodo.Hoje) else fim - dias * 24L * 3600 * 1000
            usm.queryAndAggregateUsageStats(ini, fim).values.filter { it.totalTimeInForeground > 60_000 }
                .sortedByDescending { it.totalTimeInForeground }.map { c.packageManager.rotulo(it.packageName) to it.totalTimeInForeground }
        }
    }
    FtColuna {
        OdSegmentado(listOf(1 to "Hoje", 7 to "7 dias"), dias, { dias = it })
        val total = lista.sumOf { it.second }.coerceAtLeast(1)
        FtPainel(titulo = "Parcela estimada do consumo") {
            if (lista.isEmpty()) io.github.andrexagris.opendesign.OdEstadoRecurso(OdTipoEstado.Indisponivel, "Sem uso registrado", "Nenhum app ficou mais de 1 minuto na tela neste período.")
            lista.take(25).forEach { (nome, ms) -> FtBarra(nome, ms.toFloat(), lista.first().second.toFloat(), "${num(ms * 100.0 / total, 0)}% · ${formatarDuracao(ms)}") }
        }
        FtNota("O Android não informa o consumo de bateria por app para outros apps. A estimativa usa o tempo de cada app na tela, que é a maior parte do gasto.")
    }
}
