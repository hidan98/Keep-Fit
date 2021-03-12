package com.danDay.agsr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.danDay.agsr.data.History
import com.danDay.agsr.data.HistoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class stepJobServiceViewModel @Inject constructor(
    private val historyDao: HistoryDao
) :ViewModel() {

    val current =historyDao.getCurrent().asLiveData(viewModelScope.coroutineContext)

    fun updateHistory(steps: Int) = viewModelScope.launch{

        val updatedHistory = current.value?.copy(steps = current.value!!.steps+steps)
        if (updatedHistory != null) {
            historyDao.update(updatedHistory)
        }


    }
}