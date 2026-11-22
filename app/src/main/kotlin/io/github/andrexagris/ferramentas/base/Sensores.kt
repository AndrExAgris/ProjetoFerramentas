package io.github.andrexagris.ferramentas.base

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/** Uma leitura de sensor: valores (cópia) e instante em nanossegundos (relógio do sensor). */
class LeituraSensor(val valores: FloatArray, val tempoNs: Long, val precisao: Int) {
    operator fun get(i: Int): Float = valores.getOrElse(i) { 0f }
}

/**
 * Qualquer sensor do Android como Flow. Registra ao coletar e desregistra ao cancelar.
 * atrasoUs: SensorManager.SENSOR_DELAY_* ou um período em microssegundos.
 */
fun Context.fluxoSensor(tipo: Int, atrasoUs: Int = SensorManager.SENSOR_DELAY_UI): Flow<LeituraSensor> = callbackFlow {
    val sm = getSystemService(SensorManager::class.java)
    val sensor = sm?.getDefaultSensor(tipo)
    if (sensor == null) {
        close()
        return@callbackFlow
    }
    val ouvinte = object : SensorEventListener {
        override fun onSensorChanged(e: SensorEvent) {
            trySend(LeituraSensor(e.values.copyOf(), e.timestamp, e.accuracy))
        }

        override fun onAccuracyChanged(s: Sensor?, precisao: Int) {}
    }
    sm.registerListener(ouvinte, sensor, atrasoUs)
    awaitClose { sm.unregisterListener(ouvinte) }
}

/** Última leitura de um sensor, para a tela. null até a primeira leitura (ou se o sensor não existir). */
@Composable
fun rememberSensor(tipo: Int, atrasoUs: Int = SensorManager.SENSOR_DELAY_UI): State<LeituraSensor?> {
    val contexto = LocalContext.current
    return produceState<LeituraSensor?>(null, tipo, atrasoUs) {
        contexto.fluxoSensor(tipo, atrasoUs).conflate().collect { value = it }
    }
}

fun Context.temSensor(tipo: Int): Boolean = getSystemService(SensorManager::class.java)?.getDefaultSensor(tipo) != null

/** Série de N pontos para gráficos ao vivo (OdGraficoTempoReal). */
class Serie(private val max: Int = 200) {
    val pontos: SnapshotStateList<Float> = mutableStateListOf()
    fun add(v: Float) {
        pontos.add(v)
        if (pontos.size > max) pontos.removeRange(0, pontos.size - max)
    }
    fun limpar() = pontos.clear()
}

/** Mínimo e máximo desde o início ou desde o último zerar. */
class MinMax {
    val min = mutableStateOf(Float.NaN)
    val max = mutableStateOf(Float.NaN)
    fun add(v: Float) {
        if (min.value.isNaN() || v < min.value) min.value = v
        if (max.value.isNaN() || v > max.value) max.value = v
    }
    fun zerar() { min.value = Float.NaN; max.value = Float.NaN }
}

/** Três séries X, Y e Z alimentadas por um sensor. */
@Composable
fun rememberSeriesXyz(leitura: LeituraSensor?, max: Int = 200): Triple<Serie, Serie, Serie> {
    val s = remember { Triple(Serie(max), Serie(max), Serie(max)) }
    LaunchedEffect(leitura) {
        leitura ?: return@LaunchedEffect
        s.first.add(leitura[0]); s.second.add(leitura[1]); s.third.add(leitura[2])
    }
    return s
}

/** Orientação (azimute, inclinação para frente e lateral, em graus) a partir do rotation vector. */
fun orientacao(rotationVector: FloatArray): FloatArray {
    val r = FloatArray(9)
    SensorManager.getRotationMatrixFromVector(r, rotationVector)
    val o = FloatArray(3)
    SensorManager.getOrientation(r, o)
    return floatArrayOf(
        ((Math.toDegrees(o[0].toDouble()) + 360) % 360).toFloat(),
        Math.toDegrees(o[1].toDouble()).toFloat(),
        Math.toDegrees(o[2].toDouble()).toFloat(),
    )
}
