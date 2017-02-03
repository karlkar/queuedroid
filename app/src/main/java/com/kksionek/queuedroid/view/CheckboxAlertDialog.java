package com.kksionek.queuedroid.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.kksionek.queuedroid.R;
import com.kksionek.queuedroid.model.Settings;

import static com.kksionek.queuedroid.model.Settings.PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG;

class CheckboxAlertDialog {

    public interface OnDialogClosedListener {
        void onDialogClosed(boolean result);
    }

    private Context mContext;
    private CheckBox mDontShowAgain = null;

    private OnDialogClosedListener mClosedListener;

    public CheckboxAlertDialog() {
    }

    public void show(Context context, @StringRes int title, @StringRes int message, OnDialogClosedListener listener) {
        mContext = context;
        mClosedListener = listener;

        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        View eulaLayout = LayoutInflater.from(mContext)
                .inflate(R.layout.checkbox_alert_dialog, null);

        mDontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        adb.setView(eulaLayout);
        adb.setTitle(title);
        adb.setMessage(message);
        adb.setPositiveButton(android.R.string.ok, new DialogButtonClickListener(true));
        adb.setNegativeButton(android.R.string.cancel, new DialogButtonClickListener(false));
        adb.show();
    }

    private class DialogButtonClickListener implements DialogInterface.OnClickListener {
        private boolean mRetVal = false;

        public DialogButtonClickListener(boolean val) {
            mRetVal = val;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Settings.setBoolean(mContext,
                    PREF_SHOW_NO_POINTS_CONFIRMATION_DIALOG,
                    !mDontShowAgain.isChecked());

            dialog.cancel();
            mClosedListener.onDialogClosed(mRetVal);
        }
    }
}
