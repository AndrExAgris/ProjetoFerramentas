package io.github.andrexagris.ferramentas.dados

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "preferencias")

/**
 * Preferências do app (DataStore). As de aparência ficam com o Open Design (rememberOdPreferencias).
 * - favoritos: ids das ferramentas fixadas.
 * - soDisponiveis: filtro "só o que funciona aqui". Padrão: mostrar tudo, com as indisponíveis em cinza.
 */
class PreferenciasApp(private val contexto: Context) {
    private object Chaves {
        val favoritos = stringSetPreferencesKey("favoritos")
        val soDisponiveis = booleanPreferencesKey("so_disponiveis")
    }

    val favoritos: Flow<Set<String>> = contexto.dataStore.data.map { it[Chaves.favoritos] ?: emptySet() }
    val soDisponiveis: Flow<Boolean> = contexto.dataStore.data.map { it[Chaves.soDisponiveis] ?: false }

    suspend fun alternarFavorito(id: String) {
        contexto.dataStore.edit { p ->
            val atuais = p[Chaves.favoritos] ?: emptySet()
            p[Chaves.favoritos] = if (id in atuais) atuais - id else atuais + id
        }
    }

    suspend fun definirSoDisponiveis(valor: Boolean) {
        contexto.dataStore.edit { it[Chaves.soDisponiveis] = valor }
    }
}
