package com.kksionek.queuedroid;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class FbController {

    private static final String TAG = "FbController";

    private AccessToken mAccessToken = null;
    private final LoginManager mLoginManager;
    private final CallbackManager mCallbackManager;
    private Player mMyProfile;

    public FbController(Application app) {
        FacebookSdk.sdkInitialize(app.getApplicationContext());
        AppEventsLogger.activateApp(app);
        mLoginManager = LoginManager.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
    }

    public void getFriendData(@NonNull Activity activity, @NonNull final PlayerChooserAdapter adapter) {
        if (mAccessToken == null) {
            mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "onSuccess: Facebook logged in");
                    mAccessToken = loginResult.getAccessToken();
                    GraphRequest req = GraphRequest.newMeRequest(mAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Log.d(TAG, "onCompleted: " + object.toString());
                            mMyProfile = Player.createFacebookFriend(object);
                            adapter.add(mMyProfile);
                            requestFriends(adapter, null);
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,picture");
                    req.setParameters(parameters);
                    req.executeAsync();
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "onCancel: Facebook login cancelled");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e(TAG, "onError: Facebook login error. Should try again...");
                }
            });

            mLoginManager.logInWithReadPermissions(activity, Arrays.asList("user_friends"));
        } else {
            requestFriends(adapter, null);
        }
    }

    private void requestFriends(@NonNull final PlayerChooserAdapter adapter, @Nullable String nextToken) {
        GraphRequest req = new GraphRequest(mAccessToken, "/me/taggable_friends", null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                Log.d(TAG, "onCompleted: response = " + response.toString());
                if (response.getError() != null)
                    Log.e(TAG, "onCompleted: Couldn't obtain friend data.");
                else {
                    try {
                        JSONArray friendArray = response.getJSONObject().getJSONArray("data");
                        for (int i = 0; i < friendArray.length(); ++i) {
                            Log.d(TAG, "onCompleted: FRIEND = " + friendArray.get(i).toString());
                            adapter.add(Player.createFacebookFriend(friendArray.getJSONObject(i)));
                        }
                        if (!response.getJSONObject().isNull("paging")) {
                            String token = response.getJSONObject().getJSONObject("paging").getJSONObject("cursors").getString("after");
                            requestFriends(adapter, token);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        if (nextToken != null) {
            Bundle parameters = new Bundle();
            parameters.putString("after", nextToken);
            req.setParameters(parameters);
        }
        req.executeAsync();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void shareOnFacebook(@NonNull Activity activity, @NonNull ArrayList<String> list) {

        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                .putString("og:type", "games.game")
                .putString("og:title", "What a game!")
                .build();

        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("queuedroid-test:play")
                .putObject("game", object)
                .putStringArrayList("tags", list)
                .build();

        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("game")
                .setAction(action)
                .build();
        ShareDialog dlg = new ShareDialog(activity);
        dlg.registerCallback(mCallbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.d(TAG, "onSuccess: SUCCESS");
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: CANCEL");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError: " + error.toString());
            }
        });
        dlg.show(content);
    }
}
