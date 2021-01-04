package com.kksionek.queuedroid.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kksionek.queuedroid.R

class PointsDialogFragment : DialogFragment() {

    interface PointsDialogListener {
        fun onDialogPositiveClick(points: Int)
    }

    private lateinit var mListener: PointsDialogListener
    private lateinit var mInputMethodManager: InputMethodManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = try {
            context as PointsDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.javaClass.simpleName + " must implement PointsDialogListener")
        }
        mInputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            maxLines = 1
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        builder.setView(input)
            .setMessage(R.string.view_keyboard_points_collected)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> acceptInput(dialog, input) }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                mInputMethodManager.hideSoftInputFromWindow(input.windowToken, 0)
                dialog.dismiss()
            }
        val alertDialog = builder.create().apply {
            setCanceledOnTouchOutside(false)
        }
        input.setOnEditorActionListener { _, _, _ ->
            acceptInput(alertDialog, input)
            true
        }
        input.requestFocus()
        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        return alertDialog
    }

    private fun acceptInput(dialog: DialogInterface, input: EditText) {
        if (input.text.toString().isEmpty()) {
            Toast.makeText(
                requireActivity(),
                R.string.view_keyboard_input_points_toast,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val points = input.text.toString().toInt()
        mListener.onDialogPositiveClick(points)
        mInputMethodManager.hideSoftInputFromWindow(input.windowToken, 0)
        dialog.dismiss()
    }
}