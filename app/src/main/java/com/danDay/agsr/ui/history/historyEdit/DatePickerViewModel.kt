package com.danDay.agsr.ui.history.historyEdit

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.danDay.agsr.data.History
import com.danDay.agsr.data.HistoryDao
import com.danDay.agsr.ui.history.HistoryFragmentDirections
import com.danDay.agsr.ui.history.HistoryViewModel
import com.danDay.agsr.util.exhaustive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.android.synthetic.main.history_card.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DatePickerViewModel @Inject constructor(
    private val historyDao: HistoryDao

): ViewModel(){
    private val DatePickerEventChannel = Channel<DateEvent>()
    val DatePickerEvent = DatePickerEventChannel.receiveAsFlow()

    lateinit var history: History

    fun setHistory(calendar: Calendar)=viewModelScope.launch{
        history = History(current = false, time = calendar.timeInMillis)
        historyDao.insert(history)

        //DatePickerEventChannel.send(DateEvent.NavigateEditHistory(history))
    }


    sealed class DateEvent{
        data class NavigateEditHistory(val history: History):DateEvent()
    }
}