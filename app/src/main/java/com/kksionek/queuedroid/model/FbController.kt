package com.kksionek.queuedroid.model

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.kksionek.queuedroid.data.Player
import org.json.JSONException

class FbController private constructor() {

    private var mMyProfile: Player? = null
    private lateinit var mLoginManager: LoginManager
    private var mCallbackManager: CallbackManager? = null

    interface FacebookLoginListener {
        fun onLogged()
        fun onCancel()
        fun onError()
    }

    fun logIn(activity: Activity, listener: FacebookLoginListener) {
        mLoginManager = LoginManager.getInstance()
        mCallbackManager = CallbackManager.Factory.create()
        mLoginManager.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                Log.d(TAG, "onSuccess: Facebook logged in")
                getMyProfile(null)
                listener.onLogged()
            }

            override fun onCancel() {
                Log.d(TAG, "onCancel: Facebook login cancelled")
                listener.onCancel()
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "onError: Facebook login error. Should try again...")
                listener.onError()
            }
        })
        mLoginManager.logInWithReadPermissions(activity, listOf("user_friends"))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager?.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }

    private fun getMyProfile(targetList: MutableList<Player>?) {
        val req =
            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()) { `object`, _ ->
                Log.d(TAG, "onCompleted: $`object`")
                mMyProfile = Player.createFacebookFriend(`object`, true)?.also {
                    targetList?.add(it)
                }
            }
        val parameters = Bundle()
        parameters.putString("fields", "id,name,picture")
        req.parameters = parameters
        req.executeAsync()
    }

    fun getFriendData(targetList: MutableList<Player>) {
        requestFriends(targetList, null)
    }

    private fun requestFriends(targetList: MutableList<Player>, nextToken: String?) {
        val req = GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/taggable_friends",
            null,
            HttpMethod.GET
        ) { response ->
            Log.d(TAG, "onCompleted: response = $response")
            if (response.error != null) Log.e(
                TAG,
                "onCompleted: Couldn't obtain friend data."
            ) else {
                try {
                    val friendArray = response.jsonObject.getJSONArray("data")
                    for (i in 0 until friendArray.length()) {
                        Log.d(TAG, "onCompleted: FRIEND = " + friendArray[i].toString())
                        targetList.add(Player.createFacebookFriend(friendArray.getJSONObject(i)))
                    }
                    if (!response.jsonObject.isNull("paging")) {
                        val token =
                            response.jsonObject.getJSONObject("paging").getJSONObject("cursors")
                                .getString("after")
                        requestFriends(targetList, token)
                    } else {
                        mMyProfile?.let { targetList.add(it) } ?: getMyProfile(targetList)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        if (nextToken != null) {
            val parameters = Bundle()
            parameters.putString("after", nextToken)
            req.parameters = parameters
        }
        req.executeAsync()
    }

    companion object {
        private const val TAG = "FbController"

        val instance = FbController()
        val isInitilized: Boolean
            get() = FacebookSdk.isInitialized()
        val isLogged: Boolean
            get() = AccessToken.getCurrentAccessToken() != null

        fun shareOnFacebook(activity: Activity, list: List<String>, bitmap: Bitmap) {
            if (ShareDialog.canShow(SharePhotoContent::class.java)) {
                val photo = SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .setUserGenerated(true)
                    .build()
                val sharePhotoContent = SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .setPeopleIds(list)
                    .build()
                val shareDialog = ShareDialog(activity)
                shareDialog.show(sharePhotoContent)
            }
        }
    }
}