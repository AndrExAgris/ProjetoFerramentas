package io.github.andrexagris.ferramentas.ui.ferramentas.sinais

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.hardware.ConsumerIrManager
import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.CellIdentityNr
import android.telephony.CellSignalStrengthNr
import android.telephony.TelephonyManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.andrexagris.ferramentas.base.Serie
import io.github.andrexagris.ferramentas.base.bipe
import io.github.andrexagris.ferramentas.base.exportarCsv
import io.github.andrexagris.ferramentas.base.formatarHora
import io.github.andrexagris.ferramentas.base.hexParaBytes
import io.github.andrexagris.ferramentas.base.num
import io.github.andrexagris.ferramentas.base.rememberComPermissao
import io.github.andrexagris.ferramentas.catalogo.Permissao
import io.github.andrexagris.ferramentas.servicos.ServicoMedicao
import io.github.andrexagris.ferramentas.servicos.TarefaMedicao
import io.github.andrexagris.ferramentas.ui.ferramentas.FtBotaoMedir
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.LocalToasts
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdEscalaArco
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdFaixa
import io.github.andrexagris.opendesign.OdGraficoTempoReal
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdInstrumento
import io.github.andrexagris.opendesign.OdLeitura
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdLinhaInterruptor
import io.github.andrexagris.opendesign.OdLinhaValor
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTamanhoLeitura
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.OdTipoFaixa
import io.github.andrexagris.opendesign.odFormatar
import io.github.andrexagris.opendesign.odSerieResultante
import kotlinx.coroutines.delay

fun telasSinais(): Map<String, TelaDeFerramenta> = mapOf(
    "sinais.celular" com { InfoCelular() },
    "sinais.log-celular" com { LogCelular() },
    "sinais.ble" com { ScannerBle() },
    "sinais.achar" com { AcharDispositivo() },
    "sinais.bluetooth-info" com { InfoBluetooth() },
    "sinais.infravermelho" com { ControleIr() },
    "sinais.uwb" com { InfoUwb() },
)

/** Leitura simplificada de uma célula da rede celular. */
data class CelulaLida(
    val tecnologia: String, val registrada: Boolean, val operadora: String?, val id: String?, val pci: Int?, val area: Int?,
    val canal: Int?, val bandas: String?, val dbm: Int?, val rsrp: Int?, val rsrq: Int?, val sinr: Int?, val nivel: Int,
)

private fun Int.valido(): Int? = takeIf { it != CellInfo.UNAVAILABLE && it != Int.MAX_VALUE }
private fun Long.valido(): Long? = takeIf { it != CellInfo.UNAVAILABLE_LONG && it != Long.MAX_VALUE }

