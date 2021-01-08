package com.kksionek.queuedroid.model

import com.kksionek.queuedroid.data.Player

sealed class GameState {

    object Ready : GameState()

    data class InGame(val currentPlayer: Int, val state: Map<Player, Int>) : GameState()

    data class Finished(val state: Map<Player, Int>) : GameState()
}
