package com.df4l.liftaz.soulever.seances.entrainement

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.Elastique
import com.df4l.liftaz.data.Exercice
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
        // On récupère la liste des exercices depuis l'adapter
        val items = exerciceAdapter.getItems()

        if (items.isEmpty()) return false

        // On vérifie chaque exercice
        return items.all { exercice ->
            // On vérifie chaque série de l'exercice
            exercice.series.all { serie ->
                if (serie is SerieUi.Fonte) {
                    // Pour la fonte : soit flemme, soit (poids > 0 et reps > 0)
                    // Note: j'utilise touchedByUser pour s'assurer que l'utilisateur a validé l'intention
                    serie.flemme || (serie.poids > 0f && serie.reps > 0f)
                } else if (serie is SerieUi.PoidsDuCorps) {
                    // Pour le PDC : soit flemme, soit reps > 0
                    serie.flemme || (serie.reps > 0f)
                } else {
                    false
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

            val supersetsDejaTraites = mutableSetOf<Int>()

            for (exSeance in exercicesSeance) {

                val supersetId = exSeance.idSuperset

                if(supersetId == null) {
                    val exercice = db.exerciceDao().getExerciceById(exSeance.idExercice)
                    val muscleNom = db.muscleDao().getNomMuscleById(exercice.idMuscleCible)

                    val series = mutableListOf<SerieUi>()

                    var nbSeriesRelevees = 0
                    var quantiteTotaleSoulevee = 0f

                    if (lastSeries != null)
                        nbSeriesRelevees =
                            lastSeries.filter { it.idExercice == exSeance.idExercice }.size
                    else
                        nbSeriesRelevees = exSeance.nbSeries

                    for (i in 1..nbSeriesRelevees) {
                        val previousSerie =
                            lastSeries?.find { it.idExercice == exSeance.idExercice && it.numeroSerie == i }
                        var nbPoids = 0f
                        var nbReps = 0f
                        if (exercice.poidsDuCorps) {
                            nbPoids = computeEffectiveLoad(
                                dernierPoidsUtilisateur,
                                decodeElastiques(previousSerie?.elastiqueBitMask ?: 0, elastiques)
                            )
                            nbReps = previousSerie?.nombreReps ?: 0f
                            quantiteTotaleSoulevee += nbPoids * nbReps
                        } else {
                            nbPoids = previousSerie?.poids ?: 0f
                            nbReps = previousSerie?.nombreReps ?: 0f
                            quantiteTotaleSoulevee += nbPoids * nbReps
                        }
                    }

                    for (i in 1..exSeance.nbSeries) {
                        val previousSerie =
                            lastSeries?.find { it.idExercice == exSeance.idExercice && it.numeroSerie == i }

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
                else
                {
                    if (supersetsDejaTraites.contains(supersetId)) {
                        // On a déjà créé les items pour ce superset
                        continue
                    }

                    supersetsDejaTraites.add(supersetId)

                    val exercicesDuSuperset = exercicesSeance.filter {
                        it.idSuperset == supersetId
                    }

                    val nbToursSuperset = exercicesDuSuperset.minOf { it.nbSeries }

                    var listeExercices: List<Exercice> = emptyList()
                    val poidsParExercices = MutableList(exercicesDuSuperset.size) { 0f }

                    for (numeroSerie in 1..nbToursSuperset) {

                        var stringExercices = ""

                        val seriesDuTour = mutableListOf<SerieUi>()
                        var quantiteTotaleSoulevee = 0f

                        for (exSeanceSuperset in exercicesDuSuperset) {

                            val exercice = db.exerciceDao()
                                .getExerciceById(exSeanceSuperset.idExercice)

                            listeExercices = listeExercices + exercice

                            stringExercices = stringExercices + exercice.nom + ", "

                            val previousSerie = lastSeries?.find {
                                it.idExercice == exSeanceSuperset.idExercice &&
                                        it.numeroSerie == numeroSerie
                            }

                            if (exercice.poidsDuCorps) {

                                val reps = previousSerie?.nombreReps ?: 0f
                                val bitmask = previousSerie?.elastiqueBitMask ?: 0

                                val poidsEffectif = computeEffectiveLoad(
                                    dernierPoidsUtilisateur,
                                    decodeElastiques(bitmask, elastiques)
                                )

                                poidsParExercices.add(exercicesDuSuperset.indexOf(exSeanceSuperset), poidsEffectif * reps)
                                quantiteTotaleSoulevee += poidsEffectif * reps

                                seriesDuTour.add(
                                    SerieUi.PoidsDuCorps(
                                        reps = reps,
                                        bitmaskElastiques = bitmask,
                                        flemme = false
                                    )
                                )

                            } else {

                                val poids = previousSerie?.poids ?: 0f
                                val reps = previousSerie?.nombreReps ?: 0f

                                quantiteTotaleSoulevee += poids * reps

                                seriesDuTour.add(
                                    SerieUi.Fonte(
                                        poids = poids,
                                        reps = reps,
                                        flemme = false
                                    )
                                )
                            }
                        }

                        val numeroSuperset = supersetsDejaTraites.indexOf(supersetId) + 1

                        // 👉 Item factice représentant UN TOUR DE SUPERSET
                        items.add(
                            ExerciceSeanceItem(
                                exerciceName = "Superset $numeroSuperset — Série $numeroSerie",
                                muscleName = stringExercices,
                                series = seriesDuTour,
                                poidsSouleve = quantiteTotaleSoulevee,
                                poidsUtilisateur = dernierPoidsUtilisateur ?: 0f,
                                supersetData = SupersetOrigins(
                                    exercices = listeExercices,
                                    idSuperset = supersetId,
                                    nbTours = nbToursSuperset,
                                    poidsSouleveParExercices = poidsParExercices
                                )
                            )
                        )

                        listeExercices = emptyList()
                    }
                }
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

            val dernierPoidsUtilisateur = db.entreePoidsDao().getLatestWeight()?.poids

            handler.removeCallbacks(updateRunnable)

            val seanceHistorique = SeanceHistorique(
                idSeance = seanceId,
                date = Date(),
                dureeSecondes = secondsPassed
            )

            val idSeanceHistorique = db.seanceHistoriqueDao().insert(seanceHistorique).toInt()

            val items = exerciceAdapter.getItems()
            val seriesToInsert = mutableListOf<Serie>()

            var itemsToExercices: List<ExerciceSeanceItem> = emptyList()
            val supersetsDejaTraites = mutableSetOf<Int>()

            for(item in items)
            {
                if(item.supersetData == null)
                {
                    itemsToExercices = itemsToExercices + item
                }
                else
                {
                    val superSetId = item.supersetData.idSuperset

                    if(supersetsDejaTraites.contains(superSetId))
                        continue

                    supersetsDejaTraites.add(superSetId)

                    val toursDuSuperset = items.filter {
                        it.supersetData != null && it.supersetData.idSuperset == superSetId
                    }

                    var exercicesSeriesMap: MutableMap<Exercice, MutableList<SerieUi>> = mutableMapOf()
                    //Pour chaque VRAI exercice du superset...
                    for(exercice in item.supersetData.exercices) {
                        //...je boucle sur tous les FAUX items d'exercice
                        for (tourSuperset in toursDuSuperset) {
                            //pour en récupérer les séries effectuées
                            val index = item.supersetData.exercices.indexOf(exercice)

                            val serieUi = tourSuperset.series[index]

                            exercicesSeriesMap
                                .getOrPut(exercice) { mutableListOf() }
                                .add(serieUi)
                        }

                        //A partir de là, j'ai récupéré toutes les séries
                        itemsToExercices = itemsToExercices + ExerciceSeanceItem(
                            exerciceName = exercice.nom,
                            muscleName = item.muscleName,
                            series = exercicesSeriesMap[exercice] ?: mutableListOf(),
                            poidsSouleve = item.supersetData.poidsSouleveParExercices[item.supersetData.exercices.indexOf(exercice)],
                            poidsUtilisateur = dernierPoidsUtilisateur ?: 0f
                        )
                    }
                }
            }

            for (item in itemsToExercices) {
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

