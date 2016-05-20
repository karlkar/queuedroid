package com.kksionek.queuedroid;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import java.util.HashMap;

public class PlayerChooserAdapter extends ArrayAdapter<Player> {

    private Context mCtx;
    private HashMap<String, Drawable> mThumbnailHashMap = new HashMap<>();

    public PlayerChooserAdapter(Context context) {
        super(context, R.layout.row_autocomplete);
        mCtx = context;
        setNotifyOnChange(true);
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
            holder.image.setImageDrawable(null);
        else {
            Drawable drawable = null;
            synchronized (mThumbnailHashMap) {
                if (mThumbnailHashMap.containsKey(player.getName()))
                    drawable = mThumbnailHashMap.get(player.getName());
            }
            holder.image.setImageDrawable(drawable);
            if (drawable == null) {
                ThumbnailLoader loader = new ThumbnailLoader(holder, position);
                loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, player.getImage());
            }
        }

        return convertView;
    }

    static class PlayerViewHolder {
        ImageView image;
        TextView text;
        int position;
    }

    private class ThumbnailLoader extends AsyncTask<String, Void, Drawable> {

        private PlayerViewHolder mViewHolder;
        private final int mPosition;

        public ThumbnailLoader(PlayerViewHolder viewHolder, int position) {
            mViewHolder = viewHolder;
            mPosition = position;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            try {
                InputStream is = (InputStream) new URL(params[0]).getContent();
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
                mThumbnailHashMap.put(mViewHolder.text.getText().toString(), drawable);
            }
            if (mPosition == mViewHolder.position)
                mViewHolder.image.setImageDrawable(drawable);
        }
    }
}
