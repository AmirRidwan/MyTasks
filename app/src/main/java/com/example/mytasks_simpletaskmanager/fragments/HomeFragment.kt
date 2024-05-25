package com.example.mytasks_simpletaskmanager.fragments

import ToDoDialogFragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mytasks_simpletaskmanager.R
import com.example.mytasks_simpletaskmanager.databinding.FragmentHomeBinding
import com.example.mytasks_simpletaskmanager.utils.adapter.TaskAdapter
import com.example.mytasks_simpletaskmanager.utils.model.ToDoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(), ToDoDialogFragment.OnDialogNextBtnClickListener, TaskAdapter.TaskAdapterInterface {

    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private var frag: ToDoDialogFragment? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var navController: NavController

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var toDoItemList: MutableList<ToDoData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        //get data from firebase
        getTaskFromFirebase()

        binding.addTaskBtn.setOnClickListener {
            if (frag != null)
                childFragmentManager.beginTransaction().remove(frag!!).commit()
            frag = ToDoDialogFragment()
            frag!!.setListener(this)

            frag!!.show(
                childFragmentManager,
                ToDoDialogFragment.TAG
            )
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun getTaskFromFirebase() {
        Firebase.firestore.collection("tasks")
            .document(authId)
            .collection("task")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.let {
                    toDoItemList.clear()
                    for (doc in snapshot.documents) {
                        val taskId = doc.id
                        val task = doc.getString("task")
                        if (taskId != null && task != null) {
                            val toDoData = ToDoData(taskId, task)
                            toDoItemList.add(toDoData)
                        }
                    }
                    taskAdapter.notifyDataSetChanged()
                }
            }
    }


    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser?.uid ?: ""

        // Ensure user is authenticated
        if (authId.isEmpty()) {
            navController.navigate(R.id.action_homeFragment_to_signInFragment)
            return
        }

        database = Firebase.database.reference.child("Tasks").child(authId)

        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)

        toDoItemList = mutableListOf()
        taskAdapter = TaskAdapter(toDoItemList)
        taskAdapter.setListener(this)
        binding.mainRecyclerView.adapter = taskAdapter
    }

    private fun logout() {
        auth.signOut()
        navController.navigate(R.id.action_homeFragment_to_signInFragment)
    }

    override fun saveTask(todoTask: String, todoEdit: TextInputEditText) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val taskId = db.collection("tasks").document().id // Generate a unique ID for the task
        val task = hashMapOf(
            "userId" to user?.uid,
            "taskId" to taskId,
            "task" to todoTask
        )

        db.collection("tasks")
            .document(taskId)
            .set(task)
            .addOnSuccessListener {
                Toast.makeText(context, "Task Added Successfully", Toast.LENGTH_SHORT).show()
                todoEdit.text = null
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to add task: $e", Toast.LENGTH_SHORT).show()
            }

        frag?.dismiss()
    }


    override fun updateTask(taskId: String, todoTask: String, todoEdit: TextInputEditText) {
        // Update the task in Firestore database using the provided taskId
        val db = Firebase.firestore
        val taskRef = db.collection("Tasks").document(taskId)
        taskRef.update("Task", todoTask)
            .addOnSuccessListener {
                Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating task: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Dismiss the dialog
        frag?.dismiss()
    }


    override fun onDeleteItemClicked(toDoData: ToDoData, position: Int) {
        database.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditItemClicked(toDoData: ToDoData, position: Int) {
        if (frag != null)
            childFragmentManager.beginTransaction().remove(frag!!).commit()

        frag = ToDoDialogFragment.newInstance(toDoData.taskId, toDoData.task)
        frag!!.setListener(this)
        frag!!.show(
            childFragmentManager,
            ToDoDialogFragment.TAG
        )
    }
}
