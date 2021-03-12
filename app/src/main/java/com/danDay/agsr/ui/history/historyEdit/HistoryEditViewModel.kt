package com.danDay.agsr.ui.history.historyEdit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.danDay.agsr.data.Goal
import com.danDay.agsr.data.GoalDao
import com.danDay.agsr.data.History
import com.danDay.agsr.data.HistoryDao
import com.danDay.agsr.ui.addeditgoal.ADD_TASK_RESULT_OK
import com.danDay.agsr.ui.addeditgoal.AddEditGoalViewModel
import com.danDay.agsr.ui.addeditgoal.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryEditViewModel @Inject constructor(
    private val historyDao: HistoryDao,
    private val goalDao: GoalDao,
    private val state: SavedStateHandle
) : ViewModel() {


    private val historyEditEventChannel = Channel<HistoryEditEvent>()
    val historyEditEvent = historyEditEventChannel.receiveAsFlow()

    val history = state.get<History>("history")
    val goals = goalDao.getAllGoals().asLiveData()

    var historyAddStep = state.get<String>("historyAddStep") ?: "0"
        set(value) {
            field = value
            state.set("historyAddStep", value)
        }
    var goal = state.get<Goal>("goal") ?: null
        set(value) {
            field = value
            state.set("goal", value)
        }

    var time = state.get<Long>("time") ?: 0
        set(value) {
            field = value
            state.set("time", value)
        }

    fun saveChanges() {
        if (historyAddStep.isBlank()) {
            historyAddStep = "0"
        }

        if (history != null) {
            var updatedHistory = history.copy(steps = history.steps + historyAddStep.toLong())
            if (goal != null) {
                updatedHistory =
                    updatedHistory.copy(goalName = goal!!.name, goalSteps = goal!!.steps.toLong())
            }
            updateHistory(updatedHistory)


        } else {
            val history = History(current = false, steps = historyAddStep.toLong(), time =time)
            createHistory(history)
        }


    }

    private fun createHistory(history: History)= viewModelScope.launch {
        historyDao.insert(history)
        historyEditEventChannel.send(HistoryEditEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }


    private fun updateHistory(history: History) = viewModelScope.launch {
        historyDao.update(history)
        historyEditEventChannel.send(HistoryEditEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    sealed class HistoryEditEvent {
        data class NavigateBackWithResult(val result: Int) : HistoryEditEvent()
    }


}