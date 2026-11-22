package io.github.andrexagris.ferramentas.dados

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "preferencias")

/**
 * Preferências do app (DataStore). As de aparência ficam com o Open Design (rememberOdPreferencias).
 * - favoritos: ids das ferramentas fixadas.
 * - soDisponiveis: filtro "só o que funciona aqui". Padrão: mostrar tudo, com as indisponíveis em cinza.
 * - ajustes de cada ferramenta: chaves "<id-da-ferramenta>.<nome>" (calibrações, limites, escolhas).
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

/** Ajustes soltos guardados no DataStore, para ferramentas e serviços (fora de composables). */
object Ajustes {
    suspend fun float(c: Context, chave: String, padrao: Float): Float = c.dataStore.data.first()[floatPreferencesKey(chave)] ?: padrao
    suspend fun texto(c: Context, chave: String, padrao: String): String = c.dataStore.data.first()[stringPreferencesKey(chave)] ?: padrao
    suspend fun bool(c: Context, chave: String, padrao: Boolean): Boolean = c.dataStore.data.first()[booleanPreferencesKey(chave)] ?: padrao
    suspend fun <T> definir(c: Context, chave: Preferences.Key<T>, valor: T) { c.dataStore.edit { it[chave] = valor } }
    suspend fun definirFloat(c: Context, chave: String, v: Float) = definir(c, floatPreferencesKey(chave), v)
    suspend fun definirTexto(c: Context, chave: String, v: String) = definir(c, stringPreferencesKey(chave), v)
    suspend fun definirBool(c: Context, chave: String, v: Boolean) = definir(c, booleanPreferencesKey(chave), v)
}

/** Estado persistente simples para telas: lê do DataStore e grava ao mudar. */
private class EstadoPersistente<T>(private val atual: State<T>, private val gravar: (T) -> Unit) : MutableState<T> {
    override var value: T
        get() = atual.value
        set(v) = gravar(v)
    override fun component1() = value
    override fun component2(): (T) -> Unit = { value = it }
}

@Composable
private fun <T> rememberAjuste(chave: Preferences.Key<T>, padrao: T): MutableState<T> {
    val c = LocalContext.current.applicationContext
    val escopo = rememberCoroutineScope()
    val fluxo = remember(chave) { c.dataStore.data.map { it[chave] ?: padrao } }
    val estado = fluxo.collectAsState(padrao)
    return remember(chave, estado) { EstadoPersistente(estado) { v -> escopo.launch { c.dataStore.edit { it[chave] = v } } } }
}

@Composable fun rememberAjusteFloat(chave: String, padrao: Float) = rememberAjuste(floatPreferencesKey(chave), padrao)
@Composable fun rememberAjusteTexto(chave: String, padrao: String) = rememberAjuste(stringPreferencesKey(chave), padrao)
@Composable fun rememberAjusteBool(chave: String, padrao: Boolean) = rememberAjuste(booleanPreferencesKey(chave), padrao)
