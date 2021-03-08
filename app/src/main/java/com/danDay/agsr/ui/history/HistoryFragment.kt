package com.danDay.agsr.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.danDay.agsr.R
import com.danDay.agsr.data.History

import com.danDay.agsr.databinding.FragmentHistoryBinding
import com.danDay.agsr.ui.goals.GoalsFragmentDirections
import com.danDay.agsr.util.exhaustive

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.coroutines.flow.collect


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
        }

        viewModel.history.observe(viewLifecycleOwner) {
            historyAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.historyEvent.collect { event->
                when(event)
                {
                    is HistoryViewModel.HistoryEvent.NavigateEditHistory -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToHistoryEditFragment(event.history)
                        findNavController().navigate(action)
                    }
                }.exhaustive

            }
        }

    }

    override fun onItemClick(history: History){
        viewModel.onHistorySelected(history)
    }
}