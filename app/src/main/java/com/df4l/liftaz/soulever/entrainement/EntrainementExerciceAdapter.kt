package com.df4l.liftaz.soulever.seances.entrainement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Elastique

class EntrainementExerciceAdapter(
    private var elastiques: List<Elastique>,
    private val onSeriesChanged: () -> Unit,
    private val onFlemmeTriggered : () -> Unit
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
        private val btnAddPlus = itemView.findViewById<ImageButton>(R.id.btnAddPlus)

        private lateinit var seriesAdapter: SeriesAdapter

        fun bind(item: ExerciceSeanceItem) {
            textNom.text = item.exerciceName
            textMuscle.text = item.muscleName
            seriesAdapter = SeriesAdapter(item.series, elastiques, onSeriesChanged, onFlemmeTriggered)
            recyclerSeries.layoutManager = LinearLayoutManager(itemView.context)
            recyclerSeries.adapter = seriesAdapter

            btnAddPlus.setOnClickListener {
                // Ajouter une nouvelle série par défaut
                val newSerie = if (item.series.firstOrNull() is SerieUi.Fonte) {
                    SerieUi.Fonte(0f, 0f, false)
                } else {
                    SerieUi.PoidsDuCorps(0f, 0, false)
                }
                item.series.add(newSerie)
                seriesAdapter.notifyItemInserted(item.series.size - 1)
                onSeriesChanged()
            }
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
