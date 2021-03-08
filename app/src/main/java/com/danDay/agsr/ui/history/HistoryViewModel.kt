package com.danDay.agsr.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.danDay.agsr.data.History
import com.danDay.agsr.data.HistoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {


    private val historyEventChannel = Channel<HistoryEvent>()
    val historyEvent = historyEventChannel.receiveAsFlow()



    fun onHistorySelected(history: History) = viewModelScope.launch {
        historyEventChannel.send(HistoryEvent.NavigateEditHistory(history))
    }

    val history = historyDao.getHistory().asLiveData()

    sealed class HistoryEvent{
        data class NavigateEditHistory(val history: History):HistoryEvent()
    }

}