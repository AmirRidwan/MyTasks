package com.example.mytasks_simpletaskmanager.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytasks_simpletaskmanager.R
import com.example.mytasks_simpletaskmanager.utils.model.ToDoData
import com.example.mytasks_simpletaskmanager.databinding.EachTodoItemBinding

class TaskAdapter(
    private var toDoList: MutableList<ToDoData>,
    private val listener: TaskAdapterInterface
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: EachTodoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = EachTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentItem = toDoList[position]
        holder.binding.todoTask.text = currentItem.task
        holder.binding.checkTaskDone.isChecked = currentItem.done

        holder.binding.checkTaskDone.setOnCheckedChangeListener { _, isChecked ->
            listener.onTaskCheckChanged(currentItem, isChecked)
        }

        holder.binding.editTask.setOnClickListener {
            listener.onEditItemClicked(currentItem, position)
        }

        holder.binding.deleteTask.setOnClickListener {
            listener.onDeleteItemClicked(currentItem, position)
        }
    }

    override fun getItemCount(): Int {
        return toDoList.size
    }

    interface TaskAdapterInterface {
        fun onDeleteItemClicked(toDoData: ToDoData, position: Int)
        fun onEditItemClicked(toDoData: ToDoData, position: Int)
        fun onTaskCheckChanged(toDoData: ToDoData, isChecked: Boolean)
    }
}