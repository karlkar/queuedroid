package com.kksionek.queuedroid.view

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.model.FbController
import com.kksionek.queuedroid.model.FbController.FacebookLoginListener
import com.kksionek.queuedroid.model.Settings

class SettingsActivity : AppCompatActivity() {
    private var mSettingsFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            mSettingsFragment = SettingsFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, mSettingsFragment!!, "SETTINGS")
                .commit()
        } else mSettingsFragment = supportFragmentManager.findFragmentByTag("SETTINGS") as SettingsFragment
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FbController.instance.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MainActivity.PERMISSIONS_REQUEST_READ_CONTACTS) {
            mSettingsFragment!!.onContactsPermissionResult(grantResults)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private var mSharedPreferences: SharedPreferences? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                activity
            )
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            val rootView = view
            val list = rootView!!.findViewById<View>(android.R.id.list) as ListView
            list.divider = null
            findPreference<Preference>(Settings.PREF_KEYBOARD_COLUMNS)?.let {
                it.summary =
                    mSharedPreferences!!.getString(Settings.PREF_KEYBOARD_COLUMNS, "5")
                it.setOnPreferenceChangeListener { preference, newValue ->
                    preference.summary = newValue as String
                    true
                }
            }
            findPreference<Preference>(Settings.PREF_USE_FACEBOOK)?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as Boolean
                if (value && FbController.isInitilized && !FbController.isLogged) {
                    FbController.instance.logIn(
                        requireActivity(),
                        object : FacebookLoginListener {
                            override fun onLogged() {}
                            override fun onCancel() {
                                findPreference<SwitchPreference>(Settings.PREF_USE_FACEBOOK)?.isChecked =
                                    false
                            }

                            override fun onError() {
                                findPreference<SwitchPreference>(Settings.PREF_USE_FACEBOOK)?.isChecked =
                                    false
                            }
                        })
                }
                true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermission()
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.prefs)
        }

        @TargetApi(Build.VERSION_CODES.M)
        fun requestPermission() {
            findPreference<Preference>(Settings.PREF_USE_CONTACTS)?.setOnPreferenceChangeListener { _, newValue ->
                val ac = requireActivity()
                if (newValue as Boolean
                    && ac.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                ) {
                    ac.requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        MainActivity.PERMISSIONS_REQUEST_READ_CONTACTS
                    )
                }
                true
            }
        }

        fun onContactsPermissionResult(grantResults: IntArray) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                findPreference<SwitchPreference>(Settings.PREF_USE_CONTACTS)?.isChecked = false
            }
        }
    }
}