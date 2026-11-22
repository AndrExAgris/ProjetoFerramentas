package io.github.andrexagris.ferramentas.ui.ferramentas.internet

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import io.github.andrexagris.ferramentas.base.Periodo
import io.github.andrexagris.ferramentas.base.UsoApp
import io.github.andrexagris.ferramentas.base.bytesAparelho
import io.github.andrexagris.ferramentas.base.formatarBytes
import io.github.andrexagris.ferramentas.base.formatarHora
import io.github.andrexagris.ferramentas.base.formatarVelocidade
import io.github.andrexagris.ferramentas.base.iconeDoApp
import io.github.andrexagris.ferramentas.base.inicioDe
import io.github.andrexagris.ferramentas.base.nomeDoUid
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.rememberComPermissao
import io.github.andrexagris.ferramentas.base.usoPorApp
import io.github.andrexagris.ferramentas.catalogo.Permissao
import io.github.andrexagris.ferramentas.dados.rememberAjusteBool
import io.github.andrexagris.ferramentas.dados.rememberAjusteFloat
import io.github.andrexagris.ferramentas.servicos.ChavesPeriodicas
import io.github.andrexagris.ferramentas.servicos.ServicoVelocidade
import io.github.andrexagris.ferramentas.servicos.ServicoVpnDns
import io.github.andrexagris.ferramentas.servicos.TrabalhoPeriodico
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBarra
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtDeslizante
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdLinhaInterruptor
import io.github.andrexagris.opendesign.OdLinhaOpcao
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoEstado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun telasInternet(): Map<String, TelaDeFerramenta> = mapOf(
    "internet.uso-por-app" com { UsoPorApp() },
    "internet.velocidade-barra".com(emptyList()) { VelocidadeNaBarra() },
    "internet.franquia".com(listOf(Permissao.UsoDoApp)) { Franquia() },
    "internet.conexoes" com { ConexoesPorApp() },
    "internet.app-novo".com(listOf(Permissao.UsoDoApp)) { AvisoAppNovo() },
) + telasWifi() + telasDiagnostico()

/** Ícone de um app como imagem do Compose. */
@Composable
fun IconeApp(icone: Drawable?) {
    val bmp = remember(icone) { icone?.toBitmap(64, 64)?.asImageBitmap() } ?: return
    Image(bmp, null, Modifier.size(24.dp))
}

@Composable
private fun UsoPorApp() {
    val c = LocalContext.current
    var periodo by rememberSaveable { mutableStateOf(Periodo.Hoje) }
    var rede by rememberSaveable { mutableStateOf(0) } // 0 tudo, 1 Wi-Fi, 2 móvel
    val lista by produceState<List<UsoApp>?>(null, periodo) { value = withContext(Dispatchers.IO) { c.usoPorApp(periodo) } }
    FtColuna {
        OdSegmentado(Periodo.entries.map { it to it.rotulo }, periodo, { periodo = it })
        OdSegmentado(listOf(0 to "Tudo", 1 to "Wi-Fi", 2 to "Móvel"), rede, { rede = it })
        val l = lista
        if (l == null) { FtNota("Calculando…"); return@FtColuna }
        fun valor(u: UsoApp) = when (rede) { 1 -> u.wifiBytes; 2 -> u.movelBytes; else -> u.total }
        val ordenada = l.filter { valor(it) > 0 }.sortedByDescending { valor(it) }
        FtInfos(null, listOf("Wi-Fi" to formatarBytes(l.sumOf { it.wifiBytes }), "Dados móveis" to formatarBytes(l.sumOf { it.movelBytes })))
        if (ordenada.isEmpty()) OdEstadoRecurso(OdTipoEstado.Indisponivel, "Sem uso registrado", "Nenhum app usou esta rede no período.")
        else FtPainel {
            val max = valor(ordenada.first()).toFloat()
            ordenada.take(40).forEach { u ->
                FtBarra(u.nome, valor(u).toFloat(), max, formatarBytes(valor(u)), icone = { IconeApp(c.iconeDoApp(u.pacote)) })
            }
        }
        FtNota("Os números são os mesmos que o Android guarda e mostra em Configurações, separados em blocos de algumas horas.")
    }
}

@Composable
private fun VelocidadeNaBarra() {
    val c = LocalContext.current
    val ativo by ServicoVelocidade.ativo.collectAsState()
    val v by ServicoVelocidade.atual.collectAsState()
    val comNotificacao = rememberComPermissao(Permissao.Notificacoes)
    FtColuna {
        OdGrupoConfig(null) {
            OdLinhaInterruptor("Mostrar velocidade na barra", ativo, { liga ->
                if (liga) comNotificacao { ServicoVelocidade.iniciar(c) } else ServicoVelocidade.parar(c)
            }, "O ícone da notificação vira o número, atualizado a cada segundo")
        }
        if (ativo) FtPainel(titulo = "Agora") {
            OdLeitura(formatarVelocidade(v.descida), unidade = "descida", tamanho = OdTamanhoLeitura.Destaque)
            OdLeitura(formatarVelocidade(v.subida), unidade = "subida")
        }
        FtNota("Também dá para ligar pelo bloco \"Velocidade\" no painel rápido (deslize a barra de status para baixo e edite os blocos). Sem permissão de notificação, o número não aparece.")
    }
}

