package com.danDay.agsr.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.danDay.agsr.R

import com.danDay.agsr.databinding.FragmentHistoryBinding

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_history.*


@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val viewModel: HistoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val binding = FragmentHistoryBinding.bind(view)

        val historyAdapter = HistoryAdapter()

        binding.apply {
            recycler_view_history.apply {
                adapter = historyAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        viewModel.history.observe(viewLifecycleOwner) {
            historyAdapter.submitList(it)
        }

    }
}