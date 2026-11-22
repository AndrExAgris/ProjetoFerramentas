package io.github.andrexagris.ferramentas.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs

/** Estado da bateria vindo do ACTION_BATTERY_CHANGED. */
data class EstadoBateria(
    val nivel: Float,              // %
    val status: Int,               // BatteryManager.BATTERY_STATUS_*
    val fonte: Int,                // BatteryManager.BATTERY_PLUGGED_* (0 = bateria)
    val temperatura: Float,        // °C
    val voltagem: Float,           // V
    val saude: Int,                // BatteryManager.BATTERY_HEALTH_*
    val tecnologia: String?,
    val ciclos: Int?,              // Android 14+, se o fabricante preencher
) {
    val carregando: Boolean get() = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val conectado: Boolean get() = fonte != 0

    companion object {
        fun de(i: Intent): EstadoBateria {
            val nivel = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val escala = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
            val ciclos = if (Build.VERSION.SDK_INT >= 34) i.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1).takeIf { it >= 0 } else null
            return EstadoBateria(
                nivel * 100f / escala,
                i.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN),
                i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0),
                i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f,
                i.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).let { if (it > 100) it / 1000f else it.toFloat() },
                i.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN),
                i.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY),
                ciclos,
            )
        }
    }
}

fun Context.estadoBateriaAgora(): EstadoBateria? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let { EstadoBateria.de(it) }

fun Context.fluxoBateria(): Flow<EstadoBateria> = callbackFlow {
    val r = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) { trySend(EstadoBateria.de(i)) }
    }
    registerReceiver(r, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let { trySend(EstadoBateria.de(it)) }
    awaitClose { unregisterReceiver(r) }
}

@Composable
fun rememberBateria(): State<EstadoBateria?> {
    val c = LocalContext.current
    return produceState<EstadoBateria?>(null) { c.fluxoBateria().collect { value = it } }
}

/**
 * Corrente da bateria em mA, positiva entrando (carregando) e negativa saindo.
 * O Android define CURRENT_NOW em µA com positivo entrando, mas muitos fabricantes (Samsung incluída)
 * devolvem mA e/ou o sinal invertido. Aqui: valores acima de 20 000 em módulo são µA (nenhum celular
 * consome 20 A); o sinal é corrigido pelo estado de carga quando dá para saber.
 */
fun Context.correnteMa(estado: EstadoBateria? = estadoBateriaAgora()): Float? {
    val bm = getSystemService(BatteryManager::class.java)
    val bruto = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
    if (bruto == Int.MIN_VALUE || bruto == 0) return null
    var ma = if (abs(bruto) > 20_000) bruto / 1000f else bruto.toFloat()
    if (estado != null) {
        val carregando = estado.status == BatteryManager.BATTERY_STATUS_CHARGING
        val descarregando = estado.status == BatteryManager.BATTERY_STATUS_DISCHARGING || estado.status == BatteryManager.BATTERY_STATUS_NOT_CHARGING
        if ((carregando && ma < 0) || (descarregando && ma > 0)) ma = -ma
    }
    return ma
}

/** Carga restante em mAh (CHARGE_COUNTER, que o Android define em µAh; alguns devolvem mAh). */
fun Context.cargaMah(): Float? {
    val bm = getSystemService(BatteryManager::class.java)
    val v = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    if (v == Int.MIN_VALUE || v <= 0) return null
    return if (v > 100_000) v / 1000f else v.toFloat()
}

/** Corrente ao vivo, lida a cada intervalo. */
@Composable
fun rememberCorrente(intervaloMs: Long = 1000): State<Float?> {
    val c = LocalContext.current
    return produceState<Float?>(null) {
        while (true) { value = c.correnteMa(); delay(intervaloMs) }
    }
}

fun textoStatus(s: Int) = when (s) {
    BatteryManager.BATTERY_STATUS_CHARGING -> "Carregando"
    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descarregando"
    BatteryManager.BATTERY_STATUS_FULL -> "Carga completa"
    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Conectado, sem carregar"
    else -> "Desconhecido"
}

fun textoFonte(f: Int) = when (f) {
    BatteryManager.BATTERY_PLUGGED_AC -> "Tomada"
    BatteryManager.BATTERY_PLUGGED_USB -> "USB"
    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Sem fio"
    BatteryManager.BATTERY_PLUGGED_DOCK -> "Base"
    0 -> "Bateria"
    else -> "Outra"
}

fun textoSaude(h: Int) = when (h) {
    BatteryManager.BATTERY_HEALTH_GOOD -> "Boa"
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Superaquecida"
    BatteryManager.BATTERY_HEALTH_DEAD -> "Esgotada"
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Sobretensão"
    BatteryManager.BATTERY_HEALTH_COLD -> "Fria demais"
    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Falha"
    else -> "Desconhecida"
}

/** Capacidade de fábrica dos aparelhos conhecidos (o Android não informa). */
data class ModeloBateria(val nome: String, val mah: Int, val tipo: String)

fun modeloBateria(): ModeloBateria? {
    val m = Build.MODEL.uppercase()
    return when {
        m.startsWith("SM-G780") || m.startsWith("SM-G781") -> ModeloBateria("Galaxy S20 FE", 4500, "Li-Po")
        m.startsWith("SM-A245") -> ModeloBateria("Galaxy A24", 5000, "Li-Po")
        m.startsWith("SM-A546") -> ModeloBateria("Galaxy A54", 5000, "Li-Po")
        m.startsWith("SM-S911") -> ModeloBateria("Galaxy S23", 3900, "Li-Po")
        m.startsWith("SM-S921") -> ModeloBateria("Galaxy S24", 4000, "Li-Po")
        else -> null
    }
}
