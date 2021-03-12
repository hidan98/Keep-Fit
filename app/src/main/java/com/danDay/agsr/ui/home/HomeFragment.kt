@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.danDay.agsr.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.danDay.agsr.R
import com.danDay.agsr.data.History
import com.danDay.agsr.databinding.FragmentMainBinding
import com.danDay.agsr.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dialog_add_edit_goal.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_main), SensorEventListener {

    private val viewModel: HomeViewModel by viewModels()
    private var dayChanged: Boolean = false
    private lateinit var sensorManager: SensorManager
    private var first = true
    private var last: Int = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMainBinding.bind(view)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        viewModel.historySave = sharedPreferences.getBoolean("HistorySwitch", true)
        viewModel.autoupdate = sharedPreferences.getBoolean("automaticStep", false)

        binding.apply {

            step_input_layout_edit.addTextChangedListener {
                viewModel.historySteps = it.toString()
            }
            stepInputLayoutEdit.setOnEditorActionListener { v, actionId, event ->
                var handled = false
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    viewModel.onAddStep()
                    val imm: InputMethodManager =
                        v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    true
                }
                else
                    false

            }
            AddButton.setOnClickListener {
                viewModel.onAddStep()
            }

            viewModel.goalFavorite.observe(viewLifecycleOwner) {
                val adapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
                dropDownTextView.setAdapter(adapter)
                dropDownTextView.setText(
                    "Name: ${viewModel.goalUsable.name} Steps: ${viewModel.goalUsable.steps}",
                    false
                )
                dropDownTextView.onItemClickListener =
                    AdapterView.OnItemClickListener { parent, view, position, id -> //val goal =
                        viewModel.changeActive(adapter.getItem(position)!!)
                        dropDownTextView.clearFocus()
                    }
            }
        }

        /*
        if(viewModel.autoupdate)
        {
            //viewModel.numberStepsLive.setup()
            viewModel.numberStepsLive.observe(viewLifecycleOwner){
                viewModel.historySteps = viewModel.numberStepsLive.getStep().toString()
                viewModel.findHistory()
            }
        }
        else{
            //viewModel.numberStepsLive.turnOff()
        }

         */


        viewModel.activeGoal.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.goalUsable = it
                binding.dropDownTextView.setText("Name: ${it.name} Steps: ${it.steps}")
                updateView(binding)
            }
        }


        viewModel.history.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.historyUsable = it
                if (dayChanged) {
                    viewModel.updateHistory(viewModel.stepStore)
                    dayChanged = false
                }
                updateView(binding)
            } else {
                viewModel.historyUsable = History(time = 0L, current = false)
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

                        binding.stepsPercent.text = percent.toString()
                        percent = MathUtils.clamp(percent, 0.0f, 100.0f)
                        binding.progress.progress = percent.toInt()
                    }
                    is HomeViewModel.MainEvent.ShowInvalidStepInput -> {
                        binding.stepInputLayoutEdit.error = event.message
                    }
                    is HomeViewModel.MainEvent.ShowUndoAdd -> {
                        Snackbar.make(requireView(), "Steps added", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.history)
                            }.show()
                        binding.stepInputLayoutEdit.setText("")
                    }
                    is HomeViewModel.MainEvent.HistoryFound -> {
                        viewModel.checkHistory()
                    }
                    is HomeViewModel.MainEvent.HistoryDateChecked -> {
                        viewModel.updateHistory(viewModel.stepStore)
                    }
                    is HomeViewModel.MainEvent.DateChanged -> {
                        dayChanged = true
                    }
                }.exhaustive
            }

            viewModel.preferencesFlow.collect {
                viewModel.stepStore = it.stepStore
            }
        }
        updateView(binding)
    }


    private fun updateView(binding: FragmentMainBinding) {

        binding.apply {
            stepsDone.text = viewModel.historyUsable.steps.toString()
            stepsGoal.text = viewModel.goalUsable.steps.toString()
            var percent: Float = 0.toFloat()
            if (viewModel.historyUsable.steps == 0.toLong() || viewModel.goalUsable.steps == 0) {
                percent = 0.toFloat()
            } else {
                percent =
                    (((viewModel.historyUsable.steps.toFloat() / viewModel.goalUsable.steps.toFloat()) * 100.toFloat()))
            }

            stepsPercent.text = percent.toString() + "%"
            percent = MathUtils.clamp(percent, 0.0f, 100.0f)
            progress.progress = percent.toInt()
        }
    }


    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferences.getBoolean("automaticStep", false)) {
            sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            stepCounterSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferences.getBoolean("automaticStep", false)) {
            sensorManager.unregisterListener(this)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            //ask for permission
            // requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PHYISCAL_ACTIVITY);
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (first) {
                last = event.values[0].toInt()
                first = false
            } else {
                val step = (event.values[0] - last)
                if (step > 0) {
                    viewModel.stepStore = step.toLong()
                    viewModel.findHistory()
                }

                last = event.values[0].toInt()
                Log.d("Step", last.toString())
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO("Not yet implemented")
    }


}