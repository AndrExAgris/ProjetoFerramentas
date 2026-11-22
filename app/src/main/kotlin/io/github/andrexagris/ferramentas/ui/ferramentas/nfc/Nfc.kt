package io.github.andrexagris.ferramentas.ui.ferramentas.nfc

import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcV
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.andrexagris.ferramentas.base.EstadoNfc
import io.github.andrexagris.ferramentas.base.LeitorNfc
import io.github.andrexagris.ferramentas.base.copiarTexto
import io.github.andrexagris.ferramentas.base.estadoNfc
import io.github.andrexagris.ferramentas.base.hex
import io.github.andrexagris.ferramentas.base.hexParaBytes
import io.github.andrexagris.ferramentas.ui.ferramentas.FtCampo
import io.github.andrexagris.ferramentas.ui.ferramentas.FtColuna
import io.github.andrexagris.ferramentas.ui.ferramentas.FtInfos
import io.github.andrexagris.ferramentas.ui.ferramentas.FtNota
import io.github.andrexagris.ferramentas.ui.ferramentas.FtPainel
import io.github.andrexagris.ferramentas.ui.ferramentas.LocalToasts
import io.github.andrexagris.ferramentas.ui.ferramentas.TelaDeFerramenta
import io.github.andrexagris.ferramentas.ui.ferramentas.com
import io.github.andrexagris.opendesign.OdBotao
import io.github.andrexagris.opendesign.OdDialogo
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdGrupoConfig
import io.github.andrexagris.opendesign.OdIcones
import io.github.andrexagris.opendesign.OdLinhaAcao
import io.github.andrexagris.opendesign.OdLinhaConfig
import io.github.andrexagris.opendesign.OdLinhaInterruptor
import io.github.andrexagris.opendesign.OdLinhaOpcao
import io.github.andrexagris.opendesign.OdSegmentado
import io.github.andrexagris.opendesign.OdTema
import io.github.andrexagris.opendesign.OdTipoBotao
import io.github.andrexagris.opendesign.OdTipoEstado
import io.github.andrexagris.opendesign.OdTipoToast

fun telasNfc(): Map<String, TelaDeFerramenta> = mapOf(
    "nfc.ler-tag" com { LerTag() },
    "nfc.ndef" com { LerNdef() },
    "nfc.cartao" com { LerCartao() },
    "nfc.escrever" com { EscreverNdef() },
    "nfc.apagar" com { ApagarTag() },
    "nfc.copiar" com { CopiarTag() },
    "nfc.travar" com { TravarTag() },
    "nfc.senha" com { SenhaNtag() },
    "nfc.dump" com { DumpMemoria() },
    "nfc.mifare" com { MifareClassicTela() },
    "nfc.clonar-uid" com { ClonarUid() },
    "nfc.apdu" com { ComandosApdu() },
    "nfc.gatilho" com { TagGatilho() },
)

/**
 * Estrutura comum das ferramentas de NFC: confere se o NFC está ligado, liga o modo leitor e mostra
 * "Aproxime a tag". aoLer roda na thread do NFC (pode conversar com a tag direto) e devolve uma mensagem
 * curta para o toast, ou null.
 */
@Composable
private fun SessaoNfc(instrucao: String, aoLer: (Tag) -> String?, conteudo: @Composable () -> Unit = {}) {
    val c = LocalContext.current
    val toasts = LocalToasts.current
    when (estadoNfc()) {
        EstadoNfc.SemNfc -> { OdEstadoRecurso(OdTipoEstado.Indisponivel, "Sem NFC", "Este aparelho não tem NFC."); return }
        EstadoNfc.Desligado -> {
            OdEstadoRecurso(OdTipoEstado.Indisponivel, "NFC desligado", "Ligue o NFC nas configurações para usar esta ferramenta.", acao = "Abrir configurações de NFC", aoAgir = {
                c.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            })
            return
        }
        EstadoNfc.Ligado -> {}
    }
    LeitorNfc { tag ->
        val msg = runCatching { aoLer(tag) }.getOrElse { "Falhou: ${it.message ?: it.javaClass.simpleName}. Segure a tag parada e tente de novo." }
        if (msg != null) toasts.mostrar(msg, if (msg.startsWith("Falhou")) OdTipoToast.Erro else OdTipoToast.Sucesso)
    }
    FtColuna {
        OdEstadoRecurso(OdTipoEstado.Medindo, "Aproxime a tag", instrucao)
        conteudo()
    }
}

