package com.kksionek.queuedroid.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.model.FbController;

import static com.kksionek.queuedroid.model.Settings.PREF_KEYBOARD_COLUMNS;
import static com.kksionek.queuedroid.model.Settings.PREF_USE_CONTACTS;
import static com.kksionek.queuedroid.model.Settings.PREF_USE_FACEBOOK;
import static com.kksionek.queuedroid.view.MainActivity.PERMISSIONS_REQUEST_READ_CONTACTS;

public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mSettingsFragment = new SettingsFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, mSettingsFragment, "SETTINGS")
                    .commit();
        } else
            mSettingsFragment = (SettingsFragment) getFragmentManager().findFragmentByTag("SETTINGS");
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

    public static class SettingsFragment extends PreferenceFragment {

        private SharedPreferences mSharedPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            View rootView = getView();
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            list.setDivider(null);

            Preference prefKeyboardColumns = findPreference(PREF_KEYBOARD_COLUMNS);
            prefKeyboardColumns
                    .setSummary(mSharedPreferences.getString(PREF_KEYBOARD_COLUMNS, "5"));
            prefKeyboardColumns.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((String) newValue);
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
                                                ((SwitchPreference) findPreference(PREF_USE_FACEBOOK))
                                                        .setChecked(false);
                                            }

                                            @Override
                                            public void onError() {
                                                ((SwitchPreference) findPreference(PREF_USE_FACEBOOK))
                                                        .setChecked(false);
                                            }
                                        });
                            }
                            return true;
                        }
                    });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermission();
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        public void requestPermission() {
            findPreference(PREF_USE_CONTACTS).setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            Activity ac = getActivity();
                            if ((boolean) newValue
                                    && ac.checkSelfPermission(
                                    Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                                ac.requestPermissions(
                                        new String[]{android.Manifest.permission.READ_CONTACTS},
                                        PERMISSIONS_REQUEST_READ_CONTACTS);
                            }
                            return true;
                        }
                    });
        }

        public void onContactsPermissionResult(int[] grantResults) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ((SwitchPreference) findPreference(PREF_USE_CONTACTS)).setChecked(false);
            }
        }
    }
}
