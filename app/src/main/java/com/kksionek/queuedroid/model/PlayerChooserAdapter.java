package com.kksionek.queuedroid.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.data.Player;

import java.util.Comparator;

public class PlayerChooserAdapter extends ArrayAdapter<Player> {

    public PlayerChooserAdapter(Context context) {
        super(context, R.layout.row_autocomplete);
    }

    @Override
    public void add(Player object) {
        if (getPosition(object) == -1)
            super.add(object);
        else {
            if (object.isFromFacebook()) {
                remove(object);
                super.add(object);
            } else {
                return;
            }
        }
        sort(new Comparator<Player>() {
            @Override
            public int compare(Player lhs, Player rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlayerViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.row_autocomplete, parent, false);
            holder = new PlayerViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.thumbnail);
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

        return convertView;
    }

    static class PlayerViewHolder {
        ImageView image;
        TextView text;
    }
}