/** Resumo técnico de uma tag. */
fun infoTag(tag: Tag): List<Pair<String, String?>> {
    val l = mutableListOf<Pair<String, String?>>("UID" to tag.id.hex(), "Tecnologias" to tag.techList.joinToString { it.substringAfterLast('.') })
    NfcA.get(tag)?.let { a -> l += "ATQA" to a.atqa.reversedArray().hex(""); l += "SAK" to "%02X".format(a.sak); l += "Tamanho máximo de comando" to "${a.maxTransceiveLength} bytes" }
    MifareUltralight.get(tag)?.let { u -> l += "Tipo" to when (u.type) { MifareUltralight.TYPE_ULTRALIGHT -> "MIFARE Ultralight / NTAG"; MifareUltralight.TYPE_ULTRALIGHT_C -> "MIFARE Ultralight C"; else -> "Ultralight (tipo desconhecido)" } }
    MifareClassic.get(tag)?.let { m -> l += "Tipo" to "MIFARE Classic ${m.size / 1024}K"; l += "Setores" to "${m.sectorCount}"; l += "Blocos" to "${m.blockCount}" }
    IsoDep.get(tag)?.let { d -> l += "ISO-DEP" to (d.historicalBytes?.hex("") ?: d.hiLayerResponse?.hex("") ?: "Sim") }
    NfcV.get(tag)?.let { v -> l += "ISO 15693 DSFID" to "%02X".format(v.dsfId) }
    Ndef.get(tag)?.let { n -> l += "NDEF" to n.type.substringAfterLast('.'); l += "Memória NDEF" to "${n.maxSize} bytes"; l += "Gravável" to if (n.isWritable) "Sim" else "Não (somente leitura)" }
    if (Ndef.get(tag) == null && NdefFormatable.get(tag) != null) l += "NDEF" to "Não formatada (pode ser formatada)"
    return l
}

/** Lê a mensagem NDEF de uma tag (null se não houver). */
fun lerNdef(tag: Tag): NdefMessage? {
    val n = Ndef.get(tag) ?: return null
    n.connect()
    return try { n.ndefMessage ?: n.cachedNdefMessage } finally { runCatching { n.close() } }
}

/** Grava uma mensagem NDEF, formatando a tag se precisar. */
fun gravarNdef(tag: Tag, m: NdefMessage): String {
    Ndef.get(tag)?.let { n ->
        n.connect()
        try {
            if (!n.isWritable) return "Falhou: a tag é somente leitura."
            if (m.byteArrayLength > n.maxSize) return "Falhou: não cabe (${m.byteArrayLength} de ${n.maxSize} bytes)."
            n.writeNdefMessage(m)
            return "Gravado na tag."
        } finally { runCatching { n.close() } }
    }
    NdefFormatable.get(tag)?.let { f -> f.connect(); try { f.format(m); return "Tag formatada e gravada." } finally { runCatching { f.close() } } }
    return "Falhou: esta tag não aceita NDEF."
}

private val PREFIXOS_URI = listOf("", "http://www.", "https://www.", "http://", "https://", "tel:", "mailto:", "ftp://anonymous:anonymous@", "ftp://ftp.", "ftps://", "sftp://",
    "smb://", "nfs://", "ftp://", "dav://", "news:", "telnet://", "imap:", "rtsp://", "urn:", "pop:", "sip:", "sips:", "tftp:", "btspp://", "btl2cap://",
    "btgoep://", "tcpobex://", "irdaobex://", "file://", "urn:epc:id:", "urn:epc:tag:", "urn:epc:pat:", "urn:epc:raw:", "urn:epc:", "urn:nfc:")

/** Descrição legível de um registro NDEF. */
fun descreverRegistro(r: NdefRecord): Pair<String, String> {
    val tipo = String(r.type, Charsets.US_ASCII)
    return when {
        r.tnf == NdefRecord.TNF_WELL_KNOWN && tipo == "U" -> "Link" to (PREFIXOS_URI.getOrElse(r.payload[0].toInt() and 0xFF) { "" } + String(r.payload, 1, r.payload.size - 1, Charsets.UTF_8))
        r.tnf == NdefRecord.TNF_WELL_KNOWN && tipo == "T" -> {
            val st = r.payload[0].toInt(); val lang = st and 0x3F
            val cs = if (st and 0x80 != 0) Charsets.UTF_16 else Charsets.UTF_8
            "Texto (${String(r.payload, 1, lang, Charsets.US_ASCII)})" to String(r.payload, 1 + lang, r.payload.size - 1 - lang, cs)
        }
        r.tnf == NdefRecord.TNF_WELL_KNOWN && tipo == "Sp" -> "Smart Poster" to (runCatching { NdefMessage(r.payload).records.joinToString(" | ") { descreverRegistro(it).second } }.getOrDefault("—"))
        r.tnf == NdefRecord.TNF_MIME_MEDIA && tipo == "application/vnd.wfa.wsc" -> "Wi-Fi" to (Regex("[\\x20-\\x7E]{3,}").findAll(String(r.payload, Charsets.ISO_8859_1)).joinToString(" · ") { it.value })
        r.tnf == NdefRecord.TNF_MIME_MEDIA -> "Mídia ($tipo)" to String(r.payload, Charsets.UTF_8).take(500)
        r.tnf == NdefRecord.TNF_EXTERNAL_TYPE && tipo == "android.com:pkg" -> "Abrir app" to String(r.payload, Charsets.UTF_8)
        r.tnf == NdefRecord.TNF_ABSOLUTE_URI -> "Endereço" to String(r.payload, Charsets.UTF_8)
        r.tnf == NdefRecord.TNF_EMPTY -> "Vazio" to "—"
        else -> "Registro ($tipo)" to r.payload.hex(" ").take(300)
    }
}

