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
            showInvalidNameInputMessage("Name cannot be blank")
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

    fun checkHistory() = viewModelScope.launch {

        if (historyUsable != null) {
            val sdf = SimpleDateFormat("MM/dd/yy")

            val dateString: String = historyUsable.createDateFormat
            val date: Date = sdf.parse(dateString)
            val dateNow =
                DateFormat.getDateInstance(DateFormat.SHORT).format(System.currentTimeMillis())
            val dateNowParsed: Date = sdf.parse(dateNow)
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

    /*

    inner class LiveDataSensor() : LiveData<Long>(), SensorEventListener {
        private var stepsSend =0
        private var stepsLastSent =0
        private val sensorManager
            get() = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        override fun onSensorChanged(event: SensorEvent?) {


            val totalStepSinceReboot: Int = event?.values?.get(0)?.toInt() ?: 0

            val sdf = SimpleDateFormat("MM/dd/yy")
            if (sdf.parse(event?.timestamp.toString()).after(sdf.parse(historyUsable.createDateFormat))){

                updateStepStore(totalStepSinceReboot.toLong())
            }
            else{
                stepsSend = totalStepSinceReboot - stepsLastSent
            }


            if (event != null) {
                postValue(event.timestamp)
            }

            stepsLastSent = stepsSend




        }
        private fun updateStepStore(step:Long)=viewModelScope.launch {
            preferencesManager.updateStepCheck(step)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            TODO("Not yet implemented")
        }

        override fun onActive() {
            var stepsSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

            if (stepsSensor != null) {
                sensorManager.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }


        override fun onInactive() {
            sensorManager.unregisterListener(this)
        }
        fun turnOff() {
            sensorManager.unregisterListener(this)
        }

        fun getStep():Int{
            return stepsSend
        }

    }

     */

}