package com.danDay.agsr.ui.goals

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.danDay.agsr.R
import com.danDay.agsr.data.Goal
import com.danDay.agsr.databinding.FragmentGoalsBinding
import com.danDay.agsr.util.onQueryTextChanged


import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_goals.*

@AndroidEntryPoint
class GoalsFragment : Fragment(R.layout.fragment_goals), GoalsAdapter.OnItemClickListener {

    private val viewModel: GoalsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("myLog", "fragment made")

        val binding = FragmentGoalsBinding.bind(view)

        val goalsAdapter = GoalsAdapter(this)

        binding.apply {
            recycler_view_goals.apply {
                adapter = goalsAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
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
            }).attachToRecyclerView(recyclerViewGoals)
        }

        viewModel.goals.observe(viewLifecycleOwner){
            goalsAdapter.submitList(it)
        }

        setHasOptionsMenu(true)

    }

    override fun onItemClick(goal: Goal) {
        viewModel.onGoalSelected(goal)
    }

    override fun onFavouriteClick(goal: Goal, isChecked: Boolean) {
        viewModel.onFavoriteCheckedChanged(goal, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.goal_frag_menu, menu)

        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.sort_by_name->{
                viewModel.sortOrder.value = SortOrder.BY_NAME
                true
            }
            R.id.sort_by_favourites->{
                viewModel.sortOrder.value = SortOrder.BY_FAVORITE
                true
            }
            R.id.sort_by_date->{
                viewModel.sortOrder.value= SortOrder.BY_DATE
                true
            }
            else->super.onOptionsItemSelected(item)

        }
    }


}