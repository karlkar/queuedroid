package com.kksionek.queuedroid.view.keyboard

import androidx.recyclerview.widget.RecyclerView
import com.kksionek.queuedroid.databinding.KeyboardButtonBinding

class ButtonViewHolder(
    private val binding: KeyboardButtonBinding
) : RecyclerView.ViewHolder(binding.root) {
    val button get() = binding.root
}