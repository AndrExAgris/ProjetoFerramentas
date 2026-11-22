package io.github.andrexagris.ferramentas.servicos

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import io.github.andrexagris.ferramentas.R
import io.github.andrexagris.ferramentas.base.Canais
import io.github.andrexagris.ferramentas.base.IdsNotificacao
import io.github.andrexagris.ferramentas.base.intentAbrirApp
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/** Uma consulta de nome feita por um app. */
data class ConsultaDns(val tempo: Long, val uid: Int, val dominio: String, val tipo: String)

/**
 * VPN local que só intercepta o DNS. O Android manda as consultas de nome para o endereço falso desta VPN;
 * o serviço descobre qual app fez cada consulta (getConnectionOwnerUid), repassa ao DNS real por um socket
 * protegido e devolve a resposta. O resto do tráfego não passa pela VPN, então nada fica mais lento e
 * nenhum servidor externo é usado. Limitação: apps com DNS próprio (DNS seguro estrito, DoH) não aparecem.
 */
class ServicoVpnDns : VpnService() {
    private var interfaceVpn: ParcelFileDescriptor? = null
    @Volatile private var rodando = false
    private val repasse = Executors.newFixedThreadPool(4)
    private var dnsReal: InetAddress = InetAddress.getByName("1.1.1.1")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACAO_PARAR) { parar(); return START_NOT_STICKY }
        val n = NotificationCompat.Builder(this, Canais.MEDICOES).setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle("Vendo conexões por app").setContentText("VPN local só para DNS, sem servidor externo.")
            .setOngoing(true).setContentIntent(intentAbrirApp())
            .addAction(0, "Parar", PendingIntent.getService(this, 3, Intent(this, ServicoVpnDns::class.java).setAction(ACAO_PARAR), PendingIntent.FLAG_IMMUTABLE))
            .build()
        ServiceCompat.startForeground(this, IdsNotificacao.SERVICO_VPN, n, io.github.andrexagris.ferramentas.base.TIPO_USO_ESPECIAL)
        if (rodando) return START_STICKY
        val cm = getSystemService(ConnectivityManager::class.java)
        cm.activeNetwork?.let { cm.getLinkProperties(it) }?.dnsServers?.firstOrNull { it is Inet4Address }?.let { dnsReal = it }
        interfaceVpn = Builder()
            .setSession("Ferramentas: DNS por app")
            .addAddress(ENDERECO_LOCAL, 32)
            .addRoute(ENDERECO_DNS, 32)
            .addDnsServer(ENDERECO_DNS)
            .setBlocking(true)
            .establish()
        val fd = interfaceVpn ?: run { parar(); return START_NOT_STICKY }
        rodando = true
        ativo.value = true
        Thread({ laco(fd) }, "vpn-dns").start()
        return START_STICKY
    }

    private fun laco(fd: ParcelFileDescriptor) {
        val entrada = FileInputStream(fd.fileDescriptor)
        val saida = FileOutputStream(fd.fileDescriptor)
        val cm = getSystemService(ConnectivityManager::class.java)
        val buf = ByteArray(32767)
        while (rodando) {
            val n = runCatching { entrada.read(buf) }.getOrElse { -1 }
            if (n <= 0) { if (!rodando) break else continue }
            val pacote = buf.copyOf(n)
            if ((pacote[0].toInt() shr 4) != 4 || pacote[9].toInt() != 17) continue // só IPv4/UDP
            val ihl = (pacote[0].toInt() and 0x0F) * 4
            val portaOrigem = u16(pacote, ihl)
            val portaDestino = u16(pacote, ihl + 2)
            if (portaDestino != 53) continue
            val origem = InetAddress.getByAddress(pacote.copyOfRange(12, 16))
            val destino = InetAddress.getByAddress(pacote.copyOfRange(16, 20))
            val consulta = pacote.copyOfRange(ihl + 8, n)
            val uid = runCatching {
                cm.getConnectionOwnerUid(OsConstants.IPPROTO_UDP, InetSocketAddress(origem, portaOrigem), InetSocketAddress(destino, 53))
            }.getOrDefault(-1)
            lerPergunta(consulta)?.let { (nome, tipo) ->
                val lista = consultas.value
                consultas.value = (listOf(ConsultaDns(System.currentTimeMillis(), uid, nome, tipo)) + lista).take(800)
            }
            repasse.execute {
                runCatching {
                    DatagramSocket().use { s ->
                        protect(s)
                        s.soTimeout = 5000
                        s.send(DatagramPacket(consulta, consulta.size, dnsReal, 53))
                        val r = ByteArray(4096)
                        val dp = DatagramPacket(r, r.size)
                        s.receive(dp)
                        val resposta = montarUdp(destino, 53, origem, portaOrigem, r.copyOf(dp.length))
                        synchronized(saida) { saida.write(resposta) }
                    }
                }
            }
        }
    }

    private fun parar() {
        rodando = false
        ativo.value = false
        runCatching { interfaceVpn?.close() }
        interfaceVpn = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onRevoke() = parar()

    override fun onDestroy() {
        rodando = false
        ativo.value = false
        runCatching { interfaceVpn?.close() }
        repasse.shutdownNow()
        super.onDestroy()
    }

    companion object {
        const val ACAO_PARAR = "parar"
        private const val ENDERECO_LOCAL = "10.111.222.1"
        private const val ENDERECO_DNS = "10.111.222.53"

        val ativo = MutableStateFlow(false)
        val consultas = MutableStateFlow<List<ConsultaDns>>(emptyList())

        fun iniciar(c: Context) = c.startForegroundService(Intent(c, ServicoVpnDns::class.java))
        fun parar(c: Context) { c.startService(Intent(c, ServicoVpnDns::class.java).setAction(ACAO_PARAR)) }

        private fun u16(b: ByteArray, i: Int) = ((b[i].toInt() and 0xFF) shl 8) or (b[i + 1].toInt() and 0xFF)

        /** Nome e tipo da primeira pergunta de uma mensagem DNS. */
        fun lerPergunta(m: ByteArray): Pair<String, String>? {
            if (m.size < 17) return null
            var i = 12
            val partes = mutableListOf<String>()
            while (i < m.size) {
                val len = m[i].toInt() and 0xFF
                if (len == 0) { i++; break }
                if (len > 63 || i + 1 + len > m.size) return null
                partes += String(m, i + 1, len, Charsets.US_ASCII)
                i += 1 + len
            }
            if (i + 2 > m.size) return null
            val tipo = when (u16(m, i)) { 1 -> "A"; 28 -> "AAAA"; 5 -> "CNAME"; 15 -> "MX"; 16 -> "TXT"; 65 -> "HTTPS"; 33 -> "SRV"; 12 -> "PTR"; else -> u16(m, i).toString() }
            return partes.joinToString(".") to tipo
        }

        /** Pacote IPv4 + UDP com a carga dada (checksum UDP zerado, permitido no IPv4). */
        fun montarUdp(de: InetAddress, portaDe: Int, para: InetAddress, portaPara: Int, carga: ByteArray): ByteArray {
            val total = 20 + 8 + carga.size
            val p = ByteArray(total)
            p[0] = 0x45; p[2] = (total shr 8).toByte(); p[3] = total.toByte()
            p[6] = 0x40; p[8] = 64; p[9] = 17
            System.arraycopy(de.address, 0, p, 12, 4); System.arraycopy(para.address, 0, p, 16, 4)
            var soma = 0
            for (k in 0 until 20 step 2) soma += u16(p, k)
            while (soma shr 16 != 0) soma = (soma and 0xFFFF) + (soma shr 16)
            val cs = soma.inv() and 0xFFFF
            p[10] = (cs shr 8).toByte(); p[11] = cs.toByte()
            p[20] = (portaDe shr 8).toByte(); p[21] = portaDe.toByte()
            p[22] = (portaPara shr 8).toByte(); p[23] = portaPara.toByte()
            p[24] = ((8 + carga.size) shr 8).toByte(); p[25] = (8 + carga.size).toByte()
            System.arraycopy(carga, 0, p, 28, carga.size)
            return p
        }
    }
}
