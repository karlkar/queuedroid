package com.kksionek.queuedroid;

import org.json.JSONException;
import org.json.JSONObject;

public class Player {

    private final String mName;

    public Player(String name) {
        mName = name;
    }

    public String getId() {
        return null;
    }

    public String getName() {
        return mName;
    }

    public String getImage() {
        return null;
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
            return new FacebookPlayer(name, id, image);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

}
