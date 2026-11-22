package io.github.andrexagris.ferramentas

import io.github.andrexagris.ferramentas.catalogo.Catalogo
import io.github.andrexagris.ferramentas.catalogo.Grupo
import io.github.andrexagris.ferramentas.catalogo.Requisito
import io.github.andrexagris.ferramentas.catalogo.SeusAparelhos
import io.github.andrexagris.ferramentas.ui.telas.buscar
import io.github.andrexagris.ferramentas.ui.telas.normalizarBusca
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoTest {
    @Test
    fun temAs128FerramentasDoReadme() {
        assertEquals(128, Catalogo.ferramentas.size)
    }

    @Test
    fun quantidadePorGrupoIgualAoReadme() {
        val esperado = mapOf(
            Grupo.Bateria to 12, Grupo.Internet to 25, Grupo.Sinais to 7, Grupo.Nfc to 13, Grupo.Localizacao to 9,
            Grupo.Medicao to 10, Grupo.Movimento to 6, Grupo.Ambiente to 9, Grupo.Som to 10, Grupo.Camera to 9,
            Grupo.Saude to 4, Grupo.Aparelho to 11, Grupo.Utilidades to 3,
        )
        Grupo.entries.forEach { assertEquals(it.nome, esperado[it], Catalogo.doGrupo(it).size) }
    }

    @Test
    fun todaSecaoTemFerramenta() {
        Grupo.entries.forEach { g ->
            g.secoes.forEach { s -> assertTrue("${g.nome} > $s vazia", Catalogo.doGrupo(g).any { it.secao == s }) }
        }
    }

    @Test
    fun aparelhoFuturoSempreTemRequisitoQueFalta() {
        // Sem requisito, a ferramenta apareceria como disponível nos aparelhos-alvo.
        Catalogo.ferramentas.filter { it.aparelhos == SeusAparelhos.AparelhoFuturo }
            .forEach { assertTrue(it.id, it.requisitos.isNotEmpty()) }
    }

    @Test
    fun soA24PedeAndroid14() {
        Catalogo.ferramentas.filter { it.aparelhos == SeusAparelhos.SoA24 }
            .forEach { f -> assertTrue(f.id, f.requisitos.any { it is Requisito.AndroidMinimo && it.api >= 34 }) }
    }

    @Test
    fun buscaIgnoraAcentosECaixa() {
        assertEquals("bussola", normalizarBusca("  Bússola "))
        assertTrue(buscar("bussola").any { it.id == "localizacao.bussola" })
        assertTrue(buscar("WI-FI qr").any { it.id == "internet.wifi-qr" })
        assertTrue(buscar("").isEmpty())
    }

    @Test
    fun buscaPeloGrupoEPoeQuemBateNoNomePrimeiro() {
        assertTrue(buscar("bateria").map { it.grupo }.containsAll(listOf(Grupo.Bateria)))
        assertEquals(12, buscar("bateria").count { it.grupo == Grupo.Bateria })
        val r = buscar("wifi")
        assertTrue(r.first().nome.contains("Wi-Fi") || normalizarBusca(r.first().nome).contains("wifi"))
    }
}
