package com.df4l.liftaz.soulever.exercices.creationExercice

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import com.df4l.liftaz.data.Exercice
import com.df4l.liftaz.data.ExerciceDao
import com.google.android.material.snackbar.Snackbar
import com.df4l.liftaz.R
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.soulever.muscles.SpinnerMuscleAdapter
import kotlinx.coroutines.launch

class CreateExerciceDialog(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val exerciceDao: ExerciceDao,
    private val muscleDao: MuscleDao,
    private val parentView: View,
    private val onExerciseAdded: (() -> Unit)? = null
) {
    fun show() {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_add_exercice, null)

        val editName = dialogView.findViewById<EditText>(R.id.editTextExerciseName)
        val editNotes = dialogView.findViewById<EditText>(R.id.editTextExerciseNotes)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val checkBoxPDC = dialogView.findViewById<CheckBox>(R.id.checkBoxPDC)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Nouvel exercice")
            .setView(dialogView)
            .setPositiveButton("Ajouter", null)
            .setNegativeButton("Annuler") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        lifecycleScope.launch {
            val muscles = muscleDao.getAllMuscles()

            // 🧩 Utilisation du nouvel adapter
            val adapter = SpinnerMuscleAdapter(context, muscles)
            spinnerCategory.adapter = adapter

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = editName.text.toString().trim()
                val description = editNotes.text.toString().trim()
                val selectedPosition = spinnerCategory.selectedItemPosition
                val selectedMuscle = muscles.getOrNull(selectedPosition)
                val isPDC = checkBoxPDC.isChecked

                if (name.isEmpty()) {
                    editName.error = "Le nom est obligatoire"
                    editName.requestFocus()
                    return@setOnClickListener
                }

                if (selectedMuscle == null) {
                    Snackbar.make(parentView, "Veuillez sélectionner un muscle", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                addExercise(name, selectedMuscle.id, isPDC, description)
                dialog.dismiss()
            }
        }

    }

    private fun addExercise(name: String, idMuscleCible: Int, exercicePdC: Boolean, notes: String) {
        val exercice = Exercice(
            nom = name,
            idMuscleCible = idMuscleCible,
            poidsDuCorps = exercicePdC,
            notes = notes
        )

        lifecycleScope.launch {
            exerciceDao.insert(exercice)
            Snackbar.make(parentView, "Exercice ajouté : $name", Snackbar.LENGTH_LONG).show()
            onExerciseAdded?.invoke()
        }
    }
}
