package com.kksionek.queuedroid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;

public class PlayerChooserAdapter extends ArrayAdapter<Player> {

    private Context mCtx;
    private HashMap<String, Drawable> mThumbnailHashMap = new HashMap<>();

    public PlayerChooserAdapter(Context context) {
        super(context, R.layout.row_autocomplete);
        mCtx = context;
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
            convertView = ((LayoutInflater)mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.row_autocomplete, parent, false);
            holder = new PlayerViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else
            holder = (PlayerViewHolder) convertView.getTag();

        holder.position = position;
        Player player = getItem(position);
        holder.text.setText(player.getName());

        if (player.getImage() == null)
            holder.image.setImageResource(R.drawable.ic_contact_picture);
        else if (player.getImage().startsWith("content")) {
            holder.image.setImageURI(Uri.parse(player.getImage()));
        } else {
            Drawable drawable = null;
            synchronized (mThumbnailHashMap) {
//                if (mThumbnailHashMap.containsKey(player.getName()))
//                    drawable = mThumbnailHashMap.get(player.getName());
                drawable = player.getDrawable();
            }
            if (drawable == null) {
                holder.image.setImageResource(R.drawable.ic_contact_picture);
                ThumbnailLoader loader = new ThumbnailLoader(holder, position, player);
                loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                holder.image.setImageDrawable(drawable);
            }
        }

        return convertView;
    }

    static class PlayerViewHolder {
        ImageView image;
        TextView text;
        int position;
    }

    private class ThumbnailLoader extends AsyncTask<Void, Void, Drawable> {

        private final PlayerViewHolder mViewHolder;
        private final int mPosition;
        private final Player mPlayer;

        public ThumbnailLoader(PlayerViewHolder viewHolder, int position, Player player) {
            mViewHolder = viewHolder;
            mPosition = position;
            mPlayer = player;
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            try {
                InputStream is = (InputStream) new URL(mPlayer.getImage()).getContent();
                Drawable d = Drawable.createFromStream(is, null);
                return d;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            synchronized (mThumbnailHashMap) {
//                mThumbnailHashMap.put(mViewHolder.text.getText().toString(), drawable);
                mPlayer.setDrawable(drawable);
            }
            if (mPosition == mViewHolder.position)
                mViewHolder.image.setImageDrawable(drawable);
        }
    }
}
