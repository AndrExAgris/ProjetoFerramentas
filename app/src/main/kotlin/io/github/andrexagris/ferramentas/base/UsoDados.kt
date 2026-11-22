package io.github.andrexagris.ferramentas.base

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.NetworkCapabilities
import android.os.Process
import java.util.Calendar

/** Uso de dados de um app (ou de um grupo de uid do sistema) num período. */
data class UsoApp(val uid: Int, val nome: String, val pacote: String?, val wifiBytes: Long, val movelBytes: Long) {
    val total: Long get() = wifiBytes + movelBytes
}

enum class Periodo(val rotulo: String) { Hoje("Hoje"), Semana("7 dias"), Mes("30 dias") }

fun inicioDe(p: Periodo): Long {
    val c = Calendar.getInstance()
    when (p) {
        Periodo.Hoje -> { c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0) }
        Periodo.Semana -> c.add(Calendar.DAY_OF_YEAR, -7)
        Periodo.Mes -> c.add(Calendar.DAY_OF_YEAR, -30)
    }
    return c.timeInMillis
}

/** Bytes por uid num tipo de rede (TRANSPORT_WIFI ou TRANSPORT_CELLULAR). Exige "Acesso ao uso". */
fun Context.bytesPorUid(transporte: Int, inicio: Long, fim: Long = System.currentTimeMillis()): Map<Int, Long> {
    val nsm = getSystemService(NetworkStatsManager::class.java)
    val tipo = if (transporte == NetworkCapabilities.TRANSPORT_WIFI) android.net.ConnectivityManager.TYPE_WIFI else android.net.ConnectivityManager.TYPE_MOBILE
    val mapa = HashMap<Int, Long>()
    runCatching {
        val st = nsm.querySummary(tipo, null, inicio, fim)
        val b = NetworkStats.Bucket()
        while (st.hasNextBucket()) {
            st.getNextBucket(b)
            mapa[b.uid] = (mapa[b.uid] ?: 0L) + b.rxBytes + b.txBytes
        }
        st.close()
    }
    return mapa
}

/** Total do aparelho num tipo de rede. */
fun Context.bytesAparelho(movel: Boolean, inicio: Long, fim: Long = System.currentTimeMillis()): Long {
    val nsm = getSystemService(NetworkStatsManager::class.java)
    val tipo = if (movel) android.net.ConnectivityManager.TYPE_MOBILE else android.net.ConnectivityManager.TYPE_WIFI
    return runCatching { nsm.querySummaryForDevice(tipo, null, inicio, fim).let { it.rxBytes + it.txBytes } }.getOrDefault(0L)
}

/** Nome legível de um uid (app instalado, ou grupos especiais do sistema). */
fun Context.nomeDoUid(uid: Int): Pair<String, String?> {
    when (uid) {
        NetworkStats.Bucket.UID_REMOVED -> return "Apps desinstalados" to null
        NetworkStats.Bucket.UID_TETHERING -> return "Roteador do celular (ancoragem)" to null
        Process.SYSTEM_UID, 0 -> return "Sistema Android" to null
        -1 -> return "Desconhecido" to null
    }
    val pm = packageManager
    val pacotes = pm.getPackagesForUid(uid)
    val pacote = pacotes?.firstOrNull() ?: return (pm.getNameForUid(uid)?.substringBefore(':')?.removePrefix("android.uid.")?.let { "Sistema: $it" } ?: "uid $uid") to null
    val nome = runCatching { pm.getApplicationLabel(pm.getApplicationInfo(pacote, 0)).toString() }.getOrDefault(pacote)
    return (if (pacotes.size > 1) "$nome e outros" else nome) to pacote
}

fun Context.iconeDoApp(pacote: String?): Drawable? = pacote?.let { runCatching { packageManager.getApplicationIcon(it) }.getOrNull() }

/** Uso de todos os apps num período, do maior para o menor. */
fun Context.usoPorApp(periodo: Periodo): List<UsoApp> {
    val inicio = inicioDe(periodo)
    val wifi = bytesPorUid(NetworkCapabilities.TRANSPORT_WIFI, inicio)
    val movel = bytesPorUid(NetworkCapabilities.TRANSPORT_CELLULAR, inicio)
    return (wifi.keys + movel.keys).map { uid ->
        val (nome, pacote) = nomeDoUid(uid)
        UsoApp(uid, nome, pacote, wifi[uid] ?: 0, movel[uid] ?: 0)
    }.filter { it.total > 0 }.sortedByDescending { it.total }
}

fun ApplicationInfo.ehSistema(): Boolean = flags and ApplicationInfo.FLAG_SYSTEM != 0

fun PackageManager.rotulo(pacote: String): String = runCatching { getApplicationLabel(getApplicationInfo(pacote, 0)).toString() }.getOrDefault(pacote)
