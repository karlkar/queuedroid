package com.kksionek.queuedroid.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.transition.TransitionManager;
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
import com.kksionek.queuedroid.model.FbController;
import com.kksionek.queuedroid.model.PlayerChooserAdapter;
import com.kksionek.queuedroid.model.QueueModel;
import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.model.Settings;
import com.kksionek.queuedroid.model.TooFewPlayersException;
import com.kksionek.queuedroid.model.WrongPlayerException;
import com.kksionek.queuedroid.view.keyboard.KeyboardView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static android.content.Intent.ACTION_SEND;

public class MainActivity extends FragmentActivity implements PointsDialogFragment.PointsDialogListener,
        PlayerChooserView.PlayerChooserViewActionListener {

    public static final String TAG = "MAINACTIVITY";
    public static final int REQUEST_IMAGE_CAPTURE = 9876;
    public static final int REQUEST_IMAGE_CROP = 9877;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2233;

    private final QueueModel mQueueModel = new QueueModel();
    private PlayerContainerView mPlayerContainerView;
    private Button mFirstButton;
    private Button mSecondButton;
    private Button mThirdButton;

    private final View.OnClickListener mOnStartGameBtnClicked = new OnStartGameBtnClicked();
    private final View.OnClickListener mOnSettingsBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
    };
    private final View.OnClickListener mOnEndGameBtnClicked = new OnEndGameBtnClicked();
    private final View.OnClickListener mOnNextTurnBtnClicked = new OnNextTurnBtnClicked();
    private AdView mAdView;
    private KeyboardView mKeyboardView;
    private PlayerChooserAdapter mPlayerChooserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RelativeLayout rootView = (RelativeLayout) findViewById(R.id.root);

        mPlayerContainerView = (PlayerContainerView) findViewById(R.id.button_container);
        mPlayerChooserAdapter = new PlayerChooserAdapter(this);
        mPlayerContainerView.setAdapter(mPlayerChooserAdapter);
        mPlayerContainerView.setPlayerChooserViewActionListener(this);

        mFirstButton = (Button) findViewById(R.id.first_btn);
        mFirstButton.setText(R.string.activity_main_button_play);
        mFirstButton.setOnClickListener(mOnStartGameBtnClicked);

        mSecondButton = (Button) findViewById(R.id.second_btn);
        mSecondButton.setText(R.string.activity_main_button_settings);
        mSecondButton.setOnClickListener(mOnSettingsBtnClicked);

        mThirdButton = (Button) findViewById(R.id.third_btn);
        mThirdButton.setText(R.string.activity_main_button_share);
        mThirdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Settings.isFacebookEnabled(getBaseContext())
                        && FbController.isInitilized()
                        && FbController.isLogged()) {
                    FbController.shareOnFacebook(
                            MainActivity.this,
                            mQueueModel.getFbPlayers(),
                            mPlayerContainerView.getRankBitmap());
                } else {
                    try {
                        File file = new File(getCacheDir(), "SHARE.png");
                        FileOutputStream fOut = new FileOutputStream(file);
                        mPlayerContainerView.getRankBitmap().compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                        file.setReadable(true, false);
                        final Intent intent = new Intent(ACTION_SEND);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        intent.setType("image/png");
                        startActivity(Intent.createChooser(intent, ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mKeyboardView = (KeyboardView) findViewById(R.id.keyboard_view);

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
                TransitionManager.beginDelayedTransition(rootView);
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mKeyboardView != null)
            mKeyboardView.setColumnCount(Settings.getKeyboardColumnsCount(this));

        if (FbController.isInitilized()) {
            if (FbController.isLogged()) {
                if (mPlayerChooserAdapter != null)
                    mPlayerChooserAdapter.reloadDataset(
                            Settings.isContactsEnabled(this),
                            Settings.isFacebookEnabled(this));
            } else {
                FbController.getInstance().logIn(this, new FbController.FacebookLoginListener() {
                    @Override
                    public void onLogged() {
                        if (mPlayerChooserAdapter != null)
                            mPlayerChooserAdapter.reloadDataset(
                                    Settings.isContactsEnabled(MainActivity.this),
                                    Settings.isFacebookEnabled(MainActivity.this));
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError() {
                    }
                });
            }
        }
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
        FbController.getInstance().onActivityResult(requestCode, resultCode, data);
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
            PlayerChooserView playerChooserView = (PlayerChooserView) mPlayerContainerView
                    .getChildAt(i);
            if (playerChooserView.onPhotoCreated(data))
                return;
        }
    }

    @Override
    public void onDialogPositiveClick(int points) {
        assignPointsAndNextTurn(points);
    }

    @Override
    public void onPictureRequested(Intent takePictureIntent) {
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onPointsRequested() {
        PointsDialogFragment dialog = new PointsDialogFragment();
        dialog.show(getSupportFragmentManager(), "PointsDialogFragment");
    }

    private class OnStartGameBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                List<Player> players = mPlayerContainerView.onGameStarted();
                mQueueModel.newGame(players);

                mKeyboardView.setVisibility(Settings.shouldUseInAppKeyboard(MainActivity.this) ?
                        View.VISIBLE : View.GONE);
                mKeyboardView.setKeepScreenOn(Settings.isKeepOnScreen(MainActivity.this));

                mFirstButton.setText(R.string.activity_main_button_next_turn);
                mFirstButton.setOnClickListener(mOnNextTurnBtnClicked);

                mSecondButton.setText(R.string.activity_main_button_end_game);
                mSecondButton.setOnClickListener(mOnEndGameBtnClicked);
            } catch (TooFewPlayersException ex) {
                Log.d(TAG, "onClick: Game cannot be started - too few players entered.");
                Toast.makeText(MainActivity.this,
                        R.string.activity_main_start_too_few_players_error_message,
                        Toast.LENGTH_LONG)
                        .show();
            } catch (WrongPlayerException ex) {
                Log.d(TAG, "onClick: Game cannot be started - some players are inproper.");
                Toast.makeText(MainActivity.this,
                        R.string.activity_main_start_wrong_player_on_list_error_message,
                        Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private class OnEndGameBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mPlayerContainerView.onGameEnded(mQueueModel);

            mKeyboardView.setVisibility(View.GONE);

            mFirstButton.setText(R.string.activity_main_button_new_game);
            mFirstButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restartGame(true);
                }
            });

            mSecondButton.setText(R.string.activity_main_button_play_again);
            mSecondButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restartGame(false);
                }
            });

            mThirdButton.setText(R.string.activity_main_button_share);
            mThirdButton.setVisibility(View.VISIBLE);
        }
    }

    private void restartGame(boolean hardReset) {
        mPlayerContainerView.onGameRestarted(hardReset);

        mFirstButton.setText(R.string.activity_main_button_play);
        mFirstButton.setOnClickListener(mOnStartGameBtnClicked);

        mSecondButton.setText(R.string.activity_main_button_settings);
        mSecondButton.setOnClickListener(mOnSettingsBtnClicked);

        mThirdButton.setVisibility(View.GONE);
    }

    private class OnNextTurnBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (Settings.shouldUseInAppKeyboard(MainActivity.this)) {
                int pointsCollected = mKeyboardView.getPoints();
                if (pointsCollected == 0
                        && Settings.isShowNoPointsConfirmationDialog(MainActivity.this)) {
                    CheckboxAlertDialog dialog = new CheckboxAlertDialog();
                    dialog.show(MainActivity.this,
                            R.string.checkbox_alert_dialog_title,
                            R.string.checkbox_alert_dialog_message,
                            new CheckboxAlertDialog.OnDialogClosedListener() {
                                @Override
                                public void onDialogClosed(boolean result) {
                                    if (result)
                                        assignPointsAndNextTurn(0);
                                }
                            });
                    return;
                }
                assignPointsAndNextTurn(pointsCollected);
            } else {
                onPointsRequested();
            }
        }
    }

    private void assignPointsAndNextTurn(int points) {
        mQueueModel.nextTurn(points);
        mKeyboardView.clearPoints();
        mPlayerContainerView.nextTurn(
                mQueueModel.getPointsOfPreviousPlayer(),
                mQueueModel.getCurrentPlayerIndex());
    }
}
