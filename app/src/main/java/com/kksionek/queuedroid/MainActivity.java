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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAINACTIVITY";
    private CallbackManager mCallbackManager;
    private AccessToken mAccessToken = null;
    private ArrayAdapter<Player> mAdapter;
    private static float mDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDensity = getBaseContext().getResources().getDisplayMetrics().density;

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess: Facebook logged in");
                mAccessToken = loginResult.getAccessToken();
                requestFriends(null);
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

        Button addFb = (Button) findViewById(R.id.add_fb_friend);
        addFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAccessToken == null)
                    LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("user_friends"));
                else
                    requestFriends(null);
            }
        });

        mAdapter = new AutocompleteAdapter(getApplicationContext(), android.R.layout.simple_list_item_1);

        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.auto);
        textView.setAdapter(mAdapter);

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

    private class AutocompleteAdapter extends ArrayAdapter<Player> {

        public AutocompleteAdapter(Context context, int resourceId) {
            super(context, resourceId);
            setNotifyOnChange(true);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.row_autocomplete, parent, false);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.thumbnail);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            Player player = getItem(position);
            holder.text.setText(player.getName());

            if (player.getImage() != null) {
                ThumbnailLoader loader = new ThumbnailLoader(holder.image);
                loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, player.getImage());
            } else
                holder.image.setImageDrawable(null);

            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView image;
        TextView text;
    }

    private class ThumbnailLoader extends AsyncTask<String, Void, Drawable> {

        private final ImageView mImageView;

        public ThumbnailLoader(ImageView imageView) {
            mImageView = imageView;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            try {
                InputStream is = (InputStream) new URL(params[0]).getContent();
                Drawable d = Drawable.createFromStream(is, null);
                return d;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            mImageView.setImageDrawable(drawable);
        }
    }
}
