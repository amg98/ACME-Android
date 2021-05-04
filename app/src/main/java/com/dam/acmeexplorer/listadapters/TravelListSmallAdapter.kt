package com.dam.acmeexplorer.listadapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.databinding.TravelItemSmallBinding
import com.dam.acmeexplorer.models.Travel
import com.squareup.picasso.Picasso

class TravelListSmallAdapter(private val context: Context, private val travels: List<Travel>, private val userTravels: MutableMap<String, Boolean>, private val onClickItem: (pos: Int, isCheckbox: Boolean) -> Unit)
    : RecyclerView.Adapter<TravelListSmallAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TravelItemSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(context, binding, userTravels, onClickItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(travels[position], position)
    }

    override fun getItemCount(): Int = travels.size

    class ViewHolder(private val context: Context, private val binding: TravelItemSmallBinding, private val userTravels: MutableMap<String, Boolean>, private val onClickItem: (pos: Int, isCheckbox: Boolean) -> Unit)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(travel: Travel, position: Int) {
            with(binding) {

                travelTitle.text = travel.title
                Picasso.with(context)
                        .load(travel.imagesURL[0])
                        .resize(300, 300)
                        .centerCrop()
                        .placeholder(R.drawable.ic_loading)
                        .error(R.drawable.ic_error)
                        .into(travelImage)

                checkBox.isChecked = userTravels.contains(travel.id)

                card.setOnClickListener {
                    onClickItem(position, false)
                }

                checkBox.setOnCheckedChangeListener { _, _ ->
                    onClickItem(position, true)
                }
            }
        }
    }
}
