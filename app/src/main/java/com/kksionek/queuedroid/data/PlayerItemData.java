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
        set(player);
        mInitialPosition = sInitialPositionCounter++;
    }

    private PlayerItemData(PlayerItemData itemData) {
        set(itemData.getPlayer());
        mPoints = itemData.getPoints();
        mEditable = itemData.isEditable();
        mCurrent = itemData.isCurrent();
        mInitialPosition = itemData.getInitialPosition();
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

    public void set(Player player) {
        mPlayer = player;
        mPoints = 0;
        mEditable = true;
        mCurrent = false; // TODO check it
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
