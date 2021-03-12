package com.danDay.agsr.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danDay.agsr.data.HistoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val historyDao: HistoryDao,
) : ViewModel() {

    fun deleteAll() = viewModelScope.launch {
        Log.d("myTest", "Created viewModel and deleted")
        historyDao.deleteAll()
    }

}