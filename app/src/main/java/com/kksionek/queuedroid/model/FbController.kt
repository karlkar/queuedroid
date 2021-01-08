package com.kksionek.queuedroid.model

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.google.gson.Gson
import com.kksionek.queuedroid.data.Player
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class FbController @Inject constructor() {

    private var myProfile: Player? = null
    private val loginManager: LoginManager by lazy {
        LoginManager.getInstance()
    }
    private val callbackManager: CallbackManager by lazy {
        CallbackManager.Factory.create()
    }

    val isInitialized: Boolean
        get() = FacebookSdk.isInitialized()
    val isLogged: Boolean
        get() = AccessToken.getCurrentAccessToken() != null

    @CheckResult
    fun logIn(fragment: Fragment): Single<FacebookLoginResult> {
        return Single.fromPublisher<FacebookLoginResult> { publisher ->
            loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    Log.d(TAG, "onSuccess: Facebook logged in")
                    publisher.onNext(FacebookLoginResult.Success)
                    publisher.onComplete()
                }

                override fun onCancel() {
                    Log.d(TAG, "onCancel: Facebook login cancelled")
                    publisher.onNext(FacebookLoginResult.Cancelled)
                    publisher.onComplete()
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "onError: Facebook login error. Should try again...")
                    publisher.onNext(FacebookLoginResult.Error(error))
                    publisher.onComplete()
                }
            })
            loginManager.logInWithReadPermissions(fragment, listOf("user_friends"))
        }.flatMap { list -> getMyProfile().map { list } }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }

    @CheckResult
    private fun getMyProfile(): Single<List<Player>> {
        return Single.fromCallable {
            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()) { jsonObject, _ ->
                Log.d(TAG, "onCompleted: $jsonObject")
                val friendData = Gson().fromJson(jsonObject.toString(), FriendData::class.java)
                myProfile = Player(
                    friendData.id,
                    friendData.name,
                    friendData.picture.picture.data.url,
                    Player.Type.MY_FB_PROFILE
                )
            }.apply {
                parameters = Bundle().apply {
                    putString("fields", "id,name,picture")
                }
                executeAndWait()
            }
            myProfile?.let { listOf(it) }.orEmpty()
        }
    }

    @CheckResult
    fun getFriendData(): Single<List<Player>> =
        requestFriends()

    @CheckResult
    private fun requestFriends(nextToken: String? = null): Single<List<Player>> {
        val params = nextToken?.let {
            Bundle().apply {
                putString("after", it)
            }
        }
        val request = GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/taggable_friends",
            params,
            HttpMethod.GET
        )
        return Single.fromCallable {
            val response = request.executeAndWait()
            Log.d(TAG, "onCompleted: response = $response")
            if (response.error != null) {
                Log.e(TAG, "onCompleted: Couldn't obtain friend data.")
                emptyList<Player>() to null
            } else {
                val friendDataList =
                    Gson().fromJson(response.jsonObject.toString(), FriendDataList::class.java)
                val facebookPlayers = friendDataList.data.map {
                    Player(it.id, it.name, it.picture.picture.data.url, Player.Type.FACEBOOK)
                }
                val nextRequest = friendDataList?.paging?.cursors?.after?.let {
                    requestFriends(it)
                } ?: myProfile?.let { Single.just(listOf(it)) } ?: getMyProfile()
                facebookPlayers to nextRequest
            }
        }.flatMap { pair ->
            (pair.second ?: Single.just(emptyList())).map { pair.first + it }
        }
    }

    companion object {
        private const val TAG = "FbController"

        fun shareOnFacebook(fragment: Fragment, list: List<String>, bitmap: Bitmap) {
            if (ShareDialog.canShow(SharePhotoContent::class.java)) {
                val photo = SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .setUserGenerated(true)
                    .build()
                val sharePhotoContent = SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .setPeopleIds(list)
                    .build()
                ShareDialog(fragment).show(sharePhotoContent)
            }
        }
    }
}