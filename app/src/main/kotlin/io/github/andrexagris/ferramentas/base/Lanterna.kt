package io.github.andrexagris.ferramentas.base

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Lanterna (flash da câmera traseira) para a ferramenta, o bloco do painel rápido, o widget e as tags NFC.
 * O estado acompanha o sistema (TorchCallback), então liga e desliga por fora também aparecem aqui.
 */
object Lanterna {
    val ligada = MutableStateFlow(false)
    private var registrada = false

    private fun gerenciador(c: Context) = c.getSystemService(CameraManager::class.java)

    /** Id da câmera com flash (traseira, de preferência). */
    fun idCamera(c: Context): String? = runCatching {
        val cm = gerenciador(c)
        cm.cameraIdList.sortedBy { id -> if (cm.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) 0 else 1 }
            .firstOrNull { cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
    }.getOrNull()

    fun observar(c: Context) {
        if (registrada) return
        registrada = true
        runCatching {
            gerenciador(c).registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(id: String, ativa: Boolean) { if (id == idCamera(c)) ligada.value = ativa }
            }, Handler(Looper.getMainLooper()))
        }
    }

    fun definir(c: Context, ligar: Boolean): Boolean {
        val id = idCamera(c) ?: return false
        return runCatching { gerenciador(c).setTorchMode(id, ligar); ligada.value = ligar; true }.getOrDefault(false)
    }

    fun alternar(c: Context) = definir(c, !ligada.value)

    /** Nível máximo de intensidade (1 = sem ajuste). Android 13+. */
    fun nivelMaximo(c: Context): Int = idCamera(c)?.let { gerenciador(c).getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) } ?: 1

    fun intensidade(c: Context, nivel: Int): Boolean {
        val id = idCamera(c) ?: return false
        return runCatching {
            if (nivel <= 0) gerenciador(c).setTorchMode(id, false) else gerenciador(c).turnOnTorchWithStrengthLevel(id, nivel.coerceIn(1, nivelMaximo(c)))
            ligada.value = nivel > 0
            true
        }.getOrDefault(false)
    }
}
