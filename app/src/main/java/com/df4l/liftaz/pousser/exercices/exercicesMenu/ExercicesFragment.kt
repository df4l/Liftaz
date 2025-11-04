package com.df4l.liftaz.pousser.exercices.exercicesMenu

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Exercice
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.ExerciceSeanceDao
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.data.SerieDao
import com.df4l.liftaz.pousser.exercices.creationExercice.CreateExerciceDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExercicesFragment : Fragment(R.layout.fragment_exercices) {

    private lateinit var viewModel: ExercicesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton

    private lateinit var database: AppDatabase
    private lateinit var muscleDao: MuscleDao
    private lateinit var exerciceSeanceDao: ExerciceSeanceDao
    private lateinit var serieDao: SerieDao
    private lateinit var exerciceDao: ExerciceDao

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerExercices)
        fab = view.findViewById(R.id.fab_exercicesMenu)

        database = AppDatabase.getDatabase(requireContext())
        muscleDao = database.muscleDao()
        exerciceDao = database.exerciceDao()
        exerciceSeanceDao = database.exerciceSeanceDao()
        serieDao = database.serieDao()

        val db = AppDatabase.getDatabase(requireContext())
        viewModel = ExercicesViewModel(db.muscleDao(), db.exerciceDao())

        viewModel.exercicesParMuscle.observe(viewLifecycleOwner) { data ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = ExercicesAdapter(data) {
                    exercice ->
                lifecycleScope.launch {
                    val count = exerciceSeanceDao.countByExerciceId(exercice.id)
                    if (count > 0) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Supprimer cet exercice ?")
                            .setMessage("Cet exercice est présent dans $count séance(s). Il sera aussi supprimé de l'historique.")
                            .setPositiveButton("Supprimer") { _, _ ->
                                lifecycleScope.launch {
                                    supprimerExerciceComplet(exercice)
                                }
                            }
                            .setNegativeButton("Annuler", null)
                            .show()
                    } else {
                        supprimerExerciceComplet(exercice)
                    }
                }
            }

        }

        fab.setOnClickListener {
            showFabMenu(fab)
        }
    }

    private fun showFabMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_exercices_options, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_add_exercice -> {
                    CreateExerciceDialog(
                        context = requireContext(),
                        lifecycleScope = lifecycleScope,
                        exerciceDao = exerciceDao,
                        muscleDao = muscleDao,
                        parentView = requireView()
                    ) {
                        viewModel.reloadData()
                    }.show()
                    true
                }
                R.id.action_elastiques -> {
                    val navController = findNavController()
                    navController.navigate(R.id.action_exercicesFragment_to_elastiquesFragment)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private suspend fun supprimerExerciceComplet(exercice: Exercice) {
        withContext(Dispatchers.IO) {
            // 1. Supprimer les entrées de la table ExerciceSeance
            val seances = exerciceSeanceDao.getByExercice(exercice.id)
            seances.forEach {
                exerciceSeanceDao.delete(it)
                exerciceSeanceDao.reordonnerApresSuppression(it.idSeance, it.indexOrdre)
            }

            // 2. Supprimer les séries historiques liées à cet exercice
            serieDao.deleteByExercice(exercice.id)

            // 3. Supprimer l'exercice lui-même
            exerciceDao.delete(exercice)
        }

        // 4. Rafraîchir la liste
        viewModel.reloadData()
    }

}
