package com.kksionek.queuedroid.model

import android.content.Context
import androidx.preference.PreferenceManager

object Settings {
    const val PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG = "PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG"
    const val PREF_KEEP_SCREEN_ON = "PREF_KEEP_SCREEN_ON"
    const val PREF_KEYBOARD_COLUMNS = "PREF_KEYBOARD_COLUMNS"
    const val PREF_USE_FACEBOOK = "PREF_USE_FACEBOOK"
    const val PREF_USE_CONTACTS = "PREF_USE_CONTACTS"
    const val PREF_USE_BUILT_IN_KEYBOARD = "PREF_USE_BUILT_IN_KEYBOARD"

    fun isShowNoPointsConfirmationDialog(context: Context?): Boolean {
        return getBoolean(context, PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG, true)
    }

    fun getBoolean(context: Context?, pref: String?, defVal: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(pref, defVal)
    }

    fun setBoolean(context: Context?, pref: String?, `val`: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(pref, `val`)
            .apply()
    }

    fun getString(context: Context?, pref: String?, defVal: String?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(pref, defVal)
    }

    fun setString(context: Context?, pref: String?, `val`: String?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(pref, `val`).apply()
    }

    fun isKeepOnScreen(context: Context?): Boolean {
        return getBoolean(context, PREF_KEEP_SCREEN_ON, true)
    }

    fun getKeyboardColumnsCount(context: Context?): Int {
        return Integer.valueOf(getString(context, PREF_KEYBOARD_COLUMNS, "5")!!)
    }

    fun isContactsEnabled(context: Context?): Boolean {
        return getBoolean(context, PREF_USE_CONTACTS, false)
    }

    fun isFacebookEnabled(context: Context?): Boolean {
        return getBoolean(context, PREF_USE_FACEBOOK, false)
    }

    fun shouldUseInAppKeyboard(context: Context?): Boolean {
        return getBoolean(context, PREF_USE_BUILT_IN_KEYBOARD, true)
    }
}