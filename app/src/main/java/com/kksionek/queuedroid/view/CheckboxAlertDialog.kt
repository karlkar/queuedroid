package com.kksionek.queuedroid.view

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.CheckBox
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.kksionek.queuedroid.databinding.CheckboxAlertDialogBinding
import com.kksionek.queuedroid.model.SettingsProviderImpl

internal class CheckboxAlertDialog {

    interface OnDialogClosedListener {
        fun onDialogClosed(result: Boolean)
    }

    private lateinit var context: Context
    private lateinit var mDontShowAgain: CheckBox
    private lateinit var mClosedListener: OnDialogClosedListener

    private lateinit var settingsProviderImpl: SettingsProviderImpl

    fun show(
        context: Context,
        @StringRes title: Int,
        @StringRes message: Int,
        settingsProviderImpl: SettingsProviderImpl,
        listener: OnDialogClosedListener
    ) {
        this.context = context
        mClosedListener = listener
        val binding = CheckboxAlertDialogBinding.inflate(
            LayoutInflater.from(context)
        )
        mDontShowAgain = binding.skip
        this.settingsProviderImpl = settingsProviderImpl

        AlertDialog.Builder(this.context)
            .setView(binding.root)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, DialogButtonClickListener(true))
            .setNegativeButton(android.R.string.cancel, DialogButtonClickListener(false))
            .show()
    }

    private inner class DialogButtonClickListener(
        private var mRetVal: Boolean = false
    ) : DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            settingsProviderImpl.setShowNoPointsConfirmationDialog(
                !mDontShowAgain.isChecked
            )
            dialog.cancel()
            mClosedListener.onDialogClosed(mRetVal)
        }
    }
}