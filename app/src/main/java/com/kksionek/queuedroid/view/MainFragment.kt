package com.kksionek.queuedroid.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.data.Player
import com.kksionek.queuedroid.databinding.FragmentMainBinding
import com.kksionek.queuedroid.model.*
import com.kksionek.queuedroid.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@AndroidEntryPoint
class MainFragment : Fragment(), PointsDialogFragment.PointsDialogListener, ActionListener {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private val playerChooserViewAdapter = PlayerChooserViewAdapter(this)
    private var requestedPhotoURI: Uri? = null

    private val rankBitmap: Bitmap
        get() {
            val bitmap = Bitmap.createBitmap(
                binding.playerContainer.measuredWidth,
                binding.playerContainer.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            binding.playerContainer.draw(canvas)
            return bitmap
        }

    private val backCounter = AtomicInteger(0)
    private val handler = Handler()

    private val takePicture = registerForActivityResult(TakeSmallPictureContract()) { result ->
        if (result) {
            requestedPhotoURI?.let { setImageData(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (backCounter.getAndIncrement() < 1) {
                Toast.makeText(
                    requireContext(),
                    R.string.activity_main_on_back_pressed,
                    Toast.LENGTH_SHORT
                ).show()
                handler.postDelayed(2000) {
                    backCounter.set(0)
                }
            } else {
                requireActivity().finish()
            }
        }

        viewModel.getAutocompleteData(this)
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
            playerChooserViewAdapter.add(Player())
        }
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                GameState.Ready -> setupGameReady()
                is GameState.InGame -> setupGameInGame(it.currentPlayer, it.state)
                is GameState.Finished -> setupGameFinished(it.state)
            }
        }
        viewModel.allPlayers.observe(viewLifecycleOwner) {
            playerChooserViewAdapter.setAutocompleteItems(it)
        }
        viewModel.clearKeyboard.observe(viewLifecycleOwner) {
            binding.keyboardView.clearPoints()
        }
        viewModel.showStartGameError.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                R.string.activity_main_start_wrong_player_on_list_error_message,
                Toast.LENGTH_LONG
            ).show()
        }

        setupAdView()

