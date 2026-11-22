package io.github.andrexagris.ferramentas.catalogo

import android.content.Context
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibratorManager

/** Se a ferramenta roda neste aparelho. Indisponível traz o motivo, mostrado em vez de abrir e falhar. */
sealed interface Disponibilidade {
    data object Disponivel : Disponibilidade
    data class Indisponivel(val motivo: String) : Disponibilidade
}

val Disponibilidade.disponivel: Boolean get() = this is Disponibilidade.Disponivel

/**
 * Confere os requisitos de hardware, sensores e versão do Android de cada ferramenta.
 * O resultado não muda enquanto o app roda, então é calculado uma vez para o catálogo inteiro.
 */
class VerificadorDisponibilidade(contexto: Context) {
    private val pm = contexto.packageManager
    private val sensores = contexto.getSystemService(SensorManager::class.java)
    private val cameras = contexto.getSystemService(CameraManager::class.java)
    private val vibradores = contexto.getSystemService(VibratorManager::class.java)

    private val cache = HashMap<Requisito, Boolean>()

    fun atende(r: Requisito): Boolean = cache.getOrPut(r) {
        when (r) {
            is Requisito.Recurso -> pm.hasSystemFeature(r.recurso)
            is Requisito.Sensor -> sensores?.getDefaultSensor(r.tipo) != null
            is Requisito.AndroidMinimo -> Build.VERSION.SDK_INT >= r.api
            Requisito.CameraProfundidade -> temCameraDeProfundidade()
            Requisito.Vibrador -> vibradores?.defaultVibrator?.hasVibrator() == true
            is Requisito.SemApiPadrao -> false
        }
    }

    fun verificar(f: Ferramenta): Disponibilidade =
        f.requisitos.firstOrNull { !atende(it) }?.let { Disponibilidade.Indisponivel(it.falta) } ?: Disponibilidade.Disponivel

    fun verificarTodas(): Map<String, Disponibilidade> = Catalogo.ferramentas.associate { it.id to verificar(it) }

    private fun temCameraDeProfundidade(): Boolean = try {
        cameras?.cameraIdList.orEmpty().any { id ->
            cameras!!.getCameraCharacteristics(id).get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                ?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT) == true
        }
    } catch (e: Exception) {
        false
    }
}
