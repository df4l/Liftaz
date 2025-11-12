package com.df4l.liftaz.soulever

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Exercice
import com.df4l.liftaz.data.SeanceAvecExercices

class SeancePickerAdapter(
    private val seances: List<SeanceAvecExercices>,
    private val musclesMap: Map<Int, String>,
    private val exerciceMap: Map<Int, Exercice>,
    private val onSelect: (SeanceAvecExercices) -> Unit
) : RecyclerView.Adapter<SeancePickerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNomSeance: TextView = view.findViewById(R.id.textNomSeance)
        val textNbExercices: TextView = view.findViewById(R.id.textNbExercices)
        val textMuscles: TextView = view.findViewById(R.id.textMuscles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seance, parent, false)
        // on cache la fréquence ici :
        v.findViewById<TextView>(R.id.textFrequence)?.visibility = View.GONE
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seanceAvecExercices = seances[position]
        val seance = seanceAvecExercices.seance
        val exercices = seanceAvecExercices.exercices

        holder.textNomSeance.text = seance.nom
        holder.textNbExercices.text = "${exercices.size} exercice(s)"

        val musclesSet = exercices.mapNotNull { exoSeance ->
            val exo = exerciceMap[exoSeance.idExercice]
            exo?.let { musclesMap[it.idMuscleCible] }
        }.distinct()

        holder.textMuscles.text = musclesSet.joinToString(", ")

        holder.itemView.setOnClickListener { onSelect(seanceAvecExercices) }
    }

    override fun getItemCount() = seances.size
}
