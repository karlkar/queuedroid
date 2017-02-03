package com.kksionek.queuedroid.view.keyboard;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

public class ButtonViewHolder extends RecyclerView.ViewHolder {

    public final Button button;

    public ButtonViewHolder(View itemView) {
        super(itemView);
        button = (Button) itemView;
    }
}
