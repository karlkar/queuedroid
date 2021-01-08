package com.kksionek.queuedroid.model

import com.kksionek.queuedroid.data.Player

class QueueModel {

    private val players = mutableListOf<Player>()
    private val points = mutableListOf<Int>()

    var currentPlayerIndex = 0
        private set
    val playersCount: Int
        get() = players.size
    val currentPlayer: String
        get() = players[currentPlayerIndex].name

    fun dump(): Map<Player, Int> {
        return players.zip(points) { player, points ->
            player to points
        }.toMap()
    }

    fun nextTurn(aPoints: Int) {
        points[currentPlayerIndex] = points[currentPlayerIndex] + aPoints
        currentPlayerIndex = ++currentPlayerIndex % players.size
    }

    val pointList: List<Int>
        get() = points

    fun newGame() {
        for (i in points.indices) {
            points[i] = 0
        }
    }

    fun newGame(playerList: List<Player>) {
        currentPlayerIndex = 0
        players.clear()
        points.clear()
        for (player in playerList) {
            players.add(player)
            points.add(0)
        }
    }

    fun resetScoreboard() {
        players.clear()
        points.clear()
    }

    fun getPointsOfPlayer(index: Int): Int =
        points[index]

    val fbPlayers: List<String>
        get() = players.filter { it.isFromFacebook && !it.isMyFbProfile }
            .map { it.name }


    fun getPlayerAt(idx: Int): Player =
        players[idx]

//    fun saveInstanceState(outState: Bundle) {
//        outState.putParcelableArrayList(SIS_PLAYERS, arrayListOf(*players!!.toTypedArray()))
//        outState.putIntegerArrayList(SIS_POINTS, arrayListOf(*points!!.toTypedArray()))
//        outState.putInt(SIS_PREV_PLAYER, previousPlayerIndex)
//        outState.putInt(SIS_CUR_PLAYER, currentPlayerIndex)
//    }
//
//    companion object {
//        private const val SIS_PLAYERS = "PLAYERS"
//        private const val SIS_POINTS = "POINTS"
//        private const val SIS_PREV_PLAYER = "PREV_PLAYER"
//        private const val SIS_CUR_PLAYER = "CUR_PLAYER"
//    }
//
//    init {
//        if (savedInstanceState == null || !savedInstanceState.getBoolean(SIS_IN_GAME)) {
//            players = mutableListOf()
//            points = mutableListOf()
//        } else {
//            players = savedInstanceState.getParcelableArrayList(SIS_PLAYERS)
//            points = savedInstanceState.getIntegerArrayList(SIS_POINTS)
//            previousPlayerIndex = savedInstanceState.getInt(SIS_PREV_PLAYER)
//            currentPlayerIndex = savedInstanceState.getInt(SIS_CUR_PLAYER)
//        }
//    }
}