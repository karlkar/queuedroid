package com.kksionek.queuedroid.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.kksionek.queuedroid.data.Player;
import com.kksionek.queuedroid.model.ContactsController;
import com.kksionek.queuedroid.model.FbController;
import com.kksionek.queuedroid.model.PlayerChooserViewAdapter;
import com.kksionek.queuedroid.model.QueueModel;
import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.model.Settings;
import com.kksionek.queuedroid.view.keyboard.KeyboardView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Intent.ACTION_SEND;

public class MainActivity extends AppCompatActivity implements PointsDialogFragment.PointsDialogListener {

    private static final String TAG = "MainActivity";
    public static final int REQUEST_IMAGE_CAPTURE = 9876;
    public static final int REQUEST_IMAGE_CROP = 9877;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2233;

    private final QueueModel mQueueModel = new QueueModel();
    private Button mFirstBtn;
    private Button mSecondBtn;
    private Button mThirdBtn;

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
    private AtomicInteger mBackCounter = new AtomicInteger(0);
    private RecyclerView mRecyclerView;
    private List<Player> mAllPlayers;
    private PlayerChooserViewAdapter mPlayerChooserViewAdapter;
    private Button mAddPlayerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ViewGroup rootView = (ViewGroup) findViewById(R.id.root);

        mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getBaseContext(),
                LinearLayoutManager.VERTICAL,
                false));
        mPlayerChooserViewAdapter = new PlayerChooserViewAdapter(mQueueModel);
        mRecyclerView.setAdapter(mPlayerChooserViewAdapter);

        mAddPlayerBtn = (Button) findViewById(R.id.activity_main_button_add_player);
        mAddPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerChooserViewAdapter.add(new Player("", "", "", Player.Type.CUSTOM));
            }
        });

        mFirstBtn = (Button) findViewById(R.id.first_btn);
        mFirstBtn.setText(R.string.activity_main_button_play);
        mFirstBtn.setOnClickListener(mOnStartGameBtnClicked);

        mSecondBtn = (Button) findViewById(R.id.second_btn);
        mSecondBtn.setText(R.string.activity_main_button_settings);
        mSecondBtn.setOnClickListener(mOnSettingsBtnClicked);

        mThirdBtn = (Button) findViewById(R.id.third_btn);
        mThirdBtn.setText(R.string.activity_main_button_share);
        mThirdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Settings.isFacebookEnabled(getBaseContext())
                        && FbController.isInitilized()
                        && FbController.isLogged()) {
                    FbController.shareOnFacebook(
                            MainActivity.this,
                            mQueueModel.getFbPlayers(),
                            getRankBitmap());
                } else {
                    try {
                        File file = new File(getCacheDir(), "SHARE.png");
                        FileOutputStream fOut = new FileOutputStream(file);
                        getRankBitmap().compress(Bitmap.CompressFormat.PNG, 100, fOut);
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

        mAllPlayers = ContactsController.loadContacts(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mKeyboardView != null)
            mKeyboardView.setColumnCount(Settings.getKeyboardColumnsCount(this));

        if (FbController.isInitilized()) {
            if (FbController.isLogged()) {
                FbController.getInstance().getFriendData(mAllPlayers);
                mPlayerChooserViewAdapter.setAutocompleteItems(mAllPlayers);
            } else {
                FbController.getInstance().logIn(this, new FbController.FacebookLoginListener() {
                    @Override
                    public void onLogged() {
                        FbController.getInstance().getFriendData(mAllPlayers);
                        mPlayerChooserViewAdapter.setAutocompleteItems(mAllPlayers);
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
    public void onBackPressed() {
        if (mBackCounter.incrementAndGet() < 2) {
            Toast.makeText(getBaseContext(), R.string.activity_main_on_back_pressed, Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mBackCounter.set(0);
                }
            }, 2000);
        } else
            super.onBackPressed();
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
        } catch (ActivityNotFoundException ex) {
            return false;
        }
        return true;
    }

    private void setImageData(Intent data) {
//        for (int i = 0; i < mPlayerContainerView.getChildCount() - 1; ++i) {
//            PlayerChooserView playerChooserView = (PlayerChooserView) mPlayerContainerView
//                    .getChildAt(i);
//            if (playerChooserView.onPhotoCreated(data))
//                return;
//        }
    }

    @Override
    public void onDialogPositiveClick(int points) {
        assignPointsAndNextTurn(points);
    }

//    @Override
//    public void onPictureRequested(Intent takePictureIntent) {
//        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//    }

    public void requestPoints() {
        PointsDialogFragment dialog = new PointsDialogFragment();
        dialog.show(getFragmentManager(), "PointsDialogFragment");
    }

    private class OnStartGameBtnClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            List<Player> currentPlayers = mPlayerChooserViewAdapter.getCurrentPlayers();
            if (currentPlayers.size() >= 2) {
                mAddPlayerBtn.setVisibility(View.GONE);
                mPlayerChooserViewAdapter.startGame();
                mQueueModel.newGame(currentPlayers);

                mKeyboardView.setVisibility(Settings.shouldUseInAppKeyboard(MainActivity.this) ?
                        View.VISIBLE : View.GONE);
                mRecyclerView.setKeepScreenOn(Settings.isKeepOnScreen(MainActivity.this));

                mFirstBtn.setText(R.string.activity_main_button_next_turn);
                mFirstBtn.setOnClickListener(mOnNextTurnBtnClicked);

                mSecondBtn.setText(R.string.activity_main_button_end_game);
                mSecondBtn.setOnClickListener(mOnEndGameBtnClicked);
            } else {
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
            mPlayerChooserViewAdapter.sortPlayers(mQueueModel);

            mKeyboardView.setVisibility(View.GONE);

            mFirstBtn.setText(R.string.activity_main_button_new_game);
            mFirstBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restartGame(true);
                }
            });

            mSecondBtn.setText(R.string.activity_main_button_play_again);
            mSecondBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restartGame(false);
                }
            });

            mThirdBtn.setText(R.string.activity_main_button_share);
            mThirdBtn.setVisibility(View.VISIBLE);
        }
    }

    private void restartGame(boolean hardReset) {
        mPlayerChooserViewAdapter.reset(hardReset);
        mAddPlayerBtn.setVisibility(View.VISIBLE);

        mFirstBtn.setText(R.string.activity_main_button_play);
        mFirstBtn.setOnClickListener(mOnStartGameBtnClicked);

        mSecondBtn.setText(R.string.activity_main_button_settings);
        mSecondBtn.setOnClickListener(mOnSettingsBtnClicked);

        mThirdBtn.setVisibility(View.GONE);
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
                requestPoints();
            }
        }
    }

    private void assignPointsAndNextTurn(int points) {
        mQueueModel.nextTurn(points);
        mKeyboardView.clearPoints();
        mPlayerChooserViewAdapter.updatePoints(
                mQueueModel.getPreviousPlayerIndex(),
                mQueueModel.getCurrentPlayerIndex());
    }

    public Bitmap getRankBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(
                mRecyclerView.getMeasuredWidth(),
                mRecyclerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mRecyclerView.draw(canvas);
        return bitmap;
    }


}
