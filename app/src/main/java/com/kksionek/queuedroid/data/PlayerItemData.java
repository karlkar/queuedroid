package com.kksionek.queuedroid.data;

public class PlayerItemData {

    private static int sInitialPositionCounter = 0;

    private Player mPlayer;
    private int mPoints;
    private boolean mEditable;
    private boolean mCurrent;
    private int mInitialPosition;

    public PlayerItemData() {
        reset();
    }

    public PlayerItemData(Player player) {
        set(player);
    }

    public String getImage() {
        return mPlayer.getImage();
    }

    public String getName() {
        return mPlayer.getName();
    }

    public void set(Player player) {
        mPlayer = player;
        mPoints = 0;
        mEditable = !player.getName().isEmpty();
        mCurrent = false;
        mInitialPosition = sInitialPositionCounter++;
    }

    public void reset() {
        set(new Player());
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public boolean isEditable() {
        return mEditable;
    }

    public void setEditable(boolean editable) {
        mEditable = editable;
    }

    public void setPoints(int points) {
        mPoints = points;
    }

    public int getPoints() {
        return mPoints;
    }

    public void setCurrent(boolean current) {
        mCurrent = current;
    }

    public boolean isCurrent() {
        return mCurrent;
    }

    public int getInitialPosition() {
        return mInitialPosition;
    }
}
