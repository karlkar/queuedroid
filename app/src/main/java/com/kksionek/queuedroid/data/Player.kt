package com.kksionek.queuedroid.data

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    private val type: Type = Type.CUSTOM
) : Parcelable {

    @IgnoredOnParcel
    val isFromFacebook: Boolean = type in listOf(Type.FACEBOOK, Type.MY_FB_PROFILE)
    @IgnoredOnParcel
    val isMyFbProfile: Boolean = type == Type.MY_FB_PROFILE

    override fun toString() = name

    enum class Type {
        MY_FB_PROFILE, FACEBOOK, CONTACTS, CUSTOM
    }
}