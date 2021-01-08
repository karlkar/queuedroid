package com.kksionek.queuedroid.view.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.databinding.KeyboardBinding
import com.kksionek.queuedroid.model.SettingsProviderImpl
import com.kksionek.queuedroid.model.keyboard.KeyboardViewAdapter

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var keyboardColsNum = 0

    private val binding: KeyboardBinding

    fun setColumnCount(columnCount: Int) {
        if (keyboardColsNum != columnCount) {
            keyboardColsNum = columnCount
            binding.buttonGridView.layoutManager = GridLayoutManager(
                context,
                keyboardColsNum,
                GridLayoutManager.VERTICAL,
                false
            )
        }
    }

    val points: Int
        get() = binding.curPoints.text.toString().toInt()

    fun clearPoints() {
        binding.curPoints.text = "0"
    }

    inner class KeyboardListener : OnKeyboardItemClickListener {
        @SuppressLint("SetTextI18n")
        override fun onClick(position: Int) {
            val text = binding.curPoints.text.toString()
            when {
                text == "0" -> binding.curPoints.text = position.toString()
                text.length < MAX_LENGTH -> binding.curPoints.text = "$text$position"
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
        binding = KeyboardBinding.inflate(LayoutInflater.from(context), this, true).apply {
            clearButton.setOnClickListener { clearPoints() }
            backspaceButton.setOnClickListener {
                val text = curPoints.text
                if (text.length == 1) {
                    clearPoints()
                } else {
                    curPoints.text = text.subSequence(0, text.length - 1)
                }
            }
            buttonGridView.adapter = KeyboardViewAdapter(KeyboardListener())

            keyboardColsNum = if (isInEditMode) {
                5
            } else {
                SettingsProviderImpl(PreferenceManager.getDefaultSharedPreferences(context))
                    .getKeyboardColumnsCount()
            }
            buttonGridView.layoutManager = GridLayoutManager(
                context,
                keyboardColsNum,
                GridLayoutManager.VERTICAL,
                false
            )
            buttonGridView.addItemDecoration(
                SpacesItemDecoration(context, R.dimen.keyboard_item_gap_size)
            )
        }
    }
}