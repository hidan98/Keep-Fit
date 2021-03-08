package com.danDay.agsr.ui.home

import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.*
import com.danDay.agsr.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(

    private val historyDao: HistoryDao,
    private val goalDao: GoalDao,
    private val state: SavedStateHandle,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val mainEventChannel = Channel<MainEvent>()
    val mainEvent = mainEventChannel.receiveAsFlow()

    // lateinit var history: History


    val preferencesFlow = preferencesManager.preferencesFlow

    val history = historyDao.getLastHistory().asLiveData()
    var historyUsable: History = History(time = 0, current = false)


    val activeGoal = goalDao.getActiveFlow().asLiveData()
    val goalFavorite = goalDao.getAllFavourite().asLiveData()

    var goalUsable: Goal = Goal(active = false, name = "Error", steps = 0)

    var historySteps = state.get<String>("steps") ?: ""
        set(value) {
            field = value
            state.set("steps", value)
        }


    var goal = state.get<Goal>("goal") ?: null
        set(value) {
            field = value
            state.set("goal", value)
        }

    fun onAddStep(): Boolean {


        if (historySteps.isBlank()) {
            showInvalidNameInputMessage("Name cannot be blank")
            return false
        }

        findHistory()
        return true
    }

    private fun historySetup() = viewModelScope.launch {

    }

    private fun findHistory() = viewModelScope.launch {
        if (history == null) {
            historyDao.insert(History(current = true))

        }
        //checkHistory()
        mainEventChannel.send(MainEvent.HistoryFound)
    }

    fun checkHistory() = viewModelScope.launch {

        if (historyUsable != null) {
            val sdf = SimpleDateFormat("MM/dd/yy")

            val dateString: String = historyUsable.createDateFormat
            val date: Date = sdf.parse(dateString)
            val dateNow =
                DateFormat.getDateInstance(DateFormat.SHORT).format(System.currentTimeMillis())
            val dateNowParsed: Date = sdf.parse(dateNow)
            if (dateNowParsed.after(date)) {

                historyDao.insert(History(current = true))
                historyDao.update(historyUsable.copy(current = false))
            }
            mainEventChannel.send(MainEvent.HistoryDateChecked)
        }


    }


    fun updateHistory() = viewModelScope.launch {
        val oldHistory = historyUsable.copy()

        val updatedHistory = historyUsable.copy(
            steps = historySteps.toLong() + historyUsable.steps,
            goalSteps = goalUsable.steps.toLong(),
            goalName = goalUsable.name
        )

        historyDao.update(updatedHistory)
        mainEventChannel.send(MainEvent.ShowUndoAdd(oldHistory))
        mainEventChannel.send(MainEvent.UpdateProgressWheel(goalUsable, updatedHistory))
    }


    private fun showInvalidNameInputMessage(text: String) = viewModelScope.launch {
        mainEventChannel.send(MainEvent.ShowInvalidStepInput(text))
    }

    private fun updateProgress() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.UpdateProgressWheel(goalUsable, historyUsable))
    }

    fun onUndoDeleteClick(history: History) = viewModelScope.launch {
        historyDao.insert(history)
        mainEventChannel.send(MainEvent.UpdateProgressWheel(goalUsable, history))
    }

    fun changeActive(goal: Goal) = viewModelScope.launch {

        if (goalUsable != null) {
            if (goalUsable.id == goal.id) {
                goalDao.update(goalUsable.copy(active = !goalUsable.active))
                preferencesManager.updateGalID(0)
            } else {
                goalDao.update(goalUsable.copy(active = !goalUsable.active))
                goalDao.update(goal.copy(active = !goal.active))
                preferencesManager.updateGalID(goal.id)
            }
        } else {
            goalDao.update(goal.copy(active = !goal.active))
            preferencesManager.updateGalID(goal.id)
        }
    }

    /*

    fun getCurrentGoal() = viewModelScope.launch {
        preferencesFlow.collect {
            goalDao.getGoalById(it.goalId).collect {
                goal = it
            }
        }

    }

     */

    sealed class MainEvent {
        data class ShowInvalidStepInput(val message: String) : MainEvent()
        data class UpdateProgressWheel(val goal: Goal, val history: History) : MainEvent()
        data class ShowUndoAdd(val history: History) : MainEvent()
        object HistoryFound : MainEvent()
        object HistoryDateChecked : MainEvent()
    }

}