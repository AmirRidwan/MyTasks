package com.example.mytasks_simpletaskmanager.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytasks_simpletaskmanager.AddEditTaskActivity
import com.example.mytasks_simpletaskmanager.R
import com.example.mytasks_simpletaskmanager.model.Task
import kotlinx.android.synthetic.main.item_task.view.*

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasks = emptyList<Task>()

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = tasks[position]
        holder.itemView.taskTitle.text = currentTask.title
        holder.itemView.taskDescription.text = currentTask.description

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddEditTaskActivity::class.java)
            intent.putExtra("TASK_ID", currentTask.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }
}
