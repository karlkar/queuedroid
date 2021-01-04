package com.kksionek.queuedroid.view

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.kksionek.queuedroid.R
import com.kksionek.queuedroid.model.Settings

internal class CheckboxAlertDialog {

    interface OnDialogClosedListener {
        fun onDialogClosed(result: Boolean)
    }

    private var mContext: Context? = null
    private var mDontShowAgain: CheckBox? = null
    private var mClosedListener: OnDialogClosedListener? = null

    fun show(
        context: Context?,
        @StringRes title: Int,
        @StringRes message: Int,
        listener: OnDialogClosedListener?
    ) {
        mContext = context
        mClosedListener = listener
        val eulaLayout = LayoutInflater.from(mContext)
            .inflate(R.layout.checkbox_alert_dialog, null)
        mDontShowAgain = eulaLayout.findViewById<View>(R.id.skip) as CheckBox

        AlertDialog.Builder(mContext!!)
            .setView(eulaLayout)
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
            Settings.setBoolean(
                mContext,
                Settings.PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG,
                !mDontShowAgain!!.isChecked
            )
            dialog.cancel()
            mClosedListener!!.onDialogClosed(mRetVal)
        }
    }
}