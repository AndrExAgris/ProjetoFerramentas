package io.github.andrexagris.ferramentas.dados

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Uma amostra de uma série temporal (histórico de bateria, pressão, sinal, medições…).
 * serie: nome da série, ex. "bateria". Até quatro valores por amostra; o significado é de cada série.
 * Tabela genérica de propósito: uma ferramenta nova com histórico não precisa de migração.
 */
@Entity(tableName = "amostras", indices = [Index("serie", "tempo")])
data class Amostra(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serie: String,
    val tempo: Long,
    val v1: Double = Double.NaN,
    val v2: Double = Double.NaN,
    val v3: Double = Double.NaN,
    val v4: Double = Double.NaN,
    val texto: String? = null,
)

/**
 * Algo que a pessoa salvou numa ferramenta: aparelho do Wake-on-LAN, host monitorado, ponto marcado,
 * resultado de teste… tipo separa as listas; dados é texto livre (normalmente "chave=valor" por linha).
 */
@Entity(tableName = "itens", indices = [Index("tipo")])
data class ItemSalvo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val nome: String,
    val dados: String,
    val criado: Long = System.currentTimeMillis(),
) {
    fun campos(): Map<String, String> = dados.lines().mapNotNull { l -> l.indexOf('=').takeIf { it > 0 }?.let { l.substring(0, it) to l.substring(it + 1) } }.toMap()

    companion object {
        fun de(tipo: String, nome: String, campos: Map<String, Any?>, id: Long = 0) =
            ItemSalvo(id, tipo, nome, campos.entries.joinToString("\n") { "${it.key}=${it.value ?: ""}" })
    }
}

@Dao
interface AmostrasDao {
    @Insert suspend fun inserir(a: Amostra)
    @Insert suspend fun inserirTodas(a: List<Amostra>)

    @Query("SELECT * FROM amostras WHERE serie = :serie AND tempo >= :desde ORDER BY tempo")
    fun observar(serie: String, desde: Long): Flow<List<Amostra>>

    @Query("SELECT * FROM amostras WHERE serie = :serie AND tempo >= :desde ORDER BY tempo")
    suspend fun listar(serie: String, desde: Long = 0): List<Amostra>

    @Query("SELECT * FROM amostras WHERE serie = :serie ORDER BY tempo DESC LIMIT 1")
    suspend fun ultima(serie: String): Amostra?

    @Query("DELETE FROM amostras WHERE serie = :serie")
    suspend fun apagarSerie(serie: String)

    @Query("DELETE FROM amostras WHERE tempo < :antes")
    suspend fun apagarAntigas(antes: Long)
}

@Dao
interface ItensDao {
    @Upsert suspend fun salvar(i: ItemSalvo): Long

    @Query("SELECT * FROM itens WHERE tipo = :tipo ORDER BY criado DESC")
    fun observar(tipo: String): Flow<List<ItemSalvo>>

    @Query("SELECT * FROM itens WHERE tipo = :tipo ORDER BY criado DESC")
    suspend fun listar(tipo: String): List<ItemSalvo>

    @Query("DELETE FROM itens WHERE id = :id")
    suspend fun apagar(id: Long)
}

@Database(entities = [Amostra::class, ItemSalvo::class], version = 1, exportSchema = true)
abstract class BancoDados : RoomDatabase() {
    abstract fun amostras(): AmostrasDao
    abstract fun itens(): ItensDao

    companion object {
        @Volatile private var instancia: BancoDados? = null
        fun de(contexto: Context): BancoDados = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(contexto.applicationContext, BancoDados::class.java, "ferramentas.db").build().also { instancia = it }
        }
    }
}

/** Nomes das séries e tipos usados hoje (para não espalhar textos soltos). */
object Series {
    const val BATERIA = "bateria"          // v1 nível %, v2 temperatura °C, v3 corrente mA, v4 voltagem V
    const val PRESSAO = "pressao"          // v1 hPa
    const val HOST = "host"                // texto = host, v1 1 se respondeu, v2 latência ms
}

object Tipos {
    const val WOL = "wol"
    const val HOST = "host"
    const val PONTO = "ponto"
    const val CARREGADOR = "carregador"
    const val STANDBY = "standby"
    const val APPS_REDE = "apps-rede"
    const val SITIO = "sitio"
}
