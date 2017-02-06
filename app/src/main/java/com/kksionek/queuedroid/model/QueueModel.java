package com.kksionek.queuedroid.model;

import android.os.Bundle;

import com.kksionek.queuedroid.data.Player;
import com.kksionek.queuedroid.view.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class QueueModel {
    private static final String SIS_PLAYERS = "PLAYERS";
    private static final String SIS_POINTS = "POINTS";
    private static final String SIS_PREV_PLAYER = "PREV_PLAYER";
    private static final String SIS_CUR_PLAYER = "CUR_PLAYER";

    private final ArrayList<Player> mPlayers;
    private final ArrayList<Integer> mPoints;
    private int mPreviousPlayerIndex = 0;
    private int mCurrentPlayerIndex = 0;

    public QueueModel(Bundle savedInstanceState) {
        if (savedInstanceState == null || !savedInstanceState.getBoolean(MainActivity.SIS_IN_GAME)) {
            mPlayers = new ArrayList<>();
            mPoints = new ArrayList<>();
        } else {
            mPlayers = savedInstanceState.getParcelableArrayList(SIS_PLAYERS);
            mPoints = savedInstanceState.getIntegerArrayList(SIS_POINTS);
            mPreviousPlayerIndex = savedInstanceState.getInt(SIS_PREV_PLAYER);
            mCurrentPlayerIndex = savedInstanceState.getInt(SIS_CUR_PLAYER);
        }
    }

    public int getPlayersCount() {
        return mPlayers.size();
    }

    public int getPreviousPlayerIndex() {
        return mPreviousPlayerIndex;
    }

    public int getCurrentPlayerIndex() {
        return mCurrentPlayerIndex;
    }

    public String getCurrentPlayer() {
        return mPlayers.get(mCurrentPlayerIndex).getName();
    }

    public void nextTurn(int points) {
        mPreviousPlayerIndex = mCurrentPlayerIndex;
        mPoints.set(mCurrentPlayerIndex, mPoints.get(mCurrentPlayerIndex) + points);
        mCurrentPlayerIndex = ++mCurrentPlayerIndex % mPlayers.size();
    }

    public List<Integer> getPoints() {
        return new ArrayList<>(mPoints);
    }

    public void newGame() {
        for (int i = 0; i < mPoints.size(); ++i)
            mPoints.set(i, 0);
    }

    public void newGame(List<Player> players) {
        mPreviousPlayerIndex = 0;
        mCurrentPlayerIndex = 0;
        mPlayers.clear();
        mPoints.clear();
        for (Player player : players) {
            mPlayers.add(player);
            mPoints.add(0);
        }
    }

    public void resetScoreboard() {
        mPlayers.clear();
        mPoints.clear();
    }

    public int getPointsOfPlayer(int index) {
        return mPoints.get(index);
    }

    public int getPointsOfPreviousPlayer() {
        return mPoints.get(mPreviousPlayerIndex);
    }

    public ArrayList<String> getFbPlayers() {
        ArrayList<String> list = new ArrayList<>();
        for (Player player : mPlayers) {
            if (player.isFromFacebook() && !player.isMyFbProfile())
                list.add(player.getId());
        }
        return list;
    }

    public void saveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SIS_PLAYERS, mPlayers);
        outState.putIntegerArrayList(SIS_POINTS, mPoints);
        outState.putInt(SIS_PREV_PLAYER, mPreviousPlayerIndex);
        outState.putInt(SIS_CUR_PLAYER, mCurrentPlayerIndex);
    }


    public Player getPlayerAt(int idx) {
        return mPlayers.get(idx);
    }
}
