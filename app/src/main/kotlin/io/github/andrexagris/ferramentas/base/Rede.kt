package io.github.andrexagris.ferramentas.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/** Rede ativa: IPv4 local, prefixo, gateway, DNS e tipo. */
data class InfoRede(
    val ipv4: Inet4Address?,
    val prefixo: Int,
    val gateway: InetAddress?,
    val dns: List<InetAddress>,
    val ipv6: List<InetAddress>,
    val wifi: Boolean,
    val celular: Boolean,
    val vpn: Boolean,
    val interfaceNome: String?,
    val dominio: String?,
    val mtu: Int,
)

fun Context.infoRede(): InfoRede? {
    val cm = getSystemService(ConnectivityManager::class.java)
    val rede = cm.activeNetwork ?: return null
    val lp: LinkProperties = cm.getLinkProperties(rede) ?: return null
    val cap = cm.getNetworkCapabilities(rede)
    val v4 = lp.linkAddresses.firstOrNull { it.address is Inet4Address }
    return InfoRede(
        v4?.address as? Inet4Address, v4?.prefixLength ?: 24,
        lp.routes.firstOrNull { it.isDefaultRoute && it.gateway is Inet4Address }?.gateway ?: lp.routes.firstOrNull { it.isDefaultRoute }?.gateway,
        lp.dnsServers, lp.linkAddresses.map { it.address }.filter { it !is Inet4Address },
        cap?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true,
        cap?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true,
        cap?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true,
        lp.interfaceName, lp.domains, lp.mtu,
    )
}

fun Int.paraIpv4(): String = "${this ushr 24 and 255}.${this ushr 16 and 255}.${this ushr 8 and 255}.${this and 255}"
fun Inet4Address.paraInt(): Int = address.fold(0) { acc, b -> (acc shl 8) or (b.toInt() and 255) }
fun ipv4ParaInt(texto: String): Int? {
    val p = texto.trim().split('.')
    if (p.size != 4) return null
    val n = p.map { it.toIntOrNull()?.takeIf { v -> v in 0..255 } ?: return null }
    return (n[0] shl 24) or (n[1] shl 16) or (n[2] shl 8) or n[3]
}
fun mascaraDe(prefixo: Int): Int = if (prefixo == 0) 0 else (-1 shl (32 - prefixo))

/** Tenta abrir uma conexão TCP. Devolve a latência em ms ou null. */
suspend fun tcpAberta(host: String, porta: Int, timeoutMs: Int = 800): Long? = withContext(Dispatchers.IO) {
    val t0 = System.nanoTime()
    runCatching {
        Socket().use { it.connect(InetSocketAddress(host, porta), timeoutMs) }
        (System.nanoTime() - t0) / 1_000_000
    }.getOrNull()
}

/** Resultado de um ping do sistema. */
data class Ping(val ok: Boolean, val ms: Double?, val ttl: Int?, val de: String?, val linha: String)

/**
 * Roda o ping do Android (/system/bin/ping) uma vez. ttl: limite de saltos (traceroute).
 * O ping do sistema funciona sem root para ICMP.
 */
suspend fun pingUmaVez(host: String, timeoutS: Int = 2, ttl: Int? = null): Ping = withContext(Dispatchers.IO) {
    val args = mutableListOf("/system/bin/ping", "-c", "1", "-W", timeoutS.toString())
    if (ttl != null) { args += "-t"; args += ttl.toString() }
    args += host
    runCatching {
        val p = ProcessBuilder(args).redirectErrorStream(true).start()
        val saida = p.inputStream.bufferedReader().readText()
        p.waitFor()
        val ms = Regex("time=([0-9.]+) ms").find(saida)?.groupValues?.get(1)?.toDoubleOrNull()
        val ttlR = Regex("ttl=(\\d+)").find(saida)?.groupValues?.get(1)?.toIntOrNull()
        val de = Regex("[Ff]rom ([0-9a-fA-F.:]+)").find(saida)?.groupValues?.get(1)
            ?: Regex("bytes from ([0-9a-fA-F.:]+)").find(saida)?.groupValues?.get(1)
        val linha = saida.lines().firstOrNull { "from" in it.lowercase() || "time" in it } ?: saida.lines().firstOrNull { it.isNotBlank() } ?: ""
        Ping(ms != null, ms, ttlR, de, linha.trim())
    }.getOrElse { Ping(false, null, null, null, it.message ?: "erro") }
}

suspend fun resolver(host: String): InetAddress? = withContext(Dispatchers.IO) { runCatching { InetAddress.getByName(host.trim()) }.getOrNull() }

/** Nome reverso de um IP (PTR), se houver. */
suspend fun nomeReverso(ip: String): String? = withContext(Dispatchers.IO) {
    runCatching { InetAddress.getByName(ip).canonicalHostName.takeIf { it != ip } }.getOrNull()
}

/** Portas comuns e seus serviços (scanner de portas e de LAN). */
val PORTAS_COMUNS = linkedMapOf(
    21 to "FTP", 22 to "SSH", 23 to "Telnet", 25 to "SMTP", 53 to "DNS", 80 to "HTTP", 110 to "POP3", 139 to "NetBIOS",
    143 to "IMAP", 443 to "HTTPS", 445 to "SMB", 515 to "LPD", 548 to "AFP", 554 to "RTSP", 631 to "IPP", 993 to "IMAPS",
    1883 to "MQTT", 3306 to "MySQL", 3389 to "RDP", 5000 to "UPnP", 5353 to "mDNS", 5432 to "PostgreSQL", 5900 to "VNC",
    8000 to "HTTP alt.", 8008 to "Chromecast", 8080 to "HTTP proxy", 8443 to "HTTPS alt.", 8888 to "HTTP alt.", 9100 to "Impressora",
    32400 to "Plex", 62078 to "iPhone sync",
)
