package com.kksionek.queuedroid.viewmodel

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kksionek.queuedroid.SingleLiveEvent
import com.kksionek.queuedroid.data.Player
import com.kksionek.queuedroid.model.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.io.File
import java.io.FileOutputStream

class MainViewModel @ViewModelInject constructor(
    private val settingsProviderImpl: SettingsProviderImpl,
    private val fbController: FbController,
    private val contactsController: ContactsController
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val disposables = CompositeDisposable()

    private val _allPlayers = MutableLiveData<List<Player>>()
    val allPlayers: LiveData<List<Player>> = _allPlayers

    private val queueModel = QueueModel()
    private var inGame = false

    private val _clearKeyboard = SingleLiveEvent<Unit>()
    val clearKeyboard: LiveData<Unit?> = _clearKeyboard

    private val _showStartGameError = SingleLiveEvent<Unit>()
    val showStartGameError: LiveData<Unit?> = _showStartGameError

    private val _state = MutableLiveData<GameState>().apply { value = GameState.Ready }
    val state: LiveData<GameState> = _state

    fun newGameClicked() {
        _state.value = GameState.Ready
    }

    fun playAgainClicked() {
        _state.value = GameState.Ready
    }
    @SuppressLint("SetWorldReadable")
    fun shareClicked(fragment: Fragment, rankBitmap: Bitmap) {
        if (settingsProviderImpl.isFacebookEnabled()
            && fbController.isInitialized
            && fbController.isLogged
        ) {
            FbController.shareOnFacebook(
                fragment,
                queueModel.fbPlayers,
                rankBitmap
            )
        } else {
            try {
                val file = File(fragment.requireContext().cacheDir, "SHARE.png")
                val fOut = FileOutputStream(file)
                rankBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                fOut.flush()
                fOut.close()
                file.setReadable(true, false)
                Intent(Intent.ACTION_SEND).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                    type = "image/png"
                }.also {
                    startActivity(fragment.requireContext(), Intent.createChooser(it, ""), null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error when trying to share image", e)
            }
        }
    }

    fun startGame(playerList: List<Player>) {
        if (playerList.size >= 2) {
            inGame = true
            queueModel.newGame(playerList)
            _state.value = GameState.InGame(0, queueModel.dump())
        } else {
            _showStartGameError.call()
        }
    }

    fun nextTurn(pointsCollected: Int) {
        queueModel.nextTurn(pointsCollected)
        _clearKeyboard.call()

        val currentPlayerIndex = queueModel.currentPlayerIndex
        _state.value = GameState.InGame(currentPlayerIndex, queueModel.dump())
    }

    fun endGame() {
        inGame = false
        queueModel.resetScoreboard()
        _state.value = GameState.Finished(queueModel.dump())
    }

    //TODO: Load contacts after change in settings
    fun getAutocompleteData(fragment: Fragment) {
        val contactsSource = if (settingsProviderImpl.isContactsEnabled()) {
            contactsController.loadContacts(fragment.requireContext())
        } else {
            Single.just(emptyList())
        }
        val friendsSource =
            if (settingsProviderImpl.isFacebookEnabled() && fbController.isInitialized) {
                if (fbController.isLogged) {
                    Single.just(FacebookLoginResult.Success)
                } else {
                    fbController.logIn(fragment)
                }.flatMap {
                    if (it is FacebookLoginResult.Success) {
                        fbController.getFriendData()
                    } else {
                        Single.just(emptyList())
                    }
                }
            } else {
                Single.just(emptyList())
            }

        disposables.addAll(
            Observable.merge(
                contactsSource.flatMapObservable { Observable.fromIterable(it) },
                friendsSource.flatMapObservable { Observable.fromIterable(it) }
            )
                .toList()
                .subscribe { it -> _allPlayers.value = it }
        )
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fbController.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun shouldShowNoPointsConfirmation(points: Int): Boolean =
        points == 0 && settingsProviderImpl.isShowNoPointsConfirmationDialog()

    fun shouldUseInAppKeyboard() =
        settingsProviderImpl.shouldUseInAppKeyboard()

    fun getKeyboardColumnsCount(): Int =
        settingsProviderImpl.getKeyboardColumnsCount()

    fun isKeepOnScreen(): Boolean =
        settingsProviderImpl.isKeepOnScreen()

    fun isShowNoPointsConfirmationDialog(): Boolean =
        settingsProviderImpl.isShowNoPointsConfirmationDialog()

    fun setShowNoPointsConfirmationDialog(value: Boolean) {
        settingsProviderImpl.setShowNoPointsConfirmationDialog(value)
    }
}