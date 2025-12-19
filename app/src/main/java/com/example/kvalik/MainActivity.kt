package com.example.kvalik

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kvalik.data.AppDatabase
import com.example.kvalik.data.TaskEntity
import com.example.kvalik.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TaskAdapter

    private var userId: Int = 0
    private var allTasks: List<TaskEntity> = emptyList()

    private enum class Filter { ALL, ACTIVE, DONE }
    private var filter: Filter = Filter.ALL

    private val addTaskLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == RESULT_OK) loadTasks()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        userId = intent.getIntExtra("userId", 0)
        db = AppDatabase.getInstance(this)

        adapter = TaskAdapter(
            onChecked = { task, checked ->
                lifecycleScope.launch {
                    db.taskDao().update(task.copy(isDone = checked))
                    loadTasks()
                }
            },
            onClick = { task ->
                val i = Intent(this, AddTaskActivity::class.java)
                i.putExtra("userId", userId)
                i.putExtra("taskId", task.id)
                addTaskLauncher.launch(i)
            }
        )

        b.rvTasks.layoutManager = LinearLayoutManager(this)
        b.rvTasks.adapter = adapter

        // ---------- вкладки ----------
        b.tabLayout.removeAllTabs()
        b.tabLayout.addTab(b.tabLayout.newTab().setText("Все"))
        b.tabLayout.addTab(b.tabLayout.newTab().setText("Активные"))
        b.tabLayout.addTab(b.tabLayout.newTab().setText("Выполненные"))

        b.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filter = when (tab?.position ?: 0) {
                    1 -> Filter.ACTIVE
                    2 -> Filter.DONE
                    else -> Filter.ALL
                }
                applyFilter()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // ---------- добавить задачу ----------
        b.btnAddTask.setOnClickListener {
            val i = Intent(this, AddTaskActivity::class.java)
            i.putExtra("userId", userId)
            addTaskLauncher.launch(i)
        }

        // ---------- профиль ----------
        b.btnProfile.setOnClickListener {
            val i = Intent(this, ProfileActivity::class.java)
            i.putExtra("userId", userId)
            startActivity(i)
        }

        // ---------- свайп удалить + Undo ----------
        val swipe = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (pos == RecyclerView.NO_POSITION) return

                val task = adapter.getItem(pos)

                lifecycleScope.launch {
                    db.taskDao().delete(task)
                    loadTasks()

                    Snackbar.make(b.root, "Задача удалена", Snackbar.LENGTH_LONG)
                        .setAction("ОТМЕНА") {
                            lifecycleScope.launch {
                                db.taskDao().insert(task.copy(id = 0))
                                loadTasks()
                            }
                        }
                        .show()
                }
            }
        }

        ItemTouchHelper(swipe).attachToRecyclerView(b.rvTasks)

        loadTasks()
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            allTasks = db.taskDao().getAllByUser(userId)
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filtered = when (filter) {
            Filter.ALL -> allTasks
            Filter.ACTIVE -> allTasks.filter { !it.isDone }
            Filter.DONE -> allTasks.filter { it.isDone }
        }
        adapter.submit(filtered)
    }
}
