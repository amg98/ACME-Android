package com.dam.acmeexplorer.listadapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.databinding.TravelItemBinding
import com.dam.acmeexplorer.databinding.TravelItemSmallBinding
import com.dam.acmeexplorer.extensions.formatted
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.utils.Units
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TravelListAdapter(private val context: Context, private val travels: List<Travel>, private val userTravels: MutableMap<String, Boolean>, private val travelDistances: MutableList<Double>, private val onClickItem: (pos: Int, isCheckbox: Boolean, checked: Boolean) -> Unit)
    : RecyclerView.Adapter<TravelListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TravelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, context, userTravels, onClickItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(travels[position], position, travelDistances[position])
    }

    override fun getItemCount(): Int = travels.size

    class ViewHolder(private val binding: TravelItemBinding, private val context: Context, private val userTravels: MutableMap<String, Boolean>, private val onClickItem: (pos: Int, isCheckbox: Boolean, checked: Boolean) -> Unit)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(travel: Travel, position: Int, distance: Double) {
            with(binding) {

                travelTitle.text = travel.title
                travelStartDate.text = context.getString(R.string.start_date, travel.startDate.formatted())
                travelEndDate.text = context.getString(R.string.end_date, travel.endDate.formatted())
                travelPrice.text = context.getString(R.string.price, travel.price)
                distanceText.text = if(distance < Units.MIN_DISTANCE) context.getString(R.string.loadingDistance) else context.getString(R.string.distanceText, DecimalFormat("#.##").format(distance))

                Picasso.with(context)
                        .load(travel.imagesURL[0])
                        .resize(300, 300)
                        .centerCrop()
                        .placeholder(R.drawable.ic_loading)
                        .error(R.drawable.ic_error)
                        .into(travelImage)

                checkBox.isChecked = userTravels.contains(travel.id)

                card.setOnClickListener {
                    onClickItem(position, false, false)
                }

                checkBox.setOnCheckedChangeListener { _, checked ->
                    onClickItem(position, true, checked)
                }
            }
        }
    }
}
