package com.kksionek.queuedroid.view.keyboard;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

public class ButtonViewHolder extends RecyclerView.ViewHolder {

    public final Button button;

    public ButtonViewHolder(View itemView) {
        super(itemView);
        button = (Button) itemView;
    }
}
