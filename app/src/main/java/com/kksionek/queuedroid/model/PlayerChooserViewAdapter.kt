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
import com.kksionek.queuedroid.model.PlayerChooserViewAdapter.PlayerChooserViewHolder
import com.kksionek.queuedroid.view.MyAnimator
import java.util.*

class PlayerChooserViewAdapter : RecyclerView.Adapter<PlayerChooserViewHolder> {
    private val mPlayers: MutableList<PlayerItemData> = mutableListOf()
    private var mAllPossiblePlayers: List<Player?> = mutableListOf()
    private val mQueueModel: QueueModel
    private val mActionListener: ActionListener?
    private var mPhotoRequester: PlayerChooserViewHolder? = null

    constructor(actionListener: ActionListener?, queueModel: QueueModel) {
        mActionListener = actionListener
        mQueueModel = queueModel
        setHasStableIds(true)
        if (queueModel.playersCount == 0) {
            mPlayers.add(PlayerItemData())
            mPlayers.add(PlayerItemData())
        } else {
            for (i in 0 until mQueueModel.playersCount) {
                mPlayers.add(
                    PlayerItemData(
                        mQueueModel.getPlayerAt(i),
                        mQueueModel.getPointsOfPlayer(i)
                    )
                )
                mPlayers[i].isEditable = false
                mPlayers[i].isCurrent = i == mQueueModel.currentPlayerIndex
            }
        }
    }

    constructor(
        actionListener: ActionListener?,
        queueModel: QueueModel,
        items: List<Player>
    ) {
        mActionListener = actionListener
        mQueueModel = queueModel
        setHasStableIds(true)
        for (player in items) {
            mPlayers.add(PlayerItemData(player))
        }
        while (mPlayers.size < 2) {
            mPlayers.add(PlayerItemData())
        }
    }

    override fun getItemId(position: Int): Long {
        return mPlayers[position].initialPosition.toLong()
    }

    fun add(player: Player) {
        mPlayers.add(PlayerItemData(player))
        notifyItemInserted(mPlayers.size - 1)
    }

