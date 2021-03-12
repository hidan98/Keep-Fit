package com.danDay.agsr.ui.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.danDay.agsr.data.History
import com.danDay.agsr.data.HistoryDao
import com.danDay.agsr.ui.addeditgoal.ADD_TASK_RESULT_OK
import com.danDay.agsr.ui.addeditgoal.EDIT_TASK_RESULT_OK
import com.danDay.agsr.ui.goals.GoalsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {


    private val historyEventChannel = Channel<HistoryEvent>()
    val historyEvent = historyEventChannel.receiveAsFlow()
    lateinit var historyLastIncert: History
    val historyCurrent = historyDao.getCurrent().asLiveData()
    lateinit var historyCurrentUsable: History


    fun onHistorySelected(history: History) = viewModelScope.launch {


        if (history.id != historyCurrentUsable.id)
            historyEventChannel.send(HistoryEvent.NavigateEditHistory(history))


    }

    fun onNewHistory() = viewModelScope.launch {
        historyEventChannel.send(HistoryEvent.NavigatePickDate)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> Log.d("test", "added")
            EDIT_TASK_RESULT_OK -> Log.d(
                "test",
                "updated"
            )//showGoalConfirmationMessage("Goal updated")
        }
    }

    private fun createdNew() = viewModelScope.launch {
        historyEventChannel.send(HistoryEvent.created)
    }

    fun onHistorySwipe(history: History) = viewModelScope.launch {
        historyDao.delete(history)
        historyEventChannel.send(HistoryEvent.ShowUndoDelete(history))
    }

    fun onUndoDeleteClick(history: History) = viewModelScope.launch {
        historyDao.insert(history)
    }

    val historyLast = historyDao.getLastHistory().asLiveData()
    val history = historyDao.getHistoryOrder().asLiveData()

    sealed class HistoryEvent {
        data class NavigateEditHistory(val history: History) : HistoryEvent()
        object NavigatePickDate : HistoryEvent()
        object created : HistoryEvent()
        data class ShowUndoDelete(val history: History) : HistoryEvent()
    }

}