package com.kksionek.queuedroid.view.keyboard

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

internal class SpacesItemDecoration(ctx: Context, @DimenRes space: Int) : ItemDecoration() {

    private val space = ctx.resources.getDimensionPixelSize(space)

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.left = space
        outRect.right = space
        outRect.top = space
    }

}