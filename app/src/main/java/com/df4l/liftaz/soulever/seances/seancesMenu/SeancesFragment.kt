package com.df4l.liftaz.soulever.seances.seancesMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.data.SeanceAvecExercices
import com.df4l.liftaz.data.SeanceDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class SeancesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SeanceAdapter
    private val seances = mutableListOf<SeanceAvecExercices>()
    private lateinit var database: AppDatabase
    private lateinit var seanceDao: SeanceDao
    private lateinit var exerciceDao: ExerciceDao
    private lateinit var muscleDao: MuscleDao

    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_seances, container, false)

        // Initialisation de la base de données et des DAO
        database = AppDatabase.getDatabase(requireContext())
        exerciceDao = database.exerciceDao()
        seanceDao = database.seanceDao()
        muscleDao = database.muscleDao()

        fab = view.findViewById(R.id.fab_seancesMenu)

        recyclerView = view.findViewById(R.id.recyclerViewSeances)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadSeances() // on charge les séances et on crée l'adapter après

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_seancesFragment_to_creationseanceFragment)
        }

        return view
    }


    private fun loadSeances() {
        lifecycleScope.launch {
            val seancesAvecExercices = seanceDao.getSeancesAvecExercices()
            val musclesList = muscleDao.getAllMuscles()
            val exercicesList = exerciceDao.getAllExercices()

            // Préparer les maps pour l'adapter
            val musclesMap = musclesList.associateBy({ it.id }, { it.nom })
            val exerciceMap = exercicesList.associateBy { it.id }

            seances.clear()
            seances.addAll(seancesAvecExercices)

            // Créer l'adapter avec les maps
            adapter = SeanceAdapter(
                seances = seances,
                musclesMap = musclesMap,
                exerciceMap = exerciceMap,
                onClick = { seance ->
                    findNavController().navigate(
                        R.id.action_seancesFragment_to_creationseanceFragment,
                        bundleOf("idSeance" to seance.seance.id)
                    )
                },
                onLongClick = { seance ->
                    lifecycleScope.launch {
                        // Supprimer les ExerciceSeance liés
                        database.exerciceSeanceDao().deleteExercicesForSeance(seance.seance.id)
                        // Supprimer la séance
                        database.seanceDao().delete(seance.seance)
                        // Retirer de la liste et notifier l’adapter
                        seances.remove(seance)
                        adapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Séance supprimée", Toast.LENGTH_SHORT).show()
                    }
                }
            )


            recyclerView.adapter = adapter
        }
    }


}
