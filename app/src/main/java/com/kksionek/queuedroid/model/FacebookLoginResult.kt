package com.kksionek.queuedroid.model

import com.facebook.FacebookException

sealed class FacebookLoginResult {

    object Success : FacebookLoginResult()

    object Cancelled : FacebookLoginResult()

    data class Error(val error: FacebookException) : FacebookLoginResult()
}