@Composable
private fun LerTag() {
    var info by remember { mutableStateOf<List<Pair<String, String?>>?>(null) }
    val c = LocalContext.current
    SessaoNfc("Encoste a tag na parte de trás do celular, perto da câmera.", { tag -> info = infoTag(tag); "Tag lida." }) {
        info?.let { i ->
            FtInfos("Tag", i)
            OdBotao("Copiar UID", { copiarTexto(c, i.first().second ?: "") }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun LerNdef() {
    var registros by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
    SessaoNfc("Aproxime uma tag com conteúdo gravado.", { tag ->
        val m = lerNdef(tag)
        registros = m?.records?.map { descreverRegistro(it) } ?: emptyList()
        if (m == null) "A tag não tem conteúdo NDEF." else "Tag lida."
    }) {
        registros?.let { r ->
            if (r.isEmpty()) FtNota("Nenhum registro NDEF.")
            else OdGrupoConfig("${r.size} registros") { r.forEach { (t, v) -> OdLinhaConfig(t, descricao = v) } }
        }
    }
}

// ---------- Cartão EMV ----------

/** TLV BER simples (tag, valor), recursivo nos construídos. */
fun tlv(dados: ByteArray, saida: MutableMap<String, ByteArray> = mutableMapOf()): Map<String, ByteArray> {
    var i = 0
    while (i < dados.size) {
        val inicio = i
        if (dados[i] == 0x00.toByte() || dados[i] == 0xFF.toByte()) { i++; continue }
        var fimTag = i + 1
        if (dados[i].toInt() and 0x1F == 0x1F) { while (fimTag < dados.size && dados[fimTag].toInt() and 0x80 != 0) fimTag++; fimTag++ }
        if (fimTag > dados.size) break
        val tag = dados.copyOfRange(inicio, fimTag).hex("")
        i = fimTag
        if (i >= dados.size) break
        var len = dados[i].toInt() and 0xFF; i++
        if (len > 0x80) { val n = len - 0x80; len = 0; repeat(n) { if (i < dados.size) { len = (len shl 8) or (dados[i].toInt() and 0xFF); i++ } } }
        if (i + len > dados.size) break
        val valor = dados.copyOfRange(i, i + len)
        saida.putIfAbsent(tag, valor)
        if (dados[inicio].toInt() and 0x20 != 0) tlv(valor, saida)
        i += len
    }
    return saida
}

private fun IsoDep.cmd(apdu: ByteArray): ByteArray? = transceive(apdu).takeIf { it.size >= 2 && it[it.size - 2] == 0x90.toByte() && it[it.size - 1] == 0x00.toByte() }?.let { it.copyOf(it.size - 2) }

private fun select(aid: ByteArray) = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, aid.size.toByte()) + aid + byteArrayOf(0x00)

/** Lê só dados públicos do cartão (rede, rótulo, final do número e validade). Nada é guardado. */
fun lerEmv(tag: Tag): List<Pair<String, String?>> {
    val d = IsoDep.get(tag) ?: return listOf("Erro" to "Não é um cartão por aproximação (ISO-DEP).")
    d.connect(); d.timeout = 5000
    try {
        val ppse = d.cmd(select("2PAY.SYS.DDF01".toByteArray())) ?: return listOf("Erro" to "O cartão não respondeu ao pagamento por aproximação.")
        val aid = tlv(ppse)["4F"] ?: return listOf("Erro" to "Nenhum aplicativo de pagamento encontrado.")
        val resp = d.cmd(select(aid)) ?: return listOf("Erro" to "O cartão recusou o aplicativo.")
        val t = tlv(resp)
        val rotulo = (t["50"] ?: tlv(ppse)["50"])?.let { String(it, Charsets.ISO_8859_1).trim() }
        val pdol = t["9F38"]
        var tamanho = 0
        if (pdol != null) { var i = 0; while (i < pdol.size) { i += if (pdol[i].toInt() and 0x1F == 0x1F) 2 else 1; if (i < pdol.size) { tamanho += pdol[i].toInt() and 0xFF; i++ } } }
        val gpo = d.cmd(byteArrayOf(0x80.toByte(), 0xA8.toByte(), 0, 0, (tamanho + 2).toByte(), 0x83.toByte(), tamanho.toByte()) + ByteArray(tamanho) + byteArrayOf(0))
        val registros = mutableMapOf<String, ByteArray>()
        gpo?.let { g ->
            val afl = tlv(g)["94"] ?: if (g[0] == 0x80.toByte()) g.copyOfRange(4, g.size) else null
            if (afl != null) for (k in 0 until afl.size / 4) {
                val sfi = (afl[k * 4].toInt() and 0xFF) shr 3
                for (rec in (afl[k * 4 + 1].toInt() and 0xFF)..(afl[k * 4 + 2].toInt() and 0xFF)) {
                    d.cmd(byteArrayOf(0x00, 0xB2.toByte(), rec.toByte(), ((sfi shl 3) or 4).toByte(), 0))?.let { tlv(it, registros) }
                }
            }
        }
        val pan = (registros["5A"] ?: registros["57"]?.let { it.copyOf(8) })?.hex("")?.trimEnd('F')?.substringBefore('D')
        val validade = registros["5F24"]?.hex("")?.let { "${it.substring(2, 4)}/20${it.substring(0, 2)}" }
            ?: registros["57"]?.hex("")?.substringAfter('D', "")?.takeIf { it.length >= 4 }?.let { "${it.substring(2, 4)}/20${it.substring(0, 2)}" }
        val rede = when { aid.hex("").startsWith("A000000004") -> "Mastercard"; aid.hex("").startsWith("A000000003") -> "Visa"; aid.hex("").startsWith("A000000494") -> "Elo"
            aid.hex("").startsWith("A000000025") -> "American Express"; aid.hex("").startsWith("A0000001523010") -> "Discover"; else -> null }
        return listOf("Rede" to rede, "Aplicativo" to rotulo, "AID" to aid.hex(""), "Número" to pan?.let { "•••• ${it.takeLast(4)}" }, "Validade" to validade)
    } finally { runCatching { d.close() } }
}

@Composable
private fun LerCartao() {
    var info by remember { mutableStateOf<List<Pair<String, String?>>?>(null) }
    SessaoNfc("Encoste o cartão por aproximação atrás do celular e segure parado por 2 segundos.", { tag -> info = lerEmv(tag); "Cartão lido." }) {
        info?.let { FtInfos("Cartão", it) }
        FtNota("Mostra só os dados que qualquer maquininha lê sem senha. O número aparece mascarado e nada é guardado ou enviado.")
    }
}

// ---------- Gravação ----------

private enum class TipoConteudo(val rotulo: String) { Link("Link"), Texto("Texto"), Contato("Contato"), Wifi("Wi-Fi"), App("Abrir app") }

private fun registroTexto(texto: String): NdefRecord {
    val lang = "pt".toByteArray(Charsets.US_ASCII)
    val corpo = texto.toByteArray(Charsets.UTF_8)
    return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), byteArrayOf(lang.size.toByte()) + lang + corpo)
}

/** Registro Wi-Fi Simple Config (o Android conecta ao tocar a tag). */
private fun registroWifi(ssid: String, senha: String): NdefRecord {
    fun campo(tipo: Int, v: ByteArray) = byteArrayOf((tipo shr 8).toByte(), tipo.toByte(), (v.size shr 8).toByte(), v.size.toByte()) + v
    val cred = campo(0x1026, byteArrayOf(1)) + campo(0x1045, ssid.toByteArray()) + campo(0x1003, byteArrayOf(0, if (senha.isEmpty()) 1 else 0x20)) +
        campo(0x100F, byteArrayOf(0, if (senha.isEmpty()) 1 else 8)) + campo(0x1027, senha.toByteArray()) + campo(0x1020, byteArrayOf(-1, -1, -1, -1, -1, -1))
    return NdefRecord.createMime("application/vnd.wfa.wsc", campo(0x100E, cred))
}

@Composable
private fun EscreverNdef() {
    var tipo by rememberSaveable { mutableStateOf(TipoConteudo.Link) }
    var a by rememberSaveable { mutableStateOf("https://") }
    var b by rememberSaveable { mutableStateOf("") }
    var cTel by rememberSaveable { mutableStateOf("") }
    val mensagem: NdefMessage? = runCatching {
        when (tipo) {
            TipoConteudo.Link -> NdefMessage(NdefRecord.createUri(a.trim()))
            TipoConteudo.Texto -> NdefMessage(registroTexto(a))
            TipoConteudo.Contato -> NdefMessage(NdefRecord.createMime("text/vcard", "BEGIN:VCARD\nVERSION:3.0\nFN:$a\nTEL:$cTel\nEMAIL:$b\nEND:VCARD".toByteArray()))
            TipoConteudo.Wifi -> NdefMessage(registroWifi(a, b))
            TipoConteudo.App -> NdefMessage(NdefRecord.createApplicationRecord(a.trim()))
        }
    }.getOrNull()
    SessaoNfc("Preencha o conteúdo e aproxime a tag para gravar.", { tag -> mensagem?.let { gravarNdef(tag, it) } ?: "Falhou: confira os campos." }) {
        Column2 {
            OdSegmentado(TipoConteudo.entries.take(3).map { it to it.rotulo }, tipo.takeIf { it.ordinal < 3 } ?: TipoConteudo.Link, { tipo = it; a = if (it == TipoConteudo.Link) "https://" else "" })
            OdSegmentado(TipoConteudo.entries.drop(3).map { it to it.rotulo }, tipo.takeIf { it.ordinal >= 3 } ?: TipoConteudo.Wifi, { tipo = it; a = "" })
            when (tipo) {
                TipoConteudo.Link -> FtCampo(a, { a = it }, "Endereço")
                TipoConteudo.Texto -> FtCampo(a, { a = it }, "Texto", linhas = 3)
                TipoConteudo.Contato -> { FtCampo(a, { a = it }, "Nome"); FtCampo(cTel, { cTel = it }, "Telefone"); FtCampo(b, { b = it }, "E-mail") }
                TipoConteudo.Wifi -> { FtCampo(a, { a = it }, "Nome da rede"); FtCampo(b, { b = it }, "Senha") }
                TipoConteudo.App -> FtCampo(a, { a = it }, "Pacote do app (ex.: com.spotify.music)")
            }
            FtNota("Tamanho: ${mensagem?.byteArrayLength ?: 0} bytes. Uma NTAG213 guarda até 137 bytes; a NTAG215, 492.")
        }
    }
}

@Composable
private fun Column2(conteudo: @Composable () -> Unit) = androidx.compose.foundation.layout.Column(
    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(io.github.andrexagris.opendesign.OdEspacos.e3),
) { conteudo() }

@Composable
private fun ApagarTag() {
    SessaoNfc("Aproxime a tag para apagar o conteúdo. Tags novas são formatadas.", { tag ->
        gravarNdef(tag, NdefMessage(NdefRecord(NdefRecord.TNF_EMPTY, ByteArray(0), ByteArray(0), ByteArray(0)))).replace("Gravado na tag.", "Tag apagada.")
    })
}

@Composable
private fun CopiarTag() {
    var copiada by remember { mutableStateOf<NdefMessage?>(null) }
    SessaoNfc(if (copiada == null) "Primeiro aproxime a tag de origem." else "Agora aproxime a tag de destino para gravar a cópia.", { tag ->
        val m = copiada
        if (m == null) { val lida = lerNdef(tag); copiada = lida; if (lida == null) "Falhou: a tag de origem não tem conteúdo NDEF." else "Conteúdo copiado. Aproxime a tag de destino." }
        else gravarNdef(tag, m).also { if (!it.startsWith("Falhou")) copiada = null }
    }) {
        copiada?.let { m ->
            OdGrupoConfig("Copiado (${m.byteArrayLength} bytes)") { m.records.forEach { val (t, v) = descreverRegistro(it); OdLinhaConfig(t, descricao = v) } }
            OdBotao("Descartar cópia", { copiada = null }, Modifier.fillMaxWidth())
        }
        FtNota("Copia o conteúdo NDEF, não o UID: o número de série de cada tag é gravado de fábrica.")
    }
}

@Composable
private fun TravarTag() {
    var etapa by remember { mutableStateOf(0) } // 0 parado, 1 primeira confirmação, 2 segunda, 3 aguardando tag
    if (etapa == 3) SessaoNfc("Aproxime a tag que será travada para sempre.", { tag ->
        val n = Ndef.get(tag) ?: return@SessaoNfc "Falhou: a tag não tem NDEF. Grave algo antes."
        n.connect()
        try {
            if (!n.canMakeReadOnly()) "Falhou: esta tag não pode ser travada."
            else if (n.makeReadOnly()) { etapa = 0; "Tag travada como somente leitura." } else "Falhou: a tag recusou o travamento."
        } finally { runCatching { n.close() } }
    }) { OdBotao("Cancelar", { etapa = 0 }, Modifier.fillMaxWidth()) }
    else FtColuna {
        OdEstadoRecurso(OdTipoEstado.Indisponivel, "Travar é irreversível", "Depois de travada, a tag nunca mais pode ser gravada ou apagada, nem por este app.", icone = OdIcones.Cadeado)
        OdBotao("Travar uma tag", { etapa = 1 }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Destrutivo)
    }
    if (etapa == 1) OdDialogo("Travar a próxima tag?", "O conteúdo atual fica gravado para sempre e não pode mais ser mudado.", "Continuar", { etapa = 2 }, { etapa = 0 }, destrutivo = true, icone = OdIcones.Cadeado)
    if (etapa == 2) OdDialogo("Tem certeza mesmo?", "Não há como desfazer. Confirme só se o conteúdo da tag está certo.", "Travar para sempre", { etapa = 3 }, { etapa = 0 }, destrutivo = true, icone = OdIcones.Cadeado)
}

// ---------- NTAG21x ----------

private data class Ntag(val nome: String, val cfg0: Int)

private fun identificarNtag(a: NfcA): Ntag? {
    val v = runCatching { a.transceive(byteArrayOf(0x60)) }.getOrNull() ?: return null
    if (v.size < 8 || v[2] != 0x04.toByte()) return null
    return when (v[6].toInt()) { 0x0F -> Ntag("NTAG213", 0x29); 0x11 -> Ntag("NTAG215", 0x83); 0x13 -> Ntag("NTAG216", 0xE3); else -> null }
}

private fun NfcA.escrever(pagina: Int, dados: ByteArray) { transceive(byteArrayOf(0xA2.toByte(), pagina.toByte()) + dados.copyOf(4)) }
private fun NfcA.ler(pagina: Int): ByteArray = transceive(byteArrayOf(0x30, pagina.toByte()))
private fun NfcA.autenticar(senha: ByteArray): ByteArray? = runCatching { transceive(byteArrayOf(0x1B) + senha) }.getOrNull()

@Composable
private fun SenhaNtag() {
    var acao by rememberSaveable { mutableStateOf("testar") }
    var senha by rememberSaveable { mutableStateOf("") }
    var nova by rememberSaveable { mutableStateOf("") }
    SessaoNfc("Escolha a ação, digite a senha (8 dígitos hexadecimais, ex.: 12345678) e aproxime a tag.", { tag ->
        val a = NfcA.get(tag) ?: return@SessaoNfc "Falhou: não é uma tag NFC-A."
        a.connect()
        try {
            val t = identificarNtag(a) ?: return@SessaoNfc "Falhou: não é uma NTAG213, 215 ou 216."
            val pwd = runCatching { senha.hexParaBytes() }.getOrNull()?.takeIf { it.size == 4 }
            when (acao) {
                "testar" -> { pwd ?: return@SessaoNfc "Falhou: a senha precisa ter 8 dígitos hex."; if (a.autenticar(pwd) != null) "${t.nome}: senha correta." else "${t.nome}: senha errada." }
                "definir" -> {
                    val n = runCatching { nova.hexParaBytes() }.getOrNull()?.takeIf { it.size == 4 } ?: return@SessaoNfc "Falhou: a nova senha precisa ter 8 dígitos hex."
                    if (pwd != null && a.autenticar(pwd) == null) return@SessaoNfc "Falhou: senha atual errada."
                    a.escrever(t.cfg0 + 2, n)
                    a.escrever(t.cfg0 + 3, byteArrayOf(0, 0, 0, 0))
                    val cfg0 = a.ler(t.cfg0).copyOf(4); cfg0[3] = 0x04 // protege a partir da página 4
                    val cfg1 = a.ler(t.cfg0 + 1).copyOf(4); cfg1[0] = (cfg1[0].toInt() and 0x7F).toByte() // PROT = 0: só escrita
                    a.escrever(t.cfg0 + 1, cfg1)
                    a.escrever(t.cfg0, cfg0)
                    "${t.nome}: senha definida. A tag continua legível, mas só grava com a senha."
                }
                else -> {
                    pwd ?: return@SessaoNfc "Falhou: digite a senha atual."
                    if (a.autenticar(pwd) == null) return@SessaoNfc "Falhou: senha errada."
                    val cfg0 = a.ler(t.cfg0).copyOf(4); cfg0[3] = 0xFF.toByte()
                    a.escrever(t.cfg0, cfg0)
                    "${t.nome}: proteção removida."
                }
            }
        } finally { runCatching { a.close() } }
    }) {
        OdSegmentado(listOf("testar" to "Testar", "definir" to "Definir", "remover" to "Remover"), acao, { acao = it })
        FtCampo(senha, { senha = it }, if (acao == "definir") "Senha atual (deixe vazio se não tiver)" else "Senha")
        if (acao == "definir") FtCampo(nova, { nova = it }, "Nova senha")
        FtNota("Anote a senha: sem ela não dá para gravar de novo na tag. A proteção vale só para gravação; a leitura continua livre.")
    }
}

// ---------- Avançado ----------

fun dumpTag(tag: Tag): List<String> {
    MifareUltralight.get(tag)?.let { u ->
        u.connect()
        try {
            val linhas = mutableListOf<String>()
            var p = 0
            while (p < 256) {
                val b = runCatching { u.readPages(p) }.getOrNull() ?: break
                for (k in 0 until 4) { val pg = b.copyOfRange(k * 4, k * 4 + 4); linhas += "%03d  %s  %s".format(p + k, pg.hex(" "), pg.map { if (it in 32..126) it.toInt().toChar() else '.' }.joinToString("")) }
                p += 4
            }
            return linhas
        } finally { runCatching { u.close() } }
    }
    NfcV.get(tag)?.let { v ->
        v.connect()
        try {
            val linhas = mutableListOf<String>()
            for (bloco in 0 until 256) {
                val r = runCatching { v.transceive(byteArrayOf(0x22, 0x20) + tag.id + byteArrayOf(bloco.toByte())) }.getOrNull() ?: break
                if (r.isEmpty() || r[0].toInt() != 0) break
                linhas += "%03d  %s".format(bloco, r.copyOfRange(1, r.size).hex(" "))
            }
            return linhas
        } finally { runCatching { v.close() } }
    }
    if (MifareClassic.get(tag) != null) return lerClassic(tag, emptyList()).flatMap { s -> listOf("Setor ${s.setor} (${s.chave ?: "sem chave"})") + s.blocos }
    return listOf("Tipo de tag sem leitura de memória suportada (${tag.techList.joinToString { it.substringAfterLast('.') }}).")
}

@Composable
private fun DumpMemoria() {
    val c = LocalContext.current
    var linhas by remember { mutableStateOf<List<String>?>(null) }
    SessaoNfc("Aproxime e segure a tag parada até terminar.", { tag -> linhas = dumpTag(tag); "Memória lida: ${linhas?.size ?: 0} linhas." }) {
        linhas?.let { l ->
            OdBotao("Copiar tudo", { copiarTexto(c, l.joinToString("\n")) }, Modifier.fillMaxWidth())
            FtPainel { Text(l.joinToString("\n"), style = OdTema.tipografia.codigo, color = OdTema.cores.texto) }
        }
    }
}

data class SetorClassic(val setor: Int, val chave: String?, val blocos: List<String>)

private val CHAVES_PADRAO = listOf("FFFFFFFFFFFF", "A0A1A2A3A4A5", "D3F7D3F7D3F7", "000000000000", "B0B1B2B3B4B5", "4D3A99C351DD", "1A982C7E459A", "AABBCCDDEEFF", "714C5C886E97", "587EE5F9350F")

fun lerClassic(tag: Tag, extras: List<String>): List<SetorClassic> {
    val m = MifareClassic.get(tag) ?: return emptyList()
    m.connect()
    try {
        val chaves = (extras + CHAVES_PADRAO).mapNotNull { runCatching { it.hexParaBytes() }.getOrNull()?.takeIf { k -> k.size == 6 } }
        return (0 until m.sectorCount).map { s ->
            var usada: String? = null
            for (k in chaves) {
                if (runCatching { m.authenticateSectorWithKeyA(s, k) }.getOrDefault(false)) { usada = "A " + k.hex(""); break }
                if (runCatching { m.authenticateSectorWithKeyB(s, k) }.getOrDefault(false)) { usada = "B " + k.hex(""); break }
            }
            val blocos = if (usada == null) listOf("(nenhuma chave conhecida abriu este setor)") else {
                val primeiro = m.sectorToBlock(s)
                (0 until m.getBlockCountInSector(s)).map { b -> runCatching { m.readBlock(primeiro + b).hex(" ") }.getOrDefault("(erro de leitura)") }
            }
            SetorClassic(s, usada, blocos)
        }
    } finally { runCatching { m.close() } }
}

@Composable
private fun MifareClassicTela() {
    var chaves by rememberSaveable { mutableStateOf("") }
    var setores by remember { mutableStateOf<List<SetorClassic>?>(null) }
    SessaoNfc("Aproxime o cartão MIFARE Classic e segure parado (testar as chaves leva alguns segundos).", { tag ->
        if (MifareClassic.get(tag) == null) return@SessaoNfc "Falhou: não é MIFARE Classic, ou o NFC deste aparelho não lê esse tipo."
        setores = lerClassic(tag, chaves.lines().map { it.trim() }.filter { it.isNotEmpty() })
        "Leitura concluída: ${setores!!.count { it.chave != null }} de ${setores!!.size} setores abertos."
    }) {
        FtCampo(chaves, { chaves = it }, "Chaves extras (uma por linha, 12 dígitos hex)", linhas = 2)
        setores?.forEach { s ->
            OdGrupoConfig("Setor ${s.setor}${s.chave?.let { " · chave $it" } ?: ""}") {
                s.blocos.forEach { b -> OdLinhaConfig(b) }
            }
        }
        FtNota("Testa as chaves de fábrica mais comuns e as que você digitar. Muitos Galaxy não leem MIFARE Classic por causa do controlador NFC.")
    }
}

@Composable
private fun ClonarUid() {
    var uid by rememberSaveable { mutableStateOf("") }
    var chave by rememberSaveable { mutableStateOf("FFFFFFFFFFFF") }
    var etapa by remember { mutableStateOf(0) } // 0 ler origem, 1 gravar
    var confirmar by remember { mutableStateOf(false) }
    SessaoNfc(if (etapa == 0) "Aproxime o cartão de origem para ler o UID (ou digite abaixo)." else "Aproxime o cartão mágico (gen2/CUID) para gravar o UID.", { tag ->
        if (etapa == 0) { uid = tag.id.hex(""); "UID lido: $uid" }
        else {
            val m = MifareClassic.get(tag) ?: return@SessaoNfc "Falhou: o destino precisa ser um MIFARE Classic mágico."
            val novo = runCatching { uid.hexParaBytes() }.getOrNull()?.takeIf { it.size == 4 } ?: return@SessaoNfc "Falhou: o UID precisa ter 4 bytes (8 dígitos hex)."
            m.connect()
            try {
                if (!m.authenticateSectorWithKeyA(0, chave.hexParaBytes())) return@SessaoNfc "Falhou: a chave A do setor 0 não abriu o cartão."
                val bloco = m.readBlock(0)
                novo.copyInto(bloco, 0)
                bloco[4] = (novo[0].toInt() xor novo[1].toInt() xor novo[2].toInt() xor novo[3].toInt()).toByte()
                m.writeBlock(0, bloco)
                etapa = 0
                "UID gravado."
            } finally { runCatching { m.close() } }
        }
    }) {
        FtCampo(uid, { uid = it }, "UID (8 dígitos hex)")
        FtCampo(chave, { chave = it }, "Chave A do setor 0 do cartão mágico")
        if (etapa == 0) OdBotao("Gravar este UID num cartão mágico", { confirmar = true }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Destrutivo, habilitado = uid.length == 8)
        else OdBotao("Cancelar", { etapa = 0 }, Modifier.fillMaxWidth())
        FtNota("Só funciona com cartões \"mágicos\" de segunda geração, que aceitam gravar o bloco 0. Um bloco 0 errado pode inutilizar o cartão. Use só em cartões seus.")
    }
    if (confirmar) OdDialogo("Gravar o UID $uid?", "O bloco 0 do cartão mágico será substituído. Um erro pode inutilizar o cartão.", "Gravar UID", { confirmar = false; etapa = 1 }, { confirmar = false }, destrutivo = true)
}

private fun significadoSw(sw: String): String = when {
    sw == "9000" -> "OK"; sw.startsWith("61") -> "OK, há mais dados"; sw == "6A82" -> "Arquivo não encontrado"; sw == "6D00" -> "Comando não suportado"
    sw == "6E00" -> "Classe não suportada"; sw == "6982" -> "Segurança não satisfeita"; sw == "6700" -> "Tamanho errado"; sw.startsWith("6C") -> "Tamanho errado (use ${sw.takeLast(2)})"
    else -> "Erro"
}

@Composable
private fun ComandosApdu() {
    val c = LocalContext.current
    var tecnologia by rememberSaveable { mutableStateOf("isodep") }
    var comandos by rememberSaveable { mutableStateOf("00A404000E325041592E5359532E444446303100") }
    var saida by remember { mutableStateOf<List<String>>(emptyList()) }
    SessaoNfc("Digite os comandos (um por linha, em hex) e aproxime a tag ou cartão.", { tag ->
        val linhas = comandos.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val r = mutableListOf<String>()
        if (tecnologia == "isodep") {
            val d = IsoDep.get(tag) ?: return@SessaoNfc "Falhou: a tag não é ISO-DEP. Use NFC-A."
            d.connect(); d.timeout = 5000
            try { linhas.forEach { cmd -> val resp = d.transceive(cmd.hexParaBytes()); val sw = resp.takeLast(2).toByteArray().hex(""); r += "> $cmd"; r += "< ${resp.hex("")}  [$sw ${significadoSw(sw)}]" } }
            finally { runCatching { d.close() } }
        } else {
            val a = NfcA.get(tag) ?: return@SessaoNfc "Falhou: a tag não é NFC-A."
            a.connect()
            try { linhas.forEach { cmd -> r += "> $cmd"; r += "< " + runCatching { a.transceive(cmd.hexParaBytes()).hex("") }.getOrElse { "sem resposta (${it.message})" } } }
            finally { runCatching { a.close() } }
        }
        saida = r
        "${linhas.size} comandos enviados."
    }) {
        OdSegmentado(listOf("isodep" to "ISO-DEP (APDU)", "nfca" to "NFC-A (bruto)"), tecnologia, { tecnologia = it })
        FtCampo(comandos, { comandos = it }, "Comandos (hex)", linhas = 4)
        if (saida.isNotEmpty()) {
            FtPainel { Text(saida.joinToString("\n"), style = OdTema.tipografia.codigo, color = OdTema.cores.texto) }
            OdBotao("Copiar resultado", { copiarTexto(c, saida.joinToString("\n")) }, Modifier.fillMaxWidth())
        }
    }
}

/** Ações que uma tag pode disparar (abertas pela AtividadeGatilho, sem root). */
enum class AcaoGatilho(val id: String, val rotulo: String) {
    Lanterna("lanterna", "Ligar ou desligar a lanterna"), Wifi("wifi", "Abrir as configurações de Wi-Fi"), Bluetooth("bluetooth", "Abrir as configurações de Bluetooth"),
    App("app", "Abrir um app"), Link("link", "Abrir um link"), Velocidade("velocidade", "Ligar ou desligar a velocidade na barra"),
}

fun uriGatilho(acao: AcaoGatilho, valor: String = ""): Uri =
    Uri.Builder().scheme("ferramentas").authority("acao").appendPath(acao.id).apply { if (valor.isNotBlank()) appendQueryParameter("v", valor) }.build()

@Composable
private fun TagGatilho() {
    val c = LocalContext.current
    var acao by rememberSaveable { mutableStateOf(AcaoGatilho.Lanterna) }
    var valor by rememberSaveable { mutableStateOf("") }
    var gravar by remember { mutableStateOf(false) }
    if (gravar) SessaoNfc("Aproxime a tag para gravar o gatilho.", { tag ->
        gravarNdef(tag, NdefMessage(NdefRecord.createUri(uriGatilho(acao, valor)), NdefRecord.createApplicationRecord(c.packageName))).also { if (!it.startsWith("Falhou")) gravar = false }
    }) { OdBotao("Cancelar", { gravar = false }, Modifier.fillMaxWidth()) }
    else FtColuna {
        OdGrupoConfig("Ao encostar o celular na tag") {
            AcaoGatilho.entries.forEach { a -> OdLinhaOpcao(a.rotulo, acao == a, { acao = a; valor = "" }) }
        }
        if (acao == AcaoGatilho.App) FtCampo(valor, { valor = it }, "Pacote do app (ex.: com.whatsapp)")
        if (acao == AcaoGatilho.Link) FtCampo(valor, { valor = it }, "Endereço")
        OdBotao("Gravar na tag", { gravar = true }, Modifier.fillMaxWidth(), tipo = OdTipoBotao.Primario)
        FtNota("Com a tela desbloqueada, encostar o celular na tag dispara a ação, mesmo com o app fechado. Ações do sistema como modo avião não são permitidas sem root.")
    }
}

