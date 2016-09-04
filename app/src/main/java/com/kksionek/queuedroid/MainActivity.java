package com.kksionek.queuedroid;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAINACTIVITY";
    public static final int REQUEST_IMAGE_CAPTURE = 9876;
    public static final int REQUEST_IMAGE_CROP = 9877;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2233;

    private QueueModel mQueueModel = null;
    private PlayerContainerView mPlayerContainerView;
    private RelativeLayout mRoot;
    private LinearLayout mGameModeChooser;
    private Button mStartButton;

    private View.OnClickListener mOnStartGameBtnClicked = new OnStartGameBtnClicked();
    private View.OnClickListener mOnNextTurnBtnClicked = new OnNextTurnBtnClicked();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRoot = (RelativeLayout) findViewById(R.id.root);
        mGameModeChooser = (LinearLayout) findViewById(R.id.game_mode_chooser);

        mPlayerContainerView = (PlayerContainerView) findViewById(R.id.button_container);
        mPlayerContainerView.onCreate(this, mRoot);

        mStartButton = (Button) findViewById(R.id.start_game_btn);
        mStartButton.setOnClickListener(mOnStartGameBtnClicked);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPlayerContainerView.onContactsPermission(requestCode == PERMISSIONS_REQUEST_READ_CONTACTS && grantResults[0] == PackageManager.PERMISSION_GRANTED);
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
        mPlayerContainerView.onActivityResult(requestCode, resultCode, data);
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
        for (int i = 0; i < mPlayerContainerView.getChildCount() - 1; ++i) {
            PlayerChooserView playerChooserView = (PlayerChooserView) mPlayerContainerView.getChildAt(i);
            if (playerChooserView.onPhotoCreated(data))
                return;
        }
    }

    private class OnStartGameBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mQueueModel = new QueueModel();
            List<Player> players = mPlayerContainerView.onGameStarted();
            mQueueModel.newGame(players);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mGameModeChooser.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mStartButton.setText("NEXT TURN");
            mStartButton.setOnClickListener(mOnNextTurnBtnClicked);
        }
    }

    private class OnNextTurnBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //TODO: Get points
            int points = 16;
            mQueueModel.nextTurn(points);
            mPlayerContainerView.nextTurn(mQueueModel.getPointsOfPreviousPlayer(), mQueueModel.getCurrentPlayerIndex());
        }
    }

}
