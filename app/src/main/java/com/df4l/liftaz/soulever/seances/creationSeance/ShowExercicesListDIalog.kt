package com.df4l.liftaz.soulever.seances.creationSeance

import com.df4l.liftaz.soulever.muscles.musclesListe.MuscleListAdapter
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.snackbar.Snackbar
import com.df4l.liftaz.data.Exercice
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.MuscleDao
import android.view.View
import kotlinx.coroutines.launch

class ShowExercicesListDialog(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val exerciceDao: ExerciceDao,
    private val muscleDao: MuscleDao,
    private val parentView: View,
    private val onExerciceSelected: (Exercice) -> Unit   // ✅ callback
) {

    fun show() {
        lifecycleScope.launch {
            val exercices = exerciceDao.getAllExercices()
            val muscles = muscleDao.getAllMuscles()

            if (exercices.isEmpty()) {
                Snackbar.make(parentView, "Aucun exercice créé pour le moment", Snackbar.LENGTH_SHORT).show()
                return@launch
            }

            val musclesAvecExos = muscles.filter { muscle ->
                exercices.any { it.idMuscleCible == muscle.id }
            }

            if (musclesAvecExos.isEmpty()) {
                Snackbar.make(parentView, "Aucun muscle avec des exercices", Snackbar.LENGTH_SHORT).show()
                return@launch
            }

            val musclesNames = musclesAvecExos.map { it.nom }.toTypedArray()

            val adapter = MuscleListAdapter(context, musclesAvecExos)

            AlertDialog.Builder(context)
                .setTitle("Choisir un muscle")
                .setAdapter(adapter) { dialog, which ->
                    val selectedMuscle = musclesAvecExos[which]
                    dialog.dismiss()
                    showExercisesForMuscle(selectedMuscle.id, selectedMuscle.nom, exercices)
                }
                .setNegativeButton("Annuler") { d, _ -> d.dismiss() }
                .show()

        }
    }

    private fun showExercisesForMuscle(
        idMuscle: Int,
        nomMuscle: String,
        allExercices: List<Exercice>
    ) {
        lifecycleScope.launch {
            val filteredExercices = allExercices.filter { it.idMuscleCible == idMuscle }

            if (filteredExercices.isEmpty()) {
                Snackbar.make(parentView, "Aucun exercice pour $nomMuscle", Snackbar.LENGTH_SHORT).show()
                return@launch
            }

            val exercicesStrings = filteredExercices.map { ex ->
                val type = if (ex.poidsDuCorps) "PDC" else "PO"
                "${ex.nom} - $type"
            }

            AlertDialog.Builder(context)
                .setTitle("Exercices pour $nomMuscle")
                .setItems(exercicesStrings.toTypedArray()) { dialog, which ->
                    val selectedEx = filteredExercices[which]
                    dialog.dismiss()

                    // ✅ Appel du callback avec l’exercice choisi
                    onExerciceSelected(selectedEx)
                }
                .setNegativeButton("Annuler") { d, _ -> d.dismiss() }
                .show()
        }
    }
}