@SuppressLint("MissingPermission")
fun lerCelulas(tm: TelephonyManager): List<CelulaLida> = tm.allCellInfo.orEmpty().mapNotNull { ci ->
    when (ci) {
        is CellInfoLte -> {
            val id = ci.cellIdentity; val s = ci.cellSignalStrength
            CelulaLida("4G (LTE)", ci.isRegistered, listOfNotNull(id.mccString, id.mncString).joinToString("-").ifBlank { null }, id.ci.valido()?.toString(),
                id.pci.valido(), id.tac.valido(), id.earfcn.valido(), id.bands.joinToString { "B$it" }.ifBlank { null },
                s.dbm.valido(), s.rsrp.valido(), s.rsrq.valido(), s.rssnr.valido(), s.level)
        }
        is CellInfoNr -> {
            val id = ci.cellIdentity as CellIdentityNr; val s = ci.cellSignalStrength as CellSignalStrengthNr
            CelulaLida("5G (NR)", ci.isRegistered, listOfNotNull(id.mccString, id.mncString).joinToString("-").ifBlank { null }, id.nci.valido()?.toString(),
                id.pci.valido(), id.tac.valido(), id.nrarfcn.valido(), id.bands.joinToString { "n$it" }.ifBlank { null },
                s.dbm.valido(), s.ssRsrp.valido(), s.ssRsrq.valido(), s.ssSinr.valido(), s.level)
        }
        is CellInfoWcdma -> {
            val id = ci.cellIdentity; val s = ci.cellSignalStrength
            CelulaLida("3G (WCDMA)", ci.isRegistered, listOfNotNull(id.mccString, id.mncString).joinToString("-").ifBlank { null }, id.cid.valido()?.toString(),
                id.psc.valido(), id.lac.valido(), id.uarfcn.valido(), null, s.dbm.valido(), null, null, null, s.level)
        }
        is CellInfoGsm -> {
            val id = ci.cellIdentity; val s = ci.cellSignalStrength
            CelulaLida("2G (GSM)", ci.isRegistered, listOfNotNull(id.mccString, id.mncString).joinToString("-").ifBlank { null }, id.cid.valido()?.toString(),
                id.bsic.valido(), id.lac.valido(), id.arfcn.valido(), null, s.dbm.valido(), null, null, null, s.level)
        }
        else -> null
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun InfoCelular() {
    val c = LocalContext.current
    val tm = remember { c.getSystemService(TelephonyManager::class.java) }
    val serie = remember { Serie(120) }
    val celulas by produceState(emptyList<CelulaLida>()) {
        while (true) {
            runCatching { tm.requestCellInfoUpdate(c.mainExecutor, object : TelephonyManager.CellInfoCallback() { override fun onCellInfo(l: MutableList<CellInfo>) {} }) }
            value = runCatching { lerCelulas(tm) }.getOrDefault(emptyList())
            value.firstOrNull { it.registrada }?.let { (it.rsrp ?: it.dbm)?.let { v -> serie.add(v.toFloat()) } }
            delay(2000)
        }
    }
    val principal = celulas.firstOrNull { it.registrada }
    FtColuna {
        FtInfos("Rede", listOf(
            "Operadora" to tm.networkOperatorName.ifBlank { null },
            "Tipo de dados" to tipoRede(tm),
            "Roaming" to if (tm.isNetworkRoaming) "Sim" else "Não",
        ))
        if (principal == null) OdEstadoRecurso(OdTipoEstado.Indisponivel, "Sem célula registrada", "Confira se há chip, sinal e se a localização do aparelho está ligada.")
        else {
            FtPainel(titulo = "Sinal da célula atual (${principal.tecnologia})") {
                OdLeitura((principal.rsrp ?: principal.dbm)?.toString() ?: "—", unidade = if (principal.rsrp != null) "dBm RSRP" else "dBm", tamanho = OdTamanhoLeitura.Destaque)
                OdGraficoTempoReal(listOf(odSerieResultante(serie.pontos.toList(), "dBm")), -140f, -40f, janela = 120, unidade = "dBm", casas = 0, rotuloTempo = "−4 min" to "agora")
            }
            celulas.forEach { cel ->
                FtInfos(if (cel.registrada) "Célula registrada" else "Vizinha (${cel.tecnologia})", listOf(
                    "Tecnologia" to cel.tecnologia, "MCC-MNC" to cel.operadora, "Id da célula" to cel.id, "PCI / PSC" to cel.pci?.toString(),
                    "TAC / LAC" to cel.area?.toString(), "Canal (ARFCN)" to cel.canal?.toString(), "Bandas" to cel.bandas,
                    "RSRP" to cel.rsrp?.let { "$it dBm" }, "RSRQ" to cel.rsrq?.let { "$it dB" }, "SINR" to cel.sinr?.let { "$it dB" },
                    "Potência" to cel.dbm?.let { "$it dBm" }, "Nível" to "${cel.nivel} de 4",
                ))
            }
        }
        FtNota("RSRP acima de −90 dBm é sinal ótimo; abaixo de −110 dBm, fraco. O detalhe disponível varia conforme o chip do aparelho.")
    }
}

@SuppressLint("MissingPermission")
private fun tipoRede(tm: TelephonyManager): String = when (runCatching { tm.dataNetworkType }.getOrDefault(-1)) {
    TelephonyManager.NETWORK_TYPE_NR -> "5G"
    TelephonyManager.NETWORK_TYPE_LTE -> "4G"
    TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_HSDPA -> "3G"
    TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
    0 -> "Sem dados móveis"
    -1 -> "Desconhecido"
    else -> "Outro"
}

@Composable
private fun LogCelular() {
    val c = LocalContext.current
    val tarefa by ServicoMedicao.estadoAtual.collectAsState()
    val log = tarefa as? TarefaMedicao.LogCelular
    val comNotificacao = rememberComPermissao(Permissao.Notificacoes)
    var gravando by remember { mutableStateOf(false) }
    FtColuna {
        FtBotaoMedir(gravando && log != null, { liga ->
            gravando = liga
            if (liga) comNotificacao { ServicoMedicao.iniciar(c, ServicoMedicao.ACAO_LOG_CELULAR) } else ServicoMedicao.parar(c)
        }, iniciar = "Iniciar registro")
        if (log != null) {
            FtInfos(null, listOf("Registros" to log.registros.size.toString(), "Início" to formatarHora(log.inicio)))
            OdBotao("Exportar CSV", {
                exportarCsv(c, "sinal-celular", listOf("tempo_ms", "latitude", "longitude", "tecnologia", "id_celula", "pci", "canal", "rsrp_dbm", "rsrq_db", "sinr_db", "dbm"),
                    log.registros.map { r -> listOf(r.tempo, r.lat, r.lon, r.celula?.tecnologia, r.celula?.id, r.celula?.pci, r.celula?.canal, r.celula?.rsrp, r.celula?.rsrq, r.celula?.sinr, r.celula?.dbm) })
            }, Modifier.fillMaxWidth(), habilitado = log.registros.isNotEmpty())
            OdGrupoConfig("Últimos registros") {
                log.registros.takeLast(8).reversed().forEach { r ->
                    OdLinhaConfig(formatarHora(r.tempo), descricao = "${r.celula?.tecnologia ?: "Sem célula"} · ${(r.celula?.rsrp ?: r.celula?.dbm)?.let { "$it dBm" } ?: "—"} · ${r.lat?.let { "${num(it, 5)}, ${num(r.lon ?: 0.0, 5)}" } ?: "sem GPS"}")
                }
            }
        }
        FtNota("Registra a célula e o sinal a cada 5 segundos com a posição do GPS, mesmo com a tela apagada. Bom para mapear onde o sinal cai.")
    }
}

/** Aparelho visto num scan de Bluetooth LE. */
data class AparelhoBle(val endereco: String, val nome: String?, val rssi: Int, val visto: Long, val fabricante: Int?, val conectavel: Boolean)

/** Scanner BLE enquanto a tela estiver aberta. Devolve o mapa de aparelhos por endereço. */
@SuppressLint("MissingPermission")
@Composable
fun rememberScanBle(ativo: Boolean): Map<String, AparelhoBle> {
    val c = LocalContext.current
    val mapa = remember { mutableStateMapOf<String, AparelhoBle>() }
    DisposableEffect(ativo) {
        val adaptador = c.getSystemService(BluetoothManager::class.java)?.adapter
        val scanner = adaptador?.bluetoothLeScanner
        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, r: ScanResult) {
                val sr = r.scanRecord
                mapa[r.device.address] = AparelhoBle(
                    r.device.address, sr?.deviceName ?: runCatching { r.device.name }.getOrNull(), r.rssi, System.currentTimeMillis(),
                    sr?.manufacturerSpecificData?.takeIf { it.size() > 0 }?.keyAt(0), r.isConnectable,
                )
            }
        }
        if (ativo && adaptador?.isEnabled == true) runCatching {
            scanner?.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), cb)
        }
        onDispose { runCatching { scanner?.stopScan(cb) } }
    }
    return mapa
}

