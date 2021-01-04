package com.kksionek.queuedroid.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.kksionek.queuedroid.model.PlayerChooserViewAdapter;

import java.util.List;

public class MyAnimator extends DefaultItemAnimator {
    private static final String TAG = "MyAnimator";

    private static final int ANIMATION_DURATION = 600;
    public static final int FONT_SMALL_SIZE = 15;
    public static final int FONT_LARGE_SIZE = 30;

    private static final ValueAnimator sIncreaseAnimator = ValueAnimator.ofFloat(
            FONT_SMALL_SIZE,
            FONT_LARGE_SIZE);
    private static final ValueAnimator sDecreaseAnimator = ValueAnimator.ofFloat(
            FONT_LARGE_SIZE,
            FONT_SMALL_SIZE);

    static {
        sIncreaseAnimator.setDuration(ANIMATION_DURATION);
        sDecreaseAnimator.setDuration(ANIMATION_DURATION);
    }

    private final ArrayMap<RecyclerView.ViewHolder, AnimatorInfo> mAnimatorMap = new ArrayMap<>();

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        return false;
    }

    @NonNull
    private ItemHolderInfo getItemHolderInfo(
            PlayerChooserViewAdapter.PlayerChooserViewHolder viewHolder,
            PlayerItemInfo info) {
        info.textSize = viewHolder.mTextView.getTextSize();
        return info;
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(RecyclerView.State state,
                                                     RecyclerView.ViewHolder viewHolder,
                                                     int changeFlags,
                                                     List<Object> payloads) {
        PlayerItemInfo info = (PlayerItemInfo) super.recordPreLayoutInformation(
                state,
                viewHolder,
                changeFlags,
                payloads);
        return getItemHolderInfo(
                (PlayerChooserViewAdapter.PlayerChooserViewHolder) viewHolder,
                info);
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPostLayoutInformation(@NonNull RecyclerView.State state,
                                                      @NonNull RecyclerView.ViewHolder viewHolder) {
        PlayerItemInfo info = (PlayerItemInfo) super.recordPostLayoutInformation(state, viewHolder);
        return getItemHolderInfo((PlayerChooserViewAdapter.PlayerChooserViewHolder) viewHolder, info);
    }

    @Override
    public ItemHolderInfo obtainHolderInfo() {
        return new PlayerItemInfo();
    }

    private class PlayerItemInfo extends ItemHolderInfo {
        float textSize;
    }

    @Override
    public boolean animateChange(
            @NonNull RecyclerView.ViewHolder oldHolder,
            @NonNull final RecyclerView.ViewHolder newHolder,
            @NonNull ItemHolderInfo preInfo,
            @NonNull ItemHolderInfo postInfo) {
        if (oldHolder != newHolder) {
            return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
        }

        PlayerChooserViewAdapter.PlayerChooserViewHolder viewHolder =
                (PlayerChooserViewAdapter.PlayerChooserViewHolder) newHolder;

        DisplayMetrics metrics = viewHolder.mTextView.getContext().getResources().getDisplayMetrics();
        PlayerItemInfo oldInfo = (PlayerItemInfo) preInfo;
        PlayerItemInfo newInfo = (PlayerItemInfo) postInfo;
        float oldSize = oldInfo.textSize / metrics.scaledDensity;
        float newSize = newInfo.textSize / metrics.scaledDensity;

        final TextView newTextView = viewHolder.mTextView;

        AnimatorInfo runningInfo = mAnimatorMap.get(newHolder);
        long prevAnimPlayTime = 0;
        if (runningInfo != null) {
            prevAnimPlayTime = runningInfo.textResizer.getCurrentPlayTime();
            runningInfo.textResizer.cancel();
        }

        float startSize = oldSize;
        if (runningInfo != null) {
            startSize = (float) runningInfo.textResizer.getAnimatedValue();
        }
        ObjectAnimator textResizeAnim = ObjectAnimator.ofFloat(newTextView, "textSize", startSize, newSize);
        if (runningInfo != null) {
            textResizeAnim.setCurrentPlayTime(prevAnimPlayTime);
        }

        textResizeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchAnimationFinished(newHolder);
                synchronized (mAnimatorMap) {
                    mAnimatorMap.remove(newHolder);
                }
            }
        });
        textResizeAnim.start();

        AnimatorInfo runningAnimInfo = new AnimatorInfo(textResizeAnim);
        mAnimatorMap.put(newHolder, runningAnimInfo);

        return true;
    }

    private class AnimatorInfo {
        final ObjectAnimator textResizer;

        public AnimatorInfo(ObjectAnimator oldTextRotator) {
            this.textResizer = oldTextRotator;
        }
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        super.endAnimation(item);
        synchronized (mAnimatorMap) {
            if (!mAnimatorMap.isEmpty()) {
                final int numRunning = mAnimatorMap.size();
                for (int i = numRunning; i >= 0; i--) {
                    try {
                        if (item == mAnimatorMap.keyAt(i)) {
                            mAnimatorMap.valueAt(i).textResizer.cancel();
                        }
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() || !mAnimatorMap.isEmpty();
    }

    @Override
    public void endAnimations() {
        super.endAnimations();
        synchronized (mAnimatorMap) {
            if (!mAnimatorMap.isEmpty()) {
                final int numRunning = mAnimatorMap.size();
                for (int i = numRunning; i >= 0; i--) {
                    mAnimatorMap.valueAt(i).textResizer.cancel();
                }
            }
        }
    }
}
