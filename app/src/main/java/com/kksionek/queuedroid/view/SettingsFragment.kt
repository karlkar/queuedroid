package com.kksionek.queuedroid.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.model.FacebookLoginResult
import com.kksionek.queuedroid.model.FbController
import com.kksionek.queuedroid.model.SettingsProviderImpl
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var settingsProviderImpl: SettingsProviderImpl

    @Inject
    lateinit var fbController: FbController

    private val disposables = CompositeDisposable()

    private val getContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (!result) {
                findPreference<SwitchPreference>(SettingsProviderImpl.PREF_USE_CONTACTS)?.isChecked =
                    false
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(SettingsProviderImpl.PREF_KEYBOARD_COLUMNS)?.let {
            it.summary = settingsProviderImpl.getKeyboardColumnsCount().toString()
            it.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue as String
                true
            }
        }
        findPreference<Preference>(SettingsProviderImpl.PREF_USE_FACEBOOK)
            ?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as Boolean
                if (value && fbController.isInitialized && !fbController.isLogged) {
                    disposables.add(fbController.logIn(this)
                        .subscribe { it ->
                            val pref =
                                findPreference<SwitchPreference>(SettingsProviderImpl.PREF_USE_FACEBOOK)
                            when (it) {
                                is FacebookLoginResult.Success -> {
                                    // no action
                                }
                                is FacebookLoginResult.Cancelled -> {
                                    pref?.isChecked = false
                                }
                                is FacebookLoginResult.Error -> {
                                    pref?.isChecked = false
                                }
                            }
                        })
                }
                true
            }
        findPreference<Preference>(SettingsProviderImpl.PREF_USE_CONTACTS)
            ?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as Boolean
                if (value) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && requireContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        getContactsPermission.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
                true
            }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fbController.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }
}