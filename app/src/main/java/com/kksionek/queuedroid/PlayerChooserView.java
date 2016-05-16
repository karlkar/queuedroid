package com.kksionek.queuedroid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PlayerChooserView extends LinearLayout {

    private ImageView mPlayerThumbnail;
    private AutoCompleteTextView mPlayerName;
    private PlayerChooserAdapter mAdapter;

    public PlayerChooserView(Context context) {
        this(context, null);
    }

    public PlayerChooserView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.player_chooser_view, this, true);

        mPlayerThumbnail = (ImageView) getChildAt(0);
        mPlayerName = (AutoCompleteTextView) getChildAt(1);

        mPlayerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayerChooserAdapter.PlayerViewHolder holder = (PlayerChooserAdapter.PlayerViewHolder) view.getTag();
                mPlayerThumbnail.setImageDrawable(holder.image.getDrawable());
            }
        });
    }

    public void setAdapter(PlayerChooserAdapter adapter) {
        mAdapter = adapter;
    }

    public void addPlayerToAdapter(Player player) {
        if (mAdapter != null) {
            mAdapter.add(player);
            mPlayerName.setAdapter(mAdapter);
        }
    }
}
