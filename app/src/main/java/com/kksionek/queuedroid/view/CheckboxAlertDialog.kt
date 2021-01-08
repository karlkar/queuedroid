package com.kksionek.queuedroid.view

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.kksionek.queuedroid.databinding.CheckboxAlertDialogBinding

object CheckboxAlertDialog {

    fun interface OnDialogClosedListener {
        fun onDialogClosed(accepted: Boolean, showAgain: Boolean)
    }

    fun show(
        context: Context,
        @StringRes title: Int,
        @StringRes message: Int,
        listener: OnDialogClosedListener
    ) {
        val binding = CheckboxAlertDialogBinding.inflate(
            LayoutInflater.from(context)
        )

        AlertDialog.Builder(context)
            .setView(binding.root)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                listener.onDialogClosed(true, !binding.skipCheckBox.isChecked)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                listener.onDialogClosed(false, !binding.skipCheckBox.isChecked)
            }
            .show()
    }
}