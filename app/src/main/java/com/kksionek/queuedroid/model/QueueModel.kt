package com.kksionek.queuedroid.model

import android.os.Bundle
import com.kksionek.queuedroid.data.Player
import com.kksionek.queuedroid.view.MainFragment.Companion.SIS_IN_GAME
import java.util.*

class QueueModel(savedInstanceState: Bundle?) {

    private var mPlayers: MutableList<Player>? = null
    private var mPoints: MutableList<Int>? = null

    var previousPlayerIndex = 0
        private set
    var currentPlayerIndex = 0
        private set
    val playersCount: Int
        get() = mPlayers!!.size
    val currentPlayer: String
        get() = mPlayers!![currentPlayerIndex].name

    fun nextTurn(points: Int) {
        previousPlayerIndex = currentPlayerIndex
        mPoints!![currentPlayerIndex] = mPoints!![currentPlayerIndex] + points
        currentPlayerIndex = ++currentPlayerIndex % mPlayers!!.size
    }

    val points: List<Int>
        get() = mPoints!!

    fun newGame() {
        for (i in mPoints!!.indices) mPoints!![i] = 0
    }

    fun newGame(players: List<Player>) {
        previousPlayerIndex = 0
        currentPlayerIndex = 0
        mPlayers!!.clear()
        mPoints!!.clear()
        for (player in players) {
            mPlayers!!.add(player)
            mPoints!!.add(0)
        }
    }

    fun resetScoreboard() {
        mPlayers!!.clear()
        mPoints!!.clear()
    }

    fun getPointsOfPlayer(index: Int): Int {
        return mPoints!![index]
    }

    val pointsOfPreviousPlayer: Int
        get() = mPoints!![previousPlayerIndex]

    val fbPlayers: List<String>
        get() {
            return mPlayers?.filter { it.isFromFacebook && !it.isMyFbProfile }
                ?.map { it.name }
                .orEmpty()
        }

    fun saveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(SIS_PLAYERS, arrayListOf(*mPlayers!!.toTypedArray()))
        outState.putIntegerArrayList(SIS_POINTS, arrayListOf(*mPoints!!.toTypedArray()))
        outState.putInt(SIS_PREV_PLAYER, previousPlayerIndex)
        outState.putInt(SIS_CUR_PLAYER, currentPlayerIndex)
    }

    fun getPlayerAt(idx: Int): Player {
        return mPlayers!![idx]
    }

    companion object {
        private const val SIS_PLAYERS = "PLAYERS"
        private const val SIS_POINTS = "POINTS"
        private const val SIS_PREV_PLAYER = "PREV_PLAYER"
        private const val SIS_CUR_PLAYER = "CUR_PLAYER"
    }

    init {
        if (savedInstanceState == null || !savedInstanceState.getBoolean(SIS_IN_GAME)) {
            mPlayers = ArrayList()
            mPoints = ArrayList()
        } else {
            mPlayers = savedInstanceState.getParcelableArrayList(SIS_PLAYERS)
            mPoints = savedInstanceState.getIntegerArrayList(SIS_POINTS)
            previousPlayerIndex = savedInstanceState.getInt(SIS_PREV_PLAYER)
            currentPlayerIndex = savedInstanceState.getInt(SIS_CUR_PLAYER)
        }
    }
}