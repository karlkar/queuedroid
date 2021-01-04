package com.kksionek.queuedroid.model

import androidx.recyclerview.widget.DiffUtil
import com.kksionek.queuedroid.data.PlayerItemData

//import static com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PAYLOAD_AUTOCOMPLETE;
//import static com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PAYLOAD_POINTS;
//import static com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PAYLOAD_TEXT;
//import static com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PAYLOAD_THUMBNAIL;

internal class MyDiffCallback(
    private val mOldList: List<PlayerItemData>,
    private val mNewList: List<PlayerItemData>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = mOldList.size

    override fun getNewListSize(): Int = mNewList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldList[oldItemPosition].initialPosition ==
                mNewList[newItemPosition].initialPosition
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldList[oldItemPosition] == mNewList[newItemPosition]
    } //    @Nullable

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

    companion object {
        private const val TAG = "MyDiffCallback"
    }
}