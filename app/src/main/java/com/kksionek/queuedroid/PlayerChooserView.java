package com.kksionek.queuedroid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerChooserView extends LinearLayout {

    private ImageView mPlayerThumbnail;
    private AutoCompleteTextView mPlayerName;

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

        setBackgroundResource(R.drawable.btn_big);

        mPlayerThumbnail = (ImageView) getChildAt(0);
        mPlayerName = (AutoCompleteTextView) getChildAt(1);
        mPlayerName.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                mPlayerThumbnail.setImageDrawable(null);
                return false;
            }
        });
        mPlayerName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    View nextFocus = focusSearch(FOCUS_DOWN);
                    if (nextFocus instanceof AutoCompleteTextView)
                        nextFocus.requestFocus();
                    else
                        v.clearFocus();
                    return true;
                }
                return false;
            }
        });

        mPlayerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayerChooserAdapter.PlayerViewHolder holder = (PlayerChooserAdapter.PlayerViewHolder) view.getTag();
                mPlayerThumbnail.setImageDrawable(holder.image.getDrawable());
                View nextFocus = focusSearch(FOCUS_DOWN);
                if (nextFocus instanceof AutoCompleteTextView)
                    nextFocus.requestFocus();
                else
                    mPlayerName.clearFocus();
            }
        });
    }

    public void setAdapter(PlayerChooserAdapter adapter) {
        mPlayerName.setAdapter(adapter);
    }
}