@Composable
private fun PedirLigarBluetooth(): Boolean {
    val c = LocalContext.current
    val adaptador = c.getSystemService(BluetoothManager::class.java)?.adapter
    var ligado by remember { mutableStateOf(adaptador?.isEnabled == true) }
    val ligar = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { ligado = adaptador?.isEnabled == true }
    if (!ligado) OdEstadoRecurso(OdTipoEstado.Indisponivel, "Bluetooth desligado", "Ligue o Bluetooth para procurar aparelhos.", acao = "Ligar Bluetooth", aoAgir = {
        ligar.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    })
    return ligado
}

private fun nomeFabricante(id: Int?): String? = when (id) {
    null -> null
    0x004C -> "Apple"; 0x0075 -> "Samsung"; 0x00E0 -> "Google"; 0x0006 -> "Microsoft"; 0x0157 -> "Huami (Amazfit)"; 0x038F -> "Xiaomi"
    0x0059 -> "Nordic"; 0x0087 -> "Garmin"; 0x02E5 -> "Espressif"; 0x012D -> "Sony"; 0x00D2 -> "Dialog"; 0x0499 -> "Ruuvi"
    else -> "Fabricante 0x%04X".format(id)
}

@Composable
private fun ScannerBle() {
    if (!PedirLigarBluetooth()) return
    val aparelhos = rememberScanBle(true)
    val agora by produceState(System.currentTimeMillis()) { while (true) { delay(1000); value = System.currentTimeMillis() } }
    val lista = aparelhos.values.filter { agora - it.visto < 30_000 }.sortedByDescending { it.rssi }
    FtColuna {
        FtInfos(null, listOf("Aparelhos nos últimos 30 s" to lista.size.toString()))
        OdGrupoConfig("Mais perto primeiro") {
            lista.forEach { a ->
                OdLinhaConfig(a.nome ?: "Sem nome", descricao = listOfNotNull(a.endereco, nomeFabricante(a.fabricante), if (a.conectavel) "conectável" else null).joinToString(" · ")) {
                    androidx.compose.material3.Text("${a.rssi} dBm", style = io.github.andrexagris.opendesign.OdTema.tipografia.ui.copy(fontFeatureSettings = "tnum"))
                }
            }
        }
        FtNota("Muitos aparelhos trocam o endereço de tempos em tempos por privacidade, então o mesmo fone pode aparecer mais de uma vez.")
    }
}

