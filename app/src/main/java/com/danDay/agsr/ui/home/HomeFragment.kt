@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.danDay.agsr.ui.home

import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import android.view.View
import androidx.core.math.MathUtils
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.danDay.agsr.R
import com.danDay.agsr.databinding.FragmentMainBinding
import com.danDay.agsr.ui.goals.GoalsViewModel
import com.danDay.agsr.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dialog_add_edit_goal.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.progress_layout.*
import kotlinx.android.synthetic.main.progress_layout.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_main) {
    private var progr = 50

    private val viewModel: HomeViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMainBinding.bind(view)


        //viewModel.getHistory()
        //viewModel.getCurrentGoal()


        binding.apply {

            step_input_layout_edit.addTextChangedListener {
                viewModel.historySteps = it.toString()
            }
            AddButton.setOnClickListener {
                if (viewModel.onAddStep()) {
                    stepInputLayoutEdit.setText("")

                }
            }

        }

        viewModel.goal.observe(viewLifecycleOwner) {
            if(it!=null)
            {
                viewModel.goalUsable = it
                updateView(binding)
            }

        }

        viewModel.history.observe(viewLifecycleOwner) {
            if(it!= null)
            {
                viewModel.historyUsable = it
                updateView(binding)
            }


        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.mainEvent.collect { event ->
                when (event) {
                    is HomeViewModel.MainEvent.UpdateProgressWheel -> {
                        binding.stepsDone.text = event.history.steps.toString()
                        binding.stepsGoal.text = event.goal.steps.toString()
                        var percent: Float =
                            ((event.history.steps.toFloat() / event.goal.steps.toFloat()) * 100.toFloat())
                        percent = MathUtils.clamp(percent, 0.0f, 100.0f)
                        binding.stepsPercent.text = percent.toString()
                        binding.progress.activityCompletion.progress = percent.toInt()
                    }
                    is HomeViewModel.MainEvent.ShowInvalidStepInput -> {
                        binding.stepInputLayoutEdit.error = event.message
                    }
                    is HomeViewModel.MainEvent.ShowUndoAdd -> {
                        Snackbar.make(requireView(), "Steps added", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.history)
                            }.show()
                    }
                    is HomeViewModel.MainEvent.HistoryFound -> {
                        viewModel.checkHistory()

                    }
                    HomeViewModel.MainEvent.HistoryDateChecked -> {
                        viewModel.updateHistory()
                    }
                }.exhaustive
            }
        }

        updateProgress()


    }


    private fun updateView(binding: FragmentMainBinding) {

        binding.apply {
            stepsDone.text = viewModel.historyUsable.steps.toString()
            stepsGoal.text = viewModel.goalUsable.steps.toString()
            var percent: Float = 0.toFloat()
            if (viewModel.historyUsable.steps == 0.toLong() || viewModel.goalUsable.steps == 0) {
                percent = 0.toFloat()
            } else {
                percent =(((viewModel.historyUsable.steps.toFloat() / viewModel.goalUsable.steps.toFloat()) * 100.toFloat()))
            }
            percent = MathUtils.clamp(percent, 0.0f, 100.0f)
            stepsPercent.text = percent.toString()
            progress.activityCompletion.progress = percent.toInt()
        }
    }

    private fun updateProgress() {


    }
}