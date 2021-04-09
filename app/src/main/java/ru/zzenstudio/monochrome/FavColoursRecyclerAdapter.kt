/*
 * Created by Evgeniya Zemlyanaya (@zzemlyanaya), ZZen Studio
 *  * Copyright (c) 2021 . All rights reserved.
 */

package ru.zzenstudio.monochrome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.danielnilsson9.colorpickerview.view.ColorPanelView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.item_colour.view.*
import ru.zzenstudio.monochrome.database.Colour

class FavColoursRecyclerAdapter constructor(private val onColourClick: (Colour) -> Unit,
                                            private val colours: List<Colour>):
    RecyclerView.Adapter<FavColoursRecyclerAdapter.ColourViewHolder>() {


    inner class ColourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colourName: MaterialTextView = itemView.name
        val colourHex: MaterialTextView = itemView.hex
        val colourPanel: ColorPanelView = itemView.colourPanel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColourViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_colour, parent, false)
        return ColourViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ColourViewHolder, position: Int) {
        val current = colours[position]
        holder.itemView.setOnClickListener { onColourClick.invoke(current) }
        holder.colourName.text = current.name
        holder.colourHex.text = current.hex
        holder.colourPanel.color = current.code
    }

    fun removeItem(position: Int){
        (colours as ArrayList).removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreItem(item: Colour, position: Int) {
        (colours as ArrayList).add(position, item)
        notifyItemInserted(position)
    }

    fun getData(): List<Colour> = colours

    override fun getItemCount() = colours.size
}