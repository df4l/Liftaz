package com.df4l.liftaz.soulever.seances.entrainement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import kotlinx.coroutines.launch

class EntrainementFragment : Fragment() {

    private var seanceId: Int = 0

    private lateinit var recyclerExercices: RecyclerView
    private lateinit var exerciceAdapter: EntrainementExerciceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Récupération de l'ID de la séance passée en argument
        seanceId = arguments?.getInt("SEANCE_ID") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_entrainement, container, false)
        recyclerExercices = view.findViewById(R.id.recyclerEntrainement)
        recyclerExercices.layoutManager = LinearLayoutManager(requireContext())
        exerciceAdapter = EntrainementExerciceAdapter()
        recyclerExercices.adapter = exerciceAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Chargement des données dès l'ouverture
        loadSeance()
    }

    private fun loadSeance() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext()) // Remplace par ta méthode d'accès DB
            val seance = db.seanceDao().getSeance(seanceId)
            val exercicesSeance = db.exerciceSeanceDao().getExercicesForSeance(seanceId)

            val items = mutableListOf<ExerciceSeanceItem>()

            for (exSeance in exercicesSeance) {
                val exercice = db.exerciceDao().getExerciceById(exSeance.idExercice)
                val muscleNom = db.muscleDao().getNomMuscleById(exercice.idMuscleCible)

                // Ici, on pourrait générer les séries par défaut pour l'affichage
                val series = mutableListOf<SerieUi>()
                for (i in 1..exSeance.nbSeries) {
                    series.add(
                        if (exercice.poidsDuCorps)
                            SerieUi.PoidsDuCorps(reps = exSeance.minReps, assistance = 0f, flemme = false)
                        else
                            SerieUi.Fonte(poids = 0f, reps = exSeance.minReps, flemme = false)
                    )
                }

                items.add(
                    ExerciceSeanceItem(
                        exerciceName = exercice.nom,
                        muscleName = muscleNom,
                        series = series
                    )
                )
            }

            exerciceAdapter.submitList(items)
        }
    }
}

