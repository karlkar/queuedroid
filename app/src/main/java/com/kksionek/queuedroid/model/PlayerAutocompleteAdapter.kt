package com.kksionek.queuedroid.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.data.Player
import com.kksionek.queuedroid.databinding.RowAutocompleteBinding

internal class PlayerAutocompleteAdapter(
    context: Context,
    playerList: List<Player>
) : ArrayAdapter<Player>(context, R.layout.row_autocomplete, playerList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: PlayerViewHolder
        val resultView: View
        if (convertView == null) {
            val binding = RowAutocompleteBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
            resultView = binding.root
            holder = PlayerViewHolder(binding)
            resultView.tag = holder
        } else {
            resultView = convertView
            holder = convertView.tag as PlayerViewHolder
        }
        val player = getItem(position)!!
        holder.text.text = player.name
        Glide.with(context)
            .load(player.image)
            .placeholder(R.drawable.ic_contact_picture)
            .into(holder.image)
        holder.imageThumb.visibility =
            if (player.isFromFacebook) View.VISIBLE else View.GONE
        return resultView
    }

    private class PlayerViewHolder(binding: RowAutocompleteBinding) {
        val image = binding.thumbnail
        val imageThumb = binding.miniFb
        val text = binding.text
    }
}