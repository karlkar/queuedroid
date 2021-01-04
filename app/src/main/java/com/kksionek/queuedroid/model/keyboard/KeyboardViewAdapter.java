package com.kksionek.queuedroid.model.keyboard;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.view.keyboard.ButtonViewHolder;
import com.kksionek.queuedroid.view.keyboard.OnKeyboardItemClickListener;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class KeyboardViewAdapter extends RecyclerView.Adapter<ButtonViewHolder> {

    private final Context mCtx;
    private OnKeyboardItemClickListener mOnKeyboardItemClickListener = null;

    public KeyboardViewAdapter(Context context) {
        mCtx = context;
    }

    @Override
    public ButtonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Button button = new Button(mCtx);
        button.setBackgroundResource(R.drawable.btn_small);
        button.setTextColor(Color.rgb(0,0,0));
        return new ButtonViewHolder(button);
    }

    @Override
    public void onBindViewHolder(final ButtonViewHolder holder, int position) {
        holder.button.setText(String.valueOf(position));
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos == NO_POSITION || mOnKeyboardItemClickListener == null)
                    return;
                mOnKeyboardItemClickListener.onClick(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public void setOnKeyboardItemClickListener(OnKeyboardItemClickListener onKeyboardItemClickListener) {
        mOnKeyboardItemClickListener = onKeyboardItemClickListener;
    }
}
