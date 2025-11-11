package com.df4l.liftaz.soulever.seances.entrainement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Elastique

class EntrainementExerciceAdapter(
    private var elastiques: List<Elastique>, private val onSeriesChanged: () -> Unit
) : RecyclerView.Adapter<EntrainementExerciceAdapter.ExerciceViewHolder>() {

    private val items = mutableListOf<ExerciceSeanceItem>()

    fun submitList(list: List<ExerciceSeanceItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun setElastiques(newList: List<Elastique>) {
        elastiques = newList
        notifyDataSetChanged()
    }

    inner class ExerciceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textNom = itemView.findViewById<TextView>(R.id.textExerciceName)
        private val textMuscle = itemView.findViewById<TextView>(R.id.textExerciceMuscle)
        private val recyclerSeries = itemView.findViewById<RecyclerView>(R.id.recyclerSeries)

        fun bind(item: ExerciceSeanceItem) {
            textNom.text = item.exerciceName
            textMuscle.text = item.muscleName
            recyclerSeries.layoutManager = LinearLayoutManager(itemView.context)
            recyclerSeries.adapter = SeriesAdapter(item.series, elastiques, onSeriesChanged)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercice_seance, parent, false)
        return ExerciceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun getItems(): List<ExerciceSeanceItem> = items
}

data class ExerciceSeanceItem(
    val exerciceName: String,
    val muscleName: String,
    val series: MutableList<SerieUi>
)
