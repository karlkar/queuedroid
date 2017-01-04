package com.kksionek.queuedroid.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.model.FbController;

import static com.kksionek.queuedroid.model.Settings.PREF_KEYBOARD_COLUMNS;
import static com.kksionek.queuedroid.model.Settings.PREF_SHARE_CAPTION;
import static com.kksionek.queuedroid.model.Settings.PREF_USE_CONTACTS;
import static com.kksionek.queuedroid.model.Settings.PREF_USE_FACEBOOK;
import static com.kksionek.queuedroid.view.MainActivity.PERMISSIONS_REQUEST_READ_CONTACTS;

public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, mSettingsFragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FbController.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            mSettingsFragment.onContactsPermissionResult(grantResults);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SharedPreferences mSharedPreferences;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.prefs);

            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            Preference prefKeyboardColumns = findPreference(PREF_KEYBOARD_COLUMNS);
            prefKeyboardColumns
                    .setSummary(mSharedPreferences.getString(PREF_KEYBOARD_COLUMNS, "5"));
            prefKeyboardColumns.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((String)newValue);
                    return true;
                }
            });

            Preference prefShareCaption = findPreference(PREF_SHARE_CAPTION);
            prefShareCaption
                    .setSummary(mSharedPreferences.getString(PREF_SHARE_CAPTION, ""));
            prefShareCaption
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            preference.setSummary((String)newValue);
                            return true;
                        }
                    });

            findPreference(PREF_USE_FACEBOOK)
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            boolean value = (boolean) newValue;

                            if (value && !FbController.isInitilized())
                                FbController.initialize(getActivity().getApplication());
                            if (value && !FbController.isLogged()) {
                                FbController.getInstance().logIn(
                                        getActivity(),
                                        new FbController.FacebookLoginListener() {
                                    @Override
                                    public void onLogged() {
                                    }

                                    @Override
                                    public void onCancel() {
                                        ((SwitchPreferenceCompat)findPreference(PREF_USE_FACEBOOK))
                                                .setChecked(false);
                                    }

                                    @Override
                                    public void onError() {
                                        ((SwitchPreferenceCompat)findPreference(PREF_USE_FACEBOOK))
                                                .setChecked(false);
                                    }
                                });
                            }
                            return true;
                        }
                    });

            findPreference(PREF_USE_CONTACTS)
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            boolean value = (boolean) newValue;
                            if (value) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    getActivity()
                                            .requestPermissions(
                                                    new String[]{
                                                            android.Manifest.permission.READ_CONTACTS},
                                                    PERMISSIONS_REQUEST_READ_CONTACTS);
                            }
                            return true;
                        }
                    });
        }

        public void onContactsPermissionResult(int[] grantResults) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ((SwitchPreferenceCompat)findPreference(PREF_USE_CONTACTS)).setChecked(false);
            }
        }
    }
}
