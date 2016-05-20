package com.kksionek.queuedroid;

public class ContactsPlayer extends Player {

    private final String mId;
    private final String mImage;

    public ContactsPlayer(String name, String id, String image) {
        super(name);
        mId = id;
        mImage = image;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getImage() {
        return mImage;
    }
}
