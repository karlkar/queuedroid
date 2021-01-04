package com.kksionek.queuedroid.model;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.data.Player;

import java.util.List;

class PlayerChooserAdapter extends ArrayAdapter<Player> {

    PlayerChooserAdapter(Context context, List<Player> playerList) {
        super(context, R.layout.row_autocomplete, playerList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        PlayerViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.row_autocomplete, parent, false);
            holder = new PlayerViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.imageThumb = (ImageView) convertView.findViewById(R.id.mini_fb);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else
            holder = (PlayerViewHolder) convertView.getTag();

        Player player = getItem(position);
        holder.text.setText(player.getName());

        Glide.with(getContext())
                .load(player.getImage())
                .placeholder(R.drawable.ic_contact_picture)
                .into(holder.image);

        holder.imageThumb.setVisibility(player.isFromFacebook() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    private static class PlayerViewHolder {
        ImageView image;
        ImageView imageThumb;
        TextView text;
    }
}
