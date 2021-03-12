package com.danDay.agsr.ui.goals

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.danDay.agsr.data.Goal
import com.danDay.agsr.data.GoalDao
import com.danDay.agsr.data.PreferencesManager
import com.danDay.agsr.data.SortOrder
import com.danDay.agsr.ui.addeditgoal.ADD_TASK_RESULT_OK
import com.danDay.agsr.ui.addeditgoal.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext


import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalsDao: GoalDao,
    private val state: SavedStateHandle,
    private val preferencesManager: PreferencesManager,

) : ViewModel() {
    val searchQuery = state.getLiveData("searchQuery", "")

    private val goalEventChannel = Channel<GoalEvent>()
    val goalEvent = goalEventChannel.receiveAsFlow()

    val preferencesFlow = preferencesManager.preferencesFlow

    var allowGoalEdit: Boolean = false


    private val goalFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        goalsDao.getTask(query, filterPreferences.sortOrder)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    val goals = goalFlow.asLiveData()


    fun onGoalSelected(goal: Goal) = viewModelScope.launch {
        if (allowGoalEdit) {
            if (goal.active) {
                goalEventChannel.send(GoalEvent.ShowCantEditGoal("Cant edit active goal"))
            } else {
                goalEventChannel.send(GoalEvent.NavigateEditGoal(goal))
            }
        }
        else
            goalEventChannel.send((GoalEvent.ShowCantEditGoal("Cant edit goals, please change in settings")))
    }

    fun onFavoriteCheckedChanged(goal: Goal, isChecked: Boolean) = viewModelScope.launch {
        Log.d("test", "Log")
        goalsDao.update(goal.copy(favourite = isChecked))
    }

    fun onActiveSelected(goal: Goal) = viewModelScope.launch {
        val active = goalsDao.getActive()
        if (active != null) {
            if (active.id == goal.id) {
                goalsDao.update(active.copy(active = !active.active))
                preferencesManager.updateGalID(0)
            } else {
                goalsDao.update(active.copy(active = !active.active))
                goalsDao.update(goal.copy(active = !goal.active))
                preferencesManager.updateGalID(goal.id)
            }
        } else {
            goalsDao.update(goal.copy(active = !goal.active))
            preferencesManager.updateGalID(goal.id)
        }


    }

    fun onGoalSwipe(goal: Goal) = viewModelScope.launch {
        goalsDao.delete(goal)
        goalEventChannel.send(GoalEvent.ShowUndoDelete(goal))
    }

    fun onUndoDeleteClick(goal: Goal) = viewModelScope.launch {
        goalsDao.insert(goal)
    }

    fun onAddNewGoalClick() = viewModelScope.launch {
        goalEventChannel.send(GoalEvent.NavigateAddGoal)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showGoalConfirmationMessage("Goal added")
            EDIT_TASK_RESULT_OK -> showGoalConfirmationMessage("Goal updated")
        }
    }

    private fun showGoalConfirmationMessage(text: String) = viewModelScope.launch {
        goalEventChannel.send(GoalEvent.ShowGoalSavedConfirmation(text))
    }

    sealed class GoalEvent {
        object NavigateAddGoal : GoalEvent()
        data class NavigateEditGoal(val goal: Goal) : GoalEvent()
        data class ShowUndoDelete(val goal: Goal) : GoalEvent()
        data class ShowGoalSavedConfirmation(val msg: String) : GoalEvent()
        data class ShowCantEditGoal(val msg: String):GoalEvent()
    }
}

