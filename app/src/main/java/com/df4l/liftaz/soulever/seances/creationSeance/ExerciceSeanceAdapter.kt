package com.df4l.liftaz.soulever.seances.creationSeance

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R

class ExerciceSeanceAdapter(
    private val exercices: MutableList<ExerciceSeanceUi>,
    private val onAddClick: (position: Int) -> Unit,
    private val onLongPress: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class AvecFonteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nom = view.findViewById<TextView>(R.id.textExerciceName)
        val muscle = view.findViewById<TextView>(R.id.textExerciceMuscle)
        val series = view.findViewById<EditText>(R.id.editSeries)
        val minReps = view.findViewById<EditText>(R.id.editMinReps)
        val maxReps = view.findViewById<EditText>(R.id.editMaxReps)
        val btnDelete: ImageButton = view.findViewById(R.id.btnRemoveExercice)
        val buttonAddExercice: ImageButton = view.findViewById(R.id.btnAddPlus)

        // Watchers
        var seriesWatcher: TextWatcher? = null
        var minRepsWatcher: TextWatcher? = null
        var maxRepsWatcher: TextWatcher? = null
    }

    inner class PoidsDuCorpsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nom = view.findViewById<TextView>(R.id.textExerciceName)
        val muscle = view.findViewById<TextView>(R.id.textExerciceMuscle)
        val series = view.findViewById<EditText>(R.id.editSeries)
        val reps = view.findViewById<EditText>(R.id.editReps)
        val btnDelete: ImageButton = view.findViewById(R.id.btnRemoveExercice)
        val buttonAddExercice: ImageButton = view.findViewById(R.id.btnAddPlus)

        // Watchers
        var seriesWatcher: TextWatcher? = null
        var repsWatcher: TextWatcher? = null
    }

    override fun getItemViewType(position: Int): Int {
        return when (exercices[position]) {
            is ExerciceSeanceUi.AvecFonte -> 0
            is ExerciceSeanceUi.PoidsDuCorps -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exercice_avec_fonte, parent, false)
            AvecFonteViewHolder(view)
        } else {
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

                // Supprime les anciens watchers pour éviter les crashs
                h.seriesWatcher?.let { h.series.removeTextChangedListener(it) }
                h.minRepsWatcher?.let { h.minReps.removeTextChangedListener(it) }
                h.maxRepsWatcher?.let { h.maxReps.removeTextChangedListener(it) }

                // Affichage initial
                val seriesValue = if (item.idSuperset != null) {
                    item.superSetData?.series ?: 0
                } else {
                    item.series
                }

                h.series.setText(seriesValue.takeIf { it != 0 }?.toString() ?: "")
                h.minReps.setText(item.minReps.takeIf { it != 0 }?.toString() ?: "")
                h.maxReps.setText(item.maxReps.takeIf { it != 0 }?.toString() ?: "")

                // TextWatcher pour mise à jour en direct
                h.seriesWatcher = h.series.doOnTextChanged { text, _, _, _ ->
                    val value = text.toString().toIntOrNull() ?: 0

                    if (item.idSuperset != null) {
                        item.superSetData?.series = value
                        notifySupersetChanged(item.idSuperset!!)
                    } else {
                        item.series = value
                    }
                }
                h.minRepsWatcher = h.minReps.doOnTextChanged { text, _, _, _ ->
                    item.minReps = text.toString().toIntOrNull() ?: 0
                }
                h.maxRepsWatcher = h.maxReps.doOnTextChanged { text, _, _, _ ->
                    item.maxReps = text.toString().toIntOrNull() ?: 0
                }
            }

            is ExerciceSeanceUi.PoidsDuCorps -> {
                val h = holder as PoidsDuCorpsViewHolder
                h.nom.text = item.nom
                h.muscle.text = item.muscle

                // Supprime anciens watchers
                h.seriesWatcher?.let { h.series.removeTextChangedListener(it) }
                h.repsWatcher?.let { h.reps.removeTextChangedListener(it) }

                // Affichage initial
                val seriesValue = if (item.idSuperset != null) {
                    item.superSetData?.series ?: 0
                } else {
                    item.series
                }

                h.series.setText(seriesValue.takeIf { it != 0 }?.toString() ?: "")
                h.reps.setText(item.reps.takeIf { it != 0 }?.toString() ?: "")

                // TextWatcher
                h.seriesWatcher = h.series.doOnTextChanged { text, _, _, _ ->
                    val value = text.toString().toIntOrNull() ?: 0

                    if (item.idSuperset != null) {
                        item.superSetData?.series = value
                        notifySupersetChanged(item.idSuperset!!)
                    } else {
                        item.series = value
                    }
                }
                h.repsWatcher = h.reps.doOnTextChanged { text, _, _, _ ->
                    val value = text.toString().toIntOrNull() ?: 0
                    item.reps = value
                }
            }
        }

        val item = exercices[position]

        val btnDelete = when (holder) {
            is AvecFonteViewHolder -> holder.btnDelete
            is PoidsDuCorpsViewHolder -> holder.btnDelete
            else -> null
        }

        val buttonAddExercice = when (holder) {
            is AvecFonteViewHolder -> holder.buttonAddExercice
            is PoidsDuCorpsViewHolder -> holder.buttonAddExercice
            else -> null
        }

        btnDelete?.setOnClickListener {
            exercices.removeAt(holder.bindingAdapterPosition)
            notifyItemRemoved(holder.bindingAdapterPosition)
            notifyItemRangeChanged(holder.bindingAdapterPosition, exercices.size - holder.bindingAdapterPosition)
        }

        buttonAddExercice?.setOnClickListener {
            onAddClick(holder.bindingAdapterPosition + 1)
        }

        holder.itemView.setOnLongClickListener {
            onLongPress(holder.bindingAdapterPosition)
            true
        }

        if (item.idSuperset != null && item.superSetColor != null) {
            holder.itemView.setBackgroundColor(item.superSetColor!!)
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }
    }

    private fun notifySupersetChanged(supersetId: Int) {
        exercices.forEachIndexed { index, ex ->
            if (ex.idSuperset == supersetId) {
                notifyItemChanged(index)
            }
        }
    }

    override fun getItemCount() = exercices.size
}

sealed class ExerciceSeanceUi {
    var idSuperset: Int? = null
    var superSetColor: Int? = null
    var superSetData: SupersetData? = null

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

data class SupersetData(
    var series: Int = 0
)