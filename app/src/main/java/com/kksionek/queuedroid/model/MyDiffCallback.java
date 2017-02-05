package com.kksionek.queuedroid.model;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.kksionek.queuedroid.data.PlayerItemData;

import java.util.List;

//import static com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PAYLOAD_AUTOCOMPLETE;
//import static com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PAYLOAD_POINTS;
//import static com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PAYLOAD_TEXT;
//import static com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PAYLOAD_THUMBNAIL;

public class MyDiffCallback extends DiffUtil.Callback {
    private static final String TAG = "MyDiffCallback";

    private final List<PlayerItemData> mOldList;
    private final List<PlayerItemData> mNewList;

    public MyDiffCallback(List<PlayerItemData> oldList, List<PlayerItemData> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).getInitialPosition() ==
                mNewList.get(newItemPosition).getInitialPosition();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
    }

//    @Nullable
//    @Override
//    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
//        PlayerItemData oldItem = mOldList.get(oldItemPosition);
//        PlayerItemData newItem = mNewList.get(newItemPosition);
//
//        Bundle diffBundle = new Bundle();
//        if (!oldItem.getName().equals(newItem.getName())
//                || oldItem.isCurrent() != newItem.isCurrent()
//                || oldItem.isEditable() != newItem.isEditable()) {
//            Log.d(TAG, "getChangePayload: name/current/editable changed");
//            diffBundle.putBoolean(PAYLOAD_AUTOCOMPLETE, true);
//            diffBundle.putBoolean(PAYLOAD_TEXT, true);
//        }
//        if (!oldItem.getImage().equals(newItem.getImage())) {
//            Log.d(TAG, "getChangePayload: image changed");
//            diffBundle.putBoolean(PAYLOAD_THUMBNAIL, true);
//        }
//        if (oldItem.getPoints() != newItem.getPoints()
//                || oldItem.isEditable() != newItem.isEditable()) {
//            Log.d(TAG, "getChangePayload: points/editable changed");
//            diffBundle.putBoolean(PAYLOAD_POINTS, true);
//        }
//        if (diffBundle.size() == 0)
//            return null;
//        return diffBundle;
//    }
};
