package com.kksionek.queuedroid.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.data.Player
import com.kksionek.queuedroid.model.*
import com.kksionek.queuedroid.model.FbController.FacebookLoginListener
import com.kksionek.queuedroid.view.PointsDialogFragment.PointsDialogListener
import com.kksionek.queuedroid.view.keyboard.KeyboardView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity(), PointsDialogListener, ActionListener {
    private var mItemsContainer: LinearLayout? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAddPlayerBtn: Button? = null
    private var mKeyboardView: KeyboardView? = null
    private var mFirstBtn: Button? = null
    private var mSecondBtn: Button? = null
    private var mThirdBtn: Button? = null
    private var mAdView: AdView? = null
    private val mOnStartGameBtnClicked: View.OnClickListener = OnStartGameBtnClicked()
    private val mOnSettingsBtnClicked = View.OnClickListener {
        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(intent)
    }
    private val mOnEndGameBtnClicked: View.OnClickListener = OnEndGameBtnClicked()
    private val mOnNextTurnBtnClicked: View.OnClickListener = OnNextTurnBtnClicked()
    private lateinit var mQueueModel: QueueModel
    private val mBackCounter = AtomicInteger(0)
    private val mAllPlayers: MutableList<Player> = mutableListOf()
    private var mPlayerChooserViewAdapter: PlayerChooserViewAdapter? = null
    private var mRequestedPhotoURI: Uri? = null
    private var mInGame = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rootView = findViewById<View>(R.id.root) as ViewGroup
        mItemsContainer = findViewById<View>(R.id.activity_main_items_container) as LinearLayout
        mAddPlayerBtn = findViewById<View>(R.id.activity_main_button_add_player) as Button
        mFirstBtn = findViewById<View>(R.id.first_btn) as Button
        mSecondBtn = findViewById<View>(R.id.second_btn) as Button
        mThirdBtn = findViewById<View>(R.id.third_btn) as Button
        mRecyclerView = findViewById<View>(R.id.activity_main_recyclerview) as RecyclerView
        mKeyboardView = findViewById<View>(R.id.keyboard_view) as KeyboardView
        mAdView = findViewById<View>(R.id.ad_view) as AdView
        mAddPlayerBtn!!.setOnClickListener { mPlayerChooserViewAdapter!!.add(Player()) }
        mFirstBtn!!.setText(R.string.activity_main_button_play)
        mFirstBtn!!.setOnClickListener(mOnStartGameBtnClicked)
        mSecondBtn!!.setText(R.string.activity_main_button_settings)
        mSecondBtn!!.setOnClickListener(mOnSettingsBtnClicked)
        mThirdBtn!!.setText(R.string.activity_main_button_share)
        mThirdBtn!!.setOnClickListener {
            if (Settings.isFacebookEnabled(baseContext)
                && FbController.isInitilized
                && FbController.isLogged
            ) {
                FbController.shareOnFacebook(
                    this@MainActivity,
                    mQueueModel.fbPlayers,
                    rankBitmap
                )
            } else {
                try {
                    val file = File(cacheDir, "SHARE.png")
                    val fOut = FileOutputStream(file)
                    rankBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                    fOut.flush()
                    fOut.close()
                    file.setReadable(true, false)
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                    intent.type = "image/png"
                    startActivity(Intent.createChooser(intent, ""))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        mAdView!!.visibility = View.GONE
        val adRequest = AdRequest.Builder()
            .addTestDevice(getString(R.string.adMobTestDeviceS5))
            .addTestDevice(getString(R.string.adMobTestDeviceS7))
            .build()
        mAdView!!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                TransitionManager.beginDelayedTransition(rootView)
                mAdView!!.visibility = View.VISIBLE
            }
        }
        mAdView!!.loadAd(adRequest)
        autocompleteData
        mQueueModel = QueueModel(savedInstanceState)
        if (savedInstanceState == null) {
            mPlayerChooserViewAdapter = PlayerChooserViewAdapter(this, mQueueModel)
        } else {
            mInGame = savedInstanceState.getBoolean(SIS_IN_GAME)
            if (mInGame) {
                mPlayerChooserViewAdapter = PlayerChooserViewAdapter(this, mQueueModel)
                setupInGameView()
            } else {
                val adapterItems: ArrayList<Player> =
                    savedInstanceState.getParcelableArrayList<Player>(
                        SIS_ADAPTER_ITEMS
                    ) as ArrayList<Player>
                mPlayerChooserViewAdapter =
                    PlayerChooserViewAdapter(this, mQueueModel, adapterItems)
            }
        }
        mRecyclerView!!.layoutManager = LinearLayoutManager(
            baseContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        mPlayerChooserViewAdapter!!.setAutocompleteItems(mAllPlayers)
        val itemAnimator: DefaultItemAnimator = MyAnimator()
        mRecyclerView!!.itemAnimator = itemAnimator
        mRecyclerView!!.adapter = mPlayerChooserViewAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SIS_IN_GAME, mInGame)
        if (mInGame) {
            mQueueModel.saveInstanceState(outState)
        } else {
            outState.putParcelableArrayList(
                SIS_ADAPTER_ITEMS,
                arrayListOf(*mPlayerChooserViewAdapter!!.currentPlayers.toTypedArray())
            )
        }
    }

    //TODO: Load contacts after change in settings
    private val autocompleteData: Unit
        get() {
            //TODO: Load contacts after change in settings
            if (Settings.isContactsEnabled(this)) {
                ContactsController.loadContacts(this, mAllPlayers)
            }
            if (Settings.isFacebookEnabled(this) && FbController.isInitilized) {
                if (FbController.isLogged) {
                    FbController.instance.getFriendData(mAllPlayers)
                    mPlayerChooserViewAdapter!!.setAutocompleteItems(mAllPlayers)
                } else {
                    FbController.instance.logIn(this, object : FacebookLoginListener {
                        override fun onLogged() {
                            FbController.instance.getFriendData(mAllPlayers)
                            mPlayerChooserViewAdapter!!.setAutocompleteItems(mAllPlayers)
                        }

                        override fun onCancel() {}
                        override fun onError() {}
                    })
                }
            }
        }

    override fun onResume() {
        super.onResume()
        if (mKeyboardView != null) mKeyboardView!!.setColumnCount(
            Settings.getKeyboardColumnsCount(
                this
            )
        )
        if (mAdView != null) mAdView!!.resume()
    }

    override fun onPause() {
        if (mAdView != null) mAdView!!.pause()
        super.onPause()
    }

    override fun onBackPressed() {
        if (mBackCounter.incrementAndGet() < 2) {
            Toast.makeText(baseContext, R.string.activity_main_on_back_pressed, Toast.LENGTH_SHORT)
                .show()
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    mBackCounter.set(0)
                }
            }, 2000)
        } else super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                setImageData(mRequestedPhotoURI!!)
            } else mRequestedPhotoURI = null
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
        FbController.instance.onActivityResult(requestCode, resultCode, data)
    }

    private fun setImageData(data: Uri) {
        Log.d(TAG, "setImageData: $data")
        mPlayerChooserViewAdapter!!.setRequestedPhoto(data)
        mRequestedPhotoURI = null
    }

    override fun onDialogPositiveClick(points: Int) {
        assignPointsAndNextTurn(points)
    }

    private fun requestPoints() {
        val dialog = PointsDialogFragment()
        dialog.show(supportFragmentManager, "PointsDialogFragment")
    }

    override fun requestPhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra("aspectX", 1)
        takePictureIntent.putExtra("aspectY", 1)
        takePictureIntent.putExtra("outputX", 300)
        takePictureIntent.putExtra("outputY", 300)
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (photoFile != null) {
                mRequestedPhotoURI = FileProvider.getUriForFile(
                    this,
                    "com.kksionek.queuedroid",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mRequestedPhotoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "PNG_" + timeStamp + "_"
        val storageDir = cacheDir
        //TODO: Clean the cache
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".png",  /* suffix */
            storageDir /* directory */
        )
    }

    private fun assignPointsAndNextTurn(points: Int) {
        mQueueModel.nextTurn(points)
        mKeyboardView!!.clearPoints()
        val currentPlayerIndex = mQueueModel.currentPlayerIndex
        mPlayerChooserViewAdapter!!.updatePoints(
            mQueueModel.previousPlayerIndex,
            currentPlayerIndex
        )
        if (currentPlayerIndex == 0) mRecyclerView!!.smoothScrollToPosition(currentPlayerIndex) else mRecyclerView!!.scrollToPosition(
            currentPlayerIndex
        )
    }

    private val rankBitmap: Bitmap
        get() {
            val bitmap = Bitmap.createBitmap(
                mItemsContainer!!.measuredWidth,
                mItemsContainer!!.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            mItemsContainer!!.draw(canvas)
            return bitmap
        }

    private inner class OnStartGameBtnClicked : View.OnClickListener {
        override fun onClick(v: View) {
            val currentPlayers = mPlayerChooserViewAdapter!!.currentPlayers
            if (currentPlayers.size >= 2) {
                mInGame = true
                val currentFocus = currentFocus
                currentFocus?.clearFocus()
                mRecyclerView!!.smoothScrollToPosition(0)
                mPlayerChooserViewAdapter!!.startGame()
                mQueueModel.newGame(currentPlayers)
                setupInGameView()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    R.string.activity_main_start_wrong_player_on_list_error_message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupInGameView() {
        mAddPlayerBtn!!.visibility = View.GONE
        mKeyboardView!!.visibility =
            if (Settings.shouldUseInAppKeyboard(this@MainActivity)) View.VISIBLE else View.GONE
        mKeyboardView!!.clearPoints()
        mRecyclerView!!.keepScreenOn =
            Settings.isKeepOnScreen(this@MainActivity)
        mFirstBtn!!.setText(R.string.activity_main_button_next_turn)
        mFirstBtn!!.setOnClickListener(mOnNextTurnBtnClicked)
        mSecondBtn!!.setText(R.string.activity_main_button_end_game)
        mSecondBtn!!.setOnClickListener(mOnEndGameBtnClicked)
    }

    private fun setupResultView() {
        mKeyboardView!!.visibility = View.GONE
        mAddPlayerBtn!!.visibility = View.GONE
        mFirstBtn!!.setText(R.string.activity_main_button_new_game)
        mFirstBtn!!.setOnClickListener(OnRestartGameClicked(true))
        mSecondBtn!!.setText(R.string.activity_main_button_play_again)
        mSecondBtn!!.setOnClickListener(OnRestartGameClicked(false))
        mThirdBtn!!.setText(R.string.activity_main_button_share)
        mThirdBtn!!.visibility = View.VISIBLE
    }

    private fun setupStartView() {
        mKeyboardView!!.visibility = View.GONE
        mAddPlayerBtn!!.visibility = View.VISIBLE
        mFirstBtn!!.setText(R.string.activity_main_button_play)
        mFirstBtn!!.setOnClickListener(mOnStartGameBtnClicked)
        mSecondBtn!!.setText(R.string.activity_main_button_settings)
        mSecondBtn!!.setOnClickListener(mOnSettingsBtnClicked)
        mThirdBtn!!.visibility = View.GONE
    }

    private inner class OnEndGameBtnClicked : View.OnClickListener {
        override fun onClick(view: View) {
            mInGame = false
            mPlayerChooserViewAdapter!!.endGame(mQueueModel)
            mQueueModel.resetScoreboard()
            setupResultView()
        }
    }

    private inner class OnRestartGameClicked(private val mHardReset: Boolean) :
        View.OnClickListener {
        override fun onClick(v: View) {
            mPlayerChooserViewAdapter!!.reset(mHardReset)
            setupStartView()
        }
    }

    private inner class OnNextTurnBtnClicked : View.OnClickListener {
        override fun onClick(v: View) {
            if (Settings.shouldUseInAppKeyboard(this@MainActivity)) {
                val pointsCollected = mKeyboardView!!.points
                if (pointsCollected == 0
                    && Settings.isShowNoPointsConfirmationDialog(this@MainActivity)
                ) {
                    val dialog = CheckboxAlertDialog()
                    dialog.show(
                        this@MainActivity,
                        R.string.checkbox_alert_dialog_title,
                        R.string.checkbox_alert_dialog_message,
                        object : CheckboxAlertDialog.OnDialogClosedListener {
                            override fun onDialogClosed(result: Boolean) {
                                if (result) assignPointsAndNextTurn(0)
                            }
                        })
                    return
                }
                assignPointsAndNextTurn(pointsCollected)
            } else {
                requestPoints()
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_IMAGE_CAPTURE = 9876

        const val PERMISSIONS_REQUEST_READ_CONTACTS = 2233
        const val SIS_IN_GAME = "IN_GAME"
        const val SIS_ADAPTER_ITEMS = "ADAPTER_ITEMS"
    }
}