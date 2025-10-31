package com.df4l.liftaz.pousser.seances.creationSeance

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
) : RecyclerView.Adapter<ExerciceSeanceAdapter.ExerciceViewHolder>() {

    inner class ExerciceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nom = view.findViewById<TextView>(R.id.textExerciceName)
        val muscle = view.findViewById<TextView>(R.id.textExerciceMuscle)
        val series = view.findViewById<EditText>(R.id.editSeries)
        val minReps = view.findViewById<EditText>(R.id.editMinReps)
        val maxReps = view.findViewById<EditText>(R.id.editMaxReps)
        val buttonAddExercice = view.findViewById<ImageButton>(R.id.btnAddExercice)
        val btnDelete: ImageButton = view.findViewById(R.id.btnRemoveExercice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercice_seance, parent, false)

        return ExerciceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciceViewHolder, position: Int) {
        val item = exercices[position]
        holder.nom.text = item.nom
        holder.muscle.text = item.muscle
        holder.series.setText(item.series.toString())
        holder.minReps.setText(item.minReps.toString())
        holder.maxReps.setText(item.maxReps.toString())

        holder.series.doAfterTextChanged {
            item.series = it.toString().toIntOrNull() ?: 0
        }

        holder.minReps.doAfterTextChanged {
            item.minReps = it.toString().toIntOrNull() ?: 0
        }

        holder.maxReps.doAfterTextChanged {
            item.maxReps = it.toString().toIntOrNull() ?: 0
        }

        holder.buttonAddExercice.setOnClickListener {
            onAddClick(position+1)
        }

        holder.btnDelete.setOnClickListener {
            exercices.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, exercices.size)
        }
    }

    override fun getItemCount() = exercices.size
}

data class ExerciceSeanceUi(
    val idExercice: Int,
    val nom: String,
    val muscle: String,
    var series: Int = 0,
    var minReps: Int = 0,
    var maxReps: Int = 0
)