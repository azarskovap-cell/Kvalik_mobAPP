package com.example.kvalik

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kvalik.data.AppDatabase
import com.example.kvalik.data.UserEntity
import com.example.kvalik.databinding.ActivityAuthBinding
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var b: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(b.root)

        val db = AppDatabase.getInstance(this)

        b.btnLogin.setOnClickListener {
            val login = b.etLogin.text?.toString()?.trim().orEmpty()
            val pass = b.etPassword.text?.toString()?.trim().orEmpty()

            if (login.isEmpty() || pass.isEmpty()) {
                toast("Заполни логин и пароль")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = db.userDao().findByLogin(login)

                if (user == null || user.password != pass) {
                    toast("Неверный логин или пароль")
                } else {
                    // временно переходим в MainActivity
                    val i = Intent(this@AuthActivity, MainActivity::class.java)
                    i.putExtra("userId", user.id)
                    startActivity(i)
                    finish()
                }
            }
        }

        b.btnRegister.setOnClickListener {
            val login = b.etLogin.text?.toString()?.trim().orEmpty()
            val pass = b.etPassword.text?.toString()?.trim().orEmpty()

            if (login.isEmpty() || pass.isEmpty()) {
                toast("Заполни логин и пароль")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val exists = db.userDao().findByLogin(login)
                if (exists != null) {
                    toast("Такой логин уже существует")
                    return@launch
                }

                db.userDao().insert(UserEntity(login = login, password = pass))
                toast("Аккаунт создан! Теперь войди.")
            }
        }
    }

    private fun toast(msg: String) {
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }
}
