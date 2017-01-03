package com.kksionek.queuedroid.model;

import android.content.Context;
import android.preference.PreferenceManager;

public class Settings {
    public static final String PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG = "PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG";

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
}
