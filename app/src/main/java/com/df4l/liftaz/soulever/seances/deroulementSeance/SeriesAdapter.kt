package com.df4l.liftaz.soulever.seances.entrainement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R

class SeriesAdapter(private val series: MutableList<SerieUi>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class FonteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textSerieNumber = itemView.findViewById<TextView>(R.id.textSerieNumber)
        private val editWeight = itemView.findViewById<EditText>(R.id.editPoids)
        private val editReps = itemView.findViewById<EditText>(R.id.editReps)
        private val checkboxFlemme = itemView.findViewById<CheckBox>(R.id.checkboxFlemme)

        fun bind(serie: SerieUi.Fonte, index: Int) {
            textSerieNumber.text = "Série $index"

            editWeight.setText(serie.poids.toString())
            editReps.setText(serie.reps.toString())
            checkboxFlemme.isChecked = serie.flemme

            // Update model when user edits values
            editWeight.addTextChangedListener {
                serie.poids = it.toString().toFloatOrNull() ?: 0f
            }
            editReps.addTextChangedListener {
                serie.reps = it.toString().toIntOrNull() ?: 0
            }
            checkboxFlemme.setOnCheckedChangeListener { _, checked ->
                serie.flemme = checked
            }
        }
    }

    class PoidsCorpsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textSerieNumber = itemView.findViewById<TextView>(R.id.textSerieNumber)
        private val editReps = itemView.findViewById<EditText>(R.id.editReps)
        private val checkboxFlemme = itemView.findViewById<CheckBox>(R.id.checkboxFlemme)
        //private val canvasAssistance = itemView.findViewById<View>(R.id.canvasAssistance) // remplace par ton type

        fun bind(serie: SerieUi.PoidsDuCorps, index: Int) {
            textSerieNumber.text = "Série $index"

            editReps.setText(serie.reps.toString())
            checkboxFlemme.isChecked = serie.flemme

            editReps.addTextChangedListener {
                serie.reps = it.toString().toIntOrNull() ?: 0
            }
            checkboxFlemme.setOnCheckedChangeListener { _, checked ->
                serie.flemme = checked
            }

            // TODO: lier ton canvas selon ton modèle (assistance, tension, etc)
            // Exemple :
            // canvasAssistance.setForceValue(serie.assistance)
            // canvasAssistance.onValueChanged { value -> serie.assistance = value }
        }
    }


    companion object {
        private const val TYPE_CHARGE = 0
        private const val TYPE_POIDS_CORPS = 1
    }

    override fun getItemViewType(position: Int) = when(series[position]) {
        is SerieUi.Fonte -> TYPE_CHARGE
        is SerieUi.PoidsDuCorps -> TYPE_POIDS_CORPS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_CHARGE -> FonteViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_serie_fonte, parent, false)
            )
            else -> PoidsCorpsViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_serie_pdc, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is FonteViewHolder -> holder.bind(series[position] as SerieUi.Fonte, position + 1)
            is PoidsCorpsViewHolder -> holder.bind(series[position] as SerieUi.PoidsDuCorps, position + 1)
        }
    }

    override fun getItemCount() = series.size
}


sealed class SerieUi {
    data class Fonte(var poids: Float, var reps: Int, var flemme: Boolean) : SerieUi()
    data class PoidsDuCorps(var reps: Int, var assistance: Float, var flemme: Boolean) : SerieUi()
}