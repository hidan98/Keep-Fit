package com.danDay.agsr.ui.goals

import androidx.lifecycle.*
import com.danDay.agsr.data.Goal
import com.danDay.agsr.data.GoalDao
import com.danDay.agsr.ui.addeditgoal.ADD_TASK_RESULT_OK
import com.danDay.agsr.ui.addeditgoal.EDIT_TASK_RESULT_OK
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalsDao: GoalDao,
    private val state:SavedStateHandle
) : ViewModel() {
    val searchQuery = state.getLiveData("searchQuery","")

    private val goalEventChannel = Channel<GoalEvent>()
    val goalEvent = goalEventChannel.receiveAsFlow()

    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)

    private val goalFlow = combine(
        searchQuery.asFlow(),
        sortOrder
    ) { query, sortOrder ->
        Pair(query, sortOrder)
    }.flatMapLatest { (query, sortOrder) ->
        goalsDao.getTask(query, sortOrder)
    }
    val goals = goalFlow.asLiveData()


    fun onGoalSelected(goal: Goal) = viewModelScope.launch{
        goalEventChannel.send(GoalEvent.NavigateEditGoal(goal))
    }
    fun onFavoriteCheckedChanged(goal: Goal, isChecked: Boolean) = viewModelScope.launch {
        goalsDao.update(goal.copy(favourite = isChecked))
    }

    fun onGoalSwipe(goal: Goal)= viewModelScope.launch {
        goalsDao.delete(goal)
        goalEventChannel.send(GoalEvent.ShowUndoDelete(goal))
    }
    fun onUndoDeleteClick(goal: Goal)=viewModelScope.launch {
        goalsDao.insert(goal)
    }

    fun onAddNewGoalClick() = viewModelScope.launch {
        goalEventChannel.send(GoalEvent.NavigateAddGoal)
    }

    fun onAddEditResult(result: Int) {
        when(result){
            ADD_TASK_RESULT_OK-> showGoalConfirmationMessage("Goal added")
            EDIT_TASK_RESULT_OK->showGoalConfirmationMessage("Goal updated")
        }
    }

    private fun showGoalConfirmationMessage(text : String)=viewModelScope.launch {
        goalEventChannel.send(GoalEvent.ShowGoalSavedConfirmation(text))
    }
    sealed class GoalEvent{
        object NavigateAddGoal : GoalEvent()
        data class NavigateEditGoal(val goal: Goal):GoalEvent()
        data class ShowUndoDelete(val goal: Goal) : GoalEvent()
        data class ShowGoalSavedConfirmation(val msg:String):GoalEvent()
    }
}

enum class SortOrder { BY_NAME, BY_FAVORITE, BY_DATE }