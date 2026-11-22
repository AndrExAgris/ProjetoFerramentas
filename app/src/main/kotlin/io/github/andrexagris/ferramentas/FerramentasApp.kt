package io.github.andrexagris.ferramentas

import android.app.Application
import io.github.andrexagris.ferramentas.catalogo.Disponibilidade
import io.github.andrexagris.ferramentas.catalogo.VerificadorDisponibilidade
import io.github.andrexagris.ferramentas.dados.PreferenciasApp

/** Guarda o que o app inteiro compartilha. Sem biblioteca de injeção: o app é pequeno o bastante. */
class FerramentasApp : Application() {
    val preferencias by lazy { PreferenciasApp(this) }

    /** Disponibilidade de cada ferramenta neste aparelho, por id. */
    val disponibilidade: Map<String, Disponibilidade> by lazy { VerificadorDisponibilidade(this).verificarTodas() }
}
