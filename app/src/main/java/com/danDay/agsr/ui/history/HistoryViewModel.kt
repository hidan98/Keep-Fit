package com.danDay.agsr.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.danDay.agsr.data.HistoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {

    val history = historyDao.getHistory().asLiveData()
}