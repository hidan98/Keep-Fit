package com.danDay.agsr.ui.addeditgoal

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import com.danDay.agsr.data.Goal
import com.danDay.agsr.data.GoalDao
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditGoalViewModel @Inject constructor(
    private val goalDoe: GoalDao,
    private val state: SavedStateHandle
) : ViewModel() {

    val goal = state.get<Goal>("goal")
    var goalName = state.get<String>("goalName") ?: goal?.name ?: ""
        set(value) {
            field = value
            state.set("goalName", value)
        }
    var goalStep = state.get<String>("goalSteps") ?: goal?.steps ?:""
        set(value) {
            field = value
            state.set("goalSteps", value)
        }



    private val addEditTaskEventChannel = Channel<AddEditGoalEvent>()
    val addEditGoalEvent = addEditTaskEventChannel.receiveAsFlow()


    fun onSaveClick() {
        if(goalName.isBlank()){
            showInvalidNameInputMessage("Name cannot be blank")
            return
        }
        if(goalStep.toString().isBlank())
        {
            showInvalidStepsInputMessage("Steps must not be empty")
            return
        }

        if(goal != null){
            val updatedGoal = goal.copy(name = goalName, steps = goalStep.toString().toInt())
            updateGoal(updatedGoal)
        }else{
            val goalNew = Goal(name = goalName, steps = goalStep.toString().toInt())
            createGoal(goalNew)
        }
    }



    private fun createGoal(goal: Goal)=viewModelScope.launch {
        goalDoe.insert(goal)
        addEditTaskEventChannel.send(AddEditGoalEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateGoal(goal: Goal)= viewModelScope.launch {
        goalDoe.update(goal)
        addEditTaskEventChannel.send(AddEditGoalEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidNameInputMessage(text :String)=viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditGoalEvent.ShowInvalidNameInput(text))
    }
    private fun showInvalidStepsInputMessage(text :String)=viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditGoalEvent.ShowInvalidStepInput(text))
    }



    sealed class AddEditGoalEvent{
        data class ShowInvalidStepInput(val message: String): AddEditGoalEvent()
        data class ShowInvalidNameInput(val message: String): AddEditGoalEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditGoalEvent()
    }

}

const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1