package com.example.kvalik

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.kvalik.data.AppDatabase
import com.example.kvalik.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfileBinding
    private lateinit var db: AppDatabase

    private var userId = 0
    private var username = "Пользователь"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = AppDatabase.getInstance(this)

        userId = intent.getIntExtra("userId", 0)
        username = intent.getStringExtra("username") ?: "Пользователь"

        // UI
        b.tvUsername.text = username
        b.tvUserId.text = "ID: $userId"

        // Кнопка назад
        b.btnBack.setOnClickListener { finish() }

        // Выход
        b.btnLogout.setOnClickListener {
            val i = Intent(this, AuthActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

        loadStats()
    }

    private fun loadStats() {
        b.progress.isVisible = true

        lifecycleScope.launch {
            val tasks = db.taskDao().getAllByUser(userId)

            val total = tasks.size
            val done = tasks.count { it.isDone }
            val active = total - done

            b.tvTotal.text = total.toString()
            b.tvActive.text = active.toString()
            b.tvDone.text = done.toString()

            b.progress.isVisible = false
        }
    }
}
