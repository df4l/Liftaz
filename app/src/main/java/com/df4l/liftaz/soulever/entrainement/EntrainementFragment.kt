package com.df4l.liftaz.soulever.seances.entrainement

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Elastique
import com.df4l.liftaz.data.SeanceHistorique
import com.df4l.liftaz.data.Serie
import com.df4l.liftaz.soulever.fioul.RandomFioulDialog
import kotlinx.coroutines.launch
import java.util.Date

class EntrainementFragment : Fragment() {

    private var seanceId: Int = 0
    private lateinit var recyclerExercices: RecyclerView
    private lateinit var exerciceAdapter: EntrainementExerciceAdapter

    private lateinit var textChrono: TextView
    private var secondsPassed = 0
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            secondsPassed++
            val minutes = secondsPassed / 60
            val seconds = secondsPassed % 60
            textChrono.text = String.format("%02d:%02d", minutes, seconds)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        seanceId = arguments?.getInt("SEANCE_ID") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_entrainement, container, false)

        textChrono = view.findViewById(R.id.textChrono)

        recyclerExercices = view.findViewById(R.id.recyclerEntrainement)
        recyclerExercices.layoutManager = LinearLayoutManager(requireContext())

        exerciceAdapter = EntrainementExerciceAdapter(emptyList(), ::mettreAJourEtatBouton, onFlemmeTriggered = {
            RandomFioulDialog.showRandomFioulDialog(
                context = requireContext(),
                scope = viewLifecycleOwner.lifecycleScope
            )
        })
        recyclerExercices.adapter = exerciceAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val elastiques = db.elastiqueDao().getAll()
            exerciceAdapter.setElastiques(elastiques)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSeance()

        // Démarre le chronomètre
        handler.post(updateRunnable)

        val btnStart = view.findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener {
            sauvegarderSeance()
        }
        btnStart.isEnabled = false // grisé au début

    }

    fun toutesLesSeriesRemplies(): Boolean {
        return recyclerExercices.children.all { exoView ->
            val recyclerSeries = exoView.findViewById<RecyclerView>(R.id.recyclerSeries)
            recyclerSeries.children.all { serieView ->
                val editReps = serieView.findViewById<EditText>(R.id.editReps)
                val editPoids = serieView.findViewById<EditText?>(R.id.editPoids) // null pour PoidsDuCorps
                val checkboxFlemme = serieView.findViewById<CheckBox>(R.id.checkboxFlemme)

                if (checkboxFlemme.isChecked) {
                    true
                } else {
                    val reps = editReps.text.toString().toFloatOrNull() ?: 0f
                    val poids = editPoids?.text?.toString()?.toFloatOrNull() // null si poids du corps

                    if (editPoids == null) {
                        // Poids du corps → seulement les reps comptent
                        reps > 0f
                    } else {
                        // Exercice avec poids
                        reps > 0f && (poids ?: 0f) > 0f
                    }
                }
            }
        }
    }


    fun mettreAJourEtatBouton() {
        val btnStart = view?.findViewById<Button>(R.id.btnStart) ?: return
        btnStart.isEnabled = toutesLesSeriesRemplies()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateRunnable) // Stop timer quand on quitte le fragment
    }

    private fun loadSeance() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())

            val dernierPoidsUtilisateur = db.entreePoidsDao().getLatestWeight()?.poids
            val elastiques = db.elastiqueDao().getAll()
            val exercicesSeance = db.exerciceSeanceDao().getExercicesForSeance(seanceId)

            // ✅ On récupère la dernière séance si elle existe
            val lastSeanceHistorique = db.seanceHistoriqueDao().getLastSeanceHistorique(seanceId)

            // ✅ Et ses séries associées (si elle existe)
            val lastSeries = lastSeanceHistorique?.let {
                db.serieDao().getSeriesForSeanceHistorique(it.id)
            }

            val items = mutableListOf<ExerciceSeanceItem>()

            for (exSeance in exercicesSeance) {
                val exercice = db.exerciceDao().getExerciceById(exSeance.idExercice)
                val muscleNom = db.muscleDao().getNomMuscleById(exercice.idMuscleCible)

                val series = mutableListOf<SerieUi>()

                var nbSeriesRelevees = 0
                var quantiteTotaleSoulevee = 0f

                if(lastSeries != null)
                    nbSeriesRelevees = lastSeries.filter { it.idExercice == exSeance.idExercice }.size
                else
                    nbSeriesRelevees = exSeance.nbSeries

                for(i in 1..nbSeriesRelevees)
                {
                    val previousSerie = lastSeries?.find { it.idExercice == exSeance.idExercice && it.numeroSerie == i }
                    var nbPoids = 0f
                    var nbReps = 0f
                    if(exercice.poidsDuCorps) {
                        nbPoids = computeEffectiveLoad(dernierPoidsUtilisateur, decodeElastiques(previousSerie?.elastiqueBitMask ?: 0, elastiques))
                        nbReps = previousSerie?.nombreReps ?: 0f
                        quantiteTotaleSoulevee += nbPoids * nbReps
                    } else {
                        nbPoids = previousSerie?.poids ?: 0f
                        nbReps = previousSerie?.nombreReps ?: 0f
                        quantiteTotaleSoulevee += nbPoids * nbReps
                    }
                }

                for (i in 1..exSeance.nbSeries) {
                    val previousSerie = lastSeries?.find { it.idExercice == exSeance.idExercice && it.numeroSerie == i }

                    series.add(
                        if (exercice.poidsDuCorps) {
                            SerieUi.PoidsDuCorps(
                                reps = previousSerie?.nombreReps ?: 0f,
                                bitmaskElastiques = previousSerie?.elastiqueBitMask ?: 0,
                                flemme = false
                            )
                        } else {
                            SerieUi.Fonte(
                                poids = previousSerie?.poids ?: 0f,
                                reps = previousSerie?.nombreReps ?: 0f,
                                flemme = false
                            )
                        }
                    )
                }

                items.add(
                    ExerciceSeanceItem(
                        exerciceName = exercice.nom,
                        muscleName = muscleNom,
                        series = series,
                        poidsSouleve = quantiteTotaleSoulevee,
                        poidsUtilisateur = dernierPoidsUtilisateur ?: 0f
                    )
                )
            }

            exerciceAdapter.submitList(items)
        }
    }

    private fun decodeElastiques(bitmask: Int, allElastiques: List<Elastique>): List<Elastique> {
        return allElastiques.filter { elast ->
            (bitmask and elast.valeurBitmask) != 0
        }
    }

    fun computeEffectiveLoad(userWeight: Float?, elastiques: List<Elastique>): Float {
        if (userWeight == null) return 0f

        // Somme des valeurs des élastiques en tenant compte de leur effet :
        //   +ve = rend plus difficile (ajoute au poids)
        //   -ve = rend plus facile (réduit le poids effectif)
        val elastiqueEffect = elastiques.sumOf { it.resistanceMinKg.toDouble() }.toFloat()

        return userWeight - elastiqueEffect
    }

    private fun sauvegarderSeance() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())

            handler.removeCallbacks(updateRunnable)

            val seanceHistorique = SeanceHistorique(
                idSeance = seanceId,
                date = Date(),
                dureeSecondes = secondsPassed
            )

            val idSeanceHistorique = db.seanceHistoriqueDao().insert(seanceHistorique).toInt()

            val items = exerciceAdapter.getItems()
            val seriesToInsert = mutableListOf<Serie>()

            for (item in items) {
                val exercice = db.exerciceDao().getExerciceByName(item.exerciceName)
                val idExercice = exercice.id

                item.series.forEachIndexed { index, serieUi ->
                    val numeroSerie = index + 1

                    val serie = when (serieUi) {
                        is SerieUi.Fonte -> Serie(
                            idSeanceHistorique = idSeanceHistorique,
                            idExercice = idExercice,
                            numeroSerie = numeroSerie,
                            poids = if(!serieUi.flemme) { serieUi.poids } else 0f,
                            nombreReps = if(!serieUi.flemme) { serieUi.reps } else 0f,
                            elastiqueBitMask = 0
                        )
                        is SerieUi.PoidsDuCorps -> Serie(
                            idSeanceHistorique = idSeanceHistorique,
                            idExercice = idExercice,
                            numeroSerie = numeroSerie,
                            poids = 0f,
                            nombreReps = if(!serieUi.flemme) { serieUi.reps } else 0f,
                            elastiqueBitMask = serieUi.bitmaskElastiques
                        )
                    }

                    // 🔥 DEBUG LOG
                    Log.d("SAVE_SERIE", "→ Exercice: ${item.exerciceName}, Série $numeroSerie, Poids=${serie.poids}, Reps=${serie.nombreReps}, Elastiques=${serie.elastiqueBitMask}")

                    seriesToInsert.add(serie)
                }
            }

            db.serieDao().insertAll(seriesToInsert)

            Toast.makeText(requireContext(), "Séance enregistrée ✅", Toast.LENGTH_SHORT).show()

            // 🔹 Navigation vers BilanFragment
            val bundle = Bundle().apply {
                putInt("idSeance", seanceId)
                putInt("idSeanceHistorique", idSeanceHistorique)
            }

            findNavController().navigate(
                R.id.action_entrainementFragment_to_bilanFragment,
                bundle
            )
        }
    }

}

