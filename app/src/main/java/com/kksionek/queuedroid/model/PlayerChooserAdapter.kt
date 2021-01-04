package com.kksionek.queuedroid.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.data.Player

internal class PlayerChooserAdapter(
    context: Context?,
    playerList: List<Player?>?
) : ArrayAdapter<Player?>(context!!, R.layout.row_autocomplete, playerList!!) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: PlayerViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.row_autocomplete, parent, false)
            holder = PlayerViewHolder()
            holder.image = convertView.findViewById<View>(R.id.thumbnail) as ImageView
            holder.imageThumb = convertView.findViewById<View>(R.id.mini_fb) as ImageView
            holder.text = convertView.findViewById<View>(R.id.text) as TextView
            convertView.tag = holder
        } else holder = convertView.tag as PlayerViewHolder
        val player = getItem(position)
        holder.text!!.text = player!!.name
        Glide.with(context)
            .load(player.image)
            .placeholder(R.drawable.ic_contact_picture)
            .into(holder.image!!)
        holder.imageThumb!!.visibility =
            if (player.isFromFacebook) View.VISIBLE else View.GONE
        return convertView!!
    }

    private class PlayerViewHolder {
        var image: ImageView? = null
        var imageThumb: ImageView? = null
        var text: TextView? = null
    }
}