package com.danDay.agsr.ui.addeditgoal

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.danDay.agsr.databinding.FragmentDialogAddEditGoalBinding
import com.danDay.agsr.util.exhaustive
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditGoalDialogeFragment : DialogFragment(){

    private val viewModel: AddEditGoalViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDialogAddEditGoalBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDialogAddEditGoalBinding.bind(view)
        binding.apply {

            goalNameEditText.setText(viewModel.goalName)
            goalNameEditText.addTextChangedListener {
                viewModel.goalName = it.toString()
            }
            goalStepsEditText.setText(viewModel.goalStep.toString())
            goalStepsEditText.addTextChangedListener {
                viewModel.goalStep = it.toString().toInt()
            }

            done.setOnClickListener{
                viewModel.onSaveClick()
            }

            discard.setOnClickListener{
                dismiss()
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditGoalEvent.collect { event->
                when (event){
                    is AddEditGoalViewModel.AddEditGoalEvent.ShowInvalidNameInput -> {
                        binding.goalNameEditText.error = event.message
                    }
                    is AddEditGoalViewModel.AddEditGoalEvent.ShowInvalidStepInput -> {
                        binding.goalStepsEditText.error = event.message
                    }
                    is AddEditGoalViewModel.AddEditGoalEvent.NavigateBackWithResult -> {
                        binding.goalStepsEditText.clearFocus()
                        binding.goalNameEditText.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        dismiss()
                    }
                }.exhaustive
            }
        }

    }


}