//        queueModel = QueueModel(savedInstanceState)
//        if (savedInstanceState == null) {
//            playerChooserViewAdapter = PlayerChooserViewAdapter(this)
//        } else {
//            inGame = savedInstanceState.getBoolean(SIS_IN_GAME)
//            if (inGame) {
//                playerChooserViewAdapter = PlayerChooserViewAdapter(this, queueModel)
//                setupInGameView()
//            } else {
//                val adapterItems: ArrayList<Player> =
//                    savedInstanceState.getParcelableArrayList<Player>(
//                        SIS_ADAPTER_ITEMS
//                    ) as ArrayList<Player>
//                playerChooserViewAdapter =
//                    PlayerChooserViewAdapter(this, queueModel, adapterItems)
//            }
//        }

        with(binding.activityMainRecyclerview) {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            itemAnimator = MyAnimator()
            adapter = playerChooserViewAdapter
        }
    }

    private fun setupGameReady() {
        playerChooserViewAdapter.isEditable = true
        with(binding) {
            keyboardView.visibility = View.GONE
            activityMainButtonAddPlayer.visibility = View.VISIBLE
        }
        with(binding.firstBtn) {
            setText(R.string.activity_main_button_play)
            setOnClickListener { viewModel.startGame(playerChooserViewAdapter.currentPlayers) }
        }
        with(binding.secondBtn) {
            setText(R.string.activity_main_button_settings)
            setOnClickListener {
                findNavController().navigate(R.id.settingsFragment)
            }
        }
        binding.thirdBtn.visibility = View.GONE
    }

    private fun setupGameInGame(currentPlayer: Int, state: Map<Player, Int>) {
        val currentFocus = requireActivity().currentFocus
        currentFocus?.clearFocus()

        binding.activityMainRecyclerview.smoothScrollToPosition(currentPlayer)
        playerChooserViewAdapter.setState(currentPlayer, state)

        with(binding.keyboardView) {
            visibility = if (viewModel.shouldUseInAppKeyboard()) View.VISIBLE else View.GONE
            clearPoints()
        }
        with(binding.activityMainButtonAddPlayer) {
            visibility = View.GONE
            keepScreenOn = viewModel.isKeepOnScreen()
        }
        with(binding.firstBtn) {
            setText(R.string.activity_main_button_next_turn)
            setOnClickListener {
                if (viewModel.shouldUseInAppKeyboard()) {
                    val pointsCollected = binding.keyboardView.points
                    if (viewModel.shouldShowNoPointsConfirmation(pointsCollected)) {
                        CheckboxAlertDialog.show(
                            requireContext(),
                            R.string.checkbox_alert_dialog_title,
                            R.string.checkbox_alert_dialog_message,
                        ) { accepted, dontShowAgain ->
                            if (accepted) {
                                viewModel.nextTurn(0)
                            }
                            viewModel.setShowNoPointsConfirmationDialog(dontShowAgain)
                        }
                        return@setOnClickListener
                    }
                    viewModel.nextTurn(pointsCollected)
                } else {
                    requestPoints()
                }
            }
        }
        with(binding.secondBtn) {
            setText(R.string.activity_main_button_end_game)
            setOnClickListener { viewModel.endGame() }
        }
        binding.thirdBtn.visibility = View.GONE
    }

    private fun setupGameFinished(state: Map<Player, Int>) {
        playerChooserViewAdapter.endGame(state)
        with(binding) {
            keyboardView.visibility = View.GONE
            activityMainButtonAddPlayer.visibility = View.GONE
        }
        with(binding.firstBtn) {
            setText(R.string.activity_main_button_new_game)
            setOnClickListener {
                playerChooserViewAdapter.reset(true)
                viewModel.newGameClicked()
            }
            with(binding.secondBtn) {
                setText(R.string.activity_main_button_play_again)
                setOnClickListener {
                    playerChooserViewAdapter.reset(false)
                    viewModel.playAgainClicked()
                }
            }
            with(binding.thirdBtn) {
                setText(R.string.activity_main_button_share)
                setOnClickListener {
                    viewModel.shareClicked(this@MainFragment, rankBitmap)
                }
                visibility = View.VISIBLE
            }
        }
    }

    private fun setupAdView() {
        val adRequest = AdRequest.Builder().build()
        with(binding.adView) {
            visibility = View.GONE
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
                    visibility = View.VISIBLE
                }
            }
            loadAd(adRequest)
        }
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            keyboardView.setColumnCount(viewModel.getKeyboardColumnsCount())
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

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putBoolean(SIS_IN_GAME, inGame)
//        if (inGame) {
//            queueModel.saveInstanceState(outState)
//        } else {
//            outState.putParcelableArrayList(
//                SIS_ADAPTER_ITEMS,
//                arrayListOf(*playerChooserViewAdapter!!.currentPlayers.toTypedArray())
//            )
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    private fun setImageData(data: Uri) {
        Log.d(TAG, "setImageData: $data")
        playerChooserViewAdapter.setRequestedPhoto(data)
        requestedPhotoURI = null
    }

    override fun onDialogPositiveClick(points: Int) {
        viewModel.nextTurn(points)
    }

    private fun requestPoints() {
        PointsDialogFragment()
            .show(parentFragmentManager, "PointsDialogFragment")
    }

    override fun requestPhoto() {
        val photoFile = createImageFile()
        requestedPhotoURI = FileProvider.getUriForFile(
            requireContext(),
            "com.kksionek.queuedroid",
            photoFile
        ).also {
            takePicture.launch(it)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "PNG_" + timeStamp + "_"
        val storageDir = requireContext().cacheDir
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".png",  /* suffix */
            storageDir /* directory */
        )
    }

    companion object {
        private const val TAG = "MainFragment"
    }
}