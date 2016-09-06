package com.kksionek.queuedroid;

import java.util.ArrayList;
import java.util.List;

public class QueueModel {
    private final List<Player> mPlayers = new ArrayList<>();
    private final List<Integer> mPoints = new ArrayList<>();
    private int mPreviousPlayerIndex = 0;
    private int mCurrentPlayerIndex = 0;

    public int getPlayersCount() {
        return mPlayers.size();
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
            if (player.isFromFacebook())
                list.add(player.getId());
        }
        return list;
    }
}
