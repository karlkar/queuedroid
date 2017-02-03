package com.kksionek.queuedroid.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class Player {

    public enum Type {
        MY_FB_PROFILE,
        FACEBOOK,
        CONTACTS,
        CUSTOM
    }

    private String mId;
    private String mName;
    private String mImage;
    private Type mType;

    public Player() {
        reset();
    }

    public Player(String name) {
        mId = "";
        mName = name;
        mImage = null;
        mType = Type.CUSTOM;
    }

    public Player(@NonNull String id, @NonNull String name, @Nullable String image, @NonNull Type type) {
        mId = id;
        mName = name;
        mImage = image;
        mType = type;
    }

    public void set(Player player) {
        mId = player.mId;
        mName = player.mName;
        mImage = player.mImage;
        mType = player.mType;
    }

    public void reset() {
        mId = "";
        mName = "";
        mImage = "";
        mType = Type.CUSTOM;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Player && mName.equals(((Player) o).mName);
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

    public boolean isFromFacebook() {
        return mType == Type.FACEBOOK || mType == Type.MY_FB_PROFILE;
    }

    public boolean isMyFbProfile() {
        return mType == Type.MY_FB_PROFILE;
    }

    @Override
    public String toString() {
        return mName;
    }

    public static Player createFacebookFriend(@NonNull JSONObject jsonFriend) {
        return createFacebookFriend(jsonFriend, false);
    }

    public static Player createFacebookFriend(@NonNull JSONObject jsonFriend, boolean myProfile) {
        try {
            String id = jsonFriend.getString("id");
            String name = jsonFriend.getString("name");
            String image = jsonFriend.getJSONObject("picture").getJSONObject("data").getString("url");
            return new Player(id, name, image, myProfile ? Type.MY_FB_PROFILE : Type.FACEBOOK);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
