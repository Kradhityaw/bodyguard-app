package com.example.bodyguard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bodyguard.databinding.ActivitySetupBinding
import org.json.JSONObject

class SetupActivity : AppCompatActivity() {
    lateinit var bind: ActivitySetupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.confirmBtn.setOnClickListener {
            if (bind.namaEt.text.toString() == "") {
                Toast.makeText(this@SetupActivity, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var shared = getSharedPreferences("userData", Context.MODE_PRIVATE)
            var editor = shared.edit()

            var userdata = JSONObject()
            userdata.put("nama", bind.namaEt.text.toString())

            editor.putString("user", userdata.toString())
            editor.apply()

            startActivity(Intent(this@SetupActivity, HomeActivity::class.java))
            finish()
        }
    }
}