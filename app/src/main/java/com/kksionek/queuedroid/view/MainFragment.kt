package com.kksionek.queuedroid.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.data.Player
import com.kksionek.queuedroid.databinding.FragmentMainBinding
import com.kksionek.queuedroid.model.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(), PointsDialogFragment.PointsDialogListener, ActionListener {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var settingsProviderImpl: SettingsProviderImpl

    private var mItemsContainer: LinearLayout? = null

    private val mOnStartGameBtnClicked: View.OnClickListener = OnStartGameBtnClicked()
    private val mOnEndGameBtnClicked: View.OnClickListener = OnEndGameBtnClicked()
    private val mOnNextTurnBtnClicked: View.OnClickListener = OnNextTurnBtnClicked()
    private val mOnSettingsBtnClicked = View.OnClickListener {
        findNavController().navigate(R.id.settingsFragment)
    }

    private lateinit var mQueueModel: QueueModel
    private val mAllPlayers: MutableList<Player> = mutableListOf()
    private var mPlayerChooserViewAdapter: PlayerChooserViewAdapter? = null
    private var mRequestedPhotoURI: Uri? = null
    private var mInGame = false

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

    private val mBackCounter = AtomicInteger(0)
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (mBackCounter.getAndIncrement() < 1) {
                Toast.makeText(
                    requireContext(),
                    R.string.activity_main_on_back_pressed,
                    Toast.LENGTH_SHORT
                )
                    .show()
                handler.postDelayed(2000) {
                    mBackCounter.set(0)
                }
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentMainBinding.inflate(inflater, container, false).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.activityMainButtonAddPlayer.setOnClickListener {
            mPlayerChooserViewAdapter!!.add(
                Player()
            )
        }
        with(binding) {
            firstBtn.setText(R.string.activity_main_button_play)
            firstBtn.setOnClickListener(mOnStartGameBtnClicked)
            secondBtn.setText(R.string.activity_main_button_settings)
            secondBtn.setOnClickListener(mOnSettingsBtnClicked)
            thirdBtn.setText(R.string.activity_main_button_share)
            thirdBtn.setOnClickListener {
                if (settingsProviderImpl.isFacebookEnabled()
                    && FbController.isInitilized
                    && FbController.isLogged
                ) {
                    FbController.shareOnFacebook(
                        requireActivity(),
                        mQueueModel.fbPlayers,
                        rankBitmap
                    )
                } else {
                    try {
                        val file = File(requireContext().cacheDir, "SHARE.png")
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
        }

        setupAdView()

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
        mPlayerChooserViewAdapter!!.setAutocompleteItems(mAllPlayers)

        with(binding.activityMainRecyclerview) {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            itemAnimator = MyAnimator()
            adapter = mPlayerChooserViewAdapter
        }
    }

    private fun setupAdView() {
        binding.adView.visibility = View.GONE
        val adRequest = AdRequest.Builder()
            .addTestDevice(getString(R.string.adMobTestDeviceS5))
            .addTestDevice(getString(R.string.adMobTestDeviceS7))
            .build()
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
                binding.adView.visibility = View.VISIBLE
            }
        }
        binding.adView.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            keyboardView.setColumnCount(settingsProviderImpl.getKeyboardColumnsCount())
            adView.resume()
        }
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
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
            if (settingsProviderImpl.isContactsEnabled()) {
                ContactsController.loadContacts(requireContext(), mAllPlayers)
            }
            if (settingsProviderImpl.isFacebookEnabled() && FbController.isInitilized) {
                if (FbController.isLogged) {
                    FbController.instance.getFriendData(mAllPlayers)
                    mPlayerChooserViewAdapter!!.setAutocompleteItems(mAllPlayers)
                } else {
                    FbController.instance.logIn(
                        requireActivity(),
                        object : FbController.FacebookLoginListener {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                setImageData(mRequestedPhotoURI!!)
            } else {
                mRequestedPhotoURI = null
            }
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
        dialog.show(parentFragmentManager, "PointsDialogFragment")
    }

    override fun requestPhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra("aspectX", 1)
        takePictureIntent.putExtra("aspectY", 1)
        takePictureIntent.putExtra("outputX", 300)
        takePictureIntent.putExtra("outputY", 300)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            val photoFile = try {
                createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
            photoFile?.let {
                mRequestedPhotoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "com.kksionek.queuedroid",
                    it
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
        val storageDir = requireContext().cacheDir
        //TODO: Clean the cache
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".png",  /* suffix */
            storageDir /* directory */
        )
    }

    private fun assignPointsAndNextTurn(points: Int) {
        mQueueModel.nextTurn(points)
        binding.keyboardView.clearPoints()
        val currentPlayerIndex = mQueueModel.currentPlayerIndex
        mPlayerChooserViewAdapter!!.updatePoints(
            mQueueModel.previousPlayerIndex,
            currentPlayerIndex
        )
        if (currentPlayerIndex == 0) {
            binding.activityMainRecyclerview.smoothScrollToPosition(currentPlayerIndex)
        } else {
            binding.activityMainRecyclerview.scrollToPosition(currentPlayerIndex)
        }
    }

    private inner class OnStartGameBtnClicked : View.OnClickListener {
        override fun onClick(v: View) {
            val currentPlayers = mPlayerChooserViewAdapter!!.currentPlayers
            if (currentPlayers.size >= 2) {
                mInGame = true
                val currentFocus = requireActivity().currentFocus
                currentFocus?.clearFocus()
                binding.activityMainRecyclerview.smoothScrollToPosition(0)
                mPlayerChooserViewAdapter!!.startGame()
                mQueueModel.newGame(currentPlayers)
                setupInGameView()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.activity_main_start_wrong_player_on_list_error_message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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
            if (settingsProviderImpl.shouldUseInAppKeyboard()) {
                val pointsCollected = binding.keyboardView.points
                if (pointsCollected == 0
                    && settingsProviderImpl.isShowNoPointsConfirmationDialog()
                ) {
                    val dialog = CheckboxAlertDialog()
                    dialog.show(
                        requireContext(),
                        R.string.checkbox_alert_dialog_title,
                        R.string.checkbox_alert_dialog_message,
                        settingsProviderImpl,
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

    private fun setupInGameView() {
        with(binding) {
            activityMainButtonAddPlayer.visibility = View.GONE
            keyboardView.visibility =
                if (settingsProviderImpl.shouldUseInAppKeyboard()) View.VISIBLE else View.GONE
            keyboardView.clearPoints()
            activityMainRecyclerview.keepScreenOn = settingsProviderImpl.isKeepOnScreen()
            firstBtn.setText(R.string.activity_main_button_next_turn)
            firstBtn.setOnClickListener(mOnNextTurnBtnClicked)
            secondBtn.setText(R.string.activity_main_button_end_game)
            secondBtn.setOnClickListener(mOnEndGameBtnClicked)
        }
    }

    private fun setupResultView() {
        with(binding) {
            keyboardView.visibility = View.GONE
            activityMainButtonAddPlayer.visibility = View.GONE
            firstBtn.setText(R.string.activity_main_button_new_game)
            firstBtn.setOnClickListener(OnRestartGameClicked(true))
            secondBtn.setText(R.string.activity_main_button_play_again)
            secondBtn.setOnClickListener(OnRestartGameClicked(false))
            thirdBtn.setText(R.string.activity_main_button_share)
            thirdBtn.visibility = View.VISIBLE
        }
    }

    private fun setupStartView() {
        with(binding) {
            keyboardView.visibility = View.GONE
            activityMainButtonAddPlayer.visibility = View.VISIBLE
            firstBtn.setText(R.string.activity_main_button_play)
            firstBtn.setOnClickListener(mOnStartGameBtnClicked)
            secondBtn.setText(R.string.activity_main_button_settings)
            secondBtn.setOnClickListener(mOnSettingsBtnClicked)
            thirdBtn.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "MainFragment"
        private const val REQUEST_IMAGE_CAPTURE = 9876

        const val PERMISSIONS_REQUEST_READ_CONTACTS = 2233
        const val SIS_IN_GAME = "IN_GAME"
        const val SIS_ADAPTER_ITEMS = "ADAPTER_ITEMS"
    }
}