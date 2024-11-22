package com.example.bodyguard

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.bodyguard.databinding.AturanCardBinding
import com.example.bodyguard.databinding.FragmentHomeBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class HomeFragment : Fragment() {
    private lateinit var bind: FragmentHomeBinding
    lateinit var filtered: JSONArray
    lateinit var filteredTips: JSONArray

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentHomeBinding.inflate(layoutInflater, container, false)

        isLoading(true)

        bind.refreshAturan.setOnClickListener {
            dataAturan()
            isLoading(true)
        }

        bind.refreshTips.setOnClickListener {
            dataAturan()
            isLoading(true)
        }

        dataAturan()

        var shared = requireContext().getSharedPreferences("userData", Context.MODE_PRIVATE)
        var usersString = shared.getString("user", "{}")
        var json = JSONObject(usersString)

        bind.nameTv.text = json.getString("nama")

        bind.mySwitch.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                requestCameraPermission()
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Cara Penggunaan")
                builder.setMessage("Pastikan perizinan kamera diizinkan, dan juga pastikan pandangan mata mengarah ke kamera handphone")
                    .setPositiveButton("Start") { dialog, id ->
                        parentFragmentManager.beginTransaction()
                            .add(R.id.flFragment, CameraFragment())
                            .addToBackStack(null)
                            .commit()

                        bind.mySwitch.isChecked = false
                    }
                // Create the AlertDialog object and return it.
                builder.create()
                builder.show()
            }
        }

        bind.cv1.setOnClickListener {
            try {
                Runtime.data = filtered

                parentFragmentManager.beginTransaction()
                    .add(R.id.flFragment, ListFragment())
                    .addToBackStack(null)
                    .commit()
            } catch (e : Exception) {
                Toast.makeText(requireContext(), "Kesalahan terhubung ke server", Toast.LENGTH_SHORT).show()
            }
        }

        bind.cardView2.setOnClickListener {
            try {
                Runtime.data = filteredTips

                parentFragmentManager.beginTransaction()
                    .add(R.id.flFragment, ListFragment())
                    .addToBackStack(null)
                    .commit()
            } catch (e : Exception) {
                Toast.makeText(requireContext(), "Kesalahan terhubung ke server", Toast.LENGTH_SHORT).show()
            }
        }

        return bind.root
    }

    private val cameraRequestCode = 1001

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), cameraRequestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin kamera diberikan
            } else {
                // Izin ditolak
            }
        }
    }

    fun dataAturan() {
        val queue = Volley.newRequestQueue(context)
        val url = "https://66e4467dd2405277ed13c1f0.mockapi.io/api/aturan"

        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val data = JSONArray(response)
                filtered = JSONArray()
                filteredTips = JSONArray()

                for (e in 0 until data.length()) {
                    val datas = data.getJSONObject(e)
                    if (datas.getBoolean("isAturan")) {
                        filtered.put(datas)
                    } else {
                        filteredTips.put(datas)
                    }
                }

                bind.aturanRv.adapter = object : RecyclerView.Adapter<AturanCardVh>() {
                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): AturanCardVh {
                        val holder = AturanCardBinding.inflate(layoutInflater, parent, false)
                        return AturanCardVh(holder)
                    }

                    override fun getItemCount(): Int = filtered.length()

                    override fun onBindViewHolder(holder: AturanCardVh, position: Int) {
                        val get = filtered.getJSONObject(position)


                        holder.binding.aturanTitle.text = get.getString("judul")
                        Glide.with(requireContext()).load(get.getString("image")).into(holder.binding.aturanImg)


                        holder.itemView.setOnClickListener {
                            val fragment = AturanFragment()
                            val bundle = Bundle()
                            bundle.putString("id", get.getString("id"))
                            bundle.putBoolean("isAturan", true)
                            fragment.arguments = bundle

                            parentFragmentManager.beginTransaction()
                                .add(R.id.flFragment, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
                bind.aturanRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

                bind.tipsRv.adapter = object : RecyclerView.Adapter<AturanCardVh>() {
                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): AturanCardVh {
                        val holder = AturanCardBinding.inflate(layoutInflater, parent, false)
                        return AturanCardVh(holder)
                    }

                    override fun getItemCount(): Int = filteredTips.length()

                    override fun onBindViewHolder(holder: AturanCardVh, position: Int) {
                        val get = filteredTips.getJSONObject(position)

                        holder.binding.aturanTitle.text = get.getString("judul")
                        Glide.with(requireContext()).load(get.getString("image")).into(holder.binding.aturanImg)

                        holder.itemView.setOnClickListener {
                            val fragment = AturanFragment()
                            val bundle = Bundle()
                            bundle.putString("id", get.getString("id"))
                            bundle.putBoolean("isAturan", false)
                            fragment.arguments = bundle

                            parentFragmentManager.beginTransaction()
                                .add(R.id.flFragment, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
                bind.tipsRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                isLoading(false)
            },
            {
                bind.refreshTips.visibility = View.VISIBLE
                bind.refreshAturan.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
                isLoading(false)
            })

        queue.add(stringRequest)
    }

    private fun isLoading(isloading: Boolean) {
        if (isloading) {
            bind.aturanRv.visibility = View.GONE
            bind.tipsRv.visibility = View.GONE
            bind.aturanPb.visibility = View.VISIBLE
            bind.tipsPb.visibility = View.VISIBLE
            bind.refreshTips.visibility = View.GONE
            bind.refreshAturan.visibility = View.GONE
        } else {
            bind.aturanRv.visibility = View.VISIBLE
            bind.tipsRv.visibility = View.VISIBLE
            bind.aturanPb.visibility = View.GONE
            bind.tipsPb.visibility = View.GONE
        }
    }

    class AturanCardVh(val binding: AturanCardBinding) : RecyclerView.ViewHolder(binding.root)
}