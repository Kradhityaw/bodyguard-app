package com.example.bodyguard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bodyguard.databinding.ActivitySplashBinding
import org.json.JSONObject

class SplashActivity : AppCompatActivity() {
    lateinit var bind: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySplashBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(bind.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var shared = getSharedPreferences("userData", Context.MODE_PRIVATE)
        var usersString = shared.getString("user", "{}")
//        var user = JSONObject(usersString)

        if (usersString == "{}") {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                finish()
            }, 3000)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                finish()
            }, 3000)
        }


    }
}