    fun addAll(adapterItems: List<Player>) {
        mPlayers.addAll(adapterItems.map { PlayerItemData(it) })
        notifyItemRangeInserted(mPlayers.size, adapterItems.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerChooserViewHolder {
        val viewItem = LayoutInflater.from(parent.context).inflate(
            R.layout.player_chooser_view,
            parent,
            false
        )
        return PlayerChooserViewHolder(viewItem)
    }

    override fun onBindViewHolder(holder: PlayerChooserViewHolder, position: Int) {
        holder.bindTo(mPlayers[position])
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
                PAYLOAD_THUMBNAIL -> holder.bindThumbnail(
                    mPlayers[position]
                )
                PAYLOAD_AUTOCOMPLETE -> holder.bindAutoCompleteTextView(
                    mPlayers[position]
                )
                PAYLOAD_TEXT -> holder.bindTextView(mPlayers[position])
                PAYLOAD_POINTS -> holder.bindPointsBtn(mPlayers[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return mPlayers.size
    }

    fun setAutocompleteItems(allPlayers: List<Player?>) {
        mAllPossiblePlayers = allPlayers
    }

    val currentPlayers: List<Player>
        get() {
            return mPlayers
                .map { it.player }
                .filter { it.name.isNotEmpty() }
        }

    fun endGame(queueModel: QueueModel) {
        val newList = mutableListOf<PlayerItemData>()
        for (i in 0 until queueModel.playersCount) {
            newList.add(
                mPlayers[i].copy(
                    points = queueModel.getPointsOfPlayer(i),
                    isCurrent = false
                )
            )
        }
        newList.sortWith { left, right ->
            Integer.valueOf(right.points).compareTo(left.points)
        }
        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(mPlayers, newList))
        mPlayers.clear()
        mPlayers.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updatePoints(previousPlayerIndex: Int, currentPlayerIndex: Int) {
        val prevPlayer = mPlayers[previousPlayerIndex]
        prevPlayer.points = mQueueModel.getPointsOfPlayer(previousPlayerIndex)
        prevPlayer.isCurrent = false
        mPlayers[currentPlayerIndex].isCurrent = true
        notifyDataSetChanged()
    }

    fun reset(hardReset: Boolean) {
        if (hardReset) {
            val size = mPlayers.size
            mPlayers.clear()
            mPlayers.add(PlayerItemData())
            mPlayers.add(PlayerItemData())
            notifyItemRangeChanged(0, 2)
            notifyItemRangeRemoved(2, size - 2)
        } else {
            val newList = mPlayers.map {
                it.copy(
                    points = 0,
                    isEditable = true,
                    isCurrent = false
                )
            }.sortedBy { it.initialPosition }
            val diffResult = DiffUtil.calculateDiff(MyDiffCallback(mPlayers, newList))
            mPlayers.clear()
            mPlayers.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun startGame() {
        mPlayers.forEach { it.isEditable = false }
        mPlayers.first().isCurrent = true
        //        Bundle diff = new Bundle();
//        diff.putBoolean(PAYLOAD_POINTS, true);
//        diff.putBoolean(PAYLOAD_TEXT, true);
//        diff.putBoolean(PAYLOAD_AUTOCOMPLETE, true);
//        notifyItemRangeChanged(0, mPlayers.size(), diff);
        notifyItemRangeChanged(0, mPlayers.size)
    }

    fun setRequestedPhoto(imageUri: Uri) {
        if (mPhotoRequester != null) {
            val pos = mPhotoRequester!!.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                mPlayers[pos].player.image = imageUri.toString()
                notifyItemChanged(pos)
            }
            mPhotoRequester = null
        }
    }

    inner class PlayerChooserViewHolder internal constructor(viewItem: View) :
        RecyclerView.ViewHolder(viewItem) {
        val mThumbnail: ImageView
        val mAutoCompleteTextView: AutoCompleteTextView
        val mTextView: TextView
        val mPointsBtn: Button

        fun bindTo(playerInList: PlayerItemData) {
            bindThumbnail(playerInList)
            bindAutoCompleteTextView(playerInList)
            bindTextView(playerInList)
            bindPointsBtn(playerInList)
        }

        fun bindThumbnail(playerInList: PlayerItemData) {
            val context = mThumbnail.context
            Glide.with(context)
                .load(playerInList.image)
                .placeholder(R.drawable.ic_contact_picture)
                .into(mThumbnail)
        }

        fun bindAutoCompleteTextView(playerInList: PlayerItemData) {
            if (playerInList.isEditable) {
                mAutoCompleteTextView.setText(
                    playerInList.name,
                    false
                )
                mAutoCompleteTextView.visibility = View.VISIBLE
            } else mAutoCompleteTextView.visibility = View.GONE
        }

        fun bindTextView(playerInList: PlayerItemData) {
            mTextView.visibility =
                if (playerInList.isEditable) View.GONE else View.VISIBLE
            mTextView.text = playerInList.name
            mTextView.textSize =
                if (playerInList.isCurrent) MyAnimator.FONT_LARGE_SIZE.toFloat() else MyAnimator.FONT_SMALL_SIZE.toFloat()
        }

        fun bindPointsBtn(playerInList: PlayerItemData) {
            if (playerInList.isEditable) {
                mPointsBtn.setBackgroundResource(R.drawable.btn_cancel)
                mPointsBtn.text = ""
            } else {
                mPointsBtn.setBackgroundResource(R.drawable.btn_uncheck)
                mPointsBtn.text = playerInList.points.toString()
            }
        }

        init {
            mThumbnail = viewItem.findViewById<View>(R.id.thumbnail) as ImageView
            mAutoCompleteTextView = viewItem.findViewById<View>(R.id.text) as AutoCompleteTextView
            mTextView = viewItem.findViewById<View>(R.id.staticText) as TextView
            mPointsBtn = viewItem.findViewById<View>(R.id.pointsView) as Button
            mThumbnail.setOnClickListener {
                if (mAutoCompleteTextView.text.length > 0) {
                    if (mActionListener != null) {
                        mPhotoRequester = this@PlayerChooserViewHolder
                        mActionListener.requestPhoto()
                    }
                }
            }
            mAutoCompleteTextView.addTextChangedListener(object : TextWatcher {
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
                    mPlayers[pos].player.name = s.toString()
                }
            })
            mAutoCompleteTextView.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@OnKeyListener false
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    mPlayers[pos].reset()
                    mThumbnail.setImageResource(R.drawable.ic_contact_picture)
                }
                mPlayers[pos].player.name = mAutoCompleteTextView.text.toString()
                false
            })
            mAutoCompleteTextView.onItemClickListener =
                OnItemClickListener { parent, view, position, _ ->
                    val pos = adapterPosition
                    if (pos == RecyclerView.NO_POSITION) return@OnItemClickListener
                    val player = parent.getItemAtPosition(position) as Player
                    mPlayers[pos].set(player, 0)
                    Glide.with(view.context)
                        .load(player.image)
                        .placeholder(R.drawable.ic_contact_picture)
                        .into(mThumbnail)
                    val imm = view.context
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(mAutoCompleteTextView.windowToken, 0)
                    val nextFocus = mAutoCompleteTextView.focusSearch(View.FOCUS_DOWN)
                    nextFocus?.requestFocus(View.FOCUS_DOWN)
                }
            mAutoCompleteTextView.setAdapter(
                PlayerChooserAdapter(
                    mAutoCompleteTextView.context,
                    mAllPossiblePlayers
                )
            )
            mPointsBtn.setOnClickListener(View.OnClickListener {
                val pos = adapterPosition
                if (pos >= 0 && pos < mPlayers.size) {
                    val playerItemData = mPlayers[pos]
                    if (!playerItemData.isEditable) return@OnClickListener
                    if (mPlayers.size > 2) {
                        mPlayers.removeAt(pos)
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