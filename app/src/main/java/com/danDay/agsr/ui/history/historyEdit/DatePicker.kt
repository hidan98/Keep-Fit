package com.danDay.agsr.ui.history.historyEdit

import android.R
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle

import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.danDay.agsr.ui.addeditgoal.ADD_TASK_RESULT_OK
import dagger.hilt.android.AndroidEntryPoint

import java.util.*

@AndroidEntryPoint
class DatePicker : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private val viewModel: DatePickerViewModel by viewModels()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireContext(), this, year, month, day)
    }


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, dayOfMonth)
        viewModel.setHistory(c)
        setFragmentResult(
            "add_edit_request",
            Bundle().apply {
                putInt("add_edit_result", ADD_TASK_RESULT_OK)
            }
        )

    }


}