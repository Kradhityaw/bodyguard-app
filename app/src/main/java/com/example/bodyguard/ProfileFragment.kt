    package com.example.bodyguard

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bodyguard.databinding.FragmentProfileBinding
import org.json.JSONObject

    class ProfileFragment : Fragment() {
    lateinit var bind: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentProfileBinding.inflate(layoutInflater)
        var shared = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        var usersString = shared?.getString("user", "{}")
        var json = JSONObject(usersString)
        bind.nameProfTv.text =  json.getString("nama")

        bind.deteksipandangan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.flFragment, CameraFragment())
                .addToBackStack(null)
                .commit()
        }

        bind.about.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.flFragment, AboutFragment())
                .addToBackStack(null)
                .commit()
        }

        return bind.root
    }
}