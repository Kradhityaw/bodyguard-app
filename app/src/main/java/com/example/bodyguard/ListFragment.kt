package com.example.bodyguard

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bodyguard.databinding.AturanCardBinding
import com.example.bodyguard.databinding.FragmentListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class ListFragment : Fragment() {
    lateinit var bind: FragmentListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentListBinding.inflate(layoutInflater, container, false)

        bind.listToolbar.setNavigationOnClickListener {
            fragmentManager?.popBackStack()
        }

        Log.d("datass", Runtime.data.toString())

        bind.listRv.adapter = object : RecyclerView.Adapter<AturanCardVh>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): AturanCardVh {
                val holder = AturanCardBinding.inflate(layoutInflater, parent, false)
                return AturanCardVh(holder)
            }

            override fun getItemCount(): Int = Runtime.data.length()

            override fun onBindViewHolder(holder: AturanCardVh, position: Int) {
                val get = Runtime.data.getJSONObject(position)

                GlobalScope.launch(Dispatchers.IO) {
                    val img = BitmapFactory.decodeStream(URL(get.getString("image")).openStream())
                    GlobalScope.launch(Dispatchers.Main) {
                        holder.binding.aturanTitle.text = get.getString("judul")
                        holder.binding.aturanImg.setImageBitmap(img)
                    }
                }

                if (!get.getBoolean("isAturan")) bind.listToolbar.title = "Tips Berkendara"

                holder.itemView.setOnClickListener {
                    val fragment = AturanFragment()
                    val bundle = Bundle()
                    bundle.putString("id", get.getString("id"))
                    bundle.putBoolean("isAturan", get.getBoolean("isAturan"))
                    fragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .add(R.id.flFragment, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
        bind.listRv.layoutManager = GridLayoutManager(context, 2)

        return bind.root
    }

    class AturanCardVh(val binding: AturanCardBinding) : RecyclerView.ViewHolder(binding.root)

}