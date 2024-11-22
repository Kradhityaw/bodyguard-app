package com.example.bodyguard

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.bodyguard.databinding.FragmentAturanBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class AturanFragment : Fragment() {
    private lateinit var bind: FragmentAturanBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentAturanBinding.inflate(layoutInflater, container, false)

        loadData(true)

        theData()

        bind.detailBtnrefresh.setOnClickListener {
            theData()
            loadData(true)
        }

        return bind.root
    }

    fun theData() {
        val data = arguments?.getString("id")

        if (arguments?.getBoolean("isAturan") == false) {
            bind.detailToolbar.title = "Tips Berkendara"
        }

        bind.detailToolbar.setNavigationOnClickListener {
            fragmentManager?.popBackStack()
        }

        val queue = Volley.newRequestQueue(context)
        val url = "https://66e4467dd2405277ed13c1f0.mockapi.io/api/aturan/${data}"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val get = JSONObject(response)
                bind.detailText.text = get.getString("isi")
                bind.detailTitle.text = get.getString("judul")
                Glide.with(requireContext()).load(get.getString("image")).into(bind.detailImg)
                loadData(false)
            },
            {
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                bind.detailBtnrefresh.visibility = View.VISIBLE
            })

        queue.add(stringRequest)
    }

    private fun loadData(isload: Boolean) {
        if (isload) {
            bind.detailBtnrefresh.visibility = View.GONE
            bind.loadPg.visibility = View.VISIBLE
        } else {
            bind.loadPg.visibility = View.GONE
        }
    }
}