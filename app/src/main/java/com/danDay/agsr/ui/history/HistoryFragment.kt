package com.danDay.agsr.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.danDay.agsr.R
import com.danDay.agsr.data.History

import com.danDay.agsr.databinding.FragmentHistoryBinding
import com.danDay.agsr.ui.goals.GoalsFragmentDirections
import com.danDay.agsr.util.exhaustive
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.coroutines.flow.collect
import java.util.*


@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history), HistoryAdapter.OnItemClickListener {

    private val viewModel: HistoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val binding = FragmentHistoryBinding.bind(view)

        val historyAdapter = HistoryAdapter(this)

        binding.apply {
            recycler_view_history.apply {
                adapter = historyAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            addHistory.setOnClickListener {
                viewModel.onNewHistory()

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
                    val goal = historyAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onHistorySwipe(goal)
                }
            }).attachToRecyclerView(recyclerViewHistory)
        }

        viewModel.history.observe(viewLifecycleOwner) {
            historyAdapter.submitList(it)
        }

        viewModel.historyLast.observe(viewLifecycleOwner) {
            if(it!=null) {
                viewModel.historyLastIncert = it
            }
        }

        viewModel.historyCurrent.observe(viewLifecycleOwner){
            if(it!=null)
                viewModel.historyCurrentUsable = it
        }
        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.historyEvent.collect { event ->
                when (event) {
                    is HistoryViewModel.HistoryEvent.NavigateEditHistory -> {
                        val action =
                            HistoryFragmentDirections.actionHistoryFragmentToHistoryEditFragment(
                                event.history
                            )
                        findNavController().navigate(action)
                    }
                    is HistoryViewModel.HistoryEvent.NavigatePickDate -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToDatePicker()
                        findNavController().navigate(action)
                    }
                    is HistoryViewModel.HistoryEvent.created -> {
/*
                        val action =
                            HistoryFragmentDirections.actionHistoryFragmentToHistoryEditFragment(
                                viewModel.historyLastIncert
                            )
                        findNavController().navigate(action)

 */


                    }
                    is HistoryViewModel.HistoryEvent.ShowUndoDelete -> {
                        Snackbar.make(requireView(), "History Deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.history)
                            }.show()
                    }
                }.exhaustive

            }
        }

    }

    override fun onItemClick(history: History) {
        viewModel.onHistorySelected(history)
    }
}