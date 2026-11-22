package io.github.andrexagris.ferramentas.servicos

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.github.andrexagris.ferramentas.base.Lanterna

/** Bloco do painel rápido: lanterna. */
class BlocoLanterna : TileService() {
    override fun onStartListening() = atualizar()

    override fun onClick() {
        Lanterna.alternar(this)
        atualizar()
    }

    private fun atualizar() {
        val t = qsTile ?: return
        if (Lanterna.idCamera(this) == null) { t.state = Tile.STATE_UNAVAILABLE; t.updateTile(); return }
        t.state = if (Lanterna.ligada.value) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        t.subtitle = if (Lanterna.ligada.value) "Ligada" else "Desligada"
        t.updateTile()
    }
}

/** Bloco do painel rápido: velocidade da internet na barra de status. */
class BlocoVelocidade : TileService() {
    override fun onStartListening() = atualizar()

    override fun onClick() {
        if (ServicoVelocidade.ativo.value) ServicoVelocidade.parar(this) else runCatching { ServicoVelocidade.iniciar(this) }
        ServicoVelocidade.ativo.value = !ServicoVelocidade.ativo.value
        atualizar()
    }

    private fun atualizar() {
        val t = qsTile ?: return
        t.state = if (ServicoVelocidade.ativo.value) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        t.subtitle = if (ServicoVelocidade.ativo.value) "Na barra" else "Desligada"
        t.updateTile()
    }
}
