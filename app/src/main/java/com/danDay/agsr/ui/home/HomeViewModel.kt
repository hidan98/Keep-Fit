package com.danDay.agsr.ui.home

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import javax.inject.Singleton

@HiltViewModel
class HomeViewModel @Inject constructor(

    private val historyDao: HistoryDao,
    private val goalDao: GoalDao,
    private val state: SavedStateHandle,
    private val preferencesManager: PreferencesManager,
    application: Application

) : AndroidViewModel(application) {

    private val mainEventChannel = Channel<MainEvent>()
    val mainEvent = mainEventChannel.receiveAsFlow()

    val preferencesFlow = preferencesManager.preferencesFlow

    val history = historyDao.getMostRecent().asLiveData()
    var historyUsable: History = History(time = 0, current = false)
    var historySave: Boolean = false

    var stepStore:Long =0
    //val numberStepsLive = LiveDataSensor()
    var autoupdate = false

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
            showInvalidStepsInputMessage("Invalid step input")
            return false
        }
        stepStore = historySteps.toLong()
        findHistory()
        return true
    }


    fun findHistory() = viewModelScope.launch {
        if (history == null) {
            historyDao.insert(History(current = true, time = System.currentTimeMillis()))

        }
        //checkHistory()
        mainEventChannel.send(MainEvent.HistoryFound)
    }

    // checks the current date and compares it to that of the last history input. If the current date
    //is newer than that of the stored one, a new history instance will be created. if not history will be updated
    fun checkHistory() = viewModelScope.launch {

        if (historyUsable != null) {
            val sdf = SimpleDateFormat("MM/dd/yy")

            //get saved date and current date
            val dateString: String = historyUsable.createDateFormat
            val date: Date = sdf.parse(dateString)
            val dateNow =
                DateFormat.getDateInstance(DateFormat.SHORT).format(System.currentTimeMillis())
            val dateNowParsed: Date = sdf.parse(dateNow)

            //check if the current date is after saved, if so create new else return that check is complete
            if (dateNowParsed.after(date)) {

                historyDao.insert(History(current = true, time = System.currentTimeMillis()))

                if (historySave)
                    historyDao.update(historyUsable.copy(current = false))
                else
                    historyDao.delete(historyUsable)

                mainEventChannel.send(MainEvent.DateChanged)
            } else
                mainEventChannel.send(MainEvent.HistoryDateChecked)
        }


    }

    //update steps in histiry and trigger events
    fun updateHistory(step:Long) = viewModelScope.launch {
        val oldHistory = historyUsable.copy()

        val updatedHistory = historyUsable.copy(
            steps = step + historyUsable.steps,
            goalSteps = goalUsable.steps.toLong(),
            goalName = goalUsable.name
        )

        historyDao.update(updatedHistory)
        mainEventChannel.send(MainEvent.ShowUndoAdd(oldHistory))
        mainEventChannel.send(MainEvent.UpdateProgressWheel(goalUsable, updatedHistory))
    }


    private fun showInvalidStepsInputMessage(text: String) = viewModelScope.launch {
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
            //if
            if (goalUsable.id == goal.id) {

                goalDao.update(goalUsable.copy(active = !goalUsable.active))
                historyDao.update(
                    historyUsable.copy(
                        goalSteps = goal.steps.toLong(),
                        goalName = goal.name
                    )
                )
                preferencesManager.updateGalID(0)



            } else {
                goalDao.update(goalUsable.copy(active = !goalUsable.active))
                goalDao.update(goal.copy(active = !goal.active))
                preferencesManager.updateGalID(goal.id)
                historyDao.update(
                    historyUsable.copy(
                        goalSteps = goal.steps.toLong(),
                        goalName = goal.name
                    )
                )
            }
        } else {
            goalDao.update(goal.copy(active = !goal.active))
            historyDao.update(
                historyUsable.copy(
                    goalSteps = goal.steps.toLong(),
                    goalName = goal.name
                )
            )
            preferencesManager.updateGalID(goal.id)
        }
    }



    sealed class MainEvent {
        data class ShowInvalidStepInput(val message: String) : MainEvent()
        data class UpdateProgressWheel(val goal: Goal, val history: History) : MainEvent()
        data class ShowUndoAdd(val history: History) : MainEvent()
        object HistoryFound : MainEvent()
        object HistoryDateChecked : MainEvent()
        object DateChanged : MainEvent()
    }



}