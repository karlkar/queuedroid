package com.kksionek.queuedroid.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import com.kksionek.queuedroid.R

class PointsDialogFragment : DialogFragment() {

    fun interface PointsDialogListener {
        fun onDialogPositiveClick(points: Int)
    }

    private lateinit var listener: PointsDialogListener
    private lateinit var inputMethodManager: InputMethodManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as PointsDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.javaClass.simpleName + " must implement PointsDialogListener")
        }
        inputMethodManager = context.getSystemService()!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            maxLines = 1
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        val builder = AlertDialog.Builder(requireContext())
            .setView(input)
            .setMessage(R.string.view_keyboard_points_collected)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> acceptInput(dialog, input) }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                hideKeyboard(input)
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
        showKeyboard()
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
        listener.onDialogPositiveClick(points)
        hideKeyboard(input)
        dialog.dismiss()
    }

    private fun showKeyboard() {
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard(view: View) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}