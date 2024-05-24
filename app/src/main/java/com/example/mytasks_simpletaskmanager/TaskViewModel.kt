package com.example.mytasks_simpletaskmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mytasks_simpletaskmanager.model.Task
import com.google.firebase.firestore.FirebaseFirestore

class TaskViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _allTasks = MutableLiveData<List<Task>>()
    val allTasks: LiveData<List<Task>> = _allTasks

    init {
        db.collection("tasks").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                return@addSnapshotListener
            }
            val taskList = snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
            _allTasks.postValue(taskList)
        }
    }
}
