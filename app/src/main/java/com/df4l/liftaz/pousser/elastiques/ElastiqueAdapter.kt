package com.df4l.liftaz.pousser.elastiques

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Elastique

class ElastiqueAdapter(
    private val elastiques: MutableList<Elastique>,
    private val onDelete: (Elastique) -> Unit
) : RecyclerView.Adapter<ElastiqueAdapter.ElastiqueViewHolder>() {

    inner class ElastiqueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorView: View = view.findViewById(R.id.colorView)
        val labelText: TextView = view.findViewById(R.id.labelText)
        val resistanceText: TextView = view.findViewById(R.id.resistanceText)
        val deleteButton: View = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElastiqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_elastique, parent, false)
        return ElastiqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: ElastiqueViewHolder, position: Int) {
        val elastique = elastiques[position]
        holder.colorView.setBackgroundColor(elastique.couleur)
        holder.labelText.text = elastique.label
        holder.resistanceText.text = "${elastique.resistanceMinKg}–${elastique.resistanceMaxKg} kg"

        holder.deleteButton.setOnClickListener {
            onDelete(elastique)
        }
    }

    override fun getItemCount(): Int = elastiques.size

    fun updateList(newList: List<Elastique>) {
        elastiques.clear()
        elastiques.addAll(newList)
        notifyDataSetChanged()
    }
}
