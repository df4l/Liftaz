package com.df4l.liftaz.soulever.seances.creationSeance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R

class ExerciceSeanceAdapter(
    private val exercices: MutableList<ExerciceSeanceUi>,
    private val onAddClick: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class AvecFonteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nom = view.findViewById<TextView>(R.id.textExerciceName)
        val muscle = view.findViewById<TextView>(R.id.textExerciceMuscle)
        val series = view.findViewById<EditText>(R.id.editSeries)
        val minReps = view.findViewById<EditText>(R.id.editMinReps)
        val maxReps = view.findViewById<EditText>(R.id.editMaxReps)
        val btnDelete: ImageButton = view.findViewById(R.id.btnRemoveExercice)
        val buttonAddExercice = view.findViewById<ImageButton>(R.id.btnAddExercice)
    }

    inner class PoidsDuCorpsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nom = view.findViewById<TextView>(R.id.textExerciceName)
        val muscle = view.findViewById<TextView>(R.id.textExerciceMuscle)
        val series = view.findViewById<EditText>(R.id.editSeries)
        val reps = view.findViewById<EditText>(R.id.editReps)
        val btnDelete: ImageButton = view.findViewById(R.id.btnRemoveExercice)
        val buttonAddExercice = view.findViewById<ImageButton>(R.id.btnAddExercice)
    }


    override fun getItemViewType(position: Int): Int {
        return when (exercices[position]) {
            is ExerciceSeanceUi.AvecFonte -> 0
            is ExerciceSeanceUi.PoidsDuCorps -> 1
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) { // Avec poids
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exercice_avec_fonte, parent, false)
            AvecFonteViewHolder(view)
        } else { // Poids du corps
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exercice_poids_du_corps, parent, false)
            PoidsDuCorpsViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = exercices[position]) {
            is ExerciceSeanceUi.AvecFonte -> {
                val h = holder as AvecFonteViewHolder
                h.nom.text = item.nom
                h.muscle.text = item.muscle
                h.series.setText(item.series.toString())
                h.minReps.setText(item.minReps.toString())
                h.maxReps.setText(item.maxReps.toString())

                h.series.doAfterTextChanged { item.series = it.toString().toIntOrNull() ?: 0 }
                h.minReps.doAfterTextChanged { item.minReps = it.toString().toIntOrNull() ?: 0 }
                h.maxReps.doAfterTextChanged { item.maxReps = it.toString().toIntOrNull() ?: 0 }

                h.btnDelete.setOnClickListener {
                    exercices.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, exercices.size - position)
                }

                h.buttonAddExercice.setOnClickListener {
                    onAddClick(position+1)
                }

            }

            is ExerciceSeanceUi.PoidsDuCorps -> {
                val h = holder as PoidsDuCorpsViewHolder
                h.nom.text = item.nom
                h.muscle.text = item.muscle
                h.series.setText(item.series.toString())
                h.reps.setText(item.reps.toString())

                h.series.doAfterTextChanged { item.series = it.toString().toIntOrNull() ?: 0 }
                h.reps.doAfterTextChanged { item.reps = it.toString().toIntOrNull() ?: 0 }

                h.btnDelete.setOnClickListener {
                    exercices.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, exercices.size - position)
                }

                h.buttonAddExercice.setOnClickListener {
                    onAddClick(position+1)
                }
            }
        }
    }


    override fun getItemCount() = exercices.size
}

sealed class ExerciceSeanceUi {
    data class AvecFonte(
        val idExercice: Int,
        val nom: String,
        val muscle: String,
        var series: Int = 0,
        var minReps: Int = 0,
        var maxReps: Int = 0,
    ) : ExerciceSeanceUi()

    data class PoidsDuCorps(
        val idExercice: Int,
        val nom: String,
        val muscle: String,
        var series: Int = 0,
        var reps: Int = 0
    ) : ExerciceSeanceUi()
}