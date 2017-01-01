package com.kksionek.queuedroid.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.kksionek.queuedroid.data.Player;
import com.kksionek.queuedroid.model.QueueModel;
import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.model.TooFewPlayersException;
import com.kksionek.queuedroid.model.WrongPlayerException;

import java.util.List;

public class MainActivity extends FragmentActivity implements PointsDialogFragment.PointsDialogListener {

    public static final String TAG = "MAINACTIVITY";
    public static final int REQUEST_IMAGE_CAPTURE = 9876;
    public static final int REQUEST_IMAGE_CROP = 9877;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2233;

    private final QueueModel mQueueModel = new QueueModel();
    private PlayerContainerView mPlayerContainerView;
    private RelativeLayout mRoot;
//    private LinearLayout mGameModeChooser;
    private Button mStartButton;
    private Button mEndButton;
    private Button mShareButton;

    private final View.OnClickListener mOnStartGameBtnClicked = new OnStartGameBtnClicked();
    private final View.OnClickListener mOnEndGameBtnClicked = new OnEndGameBtnClicked();
    private final View.OnClickListener mOnNextTurnBtnClicked = new OnNextTurnBtnClicked();
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRoot = (RelativeLayout) findViewById(R.id.root);
//        mGameModeChooser = (LinearLayout) findViewById(R.id.game_mode_chooser);

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

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-9982327151344679~7308090141");

        mAdView = (AdView) findViewById(R.id.ad_view);
        mAdView.setVisibility(View.GONE);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.adMobTestDeviceS5))
                .addTestDevice(getString(R.string.adMobTestDeviceS7))
                .build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                AnimationUtils.beginDelayedTransition(mRoot);
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null)
            mAdView.resume();
    }

    @Override
    protected void onPause() {
        if (mAdView != null)
            mAdView.pause();
        super.onPause();
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
            try {
                List<Player> players = mPlayerContainerView.onGameStarted();
                mQueueModel.newGame(players);

//                AnimationUtils.beginDelayedTransition(mRoot);
                //mGameModeChooser.setVisibility(View.GONE);

                AnimationUtils.beginDelayedTransition(mRoot);
                mStartButton.setText(R.string.next_turn);
                mStartButton.setOnClickListener(mOnNextTurnBtnClicked);

                mEndButton.setText(R.string.end_game);
                AnimationUtils.beginDelayedTransition(mRoot);
                mEndButton.setVisibility(View.VISIBLE);
                mEndButton.setOnClickListener(mOnEndGameBtnClicked);
            } catch (TooFewPlayersException ex) {
                Log.d(TAG, "onClick: Game cannot be started - too few players entered.");
                Toast.makeText(MainActivity.this, R.string.activity_main_start_too_few_players_error_message, Toast.LENGTH_LONG).show();
            } catch (WrongPlayerException ex) {
                Log.d(TAG, "onClick: Game cannot be started - some players are inproper.");
                Toast.makeText(MainActivity.this, R.string.activity_main_start_wrong_player_on_list_error_message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class OnEndGameBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mPlayerContainerView.onGameEnded(mQueueModel);

            AnimationUtils.beginDelayedTransition(mRoot);
            mStartButton.setText(R.string.new_game);
            mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restartGame(true);
                }
            });

            AnimationUtils.beginDelayedTransition(mRoot);
            mEndButton.setText(R.string.play_again);
            mEndButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restartGame(false);
                }
            });

            mShareButton.setText(R.string.share);
            AnimationUtils.beginDelayedTransition(mRoot);
            mShareButton.setVisibility(View.VISIBLE);
        }
    }

    private void restartGame(boolean hardReset) {
        mPlayerContainerView.onGameRestarted(hardReset);

//        AnimationUtils.beginDelayedTransition(mRoot);
        //mGameModeChooser.setVisibility(View.VISIBLE);

        AnimationUtils.beginDelayedTransition(mRoot);
        mStartButton.setText(R.string.play);
        mStartButton.setOnClickListener(mOnStartGameBtnClicked);

        AnimationUtils.beginDelayedTransition(mRoot);
        mEndButton.setVisibility(View.GONE);

        AnimationUtils.beginDelayedTransition(mRoot);
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
