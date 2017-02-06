package com.kksionek.queuedroid.model;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.kksionek.queuedroid.view.MyAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static android.view.View.FOCUS_DOWN;

public class PlayerChooserViewAdapter extends RecyclerView.Adapter<PlayerChooserViewAdapter.PlayerChooserViewHolder> {

    private static final String TAG = "PlayerChooserViewAdapte";

    private static final String PAYLOAD_THUMBNAIL = "PAYLOAD_THUMBNAIL";
    private static final String PAYLOAD_AUTOCOMPLETE = "PAYLOAD_AUTOCOMPLETE";
    private static final String PAYLOAD_TEXT = "PAYLOAD_TEXT";
    private static final String PAYLOAD_POINTS = "PAYLOAD_POINTS";

    private final List<PlayerItemData> mPlayers = new ArrayList<>();
    private List<Player> mAllPossiblePlayers = new ArrayList<>();
    private final QueueModel mQueueModel;
    private final ActionListener mActionListener;
    private PlayerChooserViewHolder mPhotoRequester;

    public PlayerChooserViewAdapter(ActionListener actionListener, QueueModel queueModel) {
        mActionListener = actionListener;
        mQueueModel = queueModel;
        setHasStableIds(true);
        if (queueModel.getPlayersCount() == 0) {
            mPlayers.add(new PlayerItemData());
            mPlayers.add(new PlayerItemData());
        } else {
            for (int i = 0; i < mQueueModel.getPlayersCount(); ++i) {
                mPlayers.add(new PlayerItemData(mQueueModel.getPlayerAt(i), mQueueModel.getPointsOfPlayer(i)));
                mPlayers.get(i).setEditable(false);
                mPlayers.get(i).setCurrent(i == mQueueModel.getCurrentPlayerIndex());
            }
        }
    }

    public PlayerChooserViewAdapter(ActionListener actionListener, QueueModel queueModel, ArrayList<Player> items) {
        mActionListener = actionListener;
        mQueueModel = queueModel;
        setHasStableIds(true);
        for (Player player : items) {
            mPlayers.add(new PlayerItemData(player));
        }
        while (mPlayers.size() < 2) {
            mPlayers.add(new PlayerItemData());
        }
    }

    @Override
    public long getItemId(int position) {
        return mPlayers.get(position).getInitialPosition();
    }

    public void add(Player player) {
        mPlayers.add(new PlayerItemData(player));
        notifyItemInserted(mPlayers.size() - 1);
    }

