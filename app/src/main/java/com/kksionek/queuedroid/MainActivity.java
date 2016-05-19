package com.kksionek.queuedroid;

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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAINACTIVITY";
    private CallbackManager mCallbackManager;
    private AccessToken mAccessToken = null;
    private LinearLayout mButtonContainer;
    private PlayerChooserAdapter mAdapter;
    private RelativeLayout mRoot;
    private Player mMyProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRoot = (RelativeLayout) findViewById(R.id.root);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess: Facebook logged in");
                mAccessToken = loginResult.getAccessToken();
                GraphRequest req = GraphRequest.newMeRequest(mAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(TAG, "onCompleted: " + object.toString());
                        mMyProfile = Player.createFacebookFriend(object);
                        mAdapter.add(mMyProfile);
                        requestFriends(null);
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
                Log.d(TAG, "onError: Facebook login error");
            }
        });

        mAdapter = new PlayerChooserAdapter(this);

        mButtonContainer = (LinearLayout) findViewById(R.id.button_container);

        PlayerChooserView playerChooser = (PlayerChooserView) findViewById(R.id.chooser1);
        playerChooser.setAdapter(mAdapter);

        playerChooser = (PlayerChooserView) findViewById(R.id.chooser2);
        playerChooser.setAdapter(mAdapter);

        Button addPlayer = (Button) findViewById(R.id.add_player_btn);
        addPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerChooserView view = new PlayerChooserView(MainActivity.this);
                view.setAdapter(mAdapter);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, (int) getResources().getDimension(R.dimen.main_activity_margin_buttons), 0, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    TransitionManager.beginDelayedTransition(mRoot);
                mButtonContainer.addView(view, params);
            }
        });

        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("user_friends"));

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void requestFriends(String nextToken) {
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
                            mAdapter.add(Player.createFacebookFriend(friendArray.getJSONObject(i)));
                        }
                        if (!response.getJSONObject().isNull("paging")) {
                            String token = response.getJSONObject().getJSONObject("paging").getJSONObject("cursors").getString("after");
                            requestFriends(token);
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
}
