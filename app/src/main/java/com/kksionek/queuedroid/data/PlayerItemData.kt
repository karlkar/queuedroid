package com.kksionek.queuedroid.data

data class PlayerItemData(
    var player: Player = Player(),
    var points: Int = 0,
    var isCurrent: Boolean = false
) {
    val initialPosition: Int

    init {
        reset()
        initialPosition = sInitialPositionCounter++
    }

    val image: String
        get() = player.image
    val name: String
        get() = player.name

    fun set(player: Player, points: Int) {
        this.player = player
        this.points = points
        isCurrent = false
    }

    fun reset() {
        set(Player(), 0)
    }

    companion object {
        private var sInitialPositionCounter = 0
    }
}