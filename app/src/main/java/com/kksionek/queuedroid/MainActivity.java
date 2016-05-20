package com.kksionek.queuedroid;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAINACTIVITY";
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2233;
    private LinearLayout mButtonContainer;
    private PlayerChooserAdapter mAdapter;
    private RelativeLayout mRoot;
    private FbController mFb = null;
    private ContactsController mContactsController = null;
    private boolean mFbEnabled = true;
    private boolean mContactsEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        else
            mContactsEnabled = true;

        mRoot = (RelativeLayout) findViewById(R.id.root);

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

        Button startButton = (Button) findViewById(R.id.start_game_btn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        loadPlayersFromContacts();
        loadPlayersFromFacebook();

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mContactsEnabled = true;
                loadPlayersFromContacts();
            } else
                mContactsEnabled = false;
        }
    }

    private void loadPlayersFromContacts() {
        if (mContactsEnabled) {
            if (mContactsController == null)
                mContactsController = new ContactsController();
            mContactsController.loadContacts(this, mAdapter);
        }
    }

    private void loadPlayersFromFacebook() {
        if (mFbEnabled) {
            if (mFb == null)
                mFb = new FbController(getApplication());
            mFb.getFriendData(this, mAdapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mFbEnabled)
            mFb.onActivityResult(requestCode, resultCode, data);
    }
}
