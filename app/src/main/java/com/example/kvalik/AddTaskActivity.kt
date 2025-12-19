package com.example.kvalik

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kvalik.data.AppDatabase
import com.example.kvalik.data.TaskEntity
import com.example.kvalik.databinding.ActivityAddTaskBinding
import kotlinx.coroutines.launch

class AddTaskActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddTaskBinding
    private lateinit var db: AppDatabase

    private var userId: Int = 0
    private var taskId: Int = 0
    private var editingTask: TaskEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = AppDatabase.getInstance(this)

        userId = intent.getIntExtra("userId", 0)
        taskId = intent.getIntExtra("taskId", 0)

        if (taskId != 0) {
            lifecycleScope.launch {
                editingTask = db.taskDao().getById(taskId)
                val t = editingTask ?: return@launch

                runOnUiThread {
                    b.tvTitle.text = "Редактирование"
                    b.etTitle.setText(t.title)
                    b.etDesc.setText(t.description)

                    when (t.priority) {
                        0 -> b.rbLow.isChecked = true
                        1 -> b.rbMedium.isChecked = true
                        2 -> b.rbHigh.isChecked = true
                    }

                    b.btnSave.text = "Сохранить"
                }
            }
        }

        b.btnSave.setOnClickListener {
            val title = b.etTitle.text?.toString()?.trim().orEmpty()
            val desc = b.etDesc.text?.toString()?.trim().orEmpty()

            if (title.isEmpty()) {
                toast("Введите название")
                return@setOnClickListener
            }

            val priority = when {
                b.rbHigh.isChecked -> 2
                b.rbMedium.isChecked -> 1
                else -> 0
            }

            lifecycleScope.launch {
                val old = editingTask

                if (old == null) {
                    db.taskDao().insert(
                        TaskEntity(
                            userId = userId,
                            title = title,
                            description = desc,
                            priority = priority,
                            isDone = false
                        )
                    )
                } else {
                    db.taskDao().update(
                        old.copy(
                            title = title,
                            description = desc,
                            priority = priority
                        )
                    )
                }

                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun toast(msg: String) {
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }
}
