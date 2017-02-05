package com.kksionek.queuedroid.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class Player {

    private String mId;
    private String mName;
    private String mImage;
    private Type mType;

    public Player() {
        reset();
    }

    public Player(@NonNull String id, @NonNull String name, @Nullable String image, @NonNull Type type) {
        mId = id;
        mName = name;
        mImage = image;
        mType = type;
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

    public void set(Player player) {
        mId = player.mId;
        mName = player.mName;
        mImage = player.mImage;
        mType = player.mType;
    }

    private void reset() {
        mId = "";
        mName = "";
        mImage = "";
        mType = Type.CUSTOM;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Player
                && mName.equals(((Player) o).mName)
                && mImage.equals(((Player) o).mImage);
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        mImage = image;
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

    public enum Type {
        MY_FB_PROFILE,
        FACEBOOK,
        CONTACTS,
        CUSTOM
    }
}
