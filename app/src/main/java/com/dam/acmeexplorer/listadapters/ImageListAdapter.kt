package com.dam.acmeexplorer.listadapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.databinding.ImageItemBinding
import com.squareup.picasso.Picasso

class ImageListAdapter(private val images: List<String>, private val onClickItem: (pos: Int) -> Unit)
    : RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClickItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size

    class ViewHolder(private val binding: ImageItemBinding, private val onClickItem: (pos: Int) -> Unit)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageURL: String, position: Int) {
            with(binding) {

                if(imageURL == "") {
                    travelImage.setImageResource(R.drawable.ic_add)
                } else {
                    travelImage.setImageURI(Uri.parse(imageURL))
                }

                card.setOnClickListener {
                    onClickItem(position)
                }
            }
        }
    }
}
