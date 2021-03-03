package com.danDay.agsr.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.danDay.agsr.data.Goal
import com.danDay.agsr.data.GoalDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalsDao: GoalDao
) : ViewModel() {
    val searchQuery = MutableStateFlow("")

    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)

    private val goalFlow = combine(
        searchQuery,
        sortOrder
    ) { query, sortOrder ->
        Pair(query, sortOrder)
    }.flatMapLatest { (query, sortOrder) ->
        goalsDao.getTask(query, sortOrder)
    }
    val goals = goalFlow.asLiveData()


    fun onGoalSelected(goal: Goal){

    }
    fun onFavoriteCheckedChanged(goal: Goal, isChecked: Boolean) = viewModelScope.launch {
        goalsDao.update(goal.copy(favourite = isChecked))
    }

    fun onGoalSwipe(goal: Goal)= viewModelScope.launch {
        goalsDao.delete(goal)
    }

}

enum class SortOrder { BY_NAME, BY_FAVORITE, BY_DATE }