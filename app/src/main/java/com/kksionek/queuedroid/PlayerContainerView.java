package com.kksionek.queuedroid;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
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
        init();
    }

    public PlayerContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Log.d("PLAYERCONTAINERVIEW", "init: ");
        mAdapter = new PlayerChooserAdapter(getContext());
        mAddPlayerBtn = new Button(getContext());
        mAddPlayerBtn.setText("DODAJ GRACZA");
        mAddPlayerBtn.setBackgroundResource(R.drawable.btn_big);
        mAddPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayerView();
            }
        });
        addPlayerView();
        addPlayerView();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, (int) getResources().getDimension(R.dimen.main_activity_margin_buttons), 0, 0);
        addView(mAddPlayerBtn, params);
    }

    public void addPlayerView() {
        PlayerChooserView view = new PlayerChooserView(getContext());
        view.setAdapter(mAdapter);
        view.setActivity(mActivity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, (int) getResources().getDimension(R.dimen.main_activity_margin_buttons), 0, 0);
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

    public List<Player> onGameStarted() {
        ArrayList<Player> players = new ArrayList<>(getChildCount() - 1);
        PlayerChooserView tmp;
        for (int i = 0; i < getChildCount() - 1; ++i) {
            tmp = (PlayerChooserView) getChildAt(i);
            if (i == 0)
                tmp.setCurrentTurn(true);
            players.add(tmp.getPlayer());
            tmp.setEditable(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(mParent);
        mAddPlayerBtn.setVisibility(View.GONE);
        return players;
    }

    public void onCreate(MainActivity mainActivity, ViewGroup root) {
        mActivity = mainActivity;
        mParent = root;
        loadContactData();
    }
}