    public void addAll(ArrayList<Player> adapterItems) {
        for (Player player : adapterItems) {
            mPlayers.add(new PlayerItemData(player));
        }
        notifyItemRangeInserted(mPlayers.size(), adapterItems.size());
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
    public void onBindViewHolder(PlayerChooserViewHolder holder, int position, List<Object> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }

        Log.d(TAG, "onBindViewHolder: with payload");
        Bundle o = (Bundle) payloads.get(0);
        for (String key : o.keySet()) {
            switch (key) {
                case PAYLOAD_THUMBNAIL:
                    holder.bindThumbnail(mPlayers.get(position));
                    break;
                case PAYLOAD_AUTOCOMPLETE:
                    holder.bindAutoCompleteTextView(mPlayers.get(position));
                    break;
                case PAYLOAD_TEXT:
                    holder.bindTextView(mPlayers.get(position));
                    break;
                case PAYLOAD_POINTS:
                    holder.bindPointsBtn(mPlayers.get(position));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    public void setAutocompleteItems(List<Player> allPlayers) {
        mAllPossiblePlayers = allPlayers;
    }

    public ArrayList<Player> getCurrentPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        for (PlayerItemData playerItemData : mPlayers) {
            if (playerItemData.getPlayer() != null
                    && !playerItemData.getPlayer().getName().isEmpty())
                players.add(playerItemData.getPlayer());
        }
        return players;
    }

    public void endGame(QueueModel queueModel) {
        final ArrayList<PlayerItemData> newList = new ArrayList<>(mPlayers.size());
        PlayerItemData itemData;
        for (int i = 0; i < queueModel.getPlayersCount(); ++i) {
            itemData = (PlayerItemData) mPlayers.get(i).clone();
            newList.add(itemData);
            itemData.setPoints(queueModel.getPointsOfPlayer(i));
            itemData.setCurrent(false);
        }

        Collections.sort(newList, new Comparator<PlayerItemData>() {
            @Override
            public int compare(PlayerItemData left, PlayerItemData right) {
                return Integer.valueOf(right.getPoints()).compareTo(left.getPoints());
            }
        });

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffCallback(mPlayers, newList));
        mPlayers.clear();
        mPlayers.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
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
            int size = mPlayers.size();
            mPlayers.clear();
            mPlayers.add(new PlayerItemData());
            mPlayers.add(new PlayerItemData());
            notifyItemRangeChanged(0, 2);
            notifyItemRangeRemoved(2, size - 2);
        } else {
            ArrayList<PlayerItemData> newList = new ArrayList<>(mPlayers.size());
            PlayerItemData itemData;
            for (PlayerItemData item : mPlayers) {
                itemData = (PlayerItemData) item.clone();
                itemData.setPoints(0);
                itemData.setEditable(true);
                itemData.setCurrent(false);
                newList.add(itemData);
            }
            Collections.sort(newList, new Comparator<PlayerItemData>() {
                @Override
                public int compare(PlayerItemData o1, PlayerItemData o2) {
                    return Integer.valueOf(o1.getInitialPosition()).compareTo(o2.getInitialPosition());
                }
            });
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffCallback(mPlayers, newList));
            mPlayers.clear();
            mPlayers.addAll(newList);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public void startGame() {
        for (PlayerItemData playerItemData : mPlayers)
            playerItemData.setEditable(false);
        mPlayers.get(0).setCurrent(true);
//        Bundle diff = new Bundle();
//        diff.putBoolean(PAYLOAD_POINTS, true);
//        diff.putBoolean(PAYLOAD_TEXT, true);
//        diff.putBoolean(PAYLOAD_AUTOCOMPLETE, true);
//        notifyItemRangeChanged(0, mPlayers.size(), diff);
        notifyItemRangeChanged(0, mPlayers.size());
    }

    public void setRequestedPhoto(Uri imageUri) {
        if (mPhotoRequester != null) {
            int pos = mPhotoRequester.getAdapterPosition();
            if (pos != NO_POSITION) {
                mPlayers.get(pos).getPlayer().setImage(imageUri.toString());
                notifyItemChanged(pos);
            }
            mPhotoRequester = null;
        }
    }

    public class PlayerChooserViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mThumbnail;
        public final AutoCompleteTextView mAutoCompleteTextView;
        public final TextView mTextView;
        public final Button mPointsBtn;

        PlayerChooserViewHolder(View viewItem) {
            super(viewItem);
            mThumbnail = (ImageView) viewItem.findViewById(R.id.thumbnail);
            mAutoCompleteTextView = (AutoCompleteTextView) viewItem.findViewById(R.id.text);
            mTextView = (TextView) viewItem.findViewById(R.id.staticText);
            mPointsBtn = (Button) viewItem.findViewById(R.id.pointsView);

            mThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAutoCompleteTextView.getText().length() > 0) {
                        if (mActionListener != null) {
                            mPhotoRequester = PlayerChooserViewHolder.this;
                            mActionListener.requestPhoto();
                        }
                    }
                }
            });

            mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int pos = getAdapterPosition();
                    if (pos == NO_POSITION)
                        return;
                    mPlayers.get(pos).getPlayer().setName(s.toString());
                }
            });

            mAutoCompleteTextView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    int pos = getAdapterPosition();
                    if (pos == NO_POSITION)
                        return false;
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        mPlayers.get(pos).reset();
                        mThumbnail.setImageResource(R.drawable.ic_contact_picture);
                    }
                    mPlayers.get(pos).getPlayer().setName(mAutoCompleteTextView.getText().toString());
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
                    mPlayers.get(pos).set(player, 0);
                    Glide.with(view.getContext())
                            .load(player.getImage())
                            .placeholder(R.drawable.ic_contact_picture)
                            .into(mThumbnail);
                    InputMethodManager imm = (InputMethodManager) view.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mAutoCompleteTextView.getWindowToken(), 0);
                    View nextFocus = mAutoCompleteTextView.focusSearch(FOCUS_DOWN);
                    if (nextFocus != null)
                        nextFocus.requestFocus(FOCUS_DOWN);
                }
            });

            mAutoCompleteTextView.setAdapter(new PlayerChooserAdapter(
                    mAutoCompleteTextView.getContext(),
                    mAllPossiblePlayers));

            mPointsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos >= 0 && pos < mPlayers.size()) {
                        PlayerItemData playerItemData = mPlayers.get(pos);
                        if (!playerItemData.isEditable())
                            return;
                        if (mPlayers.size() > 2) {
                            mPlayers.remove(pos);
                            notifyItemRemoved(pos);
                        } else {
                            playerItemData.reset();
                            notifyItemChanged(pos);
                        }
                    }
                }
            });
        }

        public void bindTo(PlayerItemData playerInList) {
            bindThumbnail(playerInList);
            bindAutoCompleteTextView(playerInList);
            bindTextView(playerInList);
            bindPointsBtn(playerInList);
        }

        public void bindThumbnail(PlayerItemData playerInList) {
            Context context = mThumbnail.getContext();
            Glide.with(context)
                    .load(playerInList.getImage())
                    .placeholder(R.drawable.ic_contact_picture)
                    .into(mThumbnail);
        }

        public void bindAutoCompleteTextView(final PlayerItemData playerInList) {
            if (playerInList.isEditable()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    mAutoCompleteTextView.setText(playerInList.getName(), false);
                else
                    mAutoCompleteTextView.setText(playerInList.getName());

                mAutoCompleteTextView.setVisibility(View.VISIBLE);
            } else
                mAutoCompleteTextView.setVisibility(View.GONE);
        }

        public void bindTextView(PlayerItemData playerInList) {
            mTextView.setVisibility(playerInList.isEditable() ? View.GONE : View.VISIBLE);
            mTextView.setText(playerInList.getName());
            mTextView.setTextSize(playerInList.isCurrent() ? MyAnimator.FONT_LARGE_SIZE : MyAnimator.FONT_SMALL_SIZE);
        }

        public void bindPointsBtn(final PlayerItemData playerInList) {
            if (playerInList.isEditable()) {
                mPointsBtn.setBackgroundResource(R.drawable.btn_cancel);
                mPointsBtn.setText("");
            } else {
                mPointsBtn.setBackgroundResource(R.drawable.btn_uncheck);
                mPointsBtn.setText(String.valueOf(playerInList.getPoints()));
            }
        }
    }
}