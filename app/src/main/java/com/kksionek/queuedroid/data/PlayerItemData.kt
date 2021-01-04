package com.kksionek.queuedroid.data;

public class PlayerItemData implements Cloneable {

    private static int sInitialPositionCounter = 0;

    private Player mPlayer;
    private int mPoints;
    private boolean mEditable;
    private boolean mCurrent;
    private final int mInitialPosition;

    public PlayerItemData() {
        reset();
        mInitialPosition = sInitialPositionCounter++;
    }

    public PlayerItemData(Player player) {
        this(player, 0);
    }

    public PlayerItemData(Player player, int pointsOfPlayer) {
        set(player, pointsOfPlayer);
        mInitialPosition = sInitialPositionCounter++;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getImage() {
        return mPlayer.getImage();
    }

    public String getName() {
        return mPlayer.getName();
    }

    public void set(Player player, int points) {
        mPlayer = player;
        mPoints = points;
        mEditable = true;
        mCurrent = false;
    }

    public void reset() {
        set(new Player(), 0);
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlayerItemData))
            return false;
        PlayerItemData other = (PlayerItemData) obj;
        return mPlayer.equals(other.getPlayer())
                && mPoints == other.mPoints
                && mEditable == other.mEditable
                && mCurrent == other.mCurrent;
    }
}
