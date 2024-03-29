package com.danDay.agsr.ui.goals

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.danDay.agsr.R
import com.danDay.agsr.data.Goal
import com.danDay.agsr.data.SortOrder
import com.danDay.agsr.databinding.FragmentGoalsBinding
import com.danDay.agsr.util.exhaustive
import com.danDay.agsr.util.onQueryTextChanged
import kotlinx.coroutines.flow.collect
import com.google.android.material.snackbar.Snackbar


import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_goals.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GoalsFragment : Fragment(R.layout.fragment_goals), GoalsAdapter.OnItemClickListener {

    private val viewModel: GoalsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("myLog", "fragment made")

        val binding = FragmentGoalsBinding.bind(view)

        val goalsAdapter = GoalsAdapter(this)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        viewModel.allowGoalEdit = sharedPreferences.getBoolean("Goal", true)

        binding.apply {
            recycler_view_goals.apply {
                adapter = goalsAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val goal = goalsAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onGoalSwipe(goal)
                }

                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    val goal = goalsAdapter.currentList[viewHolder.adapterPosition]
                    if (goal.active) {
                        Snackbar.make(requireView(), "Can't delete active goal!", Snackbar.LENGTH_SHORT).show()
                        return 0
                    }
                    return super.getMovementFlags(recyclerView, viewHolder)
                }
            }).attachToRecyclerView(recyclerViewGoals)

            addTaskFab.setOnClickListener {
                viewModel.onAddNewGoalClick()
            }
        }


        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewModel.goals.observe(viewLifecycleOwner) {
            goalsAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.goalEvent.collect { event ->
                when (event) {
                    is GoalsViewModel.GoalEvent.ShowUndoDelete -> {
                        Snackbar.make(requireView(), "Goal Deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.goal)
                            }.show()
                    }
                    is GoalsViewModel.GoalEvent.NavigateAddGoal -> {
                        val action =
                            GoalsFragmentDirections.actionGoalsFragmentToAddEditGoalDialogeFragment()
                        findNavController().navigate(action)
                    }
                    is GoalsViewModel.GoalEvent.NavigateEditGoal -> {
                        val action =
                            GoalsFragmentDirections.actionGoalsFragmentToAddEditGoalDialogeFragment(
                                event.goal
                            )
                        findNavController().navigate(action)
                    }
                    is GoalsViewModel.GoalEvent.ShowGoalSavedConfirmation -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is GoalsViewModel.GoalEvent.ShowCantEditGoal -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive

            }
        }

        setHasOptionsMenu(true)

    }

    override fun onItemClick(goal: Goal) {
        viewModel.onGoalSelected(goal)
    }

    override fun onFavouriteClick(goal: Goal, isChecked: Boolean) {
        viewModel.onFavoriteCheckedChanged(goal, isChecked)
    }

    override fun onActiveClick(goal: Goal) {
        viewModel.onActiveSelected(goal)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.goal_frag_menu, menu)

        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.sort_by_favourites -> {
                viewModel.onSortOrderSelected(SortOrder.BY_FAVORITE)
                true
            }
            R.id.sort_by_date -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }


}