package com.danDay.agsr.ui.history

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danDay.agsr.data.History
import com.danDay.agsr.databinding.HistoryCardBinding


class HistoryAdapter : ListAdapter<History, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        Log.d("myLog", "create view")
        val binding  = HistoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentHistory = getItem(position)
        holder.bind(currentHistory)
    }

    class HistoryViewHolder(private val binding: HistoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: History) {
            binding.apply {
                historyId.text = history.id.toString()

            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<History>(){
        override fun areItemsTheSame(oldItem: History, newItem: History) =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: History, newItem: History): Boolean =
            oldItem == newItem

    }
}