@Composable
private fun Franquia() {
    val c = LocalContext.current
    var limite by rememberAjusteFloat(ChavesPeriodicas.FRANQUIA_GB, 0f)
    var dia by rememberAjusteFloat(ChavesPeriodicas.FRANQUIA_DIA, 1f)
    val comNotificacao = rememberComPermissao(Permissao.Notificacoes)
    val inicio = TrabalhoPeriodico.inicioCicloFranquia(dia.toInt())
    val usado by produceState(0L, dia) { value = withContext(Dispatchers.IO) { c.bytesAparelho(true, inicio) } }
    val diasPassados = ((System.currentTimeMillis() - inicio) / 86_400_000.0).coerceAtLeast(0.5)
    val projecao = usado / diasPassados * 30
    FtColuna {
        FtPainel(titulo = "Dados móveis neste ciclo") {
            OdLeitura(formatarBytes(usado), tamanho = OdTamanhoLeitura.Destaque)
            if (limite > 0) FtBarra("de ${num(limite, 0)} GB", usado.toFloat(), limite * 1e9f, "${num(usado / (limite * 1e9) * 100, 0)}%")
        }
        FtInfos(null, listOf(
            "Ciclo começou em" to java.text.SimpleDateFormat("dd/MM", io.github.andrexagris.ferramentas.base.localeBr).format(java.util.Date(inicio)),
            "Média por dia" to formatarBytes((usado / diasPassados).toLong()),
            "Projeção para 30 dias" to formatarBytes(projecao.toLong()),
        ))
        FtPainel {
            FtDeslizante("Franquia do plano", limite, { v -> comNotificacao { limite = v; TrabalhoPeriodico.agendar(c) } }, 0f..100f, if (limite <= 0) "Sem limite" else "${limite.toInt()} GB", passos = 99)
            FtDeslizante("Dia em que o plano renova", dia, { dia = it }, 1f..28f, "Dia ${dia.toInt()}", passos = 26)
        }
        FtNota("Com uma franquia definida, o app avisa quando você passar de 90% dela (conferido a cada 15 minutos).")
    }
}

@Composable
private fun ConexoesPorApp() {
    val c = LocalContext.current
    val ativo by ServicoVpnDns.ativo.collectAsState()
    val consultas by ServicoVpnDns.consultas.collectAsState()
    val comNotificacao = rememberComPermissao(Permissao.Notificacoes)
    var app by rememberSaveable { mutableStateOf<Int?>(null) }
    val nomes = remember { HashMap<Int, Pair<String, String?>>() }
    fun nome(uid: Int) = nomes.getOrPut(uid) { c.nomeDoUid(uid) }
    FtColuna {
        OdGrupoConfig(null) {
            OdLinhaInterruptor("Ver conexões agora", ativo, { liga -> if (liga) comNotificacao { ServicoVpnDns.iniciar(c) } else ServicoVpnDns.parar(c) },
                "VPN local só para DNS; a internet continua normal")
        }
        if (app == null) {
            val porApp = consultas.groupBy { it.uid }.map { (uid, l) -> Triple(uid, l.size, l.first()) }.sortedByDescending { it.second }
            if (porApp.isNotEmpty()) OdGrupoConfig("Apps (toque para ver os endereços)") {
                porApp.forEach { (uid, n, ultima) ->
                    io.github.andrexagris.opendesign.OdLinhaValor(nome(uid).first, "$n", { app = uid }, descricao = "Último: ${ultima.dominio}")
                }
            }
        } else {
            val uid = app!!
            OdBotao("Voltar para todos os apps", { app = null }, Modifier.fillMaxWidth(), icone = OdIcones.Voltar)
            val doApp = consultas.filter { it.uid == uid }
            FtInfos(nome(uid).first, listOf("Consultas" to doApp.size.toString(), "Endereços diferentes" to doApp.map { it.dominio }.distinct().size.toString()))
            OdGrupoConfig("Endereços") {
                doApp.groupBy { it.dominio }.entries.sortedByDescending { it.value.size }.take(60).forEach { (d, l) ->
                    OdLinhaConfig(d, descricao = "${l.size}× · última às ${formatarHora(l.first().tempo)}")
                }
            }
        }
        FtNota("Mostra os endereços que cada app procura na internet, na hora. Não roda junto com outra VPN. Apps com DNS próprio ou o DNS privado estrito do Android não aparecem.")
    }
}

@Composable
private fun AvisoAppNovo() {
    val c = LocalContext.current
    var ativo by rememberAjusteBool(ChavesPeriodicas.APP_NOVO, false)
    val comNotificacao = rememberComPermissao(Permissao.Notificacoes)
    val recentes by produceState(emptyList<UsoApp>()) { value = withContext(Dispatchers.IO) { c.usoPorApp(Periodo.Semana) } }
    FtColuna {
        OdGrupoConfig(null) {
            OdLinhaInterruptor("Avisar quando um app novo usar a rede", ativo, { v -> comNotificacao { ativo = v; TrabalhoPeriodico.agendar(c) } })
        }
        FtNota("Na primeira verificação o app aprende quem já usa a rede. Depois, avisa quando aparecer alguém novo, com até 15 minutos de atraso. Para ver na hora, use \"Conexões por app em tempo real\".")
        if (recentes.isNotEmpty()) OdGrupoConfig("Usaram a rede nos últimos 7 dias (${recentes.size})") {
            recentes.take(30).forEach { u -> OdLinhaConfig(u.nome, descricao = formatarBytes(u.total)) }
        }
    }
}

