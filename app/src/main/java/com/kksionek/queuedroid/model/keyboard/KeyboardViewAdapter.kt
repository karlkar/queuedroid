package com.kksionek.queuedroid.model.keyboard

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.view.keyboard.ButtonViewHolder
import com.kksionek.queuedroid.view.keyboard.OnKeyboardItemClickListener

class KeyboardViewAdapter(private val mCtx: Context) : RecyclerView.Adapter<ButtonViewHolder>() {

    private var mOnKeyboardItemClickListener: OnKeyboardItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val button = Button(mCtx)
        button.setBackgroundResource(R.drawable.btn_small)
        button.setTextColor(Color.rgb(0, 0, 0))
        return ButtonViewHolder(button)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.button.text = position.toString()
        holder.button.setOnClickListener(View.OnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION || mOnKeyboardItemClickListener == null) return@OnClickListener
            mOnKeyboardItemClickListener!!.onClick(pos)
        })
    }

    override fun getItemCount(): Int =
        10

    fun setOnKeyboardItemClickListener(onKeyboardItemClickListener: OnKeyboardItemClickListener?) {
        mOnKeyboardItemClickListener = onKeyboardItemClickListener
    }
}