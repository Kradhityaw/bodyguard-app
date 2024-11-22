package com.example.bodyguard

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.bodyguard.databinding.ActivityHomeBinding
import com.google.android.material.color.DynamicColors

class HomeActivity : AppCompatActivity() {
    lateinit var bind: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(bind.root)

        replaceFragment(HomeFragment())

        bind.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bnHome -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.bnProfile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> {
                    false
                }
            }
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(bind.flFragment.id, fragment).commit()
    }

}