package com.kksionek.queuedroid.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.data.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PlayerChooserAdapter extends ArrayAdapter<Player> {

    private FbController mFb = null;
    private ContactsController mContactsController = null;

    private boolean mContactsLoaded = false;
    private boolean mFacebookLoaded = false;

    private ArrayList<Player> mList = new ArrayList<>();

    public PlayerChooserAdapter(Context context) {
        super(context, R.layout.row_autocomplete);
    }

    @Override
    public void add(Player object) {
        if (!mList.contains(object)) {
            mList.add(object);
            super.add(object);
        } else {
            if (object.isFromFacebook()) {
                mList.remove(object);
                super.remove(object);
                mList.add(object);
                super.add(object);
            } else
                return;
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
    public void clear() {
        mList.clear();
        super.clear();
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

    private void loadPlayersFromContacts() {
        if (mContactsController == null)
            mContactsController = new ContactsController();
        mContactsController.loadContacts(getContext(), this);
    }

    private void loadPlayersFromFacebook() {
        if (mFb == null)
            mFb = FbController.getInstance();
        if (mFb.isLogged())
            mFb.getFriendData(this);
    }

    public void reloadDataset(boolean contactsEnabled, boolean facebookEnabled) {
        if (mContactsLoaded == contactsEnabled && mFacebookLoaded == facebookEnabled)
            return;

        clear();
        if (contactsEnabled) {
            loadPlayersFromContacts();
        }
        if (facebookEnabled) {
            loadPlayersFromFacebook();
        }

        mFacebookLoaded = facebookEnabled;
        mContactsLoaded = contactsEnabled;
    }

    static class PlayerViewHolder {
        ImageView image;
        TextView text;
    }
}
