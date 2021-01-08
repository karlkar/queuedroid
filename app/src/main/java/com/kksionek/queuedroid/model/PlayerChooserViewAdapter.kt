package com.kksionek.queuedroid.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.data.Player
import com.kksionek.queuedroid.data.PlayerItemData
import com.kksionek.queuedroid.databinding.PlayerChooserViewBinding
import com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PlayerChooserViewHolder
import com.kksionek.queuedroid.view.MyAnimator
import java.util.*

class PlayerChooserViewAdapter : RecyclerView.Adapter<PlayerChooserViewHolder> {

    private val players: MutableList<PlayerItemData> = mutableListOf()
    private var allPossiblePlayers: List<Player> = mutableListOf()
    private val queueModel: QueueModel
    private val actionListener: ActionListener?
    private var photoRequester: PlayerChooserViewHolder? = null

    constructor(aActionListener: ActionListener?, aQueueModel: QueueModel) {
        actionListener = aActionListener
        queueModel = aQueueModel
        setHasStableIds(true)
        if (aQueueModel.playersCount == 0) {
            players.add(PlayerItemData())
            players.add(PlayerItemData())
        } else {
            for (i in 0 until queueModel.playersCount) {
                players.add(
                    PlayerItemData(
                        queueModel.getPlayerAt(i),
                        queueModel.getPointsOfPlayer(i)
                    )
                )
                players[i].isEditable = false
                players[i].isCurrent = i == queueModel.currentPlayerIndex
            }
        }
    }

    constructor(
        aActionListener: ActionListener?,
        aQueueModel: QueueModel,
        items: List<Player>
    ) {
        actionListener = aActionListener
        queueModel = aQueueModel
        setHasStableIds(true)
        for (player in items) {
            players.add(PlayerItemData(player))
        }
        while (players.size < 2) {
            players.add(PlayerItemData())
        }
    }

    override fun getItemId(position: Int): Long {
        return players[position].initialPosition.toLong()
    }

    fun add(player: Player) {
        players.add(PlayerItemData(player))
        notifyItemInserted(players.size - 1)
    }

