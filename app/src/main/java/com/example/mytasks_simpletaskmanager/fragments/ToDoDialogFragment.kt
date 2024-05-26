package com.example.mytasks_simpletaskmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mytasks_simpletaskmanager.R
import com.google.android.material.textfield.TextInputEditText

class ToDoDialogFragment : DialogFragment() {

    interface OnDialogNextBtnClickListener {
        fun saveTask(todoTask: String, todoEdit: TextInputEditText)
        fun updateTask(taskId: String, todoTask: String, todoEdit: TextInputEditText)
    }

    private var listener: OnDialogNextBtnClickListener? = null
    private var taskId: String? = null
    private var task: String? = null

    private lateinit var todoEdit: TextInputEditText
    private lateinit var todoClose: ImageView
    private lateinit var todoNextBtn: ImageView

    companion object {
        const val TAG = "ToDoDialogFragment"

        fun newInstance(taskId: String?, task: String?): ToDoDialogFragment {
            val frag = ToDoDialogFragment()
            val args = Bundle()
            args.putString("taskId", taskId)
            args.putString("task", task)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskId = it.getString("taskId")
            task = it.getString("task")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_to_do_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        todoEdit = view.findViewById(R.id.todoEt)
        todoClose = view.findViewById(R.id.todoClose)
        todoNextBtn = view.findViewById(R.id.todoNextBtn)

        if (task != null) {
            todoEdit.setText(task)
        }

        todoClose.setOnClickListener {
            dismiss()
        }

        todoNextBtn.setOnClickListener {
            val todoTask = todoEdit.text.toString().trim()
            if (todoTask.isEmpty()) {
                Toast.makeText(context, "Please enter a task", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (taskId == null) {
                listener?.saveTask(todoTask, todoEdit)
            } else {
                listener?.updateTask(taskId!!, todoTask, todoEdit)
            }
        }
    }

    fun setListener(listener: OnDialogNextBtnClickListener) {
        this.listener = listener
    }
}
