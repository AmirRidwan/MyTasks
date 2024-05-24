package com.example.mytasks_simpletaskmanager

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mytasks_simpletaskmanager.databinding.ActivityAddEditTaskBinding
import com.example.mytasks_simpletaskmanager.model.Task
import com.google.firebase.firestore.FirebaseFirestore

class AddEditTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditTaskBinding
    private val db = FirebaseFirestore.getInstance()
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskId = intent.getStringExtra("TASK_ID")

        if (taskId != null) {
            db.collection("tasks").document(taskId!!).get().addOnSuccessListener { document ->
                if (document != null) {
                    binding.taskTitleInput.setText(document.getString("title"))
                    binding.taskDescriptionInput.setText(document.getString("description"))
                }
            }
        }

        binding.saveButton.setOnClickListener {
            val title = binding.taskTitleInput.text.toString()
            val description = binding.taskDescriptionInput.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                val task = Task(title, description)

                if (taskId == null) {
                    db.collection("tasks").add(task).addOnSuccessListener {
                        Toast.makeText(this, "Task added successfully.", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error adding task.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    db.collection("tasks").document(taskId!!).set(task).addOnSuccessListener {
                        Toast.makeText(this, "Task updated successfully.", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error updating task.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
