package com.kksionek.queuedroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v4.util.Pair;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlayerContainerView extends LinearLayout {
    private int mCurrentPlayer = 0;
    private Button mAddPlayerBtn;
    private MainActivity mActivity;
    private ViewGroup mParent;
    private PlayerChooserAdapter mAdapter;
    private FbController mFb = null;
    private ContactsController mContactsController = null;
    private boolean mFbEnabled = true;
    private boolean mContactsEnabled = false;

    public PlayerContainerView(Context context) {
        super(context);
    }

    public PlayerContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void shareOnFacebook(ArrayList<String> players) {
//        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
//        layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        draw(canvas);
        mFb.shareOnFacebook(mActivity, players, bitmap);
    }

    private void init() {
        Log.d("PLAYERCONTAINERVIEW", "init: ");
        mAdapter = new PlayerChooserAdapter(getContext());
        mAddPlayerBtn = new Button(getContext());
        mAddPlayerBtn.setText(R.string.add_player);
        mAddPlayerBtn.setBackgroundResource(R.drawable.btn_big);
        mAddPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayerView();
            }
        });
        addPlayerView();
        addPlayerView();
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.setMargins(50, 0, 50, 0);
//        addView(mAddPlayerBtn, params);
        addView(mAddPlayerBtn);
    }

    public void addPlayerView() {
        PlayerChooserView view = new PlayerChooserView(getContext(), mParent);
        view.setAdapter(mAdapter);
        view.setActivity(mActivity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.main_activity_margin_buttons));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(this);
        addView(view, getChildCount() - 1, params);
    }

    public void nextTurn(int points, int curPlayer) {
        ((PlayerChooserView)getChildAt(mCurrentPlayer)).setCurrentTurn(false);
        ((PlayerChooserView)getChildAt(mCurrentPlayer)).setPoints(points);
        mCurrentPlayer = curPlayer;
        ((PlayerChooserView)getChildAt(mCurrentPlayer)).setCurrentTurn(true);
    }

    private void loadContactData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            mActivity.requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, MainActivity.PERMISSIONS_REQUEST_READ_CONTACTS);
        else
            mContactsEnabled = true;

        loadPlayersFromContacts();
        loadPlayersFromFacebook();
    }

    private void loadPlayersFromContacts() {
        if (mContactsEnabled) {
            if (mContactsController == null)
                mContactsController = new ContactsController();
            mContactsController.loadContacts(mActivity, mAdapter);
        }
    }

    private void loadPlayersFromFacebook() {
        if (mFbEnabled) {
            if (mFb == null)
                mFb = new FbController(mActivity.getApplication());
            mFb.getFriendData(mActivity, mAdapter);
        }
    }

    public void onContactsPermission(boolean permitted) {
        if (permitted) {
            mContactsEnabled = true;
            loadPlayersFromContacts();
        } else
            mContactsEnabled = false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mFbEnabled)
            mFb.onActivityResult(requestCode, resultCode, data);
    }

    public List<Player> onGameStarted() throws InvalidParameterException {
        ArrayList<Player> players = new ArrayList<>(getChildCount() - 1);
        PlayerChooserView tmp;
        for (int i = 0; i < getChildCount() - 1; ++i) {
            tmp = (PlayerChooserView) getChildAt(i);
            if (tmp.getPlayer() == null)
                throw new InvalidParameterException();
        }
        for (int i = 0; i < getChildCount() - 1; ++i) {
            tmp = (PlayerChooserView) getChildAt(i);
            tmp.setEditable(false);
            tmp.setCurrentTurn(i == 0);
            players.add(tmp.getPlayer());
            tmp.setPoints(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(mParent);
        mAddPlayerBtn.setVisibility(View.GONE);
        return players;
    }

    public void onGameRestarted(boolean hardReset) {
        for (int i = 0; i < getChildCount() - 1; ++i)
            getChildAt(i).animate().translationY(0).start();

        PlayerChooserView tmp;
        for (int i = 0; i < getChildCount() - 1; ++i) {
            tmp = (PlayerChooserView) getChildAt(i);
            tmp.reset(hardReset);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(mParent);
        mAddPlayerBtn.setVisibility(VISIBLE);
    }

    public void onCreate(MainActivity mainActivity, ViewGroup root) {
        mActivity = mainActivity;
        mParent = root;
        init();
        loadContactData();
    }

    public void onGameEnded(QueueModel queueModel) {
        PlayerChooserView tmp;
        for (int i = 0; i < getChildCount() - 1; ++i) {
            tmp = (PlayerChooserView) getChildAt(i);
            tmp.setCurrentTurn(false);
        }
        sort(queueModel);
    }

    private void sort(QueueModel queueModel) {
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>(getChildCount() - 1);
        for (int i = 0; i < getChildCount() - 1; ++i) {
            list.add(new Pair<>(queueModel.getPointsOfPlayer(i), i));
        }
        Collections.sort(list, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> t1, Pair<Integer, Integer> t2) {
                return t2.first.compareTo(t1.first);
            }
        });
        for (int i = 0; i < getChildCount() - 1; ++i) {
            getChildAt(list.get(i).second).animate().translationY(getChildAt(i).getBottom()
                    - getChildAt(list.get(i).second).getBottom()).start();
        }
    }
}
