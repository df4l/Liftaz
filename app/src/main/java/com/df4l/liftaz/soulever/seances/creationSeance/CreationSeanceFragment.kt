package com.df4l.liftaz.soulever.seances.creationSeance

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.df4l.liftaz.R
import com.df4l.liftaz.data.AppDatabase
import com.df4l.liftaz.data.ExerciceDao
import com.df4l.liftaz.data.ExerciceSeance
import com.df4l.liftaz.data.ExerciceSeanceDao
import com.df4l.liftaz.data.MuscleDao
import com.df4l.liftaz.data.Seance
import com.df4l.liftaz.data.SeanceDao
import com.df4l.liftaz.data.TypeFrequence
import com.df4l.liftaz.databinding.FragmentCreationseanceBinding
import com.df4l.liftaz.soulever.exercices.creationExercice.CreateExerciceDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CreationSeanceFragment : Fragment() {

    private var _binding: FragmentCreationseanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var muscleDao: MuscleDao
    private lateinit var exerciceDao: ExerciceDao
    private lateinit var seanceDao: SeanceDao
    private lateinit var exerciceSeanceDao: ExerciceSeanceDao

    private lateinit var recyclerView: RecyclerView
    private lateinit var exerciceSeanceAdapter: ExerciceSeanceAdapter

    private var idSeanceModif: Int? = null

    private val exerciceSeanceList = mutableListOf<ExerciceSeanceUi>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreationseanceBinding.inflate(inflater, container, false)

        database = AppDatabase.getDatabase(requireContext())
        muscleDao = database.muscleDao()
        exerciceDao = database.exerciceDao()
        seanceDao = database.seanceDao()
        exerciceSeanceDao = database.exerciceSeanceDao()

        binding.fabAddExercice.setOnClickListener { view ->
            CreateExerciceDialog(
                context = requireContext(),
                lifecycleScope = lifecycleScope,
                exerciceDao = exerciceDao,
                muscleDao = muscleDao,
                parentView = requireView()
            ).show()
        }

        binding.btnSauvegarderSeance.setOnClickListener {
            sauvegarderSeance()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddExercice: ImageButton = view.findViewById(R.id.btnAddExercice)

        btnAddExercice.setOnClickListener {
            addExerciceToSeance(0)
        }

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupFrequence)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupJours)
        val intervalLayout = view.findViewById<LinearLayout>(R.id.layoutIntervalle)
        val numberSlider = view.findViewById<Slider>(R.id.sliderIntervalle)
        val textProchaines = view.findViewById<TextView>(R.id.textProchainesSeances)
        var intervalleTexte = view.findViewById<TextView>(R.id.IntervalleTexte)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioJoursSemaine -> {
                    chipGroup.visibility = View.VISIBLE
                    intervalLayout.visibility = View.INVISIBLE

                }
                R.id.radioIntervalle -> {
                    chipGroup.visibility = View.INVISIBLE
                    intervalLayout.visibility = View.VISIBLE
                    updateNextDates(numberSlider.value.toInt(), textProchaines, intervalleTexte)
                }
            }
        }

        numberSlider.addOnChangeListener { _, value, _ ->
            updateNextDates(value.toInt(), textProchaines, intervalleTexte)
        }

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        exerciceSeanceAdapter = ExerciceSeanceAdapter(exerciceSeanceList) { position ->
            addExerciceToSeance(position)
        }
        recyclerView.adapter = exerciceSeanceAdapter

        idSeanceModif = arguments?.getInt("idSeance")

        if (idSeanceModif != null) {
            chargerSeanceExistante(idSeanceModif!!)

            Log.d("DEBUG_SEANCE", "Edition de la séance à l'ID $idSeanceModif")
        }

    }

    private fun chargerSeanceExistante(id: Int) {
        lifecycleScope.launch {
            val seance = seanceDao.getSeance(id)
            val exercicesSeance = exerciceSeanceDao.getExercicesForSeance(id)

            binding.editNomSeance.setText(seance.nom)

            // fréquence
            if (seance.typeFrequence == TypeFrequence.JOURS_SEMAINE) {
                binding.radioJoursSemaine.isChecked = true
                seance.joursSemaine?.forEach { jourIndex ->
                    (binding.chipGroupJours.getChildAt(jourIndex - 1) as Chip).isChecked = true
                }
            } else {
                binding.radioIntervalle.isChecked = true
                binding.sliderIntervalle.value = seance.intervalleJours?.toFloat() ?: 1f
            }

            // exercices
            exerciceSeanceList.clear()
            exercicesSeance.forEach {
                val ex = exerciceDao.getExerciceById(it.idExercice)
                val muscle = muscleDao.getMuscle(ex.idMuscleCible).first()

                if (ex.poidsDuCorps) {
                    exerciceSeanceList += ExerciceSeanceUi.PoidsDuCorps(
                        idExercice = ex.id,
                        nom = ex.nom,
                        muscle = muscle.nom,
                        series = it.nbSeries,
                        reps = it.minReps
                    )
                } else {
                    exerciceSeanceList += ExerciceSeanceUi.AvecFonte(
                        idExercice = ex.id,
                        nom = ex.nom,
                        muscle = muscle.nom,
                        series = it.nbSeries,
                        minReps = it.minReps,
                        maxReps = it.maxReps
                    )
                }

                Log.d("DEBUG_SEANCE", "Exercice ${ex.nom} series=${it.nbSeries} min=${it.minReps} max=${it.maxReps}")

            }

            exerciceSeanceAdapter.notifyDataSetChanged()
        }
    }


    private fun updateNextDates(interval: Int, textView: TextView, textView2: TextView) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH)

        val nextDates = List(3) { today.plusDays(((it + 1) * interval).toLong()) }

        val todayFormatted = today.format(formatter)
        val nextFormatted = nextDates.joinToString(", ") { it.format(formatter) }

        textView.text = "À compter d’aujourd’hui ($todayFormatted), prochaines séances :\n$nextFormatted"
        textView2.text = "Séance à effectuer tous les $interval jours"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun sauvegarderSeance() {
        val nomSeance = binding.editNomSeance.text.toString().trim()
        if (nomSeance.isBlank()) {
            binding.editNomSeance.error = "Nom requis"
            return
        }

        if (exerciceSeanceList.isEmpty()) {
            Toast.makeText(requireContext(), "Ajoute au moins un exercice à la séance.", Toast.LENGTH_SHORT).show()
            return
        }

        val typeFrequence: TypeFrequence
        var joursSemaine: List<Int>? = null
        var intervalle: Int? = null

        when (binding.radioGroupFrequence.checkedRadioButtonId) {
            R.id.radioJoursSemaine -> {
                typeFrequence = TypeFrequence.JOURS_SEMAINE

                val selected = binding.chipGroupJours.checkedChipIds
                joursSemaine = selected.map { chipId ->
                    val index = binding.chipGroupJours.indexOfChild(binding.chipGroupJours.findViewById(chipId))
                    index + 1 // Lundi = 1, Mardi = 2...
                }
            }

            R.id.radioIntervalle -> {
                typeFrequence = TypeFrequence.INTERVALLE
                intervalle = binding.sliderIntervalle.value.toInt()
            }

            else -> return
        }

        val dateAjout = java.sql.Date.valueOf(LocalDate.now().toString())

        lifecycleScope.launch {
            val idSeance = idSeanceModif?.also { id ->
                seanceDao.update(
                    Seance(
                        id = id,
                        nom = nomSeance,
                        typeFrequence = typeFrequence,
                        joursSemaine = joursSemaine,
                        intervalleJours = intervalle,
                        dateAjout = seanceDao.getSeance(id).dateAjout
                    )
                )

                // on supprime les anciens exercices
                exerciceSeanceDao.deleteExercicesForSeance(id)

            } ?: run {
                // ✅ Sinon → ajout
                seanceDao.insert(
                    Seance(
                        nom = nomSeance,
                        typeFrequence = typeFrequence,
                        joursSemaine = joursSemaine,
                        intervalleJours = intervalle,
                        dateAjout = dateAjout
                    )
                ).toInt()
            }

            exerciceSeanceList.forEachIndexed { index, exUi ->
                when (exUi) {
                    is ExerciceSeanceUi.AvecFonte -> {
                        Log.d(
                            "DEBUG_SAUVEGARDE",
                            "AvecFonte idExercice=${exUi.idExercice}, series=${exUi.series}, minReps=${exUi.minReps}, maxReps=${exUi.maxReps}"
                        )

                        exerciceSeanceDao.insert(
                            ExerciceSeance(
                                idSeance = idSeance,
                                idExercice = exUi.idExercice,
                                indexOrdre = index,
                                nbSeries = exUi.series,
                                minReps = exUi.minReps,
                                maxReps = exUi.maxReps
                            )
                        )
                    }

                    is ExerciceSeanceUi.PoidsDuCorps -> {
                        Log.d(
                            "DEBUG_SAUVEGARDE",
                            "PoidsDuCorps idExercice=${exUi.idExercice}, series=${exUi.series}, reps=${exUi.reps}"
                        )

                        exerciceSeanceDao.insert(
                            ExerciceSeance(
                                idSeance = idSeance,
                                idExercice = exUi.idExercice,
                                indexOrdre = index,
                                nbSeries = exUi.series,
                                minReps = exUi.reps,
                                maxReps = exUi.reps
                            )
                        )
                    }
                }
            }

            requireActivity().runOnUiThread {
                val msg = if (idSeanceModif != null) "Séance modifiée ✅" else "Séance sauvegardée ✅"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun addExerciceToSeance(position: Int) {
        ShowExercicesListDialog(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            exerciceDao = exerciceDao,
            muscleDao = muscleDao,
            parentView = requireView()
        ) { exercice ->
            viewLifecycleOwner.lifecycleScope.launch {
                val muscle = muscleDao.getMuscle(exercice.idMuscleCible).first()

                val nouvelExercice = if (exercice.poidsDuCorps) {
                    ExerciceSeanceUi.PoidsDuCorps(
                        idExercice = exercice.id,
                        nom = exercice.nom,
                        muscle = muscle.nom,
                    )
                } else {
                    ExerciceSeanceUi.AvecFonte(
                        idExercice = exercice.id,
                        nom = exercice.nom,
                        muscle = muscle.nom,
                    )
                }

                exerciceSeanceList.add(position, nouvelExercice)
                exerciceSeanceAdapter.notifyItemInserted(position)
            }
        }.show()
    }

}