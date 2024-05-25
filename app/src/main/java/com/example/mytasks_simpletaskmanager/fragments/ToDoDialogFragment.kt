import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mytasks_simpletaskmanager.databinding.FragmentToDoDialogBinding
import com.example.mytasks_simpletaskmanager.utils.model.ToDoData
import com.google.android.material.textfield.TextInputEditText

class ToDoDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentToDoDialogBinding
    private var listener: OnDialogNextBtnClickListener? = null
    private var taskId: String? = null
    private var todoTask: String? = null

    fun setListener(listener: OnDialogNextBtnClickListener) {
        this.listener = listener
    }

    companion object {
        const val TAG = "ToDoDialogFragment"

        @JvmStatic
        fun newInstance(taskId: String, task: String) =
            ToDoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("taskId", taskId)
                    putString("task", task)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentToDoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            taskId = args.getString("taskId")
            todoTask = args.getString("task")
            binding.todoEt.setText(todoTask)
        }

        binding.todoClose.setOnClickListener {
            dismiss()
        }

        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString()
            if (todoTask.isNotEmpty()) {
                if (taskId == null) {
                    listener?.saveTask(todoTask, binding.todoEt)
                } else {
                    taskId?.let { taskId ->
                        listener?.updateTask(taskId, todoTask, binding.todoEt)
                    }
                }
                dismiss()
            }
        }
    }

    interface OnDialogNextBtnClickListener {
        fun saveTask(todoTask: String, todoEdit: TextInputEditText)
        fun updateTask(taskId: String, todoTask: String, todoEdit: TextInputEditText)
    }
}