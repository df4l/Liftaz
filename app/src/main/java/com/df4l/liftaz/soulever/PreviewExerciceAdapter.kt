package com.df4l.liftaz.soulever

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R

data class ExercicePreviewItem(
    val nomExercice: String,
    val nomMuscle: String,
    val texteSeriesEtReps: String
)


class PreviewExerciceAdapter(
    private val items: List<ExercicePreviewItem>
) : RecyclerView.Adapter<PreviewExerciceAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textNomExercice: TextView = view.findViewById(R.id.textNomExercice)
        val textNomMuscle: TextView = view.findViewById(R.id.textNomMuscle)
        val textSeriesReps: TextView = view.findViewById(R.id.textSeriesReps)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textNomExercice.text = item.nomExercice
        holder.textNomMuscle.text = item.nomMuscle
        holder.textSeriesReps.text = item.texteSeriesEtReps
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preview_exercice, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size
}
