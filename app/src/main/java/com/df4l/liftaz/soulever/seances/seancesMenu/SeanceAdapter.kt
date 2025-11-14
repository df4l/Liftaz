package com.df4l.liftaz.soulever.seances.seancesMenu

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Exercice
import com.df4l.liftaz.data.SeanceAvecExercices
import com.df4l.liftaz.data.TypeFrequence

class SeanceAdapter(
    private val seances: List<SeanceAvecExercices>,
    private val musclesMap: Map<Int, String>, // idMuscle -> nomMuscle
    private val exerciceMap: Map<Int, Exercice>, // idExercice -> Exercice
    private val onClick: (SeanceAvecExercices) -> Unit,
    private val onLongClick: (SeanceAvecExercices) -> Unit
) : RecyclerView.Adapter<SeanceAdapter.SeanceViewHolder>() {

    inner class SeanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNomSeance: TextView = itemView.findViewById(R.id.textNomSeance)
        val textFrequence: TextView = itemView.findViewById(R.id.textFrequence)
        val textNbExercices: TextView = itemView.findViewById(R.id.textNbExercices)
        val textMuscles: TextView = itemView.findViewById(R.id.textMuscles)
        val boutonSupprimer: ImageButton = itemView.findViewById(R.id.btnDeleteSeance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seance, parent, false)
        return SeanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeanceViewHolder, position: Int) {
        val seanceAvecExercices = seances[position]
        val seance = seanceAvecExercices.seance
        val exercices = seanceAvecExercices.exercices

        // Nom de la séance
        holder.textNomSeance.text = seance.nom

        // Fréquence
        holder.textFrequence.text = when (seance.typeFrequence) {
            TypeFrequence.JOURS_SEMAINE -> joursSemaineToString(seance.joursSemaine)
            TypeFrequence.INTERVALLE -> "Tous les ${seance.intervalleJours} jours"
        }

        // Nombre d'exercices
        holder.textNbExercices.text = "${exercices.size} exercice(s)"

        // Muscles engagés
        val musclesSet = exercices.mapNotNull { exoSeance ->
            val exo = exerciceMap[exoSeance.idExercice]
            exo?.let { musclesMap[it.idMuscleCible] }
        }.distinct()
        holder.textMuscles.text = musclesSet.joinToString(", ")

        // Click simple
        holder.itemView.setOnClickListener {
            onClick(seanceAvecExercices)
        }

        // Long click pour suppression
        holder.boutonSupprimer.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Supprimer la séance ?")
                .setMessage("Voulez-vous vraiment supprimer la séance \"${seance.nom}\" et tous ses exercices associés ?")
                .setPositiveButton("Oui") { dialog, _ ->
                    onLongClick(seanceAvecExercices)
                    dialog.dismiss()
                }
                .setNegativeButton("Non") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            true
        }
    }


    fun joursSemaineToString(jours: List<Int>?): String {
        if (jours.isNullOrEmpty()) return ""
        val nomsJours = listOf(
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
        )
        return jours.sorted().map { joursIndex ->
            nomsJours.getOrNull(joursIndex - 1) ?: "Jour $joursIndex"
        }.joinToString(", ")
    }


    override fun getItemCount(): Int = seances.size
}

