package com.danDay.agsr.ui.goals

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danDay.agsr.data.Goal
import com.danDay.agsr.databinding.GoalCardBinding

class GoalsAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Goal, GoalsAdapter.GoalViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        Log.d("myLog", "create view")
        val binding = GoalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val currentGoal = getItem(position)
        holder.bind(currentGoal)
    }

    inner class GoalViewHolder(private val binding: GoalCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val goal = getItem(position)
                        listener.onItemClick(goal)
                    }
                }
                favouriteGoal.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val goal = getItem(position)
                        listener.onFavouriteClick(goal, favouriteGoal.isChecked)
                    }
                }

                activeToggle.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val goal = getItem(position)
                        listener.onActiveClick(goal)
                    }
                }
            }
        }

        fun bind(goal: Goal) {
            binding.apply {
                goalName.text = goal.name
                goalSteps.text = goal.steps.toString()
                favouriteGoal.isChecked = goal.favourite
                activeToggle.isChecked = goal.active

            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(goal: Goal)
        fun onFavouriteClick(goal: Goal, isChecked: Boolean)
        fun onActiveClick(goal: Goal)
    }

    class DiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal) =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean =
            oldItem == newItem

    }

}