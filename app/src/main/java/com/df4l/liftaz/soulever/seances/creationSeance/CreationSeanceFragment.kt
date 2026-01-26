package com.df4l.liftaz.soulever.seances.creationSeance

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.graphics.Color
import androidx.core.graphics.toColor
import kotlin.random.Random

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
    private var dateDebut: LocalDate? = null

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

        val btnAddExercice: ImageButton = view.findViewById(R.id.btnAddPlus)

        btnAddExercice.setOnClickListener {
            addExerciceToSeance(0)
        }

        binding.ajouterExerciceImportBottom.btnAddPlus.setOnClickListener {
            addExerciceToSeance(exerciceSeanceList.size)
        }

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupFrequence)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupJours)
        val intervalLayout = view.findViewById<LinearLayout>(R.id.layoutIntervalle)
        val numberSlider = view.findViewById<Slider>(R.id.sliderIntervalle)
        val textProchaines = view.findViewById<TextView>(R.id.textProchainesSeances)
        val intervalleTexte = view.findViewById<TextView>(R.id.IntervalleTexte)

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

        binding.editDateDebut.setOnClickListener {
            val today = LocalDate.now()
            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // ✅ LocalDate.of utilise bien (année, mois, jour)
                    // month dans DatePickerDialog commence à 0 → on ajoute +1
                    dateDebut = LocalDate.of(year, month + 1, dayOfMonth)

                    // Mettre à jour le texte affiché
                    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)
                    binding.editDateDebut.setText(dateDebut?.format(formatter))

                    // Mettre à jour l'affichage des prochaines dates
                    val interval = binding.sliderIntervalle.value.toInt()
                    updateNextDates(interval, binding.textProchainesSeances, binding.IntervalleTexte, dateDebut)

                    // ✅ Log pour vérifier la date choisie
                    Log.d("DEBUG_DATE_PICKER", "Date choisie = $dateDebut")
                },
                today.year, today.monthValue - 1, today.dayOfMonth
            )
            dpd.show()
        }

        binding.editDateDebut.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)
                s?.toString()?.let { text ->
                    try {
                        dateDebut = LocalDate.parse(text, formatter)

                        val interval = binding.sliderIntervalle.value.toInt()
                        updateNextDates(interval, binding.textProchainesSeances, binding.IntervalleTexte, dateDebut)

                        Log.d("DEBUG_EDITTEXT", "dateDebut = $dateDebut")
                    } catch (e: Exception) {
                        Log.e("DEBUG_EDITTEXT", "Impossible de parser la date : $text")
                        dateDebut = null
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        exerciceSeanceAdapter = ExerciceSeanceAdapter(
            exerciceSeanceList,
            onAddClick = { position ->
                addExerciceToSeance(position, true)
                toggleBottomAddButton()
            },
            onLongPress = { position ->
                toggleSuperset(position)
                toggleBottomAddButton()
            }
        )

        recyclerView.adapter = exerciceSeanceAdapter

        idSeanceModif = arguments?.getInt("idSeance")

        if (idSeanceModif != null) {
            chargerSeanceExistante(idSeanceModif!!)

            Log.d("DEBUG_SEANCE", "Edition de la séance à l'ID $idSeanceModif")
        }

    }

    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    fun randomPaleColor(): Int {
        val hue = Random.nextFloat() * 360f      // teinte
        val saturation = 0.25f + Random.nextFloat() * 0.15f  // faible saturation

        var value = 0f
        if(!isDarkMode(requireContext()))
            value = 0.9f + Random.nextFloat() * 0.1f          // très clair
        else
            value = 0.2f + Random.nextFloat() * 0.2f //très sombre

        return Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }


    private fun toggleSuperset(position: Int) {
        val exercice = exerciceSeanceList[position]

        if (exercice.idSuperset == null) {
            // ✅ devient un superset
            exercice.idSuperset = System.currentTimeMillis().toInt()
            exercice.superSetColor = randomPaleColor()
            exercice.superSetData = SupersetData()
        } else {
            // ❌ on enlève le superset
            exercice.idSuperset = null
            exercice.superSetColor = null
            exercice.superSetData = null
        }

        exerciceSeanceAdapter.notifyItemChanged(position)
    }

    private val supersetColorMap = mutableMapOf<Int, Int>()
    private fun getOrCreateSupersetColor(supersetId: Int): Int {
        return supersetColorMap.getOrPut(supersetId) {
            randomPaleColor()
        }
    }
    private val supersetDataMap = mutableMapOf<Int, SupersetData>()
    private fun getOrCreateSupersetData(
        supersetId: Int,
        initialSeries: Int
    ): SupersetData {
        return supersetDataMap.getOrPut(supersetId) {
            SupersetData(series = initialSeries)
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

                val dateLocal = seance.dateAjout.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)
                binding.editDateDebut.setText(dateLocal.format(formatter))
            }

            // exercices
            exerciceSeanceList.clear()

            supersetColorMap.clear()
            supersetDataMap.clear()

            exercicesSeance.forEach {
                val ex = exerciceDao.getExerciceById(it.idExercice)
                val muscle = muscleDao.getMuscle(ex.idMuscleCible).first()

                val ajoutEx = if (ex.poidsDuCorps) {
                    ExerciceSeanceUi.PoidsDuCorps(
                        idExercice = ex.id,
                        nom = ex.nom,
                        muscle = muscle.nom,
                        series = it.nbSeries,
                        reps = it.minReps,
                    )
                } else {
                    ExerciceSeanceUi.AvecFonte(
                        idExercice = ex.id,
                        nom = ex.nom,
                        muscle = muscle.nom,
                        series = it.nbSeries,
                        minReps = it.minReps,
                        maxReps = it.maxReps,
                    )
                }

                if (it.idSuperset != null) {
                    val supersetId = it.idSuperset!!

                    ajoutEx.idSuperset = supersetId
                    ajoutEx.superSetColor = getOrCreateSupersetColor(supersetId)
                    ajoutEx.superSetData =
                        getOrCreateSupersetData(supersetId, it.nbSeries)
                }

                exerciceSeanceList += ajoutEx

                Log.d("DEBUG_SEANCE", "Exercice ${ex.nom} series=${it.nbSeries} min=${it.minReps} max=${it.maxReps}")

            }

            exerciceSeanceAdapter.notifyDataSetChanged()
            toggleBottomAddButton()
        }
    }


    private fun updateNextDates(interval: Int, textView: TextView, textView2: TextView, startDate: LocalDate? = null) {
        val start = startDate ?: LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH)
        val nextDates = List(3) { start.plusDays(((it + 1) * interval).toLong()) }

        val startFormatted = start.format(formatter)
        val nextFormatted = nextDates.joinToString(", ") { it.format(formatter) }

        textView.text = "À compter du $startFormatted, prochaines séances :\n$nextFormatted"
        textView2.text = "Séance à effectuer tous les $interval jours"
    }

    fun toggleBottomAddButton()
    {
        if(exerciceSeanceList.isEmpty()) {
            binding.ajouterExerciceImportBottom.root.visibility = View.GONE
            return
        }

        if(exerciceSeanceList.last().idSuperset == null)
            binding.ajouterExerciceImportBottom.root.visibility = View.GONE
        else
            binding.ajouterExerciceImportBottom.root.visibility = View.VISIBLE
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

        val dateAjout = dateDebut ?: LocalDate.now()
        val dateAjoutSql = java.sql.Date.valueOf(dateAjout.toString())

        Log.d("DEBUG_SAUVEGARDE", "Date sauvegardée = $dateAjoutSql")

        lifecycleScope.launch {
            val idSeance = idSeanceModif?.also { id ->

                val seanceExistante = seanceDao.getSeance(id)

                seanceDao.update(
                    Seance(
                        id = id,
                        nom = nomSeance,
                        typeFrequence = typeFrequence,
                        joursSemaine = joursSemaine,
                        intervalleJours = intervalle,
                        dateAjout = dateAjoutSql,
                        idProgramme = seanceExistante.idProgramme
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
                        dateAjout = dateAjoutSql
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

                        val nbSeries = if (exUi.idSuperset == null) {
                            exUi.series
                        } else {
                            exUi.superSetData?.series ?: exUi.series
                        }

                        exerciceSeanceDao.insert(
                            ExerciceSeance(
                                idSeance = idSeance,
                                idExercice = exUi.idExercice,
                                indexOrdre = index,
                                nbSeries = nbSeries,
                                minReps = exUi.minReps,
                                maxReps = exUi.maxReps,
                                idSuperset = exUi.idSuperset
                            )
                        )
                    }

                    is ExerciceSeanceUi.PoidsDuCorps -> {
                        Log.d(
                            "DEBUG_SAUVEGARDE",
                            "PoidsDuCorps idExercice=${exUi.idExercice}, series=${exUi.series}, reps=${exUi.reps}"
                        )

                        val nbSeries = if (exUi.idSuperset == null) {
                            exUi.series
                        } else {
                            exUi.superSetData?.series ?: exUi.series
                        }

                        exerciceSeanceDao.insert(
                            ExerciceSeance(
                                idSeance = idSeance,
                                idExercice = exUi.idExercice,
                                indexOrdre = index,
                                nbSeries = nbSeries,
                                minReps = exUi.reps,
                                maxReps = exUi.reps,
                                idSuperset = exUi.idSuperset
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

    private fun addExerciceToSeance(position: Int, calledFromAdapter: Boolean = false) {
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

                if(calledFromAdapter && exerciceSeanceList[position-1].idSuperset != null)
                {
                    nouvelExercice.idSuperset = exerciceSeanceList[position-1].idSuperset
                    nouvelExercice.superSetColor = exerciceSeanceList[position-1].superSetColor
                    nouvelExercice.superSetData = exerciceSeanceList[position-1].superSetData
                }

                exerciceSeanceList.add(position, nouvelExercice)
                exerciceSeanceAdapter.notifyItemInserted(position)
            }
        }.show()
    }

}