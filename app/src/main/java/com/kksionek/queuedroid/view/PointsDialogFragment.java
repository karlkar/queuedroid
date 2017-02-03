package com.kksionek.queuedroid.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kksionek.queuedroid.R;

public class PointsDialogFragment extends DialogFragment {

    private InputMethodManager mInputMethodManager;

    public interface PointsDialogListener {
        void onDialogPositiveClick(int points);
    }

    private PointsDialogListener mListener = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PointsDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement PointsDialogListener");
        }
        mInputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setLines(1);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        builder.setView(input);
        builder.setMessage(R.string.view_keyboard_points_collected)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        acceptInput(dialog, input);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mInputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        dialog.dismiss();
                    }
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                acceptInput(alertDialog, input);
                return true;
            }
        });
        input.requestFocus();
        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        return alertDialog;
    }

    private void acceptInput(DialogInterface dialog, EditText input) {
        if (input.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), R.string.view_keyboard_input_points_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        int points = Integer.parseInt(input.getText().toString());
        if (mListener != null)
            mListener.onDialogPositiveClick(points);
        mInputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
        dialog.dismiss();
    }
}
