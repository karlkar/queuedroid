package com.kksionek.queuedroid.view.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.model.Settings
import com.kksionek.queuedroid.model.keyboard.KeyboardViewAdapter

class KeyboardView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val mCurPointsTextView: TextView
    private val mButtonRecylerView: RecyclerView
    private var mColsNum = 0

    fun setColumnCount(columnCount: Int) {
        if (mColsNum != columnCount) {
            mColsNum = columnCount
            mButtonRecylerView.layoutManager = GridLayoutManager(
                context,
                mColsNum,
                GridLayoutManager.VERTICAL,
                false
            )
        }
    }

    val points: Int
        get() = mCurPointsTextView.text.toString().toInt()

    fun clearPoints() {
        mCurPointsTextView.text = "0"
    }

    internal inner class KeyboardListener : OnKeyboardItemClickListener {
        override fun onClick(position: Int) {
            val text = mCurPointsTextView.text
            when {
                text[0] == '0' -> mCurPointsTextView.text = position.toString()
                text.length < MAX_LENGTH -> mCurPointsTextView.text = text.toString() + position.toString()
                else -> Toast.makeText(
                    context, R.string.view_keyboard_too_long_input_message, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "KEYBOARDVIEW"
        private const val MAX_LENGTH = 10
    }

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.keyboard, this)
        mCurPointsTextView = findViewById<View>(R.id.cur_points) as TextView
        val clearButton = findViewById<View>(R.id.clear_button) as Button
        clearButton.setOnClickListener { clearPoints() }
        val backspaceButton = findViewById<View>(R.id.backspace_button) as Button
        backspaceButton.setOnClickListener {
            val text = mCurPointsTextView.text
            if (text.length == 1) clearPoints() else mCurPointsTextView.text =
                text.subSequence(0, text.length - 1)
        }
        mButtonRecylerView = findViewById<View>(R.id.button_grid_view) as RecyclerView
        val keyboardViewAdapter = KeyboardViewAdapter(getContext())
        keyboardViewAdapter.setOnKeyboardItemClickListener(KeyboardListener())
        mButtonRecylerView.adapter = keyboardViewAdapter
        mColsNum = if (isInEditMode) 5 else Settings.getKeyboardColumnsCount(getContext())
        mButtonRecylerView.layoutManager = GridLayoutManager(
            getContext(),
            mColsNum,
            GridLayoutManager.VERTICAL,
            false
        )
        mButtonRecylerView.addItemDecoration(
            SpacesItemDecoration(getContext(), R.dimen.keyboard_item_gap_size)
        )
    }
}