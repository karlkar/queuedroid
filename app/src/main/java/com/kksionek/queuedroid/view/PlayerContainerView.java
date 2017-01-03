package com.kksionek.queuedroid.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kksionek.queuedroid.data.Player;
import com.kksionek.queuedroid.model.PlayerChooserAdapter;
import com.kksionek.queuedroid.model.QueueModel;
import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.model.ContactsController;
import com.kksionek.queuedroid.model.FbController;
import com.kksionek.queuedroid.model.Settings;
import com.kksionek.queuedroid.model.TooFewPlayersException;
import com.kksionek.queuedroid.model.WrongPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlayerContainerView extends LinearLayout {
    private int mCurrentPlayer = 0;
    private Button mAddPlayerBtn;
    private MainActivity mActivity;
    private ViewGroup mParent;
    private PlayerChooserAdapter mAdapter = null;

    public PlayerContainerView(Context context) {
        super(context);
    }

    public PlayerContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Bitmap getRankBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    private void init() {
        mAddPlayerBtn = new Button(getContext());
        mAddPlayerBtn.setText(R.string.view_player_chooser_button_add_player);
        mAddPlayerBtn.setBackgroundResource(R.drawable.btn_big);
        mAddPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayerView();
            }
        });
        addPlayerView();
        addPlayerView();
        addView(mAddPlayerBtn);
    }

    public void addPlayerView() {
        final PlayerChooserView view = new PlayerChooserView(getContext(), mParent);
        view.setAdapter(mAdapter);
        view.setActivity(mActivity);
        view.setOnRemoveListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimationUtils.beginDelayedTransition(mParent);
                removeView(view);
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.main_activity_players_margin));
        AnimationUtils.beginDelayedTransition(this);
        addView(view, getChildCount() - 1, params);
    }

    public void nextTurn(int points, int curPlayer) {
        ((PlayerChooserView)getChildAt(mCurrentPlayer)).setCurrentTurn(false);
        ((PlayerChooserView)getChildAt(mCurrentPlayer)).setPoints(points);
        mCurrentPlayer = curPlayer;
        ((PlayerChooserView)getChildAt(mCurrentPlayer)).setCurrentTurn(true);
    }

    public List<Player> onGameStarted() throws TooFewPlayersException, WrongPlayerException {
        if (getChildCount() - 1 < 2)
            throw new TooFewPlayersException();
        ArrayList<Player> players = new ArrayList<>(getChildCount() - 1);
        PlayerChooserView tmp;
        for (int i = 0; i < getChildCount() - 1; ++i) {
            tmp = (PlayerChooserView) getChildAt(i);
            if (tmp.getPlayer() == null)
                throw new WrongPlayerException();
        }
        for (int i = 0; i < getChildCount() - 1; ++i) {
            tmp = (PlayerChooserView) getChildAt(i);
            tmp.setEditable(false);
            tmp.setCurrentTurn(i == 0);
            players.add(tmp.getPlayer());
            tmp.setPoints(0);
        }
        AnimationUtils.beginDelayedTransition(mParent);
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
        AnimationUtils.beginDelayedTransition(mParent);
        mAddPlayerBtn.setVisibility(VISIBLE);
    }

    public void onCreate(MainActivity mainActivity, ViewGroup root) {
        mActivity = mainActivity;
        mParent = root;
        init();
    }

    public void onGameEnded(QueueModel queueModel) {
        PlayerChooserView tmp;
        for (int i = 0; i < getChildCount() - 1; ++i) {
            tmp = (PlayerChooserView) getChildAt(i);
            tmp.setCurrentTurn(false);
        }
        sortPlayersByCount(queueModel);
    }

    private void sortPlayersByCount(QueueModel queueModel) {
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>(queueModel.getPlayersCount());
        for (int i = 0; i < queueModel.getPlayersCount(); ++i) {
            list.add(new Pair<>(queueModel.getPointsOfPlayer(i), i));
        }
        Collections.sort(list, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> left, Pair<Integer, Integer> right) {
                return right.first.compareTo(left.first);
            }
        });
        for (int i = 0; i < getChildCount() - 1; ++i) {
            getChildAt(list.get(i).second).animate().translationY(getChildAt(i).getBottom()
                    - getChildAt(list.get(i).second).getBottom()).start();
        }
    }

    public void setAdapter(PlayerChooserAdapter playerChooserAdapter) {
        mAdapter = playerChooserAdapter;
        PlayerChooserView tmp;
        for (int i = 0; i < getChildCount() - 1; ++i) {
            tmp = (PlayerChooserView) getChildAt(i);
            tmp.setAdapter(mAdapter);
        }
    }
}
