package com.kksionek.queuedroid.model

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class SettingsProviderImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        const val PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG =
            "PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG"
        const val PREF_KEEP_SCREEN_ON = "PREF_KEEP_SCREEN_ON"
        const val PREF_KEYBOARD_COLUMNS = "PREF_KEYBOARD_COLUMNS"
        const val PREF_USE_FACEBOOK = "PREF_USE_FACEBOOK"
        const val PREF_USE_CONTACTS = "PREF_USE_CONTACTS"
        const val PREF_USE_BUILT_IN_KEYBOARD = "PREF_USE_BUILT_IN_KEYBOARD"
    }

    fun setShowNoPointsConfirmationDialog(value: Boolean){
        sharedPreferences.edit {
            putBoolean(PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG, value)
        }
    }

    fun isShowNoPointsConfirmationDialog(): Boolean =
        sharedPreferences.getBoolean(PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG, true)

    fun isKeepOnScreen(): Boolean =
        sharedPreferences.getBoolean(PREF_KEEP_SCREEN_ON, true)

    fun getKeyboardColumnsCount(): Int =
        sharedPreferences.getString(PREF_KEYBOARD_COLUMNS, "5")!!.toInt()

    fun isContactsEnabled(): Boolean =
        sharedPreferences.getBoolean(PREF_USE_CONTACTS, false)

    fun isFacebookEnabled(): Boolean =
        sharedPreferences.getBoolean(PREF_USE_FACEBOOK, false)

    fun shouldUseInAppKeyboard(): Boolean =
        sharedPreferences.getBoolean(PREF_USE_BUILT_IN_KEYBOARD, true)
}