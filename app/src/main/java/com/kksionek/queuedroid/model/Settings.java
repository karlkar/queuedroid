package com.kksionek.queuedroid.model;

import android.content.Context;
import android.preference.PreferenceManager;

@SuppressWarnings("SameParameterValue")
public class Settings {
    public static final String PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG =
            "PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG";
    public static final String PREF_KEEP_SCREEN_ON = "PREF_KEEP_SCREEN_ON";
    public static final String PREF_KEYBOARD_COLUMNS = "PREF_KEYBOARD_COLUMNS";
    public static final String PREF_USE_FACEBOOK = "PREF_USE_FACEBOOK";
    public static final String PREF_USE_CONTACTS = "PREF_USE_CONTACTS";
    public static final String PREF_USE_BUILT_IN_KEYBOARD = "PREF_USE_BUILT_IN_KEYBOARD";

    private Settings() {}

    public static boolean isShowNoPointsConfirmationDialog(Context context) {
        return getBoolean(context, PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG, true);
    }

    public static boolean getBoolean(Context context, String pref, boolean defVal) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(pref, defVal);
    }

    public static void setBoolean(Context context, String pref, boolean val) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(pref, val).apply();
    }

    public static String getString(Context context, String pref, String defVal) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(pref, defVal);
    }

    public static void setString(Context context, String pref, String val) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(pref, val).apply();
    }

    public static boolean isKeepOnScreen(Context context) {
        return getBoolean(context, PREF_KEEP_SCREEN_ON, true);
    }

    public static int getKeyboardColumnsCount(Context context) {
        return Integer.valueOf(getString(context, PREF_KEYBOARD_COLUMNS, "5"));
    }

    public static boolean isContactsEnabled(Context context) {
        return getBoolean(context, PREF_USE_CONTACTS, false);
    }

    public static boolean isFacebookEnabled(Context context) {
        return getBoolean(context, PREF_USE_FACEBOOK, false);
    }

    public static boolean shouldUseInAppKeyboard(Context context) {
        return getBoolean(context, PREF_USE_BUILT_IN_KEYBOARD, true);
    }
}
