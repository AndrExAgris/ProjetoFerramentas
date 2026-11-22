package io.github.andrexagris.ferramentas.base

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState

/**
 * Teclas físicas (volume, por exemplo) para as ferramentas. A MainActivity passa cada KeyEvent por aqui;
 * se a ferramenta na tela tratar (devolver true), o sistema não muda o volume.
 */
object Teclas {
    private val tratadores = ArrayDeque<(KeyEvent) -> Boolean>()

    fun despachar(e: KeyEvent): Boolean = tratadores.lastOrNull()?.invoke(e) == true

    internal fun empilhar(t: (KeyEvent) -> Boolean) = tratadores.addLast(t)
    internal fun remover(t: (KeyEvent) -> Boolean) = tratadores.remove(t)
}

/** Recebe as teclas físicas enquanto o composable estiver na tela. */
@Composable
fun CapturarTeclas(aoTecla: (KeyEvent) -> Boolean) {
    val atual = rememberUpdatedState(aoTecla)
    DisposableEffect(Unit) {
        val t: (KeyEvent) -> Boolean = { atual.value(it) }
        Teclas.empilhar(t)
        onDispose { Teclas.remover(t) }
    }
}
