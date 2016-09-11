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
import android.support.v4.app.FragmentActivity;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

public class MainActivity extends FragmentActivity implements PointsDialogFragment.PointsDialogListener {

    public static final String TAG = "MAINACTIVITY";
    public static final int REQUEST_IMAGE_CAPTURE = 9876;
    public static final int REQUEST_IMAGE_CROP = 9877;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2233;

    private QueueModel mQueueModel = new QueueModel();
    private PlayerContainerView mPlayerContainerView;
    private RelativeLayout mRoot;
    private LinearLayout mGameModeChooser;
    private Button mStartButton;
    private Button mEndButton;
    private Button mShareButton;

    private final View.OnClickListener mOnStartGameBtnClicked = new OnStartGameBtnClicked();
    private final View.OnClickListener mOnEndGameBtnClicked = new OnEndGameBtnClicked();
    private final View.OnClickListener mOnNextTurnBtnClicked = new OnNextTurnBtnClicked();

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

        mEndButton = (Button) findViewById(R.id.end_game_btn);
        mEndButton.setOnClickListener(mOnEndGameBtnClicked);

        mShareButton = (Button) findViewById(R.id.share_game_btn);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayerContainerView.shareOnFacebook(mQueueModel.getFbPlayers());
            }
        });

//        AdView adView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);
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

    @Override
    public void onDialogPositiveClick(int points) {
        mQueueModel.nextTurn(points);
        mPlayerContainerView.nextTurn(mQueueModel.getPointsOfPreviousPlayer(), mQueueModel.getCurrentPlayerIndex());
    }

    private class OnStartGameBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            List<Player> players = mPlayerContainerView.onGameStarted();
            mQueueModel.newGame(players);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mGameModeChooser.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mStartButton.setText("NEXT TURN");
            mStartButton.setOnClickListener(mOnNextTurnBtnClicked);

            mEndButton.setText("END GAME");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mEndButton.setVisibility(View.VISIBLE);
            mEndButton.setOnClickListener(mOnEndGameBtnClicked);
        }
    }

    private class OnEndGameBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mPlayerContainerView.onGameEnded(mQueueModel);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mStartButton.setText("NEW GAME");
            mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restartGame(true);
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mEndButton.setText("ONE MORE");
            mEndButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restartGame(false);
                }
            });

            mShareButton.setText("SHARE");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                TransitionManager.beginDelayedTransition(mRoot);
            mShareButton.setVisibility(View.VISIBLE);
        }
    }

    private void restartGame(boolean hardReset) {
        mPlayerContainerView.onGameRestarted(hardReset);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(mRoot);
        mGameModeChooser.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(mRoot);
        mStartButton.setText("START");
        mStartButton.setOnClickListener(mOnStartGameBtnClicked);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(mRoot);
        mEndButton.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(mRoot);
        mShareButton.setVisibility(View.GONE);
    }

    private class OnNextTurnBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            PointsDialogFragment dialog = new PointsDialogFragment();
            dialog.show(getSupportFragmentManager(), "PointsDialogFragment");
        }
    }
}
