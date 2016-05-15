package com.kksionek.queuedroid;

public class FacebookPlayer extends Player {

    private final String mId;
    private final String mImage;

    public String getId() {
        return mId;
    }

    public String getImage() {
        return mImage;
    }

    public FacebookPlayer(String name, String id, String image) {
        super(name);
        mId = id;
        mImage = image;
    }
}
