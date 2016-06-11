package com.kksionek.queuedroid;

import android.graphics.drawable.Drawable;

import org.json.JSONException;
import org.json.JSONObject;

public class Player {

    enum Type {
        FACEBOOK,
        CONTACTS
    }

    private final String mId;
    private final String mName;
    private final String mImage;
    private final Type mType;
    private Drawable mDrawable;

    public Player(String id, String name, String image, Type type) {
        mId = id;
        mName = name;
        mImage = image;
        mType = type;
        mDrawable = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Player)
            return mName.equals(((Player)o).mName);
        return false;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getImage() {
        return mImage;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    public boolean isFromFacebook() {
        return mType == Type.FACEBOOK;
    }

    @Override
    public String toString() {
        return mName;
    }

    public static Player createFacebookFriend(JSONObject jsonFriend) {
        try {
            String id = jsonFriend.getString("id");
            String name = jsonFriend.getString("name");
            String image = jsonFriend.getJSONObject("picture").getJSONObject("data").getString("url");
            return new Player(id, name, image, Type.FACEBOOK);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
