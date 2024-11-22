package com.example.bodyguard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bodyguard.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    lateinit var bind: FragmentAboutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentAboutBinding.inflate(layoutInflater)
        bind.aboutTb.setNavigationOnClickListener {
            fragmentManager?.popBackStack()
        }

        return bind.root
    }
}