@Composable
private fun AcharDispositivo() {
    if (!PedirLigarBluetooth()) return
    val aparelhos = rememberScanBle(true)
    var alvo by rememberSaveable { mutableStateOf<String?>(null) }
    var som by rememberSaveable { mutableStateOf(true) }
    val a = alvo?.let { aparelhos[it] }
    var suave by remember { mutableStateOf(-100f) }
    LaunchedEffect(a?.rssi, a?.visto) { a?.let { suave = if (suave <= -100f) it.rssi.toFloat() else suave * 0.7f + it.rssi * 0.3f } }
    LaunchedEffect(alvo, som) {
        while (alvo != null && som) {
            val forca = ((suave + 100) / 70f).coerceIn(0f, 1f)
            bipe(600.0 + 900 * forca, 40)
            delay((1200 - 1050 * forca).toLong())
        }
    }
    FtColuna {
        if (alvo == null) {
            FtNota("Escolha o aparelho que quer achar. Os mais próximos aparecem primeiro.")
            OdGrupoConfig("Aparelhos") {
                aparelhos.values.sortedByDescending { it.rssi }.take(30).forEach { ap ->
                    OdLinhaValor(ap.nome ?: ap.endereco, "${ap.rssi} dBm", { alvo = ap.endereco; suave = ap.rssi.toFloat() }, descricao = if (ap.nome != null) ap.endereco else null)
                }
            }
        } else {
            val estado = when { suave > -55 -> "Muito quente: está bem perto"; suave > -70 -> "Quente"; suave > -85 -> "Morno"; else -> "Frio: longe" }
            OdInstrumento(estado, suave > -55, leitura = { OdLeitura(odFormatar(suave.toDouble(), 0), unidade = "dBm", largura = 4, tamanho = OdTamanhoLeitura.Destaque) }) {
                OdEscalaArco(suave, -100f, -30f, faixas = listOf(OdFaixa(-55f, -30f, OdTipoFaixa.Ok)))
            }
            FtInfos(null, listOf("Aparelho" to (a?.nome ?: alvo), "Último sinal" to a?.let { "${((System.currentTimeMillis() - it.visto) / 1000)} s atrás" }))
            OdGrupoConfig(null) { OdLinhaInterruptor("Bipe de proximidade", som, { som = it }, "Mais rápido e agudo quanto mais perto") }
            OdBotao("Escolher outro aparelho", { alvo = null }, Modifier.fillMaxWidth())
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun InfoBluetooth() {
    val c = LocalContext.current
    val adaptador = remember { c.getSystemService(BluetoothManager::class.java)?.adapter }
    if (!PedirLigarBluetooth()) return
    val pareados = remember { adaptador?.bondedDevices?.toList().orEmpty() }
    FtColuna {
        FtInfos("Adaptador", listOf(
            "Nome" to runCatching { adaptador?.name }.getOrNull(),
            "LE Audio" to if (Build.VERSION.SDK_INT >= 33) (if (adaptador?.isLeAudioSupported == BluetoothStatusCodesOk) "Sim" else "Não") else null,
            "Áudio conectado (A2DP)" to estadoPerfil(adaptador, BluetoothProfile.A2DP),
            "Fone/viva-voz (HFP)" to estadoPerfil(adaptador, BluetoothProfile.HEADSET),
        ))
        OdGrupoConfig("Pareados (${pareados.size})") {
            pareados.forEach { d ->
                val bateria = runCatching { d.javaClass.getMethod("getBatteryLevel").invoke(d) as Int }.getOrNull()?.takeIf { it in 0..100 }
                OdLinhaConfig(runCatching { d.name }.getOrNull() ?: d.address, descricao = listOfNotNull(tipoAparelho(d), bateria?.let { "bateria $it%" }, d.address).joinToString(" · "))
            }
        }
        FtNota("A bateria só aparece quando o fone informa ao Android e o fabricante libera. O codec em uso não é exposto para apps comuns.")
    }
}

private const val BluetoothStatusCodesOk = 10 // BluetoothStatusCodes.FEATURE_SUPPORTED

@SuppressLint("MissingPermission")
private fun estadoPerfil(a: BluetoothAdapter?, perfil: Int): String = when (runCatching { a?.getProfileConnectionState(perfil) }.getOrNull()) {
    BluetoothProfile.STATE_CONNECTED -> "Conectado"
    BluetoothProfile.STATE_CONNECTING -> "Conectando"
    else -> "Desconectado"
}

@SuppressLint("MissingPermission")
private fun tipoAparelho(d: BluetoothDevice): String? = when (d.bluetoothClass?.majorDeviceClass) {
    0x0400 -> "Áudio"; 0x0200 -> "Telefone"; 0x0100 -> "Computador"; 0x0500 -> "Periférico"; 0x0700 -> "Vestível"; 0x0900 -> "Saúde"; 0x0600 -> "Imagem"
    else -> null
}

/** Padrão NEC (38 kHz): 9 ms + 4,5 ms, 32 bits (endereço, ~endereço, comando, ~comando), bit final. */
fun padraoNec(endereco: Int, comando: Int, samsung: Boolean = false): IntArray {
    val p = mutableListOf(if (samsung) 4500 else 9000, 4500)
    val bytes = if (samsung) listOf(endereco, endereco, comando, comando.inv() and 0xFF) else listOf(endereco, endereco.inv() and 0xFF, comando, comando.inv() and 0xFF)
    bytes.forEach { b -> for (i in 0 until 8) { p += 560; p += if ((b shr i) and 1 == 1) 1690 else 560 } }
    p += 560
    return p.toIntArray()
}

@Composable
private fun ControleIr() {
    val c = LocalContext.current
    val ir = remember { c.getSystemService(ConsumerIrManager::class.java) }
    val toasts = LocalToasts.current
    var protocolo by rememberSaveable { mutableStateOf("nec") }
    var endereco by rememberSaveable { mutableStateOf("07") }
    var comando by rememberSaveable { mutableStateOf("02") }
    FtColuna {
        FtInfos("Emissor", listOf("Frequências" to ir?.carrierFrequencies?.joinToString { "${it.minFrequency / 1000}–${it.maxFrequency / 1000} kHz" }))
        OdSegmentado(listOf("nec" to "NEC", "samsung" to "Samsung"), protocolo, { protocolo = it })
        FtCampo(endereco, { endereco = it }, "Endereço (hex)")
        FtCampo(comando, { comando = it }, "Comando (hex)")
        OdBotao("Enviar código", {
            runCatching {
                val a = endereco.hexParaBytes().first().toInt() and 0xFF
                val cmd = comando.hexParaBytes().first().toInt() and 0xFF
                ir.transmit(38000, padraoNec(a, cmd, protocolo == "samsung"))
                toasts.mostrar("Código enviado.")
            }.onFailure { toasts.mostrar("Confira o endereço e o comando em hexadecimal.", io.github.andrexagris.opendesign.OdTipoToast.Erro) }
        }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario)
        FtNota("Exemplo: TV Samsung, endereço 07, comando 02 (liga/desliga). A base de códigos de controles vem numa versão futura.")
    }
}

@Composable
private fun InfoUwb() {
    FtColuna {
        OdEstadoRecurso(OdTipoEstado.Medindo, "UWB disponível", "Este aparelho tem UWB. Medir distância precisa de um segundo aparelho UWB e de uma sessão de pareamento, que chega numa versão futura.")
    }
}
