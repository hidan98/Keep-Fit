package com.danDay.agsr.ui.history.historyEdit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.danDay.agsr.R
import com.danDay.agsr.data.Goal
import com.danDay.agsr.databinding.FragmentDialogAddEditGoalBinding
import com.danDay.agsr.databinding.FragmentHistoryEditBinding
import com.danDay.agsr.util.exhaustive

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_history_edit.*
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class HistoryEditFragment : DialogFragment() {

    private val viewModel: HistoryEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentHistoryEditBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHistoryEditBinding.bind(view)




        binding.apply {


            history_edit_step_input_layout_edit.addTextChangedListener {
                viewModel.historyAddStep = it.toString()
            }

            historyEditDone.setOnClickListener {
                viewModel.saveChanges()
            }




            viewModel.goals.observe(viewLifecycleOwner) {
                val adapter = ArrayAdapter(requireContext(), R.layout.list_item, it)

                historyEditDropDownTextView.setAdapter(adapter)
                historyEditDropDownTextView.setText(
                    "Name: ${viewModel.history?.goalName} Steps: ${viewModel.history?.goalSteps}",
                    false
                )
                history_edit_dropDown_TextView.onItemClickListener =
                    AdapterView.OnItemClickListener { parent, view, position, id -> //val goal =
                        viewModel.goal = adapter.getItem(position)
                    }


            }

            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.historyEditEvent.collect { event ->
                    when (event) {
                        is HistoryEditViewModel.HistoryEditEvent.NavigateBackWithResult -> {
                            setFragmentResult(
                                "add_edit_request",
                                Bundle().apply {
                                    putInt("add_edit_result", event.result)
                                }
                            )
                            dismiss()
                        }
                    }.exhaustive
                }
            }
        }


    }


}
