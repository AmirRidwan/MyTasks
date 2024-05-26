package com.example.mytasks_simpletaskmanager.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mytasks_simpletaskmanager.R
import com.example.mytasks_simpletaskmanager.databinding.FragmentHomeBinding
import com.example.mytasks_simpletaskmanager.utils.adapter.TaskAdapter
import com.example.mytasks_simpletaskmanager.utils.model.ToDoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(), ToDoDialogFragment.OnDialogNextBtnClickListener,
    TaskAdapter.TaskAdapterInterface {

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
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        getTaskFromFirebase()

        binding.addTaskFab.setOnClickListener {
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

        binding.profileAvatar.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_profileViewFragment)
        }

// Load profile image from Firestore
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(authId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                val profileImageUrl = document.getString("profileImageUrl")
                profileImageUrl?.let { url ->
                    Glide.with(this).load(url).circleCrop().into(binding.profileAvatar)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Failed to load profile image: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun getTaskFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                toDoItemList.clear()
                for (dataSnapshot in snapshot.children) {
                    val toDoData = dataSnapshot.getValue(ToDoData::class.java)
                    toDoData?.let { toDoItemList.add(it) }
                }
                taskAdapter.notifyDataSetChanged()

                // Show or hide the empty list message
                if (toDoItemList.isEmpty()) {
                    binding.emptyListMessage.visibility = View.VISIBLE
                } else {
                    binding.emptyListMessage.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                context?.let {
                    Toast.makeText(it, error.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser?.uid ?: ""

        if (authId.isEmpty()) {
            navController.navigate(R.id.action_homeFragment_to_signInFragment)
            return
        }

        database =
            FirebaseDatabase.getInstance("https://mytasks-9c536-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child(
                "Tasks"
            ).child(authId)

        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)

        toDoItemList = mutableListOf()
        taskAdapter = TaskAdapter(toDoItemList, this)
        binding.mainRecyclerView.adapter = taskAdapter
    }

    private fun logout() {
        auth.signOut()
        navController.navigate(R.id.action_homeFragment_to_signInFragment)
        Toast.makeText(context, "You're logged out", Toast.LENGTH_SHORT).show()
    }

    override fun saveTask(todoTask: String, todoEdit: TextInputEditText) {
        Log.d(TAG, "save Task called with task: $todoTask")
        val taskId = database.push().key ?: return
        val task = ToDoData(taskId, todoTask, false)
        database.child(taskId).setValue(task)
            .addOnSuccessListener {
                Log.d(TAG, "Task added successfully")
                Toast.makeText(context, "Task Added Successfully", Toast.LENGTH_SHORT).show()
                todoEdit.text = null
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add task", e)
                Toast.makeText(context, "Failed to add task: $e", Toast.LENGTH_SHORT).show()
            }

        frag?.dismiss()
    }

    override fun updateTask(taskId: String, todoTask: String, todoEdit: TextInputEditText) {
        val task = mapOf("task" to todoTask)

        database.child(taskId).updateChildren(task)
            .addOnSuccessListener {
                Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating task: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }

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

    override fun onTaskCheckChanged(toDoData: ToDoData, isChecked: Boolean) {
        Log.d(TAG, "Task ${toDoData.taskId} check changed to $isChecked")
        database.child(toDoData.taskId).child("done").setValue(isChecked)
            .addOnSuccessListener {
                val status = if (isChecked) "completed" else "marked as incomplete"
                Toast.makeText(context, "Task ${status} successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating task: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}
