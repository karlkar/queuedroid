package com.kksionek.queuedroid.model.keyboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kksionek.queuedroid.databinding.KeyboardButtonBinding
import com.kksionek.queuedroid.view.keyboard.ButtonViewHolder
import com.kksionek.queuedroid.view.keyboard.KeyboardView

class KeyboardViewAdapter(
    private val keyboardListener: KeyboardView.KeyboardListener
) : RecyclerView.Adapter<ButtonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val binding = KeyboardButtonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ButtonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.button.text = position.toString()
        holder.button.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            keyboardListener.onClick(pos)
        }
    }

    override fun getItemCount(): Int =
        10
}