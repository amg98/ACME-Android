package com.dam.acmeexplorer.listadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.dam.acmeexplorer.databinding.MenuItemBinding
import com.dam.acmeexplorer.models.MenuEntry

class MenuListAdapter(private val entries: List<MenuEntry>, val onClickItem: (pos: Int) -> Unit) : BaseAdapter() {

    override fun getCount() = entries.size

    override fun getItem(position: Int) = entries[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        lateinit var holder: ViewHolder

        if (convertView == null){
            val binding = MenuItemBinding.inflate(LayoutInflater.from(parent!!.context), parent, false)
            holder = ViewHolder(binding, binding.root)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val entry = entries[position]

        with(holder.binding) {
            menuEntryImage.setImageResource(entry.imageID)
            menuEntryTitle.text = entry.title
            card.setOnClickListener {
                onClickItem(position)
            }
        }

        return holder.view
    }

    private data class ViewHolder(val binding: MenuItemBinding, val view: View)
}
