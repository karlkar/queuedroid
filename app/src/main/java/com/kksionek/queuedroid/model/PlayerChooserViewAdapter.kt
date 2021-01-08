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

class PlayerChooserViewAdapter(
    private val actionListener: ActionListener
) : RecyclerView.Adapter<PlayerChooserViewHolder>() {

    private val players: MutableList<PlayerItemData> = mutableListOf()
    private var allPossiblePlayers: List<Player> = emptyList()

    private var photoRequester: PlayerChooserViewHolder? = null

    // TODO: should be private?
    var isEditable: Boolean = true

    init {
        setHasStableIds(true)
        players.addAll(listOf(PlayerItemData(), PlayerItemData()))
    }

    override fun getItemId(position: Int): Long =
        players[position].initialPosition.toLong()

    fun add(player: Player) {
        players.add(PlayerItemData(player))
        notifyItemInserted(players.size - 1)
    }

    fun addAll(adapterItems: List<Player>) {
        val startPosition = players.size
        players.addAll(adapterItems.map { PlayerItemData(it) })
        notifyItemRangeInserted(startPosition, adapterItems.size)
    }

    val currentPlayers =
        players.map { it.player }
            .filter { it.name.isNotEmpty() }

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
        val payload = payloads.first() as Bundle
        for (key in payload.keySet()) {
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

    fun endGame(state: Map<Player, Int>) {
        isEditable = false

        val newList = players.map {
            it.copy(
                points = state[it] ?: 0,
                isCurrent = false
            )
        }.sortedBy { it.points }

        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(players, newList))
        with(players) {
            clear()
            addAll(newList)
        }
        diffResult.dispatchUpdatesTo(this)
    }

    fun setState(currentPlayerIndex: Int, state: Map<Player, Int>) {
        players.forEachIndexed { it, player ->
            with(player) {
                points = state[player] ?: 0
                isCurrent = it == currentPlayerIndex
            }
        }
        notifyDataSetChanged()
    }

    fun reset(hardReset: Boolean) {
        isEditable = true
        if (hardReset) {
            val size = players.size
            with(players) {
                clear()
                addAll(listOf(PlayerItemData(), PlayerItemData()))
            }
            notifyItemRangeChanged(0, 2)
            notifyItemRangeRemoved(2, size - 2)
        } else {
            val newList = players.map {
                it.copy(
                    points = 0,
                    isCurrent = false
                )
            }.sortedBy { it.initialPosition }
            val diffResult = DiffUtil.calculateDiff(MyDiffCallback(players, newList))
            with(players) {
                clear()
                addAll(newList)
            }
            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun startGame() {
        isEditable = false
        players.first().isCurrent = true
        notifyItemRangeChanged(0, players.size)
    }

    fun setRequestedPhoto(imageUri: Uri) {
        photoRequester?.let {
            val pos = it.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val playerInfoData = players[pos]
                val newPlayerData = playerInfoData.player.copy(image = imageUri.toString())
                playerInfoData.player = newPlayerData
                notifyItemChanged(pos)
            }
        }
        photoRequester = null
    }

    inner class PlayerChooserViewHolder internal constructor(
        private val binding: PlayerChooserViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val textView get() = binding.text

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
                .error(R.drawable.ic_contact_picture)
                .into(binding.thumbnail)
        }

        fun bindAutoCompleteTextView(playerInList: PlayerItemData) {
            with(binding.text) {
                if (isEditable) {
                    visibility = View.VISIBLE
                    setText(playerInList.name, false)
                } else {
                    visibility = View.GONE
                }
            }
        }

        fun bindTextView(playerInList: PlayerItemData) {
            with(binding.staticText) {
                visibility = if (isEditable) View.GONE else View.VISIBLE
                text = playerInList.name
                textSize = if (playerInList.isCurrent) {
                    MyAnimator.FONT_LARGE_SIZE
                } else {
                    MyAnimator.FONT_SMALL_SIZE
                }.toFloat()
            }
        }

        fun bindPointsBtn(playerInList: PlayerItemData) {
            with(binding.pointsView) {
                if (isEditable) {
                    text = ""
                    setBackgroundResource(R.drawable.btn_cancel)
                } else {
                    text = playerInList.points.toString()
                    setBackgroundResource(R.drawable.btn_uncheck)
                }
            }
        }

        init {
            binding.root.setOnClickListener {
                Toast.makeText(binding.root.context, "Root", Toast.LENGTH_SHORT).show()
            }
            binding.thumbnail.setOnClickListener {
                if (binding.text.text.isNotEmpty()) {
                    photoRequester = this@PlayerChooserViewHolder
                    actionListener.requestPhoto()
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
                    val playerItemData = players[pos]
                    val newPlayerData = playerItemData.player.copy(name = s.toString())
                    playerItemData.player = newPlayerData
                }
            })
            binding.text.setOnKeyListener { _, keyCode, _ ->
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnKeyListener false
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    players[pos].reset()
                    binding.thumbnail.setImageResource(R.drawable.ic_contact_picture)
                }
                val playerItemData = players[pos]
                val newPlayerData = playerItemData.player.copy(name = binding.text.text.toString())
                playerItemData.player = newPlayerData
                false
            }
            binding.text.setOnItemClickListener { parent, view, position, _ ->
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnItemClickListener
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
                PlayerAutocompleteAdapter(
                    binding.text.context,
                    allPossiblePlayers
                )
            )
            binding.pointsView.setOnClickListener {
                if (!isEditable) return@setOnClickListener
                val pos = adapterPosition
                if (pos >= 0 && pos < players.size) {
                    val playerItemData = players[pos]
                    if (players.size > 2) {
                        players.removeAt(pos)
                        notifyItemRemoved(pos)
                    } else {
                        playerItemData.reset()
                        notifyItemChanged(pos)
                    }
                }
            }
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