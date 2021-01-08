package com.kksionek.queuedroid.view

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.model.FbController
import com.kksionek.queuedroid.model.FbController.FacebookLoginListener
import com.kksionek.queuedroid.model.SettingsProviderImpl
import com.kksionek.queuedroid.view.MainFragment.Companion.PERMISSIONS_REQUEST_READ_CONTACTS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val sharedPreferences: SharedPreferences get() = preferenceManager.sharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(SettingsProviderImpl.PREF_KEYBOARD_COLUMNS)?.let {
            it.summary =
                sharedPreferences.getString(SettingsProviderImpl.PREF_KEYBOARD_COLUMNS, "5")
            it.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue as String
                true
            }
        }
        findPreference<Preference>(SettingsProviderImpl.PREF_USE_FACEBOOK)
            ?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as Boolean
                if (value && FbController.isInitilized && !FbController.isLogged) {
                    FbController.instance.logIn(
                        requireActivity(),
                        object : FacebookLoginListener {
                            override fun onLogged() {}
                            override fun onCancel() {
                                findPreference<SwitchPreference>(SettingsProviderImpl.PREF_USE_FACEBOOK)?.isChecked =
                                    false
                            }

                            override fun onError() {
                                findPreference<SwitchPreference>(SettingsProviderImpl.PREF_USE_FACEBOOK)?.isChecked =
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
        findPreference<Preference>(SettingsProviderImpl.PREF_USE_CONTACTS)?.setOnPreferenceChangeListener { _, newValue ->
            val ac = requireActivity()
            if (newValue as Boolean
                && ac.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
            ) {
                ac.requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS
                )
            }
            true
        }
    }

    private fun onContactsPermissionResult(grantResults: IntArray) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            findPreference<SwitchPreference>(SettingsProviderImpl.PREF_USE_CONTACTS)?.isChecked = false
        }
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
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            onContactsPermissionResult(grantResults)
        }
    }
}