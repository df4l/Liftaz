package com.df4l.liftaz.pousser.exercices.exercicesMenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.Exercice
import com.df4l.liftaz.data.Muscle

class ExercicesAdapter(
    private val data: List<Pair<Muscle, List<Exercice>>>,
    private val onDeleteExercice: (Exercice) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>()

    init {
        data.forEach { (muscle, exercices) ->
            items.add(muscle)
            items.addAll(exercices)
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position] is Muscle) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            val view = inflater.inflate(R.layout.item_muscle_header, parent, false)
            MuscleViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_exercice, parent, false)
            ExerciceViewHolder(view, onDeleteExercice)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is MuscleViewHolder && item is Muscle) holder.bind(item)
        if (holder is ExerciceViewHolder && item is Exercice) holder.bind(item)
    }

    class MuscleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomMuscle = itemView.findViewById<TextView>(R.id.nomMuscle)
        private val iconMuscle = itemView.findViewById<ImageView>(R.id.iconMuscle)
        fun bind(muscle: Muscle) {
            nomMuscle.text = muscle.nom
            val context = itemView.context
            val resId = context.resources.getIdentifier(muscle.nomImage, "drawable", context.packageName)
            if (resId != 0) iconMuscle.setImageResource(resId)
        }
    }

    class ExerciceViewHolder(
        itemView: View,
        private val onDeleteExercice: (Exercice) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nomExercice = itemView.findViewById<TextView>(R.id.nomExercice)
        private val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDeleteExercice)

        fun bind(exercice: Exercice) {
            nomExercice.text = exercice.nom
            btnDelete.setOnClickListener { onDeleteExercice(exercice) }
        }
    }
}

