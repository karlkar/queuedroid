package com.kksionek.queuedroid;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
    public static final int REQUEST_IMAGE_CAPTURE = 9876;
    public static final int REQUEST_IMAGE_CROP = 9877;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2233;
    private QueueModel mQueueModel = null;
    private LinearLayout mButtonContainer;
    private PlayerChooserAdapter mAdapter;
    private RelativeLayout mRoot;
    private FbController mFb = null;
    private ContactsController mContactsController = null;
    private boolean mFbEnabled = true;
    private boolean mContactsEnabled = false;
    private LinearLayout mGameModeChooser;
    private Button mStartButton;
    private Button mAddPlayer;

    private View.OnClickListener mOnStartGameBtnClicked = new OnStartGameBtnClicked();
    private View.OnClickListener mOnNextTurnBtnClicked = new OnNextTurnBtnClicked();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        else
            mContactsEnabled = true;

        mRoot = (RelativeLayout) findViewById(R.id.root);
        mGameModeChooser = (LinearLayout) findViewById(R.id.game_mode_chooser);

        mAdapter = new PlayerChooserAdapter(this);

        mButtonContainer = (LinearLayout) findViewById(R.id.button_container);

        PlayerChooserView playerChooser = (PlayerChooserView) findViewById(R.id.chooser1);
        playerChooser.setAdapter(mAdapter);
        playerChooser.setActivity(this);

        playerChooser = (PlayerChooserView) findViewById(R.id.chooser2);
        playerChooser.setAdapter(mAdapter);
        playerChooser.setActivity(this);

        mAddPlayer = (Button) findViewById(R.id.add_player_btn);
        mAddPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerChooserView view = new PlayerChooserView(MainActivity.this);
                view.setAdapter(mAdapter);
                view.setActivity(MainActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, (int) getResources().getDimension(R.dimen.main_activity_margin_buttons), 0, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    TransitionManager.beginDelayedTransition(mRoot);
                mButtonContainer.addView(view, mButtonContainer.getChildCount() - 1, params);
            }
        });

        mStartButton = (Button) findViewById(R.id.start_game_btn);
        mStartButton.setOnClickListener(mOnStartGameBtnClicked);

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
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode != RESULT_OK || !performCrop(data.getData()))
                setImageData(resultCode == RESULT_OK ? data : null);
            return;
        } else if (requestCode == REQUEST_IMAGE_CROP) {
            setImageData(resultCode == RESULT_OK ? data : null);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (mFbEnabled)
            mFb.onActivityResult(requestCode, resultCode, data);
    }

    private boolean performCrop(Uri data) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(data, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
        }
        catch (ActivityNotFoundException ex) {
            return false;
        }
        return true;
    }

    private void setImageData(Intent data) {
        for (int i = 0; i < mButtonContainer.getChildCount() - 1; ++i) {
            PlayerChooserView playerChooserView = (PlayerChooserView) mButtonContainer.getChildAt(i);
            if (playerChooserView.onPhotoCreated(data))
                return;
        }
    }

    private class OnStartGameBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mQueueModel = new QueueModel();
            PlayerChooserView tmp;
            for (int i = 0; i < mButtonContainer.getChildCount() - 1; ++i) {
                tmp = (PlayerChooserView) mButtonContainer.getChildAt(i);
                if (i == 0)
                    tmp.setCurrentTurn(true);
                mQueueModel.addPlayer(tmp.getPlayer());
                tmp.setEditable(false);
            }
            mQueueModel.newGame();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mGameModeChooser.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mStartButton.setText("NEXT TURN");
            mStartButton.setOnClickListener(mOnNextTurnBtnClicked);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mAddPlayer.setVisibility(View.GONE);
        }
    }

    private class OnNextTurnBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //TODO: Get points
            int points = 16;
            mQueueModel.nextTurn(points);
            PlayerChooserView tmp;
            for (int i = 0; i < mButtonContainer.getChildCount() - 1; ++i) {
                tmp = (PlayerChooserView) mButtonContainer.getChildAt(i);
                if (i == mQueueModel.getCurrentPlayerIndex())
                    tmp.setCurrentTurn(true);
                else {
                    tmp.setCurrentTurn(false);
                    tmp.setPoints(mQueueModel.getPointsOfPlayer(i));
                }
            }
            ((PlayerChooserView)mButtonContainer.getChildAt(mQueueModel.getCurrentPlayerIndex())).setCurrentTurn(true);
        }
    }

}
