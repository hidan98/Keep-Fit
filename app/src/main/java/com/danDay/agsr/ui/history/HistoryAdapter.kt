package com.danDay.agsr.ui.history

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.math.MathUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danDay.agsr.R
import com.danDay.agsr.data.Goal
import com.danDay.agsr.data.History
import com.danDay.agsr.databinding.HistoryCardBinding
import com.danDay.agsr.ui.goals.GoalsAdapter


class HistoryAdapter(private val listener: HistoryAdapter.OnItemClickListener) :
    ListAdapter<History, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        Log.d("myLog", "create view")
        val binding = HistoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentHistory = getItem(position)
        holder.bind(currentHistory)
    }

    inner class HistoryViewHolder(private val binding: HistoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val history = getItem(position)
                        listener.onItemClick(history)
                    }
                }
            }
        }

        fun bind(history: History) {
            binding.apply {
                //historyId.text = history.id.toString()
                historyGoalSteps.text = "Goal: " + history.goalSteps.toString()
                historyGoalName.text = history.goalName
                historyDate.text = history.createDateFormat
                historySteps.text = "Steps: " + history.steps.toString()
                var percent: Float = 0F
                if (history.steps == 0L || history.goalSteps == 0L) {
                    percent = 0.toFloat()
                } else {
                    percent =
                        (((history.steps.toFloat() / history.goalSteps.toFloat()) * 100.toFloat()))
                }

                if (percent >= 100F) {
                    historyCompletionImg.setImageResource(R.drawable.ic_baseline_done_24)
                } else {
                    historyCompletionImg.setImageResource(R.drawable.ic_baseline_close_24)
                }
                historyPercent.text = percent.toString() + "%"
                percent = MathUtils.clamp(percent, 0.0f, 100.0f)
                historyProgress.activityCompletion.progress = percent.toInt()

            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(history: History)

    }

    class DiffCallback : DiffUtil.ItemCallback<History>() {
        override fun areItemsTheSame(oldItem: History, newItem: History) =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: History, newItem: History): Boolean =
            oldItem == newItem

    }
}