    fun addAll(adapterItems: List<Player>) {
        players.addAll(adapterItems.map { PlayerItemData(it) })
        notifyItemRangeInserted(players.size, adapterItems.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerChooserViewHolder {
        val binding = PlayerChooserViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayerChooserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerChooserViewHolder, position: Int) {
        holder.bindTo(players[position])
    }

    override fun onBindViewHolder(
        holder: PlayerChooserViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        Log.d(TAG, "onBindViewHolder: with payload")
        val o = payloads[0] as Bundle
        for (key in o.keySet()) {
            when (key) {
                PAYLOAD_THUMBNAIL -> holder.bindThumbnail(players[position])
                PAYLOAD_AUTOCOMPLETE -> holder.bindAutoCompleteTextView(players[position])
                PAYLOAD_TEXT -> holder.bindTextView(players[position])
                PAYLOAD_POINTS -> holder.bindPointsBtn(players[position])
            }
        }
    }

    override fun getItemCount(): Int = players.size

    fun setAutocompleteItems(allPlayers: List<Player>) {
        allPossiblePlayers = allPlayers
    }

    val currentPlayers: List<Player>
        get() {
            return players
                .map { it.player }
                .filter { it.name.isNotEmpty() }
        }

    fun endGame(queueModel: QueueModel) {
        val newList = mutableListOf<PlayerItemData>()
        for (i in 0 until queueModel.playersCount) {
            newList.add(
                players[i].copy(
                    points = queueModel.getPointsOfPlayer(i),
                    isCurrent = false
                )
            )
        }
        newList.sortWith { left, right ->
            Integer.valueOf(right.points).compareTo(left.points)
        }
        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(players, newList))
        players.clear()
        players.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updatePoints(previousPlayerIndex: Int, currentPlayerIndex: Int) {
        val prevPlayer = players[previousPlayerIndex]
        prevPlayer.points = queueModel.getPointsOfPlayer(previousPlayerIndex)
        prevPlayer.isCurrent = false
        players[currentPlayerIndex].isCurrent = true
        notifyDataSetChanged()
    }

    fun reset(hardReset: Boolean) {
        if (hardReset) {
            val size = players.size
            players.clear()
            players.add(PlayerItemData())
            players.add(PlayerItemData())
            notifyItemRangeChanged(0, 2)
            notifyItemRangeRemoved(2, size - 2)
        } else {
            val newList = players.map {
                it.copy(
                    points = 0,
                    isEditable = true,
                    isCurrent = false
                )
            }.sortedBy { it.initialPosition }
            val diffResult = DiffUtil.calculateDiff(MyDiffCallback(players, newList))
            players.clear()
            players.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun startGame() {
        players.forEach { it.isEditable = false }
        players.first().isCurrent = true
        //        Bundle diff = new Bundle();
//        diff.putBoolean(PAYLOAD_POINTS, true);
//        diff.putBoolean(PAYLOAD_TEXT, true);
//        diff.putBoolean(PAYLOAD_AUTOCOMPLETE, true);
//        notifyItemRangeChanged(0, mPlayers.size(), diff);
        notifyItemRangeChanged(0, players.size)
    }

    fun setRequestedPhoto(imageUri: Uri) {
        if (photoRequester != null) {
            val pos = photoRequester!!.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                players[pos].player.image = imageUri.toString()
                notifyItemChanged(pos)
            }
            photoRequester = null
        }
    }

    inner class PlayerChooserViewHolder internal constructor(
        private val binding: PlayerChooserViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val mTextView get() = binding.staticText

        fun bindTo(playerInList: PlayerItemData) {
            bindThumbnail(playerInList)
            bindAutoCompleteTextView(playerInList)
            bindTextView(playerInList)
            bindPointsBtn(playerInList)
        }

        fun bindThumbnail(playerInList: PlayerItemData) {
            Glide.with(binding.root.context)
                .load(playerInList.image)
                .placeholder(R.drawable.ic_contact_picture)
                .into(binding.thumbnail)
        }

        fun bindAutoCompleteTextView(playerInList: PlayerItemData) {
            if (playerInList.isEditable) {
                binding.text.setText(playerInList.name, false)
                binding.text.visibility = View.VISIBLE
            } else {
                binding.text.visibility = View.GONE
            }
        }

        fun bindTextView(playerInList: PlayerItemData) {
            with(binding.staticText) {
                visibility = if (playerInList.isEditable) View.GONE else View.VISIBLE
                text = playerInList.name
                textSize = if (playerInList.isCurrent) {
                    MyAnimator.FONT_LARGE_SIZE
                } else {
                    MyAnimator.FONT_SMALL_SIZE
                }.toFloat()
            }
        }

        fun bindPointsBtn(playerInList: PlayerItemData) {
            if (playerInList.isEditable) {
                binding.pointsView.setBackgroundResource(R.drawable.btn_cancel)
                binding.pointsView.text = ""
            } else {
                binding.pointsView.setBackgroundResource(R.drawable.btn_uncheck)
                binding.pointsView.text = playerInList.points.toString()
            }
        }

        init {
            binding.thumbnail.setOnClickListener {
                if (binding.text.text.isNotEmpty()) {
                    if (actionListener != null) {
                        photoRequester = this@PlayerChooserViewHolder
                        actionListener.requestPhoto()
                    }
                }
            }
            binding.text.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    val pos = adapterPosition
                    if (pos == RecyclerView.NO_POSITION) return
                    players[pos].player.name = s.toString()
                }
            })
            binding.text.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@OnKeyListener false
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    players[pos].reset()
                    binding.thumbnail.setImageResource(R.drawable.ic_contact_picture)
                }
                players[pos].player.name = binding.text.text.toString()
                false
            })
            binding.text.onItemClickListener =
                OnItemClickListener { parent, view, position, _ ->
                    val pos = adapterPosition
                    if (pos == RecyclerView.NO_POSITION) return@OnItemClickListener
                    val player = parent.getItemAtPosition(position) as Player
                    players[pos].set(player, 0)
                    Glide.with(view.context)
                        .load(player.image)
                        .placeholder(R.drawable.ic_contact_picture)
                        .into(binding.thumbnail)
                    val imm = view.context
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.text.windowToken, 0)
                    val nextFocus = binding.text.focusSearch(View.FOCUS_DOWN)
                    nextFocus?.requestFocus(View.FOCUS_DOWN)
                }
            binding.text.setAdapter(
                PlayerChooserAdapter(
                    binding.text.context,
                    allPossiblePlayers
                )
            )
            binding.pointsView.setOnClickListener(View.OnClickListener {
                val pos = adapterPosition
                if (pos >= 0 && pos < players.size) {
                    val playerItemData = players[pos]
                    if (!playerItemData.isEditable) return@OnClickListener
                    if (players.size > 2) {
                        players.removeAt(pos)
                        notifyItemRemoved(pos)
                    } else {
                        playerItemData.reset()
                        notifyItemChanged(pos)
                    }
                }
            })
        }
    }

    companion object {
        private const val TAG = "PlayerChooserViewAdapte"
        private const val PAYLOAD_THUMBNAIL = "PAYLOAD_THUMBNAIL"
        private const val PAYLOAD_AUTOCOMPLETE = "PAYLOAD_AUTOCOMPLETE"
        private const val PAYLOAD_TEXT = "PAYLOAD_TEXT"
        private const val PAYLOAD_POINTS = "PAYLOAD_POINTS"
    }
}