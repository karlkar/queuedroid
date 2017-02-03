package com.kksionek.queuedroid.model;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.data.Player;
import com.kksionek.queuedroid.data.PlayerItemData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class PlayerChooserViewAdapter extends RecyclerView.Adapter<PlayerChooserViewAdapter.PlayerChooserViewHolder> {

    private static final String TAG = "PlayerChooserViewAdapte";
    private static final int ANIMATION_DURATION = 600;
    private static final int FONT_SMALL_SIZE = 15;
    private static final int FONT_LARGE_SIZE = 30;

    private static final ValueAnimator sIncreaseAnimator = ValueAnimator.ofFloat(
            FONT_SMALL_SIZE,
            FONT_LARGE_SIZE);
    private static final ValueAnimator sDecreaseAnimator = ValueAnimator.ofFloat(
            FONT_LARGE_SIZE,
            FONT_SMALL_SIZE);
    static {
        sIncreaseAnimator.setDuration(ANIMATION_DURATION);
        sDecreaseAnimator.setDuration(ANIMATION_DURATION);
    }

    private final List<PlayerItemData> mPlayers = new ArrayList<>();
    private List<Player> mAllPossiblePlayers = new ArrayList<>();
    private final QueueModel mQueueModel;
    private boolean mEditable = true;

    public PlayerChooserViewAdapter(QueueModel queueModel) {
        mQueueModel = queueModel;
        setHasStableIds(true);
        mPlayers.add(new PlayerItemData());
        mPlayers.add(new PlayerItemData());
    }

    @Override
    public long getItemId(int position) {
        return mPlayers.get(position).getName().hashCode();
    }

    public void add(Player player) {
        mPlayers.add(new PlayerItemData(player));
        notifyItemInserted(mPlayers.size() - 1);
    }

    @Override
    public PlayerChooserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewItem = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.player_chooser_view,
                parent,
                false);
        return new PlayerChooserViewHolder(viewItem);
    }

    @Override
    public void onBindViewHolder(final PlayerChooserViewHolder holder, int position) {
        holder.bindTo(mPlayers.get(position));
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    public void setAutocompleteItems(List<Player> allPlayers) {
        mAllPossiblePlayers = allPlayers;
    }

    public List<Player> getCurrentPlayers() {
        List<Player> players = new ArrayList<>();
        for (PlayerItemData playerItemData : mPlayers) {
            if (playerItemData.getPlayer() != null
                    && !playerItemData.getPlayer().getName().isEmpty())
                players.add(playerItemData.getPlayer());
        }
        return players;
    }

    public void sortPlayers(QueueModel queueModel) {
        for (int i = 0; i < queueModel.getPlayersCount(); ++i) {
            mPlayers.get(i).setPoints(queueModel.getPointsOfPlayer(i));
        }
        Collections.sort(mPlayers, new Comparator<PlayerItemData>() {
            @Override
            public int compare(PlayerItemData left, PlayerItemData right) {
                return Integer.valueOf(right.getPoints()).compareTo(left.getPoints());
            }
        });
        notifyDataSetChanged();
    }

    public void updatePoints(int previousPlayerIndex, int currentPlayerIndex) {
        PlayerItemData prevPlayer = mPlayers.get(previousPlayerIndex);
        prevPlayer.setPoints(mQueueModel.getPointsOfPlayer(previousPlayerIndex));
        prevPlayer.setCurrent(false);
        mPlayers.get(currentPlayerIndex).setCurrent(true);
        notifyDataSetChanged();
    }

    public void reset(boolean hardReset) {
        if (hardReset) {
            mPlayers.clear();
            mPlayers.add(new PlayerItemData());
            mPlayers.add(new PlayerItemData());
        } else {
            for (PlayerItemData playerItemData : mPlayers) {
                playerItemData.setPoints(0);
                playerItemData.setCurrent(false);
            }
            Collections.sort(mPlayers, new Comparator<PlayerItemData>() {
                @Override
                public int compare(PlayerItemData o1, PlayerItemData o2) {
                    return (o1.getInitialPosition() < o2.getInitialPosition()) ?
                            -1 : ((o1.getInitialPosition() == o2.getInitialPosition()) ? 0 : 1);
                }
            });
        }
        mEditable = true;
        notifyDataSetChanged();
    }

    public void startGame() {
        mEditable = false;
        mPlayers.get(0).setCurrent(true);
        notifyDataSetChanged();
    }

    class PlayerChooserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mThumbnail;
        private final AutoCompleteTextView mAutoCompleteTextView;
        private final TextView mTextView;
        private final Button mPointsBtn;
        private boolean mCurrent;

        PlayerChooserViewHolder(View viewItem) {
            super(viewItem);
            mThumbnail = (ImageView) viewItem.findViewById(R.id.thumbnail);
            mAutoCompleteTextView = (AutoCompleteTextView) viewItem.findViewById(R.id.text);
            mTextView = (TextView) viewItem.findViewById(R.id.staticText);
            mPointsBtn = (Button) viewItem.findViewById(R.id.pointsView);
            mCurrent = false;

            mAutoCompleteTextView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        int pos = getAdapterPosition();
                        if (pos == NO_POSITION)
                            return false;
                        mPlayers.get(pos).reset();
                        mThumbnail.setImageResource(R.drawable.ic_contact_picture);
                    }
                    return false;
                }
            });

            mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int pos = getAdapterPosition();
                    if (pos == NO_POSITION)
                        return;
                    Player player = (Player) parent.getItemAtPosition(position);
                    mPlayers.get(pos).set(player);
                    Glide.with(view.getContext())
                            .load(player.getImage())
                            .placeholder(R.drawable.ic_contact_picture)
                            .into(mThumbnail);
                    InputMethodManager imm = (InputMethodManager) view.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mAutoCompleteTextView.getWindowToken(), 0);
                }
            });

            mAutoCompleteTextView.setAdapter(new PlayerChooserAdapter(
                    mAutoCompleteTextView.getContext(),
                    mAllPossiblePlayers));

            mPointsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEditable) {
                        int pos = getAdapterPosition();
                        if (pos > 0 && pos < mPlayers.size()) {
                            mPlayers.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    }
                }
            });
        }

        public void bindTo(PlayerItemData playerInList) {
            Context context = mTextView.getContext();
            bindThumbnail(playerInList, context);
            bindAutoCompleteTextView(playerInList, context);
            bindTextView(playerInList);
            bindPointsBtn(playerInList, context);
        }

        private void bindThumbnail(PlayerItemData playerInList, Context context) {
            Glide.with(context)
                    .load(playerInList.getImage())
                    .placeholder(R.drawable.ic_contact_picture)
                    .into(mThumbnail);
        }

        private void bindAutoCompleteTextView(final PlayerItemData playerInList, Context context) {
            if (mEditable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    mAutoCompleteTextView.setText(playerInList.getName(), false);
                else
                    mAutoCompleteTextView.setText(playerInList.getName());

                mAutoCompleteTextView.setVisibility(View.VISIBLE);
            } else
                mAutoCompleteTextView.setVisibility(View.GONE);
        }

        private void bindTextView(PlayerItemData playerInList) {
            mTextView.setVisibility(mEditable ? View.GONE : View.VISIBLE);
            mTextView.setText(playerInList.getName());
            if (mEditable) {
                mCurrent = false;
            } else {
                if (playerInList.isCurrent() || mCurrent) {
                    ValueAnimator animator = mCurrent ? sDecreaseAnimator : sIncreaseAnimator;
                    animator.removeAllUpdateListeners();
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float animatedValue = (float) valueAnimator.getAnimatedValue();
                            mTextView.setTextSize(animatedValue);
                        }
                    });
                    animator.start();
                    mCurrent = playerInList.isCurrent();
                }
            }
        }

        private void bindPointsBtn(final PlayerItemData playerInList, Context context) {
            TransitionDrawable transitionDrawable = (TransitionDrawable) mPointsBtn.getBackground();
            int duration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
            if (mEditable) {
                if (!playerInList.isEditable()) {
                    transitionDrawable.startTransition(duration);
                    mPointsBtn.setText("");
                }
            } else {
                if (playerInList.isEditable()) {
                    transitionDrawable.reverseTransition(duration);
                    mPointsBtn.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPointsBtn.setText(String.valueOf(playerInList.getPoints()));
                        }
                    }, duration);
                } else
                    mPointsBtn.setText(String.valueOf(playerInList.getPoints()));
            }
            playerInList.setEditable(mEditable);
        }
    }
}