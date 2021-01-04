package com.kksionek.queuedroid.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.collection.ArrayMap
import androidx.core.animation.addListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PlayerChooserViewHolder

class MyAnimator : DefaultItemAnimator() {
    companion object {
        private const val TAG = "MyAnimator"
        private const val ANIMATION_DURATION = 600

        const val FONT_SMALL_SIZE = 15
        const val FONT_LARGE_SIZE = 30

        private val sIncreaseAnimator = ValueAnimator.ofFloat(
            FONT_SMALL_SIZE.toFloat(),
            FONT_LARGE_SIZE.toFloat()
        ).also { it.duration = ANIMATION_DURATION.toLong() }

        private val sDecreaseAnimator = ValueAnimator.ofFloat(
            FONT_LARGE_SIZE.toFloat(),
            FONT_SMALL_SIZE.toFloat()
        ).also { it.duration = ANIMATION_DURATION.toLong() }
    }

    private val mAnimatorMap = ArrayMap<RecyclerView.ViewHolder, AnimatorInfo>()

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean =
        false

    private fun getItemHolderInfo(
        viewHolder: PlayerChooserViewHolder,
        info: PlayerItemInfo
    ): ItemHolderInfo {
        info.textSize = viewHolder.mTextView.textSize
        return info
    }

    override fun recordPreLayoutInformation(
        state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder,
        changeFlags: Int,
        payloads: List<Any>
    ): ItemHolderInfo {
        val info = super.recordPreLayoutInformation(
            state,
            viewHolder,
            changeFlags,
            payloads
        ) as PlayerItemInfo
        return getItemHolderInfo(
            viewHolder as PlayerChooserViewHolder,
            info
        )
    }

    override fun recordPostLayoutInformation(
        state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder
    ): ItemHolderInfo {
        val info = super.recordPostLayoutInformation(state, viewHolder) as PlayerItemInfo
        return getItemHolderInfo(viewHolder as PlayerChooserViewHolder, info)
    }

    override fun obtainHolderInfo(): ItemHolderInfo {
        return PlayerItemInfo()
    }

    private inner class PlayerItemInfo : ItemHolderInfo() {
        var textSize = 0f
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {
        if (oldHolder !== newHolder) {
            return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
        }
        val viewHolder = newHolder as PlayerChooserViewHolder
        val metrics = viewHolder.mTextView.context.resources.displayMetrics
        val oldInfo = preInfo as PlayerItemInfo
        val newInfo = postInfo as PlayerItemInfo
        val oldSize = oldInfo.textSize / metrics.scaledDensity
        val newSize = newInfo.textSize / metrics.scaledDensity
        val newTextView = viewHolder.mTextView
        val runningInfo = mAnimatorMap[newHolder]
        var prevAnimPlayTime: Long = 0
        if (runningInfo != null) {
            prevAnimPlayTime = runningInfo.textResizer.currentPlayTime
            runningInfo.textResizer.cancel()
        }
        var startSize = oldSize
        if (runningInfo != null) {
            startSize = runningInfo.textResizer.animatedValue as Float
        }
        val textResizeAnim = ObjectAnimator.ofFloat(newTextView, "textSize", startSize, newSize)
        if (runningInfo != null) {
            textResizeAnim.currentPlayTime = prevAnimPlayTime
        }
        textResizeAnim.addListener {
            dispatchAnimationFinished(newHolder)
            synchronized(mAnimatorMap) { mAnimatorMap.remove(newHolder) }
        }
        textResizeAnim.start()
        val runningAnimInfo = AnimatorInfo(textResizeAnim)
        mAnimatorMap[newHolder] = runningAnimInfo
        return true
    }

    private inner class AnimatorInfo(val textResizer: ObjectAnimator)

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        super.endAnimation(item)
        synchronized(mAnimatorMap) {
            if (!mAnimatorMap.isEmpty) {
                val numRunning: Int = mAnimatorMap.size
                for (i in numRunning downTo 0) {
                    try {
                        if (item === mAnimatorMap.keyAt(i)) {
                            mAnimatorMap.valueAt(i).textResizer.cancel()
                        }
                    } catch (ex: ArrayIndexOutOfBoundsException) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }

    override fun isRunning(): Boolean =
        super.isRunning() || !mAnimatorMap.isEmpty

    override fun endAnimations() {
        super.endAnimations()
        synchronized(mAnimatorMap) {
            if (mAnimatorMap.isNotEmpty()) {
                val numRunning: Int = mAnimatorMap.size
                for (i in numRunning downTo 0) {
                    mAnimatorMap.valueAt(i).textResizer.cancel()
                }
            }
        }
    }
}